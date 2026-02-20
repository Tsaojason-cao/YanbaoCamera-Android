package com.yanbao.camera

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * 启动页 - Shader 流光背景版本
 * 
 * 要求：
 * 1. Shader 流光背景（45° 渐变色缓慢位移）
 * 2. 无白边库洛米 PNG
 * 3. 星星呼吸闪烁效果
 * 4. 2 秒后跳转到首页（非相机）
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen {
                // 跳转到首页（非相机）
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
    
    // 2 秒后跳转
    LaunchedEffect(Unit) {
        for (i in 0..100) {
            progress = i / 100f
            delay(20) // 2 秒 = 100 * 20ms
        }
        onFinish()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Shader 流光背景（45° 渐变色缓慢位移）
        ShaderFlowingBackground()
        
        // 中央：库洛米角色 + 星星
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-80).dp)
                .scale(scale)
        ) {
            // 中央白色光晕（角色后方）
            Canvas(modifier = Modifier.size(250.dp).align(Alignment.Center)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0f)
                        )
                    ),
                    radius = size.minDimension / 2
                )
            }
            
            // 库洛米角色（使用真实 PNG 图片，无白边）
            Image(
                painter = painterResource(id = R.drawable.kuromi),
                contentDescription = "Kuromi",
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.Center)
            )
            
            // 黄色星星（6个，呼吸闪烁效果）
            BreathingStars()
        }
        
        // 底部：标题 + 进度条
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "yanbao AI",
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
 * Shader 流光背景（45° 渐变色缓慢位移）
 */
@Composable
fun ShaderFlowingBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "flowing_background")
    
    // 流光位移动画（4 秒一个周期）
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowing_offset"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // 计算 45° 方向的渐变起点和终点
        val angle = Math.toRadians(45.0)
        val diagonal = kotlin.math.sqrt(size.width * size.width + size.height * size.height)
        
        // 流光位移（沿 45° 方向）
        val startX = -diagonal / 2 + offset * cos(angle).toFloat()
        val startY = -diagonal / 2 + offset * sin(angle).toFloat()
        val endX = diagonal / 2 + offset * cos(angle).toFloat()
        val endY = diagonal / 2 + offset * sin(angle).toFloat()
        
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFA78BFA), // 紫色
                    Color(0xFFEC4899), // 粉色
                    Color(0xFFA78BFA), // 紫色
                    Color(0xFFEC4899)  // 粉色
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            )
        )
    }
}

/**
 * 呼吸闪烁星星（6个）
 */
@Composable
fun BoxScope.BreathingStars() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    
    val starPositions = listOf(
        Offset(-120f, -80f),   // 左上
        Offset(120f, -60f),    // 右上
        Offset(-140f, 40f),    // 左中
        Offset(140f, 60f),     // 右中
        Offset(-80f, 120f),    // 左下
        Offset(100f, 140f)     // 右下
    )
    
    // 呼吸闪烁动画（每个星星不同频率）
    val breathingScales = starPositions.mapIndexed { index, _ ->
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween((800 + index * 100), easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "star_breathing_$index"
        )
    }
    
    val breathingAlphas = starPositions.mapIndexed { index, _ ->
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween((800 + index * 100), easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "star_alpha_$index"
        )
    }
    
    starPositions.forEachIndexed { index, offset ->
        Text(
            text = "⭐",
            fontSize = 32.sp,
            color = Color(0xFFFFD700).copy(alpha = breathingAlphas[index].value),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = offset.x.dp, y = offset.y.dp)
                .scale(breathingScales[index].value)
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
    ) {
        // 背景轨道
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
            )
        }
        
        // 进度填充
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
        ) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFEC4899), // 粉色
                        Color(0xFFA78BFA)  // 紫色
                    )
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
            )
        }
    }
}
