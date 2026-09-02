package com.example.wisahandheld.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light theme matching the wisaCore web app (ink text on a warm off-white
// canvas, lime accent) — replaces the old dark "PML Tech" orange scheme.
private val LightColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Lemon,
    secondary = Muted,
    tertiary = Lemon,
    background = Canvas,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    error = ErrorText
)

@Composable
fun WISAHANDHELDTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Canvas.toArgb()
            // Light background → dark status bar icons/text.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
