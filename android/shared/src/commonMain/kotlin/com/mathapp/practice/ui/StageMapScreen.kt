package com.mathapp.practice.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StageMapScreen(
    operation: MathOperation,
    appLanguage: AppLanguage,
    onStageSelected: (Int) -> Unit,
    onBack: () -> Unit,
    progressVersion: Int
) {
    val stages = remember(progressVersion) { stagesFor(operation) }
    // "Current" stage = first unlocked stage with 0 stars, else last stage
    val currentIdx = stages.indexOfFirst { it.isUnlocked && it.stars == 0 }
        .let { if (it == -1) stages.size - 1 else it }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(
                "${opName(operation, appLanguage)} — ${L10n.string("stage_map_title", appLanguage)}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // ── Stage nodes ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            stages.forEachIndexed { idx, stage ->
                val isCurrent = idx == currentIdx
                val isLocked = !stage.isUnlocked

                if (idx > 0) {
                    ConnectorLine(locked = isLocked)
                }

                StageNode(
                    stage = stage,
                    stageNumber = idx + 1,
                    isCurrent = isCurrent,
                    appLanguage = appLanguage,
                    onClick = if (isLocked) null else ({ onStageSelected(stage.number) })
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ConnectorLine(locked: Boolean) {
    val color = if (locked)
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    else
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)

    Canvas(
        modifier = Modifier
            .width(4.dp)
            .height(36.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(size.width / 2f, 0f),
            end = Offset(size.width / 2f, size.height),
            strokeWidth = size.width,
            cap = StrokeCap.Round,
            pathEffect = if (locked) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null
        )
    }
}

@Composable
private fun StageNode(
    stage: StageInfo,
    stageNumber: Int,
    isCurrent: Boolean,
    appLanguage: AppLanguage,
    onClick: (() -> Unit)?
) {
    val isLocked = !stage.isUnlocked
    val nodeSize = if (isCurrent) 80.dp else 64.dp
    val bgColor = when {
        isLocked  -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        stage.stars == 3 -> MaterialTheme.colorScheme.primary
        stage.stars > 0  -> MaterialTheme.colorScheme.secondary
        isCurrent        -> MaterialTheme.colorScheme.primaryContainer
        else             -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isLocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        stage.stars == 3 -> MaterialTheme.colorScheme.onPrimary
        stage.stars > 0  -> MaterialTheme.colorScheme.onSecondary
        isCurrent        -> MaterialTheme.colorScheme.onPrimaryContainer
        else             -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Node circle
        Surface(
            modifier = Modifier.size(nodeSize),
            shape = CircleShape,
            color = bgColor,
            onClick = { onClick?.invoke() },
            enabled = onClick != null,
            tonalElevation = if (isCurrent) 6.dp else 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isLocked) {
                    Text("🔒", fontSize = if (isCurrent) 28.sp else 22.sp)
                } else if (stage.stars > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            starsString(stage.stars),
                            fontSize = if (isCurrent) 20.sp else 16.sp
                        )
                    }
                } else {
                    Text(
                        stageNumber.toString(),
                        fontSize = if (isCurrent) 32.sp else 24.sp,
                        color = contentColor,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        // Stage info
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = L10n.string("stage_num_format", appLanguage, stageNumber),
                    style = if (isCurrent) MaterialTheme.typography.titleMedium
                            else MaterialTheme.typography.bodyMedium,
                    color = if (isLocked)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (isCurrent && !isLocked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "▶",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Text(
                text = stage.desc(appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = if (isLocked)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun starsString(stars: Int): String = when (stars) {
    1 -> "⭐"
    2 -> "⭐⭐"
    3 -> "⭐⭐⭐"
    else -> ""
}
