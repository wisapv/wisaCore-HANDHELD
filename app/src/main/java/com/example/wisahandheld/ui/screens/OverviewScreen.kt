package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.components.BoxPackageIcon
import com.example.wisahandheld.ui.components.PrimaryButton
import com.example.wisahandheld.ui.components.Sparkle
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.Muted

/** Screen 1 — shown once before Login, on every cold start of the app. */
@Composable
fun OverviewScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(28.dp)
    ) {
        Sparkle(modifier = Modifier.align(Alignment.TopEnd))

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Ink, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BoxPackageIcon(tint = Lemon, sizeDp = 24.dp)
                }
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "Count faster.\nMiss nothing.",
                    color = Ink,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 34.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "งานของคุณถูกจัดไว้ให้แล้วจากเว็บ เปิดแอปแล้วเริ่มนับได้เลย",
                    color = Muted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
            PrimaryButton(text = "Get started", onClick = onGetStarted)
        }
    }
}
