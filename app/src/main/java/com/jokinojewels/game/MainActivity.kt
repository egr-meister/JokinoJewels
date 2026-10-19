package com.jokinojewels.game

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jokinojewels.game.ui.AppContainer
import com.jokinojewels.game.ui.LocalAppContainer
import com.jokinojewels.game.ui.navigation.AppNavHost
import com.jokinojewels.game.ui.theme.JokinoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        val app = application as JokinoApp
        val container = AppContainer(repository = app.repository, soundManager = app.soundManager)
        setContent {
            JokinoTheme {
                CompositionLocalProvider(LocalAppContainer provides container) {
                    AppNavHost()
                }
            }
        }
    }
}
