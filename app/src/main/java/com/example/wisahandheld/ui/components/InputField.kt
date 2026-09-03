package com.example.wisahandheld.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisahandheld.ui.theme.BorderLight
import com.example.wisahandheld.ui.theme.CardWhite
import com.example.wisahandheld.ui.theme.Ink
import com.example.wisahandheld.ui.theme.Muted

/**
 * A single-line boxed input, styled to match the web app's inputs (white
 * box, thin ink border, rounded corners) instead of Material's default
 * underline/outline TextField look.
 */
@Composable
fun BoxedInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    textAlign: androidx.compose.ui.text.style.TextAlign = androidx.compose.ui.text.style.TextAlign.Start,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(12.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, color = Muted, fontSize = 14.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = textAlign),
            cursorBrush = SolidColor(Ink),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
