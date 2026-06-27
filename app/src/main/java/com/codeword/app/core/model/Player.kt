package com.codeword.app.core.model

data class Player(
    val uid: String,
    val name: String,
    val team: Team,
    val role: Role,
    val connected: Boolean = true
)
