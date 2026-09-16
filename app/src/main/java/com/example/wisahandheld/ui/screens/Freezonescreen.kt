package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.data.KbnQr
import com.example.wisahandheld.data.ParsedKbn
import com.example.wisahandheld.ui.components.BackButton
import com.example.wisahandheld.ui.components.BoxPackageIcon
import com.example.wisahandheld.ui.components.ScanFrameIcon
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.LemonBadgeText
import com.example.wisahandheld.ui.theme.LemonSoft
import com.example.wisahandheld.ui.theme.Muted

/**
 * One scanned box's Kanban QR — the raw text (sent to the backend, which
 * re-decodes it as the authoritative source of truth) plus a best-effort
 * client-side parse (KbnQr.parse) purely for on-screen feedback.
 *
 * `parsed` can be null even for a perfectly valid scan: KbnQr.parse expects
 * an exact 80-char string with an 8-char address at a fixed position,
 * while the backend's own decoder (decodeLocalFreeZoneQr) treats the
 * address as "everything left" — a longer address is valid server-side but
 * fails this client-side parse. A parse failure must never drop a real
 * scan, so it's still queued and sent — just shown without the part-no/box
 * breakdown, since that's all client-side preview can't be sure of here.
 */
data class FreeZoneScan(val raw: String, val parsed: ParsedKbn?)

/**
 * Free Zone — open scan, not tied to any assignment or address list. Every
 * physical box has its own Kanban tag with a QR code; scanning it is the
 * count (see the Zone Assignment Rules discussion — Free Zone has no part
 * list to match against, unlike Fix Zone). Summing multiple boxes for the
 * same order+part already happens server-side (Process Stock's addQty is a
 * true accumulator over every handheld_free_zone_scans row) — this screen
 * only needs to get each box's raw QR text there once.
 *
 * A real Zebra scanner in keyboard-wedge (DataWedge) mode types straight
 * into whatever text field has focus and sends Enter — same pattern as
 * AddressDetailScreen's own scan field, built to work with that as-is.
 */
@Composable
fun FreeZoneScreen(zoneCodes: List<String>, onSend: (List<String>) -> Unit, onBack: () -> Unit) {
    val scans = remember { mutableStateListOf<FreeZoneScan>() }
    var scanInput by remember { mutableStateOf("") }

    fun addScan(raw: String) {
        val code = raw.trim()
        if (code.isEmpty()) return
        scanInput = ""

        val parsed = KbnQr.parse(code)
        if (parsed != null) {
            // Re-scanning the same box (same order + Part No + box sequence)
            // corrects/replaces its entry rather than adding a duplicate —
            // matches the backend's own ON CONFLICT (batch_id, order_number,
            // part_no, box_seq) DO UPDATE semantics. Only possible to detect
            // here when the parse succeeded; an unparsed re-scan just adds
            // another entry, which is still fine — the backend's own upsert
            // key handles the real dedup regardless.
            val existing = scans.indexOfFirst {
                it.parsed?.orderNumber == parsed.orderNumber &&
                        it.parsed?.partNumber == parsed.partNumber &&
                        it.parsed?.boxSeq == parsed.boxSeq
            }
            if (existing >= 0) scans.removeAt(existing)
        }
        scans.add(0, FreeZoneScan(code, parsed))
    }

    fun removeScan(index: Int) {
        scans.removeAt(index)
    }

    val distinctParts = remember(scans.size) { scans.mapNotNull { it.parsed?.partNumber }.distinct().size }
    val totalQty = remember(scans.size) { scans.sumOf { it.parsed?.qtyPerBox ?: 0 } }
    val unpreviewedCount = remember(scans.size) { scans.count { it.parsed == null } }

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
        Text(text = "สแกน QR บนป้าย Kanban ของทุกกล่อง · ระบบรวมยอดให้อัตโนมัติ", color = Muted, fontSize = 9.sp)
        Text(text = "Zone: ${zoneCodes.joinToString(", ")}", color = Ink, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(10.dp))

        // Scan / type the Kanban QR text — Enter (or a scanner's injected
        // Enter) submits, same pattern as AddressDetailScreen's own field.
        // No red-border/blocking state here on purpose — see FreeZoneScan's
        // own doc comment on why a client-side parse failure must still be
        // accepted, not rejected.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite, RoundedCornerShape(12.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScanFrameIcon(tint = Muted, sizeDp = 16.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (scanInput.isEmpty()) {
                    Text(text = "สแกน QR บนป้าย Kanban", color = Muted, fontSize = 12.sp)
                }
                BasicTextField(
                    value = scanInput,
                    onValueChange = { scanInput = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    cursorBrush = SolidColor(Ink),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addScan(scanInput) }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Running totals. distinctParts/totalQty only count scans that
        // parsed client-side — an unparsed scan is still queued and will
        // still be sent and counted for real once the backend decodes it,
        // it just can't contribute to a preview number here.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(CardWhite, RoundedCornerShape(11.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(11.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(text = "$distinctParts", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Part No.", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Medium)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(CardWhite, RoundedCornerShape(11.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(11.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(text = "${scans.size}", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "กล่อง", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Medium)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Lemon, RoundedCornerShape(11.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(text = "$totalQty", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Qty รวม", color = Ink.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (unpreviewedCount > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$unpreviewedCount กล่องดูตัวอย่างไม่ได้ แต่จะยังถูกส่งไปตรวจสอบที่ระบบตามปกติ",
                color = Muted,
                fontSize = 8.5.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (scans.isEmpty()) {
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
                Text(text = "สแกน QR กล่องแรกเพื่อเริ่ม", color = Muted, fontSize = 9.5.sp)
            }
        } else {
            Text(text = "สแกนล่าสุดอยู่บนสุด", color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(scans) { scan ->
                    val index = scans.indexOf(scan)
                    val isNewest = index == 0
                    val p = scan.parsed
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
                            if (p != null) {
                                Text(text = p.partNumber, color = Ink, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "Box ${p.boxSeq}/${p.totalBoxes} · Order ${p.orderNumber}",
                                    color = Muted,
                                    fontSize = 8.5.sp
                                )
                            } else {
                                Text(text = "สแกนแล้ว (ดูตัวอย่างไม่ได้)", color = Ink, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "จะให้ระบบตรวจสอบตอนกด Send", color = Muted, fontSize = 8.5.sp)
                            }
                        }
                        if (p != null) {
                            Text(
                                text = "×${p.qtyPerBox}",
                                color = Ink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(LemonSoft, RoundedCornerShape(7.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 22.dp, height = 20.dp)
                                .background(Color_EDEFE8, RoundedCornerShape(6.dp))
                                .clickable { removeScan(index) },
                            contentAlignment = Alignment.Center
                        ) { Text(text = "×", color = Muted, fontSize = 12.sp) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (scans.isEmpty()) Ink.copy(alpha = 0.4f) else Ink, RoundedCornerShape(12.dp))
                .clickable(enabled = scans.isNotEmpty()) { onSend(scans.map { it.raw }) }
                .padding(vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (scans.isEmpty()) "Send" else "Send ทั้งหมด (${scans.size} กล่อง)",
                color = Lemon,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        BackButton(onClick = onBack)
    }
}

// Light gray-green used for the remove-scan button background (matches Box/Pcs/Seq fields).
private val Color_EDEFE8 = androidx.compose.ui.graphics.Color(0xFFEDEFE8)