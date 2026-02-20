package com.yanbao.camera.presentation.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.core.model.CameraMode

/**
 * 相机模式选择器
 * 
 * 横向滚动的模式切换器，支持9个相机模式
 */
@Composable
fun ModeSelector(
    selectedMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(CameraMode.values().toList()) { mode ->
            ModeItem(
                mode = mode,
                isSelected = mode == selectedMode,
                onClick = { onModeSelected(mode) }
            )
        }
    }
}

/**
 * 单个模式项
 */
@Composable
private fun ModeItem(
    mode: CameraMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color(0xFFE91E63).copy(alpha = 0.3f)  // 粉色
    } else {
        Color.White.copy(alpha = 0.1f)
    }
    
    val textColor = if (isSelected) {
        Color.White
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mode.icon,
                fontSize = 16.sp
            )
            Text(
                text = mode.displayName,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
