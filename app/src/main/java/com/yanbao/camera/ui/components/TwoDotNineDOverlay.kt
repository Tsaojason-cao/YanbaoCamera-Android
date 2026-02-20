package com.yanbao.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TwoDotNineDParams(
    val colorSense: Float = 20f,
    val contrast: Float = 35f,
    val saturation: Float = 50f,
    val colorTemp: Float = 4500f
)

/**
 * 2.9D 模式叠加层
 * 显示场景选择和参数调节
 */
@Composable
fun TwoDotNineDOverlay(
    modifier: Modifier = Modifier,
    onParametersChanged: (TwoDotNineDParams) -> Unit
) {
    var selectedScene by remember { mutableStateOf("风景") }
    var colorSense by remember { mutableStateOf(20f) }
    var contrast by remember { mutableStateOf(35f) }
    var saturation by remember { mutableStateOf(50f) }
    var colorTemp by remember { mutableStateOf(4500f) }
    
    val scenes = listOf("人像", "风景", "艺术", "复古")
    
    Box(
        modifier = modifier
    ) {
        // 底部控制面板
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(24.dp)
        ) {
            // 场景选择
            Text(
                text = "场景选择",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                items(scenes) { scene ->
                    SceneCard(
                        name = scene,
                        isSelected = scene == selectedScene,
                        onClick = { selectedScene = scene }
                    )
                }
            }
            
            // 参数滑块
            ParameterSlider(
                label = "🎨 颜感",
                value = colorSense,
                valueRange = 0f..100f,
                onValueChange = {
                    colorSense = it
                    onParametersChanged(TwoDotNineDParams(colorSense, contrast, saturation, colorTemp))
                }
            )
            
            ParameterSlider(
                label = "🌓 对比度",
                value = contrast,
                valueRange = 0f..100f,
                onValueChange = {
                    contrast = it
                    onParametersChanged(TwoDotNineDParams(colorSense, contrast, saturation, colorTemp))
                }
            )
            
            ParameterSlider(
                label = "💧 饱和度",
                value = saturation,
                valueRange = 0f..100f,
                onValueChange = {
                    saturation = it
                    onParametersChanged(TwoDotNineDParams(colorSense, contrast, saturation, colorTemp))
                }
            )
            
            ParameterSlider(
                label = "🌡️ 色温",
                value = colorTemp,
                valueRange = 2000f..8000f,
                onValueChange = {
                    colorTemp = it
                    onParametersChanged(TwoDotNineDParams(colorSense, contrast, saturation, colorTemp))
                },
                unit = "K"
            )
        }
    }
}

@Composable
private fun SceneCard(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFA78BFA),
                            Color(0xFFEC4899)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = Color.White
        )
    }
}

@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    unit: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "${value.toInt()}$unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEC4899)
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFFEC4899),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
