package com.mathapp.practice.ui

enum class AppLanguage(val code: String, val displayName: String) {
    KOREAN("ko", "한국어"),
    ENGLISH("en", "English"),
    CHINESE_TRADITIONAL("zh-Hant", "中文(繁體)"),
    CHINESE_SIMPLIFIED("zh-Hans", "中文(简体)"),
    JAPANESE("ja", "日本語");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.find { it.code == code } ?: ENGLISH
        fun deviceLanguage(locale: java.util.Locale): AppLanguage {
            val lang = locale.language
            val country = locale.country
            return when {
                lang == "ko" -> KOREAN
                lang == "ja" -> JAPANESE
                lang == "zh" -> if (country == "TW" || country == "HK") CHINESE_TRADITIONAL else CHINESE_SIMPLIFIED
                else -> ENGLISH
            }
        }
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
            "language" to "언어"
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
            "language" to "Language"
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
            "language" to "語言"
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
            "language" to "语言"
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
            "language" to "言語"
        )
    )

    fun string(key: String, lang: AppLanguage, vararg args: Any): String {
        val template = strings[lang]?.get(key) ?: strings[AppLanguage.ENGLISH]!![key] ?: key
        return if (args.isEmpty()) template else String.format(template, *args)
    }
}
