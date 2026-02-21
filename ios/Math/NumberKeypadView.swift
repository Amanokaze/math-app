//
//  NumberKeypadView.swift
//  Math
//
//  커스텀 숫자 키패드 - 화면 하단 1/3 크기
//

import SwiftUI

struct NumberKeypadView: View {
    let onDigit: (String) -> Void
    let onBackspace: () -> Void
    let onSubmit: () -> Void
    var onMinus: (() -> Void)? = nil  // 마이너스 (음수 입력용)
    var confirmTitle: String = "확인"
    
    var body: some View {
        VStack(spacing: 8) {
            // 1, 2, 3 (아이폰 숫자패드와 동일한 배치)
            HStack(spacing: 8) {
                ForEach(["1", "2", "3"], id: \.self) { digit in
                    KeypadButton(title: digit) {
                        onDigit(digit)
                    }
                }
            }
            
            // 4, 5, 6
            HStack(spacing: 8) {
                ForEach(["4", "5", "6"], id: \.self) { digit in
                    KeypadButton(title: digit) {
                        onDigit(digit)
                    }
                }
            }
            
            // 7, 8, 9
            HStack(spacing: 8) {
                ForEach(["7", "8", "9"], id: \.self) { digit in
                    KeypadButton(title: digit) {
                        onDigit(digit)
                    }
                }
            }
            
            // -, 0, 백스페이스
            HStack(spacing: 8) {
                KeypadButton(title: "-", isSpecial: true) {
                    if let onMinus = onMinus {
                        onMinus()
                    } else {
                        onDigit("-")
                    }
                }
                
                KeypadButton(title: "0") {
                    onDigit("0")
                }
                
                KeypadButton(title: "⌫", isSpecial: true) {
                    onBackspace()
                }
            }
            
            // 확인 (한 줄 전체)
            KeypadButton(title: confirmTitle, isSpecial: true, isAccent: true) {
                onSubmit()
            }
        }
        .padding(12)
        .background(Color(.systemGray6))
    }
}

struct KeypadButton: View {
    let title: String
    var isSpecial: Bool = false
    var isAccent: Bool = false
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: isSpecial ? 20 : 28, weight: .medium))
                .foregroundColor(isAccent ? .white : .primary)
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .contentShape(Rectangle())
                .background(
                    isAccent ? Color.blue :
                    isSpecial ? Color(.systemGray5) : Color(.systemBackground)
                )
                .cornerRadius(10)
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    NumberKeypadView(
        onDigit: { _ in },
        onBackspace: { },
        onSubmit: { }
    )
    .frame(height: 300)
}
