package com.mathapp.practice.ui

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

object SyncRepository {
    enum class ApplyMode {
        MergeWithLocal,
        ReplaceLocal
    }

    sealed class RemoteSnapshotResult {
        data class Success(val snapshot: JsonObject?) : RemoteSnapshotResult()
        object Failure : RemoteSnapshotResult()
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val badgeIds = listOf(
        "first_step",
        "speed_demon",
        "perfectionist",
        "math_master",
        "consistent",
        "explorer"
    )
    private val client = HttpClient {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
    }

    // ── 로컬 데이터를 JSON 스냅샷으로 직렬화 ──────────────────────────────
    fun buildSnapshot(): JsonObject = buildJsonObject {
        put("selected_character",  AppSettings.getInt("selectedCharacter", -1))
        put("has_seen_onboarding", AppSettings.getInt("hasSeenOnboarding", 0))
        put("language_code",       AppSettings.getString("appLanguage", ""))
        put("color_scheme_mode",   AppSettings.getInt("colorSchemeMode", 1))
        put("total_points",        AppSettings.getInt("total_points", 0))
        put("coin_balance",        AppSettings.getInt("coin_balance", 0))
        put("streak_days",         AppSettings.getInt("streakDays", 0))
        put("last_play_date",      AppSettings.getString("lastPlayDate", ""))
        put("featured_treasure_id",AppSettings.getInt("featured_treasure_id", -1))
        put("snapshot_saved_at",   currentTimeMillis())

        // 스테이지 별 기록: "add_1" → stars
        put("stage_records", buildJsonObject {
            MathOperation.entries.forEach { op ->
                (1..20).forEach { n ->
                    val stars = AppSettings.getInt("stage_${op.key}_${n}_stars", 0)
                    if (stars > 0) put("${op.key}_$n", stars)
                }
            }
        })
        // 잠금 해제된 스테이지
        put("stage_unlocked", buildJsonObject {
            MathOperation.entries.forEach { op ->
                (1..20).forEach { n ->
                    val v = AppSettings.getInt("stage_${op.key}_${n}_unlocked", 0)
                    if (v == 1) put("${op.key}_$n", 1)
                }
            }
        })
        // 아이템 구매/장착
        put("items_owned", buildJsonArray {
            ALL_SHOP_ITEMS.filter { AppSettings.getInt("item_${it.id}", 0) == 1 }
                .forEach { add(it.id) }
        })
        put("items_equipped", buildJsonObject {
            ShopCategory.entries.forEach { cat ->
                val id = AppSettings.getString("equipped_${cat.name}", "")
                if (id.isNotEmpty()) put(cat.name, id)
            }
        })
        // 배지
        put("badges", buildJsonArray {
            badgeIds
                .filter { AppSettings.getInt("badge_$it", 0) == 1 }
                .forEach { add(it) }
        })
    }

    private fun clearSnapshotBackedData() {
        resetAllProgress()
        AppSettings.setInt("streakDays", 0)
        AppSettings.setString("lastPlayDate", "")
        badgeIds.forEach { AppSettings.setInt("badge_$it", 0) }
    }

    // ── 원격 스냅샷을 로컬에 적용 ────────────────────────────────────────
    // MergeWithLocal: 진행도 높은 쪽 우선, 배지·아이템은 합집합
    // ReplaceLocal: 계정 전환 복원용. 기존 로컬 진행도를 지운 뒤 원격만 적용
    fun applySnapshot(remote: JsonObject, mode: ApplyMode = ApplyMode.MergeWithLocal) {
        val replaceLocal = mode == ApplyMode.ReplaceLocal
        if (replaceLocal) clearSnapshotBackedData()

        fun ri(key: String) = remote[key]?.jsonPrimitive?.intOrNull ?: 0
        fun rs(key: String) = remote[key]?.jsonPrimitive?.contentOrNull ?: ""

        val remoteHasSeenOnboarding = ri("has_seen_onboarding") == 1
        if (replaceLocal) {
            AppSettings.setInt("selectedCharacter", if (remoteHasSeenOnboarding) ri("selected_character") else -1)
            AppSettings.setInt("hasSeenOnboarding", if (remoteHasSeenOnboarding) 1 else 0)
        } else if (remoteHasSeenOnboarding && AppSettings.getInt("hasSeenOnboarding", 0) == 0) {
            AppSettings.setInt("selectedCharacter", ri("selected_character"))
            AppSettings.setInt("hasSeenOnboarding", 1)
        }
        rs("language_code").takeIf { it.isNotEmpty() }?.let {
            AppSettings.setString("appLanguage", it)
        }
        AppSettings.setInt("colorSchemeMode", ri("color_scheme_mode").takeIf { it > 0 } ?: 1)
        AppSettings.setInt("total_points", if (replaceLocal) {
            ri("total_points")
        } else {
            maxOf(AppSettings.getInt("total_points", 0), ri("total_points"))
        })
        AppSettings.setInt("coin_balance", if (replaceLocal) {
            ri("coin_balance")
        } else {
            maxOf(AppSettings.getInt("coin_balance", 0), ri("coin_balance"))
        })
        AppSettings.setInt("streakDays", if (replaceLocal) {
            ri("streak_days")
        } else {
            maxOf(AppSettings.getInt("streakDays", 0), ri("streak_days"))
        })
        rs("last_play_date").takeIf { it.isNotEmpty() }?.let {
            AppSettings.setString("lastPlayDate", it)
        }
        val remoteFt = ri("featured_treasure_id").takeIf { it >= 0 } ?: -1
        if (replaceLocal) {
            AppSettings.setInt("featured_treasure_id", remoteFt)
        } else if (remoteFt >= 0 && AppSettings.getInt("featured_treasure_id", -1) < 0) {
            AppSettings.setInt("featured_treasure_id", remoteFt)
        }

        // 스테이지 별: max 병합
        remote["stage_records"]?.jsonObject?.forEach { (key, v) ->
            val remoteStars = v.jsonPrimitive.intOrNull ?: return@forEach
            val parts = key.split("_")
            if (parts.size == 2) {
                val localStars = AppSettings.getInt("stage_${parts[0]}_${parts[1]}_stars", 0)
                if (remoteStars > localStars)
                    AppSettings.setInt("stage_${parts[0]}_${parts[1]}_stars", remoteStars)
            }
        }
        // 잠금 해제: 원격에 있으면 로컬도 해제
        remote["stage_unlocked"]?.jsonObject?.forEach { (key, _) ->
            val parts = key.split("_")
            if (parts.size == 2)
                AppSettings.setInt("stage_${parts[0]}_${parts[1]}_unlocked", 1)
        }
        // 아이템: 원격 소유 목록 합집합
        remote["items_owned"]?.jsonArray?.forEach { item ->
            item.jsonPrimitive.contentOrNull?.let { AppSettings.setInt("item_$it", 1) }
        }
        remote["items_equipped"]?.jsonObject?.forEach { (cat, itemId) ->
            itemId.jsonPrimitive.contentOrNull?.let {
                if (AppSettings.getString("equipped_$cat", "").isEmpty())
                    AppSettings.setString("equipped_$cat", it)
            }
        }
        // 배지: 합집합
        remote["badges"]?.jsonArray?.forEach { badge ->
            badge.jsonPrimitive.contentOrNull?.let { AppSettings.setInt("badge_$it", 1) }
        }
    }

