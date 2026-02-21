package com.mathapp.practice

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        setContent {
            MathApp()
        }
    }
}
