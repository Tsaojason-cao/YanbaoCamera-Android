package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.theme.KUROMI_PINK

/**
 * 模式选择器 — 严格按照 ui_main_camera.png 还原
 *
 * 布局：2行 × 5列 网格
 *  第1行：记忆 | 大师 | [29D 选中胶囊] | 2.9D | 美颜
 *  第2行：美颜 | 视频 | 基本 | 原相机 | AR
 *
 * 选中项：粉色圆角胶囊背景 + 白色文字 + 图标
 * 未选中：透明背景 + 灰色图标 + 灰色文字
 */
@Composable
fun ModeSelectorRow(
    modes: List<CameraMode>,
    selectedMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit
) {
    val row1 = modes.take(5)
    val row2 = modes.drop(5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row1.forEach { mode ->
                ModeItem(
                    mode = mode,
                    isSelected = mode == selectedMode,
                    onClick = { onModeSelected(mode) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row2.forEach { mode ->
                ModeItem(
                    mode = mode,
                    isSelected = mode == selectedMode,
                    onClick = { onModeSelected(mode) }
                )
            }
        }
    }
}

@Composable
private fun ModeItem(
    mode: CameraMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) KUROMI_PINK else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(mode.iconRes),
                contentDescription = mode.displayName,
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = mode.displayName,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}
