package com.mathapp.practice.ui

expect fun getDeviceLanguageCode(): String

expect fun currentTimeMillis(): Long

expect fun setHardwareKeyboardHandler(
    onDigit: ((String) -> Unit)? = null,
    onBackspace: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    onMinus: (() -> Unit)? = null
)
