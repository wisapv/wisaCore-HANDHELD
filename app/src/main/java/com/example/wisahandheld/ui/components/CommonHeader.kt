package com.example.wisahandheld.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 28.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}