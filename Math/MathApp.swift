//
//  MathApp.swift
//  Math
//
//  산수 연습 앱
//

import SwiftUI

@main
struct MathApp: App {
    @AppStorage("colorSchemeMode") private var colorSchemeMode: Int = 1  // 1: 라이트, 2: 다크
    
    private var preferredScheme: ColorScheme? {
        colorSchemeMode == 2 ? .dark : .light
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView(colorSchemeMode: $colorSchemeMode)
                .preferredColorScheme(preferredScheme)
        }
    }
}
