package com.mathapp.practice.ui

import androidx.compose.ui.graphics.Color

// ─── Design System Colors ─────────────────────────────────────────────────────

object AppColors {
    val PrimaryPink      = Color(0xFFFF69B4)
    val SecondaryPurple  = Color(0xFF6B4FB8)
    val SuccessGreen     = Color(0xFF7FE89A)
    val WarnGold         = Color(0xFFFFB800)

    // Gradients (start → end)
    val GradientPink     = listOf(Color(0xFFFF69B4), Color(0xFFFF85C8))
    val GradientPurple   = listOf(Color(0xFF6B4FB8), Color(0xFF8B6FD8))
    val GradientGreen    = listOf(Color(0xFF7FE89A), Color(0xFF9FFFB0))
    val GradientGold     = listOf(Color(0xFFFFB800), Color(0xFFFFD700))
    val GradientTeal     = listOf(Color(0xFF26C6DA), Color(0xFF4FC3F7))
    val GradientTealBlue = listOf(Color(0xFF26C6DA), Color(0xFF2196F3))
    val GradientGreenBlue= listOf(Color(0xFF26A69A), Color(0xFF00BCD4))

    // Character accent colors
    val BearColor        = Color(0xFFFFB74D)   // warm orange
    val RabbitColor      = Color(0xFFEC407A)   // pink
    val FoxColor         = Color(0xFFFF7043)   // deep orange
    val OwlColor         = Color(0xFF7E57C2)   // purple
    val CatColor         = Color(0xFFAB8ED6)   // lavender
    val DogColor         = Color(0xFF64B5F6)   // sky blue

    // UI utility
    val CardWhite        = Color(0xFFFFFFFF)
    val OverlayDark      = Color(0x88000000)
    val OverlayGreenOk   = Color(0x4400CC44)
    val OverlayRedWrong  = Color(0x44FF4444)
}
