package com.codeword.app.feature.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codeword.app.core.model.Card
import com.codeword.app.core.model.CardColor
import com.codeword.app.core.model.Clue
import com.codeword.app.core.model.GamePhase
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Room
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason
import com.codeword.app.data.RoomRepository
import com.codeword.app.data.RoomRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(
    private val roomCode: String,
    private val uid: String,
    private val roomRepository: RoomRepository = RoomRepositoryImpl(),
) : ViewModel() {

    private val _room = MutableStateFlow<Room?>(null)

    val uiState: StateFlow<GameUiState?> = _room
        .map { it?.toGameUiState(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            roomRepository.observeRoom(roomCode).collect { _room.value = it }
        }
    }

    fun onClueSubmit(word: String, count: Int) {
        viewModelScope.launch {
            roomRepository.submitClue(roomCode, Clue(word, count))
        }
    }

    fun onCardTap(card: Card) {
        val room = _room.value ?: return
        val player = room.players[uid] ?: return

        if (room.turn.currentTeam != player.team) return
        if (player.role != Role.OPERATIVE) return
        if (room.turn.phase != GamePhase.GUESS) return
        if (card.revealed) return

        viewModelScope.launch {
            val clueCount = room.turn.clue?.count ?: 0
            val maxGuesses = if (clueCount == 0) Int.MAX_VALUE else clueCount + 1
            val newGuessesMade = room.turn.guessesMade + 1
            val currentTeam = room.turn.currentTeam
            val isMyCard = (currentTeam == Team.RED && card.color == CardColor.RED) ||
                    (currentTeam == Team.BLUE && card.color == CardColor.BLUE)

            when {
                card.color == CardColor.ASSASSIN -> {
                    roomRepository.revealCard(roomCode, card, newGuessesMade)
                    roomRepository.endGame(roomCode, currentTeam.opposite(), WinReason.ASSASSIN)
                }

                isMyCard -> {
                    // score is derived from cards in mapper, compute new value for win-check
                    val teamCardsLeft = when (currentTeam) {
                        Team.RED -> room.score.redLeft - 1
                        Team.BLUE -> room.score.blueLeft - 1
                    }
                    when {
                        teamCardsLeft <= 0 -> {
                            roomRepository.revealCard(roomCode, card, newGuessesMade)
                            roomRepository.endGame(roomCode, currentTeam, WinReason.ALL_CARDS)
                        }
                        newGuessesMade >= maxGuesses -> {
                            roomRepository.revealCard(roomCode, card, newGuessesMade)
                            roomRepository.passTurn(roomCode, currentTeam.opposite())
                        }
                        else -> roomRepository.revealCard(roomCode, card, newGuessesMade)
                    }
                }

                else -> { // wrong team or neutral
                    roomRepository.revealCard(roomCode, card, newGuessesMade)
                    roomRepository.passTurn(roomCode, currentTeam.opposite())
                }
            }
        }
    }

    companion object {
        fun factory(roomCode: String, uid: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                GameViewModel(roomCode, uid) as T
        }
    }
}

private fun Room.toGameUiState(uid: String): GameUiState? {
    val player = players[uid] ?: return null
    val clueCount = turn.clue?.count ?: 0
    val guessesLeft = if (clueCount == 0) Int.MAX_VALUE
    else (clueCount + 1 - turn.guessesMade).coerceAtLeast(0)
    return GameUiState(
        board = cards,
        currentTeam = turn.currentTeam,
        phase = turn.phase,
        clue = turn.clue,
        guessesLeft = guessesLeft,
        score = score,
        winner = winner,
        winReason = winReason,
        myRole = player.role,
        myTeam = player.team,
    )
}
