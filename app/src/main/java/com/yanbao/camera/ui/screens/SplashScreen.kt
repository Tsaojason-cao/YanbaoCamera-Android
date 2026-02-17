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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Splash 屏幕 - 应用启动画面（与设计图一致）
 * 
 * 设计规范：
 * - 背景：粉紫渐变（#A78BFA → #EC4899 → #F9A8D4）
 * - 中央：库洛米角色（缩放动画，0.8→1.0，1秒）
 * - 装饰：金色星星和毛玻璃光晕
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
        try {
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
        } catch (e: Exception) {
            // 如果动画出错，直接跳转
            onSplashFinished()
        }
    }
    
    // 粉紫渐变背景
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA78BFA),  // 紫色
                        Color(0xFFEC4899),  // 粉红色
                        Color(0xFFF9A8D4)   // 浅粉色
                    )
                )
            )
    ) {
        // 背景星星装饰 - 左上
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Star",
            tint = Color(0xFFFFD700).copy(alpha = 0.8f),
            modifier = Modifier
                .offset(x = 40.dp, y = 80.dp)
                .size(20.dp)
        )
        
        // 背景星星装饰 - 右上
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Star",
            tint = Color(0xFFFFD700).copy(alpha = 0.7f),
            modifier = Modifier
                .offset(x = 320.dp, y = 60.dp)
                .size(24.dp)
        )
        
        // 背景星星装饰 - 左下
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Star",
            tint = Color(0xFFFFD700).copy(alpha = 0.75f),
            modifier = Modifier
                .offset(x = 60.dp, y = 650.dp)
                .size(18.dp)
        )
        
        // 背景星星装饰 - 右下
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Star",
            tint = Color(0xFFFFD700).copy(alpha = 0.8f),
            modifier = Modifier
                .offset(x = 300.dp, y = 680.dp)
                .size(22.dp)
        )
        
        // 中央内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部空间
            Box(modifier = Modifier.weight(1f))
            
            // 库洛米角色 - 中央
            Box(
                modifier = Modifier
                    .scale(kuromiScale.value)
                    .size(180.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 库洛米角色简化表示
                Text(
                    text = "🎀",
                    fontSize = 80.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // 标题文字
            Text(
                text = "Yanbao Camera",
                color = Color.White.copy(alpha = titleAlpha.value),
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
                color = Color(0xFFEC4899),  // 粉红色
                trackColor = Color(0xFFC06FFF).copy(alpha = 0.3f),  // 紫色
                strokeCap = StrokeCap.Round
            )
        }
    }
}
