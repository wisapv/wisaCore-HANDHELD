package com.example.wisahandheld.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wisahandheld.ui.theme.Lemon

/**
 * The small lime "sparkle" diamond used as a decorative accent across the
 * wisaCore web app (frontend/src/components/Sparkle.jsx) — same 4-point
 * diamond shape. Kept static here for reliability; wrap it in your own
 * rememberInfiniteTransition + graphicsLayer scale if you want it to pulse
 * like the web version does.
 */
@Composable
fun Sparkle(modifier: Modifier = Modifier, sizeDp: Dp = 10.dp) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val w = sizeDp.toPx()
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w * 0.61f, w * 0.39f)
            lineTo(w, w * 0.5f)
            lineTo(w * 0.61f, w * 0.61f)
            lineTo(w * 0.5f, w)
            lineTo(w * 0.39f, w * 0.61f)
            lineTo(0f, w * 0.5f)
            lineTo(w * 0.39f, w * 0.39f)
            close()
        }
        drawPath(path, color = Lemon)
    }
}
