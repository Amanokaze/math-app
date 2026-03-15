package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberKeypad(
    modifier: Modifier = Modifier,
    confirmTitle: String,
    showMinus: Boolean = true,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    onMinus: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(if (showMinus) "-" else "", "0", "⌫")
        ).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    val isSpecial = key in listOf("-", "⌫")
                    KeypadButton(
                        text = key,
                        isSpecial = isSpecial,
                        enabled = key.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        when (key) {
                            "-" -> onMinus()
                            "⌫" -> onBackspace()
                            "" -> {}
                            else -> onDigit(key)
                        }
                    }
                }
            }
        }
        KeypadButton(
            text = confirmTitle,
            isAccent = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            onSubmit()
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isAccent: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(50.dp),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = when {
            !enabled  -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            isAccent  -> MaterialTheme.colorScheme.primary
            isSpecial -> MaterialTheme.colorScheme.surfaceVariant
            else      -> MaterialTheme.colorScheme.surface
        }
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = if (isSpecial) 20.sp else 28.sp,
                color = if (isAccent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
