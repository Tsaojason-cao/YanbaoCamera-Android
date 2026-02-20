package com.yanbao.camera.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 2.9D 参数调节控制面板
 * 提供景深强度、虚化半径、边缘柔和度三个参数的实时调节
 * 每次滑动都会触发 Log.d 打印当前数值，确保 UI 是"活"的
 */
@Composable
fun TwoDotNineDControls(
    visible: Boolean,
    depthIntensity: Float,
    blurRadius: Float,
    edgeSoftness: Float,
    onDepthIntensityChange: (Float) -> Unit,
    onBlurRadiusChange: (Float) -> Unit,
    onEdgeSoftnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier
    ) {
        // 毛玻璃背景面板
        Column(
            modifier = Modifier
                .width(280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x1AFFFFFF), // 10% 白色透明
                            Color(0x0DFFFFFF)  // 5% 白色透明
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .blur(20.dp) // 毛玻璃模糊效果
                .padding(20.dp)
        ) {
            // 标题
            Text(
                text = "2.9D 参数调节",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 景深强度滑块
            ParameterSlider(
                label = "景深强度",
                value = depthIntensity,
                valueRange = 0f..100f,
                onValueChange = { value ->
                    onDepthIntensityChange(value)
                    Log.d("TwoDotNineD", "景深强度: $value")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 虚化半径滑块
            ParameterSlider(
                label = "虚化半径",
                value = blurRadius,
                valueRange = 0f..50f,
                onValueChange = { value ->
                    onBlurRadiusChange(value)
                    Log.d("TwoDotNineD", "虚化半径: $value")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 边缘柔和度滑块
            ParameterSlider(
                label = "边缘柔和度",
                value = edgeSoftness,
                valueRange = 0f..100f,
                onValueChange = { value ->
                    onEdgeSoftnessChange(value)
                    Log.d("TwoDotNineD", "边缘柔和度: $value")
                }
            )
        }
    }
}

/**
 * 参数滑块组件（带标签和数值显示）
 */
@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        // 标签和当前值
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "${value.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEC4899) // 品牌粉色
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 滑块（使用 mutableStateOf 确保是"活"的）
        var sliderValue by remember { mutableStateOf(value) }
        
        LaunchedEffect(value) {
            sliderValue = value
        }

        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue
                onValueChange(newValue)
            },
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFEC4899), // 粉色滑块
                activeTrackColor = Color(0xFFA78BFA), // 紫色轨道
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
