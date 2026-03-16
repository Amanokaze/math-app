package com.mathapp.practice.ui

enum class AppLanguage(val code: String, val displayName: String) {
    KOREAN("ko", "한국어"),
    ENGLISH("en", "English"),
    CHINESE_TRADITIONAL("zh-Hant", "中文(繁體)"),
    CHINESE_SIMPLIFIED("zh-Hans", "中文(简体)"),
    JAPANESE("ja", "日本語");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.find { it.code == code } ?: ENGLISH
    }
}

object L10n {
    private val strings = mapOf(
        AppLanguage.KOREAN to mapOf(
            "reset_records" to "기록 초기화",
            "start" to "시작",
            "best_record" to "최고 기록",
            "time_format" to "%.2f초",
            "light" to "라이트",
            "dark" to "다크",
            "reset_confirm_title" to "기록을 초기화하시겠습니까?",
            "reset" to "초기화",
            "cancel" to "취소",
            "reset_confirm_message" to "최고 기록이 삭제됩니다.",
            "countdown_go" to "시작!",
            "complete" to "완료!",
            "new_record_title" to "최고 기록을 경신했어요! 🎉",
            "new_record_message" to "10문제를 모두 풀고\n새로운 기록을 세웠어요. 축하해요!",
            "well_done" to "수고하셨습니다",
            "best_record_format" to "최고 기록 %s",
            "to_main" to "메인으로",
            "quit_confirm" to "그만하시겠습니까?",
            "no" to "아니오",
            "yes" to "예",
            "confirm" to "확인",
            "language" to "언어",
            "level_easy" to "쉬움",
            "level_normal" to "보통",
            "help_title" to "레벨 설명",
            "help_easy_desc" to "• 왼쪽 숫자: 0~99\n• 오른쪽 숫자: 0~10\n• 뺄셈 시 결과가 음수가 되지 않음",
            "help_normal_desc" to "• 왼쪽 숫자: 0~99\n• 오른쪽 숫자: 0~99\n• 덧셈과 뺄셈 (음수 가능)",
            // ── New strings for stage UX ──
            "resume" to "이어서 하기",
            "new_start" to "새로 시작하기",
            "select_operation" to "연산 선택",
            "overall_progress" to "전체 진척도",
            "locked" to "잠김",
            "stage_map_title" to "스테이지 선택",
            "reset_progress" to "진척도 초기화",
            "reset_progress_confirm_title" to "진척도를 초기화하시겠습니까?",
            "reset_progress_confirm_message" to "모든 스테이지 기록이 삭제됩니다.",
            "stage_num_format" to "%d단계",
            "resume_hint" to "%s %d단계",
            "problem_progress" to "%d / 10",
            "result_title_3star" to "완벽해요!",
            "result_title_2star" to "잘했어요!",
            "result_title_1star" to "완주했어요!",
            "correct_format" to "맞힌 문제: %d / 10",
            "avg_time_format" to "평균 %.1f초",
            "retry" to "다시 하기",
            "next_stage" to "다음 단계로",
            "to_stage_map" to "스테이지 맵으로",
            "star_cond_3" to "⭐⭐⭐  만점 + 평균 3초 이하",
            "star_cond_2" to "⭐⭐  8문제 이상 정답",
            "star_cond_1" to "⭐  완주 성공"
        ),
        AppLanguage.ENGLISH to mapOf(
            "reset_records" to "Reset Records",
            "start" to "Start",
            "best_record" to "Best Record",
            "time_format" to "%.2fs",
            "light" to "Light",
            "dark" to "Dark",
            "reset_confirm_title" to "Reset your best record?",
            "reset" to "Reset",
            "cancel" to "Cancel",
            "reset_confirm_message" to "Your best record will be deleted.",
            "countdown_go" to "Go!",
            "complete" to "Complete!",
            "new_record_title" to "New Record! 🎉",
            "new_record_message" to "You solved all 10 problems\nand set a new record. Congratulations!",
            "well_done" to "Well done!",
            "best_record_format" to "Best record %s",
            "to_main" to "To Main",
            "quit_confirm" to "Quit the game?",
            "no" to "No",
            "yes" to "Yes",
            "confirm" to "Confirm",
            "language" to "Language",
            "level_easy" to "Easy",
            "level_normal" to "Normal",
            "help_title" to "Level Guide",
            "help_easy_desc" to "• Left number: 0~99\n• Right number: 0~10\n• Subtraction: result is never negative",
            "help_normal_desc" to "• Left number: 0~99\n• Right number: 0~99\n• Addition and subtraction (negative allowed)",
            // ── New strings for stage UX ──
            "resume" to "Continue",
            "new_start" to "Choose Operation",
            "select_operation" to "Select Operation",
            "overall_progress" to "Overall Progress",
            "locked" to "Locked",
            "stage_map_title" to "Select Stage",
            "reset_progress" to "Reset Progress",
            "reset_progress_confirm_title" to "Reset all progress?",
            "reset_progress_confirm_message" to "All stage records will be deleted.",
            "stage_num_format" to "Stage %d",
            "resume_hint" to "%s Stage %d",
            "problem_progress" to "%d / 10",
            "result_title_3star" to "Perfect!",
            "result_title_2star" to "Great job!",
            "result_title_1star" to "You did it!",
            "correct_format" to "Correct: %d / 10",
            "avg_time_format" to "Avg %.1fs",
            "retry" to "Try Again",
            "next_stage" to "Next Stage",
            "to_stage_map" to "Stage Map",
            "star_cond_3" to "⭐⭐⭐  Perfect score + avg ≤ 3s",
            "star_cond_2" to "⭐⭐  8 or more correct",
            "star_cond_1" to "⭐  Finish all 10"
        ),
        AppLanguage.CHINESE_TRADITIONAL to mapOf(
            "reset_records" to "重置紀錄",
            "start" to "開始",
            "best_record" to "最佳紀錄",
            "time_format" to "%.2f秒",
            "light" to "淺色",
            "dark" to "深色",
            "reset_confirm_title" to "確定要重置紀錄嗎？",
            "reset" to "重置",
            "cancel" to "取消",
            "reset_confirm_message" to "最佳紀錄將被刪除。",
            "countdown_go" to "開始！",
            "complete" to "完成！",
            "new_record_title" to "破紀錄了！🎉",
            "new_record_message" to "你完成了全部10題\n並創下新紀錄。恭喜！",
            "well_done" to "辛苦了",
            "best_record_format" to "最佳紀錄 %s",
            "to_main" to "返回主頁",
            "quit_confirm" to "要結束遊戲嗎？",
            "no" to "否",
            "yes" to "是",
            "confirm" to "確認",
            "language" to "語言",
            "level_easy" to "簡單",
            "level_normal" to "普通",
            "help_title" to "難度說明",
            "help_easy_desc" to "• 左側數字：0~99\n• 右側數字：0~10\n• 減法時結果不為負數",
            "help_normal_desc" to "• 左側數字：0~99\n• 右側數字：0~99\n• 加法和減法（可為負數）",
            // ── New strings for stage UX ──
            "resume" to "繼續",
            "new_start" to "選擇運算",
            "select_operation" to "選擇運算",
            "overall_progress" to "整體進度",
            "locked" to "已鎖定",
            "stage_map_title" to "選擇關卡",
            "reset_progress" to "重置進度",
            "reset_progress_confirm_title" to "確定要重置進度嗎？",
            "reset_progress_confirm_message" to "所有關卡記錄將被刪除。",
            "stage_num_format" to "第%d關",
            "resume_hint" to "%s 第%d關",
            "problem_progress" to "%d / 10",
            "result_title_3star" to "太棒了！",
            "result_title_2star" to "做得好！",
            "result_title_1star" to "完成了！",
            "correct_format" to "答對：%d / 10",
            "avg_time_format" to "平均 %.1f秒",
            "retry" to "再試一次",
            "next_stage" to "下一關",
            "to_stage_map" to "關卡地圖",
            "star_cond_3" to "⭐⭐⭐  滿分 + 平均≤3秒",
            "star_cond_2" to "⭐⭐  答對8題以上",
            "star_cond_1" to "⭐  完成全部題目"
        ),
        AppLanguage.CHINESE_SIMPLIFIED to mapOf(
            "reset_records" to "重置记录",
            "start" to "开始",
            "best_record" to "最佳记录",
            "time_format" to "%.2f秒",
            "light" to "浅色",
            "dark" to "深色",
            "reset_confirm_title" to "确定要重置记录吗？",
            "reset" to "重置",
            "cancel" to "取消",
            "reset_confirm_message" to "最佳记录将被删除。",
            "countdown_go" to "开始！",
            "complete" to "完成！",
            "new_record_title" to "破纪录了！🎉",
            "new_record_message" to "你完成了全部10题\n并创下新纪录。恭喜！",
            "well_done" to "辛苦了",
            "best_record_format" to "最佳记录 %s",
            "to_main" to "返回主页",
            "quit_confirm" to "要结束游戏吗？",
            "no" to "否",
            "yes" to "是",
            "confirm" to "确认",
            "language" to "语言",
            "level_easy" to "简单",
            "level_normal" to "普通",
            "help_title" to "难度说明",
            "help_easy_desc" to "• 左侧数字：0~99\n• 右侧数字：0~10\n• 减法时结果不为负数",
            "help_normal_desc" to "• 左侧数字：0~99\n• 右侧数字：0~99\n• 加法和减法（可为负数）",
            // ── New strings for stage UX ──
            "resume" to "继续",
            "new_start" to "选择运算",
            "select_operation" to "选择运算",
            "overall_progress" to "整体进度",
            "locked" to "已锁定",
            "stage_map_title" to "选择关卡",
            "reset_progress" to "重置进度",
            "reset_progress_confirm_title" to "确定要重置进度吗？",
            "reset_progress_confirm_message" to "所有关卡记录将被删除。",
            "stage_num_format" to "第%d关",
            "resume_hint" to "%s 第%d关",
            "problem_progress" to "%d / 10",
            "result_title_3star" to "太棒了！",
            "result_title_2star" to "做得好！",
            "result_title_1star" to "完成了！",
            "correct_format" to "答对：%d / 10",
            "avg_time_format" to "平均 %.1f秒",
            "retry" to "再试一次",
            "next_stage" to "下一关",
            "to_stage_map" to "关卡地图",
            "star_cond_3" to "⭐⭐⭐  满分 + 平均≤3秒",
            "star_cond_2" to "⭐⭐  答对8题以上",
            "star_cond_1" to "⭐  完成全部题目"
        ),
        AppLanguage.JAPANESE to mapOf(
            "reset_records" to "記録をリセット",
            "start" to "スタート",
            "best_record" to "最高記録",
            "time_format" to "%.2f秒",
            "light" to "ライト",
            "dark" to "ダーク",
            "reset_confirm_title" to "記録をリセットしますか？",
            "reset" to "リセット",
            "cancel" to "キャンセル",
            "reset_confirm_message" to "最高記録が削除されます。",
            "countdown_go" to "スタート！",
            "complete" to "完了！",
            "new_record_title" to "新記録達成！🎉",
            "new_record_message" to "10問すべて解いて\n新記録を達成しました。おめでとう！",
            "well_done" to "お疲れ様でした",
            "best_record_format" to "最高記録 %s",
            "to_main" to "メインへ",
            "quit_confirm" to "ゲームを終了しますか？",
            "no" to "いいえ",
            "yes" to "はい",
            "confirm" to "確認",
            "language" to "言語",
            "level_easy" to "かんたん",
            "level_normal" to "ふつう",
            "help_title" to "レベル説明",
            "help_easy_desc" to "• 左の数字：0~99\n• 右の数字：0~10\n• 引き算は結果がマイナスにならない",
            "help_normal_desc" to "• 左の数字：0~99\n• 右の数字：0~99\n• 足し算と引き算（マイナス可）",
            // ── New strings for stage UX ──
            "resume" to "続ける",
            "new_start" to "演算を選ぶ",
            "select_operation" to "演算を選択",
            "overall_progress" to "全体の進捗",
            "locked" to "ロック中",
            "stage_map_title" to "ステージ選択",
            "reset_progress" to "進捗をリセット",
            "reset_progress_confirm_title" to "進捗をリセットしますか？",
            "reset_progress_confirm_message" to "すべてのステージ記録が削除されます。",
            "stage_num_format" to "第%dステージ",
            "resume_hint" to "%s 第%dステージ",
            "problem_progress" to "%d / 10",
            "result_title_3star" to "完璧！",
            "result_title_2star" to "よくできました！",
            "result_title_1star" to "クリア！",
            "correct_format" to "正解: %d / 10",
            "avg_time_format" to "平均 %.1f秒",
            "retry" to "もう一度",
            "next_stage" to "次のステージへ",
            "to_stage_map" to "ステージマップ",
            "star_cond_3" to "⭐⭐⭐  満点 + 平均3秒以内",
            "star_cond_2" to "⭐⭐  8問以上正解",
            "star_cond_1" to "⭐  全問クリア"
        )
    )

