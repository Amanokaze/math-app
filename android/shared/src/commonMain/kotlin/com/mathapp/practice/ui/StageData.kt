package com.mathapp.practice.ui

import kotlin.random.Random

// ─── Character System ─────────────────────────────────────────────────────────

enum class CharacterType(val emoji: String, val nameKey: String) {
    BEAR("🐻", "char_bear"),
    RABBIT("🐰", "char_rabbit"),
    FOX("🦊", "char_fox"),
    OWL("🦉", "char_owl")
}

fun getSelectedCharacter(): CharacterType? {
    val idx = AppSettings.getInt("selectedCharacter", -1)
    return if (idx >= 0 && idx < CharacterType.entries.size) CharacterType.entries[idx] else null
}

fun setSelectedCharacter(char: CharacterType) {
    AppSettings.setInt("selectedCharacter", CharacterType.entries.indexOf(char))
}

fun hasSeenOnboarding(): Boolean = AppSettings.getInt("hasSeenOnboarding", 0) == 1

fun markOnboardingSeen() = AppSettings.setInt("hasSeenOnboarding", 1)

// ─── Daily Quest ──────────────────────────────────────────────────────────────

enum class QuestType { PROGRESS, REVIEW, NEW }

data class DailyQuest(
    val operation: MathOperation,
    val stageNumber: Int,
    val questType: QuestType,
    val targetCount: Int,
    val progress: Int
) {
    val isCompleted: Boolean get() = progress >= targetCount
    val progressFraction: Float get() = (progress.toFloat() / targetCount).coerceIn(0f, 1f)
}

fun getTodayDateString(): String {
    val epochDay = getCurrentEpochSeconds() / 86400L
    return epochDay.toString()
}

/**
 * Selects an appropriate operation + stage for today's quest based on learner progress.
 *
 * Distribution:
 *  - 60 %: continue current progress (last played op, next uncompleted stage)
 *  - 30 %: review the weakest (1-star) stage
 *  - 10 %: introduce a not-yet-started operation at stage 1
 */
private fun chooseDailyQuestParams(): Triple<MathOperation, Int, QuestType> {
    data class OpStat(val op: MathOperation, val highestCleared: Int, val weakestStage: Int?)

    val stats = MathOperation.entries.map { op ->
        val stages = stagesFor(op)
        val highestCleared = stages.filter { it.stars > 0 }.maxOfOrNull { it.number } ?: 0
        val weakestStage = stages.filter { it.stars == 1 }.minByOrNull { it.number }?.number
        OpStat(op, highestCleared, weakestStage)
    }

    val started    = stats.filter { it.highestCleared > 0 }
    val notStarted = stats.filter { it.highestCleared == 0 }
    val withWeakness = stats.filter { it.weakestStage != null }

    val rand = Random.nextFloat()

    // 10 %: introduce a new operation
    if (notStarted.isNotEmpty() && rand < 0.10f) {
        val pick = notStarted.random()
        return Triple(pick.op, 1, QuestType.NEW)
    }

    // 30 %: review the weakest stage
    if (withWeakness.isNotEmpty() && rand < 0.40f) {
        val pick = withWeakness.random()
        return Triple(pick.op, pick.weakestStage!!, QuestType.REVIEW)
    }

    // 60 %: continue progress on the last-played (or most-advanced) operation
    if (started.isNotEmpty()) {
        val lastOp = getLastPlayedStage()?.first
        val stat = if (lastOp != null)
            stats.find { it.op == lastOp } ?: started.maxByOrNull { it.highestCleared }!!
        else
            started.maxByOrNull { it.highestCleared }!!
        val nextStage = (stat.highestCleared + 1).coerceAtMost(20)
        return Triple(stat.op, nextStage, QuestType.PROGRESS)
    }

    // Fallback: no progress at all — start with addition stage 1
    return Triple(MathOperation.ADDITION, 1, QuestType.NEW)
}

