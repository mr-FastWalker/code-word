package com.codeword.app.core.game

import com.codeword.app.core.model.Card
import com.codeword.app.core.model.Score
import com.codeword.app.core.model.Team

sealed class GameResult {
    data class Correct(val card: Card, val score: Score) : GameResult()
    data class WrongTeam(val card: Card, val score: Score) : GameResult()
    data class Assassin(val card: Card) : GameResult()
    data class Win(val winner: Team, val card: Card, val score: Score) : GameResult()
}
