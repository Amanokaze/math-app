package com.mathapp.practice.ui

import kotlin.random.Random

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
    (1..10).map { n -> StageInfo(op, n) }
}

fun stagesFor(op: MathOperation): List<StageInfo> = (1..10).map { StageInfo(op, it) }

// ─── Progress persistence ─────────────────────────────────────────────────────

fun saveStageResult(op: MathOperation, number: Int, stars: Int) {
    val key = "${op.key}_${number}"
    AppSettings.setInt("stage_${key}_unlocked", 1)
    val prev = AppSettings.getInt("stage_${key}_stars", 0)
    if (stars > prev) AppSettings.setInt("stage_${key}_stars", stars)
    // Unlock next stage on any clear (≥1 star)
    if (stars >= 1) {
        val nextNum = number + 1
        if (nextNum <= 10) {
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
        (1..10).forEach { n ->
            AppSettings.setInt("stage_${op.key}_${n}_unlocked", 0)
            AppSettings.setInt("stage_${op.key}_${n}_stars", 0)
        }
    }
    AppSettings.setString("last_op", "")
    AppSettings.setInt("last_stage_num", 0)
}

// ─── Star calculation ─────────────────────────────────────────────────────────

/** correctCount = number of problems answered correctly on first attempt (0-10).
 *  totalSeconds  = total elapsed wall-clock seconds for the full game. */
fun calcStars(correctCount: Int, totalSeconds: Float): Int {
    val avg = totalSeconds / 10f
    return when {
        correctCount == 10 && avg <= 3f -> 3
        correctCount >= 8               -> 2
        else                            -> 1
    }
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

fun generateProblems(op: MathOperation, stageNumber: Int): List<MathProblem> =
    List(10) { generateOneProblem(op, stageNumber) }

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
    return list.getOrElse(stage - 1) { "Stage $stage" }
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
