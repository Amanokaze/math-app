package com.mathapp.practice.ui

// ─── Shop Categories & Items ──────────────────────────────────────────────────

enum class ShopCategory { ACCESSORY, RESULT_EFFECT, HOME_DECO }

data class ShopItem(
    val id: String,
    val emoji: String,
    val nameKey: String,
    val price: Int,
    val category: ShopCategory
)

val ALL_SHOP_ITEMS = listOf(
    // ── Accessories (shown on character) ─────────────────────────────────────
    ShopItem("acc_crown",   "👑", "item_crown",   50,  ShopCategory.ACCESSORY),
    ShopItem("acc_hat",     "🎩", "item_hat",     40,  ShopCategory.ACCESSORY),
    ShopItem("acc_glasses", "🕶️", "item_glasses", 30,  ShopCategory.ACCESSORY),
    ShopItem("acc_ribbon",  "🎀", "item_ribbon",  35,  ShopCategory.ACCESSORY),
    ShopItem("acc_star",    "⭐", "item_star",    25,  ShopCategory.ACCESSORY),
    // ── Result effects (shown on result screen) ───────────────────────────────
    ShopItem("eff_party",   "🎉", "item_party",   80,  ShopCategory.RESULT_EFFECT),
    ShopItem("eff_sparkle", "✨", "item_sparkle", 60,  ShopCategory.RESULT_EFFECT),
    ShopItem("eff_balloon", "🎈", "item_balloon", 70,  ShopCategory.RESULT_EFFECT),
    // ── Home decorations (shown on home screen) ───────────────────────────────
    ShopItem("deco_rainbow","🌈", "item_rainbow", 100, ShopCategory.HOME_DECO),
    ShopItem("deco_flowers","🌸", "item_flowers", 90,  ShopCategory.HOME_DECO),
    ShopItem("deco_castle", "🏰", "item_castle",  120, ShopCategory.HOME_DECO)
)

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
