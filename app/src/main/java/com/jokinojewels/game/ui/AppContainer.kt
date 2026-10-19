package com.jokinojewels.game.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.jokinojewels.game.audio.SoundManager
import com.jokinojewels.game.data.GameRepository

/** Hand-made dependency holder handed down the composition (no DI framework). */
class AppContainer(
    val repository: GameRepository,
    val soundManager: SoundManager
)

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer was not provided. Wrap content in CompositionLocalProvider(LocalAppContainer provides …).")
}
