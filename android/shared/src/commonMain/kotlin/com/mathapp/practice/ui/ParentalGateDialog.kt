package com.mathapp.practice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private data class GateProblem(val a: Int, val b: Int, val opSymbol: String, val answer: Int)

private fun generateProblem(): GateProblem {
    return when (Random.nextInt(2)) {
        0 -> {
            val a = Random.nextInt(15, 51)
            val b = Random.nextInt(15, 51)
            GateProblem(a, b, "+", a + b)
        }
        else -> {
            val a = Random.nextInt(4, 10)
            val b = Random.nextInt(4, 10)
            GateProblem(a, b, "×", a * b)
        }
    }
}

@Composable
fun ParentalGateDialog(
    appLanguage: AppLanguage,
    onPassed: () -> Unit,
    onDismiss: () -> Unit
) {
    var problem by remember { mutableStateOf(generateProblem()) }
    var input by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = L10n.string("parent_gate_title", appLanguage),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = L10n.string("parent_gate_desc", appLanguage),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${problem.a} ${problem.opSymbol} ${problem.b} = ?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { v ->
                        if (v.length <= 4 && v.all { it.isDigit() }) {
                            input = v
                            showError = false
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    isError = showError,
                    modifier = Modifier.width(120.dp)
                )
                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = L10n.string("parent_gate_wrong", appLanguage),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val ans = input.toIntOrNull()
                if (ans != null && ans == problem.answer) {
                    onPassed()
                } else {
                    problem = generateProblem()
                    input = ""
                    showError = true
                }
            }) {
                Text(L10n.string("parent_gate_confirm", appLanguage))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(L10n.string("cancel", appLanguage))
            }
        }
    )
}
