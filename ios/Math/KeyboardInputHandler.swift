//
//  KeyboardInputHandler.swift
//  Math
//
//  iPad 하드웨어 키보드 입력 처리 (focusable/onKeyPress가 동작하지 않을 때 대체)
//

import Foundation
import GameController

/// iPad에서 하드웨어 키보드가 연결된 경우 FocusState가 설정되지 않아 onKeyPress가 동작하지 않음.
/// GCKeyboard를 사용해 포커스 없이 키 입력을 처리.
final class KeyboardInputHandler: ObservableObject {
    static let shared = KeyboardInputHandler()
    
    var onDigit: ((String) -> Void)?
    var onBackspace: (() -> Void)?
    var onSubmit: (() -> Void)?
    var onMinus: (() -> Void)?
    
    /// 게임 중이고 입력을 받을 수 있는 상태인지 (문제 풀이 중, 팝업 없음)
    var isActive: Bool = false
    
    /// 추가 가드: 그만하기/결과 오버레이 표시 시 입력 무시
    var shouldIgnoreInput: (() -> Bool)?
    
    private var keyboard: GCKeyboard?
    
    private init() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardDidConnect),
            name: .GCKeyboardDidConnect,
            object: nil
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(keyboardDidDisconnect),
            name: .GCKeyboardDidDisconnect,
            object: nil
        )
        // 이미 연결된 키보드 처리 (시뮬레이터 등)
        if let kb = GCKeyboard.coalesced {
            setupKeyboard(kb)
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc private func keyboardDidConnect(_ notification: Notification) {
        guard let keyboard = notification.object as? GCKeyboard else { return }
        setupKeyboard(keyboard)
    }
    
    @objc private func keyboardDidDisconnect(_ notification: Notification) {
        keyboard?.keyboardInput?.keyChangedHandler = nil
        keyboard = nil
    }
    
    private func setupKeyboard(_ kb: GCKeyboard) {
        keyboard = kb
        kb.keyboardInput?.keyChangedHandler = { [weak self] _, _, keyCode, pressed in
            guard let self = self, pressed, self.isActive else { return }
            DispatchQueue.main.async {
                self.handleKey(keyCode)
            }
        }
    }
    
    private func handleKey(_ keyCode: GCKeyCode) {
        guard isActive, shouldIgnoreInput?() != true else { return }
        let digitKeys: [(GCKeyCode, String)] = [
            (.zero, "0"), (.one, "1"), (.two, "2"), (.three, "3"), (.four, "4"),
            (.five, "5"), (.six, "6"), (.seven, "7"), (.eight, "8"), (.nine, "9"),
            (.keypad0, "0"), (.keypad1, "1"), (.keypad2, "2"), (.keypad3, "3"),
            (.keypad4, "4"), (.keypad5, "5"), (.keypad6, "6"), (.keypad7, "7"),
            (.keypad8, "8"), (.keypad9, "9")
        ]
        for (code, char) in digitKeys {
            if keyCode == code {
                onDigit?(char)
                return
            }
        }
        if keyCode == .hyphen || keyCode == .keypadHyphen {
            onMinus?()
            return
        }
        if keyCode == .returnOrEnter || keyCode == .keypadEnter {
            onSubmit?()
            return
        }
        if keyCode == .deleteOrBackspace {
            onBackspace?()
            return
        }
    }
}
