package com.jokinojewels.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "jokino_jewels_store",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)

/**
 * The single local source of truth. Stores [GameData] as one serialized JSON string.
 * Created once in [com.jokinojewels.game.JokinoApp].
 */
class GameRepository(context: Context) {

    private val dataStore: DataStore<Preferences> = context.applicationContext.gameDataStore

    /** Always emits a valid [GameData]; empty or broken storage maps to defaults. */
    val gameData: Flow<GameData> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> GameSerializer.decode(prefs[GAME_DATA_KEY]) }
        .distinctUntilChanged()

    suspend fun saveQuizScore(categoryId: String, score: Int) =
        update { it.withQuizScore(categoryId, score) }

    suspend fun savePairsResult(level: Int, matchedPairs: Int) =
        update { it.withPairsResult(level, matchedPairs) }

    suspend fun setSoundEnabled(enabled: Boolean) =
        update { it.withSoundEnabled(enabled) }

    suspend fun toggleSound() =
        update { it.withSoundEnabled(!it.settings.soundEnabled) }

    /** Resets quiz and pairs progress; the sound setting is kept. */
    suspend fun resetProgress() =
        update { it.resetProgress() }

    private suspend fun update(transform: (GameData) -> GameData) {
        try {
            dataStore.edit { prefs ->
                val current = GameSerializer.decode(prefs[GAME_DATA_KEY])
                prefs[GAME_DATA_KEY] = GameSerializer.encode(transform(current))
            }
        } catch (e: IOException) {
            // Disk full / IO failure: keep playing, progress for this action is skipped.
        }
    }

    private companion object {
        val GAME_DATA_KEY = stringPreferencesKey("game_data_json")
    }
}
