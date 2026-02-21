package com.mathapp.practice.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class GameLevel { EASY, NORMAL }

data class MathProblem(val num1: Int, val num2: Int, val op: String) {
    val answer: Int = if (op == "+") num1 + num2 else num1 - num2
    val displayText: String = "$num1 $op $num2"
}

@Composable
fun GameScreen(
    level: GameLevel,
    bestTime: Float,
    appLanguage: AppLanguage,
    onComplete: (Float) -> Unit,
    onBack: () -> Unit
) {
    val problems = remember(level) {
        List(10) {
            when (level) {
                GameLevel.EASY -> {
                    val num1 = Random.nextInt(0, 100)
                    val num2 = Random.nextInt(0, 11)
                    val op = if (Random.nextBoolean()) "+" else "-"
                    if (op == "-" && num2 > num1) {
                        MathProblem(num1 = num2, num2 = num1, op = op)
                    } else {
                        MathProblem(num1 = num1, num2 = num2, op = op)
                    }
                }
                GameLevel.NORMAL -> MathProblem(
                    num1 = Random.nextInt(0, 100),
                    num2 = Random.nextInt(0, 100),
                    op = if (Random.nextBoolean()) "+" else "-"
                )
            }
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var elapsedTime by remember { mutableFloatStateOf(0f) }
    var showResult by remember { mutableStateOf<Boolean?>(null) }
    var showQuitDialog by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    val effectiveStart = remember { mutableLongStateOf(System.currentTimeMillis()) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        while (currentIndex < 10) {
            delay(10)
            if (!isPaused) {
                elapsedTime = (System.currentTimeMillis() - effectiveStart.longValue) / 1000f
            }
        }
    }

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            effectiveStart.longValue = System.currentTimeMillis() - (elapsedTime * 1000).toLong()
        }
    }

    val currentProblem = if (currentIndex < problems.size) problems[currentIndex] else null
    val config = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    val onSubmit = {
        val ans = userAnswer.toIntOrNull()
        if (userAnswer.isNotEmpty() && userAnswer != "-" && ans != null && currentProblem != null) {
            showResult = (ans == currentProblem.answer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Enter, Key.NumPadEnter -> {
                            onSubmit()
                            true
                        }
                        Key.Backspace -> {
                            if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1)
                            true
                        }
                        Key.Minus, Key.NumPadSubtract -> {
                            if (userAnswer.isEmpty()) userAnswer = "-"
                            true
                        }
                        else -> {
                            val char = keyEvent.utf16CodePoint.toChar()
                            if (char.isDigit()) {
                                if (userAnswer.length < 4 || (userAnswer.startsWith("-") && userAnswer.length < 5)) {
                                    userAnswer += char
                                }
                                true
                            } else false
                        }
                    }
                } else false
            }
    ) {
        if (currentProblem != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showQuitDialog = true; isPaused = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Text(
                        L10n.string("time_format", appLanguage, elapsedTime),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${currentIndex + 1}/10",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (isLandscape) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        // Left: Problem
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    Text(
                                        currentProblem.displayText,
                                        style = MaterialTheme.typography.displayLarge,
                                        fontSize = 64.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = userAnswer.ifEmpty { " " },
                                            modifier = Modifier.padding(horizontal = 48.dp, vertical = 12.dp),
                                            style = MaterialTheme.typography.displayMedium,
                                            fontSize = 48.sp
                                        )
                                    }
                                }
                            }
                        }
                        // Right: Keypad
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            NumberKeypad(
                                modifier = Modifier.padding(24.dp),
                                confirmTitle = L10n.string("confirm", appLanguage),
                                onDigit = { d -> if (userAnswer.length < 5) userAnswer += d },
                                onBackspace = { if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1) },
                                onSubmit = onSubmit,
                                onMinus = { if (userAnswer.isEmpty()) userAnswer = "-" }
                            )
                        }
                    }
                } else {
                    // Portrait Mode
                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            currentProblem.displayText,
                            style = MaterialTheme.typography.displayLarge,
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = userAnswer.ifEmpty { " " },
                                modifier = Modifier.padding(horizontal = 40.dp, vertical = 16.dp),
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                    }
                    NumberKeypad(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        confirmTitle = L10n.string("confirm", appLanguage),
                        onDigit = { d -> if (userAnswer.length < 5) userAnswer += d },
                        onBackspace = { if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1) },
                        onSubmit = onSubmit,
                        onMinus = { if (userAnswer.isEmpty()) userAnswer = "-" }
                    )
                }
            }
        } else {
            // Result Screen
            ResultView(elapsedTime, bestTime, appLanguage, onComplete)
        }

        // Feedback & Dialogs
        showResult?.let { correct ->
            LaunchedEffect(correct) {
                delay(500)
                showResult = null
                if (correct) {
                    currentIndex++
                    userAnswer = ""
                } else {
                    userAnswer = ""
                }
            }
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (correct) "✓" else "✗",
                    fontSize = 120.sp,
                    color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }

        if (showQuitDialog) {
            QuitConfirmDialog(
                appLanguage = appLanguage,
                onDismiss = { showQuitDialog = false; isPaused = false },
                onConfirm = { onBack() }
            )
        }
    }
}

