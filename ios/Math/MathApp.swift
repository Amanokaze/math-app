import SwiftUI
import Foundation
import UIKit
import shared

private let externalUrlNotificationName = Notification.Name("MathOpenExternalUrl")
private let pendingExternalUrlKey = "MathPendingExternalUrl"

@main
struct MathApp: App {
    init() {
        UserDefaults.standard.set(false, forKey: "MathHardwareKeyboardCaptureEnabled")
        _ = KeyboardInputBridge.shared
        _ = ExternalUrlBridge.shared
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // Google OAuth / Supabase 딥링크 콜백 처리
                    if url.scheme == "com.mathapp.practice" {
                        OAuthCallbackState.shared.pendingUrl = url.absoluteString
                    }
                }
        }
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let composeViewController = IosAppKt.MainViewController()
        return KeyInputContainerViewController(contentViewController: composeViewController)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

final class ExternalUrlBridge {
    static let shared = ExternalUrlBridge()

    private init() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(openPendingUrl),
            name: externalUrlNotificationName,
            object: nil
        )
    }

    @objc private func openPendingUrl() {
        guard
            let urlString = UserDefaults.standard.string(forKey: pendingExternalUrlKey),
            let url = URL(string: urlString)
        else {
            #if DEBUG
            print("[ExternalUrlBridge] Missing or invalid URL")
            #endif
            return
        }

        DispatchQueue.main.async {
            UIApplication.shared.open(url, options: [:]) { success in
                #if DEBUG
                print("[ExternalUrlBridge] open url success=\(success) url=\(urlString)")
                #endif
            }
        }
    }
}
