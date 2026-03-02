package com.mathapp.practice.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mathapp.practice.ui.theme.MathTheme

@Composable
fun MathApp() {
    var colorSchemeMode by remember { mutableStateOf(AppSettings.getInt("colorSchemeMode", 1)) }
    var appLanguageRaw by remember { mutableStateOf(AppSettings.getString("appLanguage", "")) }
    var bestTimeEasy by remember { mutableStateOf(AppSettings.getFloat("bestTimeEasy", 0f)) }
    var bestTimeNormal by remember { mutableStateOf(AppSettings.getFloat("bestTimeNormal", 0f)) }
    var selectedLevel by remember { mutableStateOf(GameLevel.NORMAL) }
    var isGameActive by remember { mutableStateOf(false) }
    var showCountdown by remember { mutableStateOf(false) }

    val bestTime = if (selectedLevel == GameLevel.EASY) bestTimeEasy else bestTimeNormal

    LaunchedEffect(Unit) {
        if (bestTimeNormal == 0f) {
            val legacy = AppSettings.getFloat("bestTime", 0f)
            if (legacy > 0f) {
                bestTimeNormal = legacy
                AppSettings.setFloat("bestTimeNormal", legacy)
            }
        }
    }

    val appLanguage = when {
        appLanguageRaw.isEmpty() -> AppLanguage.fromCode(getDeviceLanguageCode())
        else -> AppLanguage.fromCode(appLanguageRaw)
    }

    MathTheme(darkTheme = colorSchemeMode == 2) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isGameActive) {
                GameScreen(
                    level = selectedLevel,
                    bestTime = bestTime,
                    appLanguage = appLanguage,
                    onComplete = { time ->
                        if (selectedLevel == GameLevel.EASY) {
                            if (bestTimeEasy == 0f || time < bestTimeEasy) {
                                bestTimeEasy = time
                                AppSettings.setFloat("bestTimeEasy", time)
                            }
                        } else {
                            if (bestTimeNormal == 0f || time < bestTimeNormal) {
                                bestTimeNormal = time
                                AppSettings.setFloat("bestTimeNormal", time)
                            }
                        }
                        isGameActive = false
                    },
                    onBack = { isGameActive = false }
                )
            } else {
                MainScreen(
                    colorSchemeMode = colorSchemeMode,
                    onColorSchemeChange = {
                        colorSchemeMode = it
                        AppSettings.setInt("colorSchemeMode", it)
                    },
                    appLanguage = appLanguage,
                    appLanguageRaw = appLanguageRaw,
                    onAppLanguageChange = {
                        appLanguageRaw = it
                        AppSettings.setString("appLanguage", it)
                    },
                    bestTime = bestTime,
                    selectedLevel = selectedLevel,
                    onSelectedLevelChange = { selectedLevel = it },
                    onStartClick = { showCountdown = true },
                    showCountdown = showCountdown,
                    onShowCountdownChange = { showCountdown = it },
                    onGameActiveChange = { isGameActive = it },
                    onResetRecords = {
                        bestTimeEasy = 0f
                        bestTimeNormal = 0f
                        AppSettings.setFloat("bestTimeEasy", 0f)
                        AppSettings.setFloat("bestTimeNormal", 0f)
                    }
                )
            }
        }
    }
}
