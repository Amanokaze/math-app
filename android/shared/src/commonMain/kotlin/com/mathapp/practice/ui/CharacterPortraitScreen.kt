package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CharacterPortraitScreen(
    appLanguage: AppLanguage,
    onShopClick: () -> Unit
) {
    var selectedSlot by remember { mutableStateOf(ShopCategory.HEAD) }
    var version by remember { mutableIntStateOf(0) }

    val character  = remember { getSelectedCharacter() }
    val headItem   = remember(version) { getEquippedItem(ShopCategory.HEAD) }
    val badgeItem  = remember(version) { getEquippedItem(ShopCategory.BADGE) }
    val bgItem     = remember(version) { getEquippedItem(ShopCategory.BACKGROUND) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(AppColors.GradientPurple))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨ ${L10n.string("portrait_title", appLanguage)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Large Portrait ────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CharacterPortrait(
                character = character,
                size = 140.dp,
                headItem = headItem,
                badgeItem = badgeItem,
                bgItem = bgItem
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Slot Tabs ─────────────────────────────────────────────────────────
        val slots = listOf(ShopCategory.HEAD, ShopCategory.BADGE, ShopCategory.BACKGROUND)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            slots.forEach { slot ->
                val tabKey = when (slot) {
                    ShopCategory.HEAD       -> "shop_tab_head"
                    ShopCategory.BADGE      -> "shop_tab_badge"
                    ShopCategory.BACKGROUND -> "shop_tab_bg"
                    else                    -> "shop_tab_effect"
                }
                val isSelected = slot == selectedSlot
                Surface(
                    onClick = { SoundPlayer.play(SoundEffect.Tap); selectedSlot = slot },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AppColors.SecondaryPurple
                            else MaterialTheme.colorScheme.surface,
                    shadowElevation = if (isSelected) 0.dp else 1.dp
                ) {
                    Text(
                        text = L10n.string(tabKey, appLanguage),
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Owned Items for Selected Slot ─────────────────────────────────────
        val ownedItems = ALL_SHOP_ITEMS.filter { it.category == selectedSlot && isItemOwned(it.id) }

        if (ownedItems.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🛍️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = L10n.string("portrait_no_items", appLanguage),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { SoundPlayer.play(SoundEffect.Tap); onShopClick() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.SecondaryPurple)
                ) {
                    Text(
                        text = "🛒 ${L10n.string("shop_title", appLanguage)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ownedItems.forEach { item ->
                    val equippedId = remember(version) { getEquippedItemId(item.category) }
                    val isEquipped = equippedId == item.id

                    // Build preview equipped state
                    val previewHead   = if (selectedSlot == ShopCategory.HEAD)       item else headItem
                    val previewBadge  = if (selectedSlot == ShopCategory.BADGE)      item else badgeItem
                    val previewBg     = if (selectedSlot == ShopCategory.BACKGROUND) item else bgItem

                    PortraitItemChip(
                        item = item,
                        character = character,
                        previewHead = previewHead,
                        previewBadge = previewBadge,
                        previewBg = previewBg,
                        isEquipped = isEquipped,
                        appLanguage = appLanguage,
                        onToggle = { toggleEquip(item); version++ }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PortraitItemChip(
    item: ShopItem,
    character: CharacterType?,
    previewHead: ShopItem?,
    previewBadge: ShopItem?,
    previewBg: ShopItem?,
    isEquipped: Boolean,
    appLanguage: AppLanguage,
    onToggle: () -> Unit
) {
    val borderMod = if (isEquipped)
        Modifier.border(2.dp, AppColors.SecondaryPurple, RoundedCornerShape(16.dp))
    else
        Modifier

    Surface(
        onClick = { SoundPlayer.play(SoundEffect.Tap); onToggle() },
        modifier = borderMod.width(96.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isEquipped) AppColors.SecondaryPurple.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CharacterPortrait(
                character = character,
                size = 60.dp,
                headItem = previewHead,
                badgeItem = previewBadge,
                bgItem = previewBg
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = L10n.string(item.nameKey, appLanguage),
                fontSize = 11.sp,
                fontWeight = if (isEquipped) FontWeight.Bold else FontWeight.Normal,
                color = if (isEquipped) AppColors.SecondaryPurple
                        else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            if (isEquipped) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(AppColors.SecondaryPurple, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = L10n.string("shop_equipped", appLanguage),
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
