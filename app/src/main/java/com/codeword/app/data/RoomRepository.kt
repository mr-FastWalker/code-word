package com.codeword.app.data

import com.codeword.app.core.model.Card
import com.codeword.app.core.model.Clue
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Room
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason
import kotlinx.coroutines.flow.Flow

interface RoomRepository {

    /** Создать новую комнату; вернуть 6-символьный код. */
    suspend fun createRoom(hostUid: String, hostName: String): String

    /** Присоединиться к существующей комнате по коду (без выбора роли). */
    suspend fun joinRoom(code: String, uid: String, name: String)

    /** Realtime-поток состояния комнаты. null = документ не найден. */
    fun observeRoom(code: String): Flow<Room?>

    // ─── Лобби (Milestone 5) ────────────────────────────────────────────

    /** Занять слот: выбрать команду и роль. */
    suspend fun claimSlot(code: String, uid: String, role: Role, team: Team)

    /** Обозначить готовность / снять её. */
    suspend fun setReady(code: String, uid: String, isReady: Boolean)

    /** Хост запускает игру: записывает борд и меняет статус на playing. */
    suspend fun startGame(code: String, uid: String, board: List<Card>, startingTeam: Team)

    // ─── Игровые действия (Milestone 5) ─────────────────────────────────

    suspend fun submitClue(code: String, clue: Clue)

    suspend fun revealCard(code: String, card: Card, guessesMade: Int)

    /** Передать ход следующей команде: сбрасывает phase→CLUE, clue→null, guessesMade→0. */
    suspend fun passTurn(code: String, nextTeam: Team)

    suspend fun endGame(code: String, winner: Team, winReason: WinReason)
}

// ─── Ошибки ─────────────────────────────────────────────────────────────────

sealed class RoomError(message: String) : Exception(message) {
    class NotFound(code: String) : RoomError("Room '$code' not found")
    class NotWaiting(code: String) : RoomError("Room '$code' is not in waiting state")
    class CreateFailed : RoomError("Failed to create room after multiple attempts")
}
