package com.jokinojewels.game.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jokinojewels.game.AppConfig
import com.jokinojewels.game.BuildConfig
import com.jokinojewels.game.ui.Assets
import com.jokinojewels.game.ui.LocalAppContainer
import com.jokinojewels.game.ui.components.JewelScreen
import com.jokinojewels.game.ui.components.ModalOverlay
import com.jokinojewels.game.ui.components.PlateButton
import com.jokinojewels.game.ui.components.rememberClickSound
import com.jokinojewels.game.ui.theme.JewelPalette
import com.jokinojewels.game.viewmodel.ProgressViewModel

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: ProgressViewModel = viewModel(factory = ProgressViewModel.Factory(container.repository))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val click = rememberClickSound()

    var showPrivacy by rememberSaveable { mutableStateOf(false) }
    var showResetConfirm by rememberSaveable { mutableStateOf(false) }
    var resetDone by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        JewelScreen(title = "Settings", onBack = onBack) {
            Image(
                painter = painterResource(Assets.decoCap),
                contentDescription = null,
                modifier = Modifier.width(120.dp).aspectRatio(Assets.DECO_CAP_ASPECT)
            )
            val shape = RoundedCornerShape(20.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .clip(shape)
                    .background(JewelPalette.Navy.copy(alpha = 0.92f))
                    .border(2.dp, JewelPalette.GoldDeep, shape)
                    .toggleable(
                        value = state.soundEnabled,
                        role = Role.Switch,
                        onValueChange = { enabled ->
                            if (enabled) {
                                container.soundManager.setEnabled(true)
                                click()
                            }
                            viewModel.setSoundEnabled(enabled)
                        }
                    )
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Sound effects", style = MaterialTheme.typography.titleMedium, color = JewelPalette.Cream)
                    Text(
                        text = if (state.soundEnabled) "On" else "Off",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JewelPalette.JesterGold
                    )
                }
                Switch(
                    checked = state.soundEnabled,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JewelPalette.Midnight,
                        checkedTrackColor = JewelPalette.JesterGold,
                        uncheckedThumbColor = JewelPalette.Cream,
                        uncheckedTrackColor = JewelPalette.Navy,
                        uncheckedBorderColor = JewelPalette.GoldDeep
                    )
                )
            }

            PlateButton(text = "Privacy Policy", onClick = { showPrivacy = true }, modifier = Modifier.fillMaxWidth(0.9f))
            PlateButton(text = "Rate the Game", onClick = { openStorePage(context) }, modifier = Modifier.fillMaxWidth(0.9f))
            PlateButton(
                text = "Reset Progress",
                onClick = { resetDone = false; showResetConfirm = true },
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            if (resetDone) {
                Text(
                    text = "Progress reset. The vault is fresh again.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = JewelPalette.JesterGold,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = "Jokino Jewels ${BuildConfig.VERSION_NAME} · works fully offline",
                style = MaterialTheme.typography.labelMedium,
                color = JewelPalette.Cream,
                textAlign = TextAlign.Center
            )
            Image(
                painter = painterResource(Assets.mascotB),
                contentDescription = null,
                modifier = Modifier.height(160.dp).aspectRatio(Assets.MASCOT_B_ASPECT)
            )
        }

        if (showPrivacy) {
            ModalOverlay(title = "Privacy", onDismissRequest = { showPrivacy = false }) {
                Text(
                    text = AppConfig.PRIVACY_POLICY_TEXT,
                    style = MaterialTheme.typography.bodyLarge,
                    color = JewelPalette.Cream,
                    modifier = Modifier.fillMaxWidth()
                )
                if (AppConfig.PRIVACY_POLICY_URL.isNotBlank()) {
                    PlateButton(
                        text = "Open full policy",
                        onClick = { openUrl(context, AppConfig.PRIVACY_POLICY_URL) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                PlateButton(text = "Close", onClick = { showPrivacy = false }, modifier = Modifier.fillMaxWidth())
            }
        }

        if (showResetConfirm) {
            ModalOverlay(title = "Reset progress?", onDismissRequest = { showResetConfirm = false }) {
                Text(
                    text = "All best quiz scores and cleared levels will be erased. Levels 2–9 will lock again. This cannot be undone.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = JewelPalette.Cream,
                    textAlign = TextAlign.Center
                )
                PlateButton(
                    text = "Yes, reset",
                    onClick = {
                        viewModel.resetProgress()
                        showResetConfirm = false
                        resetDone = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    highlight = JewelPalette.RubyDeep
                )
                PlateButton(text = "Cancel", onClick = { showResetConfirm = false }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun openStorePage(context: Context) {
    val pkg = context.packageName
    if (!openUrl(context, "market://details?id=$pkg")) {
        openUrl(context, "https://play.google.com/store/apps/details?id=$pkg")
    }
}

/** Opens a link in another app (store / browser). Never crashes if nothing can handle it. */
private fun openUrl(context: Context, url: String): Boolean = try {
    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (e: Exception) {
    false
}
