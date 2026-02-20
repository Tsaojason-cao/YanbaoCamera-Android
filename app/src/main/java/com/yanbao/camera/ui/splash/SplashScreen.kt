package com.yanbao.camera.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 启动页
 * 严格按照 01_splash/splash_screen.png 设计规格实现：
 * - 粉紫渐变背景 (#A78BFA → #EC4899 → #F9A8D4)
 * - 中央 Logo 缩放动画 (0.8x → 1.0x, 1000ms EaseInOutQuad)
 * - 标题文字淡入动画 (500ms 延迟)
 * - 进度条加载动画 (0% → 100%, 3000ms)
 * - 3秒后自动跳转首页
 */
@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {

    // Logo 缩放动画
    var logoVisible by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.8f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "logoScale"
    )

    // 文字透明度动画
    var textVisible by remember { mutableStateOf(false) }
    val textAlpha by animateFloatAsState(
        targetValue = if (textVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "textAlpha"
    )

    // 进度条动画
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(3000, easing = LinearEasing),
        label = "progress"
    )

    // 光晕脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "halo")
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloScale"
    )

    // 启动动画序列
    LaunchedEffect(Unit) {
        logoVisible = true
        textVisible = true
        progress = 1f
        delay(3000)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA78BFA), // 顶部紫色
                        Color(0xFFEC4899), // 中部粉色
                        Color(0xFFF9A8D4)  // 底部浅粉
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 装饰性白色光晕背景
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(haloScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = haloAlpha),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(150.dp)
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo 区域（库洛米占位符 - 使用文字替代，实际开发时替换为真实图片）
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // 外层光晕
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(140.dp)
                        )
                )
                // 库洛米图标（文字占位符）
                Text(
                    text = "🐰",
                    fontSize = 120.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // 主标题
            Text(
                text = "Yanbao Camera",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = textAlpha),
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 副标题
            Text(
                text = "正在加载资源...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = textAlpha * 0.9f),
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 进度条
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFFEC4899),
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}
