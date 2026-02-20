//
//  ContentView.swift
//  Math
//
//  메인 화면 - 시작 버튼, 최고 기록
//

import SwiftUI

struct ContentView: View {
    @State private var isGameActive = false
    @AppStorage("bestTime") private var bestTime: Double = 0
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 40) {
                Spacer()
                
                Text("Math")
                    .font(.system(size: 48, weight: .bold))
                    .foregroundColor(.primary)
                
                Text("산수 연습")
                    .font(.title2)
                    .foregroundColor(.secondary)
                
                Spacer()
                
                Button(action: {
                    isGameActive = true
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
                
                if bestTime > 0 {
                    VStack(spacing: 8) {
                        Text("최고 기록")
                            .font(.headline)
                            .foregroundColor(.secondary)
                        Text(String(format: "%.2f초", bestTime))
                            .font(.system(size: 28, weight: .bold, design: .monospaced))
                            .foregroundColor(.green)
                    }
                    .padding(.top, 20)
                }
                
                Spacer()
            }
            .navigationDestination(isPresented: $isGameActive) {
                GameView(isPresented: $isGameActive, onComplete: { time in
                    if bestTime == 0 || time < bestTime {
                        bestTime = time
                    }
                })
            }
        }
    }
}

#Preview {
    ContentView()
}
