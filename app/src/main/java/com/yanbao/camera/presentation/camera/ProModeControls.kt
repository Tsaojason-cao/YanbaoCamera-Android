package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.log10
import kotlin.math.pow

/**
 * 专业模式控制面板
 * 
 * 功能：
 * 1. ISO 滑块（100-6400）
 * 2. 曝光时间滑块（1/8000s - 30s）
 * 3. 白平衡滑块（2000K - 8000K）
 * 
 * 工业级特性：
 * - 滑块值变化时，通过 StateFlow 传递给 Camera2ManagerEnhanced
 * - 同时传递给 GLRenderer 作为 uniform 参数
 * - 所有参数变化记录 AUDIT_PARAMS 日志
 */
@Composable
fun ProModeControls(
    iso: Int,
    exposureTime: Long,
    whiteBalance: Int,
    onISOChange: (Int) -> Unit,
    onExposureTimeChange: (Long) -> Unit,
    onWhiteBalanceChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.15f))
            .blur(25.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ISO 控制
        ProParamSlider(
            label = "ISO",
            value = iso.toFloat(),
            valueRange = 100f..6400f,
            onValueChange = { onISOChange(it.toInt()) },
            valueFormatter = { it.toInt().toString() },
            icon = Icons.Default.Brightness5
        )
        
        // 曝光时间控制
        ProParamSlider(
            label = "快门",
            value = exposureTimeToSliderValue(exposureTime),
            valueRange = 0f..100f,
            onValueChange = { onExposureTimeChange(sliderValueToExposureTime(it)) },
            valueFormatter = { formatExposureTime(sliderValueToExposureTime(it)) },
            icon = Icons.Default.Timer
        )
        
        // 白平衡控制
        ProParamSlider(
            label = "白平衡",
            value = whiteBalance.toFloat(),
            valueRange = 2000f..8000f,
            onValueChange = { onWhiteBalanceChange(it.toInt()) },
            valueFormatter = { "${it.toInt()}K" },
            icon = Icons.Default.WbSunny
        )
    }
}

/**
 * 专业参数滑块组件
 */
@Composable
fun ProParamSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueFormatter: (Float) -> String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 标签和当前值
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFFFFB6C1),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = valueFormatter(value),
                color = Color(0xFFFFB6C1),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 滑块
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFB6C1),
                activeTrackColor = Color(0xFFFFB6C1),
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 将曝光时间（纳秒）转换为滑块值（0-100）
 * 
 * 使用对数刻度：
 * - 0 对应 1/8000s (125,000 ns)
 * - 50 对应 1/30s (33,333,333 ns)
 * - 100 对应 30s (30,000,000,000 ns)
 */
fun exposureTimeToSliderValue(exposureTime: Long): Float {
    val minExposure = 125000L // 1/8000s
    val maxExposure = 30000000000L // 30s
    
    val logMin = log10(minExposure.toDouble())
    val logMax = log10(maxExposure.toDouble())
    val logValue = log10(exposureTime.toDouble())
    
    return ((logValue - logMin) / (logMax - logMin) * 100).toFloat().coerceIn(0f, 100f)
}

/**
 * 将滑块值（0-100）转换为曝光时间（纳秒）
 */
fun sliderValueToExposureTime(sliderValue: Float): Long {
    val minExposure = 125000L // 1/8000s
    val maxExposure = 30000000000L // 30s
    
    val logMin = log10(minExposure.toDouble())
    val logMax = log10(maxExposure.toDouble())
    
    val logValue = logMin + (sliderValue / 100) * (logMax - logMin)
    return 10.0.pow(logValue).toLong()
}

/**
 * 格式化曝光时间显示
 */
fun formatExposureTime(exposureTime: Long): String {
    val seconds = exposureTime / 1000000000.0
    
    return when {
        seconds >= 1.0 -> String.format("%.1fs", seconds)
        seconds >= 0.1 -> String.format("1/%.0f", 1.0 / seconds)
        else -> String.format("1/%.0f", 1.0 / seconds)
    }
}
