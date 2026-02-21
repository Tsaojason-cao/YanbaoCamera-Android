package com.yanbao.camera

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 啟動頁 - 完全照抄用戶提供的代碼
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YanbaoSplashScreen {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}

// SplashScreen.kt 1:1 还原代码
@Composable
fun YanbaoSplashScreen(onNext: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "Blink")
    val stars = remember { List(25) { Offset(Random.nextFloat(), Random.nextFloat()) } }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFFFFB6C1), Color(0xFFE0B0FF)))
    )) {
        // 1. 动态闪烁星空 (通电逻辑)
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { pos ->
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f, targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(tween(Random.nextInt(800, 2000)), RepeatMode.Reverse),
                    label = "Alpha"
                )
                drawCircle(Color.White.copy(alpha = alpha), radius = 3f, 
                           center = Offset(pos.x * size.width, pos.y * size.height))
            }
        }
        // 2. 库洛米装饰 (尺寸修正为 160dp)
        Image(
            painter = painterResource(id = R.drawable.kuromi),
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center).size(160.dp).graphicsLayer(alpha = 0.95f)
        )
    }
    LaunchedEffect(Unit) { delay(3000); onNext() }
}
