package com.jokinojewels.game

import android.app.Application
import com.jokinojewels.game.audio.SoundManager
import com.jokinojewels.game.data.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Creates the single [GameRepository] and [SoundManager] (no DI framework) and keeps
 * the sound toggle stored in DataStore in sync with the SoundManager — at startup and live.
 */
class JokinoApp : Application() {

    lateinit var repository: GameRepository
        private set

    lateinit var soundManager: SoundManager
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        repository = GameRepository(this)
        soundManager = SoundManager(this)
        appScope.launch {
            repository.gameData
                .map { it.settings.soundEnabled }
                .distinctUntilChanged()
                .collect { enabled -> soundManager.setEnabled(enabled) }
        }
    }
}
