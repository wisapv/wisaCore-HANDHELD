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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
    val glowGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFFF6B00).copy(alpha = 0.3f), Color(0xFF0A0A0A)),
        center = Offset(500f, 200f),
        radius = 1500f
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).background(glowGradient)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // --- โซนเนื้อหา (Header + รายการเลื่อน) ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Profile Icon & Text)
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(bottom = 8.dp) // 🌟 ปรับลดจาก 16.dp เหลือ 8.dp เพื่อให้ข้อความขยับขึ้นไปใกล้ไอคอนมากขึ้น
                            .size(130.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .blur(30.dp)
                                .background(Color(0xFFFF6B00).copy(alpha = 0.6f), shape = CircleShape)
                        )

                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .border(2.dp, Color(0xFFFF6B00), CircleShape)
                                .background(Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFFFFFFFF),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }

                    Text(
                        text = "Select Your PIC",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                // LazyColumn (รายการ PIC)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(picList.size) { index ->
                        DarkPicCard(text = picList[index], onClick = { onPicSelected(picList[index]) })
                    }
                }
            }

            // --- โซนแถบเมนูด้านล่าง (Solid Bottom Bar) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0A0A))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ปุ่ม Back แบบวงกลม (Circular FAB)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFF6B00),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DarkPicCard(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scaleAnim"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFFFF6B00).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
        label = "colorAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFFF6B00),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}