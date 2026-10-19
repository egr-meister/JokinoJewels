package com.jokinojewels.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jokinojewels.game.ui.Assets
import com.jokinojewels.game.ui.LocalAppContainer
import com.jokinojewels.game.ui.components.DiamondPips
import com.jokinojewels.game.ui.components.JewelScreen
import com.jokinojewels.game.ui.components.LockGlyph
import com.jokinojewels.game.ui.components.PlateButton
import com.jokinojewels.game.ui.components.rememberClickSound
import com.jokinojewels.game.ui.theme.JewelPalette
import com.jokinojewels.game.viewmodel.LevelProgressUi
import com.jokinojewels.game.viewmodel.ProgressViewModel

/** 9 level bubbles laid out as a zig-zag "jewel path" (3 staggered rows), plus Game Rules. */
@Composable
fun LevelsScreen(onLevel: (Int) -> Unit, onRules: () -> Unit, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory(container.repository))
    val state by viewModel.state.collectAsStateWithLifecycle()
    var hint by rememberSaveable { mutableStateOf<String?>(null) }

    JewelScreen(title = "Matching Pairs", onBack = onBack) {
        Text(
            text = "Levels cleared: ${state.levelsCleared}/${state.totalLevels}",
            style = MaterialTheme.typography.titleMedium,
            color = JewelPalette.Cream
        )
        DiamondPips(
            total = state.totalLevels,
            filled = state.levelsCleared,
            description = "${state.levelsCleared} of ${state.totalLevels} levels cleared"
        )
        if (!state.isLoaded) {
            Text("Opening the vault…", color = JewelPalette.Cream, style = MaterialTheme.typography.bodyMedium)
        }
        state.levels.chunked(3).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (rowIndex % 2 == 1) 36.dp else 0.dp,
                        end = if (rowIndex % 2 == 0) 36.dp else 0.dp
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { level ->
                    LevelBubble(
                        level = level,
                        onClick = {
                            if (level.unlocked) {
                                hint = null
                                onLevel(level.level)
                            } else {
                                hint = "Clear Level ${level.level - 1} to unlock Level ${level.level}."
                            }
                        }
                    )
                }
            }
        }
        hint?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge, color = JewelPalette.JesterGold, textAlign = TextAlign.Center)
        }
        PlateButton(text = "Game Rules", onClick = onRules, modifier = Modifier.fillMaxWidth(0.8f))
        Image(
            painter = painterResource(Assets.mascotF),
            contentDescription = null,
            modifier = Modifier.height(170.dp).aspectRatio(Assets.MASCOT_F_ASPECT)
        )
    }
}

@Composable
private fun LevelBubble(level: LevelProgressUi, onClick: () -> Unit) {
    val click = rememberClickSound()
    val status = when {
        level.cleared -> "Cleared"
        level.unlocked -> "Open"
        else -> "Locked"
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClickLabel = if (level.unlocked) "Play level ${level.level}" else "Locked") {
                    click()
                    onClick()
                }
                .semantics {
                    contentDescription = "Level ${level.level}, ${level.pairs} pairs, ${level.seconds} seconds"
                    stateDescription = status
                }
                .alpha(if (level.unlocked) 1f else 0.5f),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(Assets.plateRound), contentDescription = null, modifier = Modifier.size(84.dp))
            if (level.unlocked) {
                Text(
                    text = "${level.level}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.7f), Offset(0f, 3f), 6f))
                )
            } else {
                LockGlyph(Modifier.size(30.dp))
            }
        }
        Text(
            text = if (level.cleared) "✓ $status" else status,
            style = MaterialTheme.typography.labelMedium,
            color = if (level.cleared) JewelPalette.JesterGold else JewelPalette.Cream
        )
        Text(
            text = "${level.bestMatched}/${level.pairs} pairs",
            style = MaterialTheme.typography.labelMedium,
            color = JewelPalette.Cream
        )
    }
}
