package com.jokinojewels.game.logic

import kotlin.random.Random

/** One Matching Pairs level: number of pairs, grid columns and countdown seconds. */
data class LevelConfig(
    val level: Int,
    val pairs: Int,
    val columns: Int,
    val seconds: Int
) {
    val cardCount: Int get() = pairs * 2
    val rows: Int get() = (cardCount + columns - 1) / columns
}

/**
 * Pure, context-free game rules. No Android types, fully unit-testable.
 */
object GameLogic {

    /** Number of distinct match icons (item_01..item_12). */
    const val ITEM_COUNT = 12

    /** Quiz: how long the green/red answer highlight stays before auto-advance. */
    const val ANSWER_REVEAL_MS = 750L

    /** Pairs: how long a mismatched pair stays face-up before flipping back. */
    const val MISMATCH_DELAY_MS = 700L

    val levels: List<LevelConfig> = listOf(
        LevelConfig(level = 1, pairs = 2, columns = 2, seconds = 60),
        LevelConfig(level = 2, pairs = 3, columns = 2, seconds = 70),
        LevelConfig(level = 3, pairs = 4, columns = 2, seconds = 70),
        LevelConfig(level = 4, pairs = 6, columns = 3, seconds = 80),
        LevelConfig(level = 5, pairs = 6, columns = 3, seconds = 80),
        LevelConfig(level = 6, pairs = 8, columns = 4, seconds = 100),
        LevelConfig(level = 7, pairs = 8, columns = 4, seconds = 100),
        LevelConfig(level = 8, pairs = 10, columns = 4, seconds = 110),
        LevelConfig(level = 9, pairs = 12, columns = 4, seconds = 120)
    )

    val levelCount: Int get() = levels.size

    fun levelConfig(level: Int): LevelConfig? = levels.firstOrNull { it.level == level }

    fun levelKey(level: Int): String = level.toString()

    fun nextLevel(level: Int): Int? = levelConfig(level + 1)?.level

    /**
     * Builds a shuffled deck of `2 * pairs` item indices: [pairs] distinct icons out of
     * [itemCount], each duplicated once. [pairs] is clamped to `0..itemCount`.
     */
    fun buildDeck(pairs: Int, random: Random = Random.Default, itemCount: Int = ITEM_COUNT): List<Int> {
        val count = pairs.coerceIn(0, itemCount.coerceAtLeast(0))
        if (count == 0) return emptyList()
        val chosen = (0 until itemCount).shuffled(random).take(count)
        return (chosen + chosen).shuffled(random)
    }

    /** True if [deck] holds exactly [pairs] distinct icons, each exactly twice. */
    fun isValidDeck(deck: List<Int>, pairs: Int, itemCount: Int = ITEM_COUNT): Boolean {
        if (pairs <= 0 || deck.size != pairs * 2) return false
        if (deck.any { it !in 0 until itemCount }) return false
        val counts = deck.groupingBy { it }.eachCount()
        return counts.size == pairs && counts.values.all { it == 2 }
    }

    fun bestMatched(level: Int, pairsBest: Map<String, Int>): Int =
        (pairsBest[levelKey(level)] ?: 0).coerceAtLeast(0)

    /** A level is cleared once all of its pairs were matched in some run. */
    fun isLevelCleared(level: Int, pairsBest: Map<String, Int>): Boolean {
        val config = levelConfig(level) ?: return false
        return bestMatched(level, pairsBest) >= config.pairs
    }

    /** Level 1 is always open; level N opens after level N-1 is cleared. */
    fun isLevelUnlocked(level: Int, pairsBest: Map<String, Int>): Boolean = when {
        levelConfig(level) == null -> false
        level == 1 -> true
        else -> isLevelCleared(level - 1, pairsBest)
    }

    fun completedLevelCount(pairsBest: Map<String, Int>): Int =
        levels.count { isLevelCleared(it.level, pairsBest) }

    fun isWin(matchedPairs: Int, totalPairs: Int): Boolean =
        totalPairs > 0 && matchedPairs >= totalPairs

    fun quizProgress(answered: Int, total: Int): Float =
        if (total <= 0) 0f else (answered.toFloat() / total).coerceIn(0f, 1f)

    /** "m:ss" for the countdown. */
    fun formatTime(seconds: Int): String {
        val s = seconds.coerceAtLeast(0)
        return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
    }

    /** Rounds remaining milliseconds UP to whole seconds so "0:00" means truly over. */
    fun secondsLeft(remainingMs: Long): Int =
        ((remainingMs.coerceAtLeast(0L) + 999L) / 1000L).toInt()
}
