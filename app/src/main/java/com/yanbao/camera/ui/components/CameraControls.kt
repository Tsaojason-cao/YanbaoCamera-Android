package com.yanbao.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.model.CameraMode
import com.yanbao.camera.model.CameraParameters
import com.yanbao.camera.ui.theme.AccentPink
import com.yanbao.camera.ui.theme.ButtonPrimary
import com.yanbao.camera.ui.theme.GlassAlpha
import com.yanbao.camera.ui.theme.GlassWhite
import com.yanbao.camera.ui.theme.ProgressPrimary
import com.yanbao.camera.ui.theme.TextWhite

/**
 * 相机控制面板
 * 
 * 包含模式选择、闪光灯、摄像头切换等控制
 */
@Composable
fun CameraControlPanel(
    currentMode: CameraMode,
    isFlashOn: Boolean,
    isFrontCamera: Boolean,
    parameters: CameraParameters,
    onModeChanged: (CameraMode) -> Unit,
    onFlashToggled: () -> Unit,
    onCameraToggled: () -> Unit,
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 16)
            .padding(16.dp)
    ) {
        // 模式选择
        Text(
            text = "模式",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        ModeSelector(
            currentMode = currentMode,
            onModeSelected = onModeChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        
        // 快速控制按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 闪光灯按钮
            ControlButton(
                icon = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                label = if (isFlashOn) "闪光灯开" else "闪光灯关",
                isActive = isFlashOn,
                onClick = onFlashToggled
            )
            
            // 摄像头切换按钮
            ControlButton(
                icon = Icons.Filled.Cameraswitch,
                label = if (isFrontCamera) "前置" else "后置",
                isActive = isFrontCamera,
                onClick = onCameraToggled
            )
            
            // 设置按钮
            ControlButton(
                icon = Icons.Filled.Settings,
                label = "设置",
                isActive = false,
                onClick = onSettingsClicked
            )
        }
        
        // 参数显示
        ParameterDisplay(parameters = parameters)
    }
}

/**
 * 模式选择器
 */
@Composable
fun ModeSelector(
    currentMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CameraMode.values().forEach { mode ->
            ModeButton(
                mode = mode,
                isSelected = mode == currentMode,
                onClick = { onModeSelected(mode) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 模式按钮
 */
@Composable
fun ModeButton(
    mode: CameraMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isSelected) ButtonPrimary else GlassWhite.copy(alpha = GlassAlpha),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (mode) {
                CameraMode.AUTO -> "自动"
                CameraMode.PORTRAIT -> "人像"
                CameraMode.LANDSCAPE -> "风景"
                CameraMode.NIGHT -> "夜景"
                CameraMode.VIDEO -> "视频"
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) TextWhite else Color.Gray
        )
    }
}

/**
 * 控制按钮
 */
@Composable
fun ControlButton(
    icon: androidx.compose.material.icons.Icons,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    color = if (isActive) AccentPink else GlassWhite.copy(alpha = GlassAlpha),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) TextWhite else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextWhite,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * 参数显示
 */
@Composable
fun ParameterDisplay(
    parameters: CameraParameters,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 12)
            .padding(12.dp)
    ) {
        Text(
            text = "相机参数",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // ISO显示
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "ISO", fontSize = 10.sp, color = Color.Gray)
            Text(
                text = parameters.iso.toString(),
                fontSize = 10.sp,
                color = ProgressPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 白平衡显示
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "白平衡", fontSize = 10.sp, color = Color.Gray)
            Text(
                text = parameters.whiteBalance.name,
                fontSize = 10.sp,
                color = ProgressPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 曝光补偿显示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "曝光", fontSize = 10.sp, color = Color.Gray)
            Text(
                text = "${String.format("%.1f", parameters.exposureCompensation)} EV",
                fontSize = 10.sp,
                color = ProgressPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * ISO调节滑块
 */
@Composable
fun ISOControl(
    iso: Int,
    onISOChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 12)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ISO",
                fontSize = 12.sp,
                color = TextWhite,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = iso.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ProgressPrimary
            )
        }
        
        Slider(
            value = iso.toFloat(),
            onValueChange = { onISOChanged(it.toInt()) },
            modifier = Modifier.fillMaxWidth(),
            valueRange = 100f..6400f,
            steps = 50,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = ProgressPrimary,
                activeTrackColor = ProgressPrimary,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}
