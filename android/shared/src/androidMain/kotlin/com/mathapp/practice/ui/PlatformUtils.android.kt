package com.mathapp.practice.ui

actual fun getDeviceLanguageCode(): String {
    val locale = java.util.Locale.getDefault()
    val lang = locale.language
    val country = locale.country
    return when {
        lang == "ko" -> "ko"
        lang == "ja" -> "ja"
        lang == "zh" -> if (country == "TW" || country == "HK") "zh-Hant" else "zh-Hans"
        else -> "en"
    }
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun setHardwareKeyboardHandler(
    onDigit: ((String) -> Unit)?,
    onBackspace: (() -> Unit)?,
    onSubmit: (() -> Unit)?,
    onMinus: (() -> Unit)?
) = Unit
