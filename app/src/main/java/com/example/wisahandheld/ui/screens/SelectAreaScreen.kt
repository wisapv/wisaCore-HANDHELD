package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ใน ui/screens/SelectAreaScreen.kt

@Composable
fun SelectAreaScreen(picName: String, areaList: List<String>, onAreaSelected: (String) -> Unit, onBackClick: () -> Unit) {
    // 🌟 เปลี่ยนแสง Glow เป็นสีม่วง (Deep Purple)
    val purpleGlow = Brush.radialGradient(
        colors = listOf(Color(0xFFBF5AF2).copy(alpha = 0.35f), Color(0xFF0A0A0A)),
        center = Offset(500f, 300f),
        radius = 1400f
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).background(purpleGlow)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 64.dp)) {
            Text(text = "PIC: $picName", color = Color(0xFFBF5AF2), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Select Area", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)

            Spacer(modifier = Modifier.height(32.dp))

            // 🌟 กลับมาใช้ Grid แต่ปรับให้ดูเป็นเป้าหมาย (Target)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(areaList) { area ->
                    GlassCardComponent(
                        text = area,
                        accentColor = Color(0xFFBF5AF2), // ส่งสีม่วงเข้าไป
                        onClick = { onAreaSelected(area) }
                    )
                }
            }
        }
        BackButton(onClick = onBackClick)
    }
}