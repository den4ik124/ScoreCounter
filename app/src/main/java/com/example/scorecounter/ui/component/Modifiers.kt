package com.example.scorecounter.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
internal fun Modifier.clickableNoRipple(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    this.clickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )

internal fun Modifier.swipeToScore(onSwipeUp: () -> Unit, onSwipeDown: () -> Unit): Modifier =
    this.pointerInput(onSwipeUp, onSwipeDown) {
        var totalDy = 0f
        var triggered = false
        detectVerticalDragGestures(
            onDragStart = { totalDy = 0f; triggered = false },
            onDragEnd = { totalDy = 0f; triggered = false },
            onDragCancel = { totalDy = 0f; triggered = false }
        ) { change, dragAmount ->
            change.consume()
            if (!triggered) {
                totalDy += dragAmount
                if (abs(totalDy) > 80f) {
                    triggered = true
                    if (totalDy < 0) onSwipeUp() else onSwipeDown()
                }
            }
        }
    }

internal fun Modifier.coloredGlow(color: Color): Modifier =
    this.drawBehind {
        drawRoundRect(
            color = color.copy(alpha = 0.22f),
            topLeft = Offset(-8.dp.toPx(), -8.dp.toPx()),
            size = Size(size.width + 16.dp.toPx(), size.height + 16.dp.toPx()),
            cornerRadius = CornerRadius(30.dp.toPx())
        )
    }
