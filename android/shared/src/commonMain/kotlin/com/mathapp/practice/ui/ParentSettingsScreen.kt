package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParentSettingsScreen(
    appLanguage: AppLanguage,
    appLanguageRaw: String,
    onAppLanguageChange: (String) -> Unit,
    colorSchemeMode: Int,
    onColorSchemeChange: (Int) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit
) {
    var studyNotification by remember {
        mutableStateOf(AppSettings.getInt("parentStudyNotify", 1) == 1)
    }
    var achievementNotification by remember {
        mutableStateOf(AppSettings.getInt("parentAchieveNotify", 1) == 1)
    }
    var timeLimit by remember {
        mutableStateOf(AppSettings.getInt("parentTimeLimit", 0) == 1)
    }
    var dailyGoalText by remember {
        mutableStateOf(AppSettings.getInt("parentDailyGoal", 30).toString())
    }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Purple gradient header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(AppColors.GradientPurple))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Text(
                text = "⚙️ ${L10n.string("parent_settings_title", appLanguage)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Language section ──
        SettingsSectionHeader(L10n.string("language", appLanguage))

        Surface(
            onClick = { showLanguageSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌐", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = L10n.string("language", appLanguage),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = AppLanguage.fromCode(appLanguageRaw.ifEmpty { getDeviceLanguageCode() }).displayName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Notifications section ──
        SettingsSectionHeader(L10n.string("parent_notifications", appLanguage))

        SettingsToggleRow(
            label = L10n.string("parent_study_notification", appLanguage),
            icon = "📚",
            checked = studyNotification,
            onCheckedChange = {
                studyNotification = it
                AppSettings.setInt("parentStudyNotify", if (it) 1 else 0)
            }
        )

        SettingsToggleRow(
            label = L10n.string("parent_achievement_notification", appLanguage),
            icon = "🏆",
            checked = achievementNotification,
            onCheckedChange = {
                achievementNotification = it
                AppSettings.setInt("parentAchieveNotify", if (it) 1 else 0)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Study management section ──
        SettingsSectionHeader(L10n.string("parent_study_management", appLanguage))

        SettingsToggleRow(
            label = L10n.string("parent_time_limit", appLanguage),
            icon = "⏱️",
            checked = timeLimit,
            onCheckedChange = {
                timeLimit = it
                AppSettings.setInt("parentTimeLimit", if (it) 1 else 0)
            }
        )

        // Daily goal input
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎯", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = L10n.string("parent_daily_goal", appLanguage),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = dailyGoalText,
                    onValueChange = { v ->
                        if (v.length <= 3 && v.all { it.isDigit() }) {
                            dailyGoalText = v
                            v.toIntOrNull()?.let { AppSettings.setInt("parentDailyGoal", it) }
                        }
                    },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(12.dp))

        // ── Display section (theme) ──
        SettingsSectionHeader(L10n.string("parent_display", appLanguage))

        SettingsToggleRow(
            label = L10n.string("dark", appLanguage),
            icon = "🌙",
            checked = colorSchemeMode == 2,
            onCheckedChange = { onColorSchemeChange(if (it) 2 else 1) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Data section (reset progress) ──
        SettingsSectionHeader(L10n.string("parent_data", appLanguage))

        Surface(
            onClick = { showResetConfirm = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗑️", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = L10n.string("reset_progress", appLanguage),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Account section ──
        SettingsSectionHeader(L10n.string("parent_account", appLanguage))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👋", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = L10n.string("parent_logout", appLanguage),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // ── Reset progress confirm ──
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(L10n.string("reset_progress_confirm_title", appLanguage)) },
            text = { Text(L10n.string("reset_progress_confirm_message", appLanguage)) },
            confirmButton = {
                Button(
                    onClick = { onResetProgress(); showResetConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(L10n.string("reset", appLanguage)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirm = false }) {
                    Text(L10n.string("cancel", appLanguage))
                }
            }
        )
    }

    if (showLanguageSheet) {
        LanguagePickerSheet(
            selectedLanguage = AppLanguage.fromCode(appLanguageRaw.ifEmpty { getDeviceLanguageCode() }),
            onLanguageSelected = { onAppLanguageChange(it.code); showLanguageSheet = false },
            onDismiss = { showLanguageSheet = false },
            appLanguage = appLanguage
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.SecondaryPurple,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    label: String,
    icon: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppColors.SuccessGreen
                )
            )
        }
    }
}
