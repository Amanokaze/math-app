import UIKit
import Foundation

private let hardwareKeyNotificationName = Notification.Name("MathHardwareKeyInput")
private let hardwareKeyCaptureEnabledKey = "MathHardwareKeyboardCaptureEnabled"

/// Captures hardware keyboard keys at the container VC level so shared KMP input
/// can continue even when Compose text focus is lost on iOS.
final class KeyInputContainerViewController: UIViewController {
    private let contentViewController: UIViewController

    init(contentViewController: UIViewController) {
        self.contentViewController = contentViewController
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        addChild(contentViewController)
        contentViewController.view.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(contentViewController.view)
        NSLayoutConstraint.activate([
            contentViewController.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            contentViewController.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            contentViewController.view.topAnchor.constraint(equalTo: view.topAnchor),
            contentViewController.view.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        contentViewController.didMove(toParent: self)
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        becomeFirstResponder()
    }

    override var canBecomeFirstResponder: Bool { true }

    override var keyCommands: [UIKeyCommand]? {
        guard isHardwareKeyCaptureEnabled else { return nil }

        var commands: [UIKeyCommand] = []

        for digit in ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"] {
            commands.append(UIKeyCommand(input: digit, modifierFlags: [], action: #selector(handleKeyCommand(_:))))
        }

        commands.append(UIKeyCommand(input: "-", modifierFlags: [], action: #selector(handleKeyCommand(_:))))
        commands.append(UIKeyCommand(input: "\r", modifierFlags: [], action: #selector(handleKeyCommand(_:))))
        commands.append(UIKeyCommand(input: UIKeyCommand.inputDelete, modifierFlags: [], action: #selector(handleKeyCommand(_:))))

        return commands
    }

    @objc private func handleKeyCommand(_ sender: UIKeyCommand) {
        guard isHardwareKeyCaptureEnabled else { return }
        guard let input = sender.input else { return }
        mapAndPost(input: input)
    }

    override func pressesBegan(_ presses: Set<UIPress>, with event: UIPressesEvent?) {
        guard isHardwareKeyCaptureEnabled else {
            super.pressesBegan(presses, with: event)
            return
        }

        var handled = false

        for press in presses {
            if let key = press.key {
                if key.keyCode == .keyboardDeleteOrBackspace {
                    postKey("backspace")
                    handled = true
                    continue
                }

                let chars = key.charactersIgnoringModifiers
                if mapAndPost(input: chars) {
                    handled = true
                }
            }
        }

        if handled {
            return
        }

        super.pressesBegan(presses, with: event)
    }

    private var isHardwareKeyCaptureEnabled: Bool {
        UserDefaults.standard.bool(forKey: hardwareKeyCaptureEnabledKey)
    }

    @discardableResult
    private func mapAndPost(input: String) -> Bool {
        switch input {
        case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9":
            postKey(input)
            return true
        case "-":
            postKey("minus")
            return true
        case "\r", "\n":
            postKey("submit")
            return true
        case UIKeyCommand.inputDelete:
            postKey("backspace")
            return true
        default:
            return false
        }
    }

    private func postKey(_ key: String) {
        NotificationCenter.default.post(
            name: hardwareKeyNotificationName,
            object: nil,
            userInfo: ["key": key]
        )
        #if DEBUG
        print("[KeyInputContainer] key=\(key)")
        #endif
    }
}
