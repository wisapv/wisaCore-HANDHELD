package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.components.BackButton
import com.example.wisahandheld.ui.components.ChecklistIcon
import com.example.wisahandheld.ui.components.ScanFrameIcon
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.Muted

/** Screen 4 — landing page after check-in. Two entry points: Part list (assigned work) and Free zone (open scan). */
@Composable
fun HomeScreen(
    deviceCode: String,
    employeeName: String,
    phone: String,
    zonesToday: Int,
    remainingCount: Int,
    onOpenPartList: () -> Unit,
    onOpenFreeZone: () -> Unit,
    onChangePerson: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(20.dp)
    ) {
        // Who's holding the device, + a way to switch any time.
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(38.dp).background(Ink, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = employeeName.take(1), color = Lemon, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = employeeName, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(text = "$deviceCode · $phone", color = Muted, fontSize = 9.sp)
            }
            Text(
                text = "เปลี่ยนคน",
                color = Ink,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(CardWhite, RoundedCornerShape(8.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                    .clickable(onClick = onChangePerson)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick stats.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Ink, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(text = "$zonesToday", color = Lemon, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Text(text = "ZONES TODAY", color = CardWhite.copy(alpha = 0.55f), fontSize = 8.5.sp, fontWeight = FontWeight.Medium)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(CardWhite, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(text = "$remainingCount", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Text(text = "REMAINING", color = Muted, fontSize = 8.5.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(text = "START COUNTING", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        // Part list — the assigned work (from the web's AssignHandheld page).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Lemon, RoundedCornerShape(18.dp))
                .clickable(onClick = onOpenPartList)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(Ink.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { ChecklistIcon(tint = Ink, sizeDp = 19.dp) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Part list", color = Ink, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                Text(text = "$zonesToday zones assigned", color = Ink.copy(alpha = 0.6f), fontSize = 10.sp)
            }
            Text(text = "›", color = Ink, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Free zone — open scan, not tied to any assignment.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite, RoundedCornerShape(18.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                .clickable(onClick = onOpenFreeZone)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(Lemon.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { ScanFrameIcon(tint = Ink, sizeDp = 19.dp) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Free zone", color = Ink, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                Text(text = "สแกนอิสระ ไม่มี list กำหนด", color = Muted, fontSize = 10.sp)
            }
            Text(text = "›", color = Muted, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        BackButton(onClick = onBack)
    }
}