fun getOrCreateDailyQuest(): DailyQuest {
    val today = getTodayDateString()
    val savedDate = AppSettings.getString("questDate", "")
    if (savedDate != today) {
        // New day — choose quest based on learner progress
        val (op, stage, type) = chooseDailyQuestParams()
        AppSettings.setString("questDate", today)
        AppSettings.setString("questOp", op.key)
        AppSettings.setInt("questStage", stage)
        AppSettings.setString("questType", type.name)
        AppSettings.setInt("questTarget", 10)
        AppSettings.setInt("questProgress", 0)
    }
    val opKey = AppSettings.getString("questOp", MathOperation.ADDITION.key)
    val op = MathOperation.entries.find { it.key == opKey } ?: MathOperation.ADDITION
    val stage = AppSettings.getInt("questStage", 1)
    val typeStr = AppSettings.getString("questType", QuestType.PROGRESS.name)
    val type = QuestType.entries.find { it.name == typeStr } ?: QuestType.PROGRESS
    return DailyQuest(
        operation = op,
        stageNumber = stage,
        questType = type,
        targetCount = AppSettings.getInt("questTarget", 10),
        progress = AppSettings.getInt("questProgress", 0)
    )
}

fun addQuestProgress(amount: Int) {
    val quest = getOrCreateDailyQuest()
    if (!quest.isCompleted) {
        val newProgress = (quest.progress + amount).coerceAtMost(quest.targetCount)
        AppSettings.setInt("questProgress", newProgress)
    }
}

// ─── Badge System ─────────────────────────────────────────────────────────────

data class Badge(
    val id: String,
    val emoji: String,
    val nameKey: String,
    val descKey: String
)

val ALL_BADGES = listOf(
    Badge("first_step",  "🌟", "badge_first_step",  "badge_first_step_desc"),
    Badge("speed_demon", "⚡", "badge_speed_demon",  "badge_speed_demon_desc"),
    Badge("perfectionist","💎","badge_perfectionist","badge_perfectionist_desc"),
    Badge("math_master", "🏆", "badge_math_master",  "badge_math_master_desc"),
    Badge("consistent",  "🔥", "badge_consistent",   "badge_consistent_desc"),
    Badge("explorer",    "🗺️", "badge_explorer",     "badge_explorer_desc")
)

fun isBadgeEarned(badgeId: String): Boolean =
    AppSettings.getInt("badge_$badgeId", 0) == 1

fun earnBadge(badgeId: String) = AppSettings.setInt("badge_$badgeId", 1)

fun checkAndAwardBadges() {
    // First step — complete any stage
    if (ALL_STAGES.any { it.stars > 0 }) earnBadge("first_step")
    // Perfectionist — 3 stars on 10+ stages
    if (ALL_STAGES.count { it.stars == 3 } >= 10) earnBadge("perfectionist")
    // Math master — all 40 stages cleared
    if (ALL_STAGES.all { it.stars > 0 }) earnBadge("math_master")
    // Explorer — at least 1 stage cleared in all 4 operations
    if (MathOperation.entries.all { op -> stagesFor(op).any { it.stars > 0 } }) earnBadge("explorer")
    // Speed demon — any stage with 3 stars
    if (ALL_STAGES.any { it.stars == 3 }) earnBadge("speed_demon")
}

// ─── Streak Tracking ──────────────────────────────────────────────────────────

fun recordPlayToday() {
    val today = getTodayDateString()
    val lastPlay = AppSettings.getString("lastPlayDate", "")
    if (lastPlay == today) return
    val yesterday = (getTodayDateString().toLongOrNull()?.minus(1))?.toString() ?: ""
    val streak = if (lastPlay == yesterday) AppSettings.getInt("streakDays", 0) + 1 else 1
    AppSettings.setString("lastPlayDate", today)
    AppSettings.setInt("streakDays", streak)
    if (streak >= 7) earnBadge("consistent")
}

fun getStreakDays(): Int = AppSettings.getInt("streakDays", 0)

// ─── Daily Stats (for Report) ─────────────────────────────────────────────────

fun recordDailyStats(solvedCount: Int, correctCount: Int) {
    val today = getTodayDateString()
    if (AppSettings.getString("todayDate", "") != today) {
        AppSettings.setString("todayDate", today)
        AppSettings.setInt("todaySolved", 0)
        AppSettings.setInt("todayCorrect", 0)
    }
    AppSettings.setInt("todaySolved", AppSettings.getInt("todaySolved", 0) + solvedCount)
    AppSettings.setInt("todayCorrect", AppSettings.getInt("todayCorrect", 0) + correctCount)

    // Weekly chart: dayOfWeek 0=Mon … 6=Sun
    val day = (getTodayDateString().toLongOrNull() ?: 0L) % 7
    val key = "weekSolved_$day"
    AppSettings.setInt(key, AppSettings.getInt(key, 0) + solvedCount)
}

