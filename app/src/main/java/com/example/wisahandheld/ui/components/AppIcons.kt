package com.example.wisahandheld.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Small hand-drawn line icons (thin stroke, rounded caps) used instead of
 * emoji. Emoji glyphs are rendered by the OS/OEM emoji font, which looks
 * completely different (and often much flatter/uglier) on an Android
 * device or emulator than it does on the web — these draw identically
 * everywhere since they're just paths, not font glyphs. Style matches the
 * thin outline icons ("ti-" tabler icons) used in the design mockups.
 */

private fun lineStyle(widthPx: Float) = Stroke(width = widthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)

@Composable
fun BoxPackageIcon(tint: Color, modifier: Modifier = Modifier, sizeDp: Dp = 22.dp) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val w = sizeDp.toPx()
        val strokeW = w * 0.09f
        val margin = w * 0.12f

        drawRoundRect(
            color = tint,
            topLeft = Offset(margin, margin * 1.6f),
            size = Size(w - margin * 2, w - margin * 2.6f),
            cornerRadius = CornerRadius(w * 0.08f),
            style = lineStyle(strokeW)
        )
        drawLine(tint, Offset(margin, w * 0.42f), Offset(w - margin, w * 0.42f), strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.5f, w * 0.42f), Offset(w * 0.5f, w - margin * 1.3f), strokeWidth = strokeW, cap = StrokeCap.Round)
    }
}

@Composable
fun PhoneDeviceIcon(tint: Color, modifier: Modifier = Modifier, sizeDp: Dp = 22.dp) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val w = sizeDp.toPx()
        val strokeW = w * 0.09f
        val marginX = w * 0.22f

        drawRoundRect(
            color = tint,
            topLeft = Offset(marginX, w * 0.08f),
            size = Size(w - marginX * 2, w * 0.84f),
            cornerRadius = CornerRadius(w * 0.14f),
            style = lineStyle(strokeW)
        )
        drawCircle(color = tint, radius = strokeW * 0.9f, center = Offset(w * 0.5f, w * 0.82f))
    }
}

@Composable
fun IdCardIcon(tint: Color, modifier: Modifier = Modifier, sizeDp: Dp = 22.dp) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val w = sizeDp.toPx()
        val strokeW = w * 0.09f

        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.08f, w * 0.2f),
            size = Size(w * 0.84f, w * 0.6f),
            cornerRadius = CornerRadius(w * 0.1f),
            style = lineStyle(strokeW)
        )
        drawCircle(color = tint, radius = w * 0.09f, center = Offset(w * 0.32f, w * 0.5f))
        drawLine(tint, Offset(w * 0.5f, w * 0.42f), Offset(w * 0.78f, w * 0.42f), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.5f, w * 0.58f), Offset(w * 0.7f, w * 0.58f), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
    }
}

@Composable
fun ChecklistIcon(tint: Color, modifier: Modifier = Modifier, sizeDp: Dp = 22.dp) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val w = sizeDp.toPx()
        val strokeW = w * 0.09f
        val rows = listOf(w * 0.28f, w * 0.5f, w * 0.72f)
        rows.forEach { y ->
            drawRoundRect(
                color = tint,
                topLeft = Offset(w * 0.14f, y - strokeW * 0.7f),
                size = Size(strokeW * 1.4f, strokeW * 1.4f),
                cornerRadius = CornerRadius(strokeW * 0.4f),
                style = lineStyle(strokeW * 0.7f)
            )
            drawLine(tint, Offset(w * 0.32f, y), Offset(w * 0.86f, y), strokeWidth = strokeW * 0.8f, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun ScanFrameIcon(tint: Color, modifier: Modifier = Modifier, sizeDp: Dp = 22.dp) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val w = sizeDp.toPx()
        val strokeW = w * 0.09f
        val corner = w * 0.22f
        val m = w * 0.1f

        fun cornerBracket(x: Float, y: Float, dx: Float, dy: Float) {
            val path = Path().apply {
                moveTo(x, y + dy * corner)
                lineTo(x, y)
                lineTo(x + dx * corner, y)
            }
            drawPath(path, color = tint, style = lineStyle(strokeW))
        }
        cornerBracket(m, m, 1f, 1f)
        cornerBracket(w - m, m, -1f, 1f)
        cornerBracket(m, w - m, 1f, -1f)
        cornerBracket(w - m, w - m, -1f, -1f)
        drawLine(tint, Offset(m, w * 0.5f), Offset(w - m, w * 0.5f), strokeWidth = strokeW * 0.7f, cap = StrokeCap.Round)
    }
}
