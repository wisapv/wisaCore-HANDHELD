package com.example.wisahandheld.ui.theme // เช็คให้ตรงกับโปรเจคของคุณนะครับ

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// บังคับใช้ DarkColorScheme สำหรับธีม Tech
private val DarkColorScheme = darkColorScheme(
    primary = OrangePML,           // สีหลักคือส้ม
    secondary = GrayText,
    tertiary = OrangeDark,
    background = DarkBg,           // พื้นหลังดำ
    surface = CardBg,              // พื้นผิวการ์ดเทาเข้ม
    onPrimary = Color.White,
    onBackground = WhiteText,
    onSurface = WhiteText,
    error = ErrorRed
)

@Composable
fun WISAHANDHELDTheme(
    // บังคับเป็น Dark Theme เสมอ (ลบระบบ dynamicColor ออกเพื่อให้คุมโทน)
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // ตั้งค่าสี Status Bar (แถบบนสุดของเครื่อง) ให้เป็นสีดำเข้ากับแอป
            window.statusBarColor = DarkBg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // ใช้ค่ามาตรฐานจากไฟล์ Type.kt
        content = content
    )
}