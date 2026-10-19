package com.jokinojewels.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSerializerTest {

    private val defaults = GameData()

    @Test
    fun `null empty and blank input give defaults`() {
        assertEquals(defaults, GameSerializer.decode(null))
        assertEquals(defaults, GameSerializer.decode(""))
        assertEquals(defaults, GameSerializer.decode("   \n\t "))
    }

    @Test
    fun `corrupted input gives defaults`() {
        listOf(
            "{", "}", "{not json", "null", "[]", "42", "\"text\"", "{\"quizBest\":", "\u0000\u0001garbage",
            "{\"quizBest\":{\"gem_lore\":\"ten\"}}", "{\"pairsBest\":[1,2,3]}"
        ).forEach { raw ->
            assertEquals("input: $raw", defaults, GameSerializer.decode(raw))
        }
    }

    @Test
    fun `unknown keys are ignored`() {
        val raw = """{"quizBest":{"gem_lore":7},"futureFeature":{"a":1},"settings":{"soundEnabled":false,"vibration":true}}"""
        val data = GameSerializer.decode(raw)
        assertEquals(7, data.quizBest["gem_lore"])
        assertFalse(data.settings.soundEnabled)
    }

    @Test
    fun `missing fields fall back to defaults`() {
        assertEquals(defaults, GameSerializer.decode("{}"))
        val onlyPairs = GameSerializer.decode("""{"pairsBest":{"1":2}}""")
        assertEquals(mapOf("1" to 2), onlyPairs.pairsBest)
        assertTrue(onlyPairs.quizBest.isEmpty())
        assertTrue(onlyPairs.settings.soundEnabled)
        val emptySettings = GameSerializer.decode("""{"settings":{}}""")
        assertTrue(emptySettings.settings.soundEnabled)
    }

    @Test
    fun `explicit nulls are coerced to defaults`() {
        val data = GameSerializer.decode("""{"quizBest":null,"pairsBest":null,"settings":null}""")
        assertEquals(defaults, data)
    }

    @Test
    fun `impossible values are sanitized`() {
        val data = GameSerializer.decode("""{"quizBest":{"gem_lore":-3,"":5,"royal_treasures":9},"pairsBest":{"abc":2,"0":1,"2":3,"3":-1}}""")
        assertEquals(mapOf("royal_treasures" to 9), data.quizBest)
        assertEquals(mapOf("2" to 3), data.pairsBest)
    }

    @Test
    fun `encode then decode round trips`() {
        val original = GameData(
            quizBest = mapOf("gem_lore" to 8, "jesters_court" to 10),
            pairsBest = mapOf("1" to 2, "2" to 3, "3" to 1),
            settings = Settings(soundEnabled = false)
        )
        assertEquals(original, GameSerializer.decode(GameSerializer.encode(original)))
        assertEquals(defaults, GameSerializer.decode(GameSerializer.encode(defaults)))
    }

    @Test
    fun `best values only increase`() {
        val d0 = GameData()
        val d1 = d0.withQuizScore("gem_lore", 0)
        assertEquals(0, d1.quizBest["gem_lore"])
        val d2 = d1.withQuizScore("gem_lore", 6).withQuizScore("gem_lore", 4)
        assertEquals(6, d2.quizBest["gem_lore"])
        val p = d0.withPairsResult(1, 1).withPairsResult(1, 2).withPairsResult(1, 0)
        assertEquals(2, p.pairsBest["1"])
        assertEquals(d0, d0.withPairsResult(0, 5))
        assertEquals(d0, d0.withQuizScore(" ", 5))
    }

    @Test
    fun `reset clears progress but keeps settings`() {
        val data = GameData(
            quizBest = mapOf("gem_lore" to 8),
            pairsBest = mapOf("1" to 2),
            settings = Settings(soundEnabled = false)
        )
        val reset = data.resetProgress()
        assertTrue(reset.quizBest.isEmpty())
        assertTrue(reset.pairsBest.isEmpty())
        assertFalse(reset.settings.soundEnabled)
    }
}
