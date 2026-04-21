package com.mathapp.practice.ui

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Stage map layout constants ───────────────────────────────────────────────

/** Horizontal center positions as fraction of container width */
private val STAGE_X_FRACTIONS = floatArrayOf(
    0.50f, 0.24f, 0.66f, 0.20f, 0.62f,
    0.48f, 0.26f, 0.68f, 0.22f, 0.58f
)

private val STAGE_SPACING   = 120.dp
private val NODE_NORMAL     = 72.dp
private val NODE_CURRENT    = 88.dp
private val MAP_TOP_PAD     = 50.dp
private val MAP_BOTTOM_PAD  = 80.dp

// ─── Stage Map Screen ─────────────────────────────────────────────────────────

@Composable
fun StageMapScreen(
    operation: MathOperation,
    appLanguage: AppLanguage,
    onStageSelected: (Int) -> Unit,
    onBack: () -> Unit,
    progressVersion: Int,
    heartsVersion: Int,
    autoSelectStage: Int? = null
) {
    val stages     = remember(progressVersion) { stagesFor(operation) }
    val currentIdx = stages.indexOfFirst { it.isUnlocked && it.stars == 0 }
        .let { if (it == -1) stages.size - 1 else it }
    val hearts       = rememberLiveHearts(heartsVersion)
    val character    = remember { getSelectedCharacter() }
    val equippedHead = remember { getEquippedItem(ShopCategory.HEAD) }
    val equippedBg   = remember { getEquippedItem(ShopCategory.BACKGROUND) }

    var pendingStage by remember { mutableStateOf(autoSelectStage) }

    val foxBounce by rememberInfiniteTransition(label = "fox").animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "foxBounce"
    )

    // Character movement animation between stages
    // If autoSelectStage is set (came from completed game), start character from the previous stage
    val initialFromIdx = if (autoSelectStage != null) (autoSelectStage - 2).coerceAtLeast(0) else currentIdx
    var animFromIdx by remember { mutableStateOf(initialFromIdx) }
    var animToIdx   by remember { mutableStateOf(currentIdx) }
    val charMoveProgress = remember { Animatable(if (initialFromIdx != currentIdx) 0f else 1f) }

    // Trigger entry animation when arriving from a completed game
    LaunchedEffect(Unit) {
        if (charMoveProgress.value < 1f) {
            charMoveProgress.animateTo(1f, animationSpec = tween(durationMillis = 900))
        }
    }

    LaunchedEffect(currentIdx) {
        if (animToIdx != currentIdx) {
            animFromIdx = animToIdx
            animToIdx   = currentIdx
            charMoveProgress.snapTo(0f)
            charMoveProgress.animateTo(
                1f,
                animationSpec = tween(durationMillis = 900)
            )
        }
    }

    val scrollState = rememberScrollState()

    // ── Confirmation dialog ──
    pendingStage?.let { stageNum ->
        val stageLabel = L10n.string("stage_num_format", appLanguage, stageNum)
        AlertDialog(
            onDismissRequest = { pendingStage = null },
            title = { Text(L10n.string("stage_confirm_title", appLanguage)) },
            text  = { Text(L10n.string("stage_confirm_message", appLanguage, stageLabel)) },
            confirmButton = {
                Button(onClick = { pendingStage = null; onStageSelected(stageNum) }) {
                    Text(L10n.string("stage_start", appLanguage))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingStage = null }) {
                    Text(L10n.string("cancel", appLanguage))
                }
            }
        )
    }

    val bgBrush = Brush.linearGradient(
        listOf(Color(0xFFFFE5F1), Color(0xFFE8D5FF), Color(0xFFD5F0FF))
    )

    Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // White rounded back button
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color(0xFF4A3F35)
                        )
                    }
                }

                // Title
                Text(
                    text = "${opName(operation, appLanguage)} ${L10n.string("stage_map_title", appLanguage)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.SecondaryPurple,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                // Hearts badge
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "❤️ $hearts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }
            }

            // ── Winding Adventure Map ──
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val containerWidth = maxWidth
                val density        = LocalDensity.current
                val stageCount     = stages.size
                val totalHeight    = STAGE_SPACING * (stageCount - 1) + MAP_TOP_PAD + MAP_BOTTOM_PAD

                // Stage center position (Dp)
                fun stageCenter(idx: Int): Pair<Dp, Dp> {
                    val xFrac = STAGE_X_FRACTIONS[idx % STAGE_X_FRACTIONS.size]
                    val x = containerWidth * xFrac
                    val y = STAGE_SPACING * (stageCount - 1 - idx) + MAP_TOP_PAD + NODE_NORMAL / 2
                    return x to y
                }

                // Auto-scroll to current stage
                LaunchedEffect(currentIdx, stageCount) {
                    val (_, cy) = stageCenter(currentIdx)
                    val screenHalfPx = with(density) { (maxHeight / 2).roundToPx() }
                    val stageYPx     = with(density) { cy.roundToPx() }
                    val target       = maxOf(0, stageYPx - screenHalfPx)
                    scrollState.scrollTo(target)
                }

                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(totalHeight)
                    ) {

                        // ── Canvas: winding dashed path ──
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val spacingPx  = with(density) { STAGE_SPACING.toPx() }
                            val topPadPx   = with(density) { MAP_TOP_PAD.toPx() }
                            val nodeHalfPx = with(density) { (NODE_NORMAL / 2).toPx() }

                            val centers = stages.indices.map { idx ->
                                val xFrac = STAGE_X_FRACTIONS[idx % STAGE_X_FRACTIONS.size]
                                val x = size.width * xFrac
                                val y = spacingPx * (stageCount - 1 - idx) + topPadPx + nodeHalfPx
                                Offset(x, y)
                            }

                            val path = Path()
                            if (centers.isNotEmpty()) {
                                path.moveTo(centers[0].x, centers[0].y)
                                for (i in 1 until centers.size) {
                                    val prev = centers[i - 1]
                                    val curr = centers[i]
                                    val midY = (prev.y + curr.y) / 2f
                                    path.cubicTo(prev.x, midY, curr.x, midY, curr.x, curr.y)
                                }
                            }

                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFF69B4).copy(alpha = 0.28f),
                                        Color(0xFFFFB800).copy(alpha = 0.28f)
                                    ),
                                    startY = 0f,
                                    endY = size.height
                                ),
                                style = Stroke(
                                    width = with(density) { 28.dp.toPx() },
                                    cap = StrokeCap.Round,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(
                                            with(density) { 18.dp.toPx() },
                                            with(density) { 9.dp.toPx() }
                                        )
                                    )
                                )
                            )
                        }

                        // ── Decorations: clouds ──
                        listOf(
                            0.04f to 0.06f,
                            0.65f to 0.13f,
                            0.08f to 0.45f,
                            0.62f to 0.58f
                        ).forEach { (xF, yF) ->
                            Text(
                                "☁️",
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .absoluteOffset(x = containerWidth * xF, y = totalHeight * yF)
                                    .alpha(0.55f)
                            )
                        }

                        // ── Decorations: trees ──
                        listOf(
                            0.03f to 0.24f,
                            0.78f to 0.37f,
                            0.04f to 0.66f
                        ).forEach { (xF, yF) ->
                            Text(
                                "🌳",
                                fontSize = 32.sp,
                                modifier = Modifier
                                    .absoluteOffset(x = containerWidth * xF, y = totalHeight * yF)
                                    .alpha(0.75f)
                            )
                        }

                        // ── Stage nodes ──
                        stages.forEachIndexed { idx, stage ->
                            val isCurrent = idx == currentIdx
                            val isLocked  = !stage.isUnlocked
                            val nodeSize  = if (isCurrent) NODE_CURRENT else NODE_NORMAL
                            val (cx, cy)  = stageCenter(idx)

                            MapStageNode(
                                stage = stage,
                                stageNumber = idx + 1,
                                isCurrent = isCurrent,
                                nodeSize = nodeSize,
                                appLanguage = appLanguage,
                                onClick = if (isLocked || hearts <= 0) null
                                          else { { pendingStage = stage.number } },
                                modifier = Modifier.absoluteOffset(
                                    x = cx - nodeSize / 2,
                                    y = cy - nodeSize / 2
                                )
                            )
                        }

                        // ── Character (animated movement + bounce at current stage) ──
                        if (animToIdx in stages.indices) {
                            val safeFromIdx = animFromIdx.coerceIn(0, stages.size - 1)
                            val (fromCx, fromCy) = stageCenter(safeFromIdx)
                            val (toCx, toCy)     = stageCenter(animToIdx)
                            val p = charMoveProgress.value
                            val charX = fromCx + (toCx - fromCx) * p
                            val charY = fromCy + (toCy - fromCy) * p
                            val activeBounce = if (p >= 1f) foxBounce else 0f
                            val portraitSize = 44.dp
                            CharacterPortrait(
                                character = character,
                                size = portraitSize,
                                headItem = equippedHead,
                                bgItem = equippedBg,
                                modifier = Modifier.absoluteOffset(
                                    x = charX - portraitSize / 2,
                                    y = charY - NODE_CURRENT / 2 - portraitSize / 2 + activeBounce.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Map Stage Node ───────────────────────────────────────────────────────────

@Composable
private fun MapStageNode(
    stage: StageInfo,
    stageNumber: Int,
    isCurrent: Boolean,
    nodeSize: Dp,
    appLanguage: AppLanguage,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isLocked = !stage.isUnlocked

    val nodeBrush = when {
        isLocked         -> Brush.linearGradient(listOf(Color(0xFFE5E7EB), Color(0xFFD1D5DB)))
        stage.stars == 3 -> Brush.linearGradient(AppColors.GradientTealBlue)
        stage.stars > 0  -> Brush.linearGradient(AppColors.GradientGreen)
        isCurrent        -> Brush.linearGradient(listOf(Color(0xFFFFB800), Color(0xFFFF69B4)))
        else             -> Brush.linearGradient(listOf(Color(0xFFD1D5DB), Color(0xFFBBBECC)))
    }

    val pulseScale by if (isCurrent && !isLocked) {
        rememberInfiniteTransition(label = "pulse_$stageNumber").animateFloat(
            initialValue = 1f, targetValue = 1.08f,
            animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
            label = "pulseScale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    // MC/SA label for stage type
    val isMultiChoice = stageNumber % 2 == 1

    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier
            .size(nodeSize)
            .scale(if (isCurrent) pulseScale else 1f),
        shape = CircleShape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(nodeBrush, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    isLocked -> Text(
                        "🔒",
                        fontSize = if (isCurrent) 24.sp else 20.sp
                    )
                    stage.stars > 0 -> {
                        Text(
                            starsString(stage.stars),
                            fontSize = if (isCurrent) 15.sp else 12.sp
                        )
                        Text(
                            stageNumber.toString(),
                            fontSize = if (isCurrent) 13.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    else -> Text(
                        stageNumber.toString(),
                        fontSize = if (isCurrent) 28.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) Color(0xFF9CA3AF) else Color.White
                    )
                }
            }
        }
    }
}

private fun starsString(stars: Int): String = when (stars) {
    1 -> "⭐"
    2 -> "⭐⭐"
    3 -> "⭐⭐⭐"
    else -> ""
}
