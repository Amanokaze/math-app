package com.mathapp.practice.ui

import android.content.Context
import android.content.SharedPreferences

private var sharedPrefs: SharedPreferences? = null
internal var appContext: Context? = null
    private set

fun initAppSettings(context: Context) {
    appContext = context.applicationContext
    sharedPrefs = appContext?.getSharedPreferences("math_prefs", Context.MODE_PRIVATE)
}

actual object AppSettings {
    private val prefs get() = checkNotNull(sharedPrefs) {
        "AppSettings not initialized. Call initAppSettings(context) in MainActivity.onCreate()."
    }

    actual fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)
    actual fun setInt(key: String, value: Int) { prefs.edit().putInt(key, value).commit() }
    actual fun getFloat(key: String, default: Float): Float = prefs.getFloat(key, default)
    actual fun setFloat(key: String, value: Float) { prefs.edit().putFloat(key, value).commit() }
    actual fun getString(key: String, default: String): String = prefs.getString(key, default) ?: default
    actual fun setString(key: String, value: String) { prefs.edit().putString(key, value).commit() }
}