    fun string(key: String, lang: AppLanguage, vararg args: Any): String {
        val template = strings[lang]?.get(key) ?: strings[AppLanguage.ENGLISH]!![key] ?: key
        return if (args.isEmpty()) template else kmpFormat(template, *args)
    }
}

/** Multiplatform string formatter — supports %.Nf, %s, and %d specifiers. */
private fun kmpFormat(template: String, vararg args: Any): String {
    val result = StringBuilder()
    var argIndex = 0
    var i = 0
    while (i < template.length) {
        if (template[i] == '%' && i + 1 < template.length) {
            val rest = template.substring(i + 1)
            val floatMatch = Regex("^\\.(\\d+)f").find(rest)
            val stringMatch = rest.startsWith("s")
            val intMatch = rest.startsWith("d")
            when {
                floatMatch != null -> {
                    val decimals = floatMatch.groupValues[1].toInt()
                    val value = (args.getOrNull(argIndex++) as? Number)?.toFloat() ?: 0f
                    result.append(formatFloat(value, decimals))
                    i += 1 + floatMatch.value.length
                }
                stringMatch -> {
                    result.append(args.getOrNull(argIndex++)?.toString() ?: "")
                    i += 2
                }
                intMatch -> {
                    result.append((args.getOrNull(argIndex++) as? Number)?.toInt() ?: 0)
                    i += 2
                }
                else -> {
                    result.append('%')
                    i++
                }
            }
        } else {
            result.append(template[i])
            i++
        }
    }
    return result.toString()
}

private fun formatFloat(value: Float, decimals: Int): String {
    val absValue = if (value < 0f) -value else value
    val sign = if (value < 0f) "-" else ""
    val multiplier = when (decimals) {
        0 -> 1; 1 -> 10; 2 -> 100; 3 -> 1000; else -> 100
    }
    val scaled = (absValue * multiplier + 0.5f).toInt()
    val intPart = scaled / multiplier
    val fracPart = scaled % multiplier
    return if (decimals == 0) "$sign$intPart"
    else "$sign$intPart.${fracPart.toString().padStart(decimals, '0')}"
}
