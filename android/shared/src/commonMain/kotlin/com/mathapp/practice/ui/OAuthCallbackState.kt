package com.mathapp.practice.ui

// Android onNewIntent / iOS onOpenURL 에서 딥링크 URL 을 여기에 기록합니다.
// MathApp.kt 의 LaunchedEffect 가 500ms 폴링으로 감지 후 처리합니다.
object OAuthCallbackState {
    var pendingUrl: String? = null
}