    // ── Supabase 에 스냅샷 업로드 (upsert) ────────────────────────────────
    suspend fun uploadData(userId: String): Boolean {
        val token = getAccessToken()
        if (token.isEmpty()) return false
        return runCatching {
            val snapshot = buildSnapshot()
            val patchBody = buildJsonObject {
                put("data", snapshot)
            }
            val body = buildJsonObject {
                put("user_id", userId)
                put("data", snapshot)
            }

            val updatedExistingRows = client
                .patch("$SUPABASE_URL/rest/v1/user_data?user_id=eq.$userId&select=user_id") {
                    header("apikey", SUPABASE_ANON_KEY)
                    header("Authorization", "Bearer $token")
                    header("Prefer", "return=representation")
                    contentType(ContentType.Application.Json)
                    setBody(patchBody.toString())
                }
                .bodyAsText()
                .let { text ->
                    runCatching { json.parseToJsonElement(text).jsonArray.isNotEmpty() }
                        .getOrDefault(false)
                }

            if (updatedExistingRows) return@runCatching true

            val response = client.post("$SUPABASE_URL/rest/v1/user_data") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
                header("Prefer", "resolution=merge-duplicates,return=minimal")
                contentType(ContentType.Application.Json)
                setBody(body.toString())
            }
            response.status.value in 200..299
        }.getOrElse { false }
    }

    private fun pickRemoteSnapshot(bodyText: String): JsonObject? {
        val trimmed = bodyText.trimStart()
        if (!trimmed.startsWith("[")) return null
        val snapshots = runCatching {
            json.parseToJsonElement(bodyText)
                .jsonArray
                .mapNotNull { row -> row.jsonObject["data"]?.jsonObject }
        }.getOrDefault(emptyList())

        return snapshots.withIndex().maxWithOrNull(
            compareBy<IndexedValue<JsonObject>> {
                it.value["snapshot_saved_at"]?.jsonPrimitive?.longOrNull ?: 0L
            }.thenBy { it.index }
        )?.value
    }

    private suspend fun fetchRemoteRows(userId: String, token: String): String {
        val orderedText = client
            .get("$SUPABASE_URL/rest/v1/user_data?user_id=eq.$userId&select=data&order=updated_at.asc.nullslast,created_at.asc.nullslast") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            .bodyAsText()

        if (orderedText.trimStart().startsWith("[")) return orderedText

        return client
            .get("$SUPABASE_URL/rest/v1/user_data?user_id=eq.$userId&select=data") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            .bodyAsText()
    }

    // ── 원격 스냅샷 조회만 (적용 없음) ───────────────────────────────────
    suspend fun fetchRemoteSnapshotResult(userId: String): RemoteSnapshotResult {
        val token = getAccessToken()
        if (token.isEmpty()) return RemoteSnapshotResult.Failure
        return runCatching {
            val bodyText = fetchRemoteRows(userId, token)
            if (!bodyText.trimStart().startsWith("[")) {
                RemoteSnapshotResult.Failure
            } else {
                RemoteSnapshotResult.Success(pickRemoteSnapshot(bodyText))
            }
        }.getOrElse { RemoteSnapshotResult.Failure }
    }

    suspend fun fetchRemoteSnapshot(userId: String): JsonObject? {
        return (fetchRemoteSnapshotResult(userId) as? RemoteSnapshotResult.Success)?.snapshot
    }

    // ── Supabase 에서 스냅샷 다운로드 후 로컬에 병합 ─────────────────────
    suspend fun downloadAndApply(userId: String): Boolean {
        val token = getAccessToken()
        if (token.isEmpty()) return false
        return runCatching {
            when (val result = fetchRemoteSnapshotResult(userId)) {
                RemoteSnapshotResult.Failure -> false
                is RemoteSnapshotResult.Success -> {
                    val remoteData = result.snapshot
                    if (remoteData == null) {
                        uploadData(userId)   // 원격에 데이터 없으면 로컬 올림
                    } else {
                        applySnapshot(remoteData)
                        true
                    }
                }
            }
        }.getOrElse { false }
    }
}
