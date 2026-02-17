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
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.delay

/**
 * Splash 屏幕 - 应用启动画面（稳定版本）
 * 
 * 设计规范：
 * - 背景：粉紫渐变（#A78BFA → #EC4899 → #F9A8D4）
 * - 中央：库洛米角色（缩放动画，0.8→1.0，1秒）
 * - 装饰：金色星星点缀
 * - 文字：白色"Yanbao Camera"标题（淡入动画）
 * - 进度指示：简化的圆形进度指示器
 * - 自动跳转：3秒后跳转首页
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // 库洛米缩放动画
    val kuromiScale = remember { Animatable(0.8f) }
    
    // 标题透明度动画
    val titleAlpha = remember { Animatable(0f) }
    
    // 进度指示器动画
    val progressRotation = remember { Animatable(0f) }
    
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
            
            // 进度指示器旋转动画：0 → 360（3秒）
            progressRotation.animateTo(
                targetValue = 360f,
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
        // 左上星星
        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = 80.dp)
                .size(16.dp)
                .background(
                    color = Color(0xFFFFD700).copy(alpha = 0.8f),
                    shape = CircleShape
                )
        )
        
        // 右上星星
        Box(
            modifier = Modifier
                .offset(x = 320.dp, y = 60.dp)
                .size(20.dp)
                .background(
                    color = Color(0xFFFFD700).copy(alpha = 0.7f),
                    shape = CircleShape
                )
        )
        
        // 左下星星
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = 650.dp)
                .size(14.dp)
                .background(
                    color = Color(0xFFFFD700).copy(alpha = 0.75f),
                    shape = CircleShape
                )
        )
        
        // 右下星星
        Box(
            modifier = Modifier
                .offset(x = 300.dp, y = 680.dp)
                .size(18.dp)
                .background(
                    color = Color(0xFFFFD700).copy(alpha = 0.8f),
                    shape = CircleShape
                )
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
            
            // 库洛米角色 - 中央（毛玻璃效果）
            Box(
                modifier = Modifier
                    .scale(kuromiScale.value)
                    .size(160.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(48.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 库洛米角色 - 使用纯文本代替emoji
                Text(
                    text = "YB",
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 标题文字
            Text(
                text = "Yanbao Camera",
                color = Color.White.copy(alpha = titleAlpha.value),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp)
            )
            
            // 底部空间
            Box(modifier = Modifier.weight(1f))
            
            // 简化的进度指示器 - 使用纯色圆形
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 120.dp)
                    .padding(bottom = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                // 背景圆形
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFFC06FFF).copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
                
                // 前景圆形
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = Color(0xFFEC4899),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
