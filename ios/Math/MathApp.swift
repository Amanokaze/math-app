import SwiftUI
import shared

@main
struct MathApp: App {
    init() {
        _ = KeyboardInputBridge.shared
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
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
