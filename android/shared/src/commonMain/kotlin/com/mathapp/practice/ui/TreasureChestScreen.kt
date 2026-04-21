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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TreasureChestScreen(
    appLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val totalPoints = remember { getTotalPoints() }
    val level = remember { getTreasureLevel() }
    val nextLevelPoints = remember { getNextLevelPoints() }
    val levelProgress = if (nextLevelPoints > 0) (totalPoints.toFloat() / nextLevelPoints).coerceIn(0f, 1f) else 1f
    val treasures = ALL_TREASURES

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(AppColors.GradientGold))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                text = "🎁 ${L10n.string("treasure_title", appLanguage)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Level progress card ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${L10n.string("treasure_level", appLanguage)} $level",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.WarnGold
                            )
                            Text(
                                text = "${L10n.string("treasure_level_progress", appLanguage)}: $totalPoints / $nextLevelPoints P",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Points display
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(AppColors.GradientGold),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$totalPoints P",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { levelProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = AppColors.WarnGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Collection section ──
        Text(
            text = L10n.string("treasure_collection", appLanguage),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3x3 treasure grid
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                treasures.chunked(3).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { item ->
                            TreasureItemBox(
                                item = item,
                                isUnlocked = isTreasureUnlocked(item),
                                appLanguage = appLanguage,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TreasureItemBox(
    item: TreasureItem,
    isUnlocked: Boolean,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val bgBrush = if (isUnlocked)
        Brush.linearGradient(AppColors.GradientGold)
    else
        Brush.linearGradient(listOf(Color(0xFFEEEEEE), Color(0xFFDDDDDD)))

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(bgBrush, shape = RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isUnlocked) item.emoji else "🔒",
                fontSize = 36.sp,
                color = if (isUnlocked) Color.Unspecified else Color.White.copy(alpha = 0.6f)
            )
            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.requiredPoints}P",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
