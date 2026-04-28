package com.mathapp.practice.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mathapp.practice.ui.theme.MathTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

// ─── Navigation ───────────────────────────────────────────────────────────────

sealed class Screen {
    object Onboarding : Screen()
    object CharacterSelection : Screen()
    object Home : Screen()
    object Category : Screen()
    object ParentSettings : Screen()
    object Login : Screen()
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
    var showParentalGate by remember { mutableStateOf(false) }
    var authState by remember { mutableStateOf<AuthState>(loadAuthState()) }
    var pendingTransfer by remember { mutableStateOf<AuthState.LoggedIn?>(null) }
    var transferLoading by remember { mutableStateOf(false) }
    var transferError by remember { mutableStateOf("") }
    var remoteSnapshot by remember { mutableStateOf<JsonObject?>(null) }
    var remoteCheckDone by remember { mutableStateOf(false) }
    var remoteCheckFailed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // AccountSection 및 data transfer dialog에서 사용

    // Determine initial screen based on onboarding / character selection state
    val initialScreen: Screen = remember {
        when {
            !hasSeenOnboarding() -> Screen.Onboarding
            getSelectedCharacter() == null -> Screen.CharacterSelection
            else -> Screen.Home
        }
    }
    var screen by remember { mutableStateOf<Screen>(initialScreen) }

    val appLanguage = when {
        appLanguageRaw.isEmpty() -> AppLanguage.fromCode(getDeviceLanguageCode())
        else -> AppLanguage.fromCode(appLanguageRaw)
    }

    // OAuth 딥링크 콜백 폴링 (500ms 간격)
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            val url = OAuthCallbackState.pendingUrl
            if (url != null) {
                OAuthCallbackState.pendingUrl = null
                val result = AuthRepository.handleOAuthCallback(url)
                if (result is AuthResult.Success) {
                    val loggedIn = result.state as AuthState.LoggedIn
                    authState = loggedIn
                    transferError = ""
                    remoteCheckDone = false
                    remoteCheckFailed = false
                    remoteSnapshot = null
                    pendingTransfer = loggedIn
                    screen = Screen.ParentSettings
                    when (val snapshotResult = SyncRepository.fetchRemoteSnapshotResult(loggedIn.userId)) {
                        SyncRepository.RemoteSnapshotResult.Failure -> {
                            remoteCheckFailed = true
                            transferError = L10n.string("account_sync_fail", appLanguage)
                        }
                        is SyncRepository.RemoteSnapshotResult.Success -> {
                            remoteSnapshot = snapshotResult.snapshot
                        }
                    }
                    remoteCheckDone = true
                }
            }
        }
    }

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
                    onOpenParentSettings = { showParentalGate = true },
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
                    authState = authState,
                    onAuthStateChange = { authState = it },
                    onNavigateToLogin = { screen = Screen.Login },
                    onBack = { screen = Screen.Home }
                )

                is Screen.Login -> LoginScreen(
                    appLanguage = appLanguage,
                    onAuthSuccess = { loggedIn ->
                        authState = loggedIn
                        transferError = ""
                        remoteCheckDone = false
                        remoteCheckFailed = false
                        remoteSnapshot = null
                        pendingTransfer = loggedIn
                        screen = Screen.ParentSettings
                        scope.launch {
                            when (val snapshotResult = SyncRepository.fetchRemoteSnapshotResult(loggedIn.userId)) {
                                SyncRepository.RemoteSnapshotResult.Failure -> {
                                    remoteCheckFailed = true
                                    transferError = L10n.string("account_sync_fail", appLanguage)
                                }
                                is SyncRepository.RemoteSnapshotResult.Success -> {
                                    remoteSnapshot = snapshotResult.snapshot
                                }
                            }
                            remoteCheckDone = true
                        }
                    },
                    onBack = { screen = Screen.ParentSettings }
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

            if (showParentalGate) {
                ParentalGateDialog(
                    appLanguage = appLanguage,
                    onPassed = {
                        showParentalGate = false
                        screen = Screen.ParentSettings
                    },
                    onDismiss = { showParentalGate = false }
                )
            }

            // ── 로그인 직후 데이터 이전 확인 다이얼로그 ──────────────────────
            pendingTransfer?.let { loggedIn ->
                val hasRemote = remoteCheckDone && !remoteCheckFailed && remoteSnapshot != null
                val confirmKey = if (hasRemote) "account_transfer_restore_confirm"
                                 else           "account_transfer_backup_confirm"
                val bodyMsg = when {
                    !remoteCheckDone -> L10n.string("account_transfer_checking", appLanguage)
                    remoteCheckFailed -> L10n.string("account_sync_fail", appLanguage)
                    hasRemote        -> L10n.string("account_transfer_restore_msg", appLanguage)
                    else             -> L10n.string("account_transfer_backup_msg", appLanguage)
                } + if (transferError.isNotEmpty() && !remoteCheckFailed) "\n\n$transferError" else ""

                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(L10n.string("account_transfer_title", appLanguage)) },
                    text  = { Text(bodyMsg) },
                    confirmButton = {
                        Button(
                            enabled = remoteCheckDone && !remoteCheckFailed && !transferLoading,
                            onClick = {
                                transferLoading = true
                                transferError = ""
                                scope.launch {
                                    try {
                                        val ok = if (hasRemote) {
                                            // 클라우드 → 기기: 계정 전환 시 기존 로컬 진행도와 섞이지 않도록 교체 적용
                                            SyncRepository.applySnapshot(
                                                remoteSnapshot!!,
                                                SyncRepository.ApplyMode.ReplaceLocal
                                            )
                                            SyncRepository.uploadData(loggedIn.userId)
                                        } else {
                                            // 기기 → 클라우드: 업로드
                                            SyncRepository.uploadData(loggedIn.userId)
                                        }
                                        if (ok) {
                                            progressVersion++
                                            pendingTransfer = null
                                        } else {
                                            transferError = L10n.string("account_sync_fail", appLanguage)
                                        }
                                    } finally {
                                        transferLoading = false
                                    }
                                }
                            }
                        ) {
                            if (transferLoading) CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            else Text(L10n.string(confirmKey, appLanguage))
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            enabled = !transferLoading,
                            onClick = { transferError = ""; pendingTransfer = null }
                        ) {
                            Text(L10n.string("account_transfer_skip", appLanguage))
                        }
                    }
                )
            }
        }
    }
}
