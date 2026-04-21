package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import kotlin.random.Random

// ─── Game mode ────────────────────────────────────────────────────────────────

/** Odd-numbered stages (1, 3, 5 …) are multiple-choice; even-numbered are fill-in */
fun isMultiChoiceStage(stageNumber: Int): Boolean = stageNumber % 2 == 1

/** Generate 4 answer choices (one correct, three distractors) */
fun generateChoices(answer: Int): List<Int> {
    val choices = mutableSetOf(answer)
    var attempts = 0
    while (choices.size < 4 && attempts < 40) {
        attempts++
        val delta = when {
            answer <= 5  -> Random.nextInt(1, 6)
            answer <= 20 -> Random.nextInt(1, 8)
            else         -> Random.nextInt(1, 15)
        }
        val candidate = if (Random.nextBoolean()) answer + delta else maxOf(0, answer - delta)
        choices.add(candidate)
    }
    return choices.toList().shuffled()
}

@Composable
fun GameScreen(
    operation: MathOperation,
    stageNumber: Int,
    appLanguage: AppLanguage,
    onComplete: (stars: Int, correctCount: Int, avgSeconds: Float) -> Unit,
    onBack: () -> Unit,
    isQuestMode: Boolean = false
) {
    val isMultiChoice = remember { isMultiChoiceStage(stageNumber) }
    val problems = remember(operation, stageNumber) { generateProblems(operation, stageNumber) }

    var currentIndex by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var elapsedTime by remember { mutableFloatStateOf(0f) }
    var showResult by remember { mutableStateOf<Boolean?>(null) }
    var showQuitDialog by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isInputFocused by remember { mutableStateOf(false) }
    var lastInputMutationAt by remember { mutableLongStateOf(0L) }
    val effectiveStart = remember { mutableLongStateOf(currentTimeMillis()) }

    val firstAttemptDone = remember { BooleanArray(10) { false } }
    val correctOnFirstAttempt = remember { BooleanArray(10) { false } }

    var showCountdown by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(showCountdown) {
        if (showCountdown) return@LaunchedEffect
        if (!isMultiChoice) {
            repeat(6) { yield(); focusRequester.requestFocus(); delay(80) }
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
        if (showResult == null && currentIndex < 10 && !showCountdown && !isMultiChoice) {
            focusRequester.requestFocus()
        }
    }

    val onSubmitAnswer = { answer: String ->
        val prob = if (currentIndex < problems.size) problems[currentIndex] else null
        val ans = answer.toIntOrNull()
        if (answer.isNotEmpty() && answer != "-" && ans != null && prob != null) {
            val isCorrect = ans == prob.answer
            if (!firstAttemptDone[currentIndex]) {
                firstAttemptDone[currentIndex] = true
                if (isCorrect) correctOnFirstAttempt[currentIndex] = true
            }
            showResult = isCorrect
            lastInputMutationAt = currentTimeMillis()
        }
    }

    val onSubmit = { onSubmitAnswer(userAnswer) }

    val currentProblem = if (currentIndex < problems.size) problems[currentIndex] else null
    val showMinus = operation == MathOperation.SUBTRACTION && !isMultiChoice

    // Multiple choice answers for current problem
    val multiChoices = remember(currentIndex) {
        currentProblem?.let { generateChoices(it.answer) } ?: emptyList()
    }

    if (!isMultiChoice) {
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
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (showCountdown) {
            CountdownOverlay(appLanguage = appLanguage, onComplete = { showCountdown = false })
            return@BoxWithConstraints
        }

        // Hidden iOS text input bridge (fill-in-blank only)
        if (!isMultiChoice) {
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
                        .align(Alignment.TopStart).size(12.dp).alpha(0.01f)
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
                    modifier = Modifier.align(Alignment.TopStart).size(1.dp).alpha(0.01f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state -> isInputFocused = state.isFocused }.focusable()
                )
            }
        }

        if (currentProblem != null) {
            val isLandscape = maxWidth > maxHeight
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Question progress bar (top) ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((currentIndex.toFloat() / 10f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(
                                Brush.linearGradient(AppColors.GradientTeal),
                                RoundedCornerShape(bottomEnd = 6.dp, topEnd = 0.dp)
                            )
                    )
                }

                // ── Top bar with timer gradient ──
                GameTopBar(
                    operation = operation,
                    stageNumber = stageNumber,
                    currentIndex = currentIndex,
                    appLanguage = appLanguage,
                    onQuit = { showQuitDialog = true; isPaused = true },
                    isQuestMode = isQuestMode
                )

                // ── Progress dots ──
                ProgressDots(currentIndex = currentIndex, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))

                if (isMultiChoice) {
                    // ── Multiple choice layout ──
                    MultiChoiceContent(
                        problem = currentProblem,
                        choices = multiChoices,
                        appLanguage = appLanguage,
                        onChoiceSelected = { choice ->
                            if (showResult == null) {
                                onSubmitAnswer(choice.toString())
                            }
                        }
                    )
                } else {
                    // ── Fill-in-blank layout ──
                    if (isLandscape) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                ProblemCard(problem = currentProblem, userAnswer = userAnswer, modifier = Modifier.padding(24.dp))
                            }
                            Box(modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
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
                        Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(currentProblem.displayText, style = MaterialTheme.typography.displayLarge, fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(32.dp))
                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text(text = userAnswer.ifEmpty { " " }, modifier = Modifier.padding(horizontal = 40.dp, vertical = 16.dp), style = MaterialTheme.typography.displayMedium)
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
            }
        } else {
            // All 10 problems done
            val correctCount = correctOnFirstAttempt.count { it }
            val avgSeconds = elapsedTime / 10f
            val stars = calcStars(correctCount, elapsedTime)
            LaunchedEffect(Unit) { onComplete(stars, correctCount, avgSeconds) }
        }

        // Feedback overlay
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
                        if (correct) AppColors.OverlayGreenOk else AppColors.OverlayRedWrong
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
                onDismiss = { showQuitDialog = false; isPaused = false; if (!isMultiChoice) focusRequester.requestFocus() },
                onConfirm = { onBack() }
            )
        }
    }
}

