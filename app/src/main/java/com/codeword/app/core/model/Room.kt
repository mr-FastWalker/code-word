package com.codeword.app.core.model

data class Room(
    val id: String = "",
    val schemaVersion: Int = 1,
    val hostUid: String = "",
    val status: GameStatus = GameStatus.WAITING,
    val config: GameConfig = GameConfig(),
    val cards: List<Card> = emptyList(),
    val startingTeam: Team = Team.RED,
    val turn: TurnState = TurnState(Team.RED, GamePhase.CLUE),
    val players: Map<String, Player> = emptyMap(),
    val score: Score = Score(0, 0),
    val winner: Team? = null,
    val winReason: WinReason? = null,
)