fun getTodayStats(): Pair<Int, Int> {
    val today = getTodayDateString()
    if (AppSettings.getString("todayDate", "") != today) return Pair(0, 0)
    return Pair(AppSettings.getInt("todaySolved", 0), AppSettings.getInt("todayCorrect", 0))
}

/** Returns list of solved counts for the last 7 days (Mon-Sun slot), today's slot last */
fun getWeeklyStats(): List<Int> = (0..6).map { AppSettings.getInt("weekSolved_$it", 0) }

// ─── Treasure Chest ───────────────────────────────────────────────────────────

data class TreasureItem(val id: Int, val emoji: String, val requiredPoints: Int)

val ALL_TREASURES = listOf(
    TreasureItem(0, "🗝️",   100),
    TreasureItem(1, "💍",   300),
    TreasureItem(2, "📜",   600),
    TreasureItem(3, "🏺",  1000),
    TreasureItem(4, "🗡️", 1500),
    TreasureItem(5, "🛡️", 2200),
    TreasureItem(6, "💎",  3000),
    TreasureItem(7, "👑",  4200),
    TreasureItem(8, "🌟",  6000)
)

fun getTreasureLevel(): Int {
    val pts = getTotalPoints()
    return when {
        pts >= 6000 -> 10
        pts >= 4200 -> 9
        pts >= 3000 -> 8
        pts >= 2200 -> 7
        pts >= 1500 -> 6
        pts >= 1000 -> 5
        pts >= 600  -> 4
        pts >= 300  -> 3
        pts >= 100  -> 2
        else        -> 1
    }
}

fun getNextLevelPoints(): Int {
    val pts = getTotalPoints()
    return when {
        pts < 100  -> 100
        pts < 300  -> 300
        pts < 600  -> 600
        pts < 1000 -> 1000
        pts < 1500 -> 1500
        pts < 2200 -> 2200
        pts < 3000 -> 3000
        pts < 4200 -> 4200
        pts < 6000 -> 6000
        else       -> 6000
    }
}

fun isTreasureUnlocked(item: TreasureItem): Boolean = getTotalPoints() >= item.requiredPoints

// ─── Operations ───────────────────────────────────────────────────────────────

enum class MathOperation(val symbol: String, val key: String) {
    ADDITION("+", "add"),
    SUBTRACTION("-", "sub"),
    MULTIPLICATION("×", "mul"),
    DIVISION("÷", "div")
}

// ─── MathProblem ──────────────────────────────────────────────────────────────

data class MathProblem(val num1: Int, val num2: Int, val op: String) {
    val answer: Int = when (op) {
        "+" -> num1 + num2
        "-" -> num1 - num2
        "×" -> num1 * num2
        "÷" -> if (num2 != 0) num1 / num2 else 0
        else -> 0
    }
    val displayText: String = "$num1 $op $num2"
}

// ─── Stage info ───────────────────────────────────────────────────────────────

data class StageInfo(val operation: MathOperation, val number: Int) {
    private val settingsKey: String get() = "${operation.key}_${number}"

    // Stage 1 of every operation is always available
    val isUnlocked: Boolean
        get() = if (number == 1) true
                else AppSettings.getInt("stage_${settingsKey}_unlocked", 0) == 1

    val stars: Int
        get() = AppSettings.getInt("stage_${settingsKey}_stars", 0)

    fun desc(lang: AppLanguage): String = stageDesc(operation, number, lang)
}

val ALL_STAGES: List<StageInfo> = MathOperation.entries.flatMap { op ->
    (1..20).map { n -> StageInfo(op, n) }
}

fun stagesFor(op: MathOperation): List<StageInfo> = (1..20).map { StageInfo(op, it) }

// ─── Progress persistence ─────────────────────────────────────────────────────

