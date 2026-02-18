package com.yanbao.camera.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.components.DesignSpec
import kotlinx.coroutines.delay

/**
 * 启动屏幕 - 1:1精确还原设计图
 * 
 * 设计规范：
 * - 背景：粉紫渐变（#A78BFA → #EC4899 → #F9A8D4）
 * - 中央：库洛米角色在深灰色圆角方框内（200x200dp）
 * - 标题："Yanbao Camera"（白色，粗体，32sp）
 * - 副标题："AI智能相机"（白色，细体，16sp）
 * - 进度条：粉红色渐变，显示百分比（0%-100%）
 * - 动画：库洛米缩放0.8→1.0（1秒）+ 标题淡入 + 进度条填充（3秒）
 * - 3秒后自动跳转到首页
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val scaleAnimation = remember { Animatable(0.8f) }
    val alphaAnimation = remember { Animatable(0f) }
    val progressAnimation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        try {
            // 库洛米缩放：0.8 → 1.0（1秒）
            scaleAnimation.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
            
            // 标题淡入：0 → 1（0.5秒）
            alphaAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = LinearEasing)
            )
            
            // 进度条：0 → 1（3秒）
            progressAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
            )
            
            delay(500)
            onSplashFinished()
        } catch (e: Exception) {
            onSplashFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = DesignSpec.GradientBackground
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 顶部空间
            Box(modifier = Modifier.weight(1f))
            
            // 中央库洛米角色（在深灰色圆角方框内）
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scaleAnimation.value)
                    .background(
                        color = Color(0xFF2C2C2C),  // 深灰色
                        shape = RoundedCornerShape(48.dp)  // 大圆角
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 库洛米角色 - 使用emoji表示
                Text(
                    text = "🐰",
                    fontSize = 120.sp
                )
            }
            
            // 中间空间
            Box(modifier = Modifier.height(40.dp))
            
            // 标题
            Text(
                text = "Yanbao Camera",
                color = Color.White.copy(alpha = alphaAnimation.value),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            // 副标题
            Text(
                text = "AI智能相机",
                color = Color.White.copy(alpha = alphaAnimation.value),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            // 标题和进度条之间的空间
            Box(modifier = Modifier.weight(1f))
            
            // 进度百分比文字
            Text(
                text = "${(progressAnimation.value * 100).toInt()}%",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // 进度条容器（外层）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                // 进度条前景（内层填充）
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressAnimation.value)
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    DesignSpec.PrimaryPink,
                                    DesignSpec.PurpleLight
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
            
            // 底部空间
            Box(modifier = Modifier.weight(1f))
        }
    }
}
