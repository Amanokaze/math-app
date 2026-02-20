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
    @AppStorage("bestTime") private var bestTime: Double = 0
    
    var body: some View {
        NavigationStack {
            ZStack {
                Color(.systemBackground)
                    .ignoresSafeArea()
                VStack(spacing: 40) {
                    // 상단 설정 버튼
                    HStack {
                        Spacer()
                        Menu {
                            Button(role: .destructive, action: {
                                showResetConfirm = true
                            }) {
                                Label("기록 초기화", systemImage: "trash")
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
                    
                    Spacer()
                    
                    Button(action: {
                        showCountdown = true
                    }) {
                        Text("시작")
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
                        Text("최고 기록")
                            .font(.headline)
                            .foregroundColor(.secondary)
                        Text(bestTime > 0 ? String(format: "%.2f초", bestTime) : "-")
                            .font(.system(size: 28, weight: .bold, design: .monospaced))
                            .foregroundColor(bestTime > 0 ? .green : .secondary)
                    }
                    .padding(.top, 20)
                    
                    Spacer()
                    
                    // 라이트/다크 모드 선택 (화면 하단)
                    Picker("", selection: $colorSchemeMode) {
                        Text("라이트").tag(1)
                        Text("다크").tag(2)
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal, 40)
                    .padding(.bottom, 24)
                }
                .onAppear {
                    if colorSchemeMode != 1 && colorSchemeMode != 2 {
                        colorSchemeMode = 1
                    }
                }
                
                // 카운트다운 오버레이
                if showCountdown {
                    CountdownOverlay(onComplete: {
                        showCountdown = false
                        isGameActive = true
                    })
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .confirmationDialog("기록을 초기화하시겠습니까?", isPresented: $showResetConfirm) {
                Button("초기화", role: .destructive) {
                    bestTime = 0
                }
                Button("취소", role: .cancel) { }
            } message: {
                Text("최고 기록이 삭제됩니다.")
            }
            .navigationDestination(isPresented: $isGameActive) {
                GameView(
                    isPresented: $isGameActive,
                    bestTime: bestTime,
                    onComplete: { time in
                        if bestTime == 0 || time < bestTime {
                            bestTime = time
                        }
                    }
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .navigationBarHidden(true)
            }
        }
    }
}

/// 3, 2, 1, 시작! 카운트다운 오버레이
struct CountdownOverlay: View {
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
            return "시작!"
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

#Preview {
    ContentView(colorSchemeMode: .constant(1))
}
