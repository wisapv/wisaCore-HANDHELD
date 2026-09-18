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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.data.FreeZoneQueue
import com.example.wisahandheld.data.KbnQr
import com.example.wisahandheld.data.ParsedKbn
import com.example.wisahandheld.ui.components.BackButton
import com.example.wisahandheld.ui.components.BoxPackageIcon
import com.example.wisahandheld.ui.components.ScanFrameIcon
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.ErrorText
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.LemonBadgeText
import com.example.wisahandheld.ui.theme.LemonSoft
import com.example.wisahandheld.ui.theme.Muted
import com.example.wisahandheld.ui.theme.SuccessText
import kotlinx.coroutines.launch

/** One raw Kanban QR grouped with how many times it's been scanned — see FreeZoneQueue.addScan for why duplicates are counted, not collapsed. `parsed` is a best-effort client-side read for preview only. */
data class FreeZoneScan(val raw: String, val parsed: ParsedKbn?, val count: Int)

/**
 * Free Zone — open scan, not tied to any assignment or address list. Every
 * physical box has its own Kanban tag with a QR code; scanning it is the
 * count.
 *
 * Backed by FreeZoneQueue (local-first, persisted to disk) rather than
 * screen-local state — leaving this screen, or the app being killed
 * outright, never loses a box that was already scanned; everything stays
 * queued until the server actually confirms it, and a partially-failed
 * Send (e.g. a QR format the backend can't decode yet) keeps exactly the
 * failed boxes in the list instead of silently discarding them.
 *
 * A real Zebra scanner in keyboard-wedge (DataWedge) mode types straight
 * into whatever text field has focus and sends Enter — same pattern as
 * AddressDetailScreen's own scan field, built to work with that as-is.
 */
