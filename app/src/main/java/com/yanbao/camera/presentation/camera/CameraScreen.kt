package com.yanbao.camera.presentation.camera

import android.Manifest
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yanbao.camera.core.model.CameraMode
import com.yanbao.camera.presentation.camera.components.ModeSelector
import com.yanbao.camera.presentation.camera.components.Parameter29DPanel
import com.yanbao.camera.presentation.camera.components.ParameterPanel
import com.yanbao.camera.presentation.camera.components.ShutterButton

/**
 * 相机主界面 - 70/30 布局
 * 
 * 设计规范:
 * - 上部 70%: 相机预览 + yanbao AI 品牌标识
 * - 下部 30%: 毛玻璃操作面板（模式选择 + 参数控制 + 快门按钮）
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel()
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val context = LocalContext.current
    val currentMode by viewModel.currentMode.collectAsState()
    val camera29DState by viewModel.camera29DState.collectAsState()
    val show29DPanel by viewModel.show29DPanel.collectAsState()
    
    // 请求相机权限
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            Log.d("CameraScreen", "请求相机权限")
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    // 权限检查
    if (!cameraPermissionState.status.isGranted) {
        PermissionRequestScreen(
            onRequestPermission = {
                cameraPermissionState.launchPermissionRequest()
            }
        )
        return
    }
    
    // 主界面：70/30 布局
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 上部 70%：相机预览 + 品牌标识
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        ) {
            // 相机预览
            CameraPreviewView(
                onSurfaceTextureAvailable = { surfaceTexture ->
                    viewModel.onSurfaceTextureAvailable(surfaceTexture, context)
                },
                onSurfaceTextureDestroyed = {
                    viewModel.onSurfaceTextureDestroyed()
                }
            )
            
            // yanbao AI 品牌标识
            Text(
                text = "yanbao AI",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            // 实时参数浮窗（显示当前模式）
            Text(
                text = currentMode.displayName,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
        
        // 下部 30%：毛玻璃操作面板
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A).copy(alpha = 0.85f),  // 深色毛玻璃
                            Color(0xFF0D0D0D).copy(alpha = 0.95f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 模式选择器
                ModeSelector(
                    selectedMode = currentMode,
                    onModeSelected = { mode ->
                        viewModel.setMode(mode)
                    }
                )
                
                // 29D 参数控制面板
                if (show29DPanel) {
                    Parameter29DPanel(
                        state = camera29DState,
                        onParameterChange = { name, value ->
                            viewModel.updateParameter(name, value)
                        }
                    )
                }
                
                // 快门按钮
                ShutterButton(
                    onClick = {
                        viewModel.takePhoto(context)
                    },
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }
}

/**
 * 相机预览视图
 */
@Composable
private fun CameraPreviewView(
    onSurfaceTextureAvailable: (SurfaceTexture) -> Unit,
    onSurfaceTextureDestroyed: () -> Unit
) {
    AndroidView(
        factory = { context ->
            TextureView(context).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        Log.d("CameraPreviewView", "SurfaceTexture available: ${width}x${height}")
                        onSurfaceTextureAvailable(surface)
                    }
                    
                    override fun onSurfaceTextureSizeChanged(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        Log.d("CameraPreviewView", "SurfaceTexture size changed: ${width}x${height}")
                    }
                    
                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        Log.d("CameraPreviewView", "SurfaceTexture destroyed")
                        onSurfaceTextureDestroyed()
                        return true
                    }
                    
                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                        // 每帧更新时调用，不记录日志避免刷屏
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
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
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "请授予相机权限以使用相机功能",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            androidx.compose.material3.Button(
                onClick = onRequestPermission
            ) {
                Text("授予权限")
            }
        }
    }
}
