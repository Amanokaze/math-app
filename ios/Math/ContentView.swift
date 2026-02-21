//
//  ContentView.swift
//  Math
//
//  메인 화면 - 시작 버튼, 최고 기록, 다크모드 설정
//

import SwiftUI

struct ContentView: View {
    @Binding var colorSchemeMode: Int  // 1: 라이트, 2: 다크
    @State private var isGameActive = false
    @State private var showCountdown = false
    @State private var showResetConfirm = false
    @State private var showLanguageSheet = false
    @State private var showHelpSheet = false
    @State private var selectedLevel: GameLevel = .normal
    @AppStorage("bestTimeEasy") private var bestTimeEasy: Double = 0
    @AppStorage("bestTimeNormal") private var bestTimeNormal: Double = 0
    @AppStorage("appLanguage") private var appLanguageRaw: String = ""
    
    private var bestTime: Double {
        selectedLevel == .easy ? bestTimeEasy : bestTimeNormal
    }
    
    private var appLanguage: AppLanguage {
        get {
            guard !appLanguageRaw.isEmpty, let lang = AppLanguage(rawValue: appLanguageRaw) else {
                return AppLanguage.deviceLanguage
            }
            return lang
        }
        set {
            appLanguageRaw = newValue.rawValue
        }
    }
    
    var body: some View {
        NavigationStack {
            ZStack {
                Color(.systemBackground)
                    .ignoresSafeArea()
                VStack(spacing: 40) {
                    // 상단 설정 버튼
                    HStack {
                        Spacer()
                        Button(action: { showHelpSheet = true }) {
                            Image(systemName: "questionmark.circle")
                                .font(.title2)
                                .foregroundColor(.primary)
                                .frame(width: 44, height: 44)
                        }
                        Menu {
                            Button(action: {
                                showLanguageSheet = true
                            }) {
                                Label(L.string("language", language: appLanguage), systemImage: "globe")
                            }
                            Button(role: .destructive, action: {
                                showResetConfirm = true
                            }) {
                                Label(L.string("reset_records", language: appLanguage), systemImage: "trash")
                            }
                        } label: {
                            Image(systemName: "gearshape.fill")
                                .font(.title2)
                                .foregroundColor(.primary)
                                .frame(width: 44, height: 44)
                        }
                        .padding(.trailing, 8)
                    }
                    .padding(.top, 8)
                    
                    Spacer()
                    
                    Text("Math")
                        .font(.system(size: 48, weight: .bold))
                        .foregroundColor(.primary)
                    
                    Picker("", selection: $selectedLevel) {
                        Text(L.string("level_easy", language: appLanguage)).tag(GameLevel.easy)
                        Text(L.string("level_normal", language: appLanguage)).tag(GameLevel.normal)
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 40)
                    
                    Spacer()
                    
                    Button(action: {
                        showCountdown = true
                    }) {
                        Text(L.string("start", language: appLanguage))
                            .font(.title)
                            .fontWeight(.semibold)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color.blue)
                            .cornerRadius(12)
                    }
                    .padding(.horizontal, 40)
                    
                    VStack(spacing: 8) {
                        Text(L.string("best_record", language: appLanguage))
                            .font(.headline)
                            .foregroundColor(.secondary)
                        Text(bestTime > 0 ? L.string("time_format", language: appLanguage, bestTime) : "-")
                            .font(.system(size: 28, weight: .bold, design: .monospaced))
                            .foregroundColor(bestTime > 0 ? .green : .secondary)
                    }
                    .padding(.top, 20)
                    
                    Spacer()
                    
                    // 라이트/다크 모드 선택 (화면 하단)
                    Picker("", selection: $colorSchemeMode) {
                        Text(L.string("light", language: appLanguage)).tag(1)
                        Text(L.string("dark", language: appLanguage)).tag(2)
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 40)
                    .padding(.bottom, 24)
                }
                .onAppear {
                    if colorSchemeMode != 1 && colorSchemeMode != 2 {
                        colorSchemeMode = 1
                    }
                    if appLanguageRaw.isEmpty {
                        appLanguageRaw = AppLanguage.deviceLanguage.rawValue
                    }
                    if bestTimeNormal == 0 {
                        let legacy = UserDefaults.standard.double(forKey: "bestTime")
                        if legacy > 0 { bestTimeNormal = legacy }
                    }
                }
                
                // 카운트다운 오버레이
                if showCountdown {
                    CountdownOverlay(appLanguage: appLanguage, onComplete: {
                        showCountdown = false
                        isGameActive = true
                    })
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .confirmationDialog(L.string("reset_confirm_title", language: appLanguage), isPresented: $showResetConfirm) {
                Button(L.string("reset", language: appLanguage), role: .destructive) {
                    bestTimeEasy = 0
                    bestTimeNormal = 0
                }
                Button(L.string("cancel", language: appLanguage), role: .cancel) { }
            } message: {
                Text(L.string("reset_confirm_message", language: appLanguage))
            }
            .sheet(isPresented: $showLanguageSheet) {
                LanguagePickerSheet(appLanguageRaw: $appLanguageRaw, isPresented: $showLanguageSheet)
            }
            .sheet(isPresented: $showHelpSheet) {
                HelpSheet(appLanguage: appLanguage, isPresented: $showHelpSheet)
            }
            .navigationDestination(isPresented: $isGameActive) {
                GameView(
                    isPresented: $isGameActive,
                    level: selectedLevel,
                    bestTime: bestTime,
                    appLanguage: appLanguage,
                    onComplete: { time in
                        if selectedLevel == .easy {
                            if bestTimeEasy == 0 || time < bestTimeEasy { bestTimeEasy = time }
                        } else {
                            if bestTimeNormal == 0 || time < bestTimeNormal { bestTimeNormal = time }
                        }
                    }
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .navigationBarHidden(true)
            }
        }
    }
}

/// 언어 선택 시트
struct LanguagePickerSheet: View {
    @Binding var appLanguageRaw: String
    @Binding var isPresented: Bool
    
    private var selectedLanguage: AppLanguage {
        get {
            guard !appLanguageRaw.isEmpty, let lang = AppLanguage(rawValue: appLanguageRaw) else {
                return AppLanguage.deviceLanguage
            }
            return lang
        }
        set {
            appLanguageRaw = newValue.rawValue
        }
    }
    
    var body: some View {
        NavigationStack {
            List(AppLanguage.allCases, id: \.rawValue) { language in
                Button(action: {
                    appLanguageRaw = language.rawValue
                    isPresented = false
                }) {
                    HStack {
                        Text(language.displayName)
                        Spacer()
                        if language == selectedLanguage {
                            Image(systemName: "checkmark")
                                .foregroundColor(.blue)
                        }
                    }
                }
            }
            .navigationTitle(L.string("language", language: selectedLanguage))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(L.string("cancel", language: selectedLanguage)) {
                        isPresented = false
                    }
                }
            }
        }
    }
}

