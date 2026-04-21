package com.mathapp.practice.ui

import androidx.compose.ui.graphics.Color

// ─── Shop Categories & Items ──────────────────────────────────────────────────

enum class ShopCategory { HEAD, BADGE, BACKGROUND, RESULT_EFFECT }

data class ShopItem(
    val id: String,
    val emoji: String,
    val nameKey: String,
    val price: Int,
    val category: ShopCategory
)

val ALL_SHOP_ITEMS = listOf(
    // ── Head accessories ──────────────────────────────────────────────────────
    ShopItem("head_crown",  "👑", "item_crown",  50, ShopCategory.HEAD),
    ShopItem("head_hat",    "🎩", "item_hat",    40, ShopCategory.HEAD),
    ShopItem("head_ribbon", "🎀", "item_ribbon", 35, ShopCategory.HEAD),
    ShopItem("head_flower", "🌸", "item_flower", 30, ShopCategory.HEAD),
    ShopItem("head_star",   "⭐", "item_star",   25, ShopCategory.HEAD),
    // ── Badges ────────────────────────────────────────────────────────────────
    ShopItem("badge_medal",   "🏅", "item_medal",   60, ShopCategory.BADGE),
    ShopItem("badge_diamond", "💎", "item_diamond", 80, ShopCategory.BADGE),
    ShopItem("badge_fire",    "🔥", "item_fire",    45, ShopCategory.BADGE),
    ShopItem("badge_shine",   "🌟", "item_shine",   55, ShopCategory.BADGE),
    // ── Backgrounds ───────────────────────────────────────────────────────────
    ShopItem("bg_rainbow", "🌈", "item_rainbow", 100, ShopCategory.BACKGROUND),
    ShopItem("bg_night",   "🌙", "item_night",    90, ShopCategory.BACKGROUND),
    ShopItem("bg_ocean",   "🌊", "item_ocean",   120, ShopCategory.BACKGROUND),
    ShopItem("bg_spring",  "🌺", "item_spring",   80, ShopCategory.BACKGROUND),
    // ── Result effects ────────────────────────────────────────────────────────
    ShopItem("eff_party",   "🎉", "item_party",   80, ShopCategory.RESULT_EFFECT),
    ShopItem("eff_sparkle", "✨", "item_sparkle", 60, ShopCategory.RESULT_EFFECT),
    ShopItem("eff_balloon", "🎈", "item_balloon", 70, ShopCategory.RESULT_EFFECT),
)

// Background color palettes keyed by item ID
fun bgItemColors(item: ShopItem?): List<Color>? = when (item?.id) {
    "bg_rainbow" -> listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4), Color(0xFFA18CD1))
    "bg_night"   -> listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
    "bg_ocean"   -> listOf(Color(0xFF2193B0), Color(0xFF6DD5ED))
    "bg_spring"  -> listOf(Color(0xFFFF9A9E), Color(0xFFF6D365))
    else -> null
}

// ─── Ownership & Equip ────────────────────────────────────────────────────────

fun isItemOwned(itemId: String): Boolean = AppSettings.getInt("item_$itemId", 0) == 1

fun buyItem(item: ShopItem): Boolean {
    if (isItemOwned(item.id)) return false
    if (!spendCoins(item.price)) return false
    AppSettings.setInt("item_${item.id}", 1)
    return true
}

fun getEquippedItemId(category: ShopCategory): String =
    AppSettings.getString("equipped_${category.name}", "")

fun getEquippedItem(category: ShopCategory): ShopItem? {
    val id = getEquippedItemId(category)
    return if (id.isEmpty()) null else ALL_SHOP_ITEMS.find { it.id == id }
}

fun toggleEquip(item: ShopItem) {
    val key = "equipped_${item.category.name}"
    AppSettings.setString(key,
        if (AppSettings.getString(key, "") == item.id) "" else item.id
    )
}

fun resetShopProgress() {
    ALL_SHOP_ITEMS.forEach { AppSettings.setInt("item_${it.id}", 0) }
    ShopCategory.entries.forEach { AppSettings.setString("equipped_${it.name}", "") }
    AppSettings.setInt("coin_balance", 0)
}