fun saveStageResult(op: MathOperation, number: Int, stars: Int) {
    val key = "${op.key}_${number}"
    AppSettings.setInt("stage_${key}_unlocked", 1)
    val prev = AppSettings.getInt("stage_${key}_stars", 0)
    if (stars > prev) AppSettings.setInt("stage_${key}_stars", stars)
    // Unlock next stage on any clear (≥1 star)
    if (stars >= 1) {
        val nextNum = number + 1
        if (nextNum <= 20) {
            AppSettings.setInt("stage_${op.key}_${nextNum}_unlocked", 1)
        } else {
            val ops = MathOperation.entries
            val nextIdx = ops.indexOf(op) + 1
            if (nextIdx < ops.size) {
                // Next operation's stage 1 is always unlocked anyway,
                // but mark it explicitly so the UI can signal progress
                AppSettings.setInt("stage_${ops[nextIdx].key}_1_unlocked", 1)
            }
        }
    }
}

fun getLastPlayedStage(): Pair<MathOperation, Int>? {
    val opKey = AppSettings.getString("last_op", "")
    val num = AppSettings.getInt("last_stage_num", 0)
    if (opKey.isEmpty() || num == 0) return null
    return MathOperation.entries.find { it.key == opKey }?.let { Pair(it, num) }
}

fun saveLastPlayedStage(op: MathOperation, number: Int) {
    AppSettings.setString("last_op", op.key)
    AppSettings.setInt("last_stage_num", number)
}

fun resetAllProgress() {
    MathOperation.entries.forEach { op ->
        (1..20).forEach { n ->
            AppSettings.setInt("stage_${op.key}_${n}_unlocked", 0)
            AppSettings.setInt("stage_${op.key}_${n}_stars", 0)
        }
    }
    AppSettings.setString("last_op", "")
    AppSettings.setInt("last_stage_num", 0)
    AppSettings.setInt("total_points", 0)
    // Reset weekly / daily stats
    (0..6).forEach { AppSettings.setInt("weekSolved_$it", 0) }
    AppSettings.setString("todayDate", "")
    AppSettings.setInt("todaySolved", 0)
    AppSettings.setInt("todayCorrect", 0)
    // Reset shop & coins
    resetShopProgress()
    // Reset daily quest
    AppSettings.setString("questDate", "")
    AppSettings.setString("questOp", "")
    AppSettings.setInt("questStage", 1)
    AppSettings.setString("questType", "")
    AppSettings.setInt("questTarget", 10)
    AppSettings.setInt("questProgress", 0)
}

// ─── Star calculation ─────────────────────────────────────────────────────────

/** correctCount = number of problems answered correctly on first attempt (0-10).
 *  totalSeconds  = total elapsed wall-clock seconds for the full game. */
fun calcStars(correctCount: Int, totalSeconds: Float): Int {
    val avg = totalSeconds / 10f
    return when {
        correctCount == 10 && avg <= 6f -> 3
        correctCount >= 8               -> 2
        else                            -> 1
    }
}

// ─── Point calculation ────────────────────────────────────────────────────────

/** Returns points earned for a stage result.
 *  stageNumber 1~3 = easy tier, 4~6 = normal tier, 7~10 = hard tier. */
fun calcPoints(stageNumber: Int, stars: Int): Int {
    return when {
        stageNumber <= 6  -> when (stars) { 1 -> 100; 2 -> 110; else -> 120 }
        stageNumber <= 14 -> when (stars) { 1 -> 150; 2 -> 165; else -> 180 }
        else              -> when (stars) { 1 -> 200; 2 -> 220; else -> 240 }
    }
}

fun addPoints(points: Int) {
    val current = AppSettings.getInt("total_points", 0)
    AppSettings.setInt("total_points", current + points)
}

fun getTotalPoints(): Int = AppSettings.getInt("total_points", 0)

// ─── Coin System ──────────────────────────────────────────────────────────────

/** Regular stage: repeatable, so coins are modest. */
fun calcCoins(stageNumber: Int, stars: Int): Int = when {
    stageNumber <= 6  -> stars * 5
    stageNumber <= 14 -> stars * 8
    else              -> stars * 12
}

/** Quest per-session reward: ~1.5× regular (once-a-day limited). */
fun calcQuestCoins(stageNumber: Int, stars: Int): Int = when {
    stageNumber <= 6  -> stars * 8
    stageNumber <= 14 -> stars * 12
    else              -> stars * 18
}

/** Bonus awarded once when the daily quest target is fully reached. */
const val QUEST_COMPLETION_BONUS = 25

