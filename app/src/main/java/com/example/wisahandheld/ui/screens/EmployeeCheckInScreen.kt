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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.components.BackButton
import com.example.wisahandheld.ui.components.BoxedInput
import com.example.wisahandheld.ui.components.IdCardIcon
import com.example.wisahandheld.ui.components.PrimaryButton
import com.example.wisahandheld.ui.components.Sparkle
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.Muted

/**
 * Screen 3 — "who's holding this device right now." Separate from Login:
 * the device code rarely changes, but the person holding it can change
 * every shift. Reachable again any time via the "เปลี่ยนคน" action on
 * HomeScreen — never auto re-prompted.
 */
@Composable
fun EmployeeCheckInScreen(deviceCode: String, onCheckedIn: (employeeId: String, phone: String) -> Unit, onBack: () -> Unit) {
    var employeeId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(28.dp)
    ) {
        BackButton(onClick = onBack, modifier = Modifier.align(Alignment.BottomStart))
        Sparkle(modifier = Modifier.align(Alignment.TopEnd))

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Lemon.copy(alpha = 0.35f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                IdCardIcon(tint = Ink, sizeDp = 24.dp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "ใครถือเครื่องนี้อยู่", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "เครื่อง $deviceCode · กดเปลี่ยนคนได้ทุกเมื่อ",
                color = Muted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(22.dp))
            BoxedInput(
                value = employeeId,
                // Digits only — this ID gets logged (handheld_checkins) and
                // is meant to be a numeric employee/badge number.
                onValueChange = { employeeId = it.filter { c -> c.isDigit() } },
                placeholder = "Employee No.",
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(10.dp))
            BoxedInput(
                value = phone,
                // Digits only, capped at 10 — a Thai mobile number is always exactly 10 digits.
                onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                placeholder = "เบอร์โทร (10 หลัก)",
                keyboardType = KeyboardType.Number
            )
            if (phone.isNotEmpty() && phone.length < 10) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "เบอร์โทรต้องครบ 10 หลัก (ตอนนี้ ${phone.length}/10)", color = Muted, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(18.dp))
            val canSubmit = employeeId.isNotBlank() && phone.length == 10
            PrimaryButton(
                text = "เริ่มกะทำงาน",
                onClick = { if (canSubmit) onCheckedIn(employeeId, phone) }
            )
        }
    }
}