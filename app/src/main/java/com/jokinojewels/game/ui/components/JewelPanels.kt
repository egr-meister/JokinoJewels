package com.jokinojewels.game.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jokinojewels.game.ui.Assets
import com.jokinojewels.game.ui.theme.JewelPalette

/** Full-bleed velvet background image with a darkening veil for text contrast. */
@Composable
fun GameBackground(
    modifier: Modifier = Modifier,
    dim: Float = 0.3f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier.fillMaxSize().background(JewelPalette.Midnight)) {
        Image(
            painter = painterResource(Assets.bgMain),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = dim * 0.5f), Color.Black.copy(alpha = dim))
                    )
                )
        )
        content()
    }
}

/**
 * Modal / info panel DRAWN IN COMPOSE: rounded rectangle, plum→velvet gradient,
 * gold accent border. Grows with its content — no stretched frame image.
 *
 * Use [scrollable] = true only when the parent gives a bounded height (e.g. overlays).
 */
@Composable
fun JewelPanel(
    title: String?,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(JewelPalette.SapphireDeep, JewelPalette.Navy, JewelPalette.Midnight)
                )
            )
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    listOf(JewelPalette.JesterGold, JewelPalette.GoldDeep, JewelPalette.JesterGold)
                ),
                shape = shape
            )
            .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(horizontal = 22.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = JewelPalette.JesterGold,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
            GemDivider()
        }
        content()
    }
}

/**
 * Full-screen modal: dark scrim that swallows touches + centred [JewelPanel].
 * System Back calls [onDismissRequest] when provided.
 */
@Composable
fun ModalOverlay(
    title: String,
    onDismissRequest: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onDismissRequest != null) BackHandler(onBack = onDismissRequest)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JewelPalette.Scrim)
            .pointerInput(Unit) { detectTapGestures() }
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        JewelPanel(
            title = title,
            modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
            scrollable = true,
            content = content
        )
    }
}

/** Standard screen: background, safe insets, top bar and a (optionally scrolling) column. */
@Composable
fun JewelScreen(
    title: String,
    onBack: (() -> Unit)?,
    onPause: (() -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    GameBackground {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            GameTopBar(title = title, onBack = onBack, onPause = onPause)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

@Composable
fun GameTopBar(title: String, onBack: (() -> Unit)?, onPause: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            RoundIconButton(icon = Assets.iconBack, contentDescription = "Back", onClick = onBack, size = 56.dp)
        } else {
            Spacer(Modifier.size(56.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                shadow = Shadow(Color.Black.copy(alpha = 0.7f), Offset(0f, 3f), 8f)
            ),
            color = JewelPalette.JesterGold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp).semantics { heading() }
        )
        if (onPause != null) {
            RoundIconButton(icon = Assets.iconPause, contentDescription = "Pause", onClick = onPause, size = 56.dp)
        } else {
            Spacer(Modifier.size(56.dp))
        }
    }
}

/** Friendly fallback for invalid navigation args / missing data. Never crashes. */
@Composable
fun FallbackScreen(message: String, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    GameBackground {
        Box(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            JewelPanel(
                title = "A jewel went missing",
                modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth(),
                scrollable = true
            ) {
                Image(
                    painter = painterResource(Assets.mascotC),
                    contentDescription = null,
                    modifier = Modifier.height(150.dp).aspectRatio(Assets.MASCOT_C_ASPECT)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = JewelPalette.Cream,
                    textAlign = TextAlign.Center
                )
                PlateButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth(0.8f))
            }
        }
    }
}

/** Gold line with a centred diamond. Decorative only. */
@Composable
fun GemDivider(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth(0.7f).height(14.dp)) {
        val cy = size.height / 2f
        val cx = size.width / 2f
        val d = size.height / 2f
        val stroke = 2.dp.toPx()
        drawLine(JewelPalette.GoldDeep, Offset(0f, cy), Offset(cx - d * 1.8f, cy), strokeWidth = stroke)
        drawLine(JewelPalette.GoldDeep, Offset(cx + d * 1.8f, cy), Offset(size.width, cy), strokeWidth = stroke)
        drawPath(diamondPath(cx, cy, d), JewelPalette.JesterGold)
    }
}

/** A row of diamond pips: filled gold = earned. Exposes a text alternative. */
@Composable
fun DiamondPips(
    total: Int,
    filled: Int,
    description: String,
    modifier: Modifier = Modifier,
    pipSize: Dp = 14.dp,
    gap: Dp = 6.dp
) {
    val count = total.coerceIn(0, 24)
    val lit = filled.coerceIn(0, count)
    val rowWidth = pipSize * count + gap * (count - 1).coerceAtLeast(0)
    Canvas(
        modifier
            .width(rowWidth)
            .height(pipSize)
            .semantics { contentDescription = description }
    ) {
        val s = pipSize.toPx()
        val g = gap.toPx()
        for (i in 0 until count) {
            val cx = i * (s + g) + s / 2f
            val path = diamondPath(cx, s / 2f, s / 2f)
            if (i < lit) {
                drawPath(path, JewelPalette.JesterGold)
            } else {
                drawPath(path, JewelPalette.Cream.copy(alpha = 0.55f), style = Stroke(width = 1.5.dp.toPx()))
            }
        }
    }
}

/** Padlock glyph drawn in code (locked levels) — shape cue, not just color. */
@Composable
fun LockGlyph(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.27f, h * 0.08f),
            size = Size(w * 0.46f, h * 0.7f),
            style = Stroke(width = w * 0.11f)
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.14f, h * 0.43f),
            size = Size(w * 0.72f, h * 0.52f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
        )
        drawCircle(JewelPalette.Navy, radius = w * 0.07f, center = Offset(w / 2f, h * 0.66f))
    }
}

internal fun diamondPath(cx: Float, cy: Float, r: Float): Path = Path().apply {
    moveTo(cx, cy - r)
    lineTo(cx + r, cy)
    lineTo(cx, cy + r)
    lineTo(cx - r, cy)
    close()
}
