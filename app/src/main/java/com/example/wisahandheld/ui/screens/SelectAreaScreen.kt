package com.example.wisahandheld.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocationOn
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
fun SelectAreaScreen(
    picName: String,
    areaList: List<String>,
    onNextClick: (List<String>) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedAreas by remember { mutableStateOf(setOf<String>()) }

    val glowGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFFF6B00).copy(alpha = 0.3f), Color(0xFF0A0A0A)),
        center = Offset(500f, 200f),
        radius = 1500f
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).background(glowGradient)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- ส่วน Header ---
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp).size(130.dp)
                    ) {
                        Box(modifier = Modifier.size(100.dp).blur(30.dp).background(Color(0xFFFF6B00).copy(alpha = 0.6f), shape = CircleShape))
                        Box(
                            modifier = Modifier.size(96.dp).border(2.dp, Color(0xFFFF6B00), CircleShape).background(Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFFFFFFF),
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }

                    Text(
                        text = "PIC: $picName : Select Area",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp) // ระยะห่างจากปุ่มกำลังสวยตามที่ปรับไว้
                    )
                }

                // --- ส่วน Grid ปุ่มกด ---
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(17.dp),
                    verticalArrangement = Arrangement.spacedBy(17.dp),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(areaList) { area ->
                        val isSelected = selectedAreas.contains(area)
                        OrangeGridAreaCard(
                            text = area,
                            isSelected = isSelected,
                            onToggle = {
                                selectedAreas = if (isSelected) selectedAreas - area else selectedAreas + area
                            }
                        )
                    }
                }
            }

            // --- แถบเมนูด้านล่างทึบ ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0A0A))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ปุ่ม Back แบบวงกลมมินิมอล ขอบจางๆ
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

                    // 🌟 ปุ่ม Next ดีไซน์ใหม่: พื้นหลังดำทึบ + ขอบสีส้ม + ข้อความสีส้ม
                    AnimatedVisibility(
                        visible = selectedAreas.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF141416)) // พื้นหลังสีดำเข้ม (แมตช์กับสีปุ่มตอนกดเลือก)
                                .border(1.5.dp, Color(0xFFFF6B00), RoundedCornerShape(24.dp)) // เส้นขอบสีส้มหนากำลังดี
                                .clickable { onNextClick(selectedAreas.toList()) }
                                .padding(horizontal = 32.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Next →",
                                color = Color(0xFFFF6B00), // เปลี่ยนข้อความเป็นสีส้มตามสั่ง
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrangeGridAreaCard(text: String, isSelected: Boolean, onToggle: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "scaleAnim")

    val bgColor by animateColorAsState(
        targetValue = if (isSelected || isPressed) Color(0xFF141416) else Color(0xFFFF6B00),
        label = "bgColorAnim"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected || isPressed) Color.White.copy(alpha = 0.1f) else Color.Transparent,
        label = "borderColorAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = Color(0xFFFF6B00),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 8.dp)
                    .size(16.dp)
            )
        }

        Text(
            text = text,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}