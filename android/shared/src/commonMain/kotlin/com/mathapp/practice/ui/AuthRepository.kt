package com.mathapp.practice.ui

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
private data class AuthResponse(
    val access_token: String = "",
    val refresh_token: String = "",
    val user: AuthUser? = null
)

@Serializable
private data class AuthUser(
    val id: String = "",
    val email: String? = null
)

sealed class AuthResult {
    data class Success(val state: AuthState) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

object AuthRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
    }

    // ── 이메일/비밀번호 가입 ────────────────────────────────────────────────
    suspend fun signUp(email: String, password: String): AuthResult {
        return runCatching {
            val bodyText = client.post("$SUPABASE_URL/auth/v1/signup") {
                header("apikey", SUPABASE_ANON_KEY)
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("email", email)
                    put("password", password)
                }.toString())
            }.bodyAsText()
            val bodyJson = json.parseToJsonElement(bodyText).jsonObject
            val accessToken = bodyJson["access_token"]?.jsonPrimitive?.contentOrNull ?: ""
            if (accessToken.isNotEmpty()) {
                // 이메일 확인 불필요 설정 → 즉시 로그인
                applyAuthResponse(json.decodeFromString(bodyText), "email")
            } else if (bodyJson["id"]?.jsonPrimitive?.contentOrNull != null) {
                // 이메일 확인 필요 설정 → 가입은 성공, 메일 확인 안내
                AuthResult.Error("확인 이메일을 발송했습니다.\n이메일 인증 후 로그인해주세요.")
            } else {
                val msg = bodyJson["message"]?.jsonPrimitive?.contentOrNull ?: "가입에 실패했습니다."
                AuthResult.Error(msg)
            }
        }.getOrElse { AuthResult.Error(it.message ?: "알 수 없는 오류") }
    }

    // ── 이메일/비밀번호 로그인 ──────────────────────────────────────────────
    suspend fun signIn(email: String, password: String): AuthResult {
        return runCatching {
            val resp: AuthResponse =
                client.post("$SUPABASE_URL/auth/v1/token?grant_type=password") {
                    header("apikey", SUPABASE_ANON_KEY)
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject {
                        put("email", email)
                        put("password", password)
                    }.toString())
                }.body()
            applyAuthResponse(resp, "email")
        }.getOrElse { AuthResult.Error(it.message ?: "알 수 없는 오류") }
    }

    // ── Google OAuth: 브라우저에서 열 URL 반환 ─────────────────────────────
    fun googleOAuthUrl(): String {
        val encoded = OAUTH_REDIRECT_URI.encodeURLParameter()
        return "$SUPABASE_URL/auth/v1/authorize?provider=google&redirect_to=$encoded&prompt=select_account"
    }

    // ── OAuth 딥링크 콜백 처리 (URL fragment 에서 토큰 파싱) ───────────────
    suspend fun handleOAuthCallback(url: String): AuthResult {
        val fragment = url.substringAfter("#", "")
        val qp = url.substringAfter("?", "").substringBefore("#")
        val raw = if (fragment.isNotEmpty()) fragment else qp
        val params = raw.split("&").mapNotNull {
            val eq = it.indexOf('=')
            if (eq < 0) null else it.substring(0, eq) to it.substring(eq + 1)
        }.toMap()

        val accessToken  = params["access_token"]  ?: ""
        val refreshToken = params["refresh_token"] ?: ""
        if (accessToken.isEmpty()) return AuthResult.Error("Google 로그인 실패")

        return runCatching {
            val userText = client.get("$SUPABASE_URL/auth/v1/user") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $accessToken")
            }.bodyAsText()
            val userJson = json.parseToJsonElement(userText).jsonObject
            val userId = userJson["id"]?.jsonPrimitive?.content ?: ""
            val email  = userJson["email"]?.jsonPrimitive?.content ?: ""

            saveTokens(accessToken, refreshToken)
            val state = AuthState.LoggedIn(userId, email, "google")
            saveAuthState(state)
            AuthResult.Success(state)
        }.getOrElse { AuthResult.Error(it.message ?: "알 수 없는 오류") }
    }

    // ── 로그아웃 ────────────────────────────────────────────────────────────
    suspend fun signOut() {
        val token = getAccessToken()
        if (token.isNotEmpty()) {
            runCatching {
                client.post("$SUPABASE_URL/auth/v1/logout") {
                    header("apikey", SUPABASE_ANON_KEY)
                    header("Authorization", "Bearer $token")
                }
            }
        }
        saveAuthState(AuthState.Guest)
    }

    // ── 토큰 갱신 ───────────────────────────────────────────────────────────
    suspend fun refreshSession(): Boolean {
        val rt = getRefreshToken()
        if (rt.isEmpty()) return false
        return runCatching {
            val resp: AuthResponse =
                client.post("$SUPABASE_URL/auth/v1/token?grant_type=refresh_token") {
                    header("apikey", SUPABASE_ANON_KEY)
                    contentType(ContentType.Application.Json)
                    setBody(buildJsonObject { put("refresh_token", rt) }.toString())
                }.body()
            if (resp.access_token.isNotEmpty()) {
                saveTokens(resp.access_token, resp.refresh_token)
                true
            } else false
        }.getOrElse { false }
    }

    // ── 비밀번호 재설정 이메일 발송 ─────────────────────────────────────────
    suspend fun resetPassword(email: String): AuthResult {
        return runCatching {
            client.post("$SUPABASE_URL/auth/v1/recover") {
                header("apikey", SUPABASE_ANON_KEY)
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject { put("email", email) }.toString())
            }
            AuthResult.Success(AuthState.Guest)
        }.getOrElse { AuthResult.Error(it.message ?: "오류 발생") }
    }

    // ── 계정 완전 삭제 (Edge Function 경유, Admin API 사용) ──────────────────
    suspend fun requestAccountDeletion(): AuthResult {
        if (getRefreshToken().isNotEmpty()) {
            refreshSession()
        }
        val token = getAccessToken()
        if (token.isEmpty()) return AuthResult.Error("로그인 상태가 아닙니다.")
        return runCatching {
            val bodyText = client.post("$SUPABASE_URL/functions/v1/delete-account") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
            }.bodyAsText()
            val bodyJson = json.parseToJsonElement(bodyText).jsonObject
            if (bodyJson["success"]?.jsonPrimitive?.booleanOrNull == true) {
                signOut()
                AuthResult.Success(AuthState.Guest)
            } else {
                val msg = bodyJson["error"]?.jsonPrimitive?.contentOrNull ?: "삭제에 실패했습니다."
                AuthResult.Error(msg)
            }
        }.getOrElse { AuthResult.Error(it.message ?: "오류 발생") }
    }

    // ────────────────────────────────────────────────────────────────────────
    private fun applyAuthResponse(resp: AuthResponse, provider: String): AuthResult {
        if (resp.access_token.isEmpty() || resp.user == null)
            return AuthResult.Error("인증에 실패했습니다.")
        saveTokens(resp.access_token, resp.refresh_token)
        val state = AuthState.LoggedIn(
            userId   = resp.user.id,
            email    = resp.user.email ?: "",
            provider = provider
        )
        saveAuthState(state)
        return AuthResult.Success(state)
    }
}
