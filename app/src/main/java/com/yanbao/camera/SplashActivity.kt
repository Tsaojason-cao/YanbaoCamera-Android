package com.yanbao.camera

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * 启动頁 - 1:1 还原设计圖 01_splash_screen(2).png
 * 
 * 視覺元素：
 * 1. 粉紫垂直漸變背景 + 流動波紋
 * 2. 顶部 "yanbao AI" 文字
 * 3. 中央库洛米拿相機形象 (300dp)
 * 4. 底部圓角进度條
 * 5. "Loading..." 文字
 * 6. ❌ 沒有星星
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ✅ 设置全屏显示，edge-to-edge
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // ✅ 设置透明状态栏和导航栏
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        
        // ✅ 设置浅色状态栏图标（因为背景是粉紫色）
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        
        setContent {
            YanbaoSplashScreen {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}

@Composable
fun YanbaoSplashScreen(onNext: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    
    // 3 秒後跳轉
    LaunchedEffect(Unit) {
        for (i in 0..100) {
            progress = i / 100f
            delay(30) // 3 秒 = 100 * 30ms
        }
        onNext()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 流動波紋背景
        FlowingWaveBackground()
        
        // 2. 顶部 "yanbao AI" 文字
        Text(
            text = "yanbao AI",
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )
        
        // 3. 中央库洛米拿相機形象
        Image(
            painter = painterResource(id = R.drawable.kuromi),
            contentDescription = "Kuromi with Camera",
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
        )
        
        // 4. 底部进度條 + Loading 文字
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 圓角进度條
            RoundedProgressBar(progress = progress)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Loading 文字
            Text(
                text = "Loading...",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 流動波紋背景
 * 粉紫漸變 + 3-4條流動的白色波紋曲線
 */
@Composable
fun FlowingWaveBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_flow")
    
    // 波紋流動動畫（5 秒一個週期）
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // 1. 粉紫漸變背景
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFB6C1), // 粉色
                    Color(0xFFE0B0FF)  // 紫色
                )
            )
        )
        
        // 2. 繪製 4 條流動波紋曲線
        for (i in 0..3) {
            val path = Path()
            val offsetY = height * (0.2f + i * 0.2f) // 垂直分佈
            val phase = waveOffset * width * 2 + i * width * 0.3f // 流動相位
            
            // 起點
            path.moveTo(-width, offsetY)
            
            // 繪製正弦波
            for (x in 0..width.toInt() step 10) {
                val y = offsetY + sin((x + phase) * 0.01f) * 80f
                path.lineTo(x.toFloat(), y)
            }
            
            // 繪製白色半透明波紋線
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.15f),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * 圓角进度條
 * 白色半透明背景 + 粉紫漸變填充 + 白色邊框
 */
@Composable
fun RoundedProgressBar(progress: Float) {
    val progressWidth = 280.dp
    val progressHeight = 10.dp
    
    Box(
        modifier = Modifier
            .width(progressWidth)
            .height(progressHeight)
    ) {
        // 背景軌道（白色半透明 + 白色邊框）
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerRadius = size.height / 2
            
            // 白色半透明背景
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )
            
            // 白色邊框
            drawRoundRect(
                color = Color.White.copy(alpha = 0.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                style = Stroke(width = 2f)
            )
        }
        
        // 进度填充（粉紫漸變）
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
        ) {
            val cornerRadius = size.height / 2
            
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFB6C1), // 粉色
                        Color(0xFFE0B0FF)  // 紫色
                    )
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius)
            )
        }
    }
}