/// 3, 2, 1, 시작! 카운트다운 오버레이
struct CountdownOverlay: View {
    let appLanguage: AppLanguage
    let onComplete: () -> Void
    
    @State private var count = 3
    @State private var scale: CGFloat = 0.5
    @State private var opacity: Double = 0
    
    var body: some View {
        ZStack {
            Color(.systemBackground)
                .ignoresSafeArea()
            
            Text(countdownText)
                .font(.system(size: 72, weight: .bold))
                .foregroundColor(.primary)
                .scaleEffect(scale)
                .opacity(opacity)
        }
        .onAppear {
            runCountdown()
        }
    }
    
    private var countdownText: String {
        if count > 0 {
            return "\(count)"
        } else {
            return L.string("countdown_go", language: appLanguage)
        }
    }
    
    private func runCountdown() {
        animateIn()
        
        var fireCount = 0
        let timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { t in
            fireCount += 1
            if fireCount <= 3 {
                count = 3 - fireCount
                animateIn()
            }
            if fireCount == 3 {
                t.invalidate()
                // "시작!" 표시 후 완료
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                    onComplete()
                }
            }
        }
        RunLoop.main.add(timer, forMode: .common)
    }
    
    private func animateIn() {
        scale = 0.3
        opacity = 0
        withAnimation(.spring(response: 0.4, dampingFraction: 0.6)) {
            scale = 1.2
            opacity = 1
        }
        withAnimation(.easeOut(duration: 0.2).delay(0.3)) {
            scale = 1.0
        }
    }
}

/// 도움말 시트 - Easy/Normal 차이 설명
struct HelpSheet: View {
    let appLanguage: AppLanguage
    @Binding var isPresented: Bool
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(L.string("level_easy", language: appLanguage))
                            .font(.headline)
                        Text(L.string("help_easy_desc", language: appLanguage))
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                    VStack(alignment: .leading, spacing: 8) {
                        Text(L.string("level_normal", language: appLanguage))
                            .font(.headline)
                        Text(L.string("help_normal_desc", language: appLanguage))
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(24)
            }
            .navigationTitle(L.string("help_title", language: appLanguage))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(L.string("cancel", language: appLanguage)) {
                        isPresented = false
                    }
                }
            }
        }
    }
}

#Preview {
    ContentView(colorSchemeMode: .constant(1))
}
