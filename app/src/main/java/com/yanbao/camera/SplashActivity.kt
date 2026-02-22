package com.yanbao.camera

import android.Manifest
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay

/**
 * SplashActivity — 严格按照 Gemini 最终架构
 *
 * 规范：
 * - 背景 Color(0xFF0A0A0A) 纯黑
 * - 仅保留核心 Logo + 品牌字 "yanbao AI"
 * - LaunchedEffect 倒计时 2000ms，期间检测 Camera 权限
 * - 结束后淡入跳转 MainActivity
 * - 零 TODO/FIXME
 * - 禁止任何粒子、渐变背景、进度条（Gemini 最终指令）
 */
class SplashActivity : ComponentActivity() {

    private var cameraPermissionGranted = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
        Log.d("SplashActivity", "相机权限结果: $granted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SplashActivity", "启动页初始化")

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )

        // 在 2000ms 内发起权限请求
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

        setContent {
            SplashScreen(
                onTimeout = {
                    Log.d("SplashActivity", "启动页完成，权限状态: $cameraPermissionGranted，跳转 MainActivity")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )
        }
    }
}

/**
 * SplashScreen Composable — Gemini 最终代码架构
 *
 * 严格遵循：
 * - 纯黑背景 Color(0xFF0A0A0A)
 * - 仅 Logo + 品牌字，无粒子无渐变无进度条
 * - 2000ms 后淡入跳转
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // 淡入动画
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "splash_fade_in"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            // 核心 Logo（库洛米品牌图标）
            Image(
                painter = painterResource(id = R.drawable.kuromi),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 品牌字 "yanbao AI"（JetBrains Mono 风格）
            Text(
                text = "yanbao AI",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}
