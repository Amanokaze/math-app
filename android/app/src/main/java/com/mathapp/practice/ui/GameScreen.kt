package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

data class MathProblem(val num1: Int, val num2: Int, val op: String) {
    val answer: Int = if (op == "+") num1 + num2 else num1 - num2
    val displayText: String = "$num1 $op $num2"
}

@Composable
fun GameScreen(
    bestTime: Float,
    appLanguage: AppLanguage,
    onComplete: (Float) -> Unit,
    onBack: () -> Unit
) {
    val problems = remember {
        List(10) {
            MathProblem(
                num1 = Random.nextInt(0, 100),
                num2 = Random.nextInt(0, 100),
                op = if (Random.nextBoolean()) "+" else "-"
            )
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var userAnswer by remember { mutableStateOf("") }
    var elapsedTime by remember { mutableStateOf(0f) }
    var showResult by remember { mutableStateOf<Boolean?>(null) }
    var showQuitDialog by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var effectiveStart = remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (currentIndex < 10) {
            delay(10)
            if (!isPaused) {
                elapsedTime = (System.currentTimeMillis() - effectiveStart.value) / 1000f
            }
        }
    }

    LaunchedEffect(isPaused) {
        if (isPaused) {
            // nop - keep elapsedTime as is
        } else {
            effectiveStart.value = System.currentTimeMillis() - (elapsedTime * 1000).toLong()
        }
    }

    val currentProblem = if (currentIndex < problems.size) problems[currentIndex] else null
    val config = LocalConfiguration.current
    val isLandscape = config.screenWidthDp > config.screenHeightDp

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentProblem != null) {
            if (isLandscape) {
                // 가로 모드: 상단바 전체화면, 아래 좌(수식) 우(키패드)
                Column(modifier = Modifier.fillMaxSize()) {
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
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(28.dp)
                                ) {
                                    Text(
                                        currentProblem.displayText,
                                        style = MaterialTheme.typography.displayMedium,
                                        fontSize = 56.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = userAnswer.ifEmpty { " " },
                                            modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontSize = 40.sp
                                        )
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                NumberKeypad(
                                    modifier = Modifier.fillMaxWidth(),
                                    confirmTitle = L10n.string("confirm", appLanguage),
                                    onDigit = { d ->
                                        if (userAnswer.length < 4 || (userAnswer.startsWith("-") && userAnswer.length < 5)) {
                                            userAnswer += d
                                        }
                                    },
                                    onBackspace = { if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1) },
                                    onSubmit = {
                                        val ans = userAnswer.toIntOrNull()
                                        if (userAnswer.isNotEmpty() && userAnswer != "-" && ans != null) {
                                            showResult = (ans == currentProblem.answer)
                                        }
                                    },
                                    onMinus = {
                                        if (userAnswer.isEmpty()) userAnswer = "-"
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // 세로 모드: 기존 레이아웃
                Column(modifier = Modifier.fillMaxSize()) {
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            currentProblem.displayText,
                            style = MaterialTheme.typography.displayMedium,
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = userAnswer.ifEmpty { " " },
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    NumberKeypad(
                        modifier = Modifier.height(240.dp),
                        confirmTitle = L10n.string("confirm", appLanguage),
                        onDigit = { d ->
                            if (userAnswer.length < 4 || (userAnswer.startsWith("-") && userAnswer.length < 5)) {
                                userAnswer += d
                            }
                        },
                        onBackspace = { if (userAnswer.isNotEmpty()) userAnswer = userAnswer.dropLast(1) },
                        onSubmit = {
                            val ans = userAnswer.toIntOrNull()
                            if (userAnswer.isNotEmpty() && userAnswer != "-" && ans != null) {
                                showResult = (ans == currentProblem.answer)
                            }
                        },
                        onMinus = {
                            if (userAnswer.isEmpty()) userAnswer = "-"
                        }
                    )
                }
            }
        } else {
                val isNewRecord = bestTime == 0f || elapsedTime <= bestTime
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        L10n.string("complete", appLanguage),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    if (isNewRecord) {
                        Text(
                            L10n.string("new_record_title", appLanguage),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            L10n.string("new_record_message", appLanguage),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            L10n.string("well_done", appLanguage),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        L10n.string("time_format", appLanguage, elapsedTime),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!isNewRecord && bestTime > 0) {
                        Text(
                            L10n.string("best_record_format", appLanguage, L10n.string("time_format", appLanguage, bestTime))
                        )
                    }
                    Button(
                        onClick = { onComplete(elapsedTime) },
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text(L10n.string("to_main", appLanguage))
                    }
                }
            }
        }

        showResult?.let { correct ->
            LaunchedEffect(correct) {
                delay(500)
                showResult = null
                if (correct) {
                    currentIndex++
                    userAnswer = ""
                    if (currentIndex >= 10) {
                        onComplete(elapsedTime)
                    }
                } else {
                    userAnswer = ""
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
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
            Dialog(
                onDismissRequest = { showQuitDialog = false; isPaused = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            L10n.string("quit_confirm", appLanguage),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showQuitDialog = false
                                    isPaused = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(L10n.string("no", appLanguage))
                            }
                            Button(
                                onClick = {
                                    showQuitDialog = false
                                    onBack()
                                },
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
    }
}
