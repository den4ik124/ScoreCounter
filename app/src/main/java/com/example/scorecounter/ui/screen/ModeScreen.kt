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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scorecounter.ui.ThemedBackground
import com.example.scorecounter.ui.component.ChipButton
import com.example.scorecounter.ui.component.clickableNoRipple
import com.example.scorecounter.ui.theme.AppTheme
import com.example.scorecounter.ui.theme.Baloo2
import com.example.scorecounter.ui.theme.MidnightTheme
import com.example.scorecounter.ui.theme.OceanPalette
import com.example.scorecounter.ui.theme.ScoreCounterTheme
import com.example.scorecounter.ui.theme.SpaceGrotesk
import com.example.scorecounter.ui.theme.SunsetPalette

private data class ModeOption(
    val id: String, val label: String, val desc: String,
    val target: Int, val accentColor: Color, val icon: String
)

private val MODE_OPTIONS = listOf(
    ModeOption("short", "Short",    "Quick match · first to 15", 15,  Color(0xFFf4a33a), "⚡"),
    ModeOption("beach", "Beach",    "Beach volleyball · first to 21", 21, Color(0xFFe8824a), "🏖"),
    ModeOption("full",  "Full Set", "Official set · first to 25", 25,  Color(0xFF2a70d9), "🏐"),
    ModeOption("custom","Custom",   "Set your own target",        11,  Color(0xFF7c5cd9), "⚙"),
)

@Composable
fun ModeSelectionScreen(
    theme: AppTheme,
    teamAName: String,
    teamBName: String,
    onBack: () -> Unit,
    onPick: (Int) -> Unit
) {
    var customTarget by remember { mutableIntStateOf(11) }

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surface, RoundedCornerShape(16.dp))
                .border(1.dp, theme.surfaceBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(OceanPalette.base))
            Spacer(Modifier.width(10.dp))
            Text(
                text = teamAName,
                fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = theme.fg, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "VS",
                fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                color = theme.mutedFg, modifier = Modifier.padding(horizontal = 10.dp)
            )
            Text(
                text = teamBName,
                fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                color = theme.fg, maxLines = 1, overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End, modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(10.dp).clip(CircleShape).background(SunsetPalette.base))
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Game mode",
            fontFamily = Baloo2, fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp, color = theme.fg, letterSpacing = (-0.02).sp
        )
        Text(
            text = "How long should the set be?",
            fontFamily = SpaceGrotesk, fontWeight = FontWeight.Normal,
            fontSize = 14.sp, color = theme.mutedFg
        )

        Spacer(Modifier.height(18.dp))

        MODE_OPTIONS.forEach { mode ->
            ModeCard(
                mode = mode,
                theme = theme,
                customTarget = customTarget,
                onCustomChange = { customTarget = it },
                onClick = { onPick(if (mode.id == "custom") customTarget else mode.target) }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ModeCard(
    mode: ModeOption,
    theme: AppTheme,
    customTarget: Int,
    onCustomChange: (Int) -> Unit,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surface, RoundedCornerShape(18.dp))
            .border(1.dp, theme.surfaceBorder, RoundedCornerShape(18.dp))
            .clickableNoRipple(enabled = mode.id != "custom") {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(mode.accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(mode.icon, fontSize = 26.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.label,
                fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = theme.fg, lineHeight = 20.sp
            )
            Text(
                text = if (mode.id == "custom") "First to $customTarget" else mode.desc,
                fontFamily = SpaceGrotesk, fontSize = 13.sp, color = theme.mutedFg
            )
        }
        if (mode.id == "custom") {
            Row(
                modifier = Modifier
                    .background(theme.chip, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepperButton(theme, "−") { onCustomChange((customTarget - 1).coerceAtLeast(3)) }
                Text(
                    text = customTarget.toString(),
                    fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    color = theme.fg, modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )
                StepperButton(theme, "+") { onCustomChange((customTarget + 1).coerceAtMost(99)) }
            }
            Spacer(Modifier.width(8.dp))
            ChipButton(theme = theme, onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }) {
                Text(
                    "→",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = theme.fg
                )
            }
        } else {
            Text("→", fontFamily = SpaceGrotesk, fontSize = 20.sp, color = theme.mutedFg)
        }
    }
}

@Composable
private fun StepperButton(theme: AppTheme, label: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickableNoRipple {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontFamily = SpaceGrotesk,
            fontSize = 16.sp,
            color = theme.fg,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ModeSelectionScreenPreview() {
    ScoreCounterTheme {
        ThemedBackground(MidnightTheme) {
            ModeSelectionScreen(
                theme = MidnightTheme,
                teamAName = "Sharks", teamBName = "Eagles",
                onBack = {}, onPick = {}
            )
        }
    }
}
