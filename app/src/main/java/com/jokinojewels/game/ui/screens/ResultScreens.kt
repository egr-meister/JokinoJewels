package com.jokinojewels.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jokinojewels.game.data.QuizContent
import com.jokinojewels.game.logic.GameLogic
import com.jokinojewels.game.ui.Assets
import com.jokinojewels.game.ui.LocalAppContainer
import com.jokinojewels.game.ui.components.DiamondPips
import com.jokinojewels.game.ui.components.FallbackScreen
import com.jokinojewels.game.ui.components.GameBackground
import com.jokinojewels.game.ui.components.JewelPanel
import com.jokinojewels.game.ui.components.PlateButton
import com.jokinojewels.game.ui.theme.JewelPalette
import com.jokinojewels.game.viewmodel.ProgressViewModel

@Composable
fun QuizResultsScreen(
    categoryId: String?,
    score: Int,
    total: Int,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    val category = QuizContent.category(categoryId)
    if (category == null || total != category.questions.size || score !in 0..total) {
        FallbackScreen(message = "These quiz results are no longer available.", onBack = onContinue)
        return
    }
    val container = LocalAppContainer.current
    val viewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory(container.repository))
    val progress by viewModel.state.collectAsStateWithLifecycle()
    val best = progress.categories.firstOrNull { it.id == category.id }?.best

    val verdict = when {
        score >= total -> "Flawless Brilliance!"
        score >= total * 0.8 -> "Royal Sparkle!"
        score >= total * 0.5 -> "Polished Gem"
        score >= total * 0.3 -> "Rough Diamond"
        else -> "Keep Juggling!"
    }

    ResultsLayout(
        title = verdict,
        onBack = onContinue,
        mascot = if (score * 2 >= total) Assets.mascotE else Assets.mascotD,
        mascotAspect = if (score * 2 >= total) Assets.MASCOT_E_ASPECT else Assets.MASCOT_D_ASPECT
    ) {
        Text(category.title, style = MaterialTheme.typography.titleMedium, color = JewelPalette.JesterGold)
        Text(
            text = "Score: $score/$total",
            style = MaterialTheme.typography.displaySmall,
            color = JewelPalette.Cream,
            textAlign = TextAlign.Center
        )
        DiamondPips(total = total, filled = score, description = "$score of $total answers correct")
        Text(
            text = if (best != null) "Best: $best/$total" else "Best: $score/$total",
            style = MaterialTheme.typography.bodyLarge,
            color = JewelPalette.Cream
        )
        PlateButton(text = "Continue", onClick = onContinue, modifier = Modifier.fillMaxWidth())
        PlateButton(text = "Restart", onClick = onRestart, modifier = Modifier.fillMaxWidth())
        PlateButton(text = "Menu", onClick = onMenu, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun PairsResultsScreen(
    level: Int,
    won: Boolean,
    matched: Int,
    total: Int,
    onContinue: () -> Unit,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    val config = GameLogic.levelConfig(level)
    val consistent = config != null && total == config.pairs && matched in 0..total &&
        won == GameLogic.isWin(matched, total)
    if (!consistent) {
        FallbackScreen(message = "These level results are no longer available.", onBack = onContinue)
        return
    }
    val container = LocalAppContainer.current
    val viewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory(container.repository))
    val progress by viewModel.state.collectAsStateWithLifecycle()
    val best = progress.levels.firstOrNull { it.level == level }?.bestMatched ?: matched
    val nextLevel = GameLogic.nextLevel(level)

    ResultsLayout(
        title = if (won) "Well Done" else "Out of Time",
        onBack = onContinue,
        mascot = if (won) Assets.mascotE else Assets.mascotF,
        mascotAspect = if (won) Assets.MASCOT_E_ASPECT else Assets.MASCOT_F_ASPECT
    ) {
        Text("Level $level", style = MaterialTheme.typography.titleMedium, color = JewelPalette.JesterGold)
        Text(
            text = "Pairs matched: $matched/$total",
            style = MaterialTheme.typography.headlineSmall,
            color = JewelPalette.Cream,
            textAlign = TextAlign.Center
        )
        DiamondPips(total = total, filled = matched, description = "$matched of $total pairs matched", pipSize = 12.dp, gap = 4.dp)
        Text(
            text = "Best: ${maxOf(best, matched)}/$total",
            style = MaterialTheme.typography.bodyLarge,
            color = JewelPalette.Cream
        )
        Text(
            text = when {
                won && nextLevel != null -> "Level $nextLevel is now unlocked!"
                won -> "You cleared every vault. A true Jewel Master!"
                else -> "The hourglass ran dry. Shuffle in and try again!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = JewelPalette.Cream,
            textAlign = TextAlign.Center
        )
        PlateButton(text = "Continue", onClick = onContinue, modifier = Modifier.fillMaxWidth())
        PlateButton(text = "Restart", onClick = onRestart, modifier = Modifier.fillMaxWidth())
        PlateButton(text = "Menu", onClick = onMenu, modifier = Modifier.fillMaxWidth())
    }
}

/** Shared results composition: mascot peeking behind a Compose-drawn results panel. */
@Composable
private fun ResultsLayout(
    title: String,
    onBack: () -> Unit,
    mascot: Int,
    mascotAspect: Float,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    BackHandler(onBack = onBack)
    GameBackground {
        Image(
            painter = painterResource(mascot),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-24).dp)
                .fillMaxWidth(0.5f)
                .aspectRatio(mascotAspect)
                .alpha(0.9f)
        )
        Box(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            JewelPanel(
                title = title,
                modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                scrollable = true,
                content = content
            )
        }
        Image(
            painter = painterResource(Assets.decoMedal),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .size(width = 46.dp, height = 78.dp)
        )
    }
}
