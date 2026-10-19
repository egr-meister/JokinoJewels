package com.jokinojewels.game.data

import kotlinx.serialization.Serializable

/**
 * User-facing settings. Every field has a default so older/newer JSON stays readable.
 */
@Serializable
data class Settings(
    val soundEnabled: Boolean = true
)

/**
 * Everything the game persists, stored as ONE JSON string in DataStore Preferences.
 *
 * - [quizBest]  quiz category id  -> best score (0..10)
 * - [pairsBest] level number as text ("1".."9") -> best matched pairs in that level
 */
@Serializable
data class GameData(
    val quizBest: Map<String, Int> = emptyMap(),
    val pairsBest: Map<String, Int> = emptyMap(),
    val settings: Settings = Settings()
) {
    /** Keeps the higher of the stored and the new score. A first attempt of 0 is stored too. */
    fun withQuizScore(categoryId: String, score: Int): GameData {
        if (categoryId.isBlank()) return this
        val safe = score.coerceAtLeast(0)
        val current = quizBest[categoryId] ?: -1
        return if (safe > current) copy(quizBest = quizBest + (categoryId to safe)) else this
    }

    /** Keeps the higher of the stored and the new matched-pairs count for [level]. */
    fun withPairsResult(level: Int, matchedPairs: Int): GameData {
        if (level <= 0) return this
        val key = level.toString()
        val safe = matchedPairs.coerceAtLeast(0)
        val current = pairsBest[key] ?: -1
        return if (safe > current) copy(pairsBest = pairsBest + (key to safe)) else this
    }

    fun withSoundEnabled(enabled: Boolean): GameData =
        copy(settings = settings.copy(soundEnabled = enabled))

    /** Clears all progress but keeps the user's settings. */
    fun resetProgress(): GameData = GameData(settings = settings)
}
