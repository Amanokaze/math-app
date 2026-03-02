package com.mathapp.practice.ui

expect object AppSettings {
    fun getInt(key: String, default: Int): Int
    fun setInt(key: String, value: Int)
    fun getFloat(key: String, default: Float): Float
    fun setFloat(key: String, value: Float)
    fun getString(key: String, default: String): String
    fun setString(key: String, value: String)
}
