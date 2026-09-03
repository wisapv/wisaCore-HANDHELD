package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.wisahandheld.data.Api
import com.example.wisahandheld.ui.components.BackButton
import com.example.wisahandheld.ui.components.PhoneDeviceIcon
import com.example.wisahandheld.ui.components.Sparkle
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.Muted

/**
 * Screen 2 — device identity. Instead of typing a code (easy to mistype),
 * this lists every device already registered active on the web's Handheld
 * Devices page — tap one to log in as that device. In a real build, save
 * the choice to DataStore/SharedPreferences after a successful login so
 * the app can skip straight to CheckIn/Home on future launches (Prefs.kt
 * already does this — see MainActivity).
 */
@Composable
fun LoginScreen(onLoggedIn: (deviceCode: String) -> Unit, onBack: () -> Unit) {
    var status by remember { mutableStateOf("loading") } // loading | ready | empty | error
    var devices by remember { mutableStateOf<List<Api.Device>>(emptyList()) }
    var reloadTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(reloadTrigger) {
        status = "loading"
        val result = Api.fetchActiveDevices()
        if (result == null) {
            status = "error"
        } else {
            devices = result
            status = if (result.isEmpty()) "empty" else "ready"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(28.dp)
    ) {
        BackButton(onClick = onBack, modifier = Modifier.align(Alignment.BottomStart))
        Sparkle(modifier = Modifier.align(Alignment.TopEnd))

        Column(modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Ink, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    PhoneDeviceIcon(tint = Lemon, sizeDp = 24.dp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "เลือกเครื่องนี้", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "แตะชื่อเครื่องที่ตรงกับสติกเกอร์บนตัวเครื่องนี้",
                    color = Muted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(22.dp))

            when (status) {
                "loading" -> Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = "กำลังโหลดรายชื่อเครื่อง…", color = Muted, fontSize = 12.sp)
                }

                "error" -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).clickable { reloadTrigger++ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "โหลดรายชื่อเครื่องไม่สำเร็จ (แตะเพื่อลองใหม่)", color = Ink, fontSize = 12.sp, textAlign = TextAlign.Center)
                }

                "empty" -> Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).clickable { reloadTrigger++ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ยังไม่มีเครื่อง active ในระบบ — ไปเพิ่มที่หน้า Handheld Devices บนเว็บก่อน (แตะเพื่อลองใหม่)",
                        color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(devices) { device ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardWhite, RoundedCornerShape(13.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(13.dp))
                                .clickable { onLoggedIn(device.id) }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(text = device.name, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}