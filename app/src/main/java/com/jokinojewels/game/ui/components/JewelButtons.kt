package com.jokinojewels.game.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jokinojewels.game.audio.Sfx
import com.jokinojewels.game.ui.Assets
import com.jokinojewels.game.ui.LocalAppContainer

/** Returns a lambda that plays the shared click SFX. */
@Composable
fun rememberClickSound(): () -> Unit {
    val soundManager = LocalAppContainer.current.soundManager
    return remember(soundManager) { { soundManager.play(Sfx.CLICK) } }
}

/**
 * The one reusable button: an image plate with the label drawn ON TOP in code,
 * white + bold. The plate keeps its own aspect ratio (no distortion); the label is
 * sized from the plate height and follows the user's font scale up to 1.3x.
 *
 * @param highlight optional fill over the plate (quiz green/red). Always pair it with
 *                  [leadingMark] / [stateDescription] so color is never the only signal.
 */
@Composable
fun PlateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    plate: Int = Assets.btnPlate,
    aspectRatio: Float = Assets.BTN_PLATE_ASPECT,
    enabled: Boolean = true,
    highlight: Color? = null,
    leadingMark: String? = null,
    stateDescription: String? = null,
    textFraction: Float = 0.34f,
    horizontalPaddingFraction: Float = 0.08f,
    maxLines: Int = 1,
    labelCenterY: Float = 0.47f,
    playClick: Boolean = true
) {
    val click = rememberClickSound()
    val shape = RoundedCornerShape(percent = 35)
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .heightIn(min = 48.dp)
            .clip(shape)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = text) {
                if (playClick) click()
                onClick()
            }
            .semantics {
                if (stateDescription != null) this.stateDescription = stateDescription
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(plate),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        if (highlight != null) {
            Box(
                Modifier
                    .matchParentSize()
                    // Inner blue area of btn_plate (see tools/import_assets.py).
                    .padding(
                        start = maxWidth * 0.085f,
                        end = maxWidth * 0.085f,
                        top = maxHeight * 0.12f,
                        bottom = maxHeight * 0.17f
                    )
                    .clip(shape)
                    .background(highlight.copy(alpha = 0.92f))
            )
        }
        val fontScale = LocalDensity.current.fontScale
        val effectiveScale = fontScale.coerceIn(0.85f, 1.3f)
        val sizeSp = (maxHeight.value * textFraction * effectiveScale / fontScale).sp
        Text(
            text = if (leadingMark != null) "$leadingMark  $text" else text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = sizeSp,
            lineHeight = sizeSp * 1.12f,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.65f), Offset(0f, 3f), 6f)),
            modifier = Modifier
                .offset(y = maxHeight * (labelCenterY - 0.5f))
                .padding(horizontal = maxWidth * horizontalPaddingFraction)
        )
    }
}

/** Ornate framed plate for the main / grand-menu buttons (≈0.7 of screen width). */
@Composable
fun MenuPlateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textFraction: Float = Assets.BTN_MENU_LABEL_H * 0.5f
) {
    PlateButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        plate = Assets.btnMenu,
        aspectRatio = Assets.BTN_MENU_ASPECT,
        textFraction = textFraction,
        horizontalPaddingFraction = 0.12f,
        labelCenterY = Assets.BTN_MENU_LABEL_Y
    )
}

/** Round image icon button (back / pause) with a ≥48dp touch target and a text alternative. */
@Composable
fun RoundIconButton(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    val click = rememberClickSound()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClickLabel = contentDescription) {
                click()
                onClick()
            }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )
    }
}
