package com.yanbao.camera.ui.screens

import android.Manifest
import android.content.Context
import android.os.Environment
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.yanbao.camera.camera.CameraManager
import com.yanbao.camera.ui.components.KuromiCorners
import com.yanbao.camera.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * CameraScreen - 完整的相机屏幕实现
 * 包含实时预览、拍照、模式选择、参数调节等功能
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreenFinal(
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToGallery: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // 权限状态
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val storagePermissionState = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    
    // 相机管理器
    val cameraManager = remember {
        CameraManager(context, lifecycleOwner)
    }
    
    // 相机预览视图
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    
    // 状态
    var currentMode by remember { mutableStateOf("普通") }
    var flashMode by remember { mutableStateOf(0) } // 0: OFF, 1: ON, 2: AUTO
    var isFrontCamera by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    
    // 初始化相机
    LaunchedEffect(Unit) {
        if (cameraPermissionState.status.isGranted) {
            cameraManager.initializeCamera(previewView)
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            cameraManager.release()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA78BFA),
                        Color(0xFFEC4899),
                        Color(0xFFF9A8D4)
                    )
                )
            )
    ) {
        // 相机预览
        if (cameraPermissionState.status.isGranted) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            )
        } else {
            Text(
                "需要相机权限",
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                fontSize = 18.sp
            )
        }
        
        // 顶部工具栏
        TopCameraToolbar(
            onBack = onNavigateBack,
            onFlashToggle = {
                flashMode = (flashMode + 1) % 3
                val cameraFlashMode = when (flashMode) {
                    0 -> androidx.camera.core.ImageCapture.FLASH_MODE_OFF
                    1 -> androidx.camera.core.ImageCapture.FLASH_MODE_ON
                    else -> androidx.camera.core.ImageCapture.FLASH_MODE_AUTO
                }
                cameraManager.setFlashMode(cameraFlashMode)
            },
            onSwitchCamera = {
                isFrontCamera = !isFrontCamera
                cameraManager.switchCamera(previewView)
            },
            flashMode = flashMode
        )
        
        // 库洛米装饰
        KuromiCorners()
        
        // 底部功能栏
        BottomCameraBar(
            currentMode = currentMode,
            onModeChange = { currentMode = it },
            onTakePhoto = {
                if (storagePermissionState.status.isGranted && cameraPermissionState.status.isGranted) {
                    isCapturing = true
                    val photoFile = createImageFile(context)
                    cameraManager.takePhoto(
                        photoFile,
                        onSuccess = { uri ->
                            isCapturing = false
                            onNavigateToEdit(uri)
                        },
                        onError = {
                            isCapturing = false
                        }
                    )
                } else {
                    storagePermissionState.launchPermissionRequest()
                }
            },
            onGallery = onNavigateToGallery,
            isCapturing = isCapturing
        )
    }
}

/**
 * 顶部工具栏
 */
@Composable
private fun TopCameraToolbar(
    onBack: () -> Unit,
    onFlashToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    flashMode: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = Color.White.copy(alpha = 0.2f)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 模式标签
            Text(
                "自动 翻转 录像",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            // 闪光灯按钮
            IconButton(onClick = onFlashToggle) {
                Icon(
                    when (flashMode) {
                        0 -> Icons.Default.FlashOff
                        1 -> Icons.Default.FlashOn
                        else -> Icons.Default.Brightness6
                    },
                    contentDescription = "Flash",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 切换摄像头按钮
            IconButton(onClick = onSwitchCamera) {
                Icon(
                    Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // 菜单按钮
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 底部功能栏
 */
@Composable
private fun BottomCameraBar(
    currentMode: String,
    onModeChange: (String) -> Unit,
    onTakePhoto: () -> Unit,
    onGallery: () -> Unit,
    isCapturing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFF9A8D4).copy(alpha = 0.9f)
                    )
                )
            )
            .padding(vertical = 24.dp)
    ) {
        // 模式选择
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val modes = listOf("普通", "夜景", "人像", "专业", "视频")
            modes.forEach { mode ->
                ModeButton(
                    mode = mode,
                    isSelected = currentMode == mode,
                    onClick = { onModeChange(mode) }
                )
            }
        }
        
        // 拍照按钮和其他功能
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 相册按钮
            IconButton(onClick = onGallery) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Gallery",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // 拍照按钮
            Button(
                onClick = { if (!isCapturing) onTakePhoto() },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEC4899)
                ),
                enabled = !isCapturing
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = "Take Photo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // 设置按钮
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * 模式按钮
 */
@Composable
private fun ModeButton(
    mode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFEC4899) else Color.White.copy(alpha = 0.3f)
        )
    ) {
        Text(
            mode,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 创建图片文件
 */
private fun createImageFile(context: Context): File {
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "PHOTO_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}",
        ".jpg",
        storageDir
    )
}
