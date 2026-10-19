package com.jokinojewels.game.data

import kotlinx.serialization.json.Json

/**
 * Context-free JSON encode/decode for [GameData] — unit-testable on the JVM.
 *
 * Decoding NEVER throws: null, blank, corrupted or wrongly-typed JSON all yield defaults.
 * Unknown keys are ignored and missing fields fall back to their defaults, so the format
 * is forward and backward compatible.
 */
object GameSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    fun encode(data: GameData): String = json.encodeToString(GameData.serializer(), data)

    fun decode(raw: String?): GameData {
        if (raw.isNullOrBlank()) return GameData()
        val parsed = try {
            json.decodeFromString(GameData.serializer(), raw)
        } catch (e: Exception) {
            // SerializationException, IllegalArgumentException, NumberFormatException, ...
            null
        }
        return parsed?.let(::sanitize) ?: GameData()
    }

    /** Drops impossible values (negative scores, blank or non-numeric level keys). */
    internal fun sanitize(data: GameData): GameData = data.copy(
        quizBest = data.quizBest.filter { (id, score) -> id.isNotBlank() && score >= 0 },
        pairsBest = data.pairsBest.filter { (level, matched) ->
            val number = level.toIntOrNull()
            number != null && number > 0 && matched >= 0
        }
    )
}
