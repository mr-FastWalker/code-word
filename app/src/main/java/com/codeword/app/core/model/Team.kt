package com.codeword.app.core.model

enum class Team {
    RED, BLUE;

    fun opposite(): Team = if (this == RED) BLUE else RED
}
