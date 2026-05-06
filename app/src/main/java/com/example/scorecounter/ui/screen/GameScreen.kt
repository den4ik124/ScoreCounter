package com.example.scorecounter.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scorecounter.model.GameState
import com.example.scorecounter.model.Screen
import com.example.scorecounter.ui.ThemedBackground
import com.example.scorecounter.ui.component.ActionButton
import com.example.scorecounter.ui.component.ChipButton
import com.example.scorecounter.ui.component.coloredGlow
import com.example.scorecounter.ui.component.swipeToScore
import com.example.scorecounter.ui.theme.AppTheme
import com.example.scorecounter.ui.theme.Baloo2
import com.example.scorecounter.ui.theme.JetBrainsMono
import com.example.scorecounter.ui.theme.MidnightTheme
import com.example.scorecounter.ui.theme.OceanPalette
import com.example.scorecounter.ui.theme.ScoreCounterTheme
import com.example.scorecounter.ui.theme.SpaceGrotesk
import com.example.scorecounter.ui.theme.SunsetPalette
import com.example.scorecounter.ui.theme.TeamPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun firstTeamAnnouncement(state: GameState): String =
    if (state.servingTeamA) "${state.teamAName} ${state.scoreA}"
    else "${state.teamBName} ${state.scoreB}"

private fun secondTeamAnnouncement(state: GameState): String =
    if (state.servingTeamA) "${state.teamBName} ${state.scoreB}"
    else "${state.teamAName} ${state.scoreA}"

@Composable
fun GameScreen(
    state: GameState,
    theme: AppTheme,
    announcementDelayMs: Int = 0,
    onAddPoint: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onSwapServe: () -> Unit,
    onReset: () -> Unit,
    onQuit: () -> Unit,
    speak: (String) -> Unit,
    speakAppend: (String) -> Unit
) {
    val view = LocalView.current
    DisposableEffect(Unit) { view.keepScreenOn = true; onDispose { view.keepScreenOn = false } }
    val scope = rememberCoroutineScope()

    // Timer
    var startTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) { delay(1000); nowMs = System.currentTimeMillis() }
    }
    LaunchedEffect(state.scoreA, state.scoreB) {
        if (state.scoreA == 0 && state.scoreB == 0) startTimeMs = System.currentTimeMillis()
    }
    val elapsed = ((nowMs - startTimeMs) / 1000).toInt()
    val timerText = "%02d:%02d".format(elapsed / 60, elapsed % 60)

    // TTS — speaks the serving team first, waits [announcementDelayMs], then the other team.
    // Score is suppressed when a winner is declared so only "X wins!" is spoken.
    var prevScores by remember { mutableStateOf(Pair(state.scoreA, state.scoreB)) }
    LaunchedEffect(state.scoreA, state.scoreB) {
        val current = Pair(state.scoreA, state.scoreB)
        if (prevScores != current) {
            if (state.winner == null) {
                speak(firstTeamAnnouncement(state))
                delay(announcementDelayMs.toLong())
                speakAppend(secondTeamAnnouncement(state))
            }
            prevScores = current
        }
    }
    LaunchedEffect(state.winner) { state.winner?.let { speak("$it wins!") } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChipButton(theme = theme, onClick = onQuit) {
                    Text("×", fontFamily = SpaceGrotesk, fontSize = 16.sp, color = theme.fg, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier
                        .background(theme.chip, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = timerText,
                        fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium,
                        fontSize = 13.sp, color = theme.fg
                    )
                    Box(Modifier.size(3.dp).clip(CircleShape).background(theme.mutedFg))
                    Text(
                        text = "FIRST TO ${state.targetScore}",
                        fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp, color = theme.mutedFg, letterSpacing = 0.04.sp
                    )
                }
                ChipButton(
                    theme = theme,
                    enabled = state.undoHistory.isNotEmpty(),
                    onClick = onUndo
                ) {
                    Text(
                        "↺", fontFamily = SpaceGrotesk, fontSize = 16.sp,
                        color = if (state.undoHistory.isNotEmpty()) theme.fg else theme.mutedFg,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Team panels
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TeamPanel(
                        team = state.teamAName,
                        palette = OceanPalette,
                        score = state.scoreA,
                        target = state.targetScore,
                        isServing = state.servingTeamA && state.winner == null,
                        isLeader = state.scoreA > state.scoreB && state.winner == null,
                        isWinner = state.winner == state.teamAName,
                        buttonsEnabled = state.winner == null,
                        isDark = theme.isDark,
                        onUp = { onAddPoint(true) },
                        onDown = { onUndo() },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                    TeamPanel(
                        team = state.teamBName,
                        palette = SunsetPalette,
                        score = state.scoreB,
                        target = state.targetScore,
                        isServing = !state.servingTeamA && state.winner == null,
                        isLeader = state.scoreB > state.scoreA && state.winner == null,
                        isWinner = state.winner == state.teamBName,
                        buttonsEnabled = state.winner == null,
                        isDark = theme.isDark,
                        onUp = { onAddPoint(false) },
                        onDown = { onUndo() },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
                // VS pill
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(if (theme.isDark) Color(0xFF0B0F1A) else Color.White)
                        .border(1.5.dp, theme.surfaceBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "VS", fontFamily = Baloo2, fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp, color = theme.fg, letterSpacing = 0.06.sp
                    )
                }
            }

            // Bottom actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(theme = theme, label = "Reset", modifier = Modifier.weight(1f), onClick = onReset)
                ActionButton(theme = theme, label = "Swap serve", modifier = Modifier.weight(1f), onClick = onSwapServe)
                ActionButton(
                    theme = theme,
                    label = "🔊",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        scope.launch {
                            speak(firstTeamAnnouncement(state))
                            delay(announcementDelayMs.toLong())
                            speakAppend(secondTeamAnnouncement(state))
                        }
                    }
                )
            }
        }

        if (state.winner != null) {
            WinnerOverlay(
                winner = state.winner,
                winnerPalette = if (state.winner == state.teamAName) OceanPalette else SunsetPalette,
                scoreA = state.scoreA,
                scoreB = state.scoreB,
                theme = theme,
                onRematch = onReset
            )
        }
    }
}