@Composable
fun FreeZoneScreen(
    zoneCodes: List<String>,
    deviceId: String,
    batchId: String?,
    employeeName: String,
    onBack: () -> Unit
) {
    var scanInput by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var resultIsError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Catch-up: if anything was left queued from before this screen was
    // last open (e.g. the operator left before a background sync caught
    // up, or the app was killed), try sending it the moment the screen
    // opens rather than waiting for the next scan or a manual Send tap.
    LaunchedEffect(batchId) {
        batchId?.let { FreeZoneQueue.triggerSync(deviceId, it, employeeName) }
    }

    fun group(raws: List<String>): List<FreeZoneScan> {
        val counts = LinkedHashMap<String, Int>()
        raws.forEach { counts[it] = (counts[it] ?: 0) + 1 }
        return counts.map { (raw, count) -> FreeZoneScan(raw, KbnQr.parse(raw), count) }
    }

    // Grouped by raw text — the same Kanban tag scanned N times (the
    // workaround for boxes stacked too deep to reach each one's own tag,
    // see FreeZoneQueue.addScan) shows as ONE row with count=N rather than
    // N identical rows. Order follows first-occurrence in the underlying
    // (newest-first) list, so the newest group still lands on top.
    val scans = remember(FreeZoneQueue.scans.toList()) { group(FreeZoneQueue.scans) }
    // Confirmed-sent, kept visible below the pending list — see
    // FreeZoneQueue.sentThisSession's own doc comment on why this lives
    // there (not screen-local state): a background auto-sync can confirm a
    // box just as easily as the Send button, and both need to land here.
    val sentGroups = remember(FreeZoneQueue.sentThisSession.toList()) { group(FreeZoneQueue.sentThisSession) }

    fun addScan(raw: String) {
        val code = raw.trim()
        if (code.isEmpty()) return
        scanInput = ""
        resultMessage = null
        FreeZoneQueue.addScan(code)
        // Try to sync right away in the background — most boxes end up
        // confirmed (moved to the "ส่งแล้ว ✓" section below) before the
        // operator even looks up from the scanner. The Send button below
        // still exists for a manual "try right now" nudge, e.g. right
        // after WiFi comes back instead of waiting for the next scan.
        batchId?.let { FreeZoneQueue.triggerSync(deviceId, it, employeeName) }
    }

    fun send() {
        val batch = batchId
        if (batch == null) {
            resultMessage = "ไม่พบ batch ที่กำลังทำงานอยู่ — ลองกลับไป Home แล้วเข้าใหม่"
            resultIsError = true
            return
        }
        if (sending || FreeZoneQueue.scans.isEmpty()) return
        sending = true
        scope.launch {
            val result = FreeZoneQueue.sendAll(deviceId, batch, employeeName)
            sending = false

            // `result.success` tells apart two very different failures that
            // used to look identical to the operator: `false` means the
            // request never reached the server at all (real connectivity
            // problem — bad WiFi, wrong server IP, server not running).
            // `true` with failedCount > 0 means the server WAS reached and
            // responded, it just rejected some/all of these specific boxes
            // (e.g. a QR format it can't decode) — never a WiFi issue, and
            // telling the operator to check WiFi for this case just sends
            // them down the wrong troubleshooting path.
            //
            // No auto-navigate-away on success anymore, and no silent
            // "message becomes null" either — Free Zone is an ongoing task
            // at a zone, not something with a finish line like Fix Zone, so
            // a successful Send should let the operator keep scanning right
            // here, with a clear confirmation that the list emptying out
            // means "sent", not "lost".
            resultIsError = !(result.failedCount == 0 && result.success)
            resultMessage = when {
                result.failedCount == 0 && result.success -> "ส่งสำเร็จ ${result.savedCount} กล่อง"
                !result.success -> "เชื่อมต่อเซิร์ฟเวอร์ไม่ได้ — เช็ค WiFi หรือว่าเซิร์ฟเวอร์เปิดอยู่ไหม (ยังอยู่ในรายการ ลองใหม่ได้)"
                result.savedCount == 0 -> "เซิร์ฟเวอร์ปฏิเสธทั้ง ${result.failedCount} กล่อง (ไม่ใช่ปัญหา WiFi — QR อาจเป็น format ที่ระบบยังอ่านไม่ได้)"
                else -> "ส่งสำเร็จ ${result.savedCount} · เซิร์ฟเวอร์ปฏิเสธ ${result.failedCount} กล่อง (ยังอยู่ในรายการ กดส่งใหม่ได้)"
            }
        }
    }

    val distinctParts = remember(scans) { scans.mapNotNull { it.parsed?.partNumber }.distinct().size }
    val totalBoxes = FreeZoneQueue.scans.size // total physical scans, not group count — a Kanban scanned 3x is 3 boxes
    val unpreviewedCount = FreeZoneQueue.scans.count { KbnQr.parse(it) == null }

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

        // Running totals. distinctParts only counts scans that parsed
        // client-side — an unparsed scan is still queued and will still be
        // sent and counted for real once the backend decodes it, it just
        // can't contribute to this preview number. No Qty total shown here
        // on purpose — summed piece count across many boxes can run into
        // the thousands and isn't meaningful at a glance; Box count is
        // what the operator actually cares about seeing at a scan.
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
                    .background(Lemon, RoundedCornerShape(11.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(text = "$totalBoxes", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "กล่อง", color = Ink.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Medium)
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
        if (resultMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = resultMessage.orEmpty(),
                color = if (resultIsError) ErrorText else SuccessText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (scans.isEmpty() && sentGroups.isEmpty()) {
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
                            } else {
                                Text(text = "สแกนแล้ว (ดูตัวอย่างไม่ได้)", color = Ink, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "จะให้ระบบตรวจสอบตอนกด Send", color = Muted, fontSize = 8.5.sp)
                            }
                        }
                        Text(
                            text = "×${scan.count}",
                            color = Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(LemonSoft, RoundedCornerShape(7.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 22.dp, height = 20.dp)
                                .background(Color_EDEFE8, RoundedCornerShape(6.dp))
                                .clickable { FreeZoneQueue.removeScan(scan.raw) },
                            contentAlignment = Alignment.Center
                        ) { Text(text = "×", color = Muted, fontSize = 12.sp) }
                    }
                }

                // Confirmed-sent, kept visible below the pending list (see
                // sentThisSession's own doc comment) — same normal card
                // styling as the pending list (not dimmed/grayed out), just
                // a ✓ badge instead of the × remove button, since there's
                // nothing left to correct here — this box's real record now
                // lives on the server.
                if (sentGroups.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ส่งแล้ว (${FreeZoneQueue.sentThisSession.size} กล่อง)",
                            color = Muted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    items(sentGroups) { scan ->
                        val p = scan.parsed
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardWhite, RoundedCornerShape(11.dp))
                                .border(1.dp, BorderLight, RoundedCornerShape(11.dp))
                                .padding(horizontal = 11.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (p != null) {
                                    Text(text = p.partNumber, color = Ink, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text(text = "ส่งแล้ว (ดูตัวอย่างไม่ได้)", color = Ink, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Text(
                                text = "✓ ×${scan.count}",
                                color = SuccessText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .background(SuccessText.copy(alpha = 0.12f), RoundedCornerShape(7.dp))
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Back + Send share one row now — Send stopped being the one big
        // committing gesture it used to be (everything's already saved
        // locally the moment it's scanned, see FreeZoneQueue), so it no
        // longer needs to dominate the screen.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = onBack)
            Box(
                modifier = Modifier
                    .background(
                        if (scans.isEmpty() || sending) Ink.copy(alpha = 0.4f) else Ink,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(enabled = scans.isNotEmpty() && !sending) { send() }
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            ) {
                Text(
                    text = if (sending) "..." else "Send",
                    color = Lemon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Light gray-green used for the remove-scan button background (matches Box/Pcs/Seq fields).
private val Color_EDEFE8 = androidx.compose.ui.graphics.Color(0xFFEDEFE8)