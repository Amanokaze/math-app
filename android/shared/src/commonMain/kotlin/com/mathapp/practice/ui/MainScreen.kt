package com.mathapp.practice.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ─── Home Tab ─────────────────────────────────────────────────────────────────

enum class HomeTab { HOME, REPORT, TREASURE, CHARACTER, SHOP }

// ─── Home Screen ──────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    appLanguage: AppLanguage,
    appLanguageRaw: String,
    onAppLanguageChange: (String) -> Unit,
    onOperationSelected: (MathOperation) -> Unit,
    onQuestStart: (MathOperation) -> Unit,
    onOpenParentSettings: () -> Unit,
    progressVersion: Int,
    heartsVersion: Int
) {
    var activeTab by remember { mutableStateOf(HomeTab.HOME) }

    Scaffold(
        bottomBar = {
            HomeBottomBar(
                activeTab = activeTab,
                appLanguage = appLanguage,
                onTabSelected = { activeTab = it }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (activeTab) {
                HomeTab.HOME -> HomeContent(
                    appLanguage = appLanguage,
                    appLanguageRaw = appLanguageRaw,
                    onAppLanguageChange = onAppLanguageChange,
                    onOperationSelected = onOperationSelected,
                    onQuestStart = onQuestStart,
                    onOpenParentSettings = onOpenParentSettings,
                    progressVersion = progressVersion,
                    heartsVersion = heartsVersion
                )
                HomeTab.REPORT -> LearningReportScreen(
                    appLanguage = appLanguage,
                    onBack = { activeTab = HomeTab.HOME }
                )
                HomeTab.TREASURE -> TreasureChestScreen(
                    appLanguage = appLanguage,
                    onBack = { activeTab = HomeTab.HOME }
                )
                HomeTab.CHARACTER -> CharacterPortraitScreen(
                    appLanguage = appLanguage,
                    onShopClick = { activeTab = HomeTab.SHOP }
                )
                HomeTab.SHOP -> ShopScreen(appLanguage = appLanguage)
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    activeTab: HomeTab,
    appLanguage: AppLanguage,
    onTabSelected: (HomeTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = activeTab == HomeTab.HOME,
            onClick = { onTabSelected(HomeTab.HOME) },
            icon = { Text("🏠", fontSize = 22.sp) },
            label = { Text(L10n.string("nav_home", appLanguage), fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = activeTab == HomeTab.REPORT,
            onClick = { onTabSelected(HomeTab.REPORT) },
            icon = { Text("📊", fontSize = 22.sp) },
            label = { Text(L10n.string("nav_report", appLanguage), fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = activeTab == HomeTab.TREASURE,
            onClick = { onTabSelected(HomeTab.TREASURE) },
            icon = { Text("🎁", fontSize = 22.sp) },
            label = { Text(L10n.string("nav_treasure", appLanguage), fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = activeTab == HomeTab.CHARACTER,
            onClick = { onTabSelected(HomeTab.CHARACTER) },
            icon = { Text("✨", fontSize = 22.sp) },
            label = { Text(L10n.string("nav_character", appLanguage), fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = activeTab == HomeTab.SHOP,
            onClick = { onTabSelected(HomeTab.SHOP) },
            icon = { Text("🛒", fontSize = 22.sp) },
            label = { Text(L10n.string("shop_title", appLanguage), fontSize = 11.sp) }
        )
    }
}

@Composable
private fun HomeContent(
    appLanguage: AppLanguage,
    appLanguageRaw: String,
    onAppLanguageChange: (String) -> Unit,
    onQuestStart: (MathOperation) -> Unit,
    onOperationSelected: (MathOperation) -> Unit,
    onOpenParentSettings: () -> Unit,
    progressVersion: Int,
    heartsVersion: Int
) {
    val hearts = rememberLiveHearts(heartsVersion)
    val totalPoints = remember(progressVersion) { getTotalPoints() }
    val coinBalance = remember(progressVersion) { getCoinBalance() }
    val character = remember { getSelectedCharacter() }
    val dailyQuest = remember(progressVersion) { getOrCreateDailyQuest() }
    val equippedHead  = remember(progressVersion) { getEquippedItem(ShopCategory.HEAD) }
    val equippedBadge = remember(progressVersion) { getEquippedItem(ShopCategory.BADGE) }
    val equippedBg    = remember(progressVersion) { getEquippedItem(ShopCategory.BACKGROUND) }

    LaunchedEffect(Unit) {
        if (appLanguageRaw.isEmpty()) onAppLanguageChange(getDeviceLanguageCode())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings button → Settings
                IconButton(onClick = onOpenParentSettings) {
                    Icon(Icons.Default.Settings, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Hearts + Points + Coins (right side)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Points badge
                    if (totalPoints > 0) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(AppColors.GradientGold),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "⭐ $totalPoints",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    // Coin balance badge
                    if (coinBalance > 0) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(AppColors.GradientTeal),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🪙 $coinBalance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    // Heart badge
                    Box(
                        modifier = Modifier
                            .background(AppColors.PrimaryPink, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "❤️ $hearts",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Space before quest card: reduced when quest is completed (character moves into card)
            Spacer(modifier = Modifier.height(if (dailyQuest.isCompleted) 8.dp else 72.dp))

            // ── Daily Quest Card ──
            DailyQuestCard(
                quest = dailyQuest,
                appLanguage = appLanguage,
                onContinue = { onQuestStart(dailyQuest.operation) },
                characterEmoji = character?.emoji ?: "🐻",
                character = character,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Operation Grid ──
            Text(
                text = L10n.string("select_operation", appLanguage),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OperationGrid(
                appLanguage = appLanguage,
                progressVersion = progressVersion,
                onOperationSelected = onOperationSelected
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Character portrait overlay (top-right, non-scrolling): only when quest is not completed ──
        if (!dailyQuest.isCompleted) {
            CharacterPortrait(
                character = character,
                size = 88.dp,
                headItem = equippedHead,
                badgeItem = equippedBadge,
                bgItem = equippedBg,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 52.dp, end = 12.dp)
            )
        }
    }
}

// ─── Daily Quest Card ─────────────────────────────────────────────────────────

@Composable
fun DailyQuestCard(
    quest: DailyQuest,
    appLanguage: AppLanguage,
    onContinue: () -> Unit,
    characterEmoji: String = "🐻",
    character: CharacterType? = null,
    modifier: Modifier = Modifier
) {
    val questGradient = if (quest.isCompleted)
        listOf(Color(0xFF43A047), Color(0xFF66BB6A))
    else
        AppColors.GradientTealBlue

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(questGradient),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        if (quest.isCompleted) {
            // ── Completed state: character inside the card ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Character portrait in card
                val headItem  = remember { getEquippedItem(ShopCategory.HEAD) }
                val badgeItem = remember { getEquippedItem(ShopCategory.BADGE) }
                val bgItem    = remember { getEquippedItem(ShopCategory.BACKGROUND) }
                CharacterPortrait(
                    character = character,
                    size = 72.dp,
                    headItem = headItem,
                    badgeItem = badgeItem,
                    bgItem = bgItem
                )
                Column {
                    Text(
                        text = L10n.string("quest_completed_title", appLanguage),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            Text("⭐", fontSize = 24.sp)
                        }
                    }
                }
            }
        } else {
            // ── In progress state ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎯", fontSize = 36.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = L10n.string("quest_title", appLanguage),
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    val descKey = when (quest.questType) {
                        QuestType.PROGRESS -> "quest_desc_progress"
                        QuestType.REVIEW   -> "quest_desc_review"
                        QuestType.NEW      -> "quest_desc_new"
                    }
                    Text(
                        text = "${L10n.string(descKey, appLanguage, opName(quest.operation, appLanguage), quest.stageNumber)}  " +
                            L10n.string("quest_problems", appLanguage, quest.targetCount),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(quest.progressFraction)
                                .fillMaxHeight()
                                .background(Color.White, RoundedCornerShape(3.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${quest.progress}/${quest.targetCount}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Continue button
                Button(
                    onClick = onContinue,
                    modifier = Modifier.height(68.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.25f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = L10n.string("quest_continue", appLanguage),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun rememberLiveHearts(refreshKey: Int): Int {
    var hearts by remember(refreshKey) {
        mutableIntStateOf(run {
            rechargeHearts()
            getHearts()
        })
    }

    LaunchedEffect(refreshKey) {
        while (true) {
            rechargeHearts()
            hearts = getHearts()
            delay(1000)
        }
    }

    return hearts
}

// ─── Hearts Header ────────────────────────────────────────────────────────────

@Composable
fun HeartsHeader(hearts: Int, appLanguage: AppLanguage, totalPoints: Int? = null) {
    var secondsLeft by remember { mutableLongStateOf(secondsUntilNextHeart()) }

    LaunchedEffect(hearts) {
        while (true) {
            secondsLeft = secondsUntilNextHeart()
            delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (totalPoints != null) Arrangement.SpaceBetween else Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                L10n.string("hearts_display", appLanguage, hearts, MAX_HEARTS),
                style = MaterialTheme.typography.titleMedium
            )
            if (hearts < MAX_HEARTS) {
                Spacer(modifier = Modifier.width(6.dp))
                val m = secondsLeft / 60
                val s = secondsLeft % 60
                Text(
                    L10n.string("next_heart_timer", appLanguage, m, s),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (totalPoints != null && totalPoints > 0) {
            Text(
                L10n.string("total_points_format", appLanguage, totalPoints),
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.WarnGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── Operation Grid ───────────────────────────────────────────────────────────

@Composable
private fun OperationGrid(
    appLanguage: AppLanguage,
    progressVersion: Int,
    onOperationSelected: (MathOperation) -> Unit
) {
    val operationInfo = listOf(
        Triple(MathOperation.ADDITION,       "➕", AppColors.GradientPink),
        Triple(MathOperation.SUBTRACTION,    "➖", AppColors.GradientTeal),
        Triple(MathOperation.MULTIPLICATION, "✖️",  AppColors.GradientPurple),
        Triple(MathOperation.DIVISION,       "➗", AppColors.GradientGold)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        operationInfo.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { (op, icon, gradient) ->
                    val progress = remember(progressVersion) { operationProgress(op) }
                    val totalStars = remember(progressVersion) { stagesFor(op).sumOf { it.stars } }
                    OperationTile(
                        icon = icon,
                        name = opName(op, appLanguage),
                        totalStars = totalStars,
                        progress = progress,
                        gradient = gradient,
                        onClick = { onOperationSelected(op) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OperationTile(
    icon: String,
    name: String,
    totalStars: Int,
    progress: Float,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Brush.linearGradient(gradient), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "⭐ $totalStars",
                fontSize = 12.sp,
                color = AppColors.WarnGold,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(3.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(
                            Brush.linearGradient(gradient),
                            RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}

// ─── CountdownOverlay (shared, still used by GameScreen) ─────────────────────

@Composable
fun CountdownOverlay(appLanguage: AppLanguage, onComplete: () -> Unit) {
    var count by remember { mutableIntStateOf(3) }
    var scale by remember { mutableStateOf(0.3f) }
    var alpha by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        for (i in 3 downTo 0) {
            count = i
            scale = 0.3f
            alpha = 0f
            kotlinx.coroutines.delay(50)
            scale = 1.2f
            alpha = 1f
            kotlinx.coroutines.delay(400)
            scale = 1f
            kotlinx.coroutines.delay(550)
            if (i == 0) {
                kotlinx.coroutines.delay(600)
                onComplete()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 0) count.toString() else L10n.string("countdown_go", appLanguage),
            style = MaterialTheme.typography.displayLarge,
            fontSize = 72.sp,
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        )
    }
}

// ─── Language picker ──────────────────────────────────────────────────────────

@Composable
fun LanguagePickerSheet(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
    appLanguage: AppLanguage
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L10n.string("language", appLanguage)) },
        text = {
            Column {
                AppLanguage.entries.forEach { lang ->
                    TextButton(
                        onClick = { onLanguageSelected(lang) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lang.displayName)
                            if (lang == selectedLanguage) {
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(L10n.string("cancel", appLanguage))
            }
        }
    )
}
