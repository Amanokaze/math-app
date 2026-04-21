package com.mathapp.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun ShopScreen(appLanguage: AppLanguage) {
    var selectedCategory by remember { mutableStateOf(ShopCategory.ACCESSORY) }
    var shopVersion by remember { mutableIntStateOf(0) }
    val coinBalance = remember(shopVersion) { getCoinBalance() }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ── Header ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(AppColors.GradientPurple))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "🛒 ${L10n.string("shop_title", appLanguage)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = L10n.string("shop_coins", appLanguage, coinBalance),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // ── Category Tabs ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShopCategory.entries.forEach { cat ->
                val tabKey = when (cat) {
                    ShopCategory.ACCESSORY    -> "shop_tab_accessory"
                    ShopCategory.RESULT_EFFECT -> "shop_tab_effect"
                    ShopCategory.HOME_DECO    -> "shop_tab_deco"
                }
                val isSelected = cat == selectedCategory
                Surface(
                    onClick = { selectedCategory = cat },
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

        // ── Item Grid ─────────────────────────────────────────────────────────
        val items = ALL_SHOP_ITEMS.filter { it.category == selectedCategory }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        ShopItemCard(
                            item = item,
                            appLanguage = appLanguage,
                            shopVersion = shopVersion,
                            coinBalance = coinBalance,
                            onAction = { shopVersion++ },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

// ─── Item Card ────────────────────────────────────────────────────────────────

@Composable
private fun ShopItemCard(
    item: ShopItem,
    appLanguage: AppLanguage,
    shopVersion: Int,
    coinBalance: Int,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val owned    = remember(shopVersion) { isItemOwned(item.id) }
    val equipped = remember(shopVersion) { getEquippedItemId(item.category) == item.id }

    val cardModifier = if (equipped)
        modifier.border(2.dp, AppColors.SecondaryPurple, RoundedCornerShape(16.dp))
    else
        modifier

    Surface(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        color = if (equipped) AppColors.SecondaryPurple.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = item.emoji, fontSize = 44.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = L10n.string(item.nameKey, appLanguage),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            if (!owned) {
                Text(
                    text = "🪙 ${item.price}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "✓ ${L10n.string("shop_owned", appLanguage)}",
                    fontSize = 13.sp,
                    color = AppColors.SuccessGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            when {
                !owned -> Button(
                    onClick = { buyItem(item); onAction() },
                    enabled = coinBalance >= item.price,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (coinBalance >= item.price) L10n.string("shop_buy", appLanguage)
                               else L10n.string("shop_not_enough", appLanguage),
                        fontSize = 12.sp
                    )
                }
                equipped -> OutlinedButton(
                    onClick = { toggleEquip(item); onAction() },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = L10n.string("shop_equipped", appLanguage),
                        fontSize = 12.sp,
                        color = AppColors.SecondaryPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
                else -> Button(
                    onClick = { toggleEquip(item); onAction() },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.SuccessGreen),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = L10n.string("shop_equip", appLanguage),
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
