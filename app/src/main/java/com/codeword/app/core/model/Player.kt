package com.codeword.app.core.model

data class Player(
    val uid: String = "",
    val name: String = "",
    val team: Team = Team.RED,
    val role: Role = Role.SPECTATOR,
    val connected: Boolean = true,
    val ready: Boolean = false,
)
