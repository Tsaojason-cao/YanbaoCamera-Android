package com.yanbao.camera

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 启动页 - 完全按照 Cyber-Cute Glass System 设计规范
 * 
 * 视觉元素：
 * - 品牌渐变：LinearGradient(45deg, #A78BFA, #EC4899)
 * - 库洛米装饰：15% 透明度线性轮廓（四角）
 * - 浮动光晕球：6个，白色半透明，带模糊效果
 * - 进度条：粉紫渐变
 * - 3秒加载动画后跳转
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
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            for (i in 0..100) {
                progress = i / 100f
                delay(30)
            }
            delay(500)
            onFinish()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFA78BFA), // 品牌紫
                        Color(0xFFEC4899), // 品牌粉
                        Color(0xFFF9A8D4)  // 浅粉
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // 浮动光晕球（6个）
        FloatingOrbs()
        
        // 库洛米装饰（四角，15% 透明度）
        KuromiCornerDecorations()
        
        // 主内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            
            // 库洛米角色（中心）
            KuromiCharacter()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 标题
            Text(
                text = "Yanbao Camera",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 粉紫渐变进度条
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(8.dp),
                color = Color(0xFFEC4899),
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}

/**
 * 浮动光晕球（6个）
 * 白色半透明，带模糊效果，呼吸动画
 */
@Composable
fun BoxScope.FloatingOrbs() {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    
    val orbs = remember {
        listOf(
            Triple(0.1f, 0.2f, 80.dp),
            Triple(0.8f, 0.15f, 120.dp),
            Triple(0.2f, 0.6f, 100.dp),
            Triple(0.7f, 0.7f, 90.dp),
            Triple(0.5f, 0.3f, 60.dp),
            Triple(0.3f, 0.8f, 70.dp)
        )
    }
    
    orbs.forEachIndexed { index, (x, y, size) ->
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000 + index * 200, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb_scale_$index"
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(align = Alignment.TopStart)
                .offset(x = (x * 400).dp, y = (y * 800).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale)
                    .background(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .blur(20.dp)
            )
        }
    }
}

/**
 * 库洛米角色（中心）
 * 使用 emoji 🐰，周围带光晕效果
 */
@Composable
fun KuromiCharacter() {
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // 背景光晕
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .blur(30.dp)
        )
        
        // 库洛米 emoji
        Text(
            text = "🐰",
            fontSize = 120.sp
        )
    }
}

/**
 * 库洛米装饰（四角，15% 透明度）
 * 使用 Box 布局置于最顶层
 */
@Composable
fun BoxScope.KuromiCornerDecorations() {
    val alpha = 0.15f
    val kuromiEmoji = "🐰"
    val heartEmoji = "💗"
    val starEmoji = "⭐"
    
    // 左上角
    Text(
        text = "$kuromiEmoji$heartEmoji",
        fontSize = 32.sp,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(16.dp)
            .alpha(alpha)
    )
    
    // 右上角
    Text(
        text = "$heartEmoji$kuromiEmoji",
        fontSize = 32.sp,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .alpha(alpha)
    )
    
    // 左下角
    Text(
        text = "$starEmoji$kuromiEmoji$heartEmoji",
        fontSize = 28.sp,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
            .alpha(alpha)
    )
    
    // 右下角
    Text(
        text = "$heartEmoji$kuromiEmoji$starEmoji",
        fontSize = 28.sp,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .alpha(alpha)
    )
}