fun addCoins(amount: Int) {
    val current = AppSettings.getInt("coin_balance", 0)
    AppSettings.setInt("coin_balance", current + amount)
}

fun spendCoins(amount: Int): Boolean {
    val current = AppSettings.getInt("coin_balance", 0)
    if (current < amount) return false
    AppSettings.setInt("coin_balance", current - amount)
    return true
}

fun getCoinBalance(): Int = AppSettings.getInt("coin_balance", 0)

// ─── Heart (Energy) System ────────────────────────────────────────────────────

const val MAX_HEARTS = 15
const val HEART_RECHARGE_SECONDS = 300L // 5 minutes per heart

/** Must be called on screen entry to credit hearts earned while app was closed. */
fun rechargeHearts() {
    val hearts = AppSettings.getInt("hearts", MAX_HEARTS)
    if (hearts >= MAX_HEARTS) return
    val currentTime = getCurrentEpochSeconds()
    val lastChargeTime = AppSettings.getString("last_heart_charge_time", "0").toLongOrNull() ?: currentTime
    val elapsed = currentTime - lastChargeTime
    val toAdd = (elapsed / HEART_RECHARGE_SECONDS).toInt()
    if (toAdd > 0) {
        val newHearts = minOf(MAX_HEARTS, hearts + toAdd)
        AppSettings.setInt("hearts", newHearts)
        if (newHearts < MAX_HEARTS) {
            val newLastChargeTime = lastChargeTime + toAdd * HEART_RECHARGE_SECONDS
            AppSettings.setString("last_heart_charge_time", newLastChargeTime.toString())
        }
    }
}

fun getHearts(): Int = AppSettings.getInt("hearts", MAX_HEARTS)

/** Removes one heart. Returns false if no hearts available. */
fun consumeHeart(): Boolean {
    val hearts = getHearts()
    if (hearts <= 0) return false
    AppSettings.setInt("hearts", hearts - 1)
    if (hearts == MAX_HEARTS) {
        // Was full — start recharge timer now
        AppSettings.setString("last_heart_charge_time", getCurrentEpochSeconds().toString())
    }
    return true
}

/** Returns seconds remaining until the next heart recharges (0 if full). */
fun secondsUntilNextHeart(): Long {
    if (getHearts() >= MAX_HEARTS) return 0L
    val lastChargeTime = AppSettings.getString("last_heart_charge_time", "0").toLongOrNull()
        ?: return HEART_RECHARGE_SECONDS
    val elapsed = getCurrentEpochSeconds() - lastChargeTime
    val secondsInCurrentCycle = elapsed % HEART_RECHARGE_SECONDS
    return HEART_RECHARGE_SECONDS - secondsInCurrentCycle
}

// ─── Aggregate progress ───────────────────────────────────────────────────────

fun overallProgress(): Float {
    val cleared = ALL_STAGES.count { it.stars > 0 }
    return cleared.toFloat() / ALL_STAGES.size.toFloat()
}

fun operationProgress(op: MathOperation): Float {
    val stages = stagesFor(op)
    return stages.count { it.stars > 0 }.toFloat() / stages.size.toFloat()
}

// ─── Problem generation ───────────────────────────────────────────────────────

fun generateProblems(op: MathOperation, stageNumber: Int): List<MathProblem> {
    val difficulty = (stageNumber + 1) / 2   // 1,2→1; 3,4→2; ...; 19,20→10
    return List(10) { generateOneProblem(op, difficulty) }
}

private fun generateOneProblem(op: MathOperation, stage: Int): MathProblem = when (op) {
    MathOperation.ADDITION       -> generateAddition(stage)
    MathOperation.SUBTRACTION    -> generateSubtraction(stage)
    MathOperation.MULTIPLICATION -> generateMultiplication(stage)
    MathOperation.DIVISION       -> generateDivision(stage)
}

