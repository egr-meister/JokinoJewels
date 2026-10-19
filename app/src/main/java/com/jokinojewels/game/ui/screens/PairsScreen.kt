package com.jokinojewels.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jokinojewels.game.logic.GameLogic
import com.jokinojewels.game.ui.Assets
import com.jokinojewels.game.ui.LocalAppContainer
import com.jokinojewels.game.ui.components.FallbackScreen
import com.jokinojewels.game.ui.components.JewelScreen
import com.jokinojewels.game.ui.components.ModalOverlay
import com.jokinojewels.game.ui.components.PlateButton
import com.jokinojewels.game.ui.theme.JewelPalette
import com.jokinojewels.game.viewmodel.PairsCardUi
import com.jokinojewels.game.viewmodel.PairsOutcome
import com.jokinojewels.game.viewmodel.PairsUiState
import com.jokinojewels.game.viewmodel.PairsViewModel

@Composable
fun PairsScreen(
    level: Int?,
    onFinished: (level: Int, won: Boolean, matched: Int, total: Int) -> Unit,
    onMenu: () -> Unit,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: PairsViewModel = viewModel(
        factory = PairsViewModel.Factory(container.repository, container.soundManager, level ?: -1)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (!state.isValid) {
        FallbackScreen(message = "This level doesn't exist. Pick one from the level map.", onBack = onBack)
        return
    }
    if (state.isLocked) {
        FallbackScreen(message = "This level is still locked. Clear the previous level first.", onBack = onBack)
        return
    }

    val latestOnFinished by rememberUpdatedState(onFinished)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(state.outcome) {
        val outcome = state.outcome ?: return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            latestOnFinished(state.level, outcome == PairsOutcome.WIN, state.matchedPairs, state.totalPairs)
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.pause() }
    BackHandler(enabled = state.isReady && !state.isPaused && state.outcome == null) { viewModel.pause() }

    Box(Modifier.fillMaxSize()) {
        JewelScreen(
            title = "Level ${state.level}",
            onBack = onBack,
            onPause = if (state.isReady) ({ viewModel.pause() }) else null,
            scrollable = false
        ) {
            if (!state.isReady) {
                Text(
                    text = "Shuffling the jewels…",
                    style = MaterialTheme.typography.titleMedium,
                    color = JewelPalette.Cream
                )
            } else {
                PairsHud(state)
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val columns = state.columns.coerceAtLeast(1)
                    val rows = ((state.cards.size + columns - 1) / columns).coerceAtLeast(1)
                    val gap = 10.dp
                    val cardAspect = 0.76f
                    val byWidth = (maxWidth - gap * (columns - 1)) / columns
                    val byHeight = ((maxHeight - gap * (rows - 1)) / rows) * cardAspect
                    val cardWidth = minOf(byWidth, byHeight, 220.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                        state.cards.chunked(columns).forEach { rowCards ->
                            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                                rowCards.forEach { card ->
                                    MemoryCard(
                                        card = card,
                                        modifier = Modifier.width(cardWidth).aspectRatio(cardAspect),
                                        onTap = { viewModel.onCardTap(card.index) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.isPaused && state.outcome == null) {
            ModalOverlay(title = "Paused", onDismissRequest = { viewModel.resume() }) {
                Text(
                    text = "Time left ${GameLogic.formatTime(state.secondsLeft)} · Pairs ${state.matchedPairs}/${state.totalPairs}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = JewelPalette.Cream,
                    textAlign = TextAlign.Center
                )
                PlateButton(text = "Continue", onClick = { viewModel.resume() }, modifier = Modifier.fillMaxWidth())
                PlateButton(text = "Restart", onClick = { viewModel.restart() }, modifier = Modifier.fillMaxWidth())
                PlateButton(text = "Menu", onClick = onMenu, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun PairsHud(state: PairsUiState) {
    val urgent = state.secondsLeft <= 10
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HudPill(
                label = if (urgent) "Hurry!" else null,
                icon = Assets.item(7),
                value = GameLogic.formatTime(state.secondsLeft),
                accent = if (urgent) JewelPalette.Ruby else JewelPalette.SapphireBright,
                description = "Time left: ${state.secondsLeft} seconds" + if (urgent) ", hurry" else ""
            )
            HudPill(
                label = "Pairs",
                icon = null,
                value = "${state.matchedPairs}/${state.totalPairs}",
                accent = JewelPalette.JesterGold,
                description = "Pairs matched: ${state.matchedPairs} of ${state.totalPairs}"
            )
        }
        LinearProgressIndicator(
            progress = { state.timeFraction },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = if (urgent) JewelPalette.Ruby else JewelPalette.SapphireBright,
            trackColor = JewelPalette.Navy
        )
    }
}

@Composable
private fun HudPill(label: String?, icon: Int?, value: String, accent: Color, description: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(JewelPalette.Midnight.copy(alpha = 0.85f))
            .border(2.dp, accent, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            Image(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
        }
        if (label != null) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (accent == JewelPalette.Ruby) JewelPalette.Cream else JewelPalette.JesterGold)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MemoryCard(card: PairsCardUi, modifier: Modifier, onTap: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFaceUp) 180f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "card-flip"
    )
    val name = Assets.itemName(card.itemIndex)
    val description = when {
        card.isMatched -> "$name, matched"
        card.isFaceUp -> name
        else -> "Card ${card.index + 1}, face down"
    }
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(shape)
            .clickable(enabled = !card.isFaceUp, role = Role.Button, onClickLabel = "Flip card", onClick = onTap)
            .semantics { contentDescription = description }
    ) {
        if (rotation <= 90f) {
            CardBack(Modifier.fillMaxSize())
        } else {
            CardFace(card = card, modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f })
        }
    }
}

/** Card back like a playing card from the jester's deck: sapphire gradient, gold rim, logo. */
@Composable
private fun CardBack(modifier: Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(JewelPalette.Sapphire, JewelPalette.SapphireDeep, JewelPalette.Navy))
            )
            .border(2.dp, JewelPalette.JesterGold, shape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(6.dp)) {
            drawRoundRect(
                color = JewelPalette.JesterGold.copy(alpha = 0.35f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Image(
            painter = painterResource(Assets.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.66f).aspectRatio(Assets.LOGO_ASPECT)
        )
    }
}

@Composable
private fun CardFace(card: PairsCardUi, modifier: Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.radialGradient(listOf(JewelPalette.SapphireBright, JewelPalette.Sapphire, JewelPalette.SapphireDeep))
            )
            .border(if (card.isMatched) 4.dp else 2.dp, if (card.isMatched) JewelPalette.JesterGold else JewelPalette.GoldDeep, shape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Assets.item(card.itemIndex)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.78f)
        )
        if (card.isMatched) {
            // Non-color "matched" cue: a check badge in the corner.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(JewelPalette.EmeraldDeep),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