@Composable
fun TeamPanel(
    team: String,
    palette: TeamPalette,
    score: Int,
    target: Int,
    isServing: Boolean,
    isLeader: Boolean,
    isWinner: Boolean,
    buttonsEnabled: Boolean,
    isDark: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val progressAnim by animateFloatAsState(
        targetValue = (score.toFloat() / target).coerceIn(0f, 1f),
        animationSpec = tween(350),
        label = "progress"
    )
    val borderColor = if (isLeader) palette.base else palette.base.copy(alpha = 0.33f)
    val borderWidth = if (isLeader) 2.dp else 1.5.dp
    val panelBg = if (isDark)
        Brush.verticalGradient(listOf(palette.deep, Color(0xFF0B0F1A)))
    else
        Brush.verticalGradient(listOf(palette.soft, palette.base.copy(alpha = 0.1f)))
    val scoreColor = if (isDark) Color.White else palette.deep
    val textColor = if (isDark) Color.White else palette.deep

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(panelBg)
            .border(borderWidth, borderColor, RoundedCornerShape(28.dp))
            .then(if (isLeader) Modifier.coloredGlow(palette.base) else Modifier)
            .swipeToScore(
                onSwipeUp = {
                    if (buttonsEnabled) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onUp()
                    }
                },
                onSwipeDown = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDown()
                }
            )
            .padding(horizontal = 16.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(palette.base)
            )
            Text(
                text = team,
                fontFamily = Baloo2, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.01).sp
            )
        }

        if (isServing) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .background(palette.base, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                Text(
                    "SERVING",
                    fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold,
                    fontSize = 10.sp, color = Color.White, letterSpacing = 0.1.sp
                )
            }
        } else {
            Spacer(Modifier.height(22.dp))
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    (scaleIn(tween(200), initialScale = 1.1f) + fadeIn(tween(200))) togetherWith
                            (scaleOut(tween(150), targetScale = 0.9f) + fadeOut(tween(150)))
                },
                label = "score_$team"
            ) { s ->
                Text(
                    text = s.toString(),
                    fontFamily = Baloo2,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (s > 99) 100.sp else 120.sp,
                    color = scoreColor,
                    lineHeight = 108.sp,
                    letterSpacing = (-0.05).sp
                )
            }
            if (isWinner) {
                Text(
                    "🏆", fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                )
            }
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black.copy(alpha = if (isDark) 0.18f else 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressAnim)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.base)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Swipe hint
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("↑", fontSize = 13.sp, color = scoreColor.copy(alpha = 0.6f))
            Text(
                " Score  ·  ",
                fontFamily = SpaceGrotesk, fontSize = 11.sp,
                color = scoreColor.copy(alpha = 0.6f), fontWeight = FontWeight.Medium
            )
            Text("↓", fontSize = 13.sp, color = scoreColor.copy(alpha = 0.6f))
            Text(
                " Undo",
                fontFamily = SpaceGrotesk, fontSize = 11.sp,
                color = scoreColor.copy(alpha = 0.6f), fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun WinnerOverlay(
    winner: String,
    winnerPalette: TeamPalette,
    scoreA: Int,
    scoreB: Int,
    theme: AppTheme,
    onRematch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth()
                .background(theme.surface, RoundedCornerShape(24.dp))
                .border(1.5.dp, winnerPalette.base, RoundedCornerShape(24.dp))
                .padding(horizontal = 26.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏆", fontSize = 48.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = winner,
                fontFamily = Baloo2, fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp, color = winnerPalette.deep, textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "wins $scoreA–$scoreB",
                fontFamily = SpaceGrotesk, fontSize = 13.sp, color = theme.mutedFg
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onRematch,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = winnerPalette.base)
            ) {
                Text(
                    "Rematch", fontFamily = Baloo2,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun GameScreenPreview() {
    ScoreCounterTheme {
        ThemedBackground(MidnightTheme) {
            GameScreen(
                state = GameState(
                    teamAName = "Sharks", teamBName = "Eagles",
                    scoreA = 14, scoreB = 11, targetScore = 25,
                    servingTeamA = true, screen = Screen.GAME
                ),
                theme = MidnightTheme,
                onAddPoint = {}, onUndo = {}, onSwapServe = {},
                onReset = {}, onQuit = {}, speak = {}, speakAppend = {}
            )
        }
    }
}
