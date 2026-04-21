package com.mathapp.practice.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF69B4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0EE),
    onPrimaryContainer = Color(0xFF4A0029),
    secondary = Color(0xFF6B4FB8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECE1FF),
    onSecondaryContainer = Color(0xFF1D0060),
    tertiary = Color(0xFF1B7838),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2F5C2),
    onTertiaryContainer = Color(0xFF002112),
    background = Color(0xFFFFF5F9),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF3EEF8),
    onSurfaceVariant = Color(0xFF4A4458),
    outline = Color(0xFF7B7489),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB3D1),
    onPrimary = Color(0xFF65003B),
    primaryContainer = Color(0xFF8F0055),
    onPrimaryContainer = Color(0xFFFFD9E4),
    secondary = Color(0xFFCCBEFF),
    onSecondary = Color(0xFF350080),
    secondaryContainer = Color(0xFF4E35A0),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = Color(0xFF97D9A8),
    onTertiary = Color(0xFF003918),
    tertiaryContainer = Color(0xFF005227),
    onTertiaryContainer = Color(0xFFB2F5C2),
    background = Color(0xFF1C1820),
    onBackground = Color(0xFFE7E0EC),
    surface = Color(0xFF1C1820),
    onSurface = Color(0xFFE7E0EC),
    surfaceVariant = Color(0xFF4A4458),
    onSurfaceVariant = Color(0xFFCBC4D6),
    outline = Color(0xFF958EA0),
)

@Composable
actual fun MathTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
