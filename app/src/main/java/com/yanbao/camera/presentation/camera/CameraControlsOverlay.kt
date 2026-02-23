package com.yanbao.camera.presentation.camera

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.core.camera.Camera2PreviewManager
import androidx.compose.foundation.layout.fillMaxSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 相机控制叠加层
 * 
 * 功能：
 * - 前后摄像头切换
 * - 闪光灯控制（自动/开/关）
 * - 预览尺寸选择
 * - 拍照按钮
 */
@Composable
fun CameraControlsOverlay(
    previewManager: Camera2PreviewManager,
    onSwitchCamera: () -> Unit,
    onFlashModeChange: (Camera2PreviewManager.FlashMode) -> Unit,
    onPreviewSizeChange: (android.util.Size) -> Unit,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit = { android.util.Log.d("CameraControlsOverlay", "相册导航") },
    modifier: Modifier = Modifier
) {
    val currentFlashMode by remember { mutableStateOf(previewManager.getCurrentFlashMode()) }
    val currentPreviewSize by remember { mutableStateOf(previewManager.getCurrentPreviewSize()) }
    val hasFrontCamera = previewManager.hasFrontCamera()
    val hasFlash = previewManager.hasFlash()
    
    Box(modifier = modifier.fillMaxSize()) {
        // 顶部控制栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 闪光灯控制
            if (hasFlash) {
                FlashModeButton(
                    currentMode = currentFlashMode,
                    onModeChange = onFlashModeChange
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            // 预览尺寸选择
            PreviewSizeButton(
                currentSize = currentPreviewSize,
                onSizeChange = onPreviewSizeChange
            )
        }
        
        // 底部控制栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 相册按钮（真实点击事件）
            IconButton(
                onClick = {
                    android.util.Log.d("CameraControlsOverlay", "相册入口点击")
                    onGalleryClick()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = com.yanbao.camera.R.drawable.ic_album_kuromi),
                    contentDescription = "Gallery",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // 拍照按钮
            ShutterButton(onClick = onCaptureClick)
            
            // 切换摄像头按钮
            if (hasFrontCamera) {
                IconButton(
                    onClick = onSwitchCamera,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

/**
 * 闪光灯模式按钮
 */
@Composable
private fun FlashModeButton(
    currentMode: Camera2PreviewManager.FlashMode,
    onModeChange: (Camera2PreviewManager.FlashMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        // 当前模式按钮
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = when (currentMode) {
                    Camera2PreviewManager.FlashMode.AUTO -> Icons.Default.FlashAuto
                    Camera2PreviewManager.FlashMode.ON -> Icons.Default.FlashOn
                    Camera2PreviewManager.FlashMode.OFF -> Icons.Default.FlashOff
                },
                contentDescription = "Flash Mode",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 下拉菜单
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.Black.copy(alpha = 0.8f))
        ) {
            Camera2PreviewManager.FlashMode.values().forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    Camera2PreviewManager.FlashMode.AUTO -> Icons.Default.FlashAuto
                                    Camera2PreviewManager.FlashMode.ON -> Icons.Default.FlashOn
                                    Camera2PreviewManager.FlashMode.OFF -> Icons.Default.FlashOff
                                },
                                contentDescription = null,
                                tint = if (mode == currentMode) Color(0xFFEC4899) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = mode.name,
                                color = if (mode == currentMode) Color(0xFFEC4899) else Color.White,
                                fontSize = 14.sp
                            )
                        }
                    },
                    onClick = {
                        onModeChange(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 预览尺寸按钮
 */
@Composable
private fun PreviewSizeButton(
    currentSize: android.util.Size,
    onSizeChange: (android.util.Size) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        // 当前尺寸按钮
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black.copy(alpha = 0.3f)),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(
                text = "${currentSize.width}x${currentSize.height}",
                color = Color.White,
                fontSize = 12.sp
            )
        }
        
        // 下拉菜单
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.Black.copy(alpha = 0.8f))
        ) {
            Camera2PreviewManager.PREVIEW_SIZES.forEach { size ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${size.width}x${size.height}",
                            color = if (size == currentSize) Color(0xFFEC4899) else Color.White,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onSizeChange(size)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 快门按钮
 */
@Composable
private fun ShutterButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color.White)
            .clickable {
                isPressed = true
                onClick()
                // 延迟恢复
                kotlinx.coroutines.GlobalScope.launch {
                    kotlinx.coroutines.delay(100)
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color(0xFFEC4899))
            )
        }
    }
}
