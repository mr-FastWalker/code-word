package com.codeword.app.core.model

data class Card(
    val id: Int,
    val word: String,
    val color: CardColor,
    val revealed: Boolean = false
)
