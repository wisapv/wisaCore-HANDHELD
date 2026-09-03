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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.components.BackButton
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.LemonBadgeText
import com.example.wisahandheld.ui.theme.LemonSoft
import com.example.wisahandheld.ui.theme.Muted

/**
 * One zone assigned to this device from the web's AssignHandheld page —
 * a PIC + ShortAddr group. Several jobs can share the same PIC (that PIC's
 * addresses split across multiple devices), which is why `pic` is just a
 * label here, not something this device filters or chooses by.
 */
data class ZoneJob(val code: String, val pic: String, val itemCount: Int)

/** Screen 5 — "Part list": the assigned work for this device. */
@Composable
fun PartListScreen(deviceCode: String, jobs: List<ZoneJob>, onSelectJob: (ZoneJob) -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(20.dp)
    ) {
        Text(text = "Part list", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Text(text = "เครื่อง $deviceCode · ${jobs.size} zones", color = Muted, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (jobs.isEmpty()) {
            Text(text = "ยังไม่มีงานที่ assign มาให้เครื่องนี้", color = Muted, fontSize = 11.sp)
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            items(jobs) { job ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(14.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                        .clickable { onSelectJob(job) }
                        .padding(13.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = job.code,
                            color = Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${job.itemCount} items",
                            color = LemonBadgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .background(LemonSoft, RoundedCornerShape(7.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "PIC: ${job.pic}", color = Muted, fontSize = 10.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        BackButton(onClick = onBack)
    }
}