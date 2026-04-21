package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ── Gradient header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(AppColors.GradientTealBlue))
                .padding(top = 44.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                L10n.string("select_operation", appLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // ── 2×2 grid ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ops.chunked(2).forEachIndexed { rowIdx, rowOps ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
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
                    if (rowOps.size < 2) Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
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
    val stagesDone = (progress * 20).toInt()
    val cardGradient = when (operation) {
        MathOperation.ADDITION       -> Brush.linearGradient(listOf(Color(0xFFFF6B9D), Color(0xFFFF8E53)))
        MathOperation.SUBTRACTION    -> Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
        MathOperation.MULTIPLICATION -> Brush.linearGradient(listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)))
        MathOperation.DIVISION       -> Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7)))
    }

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = operation.symbol,
                    fontSize = 52.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = opName(operation, appLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$stagesDone / 20",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
