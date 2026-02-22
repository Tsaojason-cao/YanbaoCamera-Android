package com.yanbao.camera.presentation.camera

import android.Manifest
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yanbao.camera.core.camera.Camera2PreviewManager
import com.yanbao.camera.core.utils.ImageSaver
import kotlinx.coroutines.launch
import android.widget.Toast

/**
 * 相机主界面（优化版）
 * 
 * 新增功能：
 * - 前后摄像头切换
 * - 闪光灯控制
 * - 预览尺寸选择
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val previewManager = rememberCamera2PreviewManager()
    
    // 权限检查
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    if (!cameraPermissionState.status.isGranted) {
        PermissionRequestScreen(
            onRequestPermission = {
                cameraPermissionState.launchPermissionRequest()
            }
        )
        return
    }
    
    // 使用真实的Camera2预览
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera2 预览层
        Camera2PreviewView(
            onCaptureClick = {
                scope.launch {
                    val bitmap = previewManager.takePicture()
                    if (bitmap != null) {
                        Log.d("CameraScreen", "Picture taken: ${bitmap.width}x${bitmap.height}")
                        val uri = ImageSaver.saveBitmapToGallery(context, bitmap)
                        if (uri != null) {
                            Toast.makeText(context, "照片已保存到相册", Toast.LENGTH_SHORT).show()
                            Log.d("CameraScreen", "Image saved to: $uri")
                        } else {
                            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                            Log.e("CameraScreen", "Failed to save image")
                        }
                    }
                }
            },
            onPictureTaken = { bitmap ->
                Log.d("CameraScreen", "Picture received: ${bitmap.width}x${bitmap.height}")
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // 顶部品牌标识
        Text(
            text = "yanbao AI",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        )
        
        // 返回按钮
        com.yanbao.camera.presentation.components.CommonTopBar(
            title = "",
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart)
        )
        
        // 相机控制叠加层
        CameraControlsOverlay(
            previewManager = previewManager,
            onSwitchCamera = {
                scope.launch {
                    val success = previewManager.switchCamera()
                    if (success) {
                        Log.d("CameraScreen", "Camera switched successfully")
                    } else {
                        Log.e("CameraScreen", "Failed to switch camera")
                    }
                }
            },
            onFlashModeChange = { mode ->
                previewManager.setFlashMode(mode)
                Log.d("CameraScreen", "Flash mode changed to ${mode.name}")
            },
            onPreviewSizeChange = { size ->
                scope.launch {
                    val success = previewManager.setPreviewSize(size)
                    if (success) {
                        Log.d("CameraScreen", "Preview size changed to ${size.width}x${size.height}")
                    } else {
                        Log.e("CameraScreen", "Failed to change preview size")
                    }
                }
            },
            onCaptureClick = {
                scope.launch {
                    val bitmap = previewManager.takePicture()
                    if (bitmap != null) {
                        Log.d("CameraScreen", "Picture taken: ${bitmap.width}x${bitmap.height}")
                        val uri = ImageSaver.saveBitmapToGallery(context, bitmap)
                        if (uri != null) {
                            Toast.makeText(context, "照片已保存到相册", Toast.LENGTH_SHORT).show()
                            Log.d("CameraScreen", "Image saved to: $uri")
                        } else {
                            Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                            Log.e("CameraScreen", "Failed to save image")
                        }
                    } else {
                        Log.e("CameraScreen", "Failed to take picture")
                    }
                }
            }
        )
    }
}

/**
 * 权限请求界面
 */
@Composable
private fun PermissionRequestScreen(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "需要相机权限",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "请授予相机权限以使用拍照功能",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            androidx.compose.material3.Button(
                onClick = onRequestPermission,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEC4899)
                )
            ) {
                Text(text = "授予权限", color = Color.White)
            }
        }
    }
}
