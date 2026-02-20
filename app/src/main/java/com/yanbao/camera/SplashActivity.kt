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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * 启动页 - 粉紫渐变 + 库洛米流光动画
 * 
 * UI 元素：
 * - 背景：垂直渐变（#A78BFA → #EC4899）
 * - 流光动画：旋转的渐变光环
 * - 库洛米角色：4 张 PNG 拼接（kuromi_tl, tr, bl, br）
 * - 黄色星星：6个，闪烁动画
 * - 标题："yanbao AI"，白色粗体
 * - 进度条：粉紫渐变填充
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
                        Color(0xFFA78BFA), // 顶部：紫色
                        Color(0xFFEC4899)  // 底部：粉色
                    )
                )
            )
    ) {
        // 背景光晕球（浮动动画）
        FloatingGlowBalls()
        
        // 流光动画（旋转的渐变光环）
        StreamingLightRing()
        
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
                    .size(300.dp)
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
            
            // 库洛米角色（4 张 PNG 拼接）
            KuromiCharacter()
            
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
 * 库洛米角色（4 张 PNG 拼接）
 */
@Composable
fun BoxScope.KuromiCharacter() {
    // 使用 Box 布局拼接 4 张图片
    Box(
        modifier = Modifier
            .size(200.dp)
            .align(Alignment.Center)
    ) {
        // 左上
        Image(
            painter = painterResource(id = R.drawable.kuromi_tl),
            contentDescription = "Kuromi Top Left",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.TopStart)
        )
        
        // 右上
        Image(
            painter = painterResource(id = R.drawable.kuromi_tr),
            contentDescription = "Kuromi Top Right",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.TopEnd)
        )
        
        // 左下
        Image(
            painter = painterResource(id = R.drawable.kuromi_bl),
            contentDescription = "Kuromi Bottom Left",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomStart)
        )
        
        // 右下
        Image(
            painter = painterResource(id = R.drawable.kuromi_br),
            contentDescription = "Kuromi Bottom Right",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
        )
    }
}

/**
 * 流光动画（旋转的渐变光环）
 */
@Composable
fun StreamingLightRing() {
    val infiniteTransition = rememberInfiniteTransition(label = "streaming_light")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )
    
    // 动态偏移动画
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_offset"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2 + offset)
        val radius = size.minDimension * 0.6f
        
        // 绘制多个旋转的渐变光环
        for (i in 0..3) {
            val angleOffset = i * 90f
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0f),
                        Color(0xFFEC4899).copy(alpha = 0.4f),  // 粉色
                        Color(0xFFA78BFA).copy(alpha = 0.4f),  // 紫色
                        Color.White.copy(alpha = 0f)
                    ),
                    center = center
                ),
                radius = radius - i * 40f,
                center = center
            )
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
                            Color(0xFFA78BFA)  // 紫色
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )
    }
}
