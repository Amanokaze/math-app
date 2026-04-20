package com.mathapp.practice.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Onboarding Screen ────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    appLanguage: AppLanguage,
    onFinish: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding")
    val bearFloat by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bearFloat"
    )
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFFFFE5F0), Color(0xFFE5F0FF), Color(0xFFF0FFE5))))
    ) {
        // Decorations
        Text("🌈", fontSize = 36.sp, modifier = Modifier.offset(x = 20.dp, y = 60.dp))
        Text("☁️", fontSize = 28.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-24).dp, y = 70.dp))
        Text("⭐", fontSize = 20.sp, color = Color.Yellow.copy(alpha = starAlpha),
            modifier = Modifier.offset(x = 40.dp, y = 160.dp))
        Text("✨", fontSize = 16.sp, color = AppColors.WarnGold.copy(alpha = starAlpha),
            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-60).dp, y = 150.dp))
        Text("☁️", fontSize = 22.sp, modifier = Modifier.offset(x = 60.dp, y = 300.dp))

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Title
            Text(
                text = L10n.string("onboarding_title", appLanguage),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.SecondaryPurple,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Bear character with floating animation
            Text(
                text = "🐻",
                fontSize = 120.sp,
                modifier = Modifier.offset(y = bearFloat.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text(
                text = L10n.string("onboarding_desc", appLanguage),
                fontSize = 18.sp,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Start button
            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(AppColors.GradientPink),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = L10n.string("onboarding_start", appLanguage),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ─── Character Selection Screen ───────────────────────────────────────────────

@Composable
fun CharacterSelectionScreen(
    appLanguage: AppLanguage,
    onCharacterSelected: (CharacterType) -> Unit
) {
    var selected by remember { mutableStateOf<CharacterType?>(null) }

    val characters = CharacterType.entries

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFFFFE5F0), Color(0xFFE5F0FF))))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = L10n.string("select_character", appLanguage),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.SecondaryPurple,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "✨✨✨",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2x2 character grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                characters.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { char ->
                            CharacterCard(
                                character = char,
                                isSelected = selected == char,
                                appLanguage = appLanguage,
                                modifier = Modifier.weight(1f)
                            ) { selected = char }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Confirm button
            Button(
                onClick = { selected?.let { onCharacterSelected(it) } },
                enabled = selected != null,
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (selected != null)
                                Brush.linearGradient(AppColors.GradientPurple)
                            else
                                Brush.linearGradient(listOf(Color(0xFFCCCCCC), Color(0xFFAAAAAA))),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = L10n.string("character_confirm", appLanguage),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun CharacterCard(
    character: CharacterType,
    isSelected: Boolean,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )

    val borderColor = if (isSelected) AppColors.PrimaryPink else Color.Transparent

    val cardBrush = when (character) {
        CharacterType.BEAR   -> Brush.linearGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)))
        CharacterType.RABBIT -> Brush.linearGradient(listOf(Color(0xFFFCE4EC), Color(0xFFFFCDD2)))
        CharacterType.FOX    -> Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)))
        CharacterType.OWL    -> Brush.linearGradient(listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9)))
    }

    Box(
        modifier = modifier
            .scale(scale)
            .aspectRatio(1f)
            .border(
                width = 3.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .background(cardBrush, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = character.emoji, fontSize = 64.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = L10n.string(character.nameKey, appLanguage),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) AppColors.PrimaryPink else Color(0xFF333333)
            )
        }
    }
}
