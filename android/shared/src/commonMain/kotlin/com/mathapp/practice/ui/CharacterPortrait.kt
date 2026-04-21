package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable character portrait composable.
 *
 * Renders a circular portrait with:
 *  - background gradient (character default colour or equipped BG item)
 *  - character emoji centred inside
 *  - optional head accessory floating above
 *  - optional badge in the bottom-end corner
 *
 * Call sites control which decorations are shown by passing null for unused slots.
 */
@Composable
fun CharacterPortrait(
    character: CharacterType?,
    size: Dp,
    headItem: ShopItem?  = null,
    badgeItem: ShopItem? = null,
    bgItem: ShopItem?    = null,
    modifier: Modifier   = Modifier
) {
    val charEmoji = character?.emoji ?: "🐻"

    val defaultBgColors: List<Color> = when (character) {
        CharacterType.BEAR   -> listOf(AppColors.BearColor.copy(alpha = 0.85f),   AppColors.BearColor.copy(alpha = 0.35f))
        CharacterType.RABBIT -> listOf(AppColors.RabbitColor.copy(alpha = 0.85f), AppColors.RabbitColor.copy(alpha = 0.35f))
        CharacterType.FOX    -> listOf(AppColors.FoxColor.copy(alpha = 0.85f),    AppColors.FoxColor.copy(alpha = 0.35f))
        CharacterType.OWL    -> listOf(AppColors.OwlColor.copy(alpha = 0.85f),    AppColors.OwlColor.copy(alpha = 0.35f))
        null                 -> listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5))
    }
    val bgColors = bgItemColors(bgItem) ?: defaultBgColors

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // ── Background circle ──
        Box(
            modifier = Modifier
                .size(size)
                .background(Brush.radialGradient(bgColors), CircleShape)
        )

        // ── Character emoji ──
        Text(
            text  = charEmoji,
            fontSize = (size.value * 0.54f).sp
        )

        // ── Head accessory: center sits on the circle's top rim ──
        // fontSize 36% so the item is clearly visible at all sizes.
        // offset –12% so the top half floats above the circle and the
        // bottom half overlaps with the character's head, giving a natural
        // "wearing the item" look without obscuring the face.
        if (headItem != null) {
            Text(
                text     = headItem.emoji,
                fontSize = (size.value * 0.36f).sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-(size.value * 0.12f)).dp)
            )
        }

        // ── Badge (bottom-end, white circle backing) ──
        if (badgeItem != null) {
            val badgeSize = (size.value * 0.34f).dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(badgeSize)
                    .background(Color.White.copy(alpha = 0.92f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = badgeItem.emoji,
                    fontSize = (size.value * 0.21f).sp
                )
            }
        }
    }
}
