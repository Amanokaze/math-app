package com.mathapp.practice.ui

sealed class AuthState {
    object Guest : AuthState()
    data class LoggedIn(
        val userId: String,
        val email: String,
        val provider: String   // "email" | "google"
    ) : AuthState()
}

fun loadAuthState(): AuthState {
    val userId = AppSettings.getString("auth_user_id", "")
    val email  = AppSettings.getString("auth_email", "")
    val prov   = AppSettings.getString("auth_provider", "")
    return if (userId.isNotEmpty()) AuthState.LoggedIn(userId, email, prov)
    else AuthState.Guest
}

fun saveAuthState(state: AuthState) {
    when (state) {
        is AuthState.Guest -> {
            AppSettings.setString("auth_user_id", "")
            AppSettings.setString("auth_email", "")
            AppSettings.setString("auth_provider", "")
            AppSettings.setString("auth_access_token", "")
            AppSettings.setString("auth_refresh_token", "")
        }
        is AuthState.LoggedIn -> {
            AppSettings.setString("auth_user_id", state.userId)
            AppSettings.setString("auth_email", state.email)
            AppSettings.setString("auth_provider", state.provider)
        }
    }
}

fun getAccessToken(): String  = AppSettings.getString("auth_access_token", "")
fun getRefreshToken(): String = AppSettings.getString("auth_refresh_token", "")

fun saveTokens(accessToken: String, refreshToken: String) {
    AppSettings.setString("auth_access_token", accessToken)
    AppSettings.setString("auth_refresh_token", refreshToken)
}
