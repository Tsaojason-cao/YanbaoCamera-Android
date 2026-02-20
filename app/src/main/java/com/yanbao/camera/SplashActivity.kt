package com.yanbao.camera

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.yanbao.camera.R

/**
 * 启动页 - 1:1 还原设计图
 * 
 * 设计图: 01_splash/splash_screen.png
 * 
 * UI 元素：
 * - 背景：垂直渐变（深紫 → 紫粉 → 亮粉）
 * - 光晕球：多个白色半透明圆形，大小不一，随机分布
 * - 库洛米角色：黑色耳朵 + 白色身体 + 粉色装饰
 * - 黄色星星：6个，分布在角色周围
 * - 标题："Yanbao Camera"，白色粗体
 * - 进度条：圆角矩形，粉紫渐变填充
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
    val scale by animateFloatAsState(
        targetValue = if (progress > 0f) 1f else 0.8f,
        animationSpec = tween(1000, easing = EaseOutBack),
        label = "logo_scale"
    )
    
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
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8B7FD8), // 顶部：深紫色
                        Color(0xFFB89FE8), // 中部：紫粉混合
                        Color(0xFFF5A8D4)  // 底部：亮粉色
                    )
                )
            )
    ) {
        // 背景光晕球（浮动动画）
        FloatingGlowBalls()
        
        // 中央：库洛米角色 + 星星
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-80).dp)
                .scale(scale)
        ) {
            // 中央白色光晕（角色后方）
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.Center)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // 库洛米角色（使用真实 PNG 图片）
            Image(
                painter = painterResource(id = R.drawable.kuromi),
                contentDescription = "Kuromi",
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.Center)
            )
            
            // 黄色星星（6个，分布在角色周围）
            YellowStars()
        }
        
        // 底部：标题 + 进度条
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Yanbao Camera",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 进度条
            GlassProgressBar(progress = progress)
        }
    }
}

/**
 * 浮动光晕球
 */
@Composable
fun FloatingGlowBalls() {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_balls")
    
    // 生成随机位置的光晕球
    val glowBalls = remember {
        List(15) {
            Triple(
                Random.nextFloat(), // x position (0-1)
                Random.nextFloat(), // y position (0-1)
                Random.nextInt(40, 120) // size (40-120 dp)
            )
        }
    }
    
    // 预先创建所有动画
    val offsets = glowBalls.mapIndexed { index, _ ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 30f,
            animationSpec = infiniteRepeatable(
                animation = tween((2000 + index * 200), easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_offset_$index"
        )
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        glowBalls.forEachIndexed { index, (xRatio, yRatio, sizeDp) ->
            val offset = offsets[index].value
            
            val x = size.width * xRatio
            val y = size.height * yRatio + offset * sin(index.toFloat())
            val radius = sizeDp.dp.toPx()
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0f)
                    ),
                    center = Offset(x, y),
                    radius = radius
                ),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * 黄色星星（6个，分布在角色周围）
 */
@Composable
fun BoxScope.YellowStars() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    
    val starPositions = listOf(
        Offset(-120f, -80f),   // 左上
        Offset(120f, -60f),    // 右上
        Offset(-140f, 40f),    // 左中
        Offset(140f, 60f),     // 右中
        Offset(-80f, 120f),    // 左下
        Offset(100f, 140f)     // 右下
    )
    
    // 预先创建所有动画
    val twinkles = starPositions.mapIndexed { index, _ ->
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween((800 + index * 100), easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "star_twinkle_$index"
        )
    }
    
    starPositions.forEachIndexed { index, offset ->
        Text(
            text = "⭐",
            fontSize = 32.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = offset.x.dp, y = offset.y.dp)
                .scale(twinkles[index].value)
        )
    }
}

/**
 * 玻璃材质进度条
 */
@Composable
fun GlassProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .width(340.dp)
            .height(12.dp)
            .background(
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFEC4899), // 粉色
                            Color(0xFFB89FE8)  // 紫色
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )
    }
}
