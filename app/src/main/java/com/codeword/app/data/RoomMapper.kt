package com.codeword.app.data

import com.codeword.app.core.model.Card
import com.codeword.app.core.model.CardColor
import com.codeword.app.core.model.Clue
import com.codeword.app.core.model.GameConfig
import com.codeword.app.core.model.GamePhase
import com.codeword.app.core.model.GameStatus
import com.codeword.app.core.model.Player
import com.codeword.app.core.model.Role
import com.codeword.app.core.model.Room
import com.codeword.app.core.model.Score
import com.codeword.app.core.model.Team
import com.codeword.app.core.model.TurnState
import com.codeword.app.core.model.WinReason
import com.google.firebase.firestore.DocumentSnapshot

// ─── DocumentSnapshot → Room ─────────────────────────────────────────────────

fun DocumentSnapshot.toRoom(): Room {
    val data = data ?: return Room(id = id)

    @Suppress("UNCHECKED_CAST")
    val playersMap = data["players"] as? Map<String, Any> ?: emptyMap()

    @Suppress("UNCHECKED_CAST")
    val cardsRaw = data["cards"] as? List<Map<String, Any>> ?: emptyList()

    @Suppress("UNCHECKED_CAST")
    val turnMap = data["turn"] as? Map<String, Any>

    @Suppress("UNCHECKED_CAST")
    val configMap = data["config"] as? Map<String, Any> ?: emptyMap()

    val cards = cardsRaw.map { it.toCard() }

    return Room(
        id = id,
        schemaVersion = (data["schemaVersion"] as? Long)?.toInt() ?: 1,
        hostUid = data["hostUid"] as? String ?: "",
        status = (data["status"] as? String).toGameStatus(),
        config = configMap.toGameConfig(),
        cards = cards,
        startingTeam = (data["startingTeam"] as? String).toTeam(),
        turn = turnMap?.toTurnState() ?: TurnState(),
        players = playersMap.mapValues { (uid, v) ->
            @Suppress("UNCHECKED_CAST")
            (v as Map<String, Any>).toPlayer(uid)
        },
        // Score derived from cards so it's always consistent with revealed state
        score = Score(
            redLeft = cards.count { it.color == CardColor.RED && !it.revealed },
            blueLeft = cards.count { it.color == CardColor.BLUE && !it.revealed },
        ),
        winner = (data["winner"] as? String)?.toTeamOrNull(),
        winReason = (data["winReason"] as? String)?.toWinReasonOrNull(),
        isPrivate = data["isPrivate"] as? Boolean ?: false,
    )
}

// ─── Sub-map converters ───────────────────────────────────────────────────────

private fun Map<String, Any>.toGameConfig() = GameConfig(
    locale = this["locale"] as? String ?: "ru",
    wordPackId = this["wordPackId"] as? String ?: "ru_base",
    skinId = this["skinId"] as? String ?: "classic",
)

private fun Map<String, Any>.toCard() = Card(
    id = (this["id"] as? Long)?.toInt() ?: 0,
    word = this["word"] as? String ?: "",
    color = (this["color"] as? String).toCardColor(),
    revealed = this["revealed"] as? Boolean ?: false,
)

private fun Map<String, Any>.toTurnState(): TurnState {
    @Suppress("UNCHECKED_CAST")
    val clueMap = this["clue"] as? Map<String, Any>
    return TurnState(
        currentTeam = (this["currentTeam"] as? String).toTeam(),
        phase = (this["phase"] as? String).toGamePhase(),
        clue = clueMap?.let {
            Clue(
                word = it["word"] as? String ?: "",
                count = (it["count"] as? Long)?.toInt() ?: 1,
            )
        },
        guessesMade = (this["guessesMade"] as? Long)?.toInt() ?: 0,
    )
}

private fun Map<String, Any>.toPlayer(uid: String) = Player(
    uid = uid,
    name = this["name"] as? String ?: "",
    team = (this["team"] as? String).toTeam(),
    role = (this["role"] as? String).toRole(),
    connected = this["connected"] as? Boolean ?: true,
    ready = this["ready"] as? Boolean ?: false,
)

// ─── Enum helpers ─────────────────────────────────────────────────────────────

private fun String?.toGameStatus() = when (this) {
    "playing" -> GameStatus.PLAYING
    "finished" -> GameStatus.FINISHED
    else -> GameStatus.WAITING
}

private fun String?.toTeam() = when (this) {
    "blue" -> Team.BLUE
    else -> Team.RED
}

private fun String?.toTeamOrNull() = when (this) {
    "red" -> Team.RED
    "blue" -> Team.BLUE
    else -> null
}

private fun String?.toGamePhase() = when (this) {
    "guess" -> GamePhase.GUESS
    else -> GamePhase.CLUE
}

private fun String?.toRole() = when (this) {
    "spymaster" -> Role.SPYMASTER
    "operative" -> Role.OPERATIVE
    else -> Role.SPECTATOR
}

private fun String?.toCardColor() = when (this) {
    "red" -> CardColor.RED
    "blue" -> CardColor.BLUE
    "assassin" -> CardColor.ASSASSIN
    else -> CardColor.NEUTRAL
}

private fun String?.toWinReasonOrNull() = when (this) {
    "all_cards" -> WinReason.ALL_CARDS
    "assassin" -> WinReason.ASSASSIN
    else -> null
}

// ─── Domain → Firestore Map ───────────────────────────────────────────────────

fun Card.toFirestoreMap(): Map<String, Any> = mapOf(
    "id" to id,
    "word" to word,
    "color" to color.name.lowercase(),
    "revealed" to revealed,
)

fun Clue.toFirestoreMap(): Map<String, Any> = mapOf(
    "word" to word,
    "count" to count,
)

fun Player.toFirestoreMap(): Map<String, Any> = mapOf(
    "name" to name,
    "team" to team.name.lowercase(),
    "role" to role.name.lowercase(),
    "connected" to connected,
    "ready" to ready,
)

