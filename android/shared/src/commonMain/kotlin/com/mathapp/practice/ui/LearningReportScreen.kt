package com.mathapp.practice.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LearningReportScreen(
    appLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val character = remember { getSelectedCharacter() }
    val (todaySolved, todayCorrect) = remember { getTodayStats() }
    val accuracy = if (todaySolved > 0) (todayCorrect * 100 / todaySolved) else 0
    val streak = remember { getStreakDays() }
    val weeklyStats = remember { getWeeklyStats() }
    val badges = ALL_BADGES

    val dayLabels = listOf(
        L10n.string("report_day_mon", appLanguage),
        L10n.string("report_day_tue", appLanguage),
        L10n.string("report_day_wed", appLanguage),
        L10n.string("report_day_thu", appLanguage),
        L10n.string("report_day_fri", appLanguage),
        L10n.string("report_day_sat", appLanguage),
        L10n.string("report_day_sun", appLanguage)
    )

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
                .background(Brush.linearGradient(AppColors.GradientPurple))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                text = "${character?.emoji ?: "📊"} ${L10n.string("report_title", appLanguage)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Stats Cards ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                emoji = "📝",
                value = todaySolved.toString(),
                label = L10n.string("report_solved", appLanguage),
                gradientColors = AppColors.GradientTeal,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                emoji = "🎯",
                value = "$accuracy%",
                label = L10n.string("report_accuracy", appLanguage),
                gradientColors = AppColors.GradientPink,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                emoji = "🔥",
                value = "$streak${L10n.string("report_streak_unit", appLanguage)}",
                label = L10n.string("report_streak", appLanguage),
                gradientColors = listOf(Color(0xFFFF8C00), Color(0xFFFF5722)),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Weekly Chart ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = L10n.string("report_weekly", appLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyBarChart(
                    data = weeklyStats,
                    labels = dayLabels,
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Badges ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = L10n.string("report_badges", appLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                val rows = badges.chunked(3)
                rows.forEachIndexed { rowIdx, row ->
                    if (rowIdx > 0) Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { badge ->
                            BadgeItem(
                                badge = badge,
                                earned = isBadgeEarned(badge.id),
                                appLanguage = appLanguage,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // fill empty spots
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(
    emoji: String,
    value: String,
    label: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .background(
                Brush.linearGradient(gradientColors),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun WeeklyBarChart(
    data: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxVal = (data.maxOrNull() ?: 1).coerceAtLeast(1).toFloat()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.zip(labels).forEach { (count, label) ->
            val fraction = count / maxVal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Count label — always reserves the same height to keep bars aligned
                Box(modifier = Modifier.height(16.dp)) {
                    if (count > 0) {
                        Text(
                            text = count.toString(),
                            fontSize = 10.sp,
                            color = AppColors.SecondaryPurple,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Bar area — fills remaining flexible space; bar grows within it
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(fraction.coerceAtLeast(0.04f))
                            .background(
                                Brush.linearGradient(
                                    colors = AppColors.GradientTealBlue,
                                    start = Offset(0f, Float.POSITIVE_INFINITY),
                                    end = Offset(0f, 0f)
                                ),
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BadgeItem(
    badge: Badge,
    earned: Boolean,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    if (earned)
                        Brush.linearGradient(AppColors.GradientGold)
                    else
                        Brush.linearGradient(listOf(Color(0xFFDDDDDD), Color(0xFFBBBBBB))),
                    shape = CircleShape
                )
                .then(
                    if (!earned)
                        Modifier
                    else
                        Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (earned) badge.emoji else "🔒",
                fontSize = 28.sp,
                color = if (earned) Color.Unspecified else Color.White.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = L10n.string(badge.nameKey, appLanguage),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (earned)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
