package com.mathapp.practice.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
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
    var isInputFocused by remember { mutableStateOf(false) }
    var lastInputMutationAt by remember { mutableLongStateOf(0L) }
    val effectiveStart = remember { mutableLongStateOf(currentTimeMillis()) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // Focus is sometimes not immediately granted on iOS; retry a few times.
        repeat(6) {
            yield()
            focusRequester.requestFocus()
            delay(80)
        }
        while (currentIndex < 10) {
            delay(10)
            if (!isPaused) {
                elapsedTime = (currentTimeMillis() - effectiveStart.longValue) / 1000f
            }
        }
    }

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            effectiveStart.longValue = currentTimeMillis() - (elapsedTime * 1000).toLong()
        }
    }

    LaunchedEffect(showResult) {
        if (showResult == null && currentIndex < 10) {
            focusRequester.requestFocus()
        }
    }

    val onSubmit = {
        val currentProblem = if (currentIndex < problems.size) problems[currentIndex] else null
        val ans = userAnswer.toIntOrNull()
        if (userAnswer.isNotEmpty() && userAnswer != "-" && ans != null && currentProblem != null) {
            showResult = (ans == currentProblem.answer)
            lastInputMutationAt = currentTimeMillis()
        }
    }

    val currentProblem = if (currentIndex < problems.size) problems[currentIndex] else null

    DisposableEffect(Unit) {
        setHardwareKeyboardHandler(
            onDigit = { digit ->
                val now = currentTimeMillis()
                if (currentIndex < 10 && showResult == null && !showQuitDialog && now - lastInputMutationAt > 35) {
                    appendDigit(digit, userAnswer) { userAnswer = it }
                    lastInputMutationAt = now
                }
            },
            onBackspace = {
                val now = currentTimeMillis()
                if (currentIndex < 10 && showResult == null && !showQuitDialog && now - lastInputMutationAt > 35) {
                    if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1)
                    lastInputMutationAt = now
                }
            },
            onSubmit = {
                val now = currentTimeMillis()
                if (currentIndex < 10 && showResult == null && !showQuitDialog && now - lastInputMutationAt > 35) {
                    onSubmit()
                    lastInputMutationAt = now
                }
            },
            onMinus = {
                val now = currentTimeMillis()
                if (currentIndex < 10 && showResult == null && !showQuitDialog && now - lastInputMutationAt > 35) {
                    if (userAnswer.isEmpty()) userAnswer = "-"
                    lastInputMutationAt = now
                }
            }
        )
        onDispose {
            setHardwareKeyboardHandler()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // iOS hardware keyboard input is much more reliable through the platform
        // text input system than through raw key events. Keep this hidden field focused.
        BasicTextField(
            value = userAnswer,
            onValueChange = { raw ->
                val submitRequested = raw.any { it == '\n' || it == '\r' }
                userAnswer = sanitizeAnswerInput(raw)
                lastInputMutationAt = currentTimeMillis()
                if (submitRequested) onSubmit()
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                // Use Ascii so hardware keyboard input is not filtered at the platform layer;
                // we sanitize manually.
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSubmit() }
            ),
            modifier = Modifier
                // Needs a real (non-zero) size and non-zero alpha on iOS to reliably become first responder.
                .align(Alignment.TopStart)
                .size(12.dp)
                .alpha(0.01f)
                .focusRequester(focusRequester)
                .onFocusChanged { state -> isInputFocused = state.isFocused }
                .focusable()
                .onKeyEvent { keyEvent ->
                    // Fallback path for platforms where Enter doesn't get reflected into text.
                    if (keyEvent.type == KeyEventType.KeyUp) {
                        when (keyEvent.key) {
                            Key.Enter, Key.NumPadEnter -> {
                                onSubmit()
                                true
                            }
                            else -> {
                                when (keyEvent.utf16CodePoint) {
                                    10, 13 -> { onSubmit(); true }
                                    else -> false
                                }
                            }
                        }
                    } else {
                        false
                    }
                }
        )

        val isLandscape = maxWidth > maxHeight
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
                                onDigit = { d ->
                                    appendDigit(d, userAnswer) { userAnswer = it }
                                    lastInputMutationAt = currentTimeMillis()
                                    focusRequester.requestFocus()
                                },
                                onBackspace = {
                                    if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1)
                                    lastInputMutationAt = currentTimeMillis()
                                    focusRequester.requestFocus()
                                },
                                onSubmit = {
                                    onSubmit()
                                    lastInputMutationAt = currentTimeMillis()
                                    focusRequester.requestFocus()
                                },
                                onMinus = {
                                    if (userAnswer.isEmpty()) userAnswer = "-"
                                    lastInputMutationAt = currentTimeMillis()
                                    focusRequester.requestFocus()
                                }
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
                        onDigit = { d ->
                            appendDigit(d, userAnswer) { userAnswer = it }
                            lastInputMutationAt = currentTimeMillis()
                            focusRequester.requestFocus()
                        },
                        onBackspace = {
                            if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1)
                            lastInputMutationAt = currentTimeMillis()
                            focusRequester.requestFocus()
                        },
                        onSubmit = {
                            onSubmit()
                            lastInputMutationAt = currentTimeMillis()
                            focusRequester.requestFocus()
                        },
                        onMinus = {
                            if (userAnswer.isEmpty()) userAnswer = "-"
                            lastInputMutationAt = currentTimeMillis()
                            focusRequester.requestFocus()
                        }
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
                onDismiss = { showQuitDialog = false; isPaused = false; focusRequester.requestFocus() },
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
    val startTime = remember { currentTimeMillis() }
    var elapsed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            elapsed = (currentTimeMillis() - startTime) / 1000f
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
                    Color(0xFF, 0x69, 0xB4), Color(0xFF, 0x8C, 0x00), Color(0xFF, 0xFF, 0x00), Color(0x00, 0xFF, 0x00),
                    Color(0x98, 0xFB, 0x98), Color(0x00, 0xFF, 0xFF), Color(0x00, 0x00, 0xFF), Color(0x8A, 0x2B, 0xE2), Color(0xFF, 0x00, 0x00)
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

private fun appendDigit(digit: String, current: String, update: (String) -> Unit) {
    val maxLen = if (current.startsWith("-")) 5 else 4
    if (current.length < maxLen) update(current + digit)
}

private fun sanitizeAnswerInput(raw: String): String {
    // Keep digits and an optional leading '-' only.
    val stripped = raw.filterNot { it == '\n' || it == '\r' }
    val cleaned = buildString {
        for (c in stripped) {
            when {
                c.isDigit() -> append(c)
                c == '-' && isEmpty() -> append(c)
            }
        }
    }
    val maxLen = if (cleaned.startsWith("-")) 5 else 4
    return cleaned.take(maxLen)
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
