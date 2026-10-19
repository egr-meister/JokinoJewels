package com.jokinojewels.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JewelColorScheme = darkColorScheme(
    primary = JewelPalette.JesterGold,
    onPrimary = JewelPalette.Midnight,
    primaryContainer = JewelPalette.SapphireDeep,
    onPrimaryContainer = JewelPalette.Cream,
    secondary = JewelPalette.Sapphire,
    onSecondary = JewelPalette.Cream,
    tertiary = JewelPalette.SapphireBright,
    onTertiary = JewelPalette.Midnight,
    background = JewelPalette.Midnight,
    onBackground = JewelPalette.Cream,
    surface = JewelPalette.Navy,
    onSurface = JewelPalette.Cream,
    surfaceVariant = JewelPalette.SapphireDeep,
    onSurfaceVariant = JewelPalette.Cream,
    error = JewelPalette.Ruby,
    onError = JewelPalette.Midnight,
    outline = JewelPalette.GoldDeep
)

@Composable
fun JokinoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JewelColorScheme,
        typography = JewelTypography,
        content = content
    )
}
