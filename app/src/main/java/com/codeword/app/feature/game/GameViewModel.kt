package com.codeword.app.feature.game

import androidx.lifecycle.ViewModel
import com.codeword.app.core.game.GameEngine
import com.codeword.app.core.game.GameResult
import com.codeword.app.core.model.Card
import com.codeword.app.core.model.GamePhase
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.Score
import com.codeword.app.core.model.WinReason
import com.codeword.app.data.WordPackProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(buildInitialState(Role.SPYMASTER, Team.RED))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun init(myRole: Role, myTeam: Team) {
        _uiState.value = buildInitialState(myRole, myTeam)
    }

    fun onClueSubmit(word: String, count: Int) {
        val clue = GameEngine.submitClue(word, count)
        // count == 0 means unlimited: use Int.MAX_VALUE so decrement logic stays uniform
        val guessesLeft = if (count == 0) Int.MAX_VALUE else count
        _uiState.update { it.copy(clue = clue, phase = GamePhase.GUESS, guessesLeft = guessesLeft) }
    }

    fun onCardTap(card: Card) {
        val state = _uiState.value
        if (state.winner != null || state.phase != GamePhase.GUESS) return

        val (updatedBoard, result) = GameEngine.applyGuess(state.board, card, state.currentTeam)

        _uiState.update {
            when (result) {
                is GameResult.Correct -> {
                    if (state.guessesLeft > 1) {
                        it.copy(board = updatedBoard, score = result.score, guessesLeft = state.guessesLeft - 1)
                    } else {
                        it.passTurn(updatedBoard, result.score)
                    }
                }
                is GameResult.WrongTeam -> it.passTurn(updatedBoard, result.score)
                is GameResult.Assassin -> it.copy(
                    board = updatedBoard,
                    winner = state.currentTeam.opposite(),
                    winReason = WinReason.ASSASSIN,
                )
                is GameResult.Win -> it.copy(
                    board = updatedBoard,
                    score = result.score,
                    winner = result.winner,
                    winReason = WinReason.ALL_CARDS,
                )
            }
        }
    }

    fun resetGame() {
        val state = _uiState.value
        _uiState.value = buildInitialState(state.myRole, state.myTeam)
    }

    private fun GameUiState.passTurn(updatedBoard: List<Card>, score: Score) = copy(
        board = updatedBoard,
        score = score,
        phase = GamePhase.CLUE,
        currentTeam = currentTeam.opposite(),
        clue = null,
        guessesLeft = 0,
    )

    private fun buildInitialState(myRole: Role, myTeam: Team): GameUiState {
        val words = WordPackProvider.getWords(locale = "ru", packId = "ru_base")
        val startingTeam = if ((0..1).random() == 0) Team.RED else Team.BLUE
        val board = GameEngine.generateBoard(words, startingTeam)
        return GameUiState(
            board = board,
            currentTeam = startingTeam,
            phase = GamePhase.CLUE,
            clue = null,
            guessesLeft = 0,
            score = GameEngine.calculateScore(board),
            winner = null,
            winReason = null,
            myRole = myRole,
            myTeam = myTeam,
        )
    }
}
