package com.mathapp.practice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryScreen(
    appLanguage: AppLanguage,
    onOperationSelected: (MathOperation) -> Unit,
    onBack: () -> Unit,
    progressVersion: Int
) {
    val ops = MathOperation.entries
    val progressList = remember(progressVersion) { ops.map { operationProgress(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                L10n.string("select_operation", appLanguage),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2x2 grid of operation cards
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ops.chunked(2).forEachIndexed { rowIdx, rowOps ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowOps.forEachIndexed { colIdx, op ->
                        val globalIdx = rowIdx * 2 + colIdx
                        OperationCard(
                            operation = op,
                            progress = progressList[globalIdx],
                            appLanguage = appLanguage,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onClick = { onOperationSelected(op) }
                        )
                    }
                    // Fill gap if odd number of ops in last row
                    if (rowOps.size < 2) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OperationCard(
    operation: MathOperation,
    progress: Float,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val stagesDone = (progress * 10).toInt()

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Operation symbol (big)
            Text(
                text = operation.symbol,
                fontSize = 56.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Operation name
            Text(
                text = opName(operation, appLanguage),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                trackColor = MaterialTheme.colorScheme.background
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                    text = "$stagesDone / 10",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
