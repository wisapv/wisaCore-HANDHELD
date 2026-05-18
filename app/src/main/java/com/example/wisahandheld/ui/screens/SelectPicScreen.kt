package com.example.wisahandheld.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectPicScreen(
    shopName: String,
    picList: List<String>,
    onPicSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    // แสง Glow ตรงกลาง แบบเดียวกับหน้า Shop
    val glowGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFFF6B00).copy(alpha = 0.4f), Color(0xFF0A0A0A)),
        center = Offset(500f, 300f),
        radius = 1400f
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).background(glowGradient)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally // จัดกึ่งกลางแบบหน้า Shop
        ) {
            Text(
                text = "Shop $shopName",
                color = Color(0xFFFF6B00),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Select PIC",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // กลับมาใช้ Grid 2 คอลัมน์ แบบหน้า Shop
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(picList) { pic ->
                    // 🌟 เรียกใช้ปุ่มสีส้มล้วน
                    OrangeGlassCard(text = pic, onClick = { onPicSelected(pic) })
                }
            }
        }

        BackButtonComponent(onClick = onBackClick)
    }
}

// 🌟 Component ใหม่: ปุ่มสีส้มล้วน สไตล์เดียวกับหน้า Shop
@Composable
fun OrangeGlassCard(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // อนิเมชั่นเด้งดึ๋ง
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scaleAnim"
    )

    // 🌟 พื้นหลังเป็นสีส้มตลอดเวลา พอกดแล้วจะสีเข้มขึ้นนิดนึงให้ดูมีมิติ
    val bgColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFFCC5500) else Color(0xFFFF6B00),
        label = "colorAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp) // ขนาดกล่อง 85.dp เท่ากับหน้า Shop เป๊ะ
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)) // ขอบขาวใสๆ บางๆ
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun BackButtonComponent(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = "← Back",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(8.dp)
        )
    }
}