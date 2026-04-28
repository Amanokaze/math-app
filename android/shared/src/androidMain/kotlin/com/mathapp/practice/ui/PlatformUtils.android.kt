package com.mathapp.practice.ui

import android.content.Intent
import android.net.Uri
import android.view.KeyEvent

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

actual fun getCurrentEpochSeconds(): Long = System.currentTimeMillis() / 1000

private var hardwareOnDigit: ((String) -> Unit)? = null
private var hardwareOnBackspace: (() -> Unit)? = null
private var hardwareOnSubmit: (() -> Unit)? = null
private var hardwareOnMinus: (() -> Unit)? = null

actual fun setHardwareKeyboardHandler(
    onDigit: ((String) -> Unit)?,
    onBackspace: (() -> Unit)?,
    onSubmit: (() -> Unit)?,
    onMinus: (() -> Unit)?
) {
    hardwareOnDigit = onDigit
    hardwareOnBackspace = onBackspace
    hardwareOnSubmit = onSubmit
    hardwareOnMinus = onMinus
}

actual fun requiresHiddenTextInputBridge(): Boolean = false

actual fun openExternalUrl(url: String): Boolean {
    val context = appContext ?: return false
    return runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    }.getOrDefault(false)
}

fun dispatchHardwareKeyboardEvent(event: KeyEvent): Boolean {
    if (event.action != KeyEvent.ACTION_UP) return false

    return when (event.keyCode) {
        KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> {
            hardwareOnDigit?.invoke("0"); hardwareOnDigit != null
        }
        KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> {
            hardwareOnDigit?.invoke("1"); hardwareOnDigit != null
        }
        KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> {
            hardwareOnDigit?.invoke("2"); hardwareOnDigit != null
        }
        KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> {
            hardwareOnDigit?.invoke("3"); hardwareOnDigit != null
        }
        KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> {
            hardwareOnDigit?.invoke("4"); hardwareOnDigit != null
        }
        KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> {
            hardwareOnDigit?.invoke("5"); hardwareOnDigit != null
        }
        KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_NUMPAD_6 -> {
            hardwareOnDigit?.invoke("6"); hardwareOnDigit != null
        }
        KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> {
            hardwareOnDigit?.invoke("7"); hardwareOnDigit != null
        }
        KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> {
            hardwareOnDigit?.invoke("8"); hardwareOnDigit != null
        }
        KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> {
            hardwareOnDigit?.invoke("9"); hardwareOnDigit != null
        }
        KeyEvent.KEYCODE_DEL -> {
            hardwareOnBackspace?.invoke(); hardwareOnBackspace != null
        }
        KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
            hardwareOnSubmit?.invoke(); hardwareOnSubmit != null
        }
        KeyEvent.KEYCODE_MINUS, KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> {
            hardwareOnMinus?.invoke(); hardwareOnMinus != null
        }
        else -> false
    }
}