// ─── Game Top Bar ─────────────────────────────────────────────────────────────

@Composable
private fun GameTopBar(
    operation: MathOperation,
    stageNumber: Int,
    currentIndex: Int,
    appLanguage: AppLanguage,
    onQuit: () -> Unit,
    isQuestMode: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(AppColors.GradientTealBlue))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        IconButton(onClick = onQuit, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
        }
        Text(
            text = if (isQuestMode)
                "🎯 ${L10n.string("quest_title", appLanguage)}"
            else
                "${opName(operation, appLanguage)} ${L10n.string("stage_num_format", appLanguage, stageNumber)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${currentIndex + 1} / 10",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─── Progress Dots ────────────────────────────────────────────────────────────

@Composable
private fun ProgressDots(currentIndex: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (0..9).forEach { idx ->
            when {
                idx < currentIndex -> {
                    // Completed: green circle
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(AppColors.SuccessGreen, CircleShape)
                    )
                }
                idx == currentIndex -> {
                    // Current: pink elongated oval
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .width(24.dp)
                            .background(AppColors.PrimaryPink, RoundedCornerShape(5.dp))
                    )
                }
                else -> {
                    // Waiting: light purple
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(AppColors.SecondaryPurple.copy(alpha = 0.25f), CircleShape)
                    )
                }
            }
        }
    }
}

// ─── Multiple Choice Content ──────────────────────────────────────────────────

@Composable
private fun MultiChoiceContent(
    problem: MathProblem,
    choices: List<Int>,
    appLanguage: AppLanguage,
    onChoiceSelected: (Int) -> Unit
) {
    val character = remember { getSelectedCharacter() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Problem label + character
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${L10n.string("stage_num_format", appLanguage, 0).take(0)}문제",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = character?.emoji ?: "🦊", fontSize = 40.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Problem formula card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 32.dp)
            ) {
                Text(
                    text = problem.displayText + " = ?",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2x2 answer buttons
        val answerGradients = listOf(
            AppColors.GradientPink,
            AppColors.GradientTeal,
            AppColors.GradientGold,
            AppColors.GradientGreen
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            choices.chunked(2).forEachIndexed { rowIdx, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEachIndexed { colIdx, choice ->
                        val gradIdx = rowIdx * 2 + colIdx
                        ChoiceButton(
                            value = choice,
                            gradient = answerGradients.getOrElse(gradIdx) { AppColors.GradientPink },
                            onClick = { onChoiceSelected(choice) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceButton(
    value: Int,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(88.dp)
            .background(
                Brush.linearGradient(gradient),
                shape = RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
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
            Text(problem.displayText, style = MaterialTheme.typography.displayLarge, fontSize = 64.sp)
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    text = userAnswer.ifEmpty { " " },
                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.displayMedium, fontSize = 48.sp
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
                c.isDigit()           -> append(c)
                c == '-' && isEmpty() -> append(c)
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
                Text(L10n.string("quit_confirm", appLanguage), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(L10n.string("no", appLanguage)) }
                    Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.weight(1f)) { Text(L10n.string("yes", appLanguage)) }
                }
            }
        }
    }
}