private fun generateAddition(stage: Int): MathProblem {
    val (a, b) = when (stage) {
        1    -> { val a = Random.nextInt(0, 6);  Pair(a, Random.nextInt(0, 5 - a + 1)) }   // sum ≤ 5
        2    -> { val a = Random.nextInt(1, 9);  Pair(a, Random.nextInt(1, 9 - a + 1)) }   // sum ≤ 9
        3    -> { val a = Random.nextInt(1, 10); Pair(a, 10 - a) }                          // make 10
        4    -> { val a = Random.nextInt(2, 15); Pair(a, Random.nextInt(2, 21 - a)) }       // sum ≤ 20
        5    -> { val a = Random.nextInt(10, 90); Pair(a, Random.nextInt(1, 100 - a)) }     // 2-digit general
        6    -> {                                                                             // 2-digit + 1-digit with carry
            val onesA = Random.nextInt(2, 9)
            val b2 = Random.nextInt(10 - onesA, 10)
            Pair(Random.nextInt(1, 9) * 10 + onesA, b2)
        }
        7    -> { val a = Random.nextInt(10, 70); Pair(a, Random.nextInt(10, 100 - a)) }    // 2-digit + 2-digit
        8    -> { Pair(Random.nextInt(100, 990), Random.nextInt(1, 10)) }                    // 3-digit + 1-digit
        9    -> { Pair(Random.nextInt(100, 900), Random.nextInt(10, 100)) }                  // 3-digit + 2-digit
        else -> { val a = Random.nextInt(100, 900); Pair(a, Random.nextInt(1, 100)) }       // mixed large
    }
    return MathProblem(a, b, "+")
}

private fun generateSubtraction(stage: Int): MathProblem {
    val (a, b) = when (stage) {
        1    -> { val res = Random.nextInt(0, 6); val b = Random.nextInt(0, 6 - res); Pair(res + b, b) } // result ≤ 5
        2    -> { val a = Random.nextInt(1, 10); Pair(a, Random.nextInt(0, a + 1)) }                      // single-digit
        3    -> { val a = Random.nextInt(10, 20); Pair(a, Random.nextInt(1, a)) }                         // from teens
        4    -> { val a = Random.nextInt(10, 30); Pair(a, Random.nextInt(1, a)) }                         // borrowing
        5    -> { val a = Random.nextInt(10, 100); Pair(a, Random.nextInt(0, a + 1)) }                    // 2-digit general
        6    -> {                                                                                           // 2-digit - 1-digit with borrow
            val onesA = Random.nextInt(0, 8)
            val b2 = Random.nextInt(onesA + 1, 10)
            Pair(Random.nextInt(1, 10) * 10 + onesA, b2)
        }
        7    -> { val a = Random.nextInt(20, 100); Pair(a, Random.nextInt(10, a)) }                       // 2-digit - 2-digit
        8    -> { Pair(Random.nextInt(100, 999), Random.nextInt(1, 10)) }                                  // 3-digit - 1-digit
        9    -> { val a = Random.nextInt(100, 999); Pair(a, Random.nextInt(10, a - 9)) }                  // 3-digit - 2-digit
        else -> { val a = Random.nextInt(100, 999); Pair(a, Random.nextInt(1, a + 1)) }                   // mixed large
    }
    return MathProblem(a, b, "-")
}

private fun generateMultiplication(stage: Int): MathProblem {
    // Stages 8-10: two-digit × single-digit
    if (stage >= 8) {
        val (a, b) = when (stage) {
            8    -> Pair(Random.nextInt(10, 30), Random.nextInt(2, 4))   // 2-digit × 1~3
            9    -> Pair(Random.nextInt(10, 20), Random.nextInt(4, 7))   // 2-digit × 4~6
            else -> Pair(Random.nextInt(11, 16), Random.nextInt(7, 10))  // 2-digit × 7~9
        }
        return if (Random.nextBoolean()) MathProblem(a, b, "×") else MathProblem(b, a, "×")
    }
    val m = when (stage) {
        1    -> Random.nextInt(1, 3)   // ×1, ×2
        2    -> Random.nextInt(3, 5)   // ×3, ×4
        3    -> Random.nextInt(5, 7)   // ×5, ×6
        4    -> Random.nextInt(7, 9)   // ×7, ×8
        5    -> 9                      // ×9
        6    -> Random.nextInt(1, 6)   // ×1~×5 mixed
        else -> Random.nextInt(6, 10)  // ×6~×9 mixed (stage 7)
    }
    val n = Random.nextInt(1, 10)
    return if (Random.nextBoolean()) MathProblem(m, n, "×") else MathProblem(n, m, "×")
}

