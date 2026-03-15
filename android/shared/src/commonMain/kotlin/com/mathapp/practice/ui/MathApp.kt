package com.mathapp.practice.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mathapp.practice.ui.theme.MathTheme

// ─── Navigation ───────────────────────────────────────────────────────────────

sealed class Screen {
    object Home : Screen()
    object Category : Screen()
    data class StageMap(val operation: MathOperation) : Screen()
    data class Game(val operation: MathOperation, val stageNumber: Int) : Screen()
    data class Result(
        val operation: MathOperation,
        val stageNumber: Int,
        val stars: Int,
        val correctCount: Int,
        val avgSeconds: Float
    ) : Screen()
}

// ─── Root composable ──────────────────────────────────────────────────────────

@Composable
fun MathApp() {
    var colorSchemeMode by remember { mutableStateOf(AppSettings.getInt("colorSchemeMode", 1)) }
    var appLanguageRaw by remember { mutableStateOf(AppSettings.getString("appLanguage", "")) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    // Bump this whenever stage progress changes so child screens re-read AppSettings
    var progressVersion by remember { mutableIntStateOf(0) }

    val appLanguage = when {
        appLanguageRaw.isEmpty() -> AppLanguage.fromCode(getDeviceLanguageCode())
        else -> AppLanguage.fromCode(appLanguageRaw)
    }

    MathTheme(darkTheme = colorSchemeMode == 2) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val s = screen) {
                is Screen.Home -> HomeScreen(
                    appLanguage = appLanguage,
                    colorSchemeMode = colorSchemeMode,
                    onColorSchemeChange = {
                        colorSchemeMode = it
                        AppSettings.setInt("colorSchemeMode", it)
                    },
                    appLanguageRaw = appLanguageRaw,
                    onAppLanguageChange = {
                        appLanguageRaw = it
                        AppSettings.setString("appLanguage", it)
                    },
                    onResumeClick = { (op, num) ->
                        saveLastPlayedStage(op, num)
                        screen = Screen.Game(op, num)
                    },
                    onNewStartClick = { screen = Screen.Category },
                    onResetProgress = {
                        resetAllProgress()
                        progressVersion++
                    },
                    progressVersion = progressVersion
                )

                is Screen.Category -> CategoryScreen(
                    appLanguage = appLanguage,
                    onOperationSelected = { op -> screen = Screen.StageMap(op) },
                    onBack = { screen = Screen.Home },
                    progressVersion = progressVersion
                )

                is Screen.StageMap -> StageMapScreen(
                    operation = s.operation,
                    appLanguage = appLanguage,
                    onStageSelected = { num ->
                        saveLastPlayedStage(s.operation, num)
                        screen = Screen.Game(s.operation, num)
                    },
                    onBack = { screen = Screen.Category },
                    progressVersion = progressVersion
                )

                is Screen.Game -> GameScreen(
                    operation = s.operation,
                    stageNumber = s.stageNumber,
                    appLanguage = appLanguage,
                    onComplete = { stars, correctCount, avgSec ->
                        saveStageResult(s.operation, s.stageNumber, stars)
                        progressVersion++
                        screen = Screen.Result(s.operation, s.stageNumber, stars, correctCount, avgSec)
                    },
                    onBack = { screen = Screen.StageMap(s.operation) }
                )

                is Screen.Result -> {
                    val nextNum = s.stageNumber + 1
                    val hasNextStage = nextNum <= 10 && StageInfo(s.operation, nextNum).isUnlocked
                    ResultScreen(
                        operation = s.operation,
                        stageNumber = s.stageNumber,
                        stars = s.stars,
                        correctCount = s.correctCount,
                        avgSeconds = s.avgSeconds,
                        appLanguage = appLanguage,
                        hasNextStage = hasNextStage,
                        onRetry = { screen = Screen.Game(s.operation, s.stageNumber) },
                        onNextStage = {
                            if (hasNextStage) screen = Screen.Game(s.operation, nextNum)
                            else screen = Screen.StageMap(s.operation)
                        },
                        onToStageMap = { screen = Screen.StageMap(s.operation) }
                    )
                }
            }
        }
    }
}
