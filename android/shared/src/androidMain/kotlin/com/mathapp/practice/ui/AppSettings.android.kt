package com.mathapp.practice.ui

import android.content.Context
import android.content.SharedPreferences

private var sharedPrefs: SharedPreferences? = null

fun initAppSettings(context: Context) {
    sharedPrefs = context.getSharedPreferences("math_prefs", Context.MODE_PRIVATE)
}

actual object AppSettings {
    private val prefs get() = checkNotNull(sharedPrefs) {
        "AppSettings not initialized. Call initAppSettings(context) in MainActivity.onCreate()."
    }

    actual fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)
    actual fun setInt(key: String, value: Int) { prefs.edit().putInt(key, value).apply() }
    actual fun getFloat(key: String, default: Float): Float = prefs.getFloat(key, default)
    actual fun setFloat(key: String, value: Float) { prefs.edit().putFloat(key, value).apply() }
    actual fun getString(key: String, default: String): String = prefs.getString(key, default) ?: default
    actual fun setString(key: String, value: String) { prefs.edit().putString(key, value).apply() }
}
