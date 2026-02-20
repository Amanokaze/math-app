//
//  GameView.swift
//  Math
//
//  게임 화면 - 문제, 타이머, 키패드
//

import SwiftUI

enum MathOperation: String, CaseIterable {
    case add = "+"
    case subtract = "-"
}

struct MathProblem {
    let num1: Int
    let num2: Int
    let operation: MathOperation
    
    var answer: Int {
        switch operation {
        case .add: return num1 + num2
        case .subtract: return num1 - num2
        }
    }
    
    var displayText: String {
        "\(num1) \(operation.rawValue) \(num2)"
    }
}

struct GameView: View {
    @Binding var isPresented: Bool
    let onComplete: (Double) -> Void
    
    @State private var problems: [MathProblem] = []
    @State private var currentIndex = 0
    @State private var userAnswer = ""
    @State private var showResult: Bool? = nil  // nil: 없음, true: 정답, false: 오답
    @State private var timer: Timer?
    @State private var elapsedTime: Double = 0
    @State private var showQuitAlert = false
    @State private var isTimerPaused = false
    
    private let totalProblems = 10
    
    var currentProblem: MathProblem? {
        guard currentIndex < problems.count else { return nil }
        return problems[currentIndex]
    }
    
    var body: some View {
        GeometryReader { geometry in
        ZStack {
            VStack(spacing: 0) {
                // 상단 바: 뒤로가기, 타이머, 문제 번호
                HStack {
                    Button(action: {
                        isTimerPaused = true
                        timer?.invalidate()
                        showQuitAlert = true
                    }) {
                        Image(systemName: "chevron.left")
                            .font(.title2)
                            .foregroundColor(.blue)
                            .frame(width: 44, height: 44)
                    }
                    
                    Spacer()
                    
                    Text(String(format: "%.2f초", elapsedTime))
                        .font(.system(size: 20, weight: .medium, design: .monospaced))
                        .foregroundColor(.primary)
                    
                    Spacer()
                    
                    Text("\(currentIndex + 1)/\(totalProblems)")
                        .font(.headline)
                        .foregroundColor(.secondary)
                        .frame(width: 44, alignment: .trailing)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(Color(.systemBackground))
                
                if let problem = currentProblem {
                    // 문제 영역
                    VStack(spacing: 24) {
                        Spacer()
                        
                        Text(problem.displayText)
                            .font(.system(size: 48, weight: .bold, design: .monospaced))
                            .foregroundColor(.primary)
                        
                        // 답 입력 칸
                        Text(userAnswer.isEmpty ? " " : userAnswer)
                            .font(.system(size: 36, weight: .medium, design: .monospaced))
                            .foregroundColor(.primary)
                            .frame(height: 50)
                            .frame(maxWidth: .infinity)
                            .background(Color(.systemGray6))
                            .cornerRadius(12)
                            .padding(.horizontal, 40)
                        
                        Spacer()
                    }
                        
                    // 커스텀 숫자 키패드 (화면 하단 1/3)
                    NumberKeypadView(
                        onDigit: { digit in
                            if userAnswer == "-" || (userAnswer.hasPrefix("-") && userAnswer.count < 5) || (!userAnswer.hasPrefix("-") && userAnswer.count < 4) {
                                userAnswer += digit
                            }
                        },
                        onBackspace: {
                            if !userAnswer.isEmpty {
                                userAnswer.removeLast()
                            }
                        },
                        onSubmit: {
                            submitAnswer()
                        },
                        onMinus: {
                            if userAnswer.isEmpty {
                                userAnswer = "-"
                            }
                        }
                    )
                    .frame(height: geometry.size.height / 3)
                } else {
                    // 게임 완료
                    VStack(spacing: 24) {
                        Spacer()
                        Text("완료!")
                            .font(.system(size: 36, weight: .bold))
                        Text(String(format: "%.2f초", elapsedTime))
                            .font(.system(size: 28, design: .monospaced))
                            .foregroundColor(.green)
                        Button("메인으로") {
                            isPresented = false
                        }
                        .font(.headline)
                        .foregroundColor(.white)
                        .padding(.horizontal, 32)
                        .padding(.vertical, 12)
                        .background(Color.blue)
                        .cornerRadius(12)
                        .padding(.top, 20)
                        Spacer()
                    }
                }
            }
            
            // 정답/오답 오버레이
            if let result = showResult {
                ZStack {
                    Color.black.opacity(0.3)
                        .ignoresSafeArea()
                    
                    Image(systemName: result ? "checkmark.circle.fill" : "xmark.circle.fill")
                        .font(.system(size: 120))
                        .foregroundColor(result ? .green : .red)
                }
                .allowsHitTesting(false)
            }
            
            // 그만하기 팝업 (문제 가림)
            if showQuitAlert {
                Color.black.opacity(0.7)
                    .ignoresSafeArea()
                    .onTapGesture { }
                
                VStack(spacing: 24) {
                    Text("그만하시겠습니까?")
                        .font(.title2)
                        .fontWeight(.semibold)
                        .multilineTextAlignment(.center)
                    
                    HStack(spacing: 16) {
                        Button("아니오") {
                            showQuitAlert = false
                            isTimerPaused = false
                            startTimer()
                        }
                        .font(.headline)
                        .foregroundColor(.blue)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(12)
                        
                        Button("예") {
                            showQuitAlert = false
                            isPresented = false
                        }
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.red)
                        .cornerRadius(12)
                    }
                }
                .padding(32)
                .background(Color(.systemBackground))
                .cornerRadius(20)
                .shadow(radius: 20)
                .padding(40)
            }
        }
        .onAppear {
            generateProblems()
            startTimer()
        }
        .onDisappear {
            timer?.invalidate()
        }
        }
    }
    
    private func generateProblems() {
        problems = (0..<totalProblems).map { _ in
            let num1 = Int.random(in: 0...99)
            let num2 = Int.random(in: 0...99)
            let operation: MathOperation = Bool.random() ? .add : .subtract
            return MathProblem(num1: num1, num2: num2, operation: operation)
        }
    }
    
    private func startTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 0.01, repeats: true) { _ in
            if !isTimerPaused {
                elapsedTime += 0.01
            }
        }
        RunLoop.main.add(timer!, forMode: .common)
    }
    
    private func submitAnswer() {
        guard let problem = currentProblem else { return }
        guard !userAnswer.isEmpty, userAnswer != "-",
              let answer = Int(userAnswer) else { return }
        
        if answer == problem.answer {
            showResult = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                showResult = nil
                currentIndex += 1
                userAnswer = ""
                
                if currentIndex >= totalProblems {
                    timer?.invalidate()
                    onComplete(elapsedTime)
                }
            }
        } else {
            showResult = false
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                showResult = nil
                userAnswer = ""
            }
        }
    }
}

#Preview {
    GameView(isPresented: .constant(true), onComplete: { _ in })
}
