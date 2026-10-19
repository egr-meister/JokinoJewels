package com.jokinojewels.game.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameLogicTest {

    @Test
    fun `level table matches the specification`() {
        val expected = listOf(
            Triple(2, 2, 60), Triple(3, 2, 70), Triple(4, 2, 70), Triple(6, 3, 80), Triple(6, 3, 80),
            Triple(8, 4, 100), Triple(8, 4, 100), Triple(10, 4, 110), Triple(12, 4, 120)
        )
        assertEquals(9, GameLogic.levelCount)
        GameLogic.levels.forEachIndexed { i, cfg ->
            assertEquals(i + 1, cfg.level)
            assertEquals(expected[i], Triple(cfg.pairs, cfg.columns, cfg.seconds))
            assertEquals(cfg.pairs * 2, cfg.cardCount)
        }
    }

    @Test
    fun `deck for every level has exactly two of each chosen icon`() {
        GameLogic.levels.forEach { cfg ->
            repeat(25) { seed ->
                val deck = GameLogic.buildDeck(cfg.pairs, Random(seed))
                assertEquals("level ${cfg.level} size", cfg.pairs * 2, deck.size)
                val counts = deck.groupingBy { it }.eachCount()
                assertEquals("level ${cfg.level} distinct icons", cfg.pairs, counts.size)
                assertTrue("every icon exactly twice", counts.values.all { it == 2 })
                assertTrue("icons in range", deck.all { it in 0 until GameLogic.ITEM_COUNT })
                assertTrue(GameLogic.isValidDeck(deck, cfg.pairs))
            }
        }
    }

    @Test
    fun `deck is deterministic for a seeded random and shuffled`() {
        val a = GameLogic.buildDeck(12, Random(42))
        val b = GameLogic.buildDeck(12, Random(42))
        assertEquals(a, b)
        assertFalse("deck should come out shuffled", a == a.sorted())
    }

    @Test
    fun `deck clamps pair count to the available icons`() {
        assertTrue(GameLogic.buildDeck(0).isEmpty())
        assertTrue(GameLogic.buildDeck(-3).isEmpty())
        val big = GameLogic.buildDeck(50, Random(1))
        assertEquals(GameLogic.ITEM_COUNT * 2, big.size)
        assertEquals(GameLogic.ITEM_COUNT, big.toSet().size)
    }

    @Test
    fun `invalid decks are rejected`() {
        assertFalse(GameLogic.isValidDeck(emptyList(), 2))
        assertFalse(GameLogic.isValidDeck(listOf(0, 0, 1), 2))
        assertFalse(GameLogic.isValidDeck(listOf(0, 0, 0, 1), 2))
        assertFalse(GameLogic.isValidDeck(listOf(0, 0, 99, 99), 2))
        assertTrue(GameLogic.isValidDeck(listOf(3, 5, 5, 3), 2))
    }

    @Test
    fun `level one is always unlocked and others start locked`() {
        val empty = emptyMap<String, Int>()
        assertTrue(GameLogic.isLevelUnlocked(1, empty))
        (2..9).forEach { assertFalse("level $it", GameLogic.isLevelUnlocked(it, empty)) }
    }

    @Test
    fun `clearing a level unlocks only the next one`() {
        val best = mapOf("1" to 2)
        assertTrue(GameLogic.isLevelCleared(1, best))
        assertTrue(GameLogic.isLevelUnlocked(2, best))
        assertFalse(GameLogic.isLevelUnlocked(3, best))
    }

    @Test
    fun `partial progress does not clear or unlock`() {
        val best = mapOf("1" to 1, "2" to 2)
        assertFalse(GameLogic.isLevelCleared(1, best))
        assertFalse(GameLogic.isLevelUnlocked(2, best))
        assertFalse(GameLogic.isLevelCleared(2, best))
    }

    @Test
    fun `out of range levels are never unlocked`() {
        val all = GameLogic.levels.associate { it.level.toString() to it.pairs }
        assertFalse(GameLogic.isLevelUnlocked(0, all))
        assertFalse(GameLogic.isLevelUnlocked(10, all))
        assertFalse(GameLogic.isLevelUnlocked(-1, all))
        assertNull(GameLogic.levelConfig(10))
        assertNull(GameLogic.nextLevel(9))
        assertEquals(2, GameLogic.nextLevel(1))
    }

    @Test
    fun `completed level count counts only fully cleared known levels`() {
        assertEquals(0, GameLogic.completedLevelCount(emptyMap()))
        val best = mapOf("1" to 2, "2" to 3, "3" to 3, "4" to 6, "x" to 99, "42" to 99, "9" to 12)
        assertEquals(4, GameLogic.completedLevelCount(best))
        val all = GameLogic.levels.associate { it.level.toString() to it.pairs }
        assertEquals(9, GameLogic.completedLevelCount(all))
    }

    @Test
    fun `win needs every pair`() {
        assertTrue(GameLogic.isWin(6, 6))
        assertFalse(GameLogic.isWin(5, 6))
        assertFalse(GameLogic.isWin(0, 0))
    }

    @Test
    fun `time formatting and rounding`() {
        assertEquals("2:00", GameLogic.formatTime(120))
        assertEquals("1:05", GameLogic.formatTime(65))
        assertEquals("0:00", GameLogic.formatTime(-4))
        assertEquals(1, GameLogic.secondsLeft(1))
        assertEquals(60, GameLogic.secondsLeft(60_000))
        assertEquals(0, GameLogic.secondsLeft(0))
        assertEquals(0, GameLogic.secondsLeft(-10))
    }

    @Test
    fun `quiz progress is clamped`() {
        assertEquals(0f, GameLogic.quizProgress(0, 10), 0.0001f)
        assertEquals(0.5f, GameLogic.quizProgress(5, 10), 0.0001f)
        assertEquals(1f, GameLogic.quizProgress(12, 10), 0.0001f)
        assertEquals(0f, GameLogic.quizProgress(3, 0), 0.0001f)
    }
}
