package com.mathapp.practice.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import math.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
fun characterDrawable(character: CharacterType): DrawableResource = when (character) {
    CharacterType.BEAR   -> Res.drawable.char_bear
    CharacterType.RABBIT -> Res.drawable.char_rabbit
    CharacterType.FOX    -> Res.drawable.char_fox
    CharacterType.OWL    -> Res.drawable.char_owl
    CharacterType.CAT    -> Res.drawable.char_cat
    CharacterType.DOG    -> Res.drawable.char_dog
}

fun characterBgColors(character: CharacterType?): List<Color> = when (character) {
    CharacterType.BEAR   -> listOf(AppColors.BearColor.copy(alpha = 0.85f),   AppColors.BearColor.copy(alpha = 0.35f))
    CharacterType.RABBIT -> listOf(AppColors.RabbitColor.copy(alpha = 0.85f), AppColors.RabbitColor.copy(alpha = 0.35f))
    CharacterType.FOX    -> listOf(AppColors.FoxColor.copy(alpha = 0.85f),    AppColors.FoxColor.copy(alpha = 0.35f))
    CharacterType.OWL    -> listOf(AppColors.OwlColor.copy(alpha = 0.85f),    AppColors.OwlColor.copy(alpha = 0.35f))
    CharacterType.CAT    -> listOf(AppColors.CatColor.copy(alpha = 0.85f),    AppColors.CatColor.copy(alpha = 0.35f))
    CharacterType.DOG    -> listOf(AppColors.DogColor.copy(alpha = 0.85f),    AppColors.DogColor.copy(alpha = 0.35f))
    null                 -> listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5))
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CharacterPortrait(
    character: CharacterType?,
    size: Dp,
    headItem: ShopItem?  = null,
    badgeItem: ShopItem? = null,
    bgItem: ShopItem?    = null,
    modifier: Modifier   = Modifier
) {
    val bgColors = bgItemColors(bgItem) ?: characterBgColors(character)

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

        // ── Character image ──
        Image(
            painter = painterResource(characterDrawable(character ?: CharacterType.BEAR)),
            contentDescription = null,
            modifier = Modifier.size(size * 0.72f),
            contentScale = ContentScale.Fit
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
