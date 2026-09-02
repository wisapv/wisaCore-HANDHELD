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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.LemonBadgeText
import com.example.wisahandheld.ui.theme.LemonSoft
import com.example.wisahandheld.ui.theme.Muted

data class KbnRow(val supplier: String, val kbn: String, val address: String)

/** Screen 7 — "Address" (originally your Screen 4): remain counter + the KBN rows still left to scan. */
@Composable
fun AddressDetailScreen(
    zoneCode: String,
    addressCode: String,
    remain: Int,
    rows: List<KbnRow>,
    onSelectRow: (KbnRow) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(20.dp)
    ) {
        Text(text = "$zoneCode · $addressCode", color = Muted, fontSize = 9.sp)
        Text(text = "Address", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
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
            Text(text = "$remain", color = Lemon, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(11.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(11.dp))
                        .clickable { onSelectRow(row) }
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack)
                    .padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.Center
            ) { Text(text = "Back", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium) }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Ink, RoundedCornerShape(12.dp))
                    .clickable(onClick = onEdit)
                    .padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.Center
            ) { Text(text = "Edit", color = Lemon, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
        }
    }
}