private fun generateDivision(stage: Int): MathProblem {
    val (divisors, maxQuotient) = when (stage) {
        1    -> Pair(listOf(2, 3), 9)
        2    -> Pair(listOf(4, 5), 9)
        3    -> Pair(listOf(6, 7), 9)
        4    -> Pair(listOf(8, 9), 9)
        5    -> Pair((2..5).toList(), 9)    // ÷2~÷5 mixed
        6    -> Pair((6..9).toList(), 9)    // ÷6~÷9 mixed
        7    -> Pair((2..9).toList(), 9)    // ÷2~÷9 mixed
        8    -> Pair((2..5).toList(), 12)   // large ÷2~÷5
        9    -> Pair((6..9).toList(), 12)   // large ÷6~÷9
        else -> Pair((2..9).toList(), 12)   // mixed large
    }
    val b = divisors.random()
    return MathProblem(b * Random.nextInt(1, maxQuotient + 1), b, "÷")
}

// ─── Stage descriptions (per language) ───────────────────────────────────────

fun stageDesc(op: MathOperation, stage: Int, lang: AppLanguage): String {
    val level = (stage + 1) / 2  // 1,2→1; 3,4→2; ...; 19,20→10
    val list = when (op) {
        MathOperation.ADDITION -> when (lang) {
            AppLanguage.KOREAN             -> listOf("합이 5 이하", "합이 9 이하", "10 만들기", "합이 20 이하", "두 자리 덧셈", "받아올림 덧셈", "두 자리끼리 덧셈", "세 자리+한 자리", "세 자리+두 자리", "혼합 덧셈")
            AppLanguage.JAPANESE           -> listOf("合計5以下", "合計9以下", "10の合成", "合計20以下", "2桁のたし算", "繰り上がりあり", "2桁+2桁", "3桁+1桁", "3桁+2桁", "混合たし算")
            AppLanguage.CHINESE_TRADITIONAL-> listOf("和≤5", "和≤9", "湊成10", "和≤20", "兩位數加法", "進位加法", "兩位+兩位", "三位+個位", "三位+兩位", "混合加法")
            AppLanguage.CHINESE_SIMPLIFIED -> listOf("和≤5", "和≤9", "凑成10", "和≤20", "两位数加法", "进位加法", "两位+两位", "三位+个位", "三位+两位", "混合加法")
            else                           -> listOf("Sum ≤ 5", "Sum ≤ 9", "Make 10", "Sum ≤ 20", "Two-digit", "With carry", "2-digit + 2-digit", "3-digit + 1-digit", "3-digit + 2-digit", "Mixed")
        }
        MathOperation.SUBTRACTION -> when (lang) {
            AppLanguage.KOREAN             -> listOf("결과가 5 이하", "한 자리 뺄셈", "십의 자리 뺄셈", "받아내림", "두 자리 뺄셈", "받아내림 뺄셈", "두 자리끼리 뺄셈", "세 자리-한 자리", "세 자리-두 자리", "혼합 뺄셈")
            AppLanguage.JAPANESE           -> listOf("結果5以下", "1桁のひき算", "10台のひき算", "繰り下がり", "2桁のひき算", "繰り下がりあり", "2桁-2桁", "3桁-1桁", "3桁-2桁", "混合ひき算")
            AppLanguage.CHINESE_TRADITIONAL-> listOf("結果≤5", "個位數減法", "十位數減法", "借位減法", "兩位數減法", "借位進階", "兩位-兩位", "三位-個位", "三位-兩位", "混合減法")
            AppLanguage.CHINESE_SIMPLIFIED -> listOf("结果≤5", "个位数减法", "十位数减法", "借位减法", "两位数减法", "借位进阶", "两位-两位", "三位-个位", "三位-两位", "混合减法")
            else                           -> listOf("Result ≤ 5", "Single-digit", "From teens", "Borrowing", "Two-digit", "With borrow", "2-digit - 2-digit", "3-digit - 1-digit", "3-digit - 2-digit", "Mixed")
        }
        MathOperation.MULTIPLICATION -> when (lang) {
            AppLanguage.KOREAN             -> listOf("×1, ×2", "×3, ×4", "×5, ×6", "×7, ×8", "×9", "×1~×5 혼합", "×6~×9 혼합", "두 자리×1~3", "두 자리×4~6", "두 자리×7~9")
            AppLanguage.JAPANESE           -> listOf("×1, ×2", "×3, ×4", "×5, ×6", "×7, ×8", "×9", "×1~×5 混合", "×6~×9 混合", "2桁×1~3", "2桁×4~6", "2桁×7~9")
            AppLanguage.CHINESE_TRADITIONAL-> listOf("×1, ×2", "×3, ×4", "×5, ×6", "×7, ×8", "×9", "×1~×5 混合", "×6~×9 混合", "兩位×1~3", "兩位×4~6", "兩位×7~9")
            AppLanguage.CHINESE_SIMPLIFIED -> listOf("×1, ×2", "×3, ×4", "×5, ×6", "×7, ×8", "×9", "×1~×5 混合", "×6~×9 混合", "两位×1~3", "两位×4~6", "两位×7~9")
            else                           -> listOf("×1, ×2", "×3, ×4", "×5, ×6", "×7, ×8", "×9", "×1~×5 mixed", "×6~×9 mixed", "2-digit × 1~3", "2-digit × 4~6", "2-digit × 7~9")
        }
        MathOperation.DIVISION -> when (lang) {
            AppLanguage.KOREAN             -> listOf("÷2, ÷3", "÷4, ÷5", "÷6, ÷7", "÷8, ÷9", "÷2~÷5 혼합", "÷6~÷9 혼합", "÷2~÷9 혼합", "큰 수 ÷2~÷5", "큰 수 ÷6~÷9", "혼합 나눗셈")
            AppLanguage.JAPANESE           -> listOf("÷2, ÷3", "÷4, ÷5", "÷6, ÷7", "÷8, ÷9", "÷2~÷5 混合", "÷6~÷9 混合", "÷2~÷9 混合", "大きな数÷2~5", "大きな数÷6~9", "混合わり算")
            AppLanguage.CHINESE_TRADITIONAL-> listOf("÷2, ÷3", "÷4, ÷5", "÷6, ÷7", "÷8, ÷9", "÷2~÷5 混合", "÷6~÷9 混合", "÷2~÷9 混合", "大數÷2~÷5", "大數÷6~÷9", "混合除法")
            AppLanguage.CHINESE_SIMPLIFIED -> listOf("÷2, ÷3", "÷4, ÷5", "÷6, ÷7", "÷8, ÷9", "÷2~÷5 混合", "÷6~÷9 混合", "÷2~÷9 混合", "大数÷2~÷5", "大数÷6~÷9", "混合除法")
            else                           -> listOf("÷2, ÷3", "÷4, ÷5", "÷6, ÷7", "÷8, ÷9", "÷2~÷5 mixed", "÷6~÷9 mixed", "÷2~÷9 mixed", "Large ÷2~÷5", "Large ÷6~÷9", "Mixed ÷2~÷9")
        }
    }
    return list.getOrElse(level - 1) { "Stage $stage" }
}

