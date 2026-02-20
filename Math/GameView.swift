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
    let bestTime: Double
    let onComplete: (Double) -> Void
    
    @State private var problems: [MathProblem] = []
    @State private var currentIndex = 0
    @State private var userAnswer = ""
    @State private var showResult: Bool? = nil  // nil: 없음, true: 정답, false: 오답
    @State private var timer: Timer?
    @State private var elapsedTime: Double = 0
    @State private var showQuitAlert = false
    @State private var isTimerPaused = false
    @FocusState private var isGameFocused: Bool
    
    private let totalProblems = 10
    
    var currentProblem: MathProblem? {
        guard currentIndex < problems.count else { return nil }
        return problems[currentIndex]
    }
    
    var body: some View {
        GeometryReader { geometry in
        ZStack {
            Color(.systemBackground)
                .ignoresSafeArea()
            VStack(spacing: 0) {
                // 상단 바: 게임 중에만 표시 (완료 화면에서는 숨김)
                if currentProblem != nil {
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
                }
                
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
                    // 게임 완료 (bestTime==0 또는 기록 경신/동률 시 축하)
                    let isNewRecord = bestTime == 0 || elapsedTime <= bestTime
                    ZStack {
                        VStack(spacing: 28) {
                            Spacer()
                            Text("완료!")
                                .font(.system(size: 38, weight: .bold))
                            if isNewRecord {
                                Text("최고 기록을 경신했어요! 🎉")
                                    .font(.title2)
                                    .fontWeight(.semibold)
                                    .foregroundColor(.green)
                                    .multilineTextAlignment(.center)
                                Text("10문제를 모두 풀고\n새로운 기록을 세웠어요. 축하해요!")
                                    .font(.body)
                                    .foregroundColor(.secondary)
                                    .multilineTextAlignment(.center)
                                    .lineSpacing(4)
                                    .padding(.horizontal, 24)
                            } else {
                                Text("수고하셨습니다")
                                    .font(.title3)
                                    .foregroundColor(.secondary)
                            }
                            Text(String(format: "%.2f초", elapsedTime))
                                .font(.system(size: 32, weight: .semibold, design: .monospaced))
                                .foregroundColor(.green)
                                .padding(.top, 8)
                            if !isNewRecord && bestTime > 0 {
                                Text("최고 기록 \(String(format: "%.2f초", bestTime))")
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                    .padding(.top, 4)
                            }
                            Button("메인으로") {
                                isPresented = false
                            }
                            .font(.headline)
                            .foregroundColor(.white)
                            .padding(.horizontal, 40)
                            .padding(.vertical, 14)
                            .background(Color.blue)
                            .cornerRadius(14)
                            .padding(.top, 32)
                            Spacer()
                        }
                        if isNewRecord {
                            ConfettiView()
                                .ignoresSafeArea()
                                .allowsHitTesting(false)
                        }
                    }
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            
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
        .focusable()
        .focused($isGameFocused)
        .onAppear {
            generateProblems()
            startTimer()
            isGameFocused = true
        }
        .onDisappear {
            timer?.invalidate()
        }
        .onKeyPress(characters: .init(charactersIn: "0123456789")) { press in
            guard currentProblem != nil, !showQuitAlert, showResult == nil else { return .ignored }
            let digit = String(press.characters)
            if userAnswer == "-" || (userAnswer.hasPrefix("-") && userAnswer.count < 5) || (!userAnswer.hasPrefix("-") && userAnswer.count < 4) {
                userAnswer += digit
            }
            return .handled
        }
        .onKeyPress(.return) {
            guard currentProblem != nil, !showQuitAlert, showResult == nil else { return .ignored }
            submitAnswer()
            return .handled
        }
        .onKeyPress(.delete) {
            guard !showQuitAlert, showResult == nil else { return .ignored }
            if !userAnswer.isEmpty {
                userAnswer.removeLast()
            }
            return .handled
        }
        .onKeyPress(characters: .init(charactersIn: "-")) { _ in
            guard currentProblem != nil, !showQuitAlert, showResult == nil else { return .ignored }
            if userAnswer.isEmpty {
                userAnswer = "-"
            }
            return .handled
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
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

/// 꽃가루/축하 파티클 효과
struct ConfettiView: View {
    @State private var particles: [ConfettiParticle] = []
    @State private var startTime: Double = 0
    
    private let particleCount = 80
    private let colors: [Color] = [
        .pink, .orange, .yellow, .green, .mint, .cyan, .blue, .purple, .red
    ]
    
    var body: some View {
        GeometryReader { geometry in
            let size = geometry.size
            TimelineView(.animation(minimumInterval: 1/60)) { timeline in
                ZStack {
                    ForEach(particles) { particle in
                        let pos = particle.position(at: timeline.date.timeIntervalSinceReferenceDate, in: size)
                        Circle()
                            .fill(particle.color.opacity(0.85))
                            .frame(width: particle.size, height: particle.size)
                            .position(pos)
                    }
                }
            }
            .onAppear {
                if particles.isEmpty && size.width > 0 && size.height > 0 {
                    startTime = Date().timeIntervalSinceReferenceDate
                    particles = (0..<particleCount).map { i in
                        ConfettiParticle(
                            startX: CGFloat.random(in: 0...max(1, size.width)),
                            startY: size.height + CGFloat.random(in: 0...100),
                            size: CGFloat.random(in: 8...18),
                            color: colors.randomElement() ?? .yellow,
                            startTime: startTime + Double(i) * 0.05,
                            speed: Double.random(in: 100...220),
                            drift: CGFloat.random(in: -120...120)
                        )
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct ConfettiParticle: Identifiable {
    let id = UUID()
    let startX: CGFloat
    let startY: CGFloat
    let size: CGFloat
    let color: Color
    let startTime: Double
    let speed: Double
    let drift: CGFloat
    
    func position(at time: Double, in size: CGSize) -> CGPoint {
        let elapsed = max(0, time - startTime)
        let y = startY - CGFloat(speed * elapsed)
        let x = startX + CGFloat(drift) * elapsed * 0.25 + sin(elapsed * 2.5) * 25
        return CGPoint(x: x, y: y)
    }
}

#Preview {
    GameView(isPresented: .constant(true), bestTime: 0, onComplete: { _ in })
}
