package com.example.scorecounter.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scorecounter.model.AppSettings
import com.example.scorecounter.ui.ThemedBackground
import com.example.scorecounter.ui.component.ChipButton
import com.example.scorecounter.ui.component.clickableNoRipple
import com.example.scorecounter.ui.theme.AppTheme
import com.example.scorecounter.ui.theme.Baloo2
import com.example.scorecounter.ui.theme.MidnightTheme
import com.example.scorecounter.ui.theme.OceanPalette
import com.example.scorecounter.ui.theme.ScoreCounterTheme
import com.example.scorecounter.ui.theme.SpaceGrotesk

private const val DELAY_MIN_MS = 0
private const val DELAY_MAX_MS = 5_000

private fun formatDelay(ms: Int): String = when {
    ms == 0 -> "Off"
    ms % 1_000 == 0 -> "${ms / 1_000} s"
    else -> "${"%.1f".format(ms / 1_000.0)} s"
}

@Composable
fun SettingsScreen(
    theme: AppTheme,
    settings: AppSettings,
    onBack: () -> Unit,
    onIncreaseDelay: () -> Unit,
    onDecreaseDelay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        ChipButton(theme = theme, onClick = onBack) {
            Text(
                "← Back",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = theme.fg
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Settings",
            fontFamily = Baloo2, fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp, color = theme.fg, letterSpacing = (-0.02).sp
        )
        Text(
            text = "Adjust how the app behaves during play",
            fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, color = theme.mutedFg
        )

        Spacer(Modifier.height(18.dp))

        AnnouncementDelayCard(
            theme = theme,
            delayMs = settings.announcementDelayMs,
            onIncrease = onIncreaseDelay,
            onDecrease = onDecreaseDelay
        )
    }
}

@Composable
private fun AnnouncementDelayCard(
    theme: AppTheme,
    delayMs: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surface, RoundedCornerShape(18.dp))
            .border(1.dp, theme.surfaceBorder, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(OceanPalette.base.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🔊", fontSize = 26.sp)
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Announcement delay",
                fontFamily = Baloo2, fontWeight = FontWeight.Bold,
                fontSize = 18.sp, color = theme.fg, lineHeight = 20.sp
            )
            Text(
                text = if (delayMs == 0) "Announces immediately" else "Waits ${formatDelay(delayMs)} before speaking",
                fontFamily = SpaceGrotesk, fontSize = 13.sp, color = theme.mutedFg
            )
        }

        Row(
            modifier = Modifier
                .background(theme.chip, RoundedCornerShape(10.dp))
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            StepperButton(theme = theme, label = "−", enabled = delayMs > DELAY_MIN_MS, onClick = onDecrease)
            Text(
                text = formatDelay(delayMs),
                fontFamily = Baloo2, fontWeight = FontWeight.Bold,
                fontSize = 15.sp, color = theme.fg,
                modifier = Modifier.width(44.dp),
                textAlign = TextAlign.Center
            )
            StepperButton(theme = theme, label = "+", enabled = delayMs < DELAY_MAX_MS, onClick = onIncrease)
        }
    }
}

@Composable
private fun StepperButton(theme: AppTheme, label: String, enabled: Boolean = true, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickableNoRipple(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontFamily = SpaceGrotesk,
            fontSize = 16.sp,
            color = if (enabled) theme.fg else theme.mutedFg,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    ScoreCounterTheme {
        ThemedBackground(MidnightTheme) {
            SettingsScreen(
                theme = MidnightTheme,
                settings = AppSettings(announcementDelayMs = 1_000),
                onBack = {},
                onIncreaseDelay = {},
                onDecreaseDelay = {}
            )
        }
    }
}
