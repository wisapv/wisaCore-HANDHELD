package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.data.KbnQr
import com.example.wisahandheld.ui.components.BackButton
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.ErrorText
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.ManualAccent
import com.example.wisahandheld.ui.theme.Muted

/** What the operator scanned — auto-filled, never typed. qtyPerBox comes from the Kanban QR (0 if opened via a plain list tap, no real scan yet). */
data class ScanResult(val kbn: String, val address: String, val partName: String, val qtyPerBox: Int)

/**
 * Screen 8 — Input Stock. KBN / Address / Part Name / Qty-per-Box come
 * straight from the first scan that got us here (green dot, read-only).
 *
 * Real workflow this matches: the shelf normally has one already-opened
 * box (being consumed) plus some number of still-sealed full boxes. The
 * operator scans each full box's Kanban tag right on THIS screen — every
 * matching scan bumps Box by 1 (same tag can be re-scanned if a second
 * box isn't physically reachable, that's fine, it just means "one more
 * full box confirmed"). Pcs is always typed by hand — it's what's left in
 * the opened box, which by definition isn't full, so no QR can give it.
 * Seq is also always typed — the operator reads it off something at the
 * work site, unrelated to scanning.
 *
 * Total submitted = Box × Qty-per-Box (from the QR) + Pcs.
 *
 * `isEdit` = re-scanning something already counted before — Box/Pcs/Seq
 * start from what was submitted last time instead of from scratch, so the
 * operator is correcting, not starting over.
 */
@Composable
fun InputStockScreen(
    zoneCode: String,
    addressCode: String,
    scan: ScanResult,
    isEdit: Boolean = false,
    initialBox: Int = 1,
    initialPcs: String = "",
    initialSeq: String = "",
    onNotFound: () -> Unit,
    onBack: () -> Unit,
    onSend: (qty: Int, box: Int, pcs: String, seq: String) -> Unit
) {
    var box by remember(scan) { mutableStateOf(initialBox) }
    var boxInputText by remember(scan) { mutableStateOf("$initialBox") }
    var pcs by remember(scan) { mutableStateOf(initialPcs) }
    var seq by remember(scan) { mutableStateOf(initialSeq) }
    var mismatch by remember(scan) { mutableStateOf(false) }

    val computedQty = box * scan.qtyPerBox + (pcs.toIntOrNull() ?: 0)

    // Tap the Box field and either scan another full box's Kanban tag
    // (bumps box +1) or just type a number directly to set the count —
    // whichever's on the tag/keyboard when Done/Enter fires.
    fun commitBoxInput() {
        val raw = boxInputText.trim()
        if (raw.isEmpty()) { boxInputText = "$box"; return }

        val parsedQr = KbnQr.parse(raw)
        when {
            parsedQr != null && parsedQr.kbnCode == scan.kbn -> { box += 1; mismatch = false }
            parsedQr != null -> mismatch = true // valid QR, but a different part
            raw.toIntOrNull() != null -> { box = raw.toInt(); mismatch = false }
            else -> mismatch = true
        }
        boxInputText = "$box"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(16.dp)
    ) {
        Text(text = "$zoneCode · $addressCode", color = Muted, fontSize = 9.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Input Stock", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (isEdit) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "แก้ไขรายการที่นับไปแล้ว",
                    color = Ink,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(ManualAccent.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScannedField(label = "KBN", value = scan.kbn, modifier = Modifier.weight(1f))
            ScannedField(label = "ADDRESS", value = scan.address, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        ScannedField(label = "PART NAME", value = scan.partName)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            // Box — tap the number to scan another full box's tag (Box +1)
            // or type a count directly; − / + are for a quick nudge without
            // opening the keyboard at all.
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel(text = "BOX", dotColor = Lemon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(11.dp))
                        .border(1.dp, if (mismatch) ErrorText.copy(alpha = 0.4f) else BorderLight, RoundedCornerShape(11.dp))
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(CardWhite, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                            .clickable { if (box > 0) box -= 1; boxInputText = "$box" },
                        contentAlignment = Alignment.Center
                    ) { Text(text = "−", color = Ink, fontSize = 12.sp) }

                    BasicTextField(
                        value = boxInputText,
                        onValueChange = { boxInputText = it; mismatch = false },
                        singleLine = true,
                        textStyle = TextStyle(color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                        cursorBrush = SolidColor(Ink),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { commitBoxInput() }),
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Ink, RoundedCornerShape(6.dp))
                            .clickable { box += 1; boxInputText = "$box" },
                        contentAlignment = Alignment.Center
                    ) { Text(text = "+", color = Lemon, fontSize = 12.sp) }
                }
                if (mismatch) {
                    Text(text = "ไม่ตรง KBN นี้", color = ErrorText, fontSize = 8.5.sp, modifier = Modifier.padding(top = 3.dp))
                }
            }
            EditableField(
                label = "PCS", value = pcs,
                onValueChange = { pcs = it.filter { c -> c.isDigit() } },
                modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number
            )
            EditableField(
                label = "SEQ", value = seq,
                onValueChange = { seq = it.filter { c -> c.isDigit() }.take(3) },
                modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = onBack)

            Row {
                Box(
                    modifier = Modifier
                        .background(ErrorText, RoundedCornerShape(10.dp))
                        .clickable(onClick = onNotFound)
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) { Text(text = "Not Found", color = CardWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Ink, RoundedCornerShape(10.dp))
                        .clickable { onSend(computedQty, box, pcs, seq) }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) { Text(text = "Send", color = Lemon, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
            }
        }
    }
}

/** Read-only field auto-filled by the barcode scan. */
@Composable
private fun ScannedField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        FieldLabel(text = label, dotColor = Lemon)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite, RoundedCornerShape(11.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(11.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text(text = value, color = Ink, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** Editable field the operator must fill in themselves — never auto-filled. Digits only. */
@Composable
private fun EditableField(
    label: String, value: String, onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier, keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        FieldLabel(text = label, dotColor = ManualAccent)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite, RoundedCornerShape(11.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(11.dp))
                .padding(horizontal = 12.dp, vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                cursorBrush = SolidColor(Ink),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String, dotColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
        Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Medium)
    }
}