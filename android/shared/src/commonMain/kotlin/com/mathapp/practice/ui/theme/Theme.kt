package com.mathapp.practice.ui.theme

import androidx.compose.runtime.Composable

@Composable
expect fun MathTheme(darkTheme: Boolean, content: @Composable () -> Unit)
