package com.mathapp.practice

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mathapp.practice.ui.MathApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 폰(sw<600dp): 세로만, 태블릿: 세로/가로 모두 허용
        val swDp = resources.configuration.smallestScreenWidthDp
        requestedOrientation = if (swDp < 600) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }

        enableEdgeToEdge()
        hideSystemBars() // 시스템 바 숨기기 호출

        setContent {
            MathApp()
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // 시스템 바(상태바, 네비게이션바/작업표시줄)를 숨깁니다.
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        // 스와이프했을 때만 잠시 나타나도록 설정합니다.
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    
    // 화면에 포커스가 다시 올 때(앱 복귀 등) 시스템 바를 다시 숨기기 위해 추가
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }
}
