package com.example.scorecounter.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp

@Composable
fun VolleyballGlyph(size: Dp, color: Color) {
    Canvas(modifier = Modifier.size(size)) {
        val r = minOf(this.size.width, this.size.height) / 2f
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val strokeW = r * 0.09f

        drawIntoCanvas { canvas ->
            val fPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = strokeW
                this.color = color.copy(alpha = 0.85f).toArgb()
                strokeCap = android.graphics.Paint.Cap.ROUND
            }
            val path1 = android.graphics.Path().apply {
                moveTo(cx, cy - r * 0.87f)
                quadTo(cx - r * 0.6f, cy, cx, cy + r * 0.87f)
            }
            val path2 = android.graphics.Path().apply {
                moveTo(cx - r * 0.87f, cy - r * 0.44f)
                quadTo(cx, cy - r * 0.1f, cx + r * 0.87f, cy - r * 0.44f)
            }
            val path3 = android.graphics.Path().apply {
                moveTo(cx - r * 0.87f, cy + r * 0.44f)
                quadTo(cx, cy + r * 0.1f, cx + r * 0.87f, cy + r * 0.44f)
            }
            canvas.nativeCanvas.drawPath(path1, fPaint)
            canvas.nativeCanvas.drawPath(path2, fPaint)
            canvas.nativeCanvas.drawPath(path3, fPaint)
        }
    }
}
