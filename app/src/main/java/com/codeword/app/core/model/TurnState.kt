package com.codeword.app.core.model

data class TurnState(
    val currentTeam: Team = Team.RED,
    val phase: GamePhase = GamePhase.CLUE,
    val clue: Clue? = null,
    val guessesMade: Int = 0,
)
