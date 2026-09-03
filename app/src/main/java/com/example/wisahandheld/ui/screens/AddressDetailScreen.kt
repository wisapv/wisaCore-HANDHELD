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
import com.example.wisahandheld.ui.components.BackButton
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

/**
 * One part somewhere in the zone. `counted` + the `counted*` fields let a
 * re-scan of an already-submitted part open Input Stock pre-filled for
 * correction instead of a blank form (see InputStockScreen's edit mode).
 */
data class KbnRow(
    val supplier: String,
    val kbn: String,
    val address: String,
    val partName: String = "",
    val partNo: String = "",
    val shop: String = "",
    val dock: String = "",
    val sPlant: String = "",
    val sDock: String = "",
    val qty: String = "",
    val counted: Boolean = false,
    val countedQty: Int? = null,
    val countedBox: String? = null,
    val countedPcs: String? = null,
    val countedSeq: String? = null
)

/**
 * Screen — one flat list for the whole zone (replaces the old Select
 * Address → per-address list two-step). A scan or typed KBN/Part no. at
 * the top matches against every part in the zone (counted or not) and
 * opens Input Stock — fresh for a new count, or pre-filled for a re-scan
 * of something already counted (so the operator can correct it). Tapping
 * a row in the list does the same as scanning that row's code — it's the
 * fallback for when scanning isn't available.
 *
 * A real Zebra scanner in keyboard-wedge (DataWedge) mode types straight
 * into whatever text field has focus and sends Enter — this field is built
 * to work with that as-is, no extra wiring needed once the device side is
 * configured.
 */
@Composable
fun AddressDetailScreen(
    zoneCode: String,
    parts: List<KbnRow>,
    onMatch: (KbnRow) -> Unit,
    onBack: () -> Unit
) {
    var scanInput by remember { mutableStateOf("") }
    var noMatch by remember { mutableStateOf(false) }

    val remaining = remember(parts) { parts.filter { !it.counted } }

    fun tryMatch(raw: String) {
        val code = raw.trim()
        if (code.isEmpty()) return
        val match = parts.firstOrNull { it.kbn == code || it.partNo == code }
        if (match != null) {
            noMatch = false
            scanInput = ""
            onMatch(match)
        } else {
            noMatch = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(20.dp)
    ) {
        Text(text = zoneCode, color = Muted, fontSize = 9.sp)
        Text(text = "Zone Detail", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink, RoundedCornerShape(13.dp))
                .padding(horizontal = 15.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "REMAIN", color = CardWhite.copy(alpha = 0.55f), fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text(text = "${remaining.size}", color = Lemon, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Scan / type KBN or Part no. — Enter (or a scanner's injected
        // Enter) submits, same as tapping a row below.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite, RoundedCornerShape(12.dp))
                .border(1.dp, if (noMatch) ErrorText.copy(alpha = 0.4f) else BorderLight, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScanFrameIcon(tint = Muted, sizeDp = 16.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (scanInput.isEmpty()) {
                    Text(text = "สแกน หรือพิมพ์ KBN / Part no.", color = Muted, fontSize = 12.sp)
                }
                BasicTextField(
                    value = scanInput,
                    onValueChange = { scanInput = it; noMatch = false },
                    singleLine = true,
                    textStyle = TextStyle(color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    cursorBrush = SolidColor(Ink),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { tryMatch(scanInput) }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (noMatch) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "ไม่พบ part นี้ใน zone นี้ — ลองสแกนใหม่ หรือแตะเลือกจากลิสต์ด้านล่าง", color = ErrorText, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (remaining.isEmpty()) {
            Text(text = "นับครบทุกรายการในโซนนี้แล้ว", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(remaining) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(11.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(11.dp))
                        .clickable { onMatch(row) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = row.supplier, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(text = row.address, color = Muted, fontSize = 9.sp)
                    }
                    Text(
                        text = row.kbn,
                        color = LemonBadgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .background(LemonSoft, RoundedCornerShape(7.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        BackButton(onClick = onBack)
    }
}