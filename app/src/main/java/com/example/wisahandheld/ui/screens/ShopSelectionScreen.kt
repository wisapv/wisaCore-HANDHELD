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
fun ShopSelectionScreen(onShopSelected: (String) -> Unit = {}) {
    val shops = listOf("W", "T", "A", "R", "K", "QC", "TTAT", "Damage")
    var selectedShop by remember { mutableStateOf<String?>(null) }

    val glowGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFF6B00).copy(alpha = 0.4f),
            Color(0xFF0A0A0A)
        ),
        center = Offset(500f, 300f),
        radius = 1400f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .background(glowGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 64.dp),
            // 🌟 ปรับให้ข้อความและเนื้อหาอยู่กึ่งกลางหน้าจอ
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "wisaCore",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Select your target shop",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.2).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                // 🌟 เพิ่ม padding ซ้าย-ขวาตรงนี้เพื่อให้บล็อกปุ่มบีบแคบลงมาดูสวยงามขึ้น
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                items(shops) { shop ->
                    GlassShopCard(
                        shopName = shop,
                        isSelected = selectedShop == shop,
                        onClick = {
                            selectedShop = shop
                            onShopSelected(shop)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GlassShopCard(shopName: String, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected || isPressed) Color(0xFF141416) else Color(0xFFFF6B00),
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected || isPressed) Color.White.copy(alpha = 0.1f) else Color.Transparent,
        label = "borderColor"
    )

    Box(
        modifier = Modifier
            // 🌟 ปรับความสูงลดลงจาก 85.dp เหลือ 65.dp เพื่อให้ปุ่มดูสั้นและกระชับขึ้น
            .height(65.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = shopName,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}