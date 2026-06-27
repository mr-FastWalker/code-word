package com.codeword.app.core.game

import com.codeword.app.core.model.Card
import com.codeword.app.core.model.CardColor
import com.codeword.app.core.model.Score
import com.codeword.app.core.model.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private val testWords = (1..60).map { "word$it" }

    // --- generateBoard ---

    @Test
    fun `generateBoard produces 25 cards`() {
        val board = GameEngine.generateBoard(testWords, Team.RED)
        assertEquals(25, board.size)
    }

    @Test
    fun `generateBoard RED start has 9 red 8 blue 7 neutral 1 assassin`() {
        val board = GameEngine.generateBoard(testWords, Team.RED)
        assertEquals(9, board.count { it.color == CardColor.RED })
        assertEquals(8, board.count { it.color == CardColor.BLUE })
        assertEquals(7, board.count { it.color == CardColor.NEUTRAL })
        assertEquals(1, board.count { it.color == CardColor.ASSASSIN })
    }

    @Test
    fun `generateBoard BLUE start has 8 red 9 blue 7 neutral 1 assassin`() {
        val board = GameEngine.generateBoard(testWords, Team.BLUE)
        assertEquals(8, board.count { it.color == CardColor.RED })
        assertEquals(9, board.count { it.color == CardColor.BLUE })
        assertEquals(7, board.count { it.color == CardColor.NEUTRAL })
        assertEquals(1, board.count { it.color == CardColor.ASSASSIN })
    }

    @Test
    fun `generateBoard uses words from provided list`() {
        val board = GameEngine.generateBoard(testWords, Team.RED)
        board.forEach { card -> assertTrue(card.word in testWords) }
    }

    @Test
    fun `generateBoard all cards start unrevealed`() {
        val board = GameEngine.generateBoard(testWords, Team.RED)
        assertTrue(board.none { it.revealed })
    }

    @Test
    fun `generateBoard throws when fewer than 25 words provided`() {
        assertThrows(IllegalArgumentException::class.java) {
            GameEngine.generateBoard(testWords.take(24), Team.RED)
        }
    }

    // --- applyGuess ---

    @Test
    fun `applyGuess correct team card returns Correct`() {
        val cards = buildBoard(redCount = 2, blueCount = 2)
        val redCard = cards.first { it.color == CardColor.RED }
        val (_, result) = GameEngine.applyGuess(cards, redCard, Team.RED)
        assertTrue(result is GameResult.Correct)
    }

    @Test
    fun `applyGuess reveals the guessed card`() {
        val cards = buildBoard(redCount = 2, blueCount = 2)
        val redCard = cards.first { it.color == CardColor.RED }
        val (updatedCards, _) = GameEngine.applyGuess(cards, redCard, Team.RED)
        assertTrue(updatedCards.first { it.id == redCard.id }.revealed)
    }

    @Test
    fun `applyGuess does not reveal other cards`() {
        val cards = buildBoard(redCount = 2, blueCount = 2)
        val redCard = cards.first { it.color == CardColor.RED }
        val (updatedCards, _) = GameEngine.applyGuess(cards, redCard, Team.RED)
        val others = updatedCards.filter { it.id != redCard.id }
        assertTrue(others.none { it.revealed })
    }

    @Test
    fun `applyGuess wrong team card returns WrongTeam`() {
        val cards = buildBoard(redCount = 2, blueCount = 2)
        val blueCard = cards.first { it.color == CardColor.BLUE }
        val (_, result) = GameEngine.applyGuess(cards, blueCard, Team.RED)
        assertTrue(result is GameResult.WrongTeam)
    }

    @Test
    fun `applyGuess neutral card returns WrongTeam`() {
        val cards = buildBoard(neutral = 1, redCount = 1)
        val neutralCard = cards.first { it.color == CardColor.NEUTRAL }
        val (_, result) = GameEngine.applyGuess(cards, neutralCard, Team.RED)
        assertTrue(result is GameResult.WrongTeam)
    }

    @Test
    fun `applyGuess assassin card returns Assassin`() {
        val cards = buildBoard(assassin = 1, redCount = 1)
        val assassinCard = cards.first { it.color == CardColor.ASSASSIN }
        val (_, result) = GameEngine.applyGuess(cards, assassinCard, Team.RED)
        assertTrue(result is GameResult.Assassin)
    }

    @Test
    fun `applyGuess last red card returns Win for RED`() {
        val cards = buildBoard(redCount = 1, blueCount = 1)
        val (_, result) = GameEngine.applyGuess(cards, cards[0], Team.RED)
        assertTrue(result is GameResult.Win)
        assertEquals(Team.RED, (result as GameResult.Win).winner)
    }

    @Test
    fun `applyGuess last blue card returns Win for BLUE`() {
        val cards = buildBoard(redCount = 1, blueCount = 1)
        val blueCard = cards.first { it.color == CardColor.BLUE }
        val (_, result) = GameEngine.applyGuess(cards, blueCard, Team.BLUE)
        assertTrue(result is GameResult.Win)
        assertEquals(Team.BLUE, (result as GameResult.Win).winner)
    }

    @Test
    fun `applyGuess score decrements after reveal`() {
        val cards = buildBoard(redCount = 3, blueCount = 2)
        val redCard = cards.first { it.color == CardColor.RED }
        val (_, result) = GameEngine.applyGuess(cards, redCard, Team.RED)
        val score = (result as GameResult.Correct).score
        assertEquals(2, score.redLeft)
        assertEquals(2, score.blueLeft)
    }

    // --- checkWinner ---

    @Test
    fun `checkWinner returns null when game in progress`() {
        assertNull(GameEngine.checkWinner(Score(redLeft = 3, blueLeft = 2)))
    }

    @Test
    fun `checkWinner returns RED when redLeft is 0`() {
        assertEquals(Team.RED, GameEngine.checkWinner(Score(redLeft = 0, blueLeft = 2)))
    }

    @Test
    fun `checkWinner returns BLUE when blueLeft is 0`() {
        assertEquals(Team.BLUE, GameEngine.checkWinner(Score(redLeft = 2, blueLeft = 0)))
    }

    // --- submitClue ---

    @Test
    fun `submitClue returns valid Clue`() {
        val clue = GameEngine.submitClue("animal", 3)
        assertEquals("animal", clue.word)
        assertEquals(3, clue.count)
    }

    @Test
    fun `submitClue trims whitespace`() {
        val clue = GameEngine.submitClue("  animal  ", 2)
        assertEquals("animal", clue.word)
    }

    @Test
    fun `submitClue allows count 0 for unlimited`() {
        val clue = GameEngine.submitClue("animal", 0)
        assertEquals(0, clue.count)
    }

    @Test
    fun `submitClue throws on blank word`() {
        assertThrows(IllegalArgumentException::class.java) {
            GameEngine.submitClue("   ", 2)
        }
    }

    @Test
    fun `submitClue throws on count above 9`() {
        assertThrows(IllegalArgumentException::class.java) {
            GameEngine.submitClue("word", 10)
        }
    }

    // --- calculateScore ---

    @Test
    fun `calculateScore counts only unrevealed cards`() {
        val cards = listOf(
            Card(0, "a", CardColor.RED, revealed = true),
            Card(1, "b", CardColor.RED, revealed = false),
            Card(2, "c", CardColor.BLUE, revealed = false),
            Card(3, "d", CardColor.BLUE, revealed = true),
        )
        val score = GameEngine.calculateScore(cards)
        assertEquals(1, score.redLeft)
        assertEquals(1, score.blueLeft)
    }

    // --- helpers ---

    private fun buildBoard(
        redCount: Int = 0,
        blueCount: Int = 0,
        neutral: Int = 0,
        assassin: Int = 0
    ): List<Card> {
        var id = 0
        return buildList {
            repeat(redCount) { add(Card(id++, "red$id", CardColor.RED)) }
            repeat(blueCount) { add(Card(id++, "blue$id", CardColor.BLUE)) }
            repeat(neutral) { add(Card(id++, "neutral$id", CardColor.NEUTRAL)) }
            repeat(assassin) { add(Card(id++, "assassin$id", CardColor.ASSASSIN)) }
        }
    }
}
