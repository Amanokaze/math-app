//
//  Localization.swift
//  Math
//
//  다국어 지원
//

import SwiftUI

enum AppLanguage: String, CaseIterable, Identifiable {
    case korean = "ko"
    case english = "en"
    case chineseTraditional = "zh-Hant"
    case chineseSimplified = "zh-Hans"
    case japanese = "ja"
    
    var id: String { rawValue }
    
    var displayName: String {
        switch self {
        case .korean: return "한국어"
        case .english: return "English"
        case .chineseTraditional: return "中文(繁體)"
        case .chineseSimplified: return "中文(简体)"
        case .japanese: return "日本語"
        }
    }
    
    static func from(localeIdentifier: String) -> AppLanguage {
        let code = localeIdentifier.prefix(2).lowercased()
        if code == "zh" {
            if localeIdentifier.contains("Hant") || localeIdentifier.contains("TW") || localeIdentifier.contains("HK") {
                return .chineseTraditional
            }
            return .chineseSimplified
        }
        switch code {
        case "ko": return .korean
        case "en": return .english
        case "ja": return .japanese
        default: return .english
        }
    }
    
    static var deviceLanguage: AppLanguage {
        from(localeIdentifier: Locale.current.identifier)
    }
}

struct L10n {
    private static let translations: [AppLanguage: [String: String]] = [
        .korean: [
            "reset_records": "기록 초기화",
            "start": "시작",
            "best_record": "최고 기록",
            "time_format": "%.2f초",
            "light": "라이트",
            "dark": "다크",
            "reset_confirm_title": "기록을 초기화하시겠습니까?",
            "reset": "초기화",
            "cancel": "취소",
            "reset_confirm_message": "최고 기록이 삭제됩니다.",
            "countdown_go": "시작!",
            "complete": "완료!",
            "new_record_title": "최고 기록을 경신했어요! 🎉",
            "new_record_message": "10문제를 모두 풀고\n새로운 기록을 세웠어요. 축하해요!",
            "well_done": "수고하셨습니다",
            "best_record_format": "최고 기록 %@",
            "to_main": "메인으로",
            "quit_confirm": "그만하시겠습니까?",
            "no": "아니오",
            "yes": "예",
            "confirm": "확인",
            "language": "언어",
            "level_easy": "쉬움",
            "level_normal": "보통",
            "help_title": "레벨 설명",
            "help_easy_desc": "• 왼쪽 숫자: 0~99\n• 오른쪽 숫자: 0~10\n• 뺄셈 시 결과가 음수가 되지 않음",
            "help_normal_desc": "• 왼쪽 숫자: 0~99\n• 오른쪽 숫자: 0~99\n• 덧셈과 뺄셈 (음수 가능)",
        ],
        .english: [
            "reset_records": "Reset Records",
            "start": "Start",
            "best_record": "Best Record",
            "time_format": "%.2fs",
            "light": "Light",
            "dark": "Dark",
            "reset_confirm_title": "Reset your best record?",
            "reset": "Reset",
            "cancel": "Cancel",
            "reset_confirm_message": "Your best record will be deleted.",
            "countdown_go": "Go!",
            "complete": "Complete!",
            "new_record_title": "New Record! 🎉",
            "new_record_message": "You solved all 10 problems\nand set a new record. Congratulations!",
            "well_done": "Well done!",
            "best_record_format": "Best record %@",
            "to_main": "To Main",
            "quit_confirm": "Quit the game?",
            "no": "No",
            "yes": "Yes",
            "confirm": "Confirm",
            "language": "Language",
            "level_easy": "Easy",
            "level_normal": "Normal",
            "help_title": "Level Guide",
            "help_easy_desc": "• Left number: 0~99\n• Right number: 0~10\n• Subtraction: result is never negative",
            "help_normal_desc": "• Left number: 0~99\n• Right number: 0~99\n• Addition and subtraction (negative allowed)",
        ],
        .chineseTraditional: [
            "reset_records": "重置紀錄",
            "start": "開始",
            "best_record": "最佳紀錄",
            "time_format": "%.2f秒",
            "light": "淺色",
            "dark": "深色",
            "reset_confirm_title": "確定要重置紀錄嗎？",
            "reset": "重置",
            "cancel": "取消",
            "reset_confirm_message": "最佳紀錄將被刪除。",
            "countdown_go": "開始！",
            "complete": "完成！",
            "new_record_title": "破紀錄了！🎉",
            "new_record_message": "你完成了全部10題\n並創下新紀錄。恭喜！",
            "well_done": "辛苦了",
            "best_record_format": "最佳紀錄 %@",
            "to_main": "返回主頁",
            "quit_confirm": "要結束遊戲嗎？",
            "no": "否",
            "yes": "是",
            "confirm": "確認",
            "language": "語言",
            "level_easy": "簡單",
            "level_normal": "普通",
            "help_title": "難度說明",
            "help_easy_desc": "• 左側數字：0~99\n• 右側數字：0~10\n• 減法時結果不為負數",
            "help_normal_desc": "• 左側數字：0~99\n• 右側數字：0~99\n• 加法和減法（可為負數）",
        ],
        .chineseSimplified: [
            "reset_records": "重置记录",
            "start": "开始",
            "best_record": "最佳记录",
            "time_format": "%.2f秒",
            "light": "浅色",
            "dark": "深色",
            "reset_confirm_title": "确定要重置记录吗？",
            "reset": "重置",
            "cancel": "取消",
            "reset_confirm_message": "最佳记录将被删除。",
            "countdown_go": "开始！",
            "complete": "完成！",
            "new_record_title": "破纪录了！🎉",
            "new_record_message": "你完成了全部10题\n并创下新纪录。恭喜！",
            "well_done": "辛苦了",
            "best_record_format": "最佳记录 %@",
            "to_main": "返回主页",
            "quit_confirm": "要结束游戏吗？",
            "no": "否",
            "yes": "是",
            "confirm": "确认",
            "language": "语言",
            "level_easy": "简单",
            "level_normal": "普通",
            "help_title": "难度说明",
            "help_easy_desc": "• 左侧数字：0~99\n• 右侧数字：0~10\n• 减法时结果不为负数",
            "help_normal_desc": "• 左侧数字：0~99\n• 右侧数字：0~99\n• 加法和减法（可为负数）",
        ],
        .japanese: [
            "reset_records": "記録をリセット",
            "start": "スタート",
            "best_record": "最高記録",
            "time_format": "%.2f秒",
            "light": "ライト",
            "dark": "ダーク",
            "reset_confirm_title": "記録をリセットしますか？",
            "reset": "リセット",
            "cancel": "キャンセル",
            "reset_confirm_message": "最高記録が削除されます。",
            "countdown_go": "スタート！",
            "complete": "完了！",
            "new_record_title": "新記録達成！🎉",
            "new_record_message": "10問すべて解いて\n新記録を達成しました。おめでとう！",
            "well_done": "お疲れ様でした",
            "best_record_format": "最高記録 %@",
            "to_main": "メインへ",
            "quit_confirm": "ゲームを終了しますか？",
            "no": "いいえ",
            "yes": "はい",
            "confirm": "確認",
            "language": "言語",
            "level_easy": "かんたん",
            "level_normal": "ふつう",
            "help_title": "レベル説明",
            "help_easy_desc": "• 左の数字：0~99\n• 右の数字：0~10\n• 引き算は結果がマイナスにならない",
            "help_normal_desc": "• 左の数字：0~99\n• 右の数字：0~99\n• 足し算と引き算（マイナス可）",
        ],
    ]
    
    static func string(_ key: String, _ language: AppLanguage, args: [CVarArg] = []) -> String {
        let dict = translations[language] ?? translations[.english]!
        let template = dict[key] ?? key
        if args.isEmpty {
            return template
        }
        return String(format: template, arguments: args)
    }
}

/// 번역 문자열 조회
struct L {
    static func string(_ key: String, language: AppLanguage, _ args: CVarArg...) -> String {
        L10n.string(key, language, args: Array(args))
    }
}
