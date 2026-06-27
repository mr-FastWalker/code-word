package com.codeword.app.feature.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codeword.app.core.game.GameEngine
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Room
import com.codeword.app.core.model.Team
import com.codeword.app.data.RoomRepository
import com.codeword.app.data.RoomRepositoryImpl
import com.codeword.app.data.WordPackProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LobbyViewModel(
    val roomCode: String,
    val uid: String,
    private val roomRepository: RoomRepository = RoomRepositoryImpl(),
) : ViewModel() {

    val room: StateFlow<Room?> = roomRepository
        .observeRoom(roomCode)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun claimSlot(role: Role, team: Team) {
        viewModelScope.launch { roomRepository.claimSlot(roomCode, uid, role, team) }
    }

    fun toggleReady() {
        viewModelScope.launch {
            val currentReady = room.value?.players?.get(uid)?.ready ?: false
            roomRepository.setReady(roomCode, uid, !currentReady)
        }
    }

    fun startGame() {
        viewModelScope.launch {
            val room = room.value ?: return@launch
            val words = WordPackProvider.getWords(room.config.locale, room.config.wordPackId)
            val startingTeam = if ((0..1).random() == 0) Team.RED else Team.BLUE
            val board = GameEngine.generateBoard(words, startingTeam)
            roomRepository.startGame(roomCode, uid, board, startingTeam)
        }
    }

    companion object {
        fun factory(roomCode: String, uid: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LobbyViewModel(roomCode, uid) as T
        }
    }
}
