package com.example.wisahandheld.ui.screens

import androidx.compose.foundation.background
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
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.Canvas
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon
import com.example.wisahandheld.ui.theme.Muted

/**
 * Shown only when a device has work in BOTH the Part Runout active batch
 * AND at least one Getsudo batch at the same time (MainActivity skips this
 * screen entirely otherwise — see loadWorkModes). Picking one just fixes
 * which batchId the rest of the session (Home, Part list, Zone Detail,
 * Input Stock) uses — nothing here is fetched or shown from the other
 * batch until the operator checks in again and picks differently.
 */
@Composable
fun WorkModeScreen(
    onPickTbos: () -> Unit,
    onPickGetsudo: (batchId: String) -> Unit,
    getsudoBatchIds: List<String>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .padding(20.dp)
    ) {
        Text(text = "วันนี้จะนับอะไร?", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Text(text = "เครื่องนี้มีงานมากกว่า 1 อย่าง เลือกก่อนเริ่ม", color = Muted, fontSize = 10.5.sp)
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink, RoundedCornerShape(14.dp))
                .clickable(onClick = onPickTbos)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column {
                Text(text = "Part Runout", color = Lemon, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(text = "นับตามแผนควบคุม part ที่กำลังเลิกใช้งาน", color = CardWhite.copy(alpha = 0.6f), fontSize = 10.sp)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        getsudoBatchIds.forEachIndexed { i, batchId ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardWhite, RoundedCornerShape(14.dp))
                    .clickable { onPickGetsudo(batchId) }
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Column {
                    Text(
                        text = if (getsudoBatchIds.size > 1) "Getsudo (${i + 1})" else "Getsudo",
                        color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                    Text(text = "นับแบบยืดหยุ่นตามความจำเป็น", color = Muted, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.weight(1f))
        BackButton(onClick = onBack)
    }
}
