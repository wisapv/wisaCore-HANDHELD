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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.components.BoxPackageIcon
import com.example.wisahandheld.ui.components.ScanFrameIcon
import com.example.wisahandheld.ui.components.Sparkle
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.LemonBadgeText
import com.example.wisahandheld.ui.theme.LemonSoft
import com.example.wisahandheld.ui.theme.Muted

/** One barcode's running box tally in Free Zone. Full boxes, so we count boxes, not pieces. */
data class FreeZoneItem(val barcode: String, var boxCount: Int)

/**
 * Free Zone — open scan, not tied to any assignment or list. Every scan is
 * a full box; scanning the same barcode again just adds another box. Items
 * are grouped by barcode, newest on top, with a running total. The +/-
 * controls are there to fix an over- or mis-scan.
 *
 * State here is in-memory only (a demo). In a real build, drive `items`
 * from a ViewModel and feed real scans in via DataWedge instead of the
 * simulateScan() helper.
 */
@Composable
fun FreeZoneScreen(onSend: (List<FreeZoneItem>) -> Unit, onBack: () -> Unit) {
    val items = remember { mutableStateListOf<FreeZoneItem>() }

    // Demo-only: cycles through a few fake barcodes so the button does
    // something without a real scanner. Delete once DataWedge is wired in.
    var demoIndex by remember { mutableStateOf(0) }
    val demoBarcodes = listOf("A001-4402", "B233-0091", "A118-7723")

    fun addScan(barcode: String) {
        val existing = items.indexOfFirst { it.barcode == barcode }
        if (existing >= 0) {
            val current = items.removeAt(existing)
            items.add(0, current.copy(boxCount = current.boxCount + 1))
        } else {
            items.add(0, FreeZoneItem(barcode, 1))
        }
    }

    fun changeBox(index: Int, delta: Int) {
        val item = items[index]
        val next = item.boxCount + delta
        if (next <= 0) items.removeAt(index)
        else items[index] = item.copy(boxCount = next)
    }

    val totalBoxes = items.sumOf { it.boxCount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Free Zone", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = "FULL BOX",
                color = LemonBadgeText,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(LemonSoft, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Text(text = "สแกนกล่องเต็ม · ระบบรวมยอดตามบาร์โค้ด", color = Muted, fontSize = 9.sp)
        Spacer(modifier = Modifier.height(10.dp))

        // Scan trigger.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink, RoundedCornerShape(14.dp))
                .clickable {
                    addScan(demoBarcodes[demoIndex % demoBarcodes.size])
                    demoIndex++
                }
                .padding(14.dp)
        ) {
            Sparkle(modifier = Modifier.align(Alignment.TopEnd), sizeDp = 8.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(34.dp).background(CardWhite.copy(alpha = 0.12f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) { ScanFrameIcon(tint = Lemon, sizeDp = 18.dp) }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (items.isEmpty()) "พร้อมสแกนกล่องแรก" else "พร้อมสแกนกล่องถัดไป",
                        color = CardWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(text = "กดปุ่มยิงบนเครื่อง Zebra", color = CardWhite.copy(alpha = 0.5f), fontSize = 8.5.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Running totals.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(CardWhite, RoundedCornerShape(11.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(11.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(text = "${items.size}", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "บาร์โค้ด", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Medium)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Lemon, RoundedCornerShape(11.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(text = "$totalBoxes", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "กล่องรวม", color = Ink.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (items.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Ink.copy(alpha = 0.05f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) { BoxPackageIcon(tint = Muted, sizeDp = 22.dp) }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "ยังไม่มีกล่องที่สแกน", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "ยิงบาร์โค้ดกล่องแรกเพื่อเริ่ม", color = Muted, fontSize = 9.5.sp)
            }
        } else {
            Text(text = "สแกนล่าสุดอยู่บนสุด", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(items) { item ->
                    val index = items.indexOf(item)
                    val isNewest = index == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardWhite, RoundedCornerShape(11.dp))
                            .border(
                                1.dp,
                                if (isNewest) Lemon.copy(alpha = 0.6f) else BorderLight,
                                RoundedCornerShape(11.dp)
                            )
                            .padding(horizontal = 11.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.barcode, color = Ink, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            if (isNewest) {
                                Text(text = "เพิ่งสแกน · +1 กล่อง", color = Muted, fontSize = 8.5.sp)
                            }
                        }
                        Text(text = "${item.boxCount}", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(width = 18.dp, height = 15.dp)
                                    .background(Ink, RoundedCornerShape(5.dp))
                                    .clickable { changeBox(index, +1) },
                                contentAlignment = Alignment.Center
                            ) { Text(text = "+", color = Lemon, fontSize = 11.sp) }
                            Box(
                                modifier = Modifier
                                    .size(width = 18.dp, height = 15.dp)
                                    .background(Color_EDEFE8, RoundedCornerShape(5.dp))
                                    .clickable { changeBox(index, -1) },
                                contentAlignment = Alignment.Center
                            ) { Text(text = "−", color = Muted, fontSize = 11.sp) }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (items.isEmpty()) Ink.copy(alpha = 0.4f) else Ink, RoundedCornerShape(12.dp))
                .clickable(enabled = items.isNotEmpty()) { onSend(items.toList()) }
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (items.isEmpty()) "Send" else "Send ทั้งหมด ($totalBoxes กล่อง)",
                color = Lemon,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Light gray-green used for the "−" button background (matches Box/Pcs/Seq fields).
private val Color_EDEFE8 = androidx.compose.ui.graphics.Color(0xFFEDEFE8)
