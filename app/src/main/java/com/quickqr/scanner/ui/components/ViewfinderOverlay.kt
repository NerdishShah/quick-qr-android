package com.quickqr.scanner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.quickqr.scanner.ui.theme.ScanAccent
import com.quickqr.scanner.ui.theme.ScanBackground
import com.quickqr.scanner.ui.theme.ScanCorner

@Composable
fun ViewfinderOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val boxSize = size.minDimension * 0.65f
        val left = (size.width - boxSize) / 2f
        val top = (size.height - boxSize) / 2.4f
        val rect = Rect(left, top, left + boxSize, top + boxSize)
        val hole = Path().apply {
            addRoundRect(RoundRect(rect, CornerRadius(24.dp.toPx(), 24.dp.toPx())))
        }

        clipPath(hole, clipOp = ClipOp.Difference) {
            drawRect(ScanBackground.copy(alpha = 0.55f))
        }

        val stroke = 4.dp.toPx()
        val arm = boxSize * 0.18f
        val color = ScanCorner

        // Corner brackets
        fun corner(x0: Float, y0: Float, dx: Float, dy: Float) {
            drawLine(color, Offset(x0, y0), Offset(x0 + dx * arm, y0), stroke)
            drawLine(color, Offset(x0, y0), Offset(x0, y0 + dy * arm), stroke)
        }
        corner(rect.left, rect.top, 1f, 1f)
        corner(rect.right, rect.top, -1f, 1f)
        corner(rect.left, rect.bottom, 1f, -1f)
        corner(rect.right, rect.bottom, -1f, -1f)

        drawRoundRect(
            color = ScanAccent.copy(alpha = 0.35f),
            topLeft = Offset(rect.left, rect.top),
            size = Size(rect.width, rect.height),
            cornerRadius = CornerRadius(24.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}