@Composable
fun ResultView(elapsedTime: Float, bestTime: Float, appLanguage: AppLanguage, onComplete: (Float) -> Unit) {
    val isNewRecord = bestTime == 0f || elapsedTime <= bestTime
    Box(modifier = Modifier.fillMaxSize()) {
        if (isNewRecord) {
            ConfettiView(modifier = Modifier.fillMaxSize())
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                L10n.string("complete", appLanguage),
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 38.sp
            )
            Spacer(modifier = Modifier.height(28.dp))
            if (isNewRecord) {
                Text(
                    L10n.string("new_record_title", appLanguage),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    L10n.string("new_record_message", appLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            } else {
                Text(
                    L10n.string("well_done", appLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                L10n.string("time_format", appLanguage, elapsedTime),
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (!isNewRecord && bestTime > 0) {
                Text(
                    L10n.string("best_record_format", appLanguage, L10n.string("time_format", appLanguage, bestTime)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Button(
                onClick = { onComplete(elapsedTime) },
                modifier = Modifier
                    .padding(top = 32.dp)
                    .padding(horizontal = 40.dp, vertical = 14.dp)
            ) {
                Text(L10n.string("to_main", appLanguage), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ConfettiView(modifier: Modifier = Modifier) {
    val startTime = remember { System.currentTimeMillis() }
    var elapsed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            elapsed = (System.currentTimeMillis() - startTime) / 1000f
        }
    }
    val particleCount = 80
    val particles = remember {
        List(particleCount) { i ->
            ConfettiParticle(
                startX = Random.nextFloat(),
                startY = 1f + Random.nextFloat() * 0.2f,
                size = (8 + Random.nextInt(11)).toFloat(),
                color = listOf(
                    Color(0xFFFF69B4), Color(0xFFFF8C00), Color(0xFFFFFF00), Color(0xFF00FF00),
                    Color(0xFF98FB98), Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFF8A2BE2), Color(0xFFFF0000)
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
            val x = (p.startX * w) + (p.drift * t * 0.25f) + (kotlin.math.sin(t.toDouble() * 2.5).toFloat() * 25f)
            if (y > -50) {
                drawCircle(color = p.color.copy(alpha = 0.85f), radius = p.size, center = Offset(x, y))
            }
        }
    }
}

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val size: Float,
    val color: Color,
    val startDelay: Float,
    val speed: Float,
    val drift: Float
)

@Composable
fun QuitConfirmDialog(appLanguage: AppLanguage, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth(0.85f).padding(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(L10n.string("quit_confirm", appLanguage), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(L10n.string("no", appLanguage)) }
                    Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.weight(1f)) { Text(L10n.string("yes", appLanguage)) }
                }
            }
        }
    }
}
