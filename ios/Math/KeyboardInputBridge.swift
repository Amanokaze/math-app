import Foundation
import GameController

private let hardwareKeyNotificationName = Notification.Name("MathHardwareKeyInput")

/// Broadcasts hardware keyboard keys through NotificationCenter so shared KMP code
/// can react even when Compose focus is not active on iOS.
final class KeyboardInputBridge {
    static let shared = KeyboardInputBridge()

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

        if let coalescedKeyboard = GCKeyboard.coalesced {
            setupKeyboard(coalescedKeyboard)
        }
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
        keyboard?.keyboardInput?.keyChangedHandler = nil
    }

    @objc private func keyboardDidConnect(_ notification: Notification) {
        guard let connectedKeyboard = notification.object as? GCKeyboard else { return }
        setupKeyboard(connectedKeyboard)
    }

    @objc private func keyboardDidDisconnect(_ notification: Notification) {
        keyboard?.keyboardInput?.keyChangedHandler = nil
        keyboard = nil
    }

    private func setupKeyboard(_ newKeyboard: GCKeyboard) {
        keyboard = newKeyboard
        newKeyboard.keyboardInput?.keyChangedHandler = { _, _, keyCode, isPressed in
            guard isPressed else { return }
            self.postKeyIfSupported(keyCode)
        }
    }

    private func postKeyIfSupported(_ keyCode: GCKeyCode) {
        let keyValue: String?

        switch keyCode {
        case .zero, .keypad0: keyValue = "0"
        case .one, .keypad1: keyValue = "1"
        case .two, .keypad2: keyValue = "2"
        case .three, .keypad3: keyValue = "3"
        case .four, .keypad4: keyValue = "4"
        case .five, .keypad5: keyValue = "5"
        case .six, .keypad6: keyValue = "6"
        case .seven, .keypad7: keyValue = "7"
        case .eight, .keypad8: keyValue = "8"
        case .nine, .keypad9: keyValue = "9"
        case .hyphen, .keypadHyphen: keyValue = "minus"
        case .deleteOrBackspace: keyValue = "backspace"
        case .returnOrEnter, .keypadEnter: keyValue = "submit"
        default: keyValue = nil
        }

        guard let value = keyValue else { return }

        DispatchQueue.main.async {
            NotificationCenter.default.post(
                name: hardwareKeyNotificationName,
                object: nil,
                userInfo: ["key": value]
            )
            #if DEBUG
            print("[KeyboardInputBridge] key=\(value)")
            #endif
        }
    }
}
