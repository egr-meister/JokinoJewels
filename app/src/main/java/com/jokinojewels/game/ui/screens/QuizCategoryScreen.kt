package com.jokinojewels.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jokinojewels.game.data.QuizContent
import com.jokinojewels.game.ui.Assets
import com.jokinojewels.game.ui.LocalAppContainer
import com.jokinojewels.game.ui.components.DiamondPips
import com.jokinojewels.game.ui.components.JewelPanel
import com.jokinojewels.game.ui.components.JewelScreen
import com.jokinojewels.game.ui.components.PlateButton
import com.jokinojewels.game.ui.theme.JewelPalette
import com.jokinojewels.game.viewmodel.ProgressViewModel

/** Quiz difficulty select: Easy / Medium / Hard, 10 questions each. */
@Composable
fun QuizCategoryScreen(onCategory: (String) -> Unit, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory(container.repository))
    val state by viewModel.state.collectAsStateWithLifecycle()

    JewelScreen(title = QuizContent.QUIZ_TITLE, onBack = onBack) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(Assets.mascotA),
                contentDescription = null,
                modifier = Modifier.height(120.dp).aspectRatio(Assets.MASCOT_A_ASPECT)
            )
            Text(
                text = "Circles, spheres and balls! Pick a difficulty — 10 questions, 4 answers, one is true.",
                style = MaterialTheme.typography.bodyLarge,
                color = JewelPalette.Cream,
                modifier = Modifier.weight(1f)
            )
        }
        if (!state.isLoaded) {
            Text("Polishing the questions…", color = JewelPalette.Cream, style = MaterialTheme.typography.bodyMedium)
        }
        state.categories.forEach { category ->
            JewelPanel(title = null, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(Assets.difficultyIcon(category.difficulty)),
                            contentDescription = null,
                            modifier = Modifier.width(64.dp).aspectRatio(Assets.difficultyIconAspect(category.difficulty))
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(category.title, style = MaterialTheme.typography.headlineSmall, color = JewelPalette.JesterGold)
                        Text(category.subtitle, style = MaterialTheme.typography.bodyMedium, color = JewelPalette.Cream)
                        Text(
                            text = category.best?.let { "Best: $it/${category.total}" } ?: "Not played yet",
                            style = MaterialTheme.typography.labelLarge,
                            color = JewelPalette.Cream
                        )
                    }
                }
                DiamondPips(
                    total = category.total,
                    filled = category.best ?: 0,
                    description = "Best score ${category.best ?: 0} of ${category.total}",
                    pipSize = 12.dp,
                    gap = 5.dp
                )
                PlateButton(
                    text = "Play ${category.title}",
                    onClick = { onCategory(category.id) },
                    modifier = Modifier.fillMaxWidth(0.92f),
                    textFraction = 0.3f
                )
            }
        }
        Text(
            text = "Answers glow green ✓ when right and red ✗ when wrong.",
            style = MaterialTheme.typography.labelMedium,
            color = JewelPalette.Cream,
            textAlign = TextAlign.Center
        )
    }
}
