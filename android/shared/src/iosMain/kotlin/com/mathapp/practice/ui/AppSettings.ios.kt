package com.mathapp.practice.ui

import platform.Foundation.NSUserDefaults

actual object AppSettings {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getInt(key: String, default: Int): Int {
        return if (defaults.objectForKey(key) != null) defaults.integerForKey(key).toInt()
        else default
    }

    actual fun setInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), key)
    }

    actual fun getFloat(key: String, default: Float): Float {
        return if (defaults.objectForKey(key) != null) defaults.floatForKey(key)
        else default
    }

    actual fun setFloat(key: String, value: Float) {
        defaults.setFloat(value, key)
    }

    actual fun getString(key: String, default: String): String {
        return defaults.stringForKey(key) ?: default
    }

    actual fun setString(key: String, value: String) {
        defaults.setObject(value, key)
    }
}
