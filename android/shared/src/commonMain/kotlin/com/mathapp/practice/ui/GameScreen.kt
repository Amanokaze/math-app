package com.mathapp.practice.ui

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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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

@Composable
fun GameScreen(
    operation: MathOperation,
    stageNumber: Int,
    appLanguage: AppLanguage,
    onComplete: (stars: Int, correctCount: Int, avgSeconds: Float) -> Unit,
    onBack: () -> Unit
) {
    val problems = remember(operation, stageNumber) { generateProblems(operation, stageNumber) }

    var currentIndex by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    // Hidden timer: track elapsed wall-clock time from game start
    var elapsedTime by remember { mutableFloatStateOf(0f) }
    var showResult by remember { mutableStateOf<Boolean?>(null) }
    var showQuitDialog by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isInputFocused by remember { mutableStateOf(false) }
    var lastInputMutationAt by remember { mutableLongStateOf(0L) }
    val effectiveStart = remember { mutableLongStateOf(currentTimeMillis()) }

    // Track first-attempt correctness for each problem (for star calculation)
    val firstAttemptDone = remember { BooleanArray(10) { false } }
    val correctOnFirstAttempt = remember { BooleanArray(10) { false } }

    // Countdown before game starts
    var showCountdown by remember { mutableStateOf(true) }

    val focusRequester = remember { FocusRequester() }

    // Timer loop (runs after countdown, hidden from UI)
    LaunchedEffect(showCountdown) {
        if (showCountdown) return@LaunchedEffect
        repeat(6) { yield(); focusRequester.requestFocus(); delay(80) }
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
        if (showResult == null && currentIndex < 10 && !showCountdown) {
            focusRequester.requestFocus()
        }
    }

    val onSubmit = {
        val prob = if (currentIndex < problems.size) problems[currentIndex] else null
        val ans = userAnswer.toIntOrNull()
        if (userAnswer.isNotEmpty() && userAnswer != "-" && ans != null && prob != null) {
            val isCorrect = ans == prob.answer
            if (!firstAttemptDone[currentIndex]) {
                firstAttemptDone[currentIndex] = true
                if (isCorrect) correctOnFirstAttempt[currentIndex] = true
            }
            showResult = isCorrect
            lastInputMutationAt = currentTimeMillis()
        }
    }

    val currentProblem = if (currentIndex < problems.size) problems[currentIndex] else null
    val showMinus = operation == MathOperation.SUBTRACTION

    DisposableEffect(Unit) {
        setHardwareKeyboardHandler(
            onDigit = { digit ->
                val now = currentTimeMillis()
                if (currentIndex < 10 && showResult == null && !showQuitDialog && !showCountdown && now - lastInputMutationAt > 35) {
                    appendDigit(digit, userAnswer) { userAnswer = it }
                    lastInputMutationAt = now
                }
            },
            onBackspace = {
                val now = currentTimeMillis()
                if (currentIndex < 10 && showResult == null && !showQuitDialog && !showCountdown && now - lastInputMutationAt > 35) {
                    if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1)
                    lastInputMutationAt = now
                }
            },
            onSubmit = {
                val now = currentTimeMillis()
                if (currentIndex < 10 && showResult == null && !showQuitDialog && !showCountdown && now - lastInputMutationAt > 35) {
                    onSubmit()
                    lastInputMutationAt = now
                }
            },
            onMinus = {
                val now = currentTimeMillis()
                if (showMinus && currentIndex < 10 && showResult == null && !showQuitDialog && !showCountdown && now - lastInputMutationAt > 35) {
                    if (userAnswer.isEmpty()) userAnswer = "-"
                    lastInputMutationAt = now
                }
            }
        )
        onDispose { setHardwareKeyboardHandler() }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Countdown overlay
        if (showCountdown) {
            CountdownOverlay(appLanguage = appLanguage, onComplete = { showCountdown = false })
            return@BoxWithConstraints
        }

        // Hidden iOS text input bridge
        if (requiresHiddenTextInputBridge()) {
            BasicTextField(
                value = userAnswer,
                onValueChange = { raw ->
                    val submitRequested = raw.any { it == '\n' || it == '\r' }
                    userAnswer = sanitizeAnswerInput(raw)
                    lastInputMutationAt = currentTimeMillis()
                    if (submitRequested) onSubmit()
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(12.dp)
                    .alpha(0.01f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state -> isInputFocused = state.isFocused }
                    .focusable()
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyUp) {
                            when (keyEvent.key) {
                                Key.Enter, Key.NumPadEnter -> { onSubmit(); true }
                                else -> when (keyEvent.utf16CodePoint) {
                                    10, 13 -> { onSubmit(); true }
                                    else -> false
                                }
                            }
                        } else false
                    }
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(1.dp)
                    .alpha(0.01f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state -> isInputFocused = state.isFocused }
                    .focusable()
            )
        }

        val isLandscape = maxWidth > maxHeight

        if (currentProblem != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Top bar (no timer shown) ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showQuitDialog = true; isPaused = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    // Stage label
                    Text(
                        "${opName(operation, appLanguage)} ${L10n.string("stage_num_format", appLanguage, stageNumber)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Problem progress (e.g. "3 / 10")
                    Text(
                        L10n.string("problem_progress", appLanguage, currentIndex + 1),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Progress bar (thin, shows problem index visually)
                LinearProgressIndicator(
                    progress = { (currentIndex + 1) / 10f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                if (isLandscape) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        // Left: problem
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            ProblemCard(
                                problem = currentProblem,
                                userAnswer = userAnswer,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                        // Right: keypad
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
                                showMinus = showMinus,
                                onDigit = { d -> appendDigit(d, userAnswer) { userAnswer = it }; lastInputMutationAt = currentTimeMillis(); focusRequester.requestFocus() },
                                onBackspace = { if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1); lastInputMutationAt = currentTimeMillis(); focusRequester.requestFocus() },
                                onSubmit = { onSubmit(); lastInputMutationAt = currentTimeMillis(); focusRequester.requestFocus() },
                                onMinus = { if (showMinus && userAnswer.isEmpty()) userAnswer = "-"; lastInputMutationAt = currentTimeMillis(); focusRequester.requestFocus() }
                            )
                        }
                    }
                } else {
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
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        confirmTitle = L10n.string("confirm", appLanguage),
                        showMinus = showMinus,
                        onDigit = { d -> appendDigit(d, userAnswer) { userAnswer = it }; lastInputMutationAt = currentTimeMillis(); focusRequester.requestFocus() },
                        onBackspace = { if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1); lastInputMutationAt = currentTimeMillis(); focusRequester.requestFocus() },
                        onSubmit = { onSubmit(); lastInputMutationAt = currentTimeMillis(); focusRequester.requestFocus() },
                        onMinus = { if (showMinus && userAnswer.isEmpty()) userAnswer = "-"; lastInputMutationAt = currentTimeMillis(); focusRequester.requestFocus() }
                    )
                }
            }
        } else {
            // All 10 problems done — calculate and report
            val correctCount = correctOnFirstAttempt.count { it }
            val avgSeconds = elapsedTime / 10f
            val stars = calcStars(correctCount, elapsedTime)
            LaunchedEffect(Unit) {
                onComplete(stars, correctCount, avgSeconds)
            }
        }

        // Feedback overlay (correct / wrong)
        showResult?.let { correct ->
            LaunchedEffect(correct) {
                delay(if (correct) 400L else 700L)
                showResult = null
                userAnswer = ""
                if (correct) currentIndex++
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (correct) Color(0x4400CC44)
                        else Color(0x44FF4444)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (correct) "✓" else "✗",
                    fontSize = 120.sp,
                    color = if (correct) Color(0xFF00AA00) else Color(0xFFCC0000)
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

// ─── Problem card (landscape mode) ───────────────────────────────────────────

@Composable
private fun ProblemCard(problem: MathProblem, userAnswer: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                problem.displayText,
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

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun appendDigit(digit: String, current: String, update: (String) -> Unit) {
    val maxLen = if (current.startsWith("-")) 5 else 4
    if (current.length < maxLen) update(current + digit)
}

private fun sanitizeAnswerInput(raw: String): String {
    val stripped = raw.filterNot { it == '\n' || it == '\r' }
    val cleaned = buildString {
        for (c in stripped) {
            when {
                c.isDigit()              -> append(c)
                c == '-' && isEmpty()    -> append(c)
            }
        }
    }
    val maxLen = if (cleaned.startsWith("-")) 5 else 4
    return cleaned.take(maxLen)
}

// ─── Quit dialog ──────────────────────────────────────────────────────────────

@Composable
fun QuitConfirmDialog(appLanguage: AppLanguage, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth(0.85f).padding(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    L10n.string("quit_confirm", appLanguage),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(L10n.string("no", appLanguage))
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(L10n.string("yes", appLanguage))
                    }
                }
            }
        }
    }
}
