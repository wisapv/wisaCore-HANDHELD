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
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.Muted

enum class AddressStatus { DONE, CURRENT, PENDING }
data class AddressRow(val code: String, val status: AddressStatus, val remain: Int)

/** Screen 6 — one job (ZoneJob) can still contain several physical addresses to visit one by one. */
@Composable
fun SelectAddressScreen(
    zoneCode: String,
    employeeName: String,
    addresses: List<AddressRow>,
    onSelectAddress: (AddressRow) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(20.dp)
    ) {
        Text(text = "$zoneCode · $employeeName", color = Muted, fontSize = 9.sp)
        Text(text = "Select Address", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(14.dp))

        if (addresses.isEmpty()) {
            Text(text = "ไม่มี address ในโซนนี้", color = Muted, fontSize = 11.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            addresses.forEach { addr ->
                val bg = when (addr.status) {
                    AddressStatus.DONE -> Ink
                    AddressStatus.CURRENT -> Lemon
                    AddressStatus.PENDING -> CardWhite
                }
                val border = when (addr.status) {
                    AddressStatus.DONE -> Ink
                    AddressStatus.CURRENT -> Lemon
                    AddressStatus.PENDING -> BorderLight
                }
                val textColor = if (addr.status == AddressStatus.DONE) CardWhite else Ink

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg, RoundedCornerShape(13.dp))
                        .border(1.dp, border, RoundedCornerShape(13.dp))
                        .clickable { onSelectAddress(addr) }
                        .padding(horizontal = 15.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = addr.code, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    when (addr.status) {
                        AddressStatus.DONE -> Text(text = "✓ Done", color = Lemon, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
                        AddressStatus.CURRENT -> Text(text = "${addr.remain} remain", color = Ink.copy(alpha = 0.65f), fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
                        AddressStatus.PENDING -> Text(text = "${addr.remain} remain", color = Muted, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        BackButton(onClick = onBack)
    }
}