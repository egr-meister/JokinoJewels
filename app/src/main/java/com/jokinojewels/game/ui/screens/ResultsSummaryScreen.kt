package com.jokinojewels.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jokinojewels.game.ui.Assets
import com.jokinojewels.game.ui.LocalAppContainer
import com.jokinojewels.game.ui.components.DiamondPips
import com.jokinojewels.game.ui.components.JewelPanel
import com.jokinojewels.game.ui.components.JewelScreen
import com.jokinojewels.game.ui.theme.JewelPalette
import com.jokinojewels.game.viewmodel.ProgressViewModel

/** Treasury ledger: best per quiz category and pairs levels cleared out of 9. */
@Composable
fun ResultsSummaryScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory(container.repository))
    val state by viewModel.state.collectAsStateWithLifecycle()

    JewelScreen(title = "Results", onBack = onBack) {
        Image(
            painter = painterResource(Assets.decoMedal),
            contentDescription = null,
            modifier = Modifier.height(96.dp).aspectRatio(Assets.DECO_MEDAL_ASPECT)
        )
        JewelPanel(title = "Round Wonders Quiz", modifier = Modifier.fillMaxWidth()) {
            if (state.categories.isEmpty()) {
                Text("No quiz played yet.", color = JewelPalette.Cream, style = MaterialTheme.typography.bodyLarge)
            }
            state.categories.forEach { category ->
                Row(
                    modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(Assets.difficultyIcon(category.difficulty)),
                            contentDescription = null,
                            modifier = Modifier.width(48.dp).aspectRatio(Assets.difficultyIconAspect(category.difficulty))
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(category.title, style = MaterialTheme.typography.titleMedium, color = JewelPalette.JesterGold)
                        Text(
                            text = category.best?.let { "Best: $it/${category.total}" } ?: "Best: not played",
                            style = MaterialTheme.typography.bodyMedium,
                            color = JewelPalette.Cream
                        )
                        DiamondPips(
                            total = category.total,
                            filled = category.best ?: 0,
                            description = "Best ${category.best ?: 0} of ${category.total}",
                            pipSize = 10.dp,
                            gap = 4.dp
                        )
                    }
                }
            }
        }

        JewelPanel(title = "Matching Pairs", modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Pairs cleared: ${state.levelsCleared}/${state.totalLevels}",
                style = MaterialTheme.typography.headlineSmall,
                color = JewelPalette.Cream,
                textAlign = TextAlign.Center
            )
            DiamondPips(
                total = state.totalLevels,
                filled = state.levelsCleared,
                description = "${state.levelsCleared} of ${state.totalLevels} levels cleared"
            )
            state.levels.forEach { level ->
                Row(
                    modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Level ${level.level}", style = MaterialTheme.typography.bodyLarge, color = JewelPalette.Cream)
                    Text(
                        text = when {
                            level.cleared -> "✓ ${level.bestMatched}/${level.pairs}"
                            level.unlocked -> "${level.bestMatched}/${level.pairs}"
                            else -> "Locked"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (level.cleared) JewelPalette.JesterGold else JewelPalette.Cream
                    )
                }
            }
        }
    }
}
