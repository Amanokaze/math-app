package com.mathapp.practice.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Home Screen ──────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    appLanguage: AppLanguage,
    colorSchemeMode: Int,
    onColorSchemeChange: (Int) -> Unit,
    appLanguageRaw: String,
    onAppLanguageChange: (String) -> Unit,
    onResumeClick: (Pair<MathOperation, Int>) -> Unit,
    onNewStartClick: () -> Unit,
    onResetProgress: () -> Unit,
    progressVersion: Int
) {
    val lastStage = remember(progressVersion) { getLastPlayedStage() }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (appLanguageRaw.isEmpty()) onAppLanguageChange(getDeviceLanguageCode())
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Top bar ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp))

                Text(
                    text = "Math",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 40.sp
                )

                Box {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        // Theme toggle
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (colorSchemeMode == 1)
                                        L10n.string("dark", appLanguage)
                                    else
                                        L10n.string("light", appLanguage)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onColorSchemeChange(if (colorSchemeMode == 1) 2 else 1)
                            }
                        )
                        // Language
                        DropdownMenuItem(
                            text = { Text(L10n.string("language", appLanguage)) },
                            onClick = { showMenu = false; showLanguageSheet = true }
                        )
                        // Reset progress
                        DropdownMenuItem(
                            text = {
                                Text(
                                    L10n.string("reset_progress", appLanguage),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = { showMenu = false; showResetConfirm = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Overall progress ──
            val overallPct = remember(progressVersion) { (overallProgress() * 100).toInt() }
            if (overallPct > 0) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        L10n.string("overall_progress", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { overallProgress() },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(6.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "$overallPct%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Resume button ──
            if (lastStage != null) {
                val (op, num) = lastStage
                Button(
                    onClick = { onResumeClick(lastStage) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            L10n.string("resume", appLanguage),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            L10n.string("resume_hint", appLanguage, opName(op, appLanguage), num),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── New Start button ──
            OutlinedButton(
                onClick = onNewStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    L10n.string("new_start", appLanguage),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // ── Language picker ──
        if (showLanguageSheet) {
            LanguagePickerSheet(
                selectedLanguage = AppLanguage.fromCode(
                    appLanguageRaw.ifEmpty { getDeviceLanguageCode() }
                ),
                onLanguageSelected = {
                    onAppLanguageChange(it.code)
                    showLanguageSheet = false
                },
                onDismiss = { showLanguageSheet = false },
                appLanguage = appLanguage
            )
        }

        // ── Reset confirm ──
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
