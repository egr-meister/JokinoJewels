package com.jokinojewels.game.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jokinojewels.game.ui.Assets
import com.jokinojewels.game.ui.LocalAppContainer
import com.jokinojewels.game.ui.components.DiamondPips
import com.jokinojewels.game.ui.components.GameBackground
import com.jokinojewels.game.ui.components.MenuPlateButton
import com.jokinojewels.game.ui.theme.JewelPalette
import com.jokinojewels.game.viewmodel.ProgressViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Unique home composition — "the Jester's stage":
 *  - a crown-shaped ARC of seven twinkling treasures hangs from the curtains,
 *  - the logo hangs under the arc like a theatre sign and sways,
 *  - two jesters enter from the left and right wings (partly off-stage, bobbing in turn),
 *  - a "string of jewels" (pairs levels cleared) leads down into the ornate Start plate.
 * No stats card, no stacked dashboard.
 */
@Composable
fun MainScreen(onStart: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory(container.repository))
    val progress by viewModel.state.collectAsStateWithLifecycle()

    val transition = rememberInfiniteTransition(label = "main-stage")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val twinkle by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "twinkle"
    )
    val sway by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway"
    )

    GameBackground(dim = 0.05f) {
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            val w = maxWidth
            val h = maxHeight

            // 1) Crown arc of gems.
            val arcCount = 7
            for (i in 0 until arcCount) {
                val t = i / (arcCount - 1f)
                val angle = PI * (1.0 - t)
                val gemSize = if (i == arcCount / 2) 58.dp else 40.dp
                val cx = w * (0.5f + 0.40f * cos(angle).toFloat())
                val cy = h * (0.10f - 0.06f * sin(angle).toFloat())
                val glow = if (i % 2 == 0) twinkle else (1.55f - twinkle)
                Image(
                    painter = painterResource(Assets.item(i)),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = cx - gemSize / 2, y = cy - gemSize / 2)
                        .size(gemSize)
                        .alpha(glow.coerceIn(0.4f, 1f))
                )
            }

            // 2) The logo hangs under the arc like a theatre sign and sways gently.
            Image(
                painter = painterResource(Assets.logo),
                contentDescription = "Jokino Jewels",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = h * 0.14f)
                    .fillMaxWidth(0.9f)
                    .aspectRatio(Assets.LOGO_ASPECT)
                    .graphicsLayer {
                        rotationZ = sway * 0.35f
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                    }
            )
            Text(
                text = "Juggle the gems. Match the treasure.",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = JewelPalette.Cream,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = h * 0.14f + w * 0.9f / Assets.LOGO_ASPECT + 4.dp)
                    .padding(horizontal = 24.dp)
            )

            // 3) Jokino steps in from the left wing, presenting the stage.
            Image(
                painter = painterResource(Assets.mascotA),
                contentDescription = "Jokino the jester presenting a ruby",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = -(w * 0.10f), y = -(h * 0.15f) - (bob * 10f).dp)
                    .height(h * 0.37f)
                    .aspectRatio(Assets.MASCOT_A_ASPECT)
                    .graphicsLayer {
                        rotationZ = sway * 0.5f
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                    }
            )

            // 4) His twin raises a floating ruby from the right wing.
            Image(
                painter = painterResource(Assets.mascotB),
                contentDescription = "A second jester raising a floating ruby",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = w * 0.06f, y = -(h * 0.15f) + (bob * 10f).dp)
                    .height(h * 0.36f)
                    .aspectRatio(Assets.MASCOT_B_ASPECT)
                    .graphicsLayer {
                        rotationZ = -sway * 0.5f
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                    }
            )

            // 5) String of jewels + ornate Start plate.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiamondPips(
                    total = progress.totalLevels,
                    filled = progress.levelsCleared,
                    description = "Pairs levels cleared: ${progress.levelsCleared} of ${progress.totalLevels}"
                )
                MenuPlateButton(
                    text = "START",
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(0.72f)
                )
            }
        }
    }
}
