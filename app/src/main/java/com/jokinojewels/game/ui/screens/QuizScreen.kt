package com.jokinojewels.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.jokinojewels.game.viewmodel.QuizUiState
import com.jokinojewels.game.viewmodel.QuizViewModel

@Composable
fun QuizScreen(
    categoryId: String?,
    onFinished: (categoryId: String, score: Int, total: Int) -> Unit,
    onMenu: () -> Unit,
    onBack: () -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: QuizViewModel = viewModel(
        factory = QuizViewModel.Factory(container.repository, container.soundManager, categoryId.orEmpty())
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (!state.isValid) {
        FallbackScreen(
            message = "This quiz level could not be found. It may have been renamed in an update.",
            onBack = onBack
        )
        return
    }

    val latestOnFinished by rememberUpdatedState(onFinished)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            // Navigate only once the screen is RESUMED (e.g. not while the app is in background).
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                latestOnFinished(state.categoryId, state.score, state.total)
            }
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.pause() }
    BackHandler(enabled = !state.isPaused && !state.isFinished) { viewModel.pause() }

    Box(Modifier.fillMaxSize()) {
        JewelScreen(
            title = "${state.categoryTitle} Quiz",
            onBack = { viewModel.pause() },
            onPause = { viewModel.pause() },
            scrollable = false
        ) {
            QuizProgress(state)
            val question = state.question
            if (question != null) {
                // Everything is sized from the real space left on screen: no scrolling.
                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                    val gap = 8.dp
                    val frameWidth = minOf(maxWidth, maxHeight * 0.30f * Assets.FRAME_QUESTION_ASPECT)
                    val frameHeight = frameWidth / Assets.FRAME_QUESTION_ASPECT
                    val plateWidth = minOf(maxWidth * 0.96f, maxHeight * 0.115f * Assets.BTN_PLATE_ASPECT)
                    val plateHeight = plateWidth / Assets.BTN_PLATE_ASPECT
                    val used = frameHeight + (plateHeight + gap) * question.options.size
                    val spare = (maxHeight - used).coerceAtLeast(0.dp)

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(gap)
                    ) {
                        QuestionFrame(
                            text = question.text,
                            index = state.questionIndex,
                            modifier = Modifier.width(frameWidth).height(frameHeight)
                        )
                        question.options.forEachIndexed { index, option ->
                            val revealed = state.isRevealed
                            val isCorrect = index == question.correctIndex
                            val isPicked = index == state.selectedIndex
                            PlateButton(
                                text = option,
                                onClick = { viewModel.answer(index) },
                                modifier = Modifier.width(plateWidth),
                                highlight = when {
                                    revealed && isCorrect -> JewelPalette.EmeraldDeep
                                    revealed && isPicked -> JewelPalette.RubyDeep
                                    else -> null
                                },
                                leadingMark = when {
                                    revealed && isCorrect -> "✓"
                                    revealed && isPicked -> "✗"
                                    else -> null
                                },
                                stateDescription = when {
                                    revealed && isCorrect -> "Correct answer"
                                    revealed && isPicked -> "Wrong answer"
                                    else -> null
                                },
                                textFraction = 0.27f,
                                horizontalPaddingFraction = 0.11f,
                                maxLines = 2,
                                playClick = false
                            )
                        }
                        if (spare >= 72.dp) {
                            CheeringJesters(
                                state = state,
                                correctIndex = question.correctIndex,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (state.isPaused && !state.isFinished) {
            ModalOverlay(title = "Paused", onDismissRequest = { viewModel.resume() }) {
                Text(
                    text = "${state.categoryTitle} · Question ${state.progressLabel}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = JewelPalette.Cream
                )
                PlateButton(text = "Continue", onClick = { viewModel.resume() }, modifier = Modifier.fillMaxWidth())
                PlateButton(text = "Restart", onClick = { viewModel.restart() }, modifier = Modifier.fillMaxWidth())
                PlateButton(text = "Menu", onClick = onMenu, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/** The question sits inside the gold-and-sapphire frame (its aspect ratio is kept). */
@Composable
private fun QuestionFrame(text: String, index: Int, modifier: Modifier) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(Assets.frameQuestion),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        val fontScale = LocalDensity.current.fontScale
        val base = (maxHeight.value * 0.105f).coerceIn(14f, 24f)
        val sizeSp = (base * fontScale.coerceIn(0.85f, 1.25f) / fontScale).sp
        Text(
            text = text,
            color = JewelPalette.Cream,
            fontWeight = FontWeight.SemiBold,
            fontSize = sizeSp,
            lineHeight = sizeSp * 1.22f,
            textAlign = TextAlign.Center,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.6f), Offset(0f, 2f), 5f)),
            modifier = Modifier
                .padding(horizontal = maxWidth * 0.11f, vertical = maxHeight * 0.13f)
                .semantics { heading(); contentDescription = "Question ${index + 1}: $text" }
        )
    }
}

/**
 * Two jesters fill the free space under the answers. They sway idly,
 * jump for joy on a correct pick and droop a little on a wrong one.
 */
@Composable
private fun CheeringJesters(state: QuizUiState, correctIndex: Int, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "quiz-jesters")
    val sway by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway"
    )
    val correct = state.isRevealed && state.selectedIndex == correctIndex
    val wrong = state.isRevealed && !correct
    val jump by animateFloatAsState(
        targetValue = when {
            correct -> -18f
            wrong -> 6f
            else -> 0f
        },
        animationSpec = spring(dampingRatio = 0.45f),
        label = "jump"
    )
    val scale by animateFloatAsState(
        targetValue = if (correct) 1.08f else if (wrong) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "scale"
    )
    // Rotate through the six jester poses as the quiz progresses.
    val poses = listOf(
        Assets.mascotD to Assets.MASCOT_D_ASPECT,
        Assets.mascotB to Assets.MASCOT_B_ASPECT,
        Assets.mascotA to Assets.MASCOT_A_ASPECT,
        Assets.mascotF to Assets.MASCOT_F_ASPECT,
        Assets.mascotC to Assets.MASCOT_C_ASPECT,
        Assets.mascotE to Assets.MASCOT_E_ASPECT
    )
    val left = poses[(state.questionIndex * 2) % poses.size]
    val right = poses[(state.questionIndex * 2 + 1) % poses.size]
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        JesterFigure(left.first, left.second, jump, scale, sway, Modifier.weight(1f))
        JesterFigure(right.first, right.second, jump, scale, -sway, Modifier.weight(1f))
    }
}

@Composable
private fun JesterFigure(
    res: Int,
    aspect: Float,
    jump: Float,
    scale: Float,
    sway: Float,
    modifier: Modifier
) {
    Box(modifier.fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(aspect)
                .offset(y = jump.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = sway
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
        )
    }
}

@Composable
private fun QuizProgress(state: QuizUiState) {
    val answered = state.questionIndex + if (state.isRevealed) 1 else 0
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Question",
                style = MaterialTheme.typography.labelLarge,
                color = JewelPalette.Cream,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = state.progressLabel,
                style = MaterialTheme.typography.titleMedium,
                color = JewelPalette.JesterGold,
                modifier = Modifier.semantics {
                    contentDescription = "Question ${state.questionIndex + 1} of ${state.total}"
                }
            )
        }
        LinearProgressIndicator(
            progress = { GameLogic.quizProgress(answered, state.total) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = JewelPalette.JesterGold,
            trackColor = JewelPalette.Navy
        )
    }
}
