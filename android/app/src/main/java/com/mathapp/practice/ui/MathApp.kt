package com.mathapp.practice.ui

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mathapp.practice.ui.theme.MathTheme
import java.util.Locale

@Composable
fun MathApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("math_prefs", Context.MODE_PRIVATE) }

    var colorSchemeMode by remember { mutableIntStateOf(prefs.getInt("colorSchemeMode", 1)) }
    var appLanguageRaw by remember {
        mutableStateOf(prefs.getString("appLanguage", "").orEmpty())
    }
    var bestTimeEasy by remember { mutableStateOf(prefs.getFloat("bestTimeEasy", 0f)) }
    var bestTimeNormal by remember { mutableStateOf(prefs.getFloat("bestTimeNormal", 0f)) }
    var selectedLevel by remember { mutableStateOf(GameLevel.NORMAL) }
    var isGameActive by remember { mutableStateOf(false) }
    var showCountdown by remember { mutableStateOf(false) }

    val bestTime = if (selectedLevel == GameLevel.EASY) bestTimeEasy else bestTimeNormal

    LaunchedEffect(Unit) {
        if (bestTimeNormal == 0f) {
            val legacy = prefs.getFloat("bestTime", 0f)
            if (legacy > 0f) {
                bestTimeNormal = legacy
                prefs.edit().putFloat("bestTimeNormal", legacy).apply()
            }
        }
    }

    val appLanguage = when {
        appLanguageRaw.isEmpty() -> AppLanguage.deviceLanguage(Locale.getDefault())
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
                                prefs.edit().putFloat("bestTimeEasy", time).apply()
                            }
                        } else {
                            if (bestTimeNormal == 0f || time < bestTimeNormal) {
                                bestTimeNormal = time
                                prefs.edit().putFloat("bestTimeNormal", time).apply()
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
                        prefs.edit().putInt("colorSchemeMode", it).apply()
                    },
                    appLanguage = appLanguage,
                    appLanguageRaw = appLanguageRaw,
                    onAppLanguageChange = {
                        appLanguageRaw = it
                        prefs.edit().putString("appLanguage", it).apply()
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
                        prefs.edit().putFloat("bestTimeEasy", 0f).putFloat("bestTimeNormal", 0f).apply()
                    }
                )
            }
        }
    }
}
