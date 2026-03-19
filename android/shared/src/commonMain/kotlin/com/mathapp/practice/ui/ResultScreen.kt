package com.mathapp.practice.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
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
    correctCount: Int,
    avgSeconds: Float,
    points: Int,
    hearts: Int,
    appLanguage: AppLanguage,
    hasNextStage: Boolean,
    onRetry: () -> Unit,
    onNextStage: () -> Unit,
    onToStageMap: () -> Unit
) {
    val titleKey = when (stars) {
        3    -> "result_title_3star"
        2    -> "result_title_2star"
        else -> "result_title_1star"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Confetti for 3 stars
        if (stars == 3) {
            ConfettiView(modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeartsHeader(hearts = hearts, appLanguage = appLanguage)

            Spacer(modifier = Modifier.weight(1f))

            // Stage label
            Text(
                "${opName(operation, appLanguage)} ${L10n.string("stage_num_format", appLanguage, stageNumber)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                L10n.string(titleKey, appLanguage),
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 36.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Animated stars
            StarDisplay(stars = stars)

            Spacer(modifier = Modifier.height(28.dp))

            // Animated points display
            PointsDisplay(points = points, appLanguage = appLanguage)

            Spacer(modifier = Modifier.height(20.dp))

            // Stats card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        L10n.string("correct_format", appLanguage, correctCount),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        L10n.string("avg_time_format", appLanguage, avgSeconds),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "스테이지 맵으로" — always visible
                OutlinedButton(
                    onClick = onToStageMap,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(L10n.string("to_stage_map", appLanguage), style = MaterialTheme.typography.titleMedium)
                }

                // "다시 하기" — always visible
                OutlinedButton(
                    onClick = onRetry,
                    enabled = hearts > 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(L10n.string("retry", appLanguage), style = MaterialTheme.typography.titleMedium)
                }

                // "다음 단계로" — only if next stage unlocked (primary action)
                if (hasNextStage) {
                    Button(
                        onClick = onNextStage,
                        enabled = hearts > 0,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(L10n.string("next_stage", appLanguage), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
        // Star 2 (left, slightly smaller when inactive — middle-left-right order for visual appeal)
        Text(
            "⭐",
            fontSize = 42.sp,
            modifier = Modifier.scale(if (stars >= 2) scale2.value else 1f),
            color = if (stars >= 2) Color.Unspecified
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = emptyAlpha)
        )
        // Star 1 (center, slightly larger)
        Text(
            "⭐",
            fontSize = 52.sp,
            modifier = Modifier.scale(scale1.value),
            color = if (stars >= 1) Color.Unspecified
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = emptyAlpha)
        )
        // Star 3 (right)
        Text(
            "⭐",
            fontSize = 42.sp,
            modifier = Modifier.scale(if (stars >= 3) scale3.value else 1f),
            color = if (stars >= 3) Color.Unspecified
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = emptyAlpha)
        )
    }
}

// ─── Animated points display ──────────────────────────────────────────────────

@Composable
private fun PointsDisplay(points: Int, appLanguage: AppLanguage) {
    var displayedPoints by remember { mutableIntStateOf(0) }
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(points) {
        kotlinx.coroutines.delay(600)
        scale.animateTo(1.15f, animationSpec = tween(300))
        scale.animateTo(1f, animationSpec = tween(150))
        // Count-up animation
        val steps = 20
        val stepDelay = 500L / steps
        for (i in 1..steps) {
            displayedPoints = (points * i / steps)
            kotlinx.coroutines.delay(stepDelay)
        }
        displayedPoints = points
    }

    Text(
        L10n.string("points_earned", appLanguage, displayedPoints),
        style = MaterialTheme.typography.headlineMedium,
        fontSize = 44.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.scale(scale.value),
        textAlign = TextAlign.Center
    )
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
