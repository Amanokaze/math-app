package com.mathapp.practice.ui

import platform.Foundation.NSUserDefaults
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.posix.time
import kotlin.time.TimeSource

private val originMark = TimeSource.Monotonic.markNow()
private const val hardwareKeyNotificationName = "MathHardwareKeyInput"
private const val hardwareCaptureEnabledKey = "MathHardwareKeyboardCaptureEnabled"
private const val externalUrlNotificationName = "MathOpenExternalUrl"
private const val pendingExternalUrlKey = "MathPendingExternalUrl"

private var hardwareKeyObserver: Any? = null
private var hardwareOnDigit: ((String) -> Unit)? = null
private var hardwareOnBackspace: (() -> Unit)? = null
private var hardwareOnSubmit: (() -> Unit)? = null
private var hardwareOnMinus: (() -> Unit)? = null

actual fun getDeviceLanguageCode(): String {
    @Suppress("UNCHECKED_CAST")
    val languages = NSUserDefaults.standardUserDefaults
        .arrayForKey("AppleLanguages") as? List<*>
    val lang = languages?.firstOrNull() as? String ?: return "en"
    return when {
        lang.startsWith("ko") -> "ko"
        lang.startsWith("ja") -> "ja"
        lang.startsWith("zh-Hant") ||
        lang.startsWith("zh_TW") ||
        lang.startsWith("zh_HK") -> "zh-Hant"
        lang.startsWith("zh") -> "zh-Hans"
        else -> "en"
    }
}

actual fun currentTimeMillis(): Long = originMark.elapsedNow().inWholeMilliseconds

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun getCurrentEpochSeconds(): Long = time(null).toLong()

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

    if (onDigit == null && onBackspace == null && onSubmit == null && onMinus == null) {
        NSUserDefaults.standardUserDefaults.setInteger(0L, hardwareCaptureEnabledKey)
        hardwareKeyObserver?.let { NSNotificationCenter.defaultCenter.removeObserver(it) }
        hardwareKeyObserver = null
        return
    }

    NSUserDefaults.standardUserDefaults.setInteger(1L, hardwareCaptureEnabledKey)

    if (hardwareKeyObserver == null) {
        hardwareKeyObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = hardwareKeyNotificationName,
            `object` = null,
            queue = null
        ) { notification: NSNotification? ->
            val key = notification?.userInfo?.get("key") as? String ?: return@addObserverForName
            when (key) {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> hardwareOnDigit?.invoke(key)
                "backspace" -> hardwareOnBackspace?.invoke()
                "submit" -> hardwareOnSubmit?.invoke()
                "minus" -> hardwareOnMinus?.invoke()
            }
        }
    }
}

actual fun requiresHiddenTextInputBridge(): Boolean = true

actual fun openExternalUrl(url: String): Boolean {
    if (!url.startsWith("http://") && !url.startsWith("https://")) return false
    NSUserDefaults.standardUserDefaults.setObject(url, pendingExternalUrlKey)
    NSNotificationCenter.defaultCenter.postNotificationName(
        aName = externalUrlNotificationName,
        `object` = null
    )
    return true
}
