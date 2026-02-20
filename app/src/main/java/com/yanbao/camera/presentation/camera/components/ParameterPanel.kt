package com.yanbao.camera.presentation.camera.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.core.model.CameraMode

/**
 * 参数控制面板
 * 
 * 根据不同模式显示不同的参数控制
 */
@Composable
fun ParameterPanel(
    currentMode: CameraMode,
    iso: Int,
    onIsoChange: (Int) -> Unit,
    exposureTime: Long,
    onExposureTimeChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (currentMode) {
            CameraMode.PROFESSIONAL -> {
                // 专业模式：显示 ISO 和曝光时间控制
                ParameterSlider(
                    label = "ISO",
                    value = iso.toFloat(),
                    valueRange = 100f..3200f,
                    onValueChange = { onIsoChange(it.toInt()) },
                    valueText = iso.toString()
                )
                
                ParameterSlider(
                    label = "快门",
                    value = exposureTime.toFloat(),
                    valueRange = 1000000f..100000000f,  // 1ms - 100ms
                    onValueChange = { onExposureTimeChange(it.toLong()) },
                    valueText = "${exposureTime / 1000000}ms"
                )
            }
            CameraMode.PORTRAIT -> {
                // 人像模式：显示美颜参数
                Text(
                    text = "人像模式参数调整（即将推出）",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            CameraMode.NIGHT -> {
                // 夜景模式：显示夜景参数
                Text(
                    text = "夜景模式参数调整（即将推出）",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            else -> {
                // 其他模式：暂无参数调整
                Text(
                    text = "当前模式无需参数调整",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 参数滑块
 */
@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueText: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueText,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFE91E63),
                activeTrackColor = Color(0xFFE91E63).copy(alpha = 0.8f),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}
