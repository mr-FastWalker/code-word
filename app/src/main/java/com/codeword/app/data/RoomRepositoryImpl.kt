package com.codeword.app.data

import com.codeword.app.core.model.Card
import com.codeword.app.core.model.CardColor
import com.codeword.app.core.model.Clue
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Room
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.WinReason
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RoomRepositoryImpl : RoomRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val rooms = firestore.collection("rooms")

    // ─── Создание комнаты ────────────────────────────────────────────────────

    override suspend fun createRoom(hostUid: String, hostName: String): String {
        repeat(MAX_CREATE_ATTEMPTS) {
            val code = generateRoomCode()
            val docRef = rooms.document(code)

            val created = firestore.runTransaction { tx ->
                if (!tx.get(docRef).exists()) {
                    tx.set(docRef, initialRoomData(code, hostUid, hostName))
                    true
                } else false
            }.await()

            if (created) return code
        }
        throw RoomError.CreateFailed()
    }

    private fun initialRoomData(code: String, hostUid: String, hostName: String) = mapOf(
        "schemaVersion" to 1,
        "hostUid" to hostUid,
        "status" to "waiting",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
        "config" to mapOf("locale" to "ru", "wordPackId" to "ru_base", "skinId" to "classic"),
        "cards" to emptyList<Any>(),
        "startingTeam" to "red",
        "turn" to null,
        "players" to mapOf(
            hostUid to mapOf(
                "name" to hostName,
                "team" to "red",
                "role" to "operative",
                "connected" to true,
                "ready" to false,
            )
        ),
        "score" to mapOf("redLeft" to 0, "blueLeft" to 0),
        "winner" to null,
        "winReason" to null,
    )

    // ─── Вход в комнату ──────────────────────────────────────────────────────

    override suspend fun joinRoom(code: String, uid: String, name: String) {
        val docRef = rooms.document(code)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) throw RoomError.NotFound(code)
        if (snapshot.getString("status") != "waiting") throw RoomError.NotWaiting(code)

        docRef.update(
            mapOf(
                "players.$uid" to mapOf(
                    "name" to name,
                    "team" to "red",
                    "role" to "operative",
                    "connected" to true,
                    "ready" to false,
                ),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    // ─── Realtime listener ───────────────────────────────────────────────────

    override fun observeRoom(code: String): Flow<Room?> = callbackFlow {
        val listener = rooms.document(code).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.takeIf { it.exists() }?.toRoom())
        }
        awaitClose { listener.remove() }
    }

    // ─── Лобби ───────────────────────────────────────────────────────────────

    override suspend fun claimSlot(code: String, uid: String, role: Role, team: Team) {
        rooms.document(code).update(
            mapOf(
                "players.$uid.role" to role.name.lowercase(),
                "players.$uid.team" to team.name.lowercase(),
                "players.$uid.ready" to false,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    override suspend fun setReady(code: String, uid: String, isReady: Boolean) {
        rooms.document(code).update(
            mapOf(
                "players.$uid.ready" to isReady,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    override suspend fun startGame(code: String, uid: String, board: List<Card>, startingTeam: Team) {
        val redLeft = board.count { it.color == CardColor.RED }
        val blueLeft = board.count { it.color == CardColor.BLUE }

        rooms.document(code).update(
            mapOf(
                "status" to "playing",
                "cards" to board.map { it.toFirestoreMap() },
                "startingTeam" to startingTeam.name.lowercase(),
                "turn" to mapOf(
                    "currentTeam" to startingTeam.name.lowercase(),
                    "phase" to "clue",
                    "clue" to null,
                    "guessesMade" to 0,
                ),
                "score" to mapOf("redLeft" to redLeft, "blueLeft" to blueLeft),
                "winner" to null,
                "winReason" to null,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    // ─── Игровые действия ────────────────────────────────────────────────────

    override suspend fun submitClue(code: String, clue: Clue) {
        rooms.document(code).update(
            mapOf(
                "turn.clue" to clue.toFirestoreMap(),
                "turn.phase" to "guess",
                "turn.guessesMade" to 0,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    override suspend fun revealCard(code: String, card: Card, guessesMade: Int) {
        val docRef = rooms.document(code)
        firestore.runTransaction { tx ->
            val snapshot = tx.get(docRef)
            @Suppress("UNCHECKED_CAST")
            val rawCards = snapshot.get("cards") as? List<Map<String, Any>> ?: return@runTransaction
            val updated = rawCards.map { c ->
                if ((c["id"] as? Long)?.toInt() == card.id) c + ("revealed" to true) else c
            }
            tx.update(
                docRef, mapOf(
                    "cards" to updated,
                    "turn.guessesMade" to guessesMade,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
            )
        }.await()
    }

    override suspend fun passTurn(code: String, nextTeam: Team) {
        rooms.document(code).update(
            mapOf(
                "turn.currentTeam" to nextTeam.name.lowercase(),
                "turn.phase" to "clue",
                "turn.clue" to null,
                "turn.guessesMade" to 0,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    override suspend fun endGame(code: String, winner: Team, winReason: WinReason) {
        rooms.document(code).update(
            mapOf(
                "status" to "finished",
                "winner" to winner.name.lowercase(),
                "winReason" to winReason.name.lowercase(),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    // ─── Вспомогательное ─────────────────────────────────────────────────────

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..ROOM_CODE_LENGTH).map { chars.random() }.joinToString("")
    }

    companion object {
        private const val ROOM_CODE_LENGTH = 6
        private const val MAX_CREATE_ATTEMPTS = 5
    }
}
