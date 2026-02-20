package com.yanbao.camera

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 启动页 - Cyber-Cute 旗舰版
 * 
 * UI 规范：
 * - 背景：深紫到亮粉渐变（#6B21A8 → #EC4899）
 * - 核心元素：霓虹光晕库洛米头像
 * - 底部：玻璃材质进度条
 * - 动画：3秒加载后跳转
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        for (i in 0..100) {
            progress = i / 100f
            delay(30)
        }
        onFinish()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6B21A8), // 深紫
                        Color(0xFFEC4899)  // 亮粉
                    )
                )
            )
    ) {
        // 霓虹光晕库洛米头像
        NeonKuromiAvatar(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-50).dp)
        )
        
        // 底部：标题 + 玻璃材质进度条
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Yanbao\nCamera",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 56.sp
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 玻璃材质进度条
            GlassProgressBar(progress = progress)
        }
    }
}

@Composable
fun NeonKuromiAvatar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    
    // 霓虹光晕呼吸动画
    val neonScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_scale"
    )
    
    val neonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_alpha"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 外层：霓虹光晕（3层）
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size((200 + index * 40).dp)
                    .scale(neonScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFEC4899).copy(alpha = neonAlpha * (1 - index * 0.2f)),
                                Color(0xFFEC4899).copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .blur((20 + index * 10).dp)
            )
        }
        
        // 中层：库洛米头像背景圆
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF9A8D4),
                            Color(0xFFEC4899)
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // 内层：库洛米头像（使用 emoji 🐰）
        Text(
            text = "🐰",
            fontSize = 100.sp,
            modifier = Modifier.offset(y = (-10).dp)
        )
    }
}

@Composable
fun GlassProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(8.dp)
            .background(
                color = Color.White.copy(alpha = 0.2f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFA78BFA),
                            Color(0xFFEC4899)
                        )
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
        )
    }
}
