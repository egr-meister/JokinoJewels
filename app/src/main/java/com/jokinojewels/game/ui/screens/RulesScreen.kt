package com.jokinojewels.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.res.painterResource
import com.jokinojewels.game.ui.Assets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jokinojewels.game.logic.GameLogic
import com.jokinojewels.game.ui.components.JewelPanel
import com.jokinojewels.game.ui.components.JewelScreen
import com.jokinojewels.game.ui.components.PlateButton
import com.jokinojewels.game.ui.theme.JewelPalette

@Composable
fun RulesScreen(onBack: () -> Unit) {
    JewelScreen(title = "Game Rules", onBack = onBack) {
        Image(
            painter = painterResource(Assets.decoFeather),
            contentDescription = null,
            modifier = Modifier.height(90.dp).aspectRatio(Assets.DECO_FEATHER_ASPECT)
        )
        JewelPanel(title = "Round Wonders Quiz", modifier = Modifier.fillMaxWidth()) {
            RuleText(
                "• Circles, spheres and balls! Pick Easy, Medium or Hard.\n" +
                    "• 10 questions, one correct answer each.\n" +
                    "• Tap an answer: the correct one turns green with ✓, a wrong pick turns red with ✗. " +
                    "The next question appears by itself.\n" +
                    "• Your best score per difficulty is saved on this device.\n" +
                    "• Tap Pause (or Back) to Continue, Restart or return to the Menu."
            )
        }
        JewelPanel(title = "Matching Pairs", modifier = Modifier.fillMaxWidth()) {
            RuleText(
                "• Flip two cards. A matching pair stays open, a mismatch flips back.\n" +
                    "• Clear all pairs before the hourglass runs out to win: \"Well Done\".\n" +
                    "• If the time hits 0:00 first: \"Out of Time\".\n" +
                    "• Clearing a level unlocks the next one. Level 1 is always open.\n" +
                    "• The timer stops while the game is paused."
            )
            Text(
                text = "Levels",
                style = MaterialTheme.typography.titleMedium,
                color = JewelPalette.JesterGold,
                modifier = Modifier.semantics { heading() }
            )
            RuleText(
                GameLogic.levels.joinToString("\n") {
                    "Level ${it.level}: ${it.pairs} pairs · ${it.columns} columns · ${it.seconds} s"
                }
            )
        }
        Image(
            painter = painterResource(Assets.mascotC),
            contentDescription = null,
            modifier = Modifier.height(170.dp).aspectRatio(Assets.MASCOT_C_ASPECT)
        )
        PlateButton(text = "Got it", onClick = onBack, modifier = Modifier.fillMaxWidth(0.8f))
    }
}

@Composable
private fun RuleText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = JewelPalette.Cream,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )
}
