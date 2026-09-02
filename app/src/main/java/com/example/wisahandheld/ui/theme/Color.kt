package com.example.wisahandheld.ui.theme

import androidx.compose.ui.graphics.Color

// Same palette as the wisaCore web app (frontend/tailwind.config.js) —
// keeping the two apps visually consistent.
val Ink = Color(0xFF14140F)      // near-black text / dark surfaces
val Lemon = Color(0xFFD7FF3F)    // accent lime-green
val Canvas = Color(0xFFF3F2ED)   // page background
val CardWhite = Color(0xFFFFFFFF)
val Muted = Color(0xFF9B9890)    // secondary / muted text

val BorderLight = Ink.copy(alpha = 0.08f)
val LemonSoft = Lemon.copy(alpha = 0.3f)   // tinted badge backgrounds
val LemonBadgeText = Color(0xFF5C6B0A)     // readable text on LemonSoft

// Grayish-green used for "you must type this yourself" fields (Box/Pcs/Seq
// on Input Stock) — distinct from Lemon, which marks scanned/auto-filled.
val ManualAccent = Color(0xFF9BAE8C)

val SuccessText = Color(0xFF3B6D11)
val ErrorText = Color(0xFFA32D2D)
