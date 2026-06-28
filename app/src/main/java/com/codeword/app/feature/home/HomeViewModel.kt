package com.codeword.app.feature.home

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codeword.app.data.RoomError
import com.codeword.app.data.RoomRepository
import com.codeword.app.data.RoomRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Application.prefs by preferencesDataStore(name = "home_prefs")
private val KEY_NAME = stringPreferencesKey("player_name")

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class Done(val code: String) : HomeState()
    data class Error(val message: String) : HomeState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val roomRepository: RoomRepository = RoomRepositoryImpl()

    private val _state = MutableStateFlow<HomeState>(HomeState.Idle)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = getApplication<Application>().prefs.data.first()[KEY_NAME] ?: ""
            if (_name.value.isEmpty()) _name.value = saved
        }
    }

    fun onNameChange(value: String) {
        _name.value = value
        viewModelScope.launch {
            getApplication<Application>().prefs.edit { it[KEY_NAME] = value }
        }
    }

    fun createRoom(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) { _state.value = HomeState.Error("Введите имя"); return }
        viewModelScope.launch {
            _state.value = HomeState.Loading
            try {
                val uid = requireUid()
                val code = roomRepository.createRoom(uid, trimmed)
                _state.value = HomeState.Done(code)
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Ошибка создания комнаты")
            }
        }
    }

    fun joinRoom(code: String, name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) { _state.value = HomeState.Error("Введите имя"); return }
        val trimmedCode = code.trim().uppercase()
        if (trimmedCode.length != 6) { _state.value = HomeState.Error("Код — 6 символов"); return }
        viewModelScope.launch {
            _state.value = HomeState.Loading
            try {
                val uid = requireUid()
                roomRepository.joinRoom(trimmedCode, uid, trimmedName)
                _state.value = HomeState.Done(trimmedCode)
            } catch (e: RoomError.NotFound) {
                _state.value = HomeState.Error("Комната '$trimmedCode' не найдена")
            } catch (e: RoomError.GameInProgress) {
                _state.value = HomeState.Error("Комната закрыта — игра уже идёт")
            } catch (e: RoomError.NotWaiting) {
                _state.value = HomeState.Error("Игра в этой комнате уже завершена")
            } catch (e: Exception) {
                _state.value = HomeState.Error(e.message ?: "Ошибка")
            }
        }
    }

    fun resetError() {
        if (_state.value is HomeState.Error) _state.value = HomeState.Idle
    }

    fun reset() {
        _state.value = HomeState.Idle
    }

    private fun requireUid(): String =
        FirebaseAuth.getInstance().currentUser?.uid ?: error("Not signed in")
}
