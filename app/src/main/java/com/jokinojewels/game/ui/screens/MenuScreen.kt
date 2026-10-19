package com.jokinojewels.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jokinojewels.game.ui.Assets
import com.jokinojewels.game.ui.components.JewelScreen
import com.jokinojewels.game.ui.components.MenuPlateButton
import com.jokinojewels.game.ui.theme.JewelPalette

/** Grand Menu: ornate plates zig-zag like harlequin diamonds; the column scrolls if needed. */
@Composable
fun MenuScreen(
    onQuiz: () -> Unit,
    onPairs: () -> Unit,
    onResults: () -> Unit,
    onRules: () -> Unit,
    onSettings: () -> Unit,
    onBack: () -> Unit
) {
    val entries = listOf(
        "Quiz" to onQuiz,
        "Matching Pairs" to onPairs,
        "Results" to onResults,
        "Game Rules" to onRules,
        "Settings" to onSettings
    )
    JewelScreen(title = "Grand Menu", onBack = onBack) {
        Image(
            painter = painterResource(Assets.decoTicket),
            contentDescription = null,
            modifier = Modifier.width(150.dp).aspectRatio(Assets.DECO_TICKET_ASPECT)
        )
        Text(
            text = "Pick your act, jester!",
            style = MaterialTheme.typography.bodyLarge,
            color = JewelPalette.Cream,
            textAlign = TextAlign.Center
        )
        entries.forEachIndexed { index, (label, action) ->
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val shift = if (index % 2 == 0) -0.07f else 0.07f
                MenuPlateButton(
                    text = label,
                    onClick = action,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = maxWidth * shift)
                        .fillMaxWidth(0.7f)
                )
            }
        }
        Image(
            painter = painterResource(Assets.mascotD),
            contentDescription = "Jokino dancing with a ruby",
            modifier = Modifier
                .height(190.dp)
                .aspectRatio(Assets.MASCOT_D_ASPECT)
        )
        Spacer(Modifier.height(8.dp))
    }
}
