package com.yanbao.camera.presentation.camera.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 美颜滑块组件（1:1 还原图 42）
 * 
 * 核心特性：
 * - 实时数字显示（+15 发光特效）
 * - 粉紫流光渐变描边
 * - 滑动时数字实时更新
 * - 硬件级参数下发
 */
@Composable
fun BeautySliderWithValue(
    label: String,
    emoji: String,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { mutableStateOf(initialValue.toFloat()) }
    
    // 发光动画
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(160.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFE0B0FF).copy(alpha = glowAlpha), // 紫
                        Color(0xFFFFB6C1).copy(alpha = glowAlpha)  // 粉
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // 顶部 Emoji 图标
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 核心：显示图 42 同款的 +15 数字标识（带发光特效）
        Text(
            text = if (sliderValue > 0) "+${sliderValue.toInt()}" else "${sliderValue.toInt()}",
            color = Color(0xFFFFB6C1).copy(alpha = glowAlpha), // 雁宝粉 + 发光
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(4.dp),
                    ambientColor = Color(0xFFFFB6C1),
                    spotColor = Color(0xFFFFB6C1)
                )
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 滑块
        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                onValueChange(it.toInt())
            },
            valueRange = -100f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFFFFB6C1),
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 底部标签
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 美颜参数数据类
 */
data class BeautyParams(
    val smoothness: Int = 0,      // 磨皮 (-100 ~ +100)
    val whitening: Int = 0,       // 美白 (-100 ~ +100)
    val blemishRemoval: Int = 0,  // 祛斑 (-100 ~ +100)
    val sharpness: Int = 0        // 锐化 (-100 ~ +100)
)
