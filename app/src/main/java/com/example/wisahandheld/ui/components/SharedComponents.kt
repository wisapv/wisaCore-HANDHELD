package com.example.wisahandheld.ui.screens // หรือตังเป็น components ก็ได้ครับ

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun GlassCardComponent(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFFFF6B00) else Color.White.copy(alpha = 0.08f),
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BackButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.BottomStart) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() }.padding(8.dp)
        ) {
            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Back", color = Color.White.copy(alpha = 0.7f), fontSize = 17.sp)
        }
    }
}

// ใน ui/components/SharedComponents.kt

@Composable
fun GlassCardComponent(
    text: String,
    accentColor: Color = Color(0xFFFF6B00), // เพิ่ม parameter สี (Default เป็นส้ม)
    isFullWidth: Boolean = false,           // เพิ่มตัวเลือกให้ขยายเต็มความกว้าง
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val bgColor by animateColorAsState(
        targetValue = if (isPressed) accentColor else Color.White.copy(alpha = 0.05f)
    )

    Box(
        modifier = Modifier
            .then(if (isFullWidth) Modifier.fillMaxWidth() else Modifier.fillMaxWidth())
            .height(if (isFullWidth) 70.dp else 80.dp) // ถ้าเป็นแถวยาวให้เตี้ยลงหน่อย
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isPressed) Color.White else Color.White.copy(alpha = 0.9f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}