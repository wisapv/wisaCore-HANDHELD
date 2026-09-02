package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.components.BoxedInput
import com.example.wisahandheld.ui.components.PrimaryButton
import com.example.wisahandheld.ui.components.Sparkle
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Muted

/**
 * Screen 2 — device identity. The code entered here must match a device
 * name already registered on the web app's Handheld Devices page
 * (backend/handheld_part_list/deviceRoute.js). In a real build, save this
 * to DataStore/SharedPreferences after the first successful login so the
 * app can skip straight to CheckIn/Home on future launches.
 */
@Composable
fun LoginScreen(onLoggedIn: (deviceCode: String) -> Unit) {
    var deviceCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(28.dp)
    ) {
        Sparkle(modifier = Modifier.align(Alignment.TopEnd))

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Ink, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📱", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "รหัสเครื่อง", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ใส่รหัสที่ได้ตอน register เครื่องนี้ไว้บนเว็บ",
                color = Muted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(22.dp))
            BoxedInput(
                value = deviceCode,
                onValueChange = { deviceCode = it.uppercase() },
                placeholder = "HH-01",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            PrimaryButton(
                text = "เข้าสู่ระบบ",
                onClick = { if (deviceCode.isNotBlank()) onLoggedIn(deviceCode) }
            )
        }
    }
}
