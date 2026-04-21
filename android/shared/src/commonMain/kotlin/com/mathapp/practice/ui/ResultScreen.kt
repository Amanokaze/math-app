package com.mathapp.practice.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ResultScreen(
    operation: MathOperation,
    stageNumber: Int,
    stars: Int,
    avgSeconds: Float,
    points: Int,
    coinsEarned: Int,
    hearts: Int,
    appLanguage: AppLanguage,
    hasNextStage: Boolean,
    onRetry: () -> Unit,
    onNextStage: () -> Unit,
    onToHome: () -> Unit
) {
    val character      = remember { getSelectedCharacter() }
    val equippedEffect = remember { getEquippedItem(ShopCategory.RESULT_EFFECT) }
    val equippedHead   = remember { getEquippedItem(ShopCategory.HEAD) }
    val equippedBadge  = remember { getEquippedItem(ShopCategory.BADGE) }
    val equippedBg     = remember { getEquippedItem(ShopCategory.BACKGROUND) }
    val titleKey = when (stars) {
        3 -> "result_title_3star"
        2 -> "result_title_2star"
        else -> "result_title_1star"
    }

    val bgGradient = when (stars) {
        3 -> Brush.linearGradient(listOf(Color(0xFFFFE5F0), Color(0xFFE5EFFF)))
        2 -> Brush.linearGradient(listOf(Color(0xFFE5F9FF), Color(0xFFE5FFEF)))
        else -> Brush.linearGradient(listOf(Color(0xFFFFF9E5), Color(0xFFE5F0FF)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        if (stars == 3) {
            ConfettiView(modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Top: hearts + stage label ──
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HeartsHeader(hearts = hearts, appLanguage = appLanguage)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${opName(operation, appLanguage)} ${L10n.string("stage_num_format", appLanguage, stageNumber)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Middle: character + title + stars + stats ──
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Equipped result effect row
                if (equippedEffect != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(6) { Text(equippedEffect.emoji, fontSize = 26.sp) }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Character + Title message (combined)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CharacterPortrait(
                        character = character,
                        size = 70.dp,
                        headItem = equippedHead,
                        badgeItem = equippedBadge,
                        bgItem = equippedBg
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            L10n.string(titleKey, appLanguage),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = AppColors.SecondaryPurple
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StarDisplay(stars = stars)
                    }
                }

                // Points + Avg time + Coins (one row)
                ResultStatsRow(points = points, coinsEarned = coinsEarned, avgSeconds = avgSeconds, appLanguage = appLanguage)
            }

            // ── Bottom: action buttons ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasNextStage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .background(
                                Brush.linearGradient(AppColors.GradientGreenBlue),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Button(
                            onClick = onNextStage,
                            enabled = hearts > 0,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(
                                "→ ${L10n.string("next_stage", appLanguage)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            Brush.linearGradient(AppColors.GradientGold),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Button(
                        onClick = onRetry,
                        enabled = hearts > 0,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            "↻ ${L10n.string("retry", appLanguage)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                OutlinedButton(
                    onClick = onToHome,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "🏠 ${L10n.string("to_home", appLanguage)}",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ─── Result stats row (points + avg time) ────────────────────────────────────

@Composable
private fun ResultStatsRow(points: Int, coinsEarned: Int, avgSeconds: Float, appLanguage: AppLanguage) {
    var displayedPoints by remember { mutableIntStateOf(0) }
    var displayedCoins  by remember { mutableIntStateOf(0) }

    LaunchedEffect(points) {
        kotlinx.coroutines.delay(400)
        val steps = 20
        val stepDelay = 500L / steps
        for (i in 1..steps) {
            displayedPoints = points * i / steps
            displayedCoins  = coinsEarned * i / steps
            kotlinx.coroutines.delay(stepDelay)
        }
        displayedPoints = points
        displayedCoins  = coinsEarned
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Learning points (XP)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⭐", fontSize = 24.sp)
                Text(
                    text = "+${displayedPoints}P",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.WarnGold
                )
            }

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Coins earned
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🪙", fontSize = 24.sp)
                Text(
                    text = "+${displayedCoins}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Avg time
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⏱️", fontSize = 24.sp)
                Text(
                    text = L10n.string("avg_time_format", appLanguage, avgSeconds),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.SecondaryPurple
                )
            }
        }
    }
}

// ─── Animated star display ────────────────────────────────────────────────────

@Composable
private fun StarDisplay(stars: Int) {
    val scale1 = remember { Animatable(0f) }
    val scale2 = remember { Animatable(0f) }
    val scale3 = remember { Animatable(0f) }

    LaunchedEffect(stars) {
        kotlinx.coroutines.delay(300)
        scale1.animateTo(1f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f))
        if (stars >= 2) {
            kotlinx.coroutines.delay(150)
            scale2.animateTo(1f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f))
        }
        if (stars >= 3) {
            kotlinx.coroutines.delay(150)
            scale3.animateTo(1f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f))
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val emptyAlpha = 0.2f
        Text(
            "⭐", fontSize = 42.sp,
            modifier = Modifier.scale(if (stars >= 2) scale2.value else 1f),
            color = if (stars >= 2) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(alpha = emptyAlpha)
        )
        Text(
            "⭐", fontSize = 52.sp,
            modifier = Modifier.scale(scale1.value),
            color = if (stars >= 1) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(alpha = emptyAlpha)
        )
        Text(
            "⭐", fontSize = 42.sp,
            modifier = Modifier.scale(if (stars >= 3) scale3.value else 1f),
            color = if (stars >= 3) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(alpha = emptyAlpha)
        )
    }
}

// ─── Confetti (3-star celebration) ───────────────────────────────────────────

@Composable
fun ConfettiView(modifier: Modifier = Modifier) {
    val startTime = remember { currentTimeMillis() }
    var elapsed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16)
            elapsed = (currentTimeMillis() - startTime) / 1000f
        }
    }
    val particles = remember {
        List(80) { i ->
            ConfettiParticle(
                startX = Random.nextFloat(),
                startY = 1f + Random.nextFloat() * 0.2f,
                size = (8 + Random.nextInt(11)).toFloat(),
                color = listOf(
                    Color(0xFF, 0x69, 0xB4), Color(0xFF, 0x8C, 0x00), Color(0xFF, 0xFF, 0x00),
                    Color(0x00, 0xFF, 0x00), Color(0x98, 0xFB, 0x98), Color(0x00, 0xFF, 0xFF),
                    Color(0x00, 0x00, 0xFF), Color(0x8A, 0x2B, 0xE2), Color(0xFF, 0x00, 0x00)
                ).random(),
                startDelay = i * 0.05f,
                speed = 100f + Random.nextFloat() * 120f,
                drift = -120f + Random.nextFloat() * 240f
            )
        }
    }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            val t = maxOf(0f, elapsed - p.startDelay)
            val y = (p.startY * h) - (p.speed * t)
            val x = (p.startX * w) + (p.drift * t * 0.25f) + (sin(t.toDouble() * 2.5).toFloat() * 25f)
            if (y > -50) {
                drawCircle(color = p.color.copy(alpha = 0.85f), radius = p.size, center = Offset(x, y))
            }
        }
    }
}

private data class ConfettiParticle(
    val startX: Float, val startY: Float, val size: Float, val color: Color,
    val startDelay: Float, val speed: Float, val drift: Float
)
