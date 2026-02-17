package com.yanbao.camera.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.components.KuromiPlaceholder
import com.yanbao.camera.ui.theme.GradientEnd
import com.yanbao.camera.ui.theme.GradientMiddle
import com.yanbao.camera.ui.theme.GradientStart
import com.yanbao.camera.ui.theme.ProgressPrimary
import com.yanbao.camera.ui.theme.ProgressSecondary
import com.yanbao.camera.ui.theme.TextWhite
import kotlinx.coroutines.delay

/**
 * Splash 屏幕 - 应用启动画面
 * 
 * 设计规范：
 * - 背景：粉紫渐变（#A78BFA → #EC4899 → #F9A8D4）
 * - 中央：库洛米角色（缩放动画，0.8→1.0，1秒）
 * - 文字：白色"Yanbao Camera"标题（淡入动画）
 * - 进度条：粉色渐变进度条（3秒线性增长）
 * - 自动跳转：3秒后跳转首页
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // 库洛米缩放动画
    val kuromiScale = remember { Animatable(0.8f) }
    
    // 标题透明度动画
    val titleAlpha = remember { Animatable(0f) }
    
    // 进度条进度
    val progressValue = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // 库洛米缩放动画：0.8 → 1.0（1秒）
        kuromiScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
        
        // 标题淡入动画：0 → 1（0.5秒）
        titleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
        
        // 进度条加载动画：0 → 1（3秒）
        progressValue.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
        
        // 3秒后自动跳转
        delay(3000)
        onSplashFinished()
    }
    
    // 粉紫渐变背景
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部空间
            Box(modifier = Modifier.weight(1f))
            
            // 库洛米角色（中央）
            KuromiPlaceholder(
                modifier = Modifier.scale(kuromiScale.value),
                size = 120.dp
            )
            
            // 标题文字
            Text(
                text = "Yanbao Camera",
                color = TextWhite.copy(alpha = titleAlpha.value),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp)
            )
            
            // 底部空间
            Box(modifier = Modifier.weight(1f))
            
            // 进度条
            LinearProgressIndicator(
                progress = progressValue.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(horizontal = 48.dp)
                    .padding(bottom = 48.dp),
                color = ProgressPrimary,
                trackColor = ProgressSecondary.copy(alpha = 0.3f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