fun opName(op: MathOperation, lang: AppLanguage): String = when (op) {
    MathOperation.ADDITION -> when (lang) {
        AppLanguage.KOREAN -> "덧셈"; AppLanguage.JAPANESE -> "たし算"
        AppLanguage.CHINESE_TRADITIONAL, AppLanguage.CHINESE_SIMPLIFIED -> "加法"
        else -> "Addition"
    }
    MathOperation.SUBTRACTION -> when (lang) {
        AppLanguage.KOREAN -> "뺄셈"; AppLanguage.JAPANESE -> "ひき算"
        AppLanguage.CHINESE_TRADITIONAL, AppLanguage.CHINESE_SIMPLIFIED -> "減法"
        else -> "Subtraction"
    }
    MathOperation.MULTIPLICATION -> when (lang) {
        AppLanguage.KOREAN -> "곱셈"; AppLanguage.JAPANESE -> "かけ算"
        AppLanguage.CHINESE_TRADITIONAL, AppLanguage.CHINESE_SIMPLIFIED -> "乘法"
        else -> "Multiplication"
    }
    MathOperation.DIVISION -> when (lang) {
        AppLanguage.KOREAN -> "나눗셈"; AppLanguage.JAPANESE -> "わり算"
        AppLanguage.CHINESE_TRADITIONAL, AppLanguage.CHINESE_SIMPLIFIED -> "除法"
        else -> "Division"
    }
}
