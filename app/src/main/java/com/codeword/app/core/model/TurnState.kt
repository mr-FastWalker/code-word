package com.codeword.app.core.model

data class TurnState(
    val currentTeam: Team,
    val phase: GamePhase,
    val clue: Clue? = null
)
