package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    colorSchemeMode: Int,
    onColorSchemeChange: (Int) -> Unit,
    appLanguage: AppLanguage,
    appLanguageRaw: String,
    onAppLanguageChange: (String) -> Unit,
    bestTime: Float,
    onStartClick: () -> Unit,
    showCountdown: Boolean,
    onShowCountdownChange: (Boolean) -> Unit,
    onGameActiveChange: (Boolean) -> Unit,
    onResetRecords: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (appLanguageRaw.isEmpty()) {
            onAppLanguageChange(AppLanguage.deviceLanguage(java.util.Locale.getDefault()).code)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(L10n.string("language", appLanguage)) },
                            onClick = {
                                showMenu = false
                                showLanguageSheet = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(L10n.string("reset_records", appLanguage), color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showResetConfirm = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Math",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(L10n.string("start", appLanguage), fontSize = 20.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    L10n.string("best_record", appLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (bestTime > 0) L10n.string("time_format", appLanguage, bestTime) else "-",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (bestTime > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                SegmentedButton(
                    selected = colorSchemeMode == 1,
                    onClick = { onColorSchemeChange(1) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(L10n.string("light", appLanguage))
                }
                SegmentedButton(
                    selected = colorSchemeMode == 2,
                    onClick = { onColorSchemeChange(2) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(L10n.string("dark", appLanguage))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showCountdown) {
            CountdownOverlay(
                appLanguage = appLanguage,
                onComplete = {
                    onShowCountdownChange(false)
                    onGameActiveChange(true)
                }
            )
        }

        if (showLanguageSheet) {
            LanguagePickerSheet(
                selectedLanguage = appLanguage,
                onLanguageSelected = {
                    onAppLanguageChange(it.code)
                    showLanguageSheet = false
                },
                onDismiss = { showLanguageSheet = false },
                appLanguage = appLanguage
            )
        }

        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text(L10n.string("reset_confirm_title", appLanguage)) },
                text = { Text(L10n.string("reset_confirm_message", appLanguage)) },
                confirmButton = {
                    Button(
                        onClick = {
                            onResetRecords()
                            showResetConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(L10n.string("reset", appLanguage))
                    }
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

@Composable
fun CountdownOverlay(
    appLanguage: AppLanguage,
    onComplete: () -> Unit
) {
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
                    androidx.compose.material3.TextButton(
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
