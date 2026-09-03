package com.example.wisahandheld.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Lemon

/** Solid ink background, lemon text — matches the web app's `bg-ink text-accent` buttons. */
@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Ink, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Lemon, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/** Outline button — used for "Back" and other low-emphasis actions. */
@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * Small "‹ Back" pill — ink background, lemon text — used at the top of
 * every screen except Overview to go to the previous screen in the flow.
 * Deliberately compact (not full-width) so it doesn't compete with the
 * screen's main content/actions.
 */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Ink, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text = "‹ Back", color = Lemon, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}