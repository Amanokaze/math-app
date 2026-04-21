package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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

    var selectedTreasure by remember { mutableStateOf<TreasureItem?>(null) }
    var featuredId by remember { mutableStateOf(getFeaturedTreasureId()) }

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

                LinearProgressIndicator(
                    progress = { levelProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = AppColors.WarnGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Featured treasure section ──
        FeaturedTreasureSection(
            featuredId = featuredId,
            appLanguage = appLanguage,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

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
                                isFeatured = item.id == featuredId,
                                appLanguage = appLanguage,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedTreasure = item }
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

    // ── Detail dialog ──
    selectedTreasure?.let { item ->
        TreasureDetailDialog(
            item = item,
            isUnlocked = isTreasureUnlocked(item),
            isFeatured = item.id == featuredId,
            totalPoints = totalPoints,
            appLanguage = appLanguage,
            onSetFeatured = {
                setFeaturedTreasureId(item.id)
                featuredId = item.id
                selectedTreasure = null
            },
            onDismiss = { selectedTreasure = null }
        )
    }
}

@Composable
private fun FeaturedTreasureSection(
    featuredId: Int,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val featured = ALL_TREASURES.find { it.id == featuredId && isTreasureUnlocked(it) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (featured != null)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        if (featured != null) Brush.linearGradient(AppColors.GradientGold)
                        else Brush.linearGradient(listOf(Color(0xFFEEEEEE), Color(0xFFDDDDDD))),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = featured?.emoji ?: "🎁",
                    fontSize = 28.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = L10n.string("treasure_featured_section", appLanguage),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (featured != null)
                        L10n.string(featured.nameKey, appLanguage)
                    else
                        "—",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (featured != null)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (featured != null) {
                    Text(
                        text = L10n.string(featured.praiseKey, appLanguage),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TreasureItemBox(
    item: TreasureItem,
    isUnlocked: Boolean,
    isFeatured: Boolean,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgBrush = if (isUnlocked)
        Brush.linearGradient(AppColors.GradientGold)
    else
        Brush.linearGradient(listOf(Color(0xFFEEEEEE), Color(0xFFDDDDDD)))

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(bgBrush, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isUnlocked) item.emoji else "🔒",
                fontSize = 32.sp,
                color = if (isUnlocked) Color.Unspecified else Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isUnlocked)
                    L10n.string(item.nameKey, appLanguage)
                else
                    "${item.requiredPoints}P",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isUnlocked) Color.White.copy(alpha = 0.9f) else Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        if (isFeatured) {
            Text(
                text = "✨",
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun TreasureDetailDialog(
    item: TreasureItem,
    isUnlocked: Boolean,
    isFeatured: Boolean,
    totalPoints: Int,
    appLanguage: AppLanguage,
    onSetFeatured: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emoji circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (isUnlocked) Brush.linearGradient(AppColors.GradientGold)
                            else Brush.linearGradient(listOf(Color(0xFFEEEEEE), Color(0xFFDDDDDD))),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isUnlocked) item.emoji else "🔒",
                        fontSize = 40.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = if (isUnlocked)
                        L10n.string(item.nameKey, appLanguage)
                    else
                        L10n.string("treasure_locked", appLanguage),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) AppColors.WarnGold else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            if (isUnlocked) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isUnlocked)
                            L10n.string("treasure_achieved", appLanguage)
                        else
                            L10n.string("treasure_not_yet", appLanguage),
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(16.dp))

                if (isUnlocked) {
                    // Description
                    Text(
                        text = L10n.string(item.descKey, appLanguage),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Praise
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "\"${L10n.string(item.praiseKey, appLanguage)}\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Featured button
                    if (isFeatured) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(AppColors.GradientGold),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = L10n.string("treasure_is_featured", appLanguage),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = onSetFeatured,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.WarnGold
                            )
                        ) {
                            Text(
                                text = L10n.string("treasure_set_featured", appLanguage),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Locked hint
                    val pointsNeeded = item.requiredPoints - totalPoints
                    Text(
                        text = kmpFormat(L10n.string("treasure_hint_points", appLanguage), pointsNeeded),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress toward this treasure
                    val progress = (totalPoints.toFloat() / item.requiredPoints).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = AppColors.WarnGold,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "$totalPoints / ${item.requiredPoints} P",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(L10n.string("cancel", appLanguage))
                }
            }
        }
    }
}
