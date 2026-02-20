package com.yanbao.camera.presentation.camera.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.core.model.CameraMode

/**
 * 相机模式切换器
 * 
 * 横向滚动选择器，显示9个模式
 * 当前模式高亮显示，带呼吸动画
 */
@Composable
fun ModeSwitcher(
    currentMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CameraMode.getAllModes().forEach { mode ->
            ModeItem(
                mode = mode,
                isSelected = mode == currentMode,
                onClick = { onModeSelected(mode) }
            )
        }
    }
}

/**
 * 单个模式项
 */
@Composable
fun ModeItem(
    mode: CameraMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 呼吸动画（仅选中时）
    val infiniteTransition = rememberInfiniteTransition(label = "mode_breathe")
    val scale by infiniteTransition.animateFloat(
        initialValue = if (isSelected) 1.0f else 1.0f,
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mode_scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .scale(scale)
            .padding(horizontal = 8.dp)
    ) {
        // 图标背景
        Box(
            modifier = Modifier
                .size(if (isSelected) 64.dp else 56.dp)
                .background(
                    brush = if (isSelected) {
                        Brush.radialGradient(
                            colors = listOf(
                                mode.color.copy(alpha = 0.8f),
                                mode.color.copy(alpha = 0.4f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mode.icon,
                fontSize = if (isSelected) 32.sp else 28.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 模式名称
        Text(
            text = mode.displayName,
            color = if (isSelected) mode.color else Color.White.copy(alpha = 0.7f),
            fontSize = if (isSelected) 14.sp else 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
