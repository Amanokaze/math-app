package com.mathapp.practice.ui

expect fun getDeviceLanguageCode(): String

expect fun currentTimeMillis(): Long

/** Wall-clock seconds since Unix epoch — safe to use across app restarts. */
expect fun getCurrentEpochSeconds(): Long

expect fun setHardwareKeyboardHandler(
    onDigit: ((String) -> Unit)? = null,
    onBackspace: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    onMinus: (() -> Unit)? = null
)

expect fun requiresHiddenTextInputBridge(): Boolean

expect fun openExternalUrl(url: String): Boolean
