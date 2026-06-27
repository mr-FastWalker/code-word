package com.codeword.app.core.game

import com.codeword.app.core.model.Card
import com.codeword.app.core.model.CardColor
import com.codeword.app.core.model.Clue
import com.codeword.app.core.model.Score
import com.codeword.app.core.model.Team

object GameEngine {

    private const val BOARD_SIZE = 25
    private const val STARTING_TEAM_COUNT = 9
    private const val OTHER_TEAM_COUNT = 8
    private const val NEUTRAL_COUNT = 7
    private const val ASSASSIN_COUNT = 1

    fun generateBoard(words: List<String>, startingTeam: Team): List<Card> {
        require(words.size >= BOARD_SIZE) {
            "Need at least $BOARD_SIZE words, got ${words.size}"
        }

        val selected = words.shuffled().take(BOARD_SIZE)

        val startColor = startingTeam.toCardColor()
        val otherColor = startingTeam.opposite().toCardColor()

        val colors = buildList {
            repeat(STARTING_TEAM_COUNT) { add(startColor) }
            repeat(OTHER_TEAM_COUNT) { add(otherColor) }
            repeat(NEUTRAL_COUNT) { add(CardColor.NEUTRAL) }
            repeat(ASSASSIN_COUNT) { add(CardColor.ASSASSIN) }
        }.shuffled()

        return selected.mapIndexed { i, word -> Card(id = i, word = word, color = colors[i]) }
    }

    fun applyGuess(
        cards: List<Card>,
        guessedCard: Card,
        currentTeam: Team
    ): Pair<List<Card>, GameResult> {
        val updatedCards = cards.map {
            if (it.id == guessedCard.id) it.copy(revealed = true) else it
        }
        val revealed = updatedCards.first { it.id == guessedCard.id }
        val score = calculateScore(updatedCards)

        val result = when (revealed.color) {
            CardColor.ASSASSIN -> GameResult.Assassin(revealed)
            currentTeam.toCardColor() -> {
                val winner = checkWinner(score)
                if (winner != null) GameResult.Win(winner, revealed, score)
                else GameResult.Correct(revealed, score)
            }
            else -> GameResult.WrongTeam(revealed, score)
        }

        return updatedCards to result
    }

    fun submitClue(word: String, count: Int): Clue {
        require(word.isNotBlank()) { "Clue word must not be blank" }
        require(count in 0..9) { "Clue count must be between 0 and 9" }
        return Clue(word = word.trim(), count = count)
    }

    fun checkWinner(score: Score): Team? = when {
        score.redLeft == 0 -> Team.RED
        score.blueLeft == 0 -> Team.BLUE
        else -> null
    }

    fun calculateScore(cards: List<Card>): Score {
        val redLeft = cards.count { it.color == CardColor.RED && !it.revealed }
        val blueLeft = cards.count { it.color == CardColor.BLUE && !it.revealed }
        return Score(redLeft, blueLeft)
    }

    private fun Team.toCardColor(): CardColor = when (this) {
        Team.RED -> CardColor.RED
        Team.BLUE -> CardColor.BLUE
    }
}
