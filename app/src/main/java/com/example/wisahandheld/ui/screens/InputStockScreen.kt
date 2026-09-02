package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.ErrorText
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.ManualAccent
import com.example.wisahandheld.ui.theme.Muted

/** What the operator scanned — auto-filled, never typed. */
data class ScanResult(val kbn: String, val address: String, val partName: String, val qty: Int)

/**
 * Screen 8 — Input Stock. KBN / Address / Part Name / Q'TY come straight
 * from the barcode scan (green dot, read-only in a real build — the scan
 * result is passed in via `scan`, not editable here). Box / Pcs / Seq have
 * no barcode source and must always be typed by the operator (gray-green
 * dot). "Not Found" sets qty to 0 but is recorded as a distinct status —
 * NOT the same thing as a confirmed zero count.
 */
@Composable
fun InputStockScreen(
    zoneCode: String,
    addressCode: String,
    scan: ScanResult,
    onNotFound: () -> Unit,
    onBack: () -> Unit,
    onSend: (box: String, pcs: String, seq: String) -> Unit
) {
    var box by remember { mutableStateOf("") }
    var pcs by remember { mutableStateOf("") }
    var seq by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(20.dp)
    ) {
        Text(text = "$zoneCode · $addressCode", color = Muted, fontSize = 9.sp)
        Text(text = "Input Stock", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScannedField(label = "KBN", value = scan.kbn, modifier = Modifier.weight(1f))
            ScannedField(label = "ADDRESS", value = scan.address, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        ScannedField(label = "PART NAME", value = scan.partName)
        Spacer(modifier = Modifier.height(8.dp))
        ScannedField(label = "Q'TY", value = "${scan.qty}")
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            EditableField(label = "BOX", value = box, onValueChange = { box = it }, modifier = Modifier.weight(1f))
            EditableField(label = "PCS", value = pcs, onValueChange = { pcs = it }, modifier = Modifier.weight(1f))
            EditableField(label = "SEQ", value = seq, onValueChange = { seq = it }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, ErrorText.copy(alpha = 0.25f), RoundedCornerShape(11.dp))
                    .clickable(onClick = onNotFound)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) { Text(text = "Not Found", color = ErrorText, fontSize = 10.5.sp, fontWeight = FontWeight.Medium) }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, BorderLight, RoundedCornerShape(11.dp))
                    .clickable(onClick = onBack)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) { Text(text = "Back", color = Ink, fontSize = 10.5.sp, fontWeight = FontWeight.Medium) }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Ink, RoundedCornerShape(11.dp))
                    .clickable { onSend(box, pcs, seq) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) { Text(text = "Send", color = Lemon, fontSize = 10.5.sp, fontWeight = FontWeight.Medium) }
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

/** Editable field the operator must fill in themselves — never auto-filled. */
@Composable
private fun EditableField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        FieldLabel(text = label, dotColor = ManualAccent)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite, RoundedCornerShape(11.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(11.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                cursorBrush = SolidColor(Ink),
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
