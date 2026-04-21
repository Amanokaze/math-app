package com.mathapp.practice.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mathapp.practice.ui.theme.MathTheme

// ─── Navigation ───────────────────────────────────────────────────────────────

sealed class Screen {
    object Onboarding : Screen()
    object CharacterSelection : Screen()
    object Home : Screen()
    object Category : Screen()
    object ParentSettings : Screen()
    data class StageMap(val operation: MathOperation, val autoSelectStage: Int? = null) : Screen()
    data class Game(val operation: MathOperation, val stageNumber: Int) : Screen()
    data class Quest(val operation: MathOperation, val stageNumber: Int) : Screen()
    data class Result(
        val operation: MathOperation,
        val stageNumber: Int,
        val stars: Int,
        val avgSeconds: Float,
        val points: Int,
        val coinsEarned: Int
    ) : Screen()
}

// ─── Root composable ──────────────────────────────────────────────────────────

@Composable
fun MathApp() {
    var colorSchemeMode by remember { mutableStateOf(AppSettings.getInt("colorSchemeMode", 1)) }
    var appLanguageRaw by remember { mutableStateOf(AppSettings.getString("appLanguage", "")) }
    var progressVersion by remember { mutableIntStateOf(0) }
    var heartsVersion by remember { mutableIntStateOf(0) }

    // Determine initial screen based on onboarding / character selection state
    val initialScreen: Screen = remember {
        when {
            !hasSeenOnboarding() -> Screen.Onboarding
            getSelectedCharacter() == null -> Screen.CharacterSelection
            else -> Screen.Home
        }
    }
    var screen by remember { mutableStateOf<Screen>(initialScreen) }

    fun enterGame(op: MathOperation, num: Int) {
        rechargeHearts()
        if (!consumeHeart()) {
            heartsVersion++
            return
        }
        heartsVersion++
        saveLastPlayedStage(op, num)
        recordPlayToday()
        screen = Screen.Game(op, num)
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
            when (val s = screen) {
                is Screen.Onboarding -> OnboardingScreen(
                    appLanguage = appLanguage,
                    onFinish = {
                        markOnboardingSeen()
                        screen = Screen.CharacterSelection
                    }
                )

                is Screen.CharacterSelection -> CharacterSelectionScreen(
                    appLanguage = appLanguage,
                    onCharacterSelected = { char ->
                        setSelectedCharacter(char)
                        screen = Screen.Home
                    }
                )

                is Screen.Home -> HomeScreen(
                    appLanguage = appLanguage,
                    appLanguageRaw = appLanguageRaw,
                    onAppLanguageChange = {
                        appLanguageRaw = it
                        AppSettings.setString("appLanguage", it)
                    },
                    onOperationSelected = { op -> screen = Screen.StageMap(op) },
                    onQuestStart = { _ ->
                        val q = getOrCreateDailyQuest()
                        screen = Screen.Quest(q.operation, q.stageNumber)
                    },
                    onOpenParentSettings = { screen = Screen.ParentSettings },
                    progressVersion = progressVersion,
                    heartsVersion = heartsVersion
                )

                is Screen.ParentSettings -> ParentSettingsScreen(
                    appLanguage = appLanguage,
                    appLanguageRaw = appLanguageRaw,
                    onAppLanguageChange = {
                        appLanguageRaw = it
                        AppSettings.setString("appLanguage", it)
                    },
                    colorSchemeMode = colorSchemeMode,
                    onColorSchemeChange = {
                        colorSchemeMode = it
                        AppSettings.setInt("colorSchemeMode", it)
                    },
                    onResetProgress = {
                        resetAllProgress()
                        progressVersion++
                    },
                    onBack = { screen = Screen.Home }
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
                    onStageSelected = { num -> enterGame(s.operation, num) },
                    onBack = { screen = Screen.Home },
                    progressVersion = progressVersion,
                    heartsVersion = heartsVersion,
                    autoSelectStage = s.autoSelectStage
                )

                is Screen.Game -> GameScreen(
                    operation = s.operation,
                    stageNumber = s.stageNumber,
                    appLanguage = appLanguage,
                    onComplete = { stars, correctCount, avgSec ->
                        saveStageResult(s.operation, s.stageNumber, stars)
                        val points = calcPoints(s.stageNumber, stars)
                        val coins  = calcCoins(s.stageNumber, stars)
                        addPoints(points)
                        addCoins(coins)
                        // Record stats for report
                        recordDailyStats(solvedCount = 10, correctCount = correctCount)
                        // Check badges
                        checkAndAwardBadges()
                        progressVersion++
                        screen = Screen.Result(s.operation, s.stageNumber, stars, avgSec, points, coins)
                    },
                    onBack = { screen = Screen.StageMap(s.operation) }
                )

                is Screen.Quest -> GameScreen(
                    operation = s.operation,
                    stageNumber = s.stageNumber,
                    appLanguage = appLanguage,
                    isQuestMode = true,
                    onComplete = { stars, correctCount, _ ->
                        val wasCompleted = getOrCreateDailyQuest().isCompleted
                        addQuestProgress(correctCount)
                        val justCompleted = !wasCompleted && getOrCreateDailyQuest().isCompleted
                        val coins = calcQuestCoins(s.stageNumber, stars) +
                                    if (justCompleted) QUEST_COMPLETION_BONUS else 0
                        addCoins(coins)
                        recordDailyStats(solvedCount = 10, correctCount = correctCount)
                        progressVersion++
                        screen = Screen.Home
                    },
                    onBack = { screen = Screen.Home }
                )

                is Screen.Result -> {
                    val nextNum = s.stageNumber + 1
                    val hasNextStage = nextNum <= 20 && StageInfo(s.operation, nextNum).isUnlocked
                    val hearts = rememberLiveHearts(heartsVersion)
                    ResultScreen(
                        operation = s.operation,
                        stageNumber = s.stageNumber,
                        stars = s.stars,
                        avgSeconds = s.avgSeconds,
                        points = s.points,
                        coinsEarned = s.coinsEarned,
                        hearts = hearts,
                        appLanguage = appLanguage,
                        hasNextStage = hasNextStage,
                        onRetry = { enterGame(s.operation, s.stageNumber) },
                        onNextStage = {
                            screen = Screen.StageMap(s.operation, if (hasNextStage) nextNum else null)
                        },
                        onToHome = { screen = Screen.Home }
                    )
                }
            }
        }
    }
}
