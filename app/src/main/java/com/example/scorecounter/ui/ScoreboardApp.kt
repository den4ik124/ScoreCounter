package com.example.scorecounter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorecounter.model.Screen
import com.example.scorecounter.ui.screen.GameScreen
import com.example.scorecounter.ui.screen.ModeSelectionScreen
import com.example.scorecounter.ui.screen.SetupScreen
import com.example.scorecounter.ui.theme.AppTheme
import com.example.scorecounter.ui.theme.MidnightTheme
import com.example.scorecounter.viewmodel.ScoreboardViewModel

@Composable
fun ScoreboardApp(
    viewModel: ScoreboardViewModel = viewModel(),
    speak: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val theme = MidnightTheme

    Surface(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        color = Color.Transparent
    ) {
        AnimatedContent(
            targetState = state.screen,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                if (forward) slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                else slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            },
            label = "screen"
        ) { screen ->
            ThemedBackground(theme) {
                when (screen) {
                    Screen.SETUP -> SetupScreen(
                        theme = theme,
                        onNext = { a, b ->
                            viewModel.setTeamNames(a, b)
                            viewModel.navigateToMode()
                        }
                    )
                    Screen.MODE -> ModeSelectionScreen(
                        theme = theme,
                        teamAName = state.teamAName,
                        teamBName = state.teamBName,
                        onBack = viewModel::goBack,
                        onPick = viewModel::startGame
                    )
                    Screen.GAME -> GameScreen(
                        state = state,
                        theme = theme,
                        onAddPoint = viewModel::addPoint,
                        onUndo = viewModel::undoPoint,
                        onSwapServe = viewModel::swapServe,
                        onReset = viewModel::resetGame,
                        onQuit = viewModel::restart,
                        speak = speak
                    )
                }
            }
        }
    }
}

@Composable
fun ThemedBackground(theme: AppTheme, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(theme.bgTop, theme.bgBottom)))
    ) {
        if (theme.isDark) DotsPattern()
        content()
    }
}

@Composable
private fun DotsPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = 24.dp.toPx()
        val radius = 1.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            var y = 0f
            while (y <= size.height) {
                drawCircle(Color.White.copy(alpha = 0.045f), radius, Offset(x, y))
                y += spacing
            }
            x += spacing
        }
    }
}
