package com.yanbao.camera.presentation.camera

import android.Manifest
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yanbao.camera.core.util.Camera2Manager

/**
 * 相机主界面 - Camera2 API 实现
 * 
 * 技术要点:
 * 1. 使用 TextureView 显示 Camera2 预览
 * 2. Camera2Manager 管理相机生命周期
 * 3. 完整的权限处理流程
 * 4. 真实的拍照功能（非占位符）
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            Log.d("CameraScreen", "请求相机权限")
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    if (cameraPermissionState.status.isGranted) {
        Log.d("CameraScreen", "相机权限已授予，显示相机界面")
        CameraContent()
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("需要相机权限才能使用相机功能")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("授予权限")
                }
            }
        }
    }
}

@Composable
fun CameraContent() {
    val context = LocalContext.current
    
    // Camera2Manager 实例
    val camera2Manager = remember { Camera2Manager(context) }
    
    // 状态管理
    var isCapturing by remember { mutableStateOf(false) }
    var lastPhotoUri by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // TextureView 用于显示预览
    val textureView = remember { TextureView(context) }
    
    // 设置 Camera2Manager 回调
    LaunchedEffect(Unit) {
        camera2Manager.onPreviewSurfaceReady = { surface ->
            Log.d("CameraContent", "预览 Surface 已准备")
        }
        
        camera2Manager.onPhotoSaved = { uri ->
            Log.d("CameraContent", "照片已保存: $uri")
            lastPhotoUri = uri
            isCapturing = false
        }
        
        camera2Manager.onError = { error ->
            Log.e("CameraContent", "相机错误: $error")
            errorMessage = error
            isCapturing = false
        }
    }
    
    // TextureView 监听器
    DisposableEffect(textureView) {
        val listener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d("CameraContent", "SurfaceTexture 可用: ${width}x${height}")
                camera2Manager.openCamera(Surface(surface))
            }
            
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d("CameraContent", "SurfaceTexture 尺寸变化: ${width}x${height}")
            }
            
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.d("CameraContent", "SurfaceTexture 销毁")
                camera2Manager.closeCamera()
                return true
            }
            
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // 预览帧更新（频繁调用，不记录日志）
            }
        }
        
        textureView.surfaceTextureListener = listener
        
        onDispose {
            Log.d("CameraContent", "清理资源")
            camera2Manager.closeCamera()
        }
    }
    
    // 拍照函数
    fun takePhoto() {
        if (isCapturing) {
            Log.w("CameraContent", "正在拍照中，忽略重复请求")
            return
        }
        
        isCapturing = true
        Log.d("CameraContent", "开始拍照")
        camera2Manager.takePhoto()
    }
    
    // 切换摄像头函数
    fun switchCamera() {
        Log.d("CameraContent", "切换摄像头")
        camera2Manager.switchCamera()
        
        // 重新打开相机
        val surface = textureView.surfaceTexture
        if (surface != null) {
            camera2Manager.openCamera(Surface(surface))
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5A8D4), // 顶部：粉色
                        Color(0xFFB89FE8)  // 底部：紫色
                    )
                )
            )
    ) {
        // 中央预览区（使用 TextureView 显示 Camera2 预览）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.Center)
        ) {
            // Camera2 预览（TextureView）
            AndroidView(
                factory = { textureView },
                modifier = Modifier.fillMaxSize()
            )
            
            // 四个角落的库洛米装饰
            KuromiDecorations()
        }
        
        // 顶部控制栏
        TopControlBar(
            onSwitchCamera = { switchCamera() }
        )
        
        // 底部操作栏
        BottomOperationBar(
            lastPhotoUri = lastPhotoUri,
            onTakePhoto = { takePhoto() },
            isCapturing = isCapturing,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // 错误提示
        if (errorMessage != null) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("关闭")
                    }
                }
            ) {
                Text(errorMessage ?: "")
            }
        }
    }
}

/**
 * 顶部控制栏
 */
@Composable
fun TopControlBar(
    onSwitchCamera: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            IconButton(onClick = { Log.d("TopControlBar", "点击返回") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            
            // 首页按钮
            IconButton(onClick = { Log.d("TopControlBar", "点击首页") }) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "首页",
                    tint = Color.White
                )
            }
            
            // 闪光灯按钮
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { Log.d("TopControlBar", "点击闪光灯") }) {
                    Icon(
                        imageVector = Icons.Default.FlashAuto,
                        contentDescription = "闪光灯",
                        tint = Color.White
                    )
                }
                Text(
                    text = "自动",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            
            // 翻转摄像头按钮
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onSwitchCamera) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "翻转",
                        tint = Color.White
                    )
                }
                Text(
                    text = "翻转",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            
            // 录像按钮
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { Log.d("TopControlBar", "点击录像") }) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "录像",
                        tint = Color.White
                    )
                }
                Text(
                    text = "录像",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            
            // 库洛米头像
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFEC4899), CircleShape)
                    .clickable { Log.d("TopControlBar", "点击库洛米头像") },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🐰", fontSize = 24.sp)
            }
            
            // 更多按钮
            IconButton(onClick = { Log.d("TopControlBar", "点击更多") }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 四个角落的库洛米装饰
 */
@Composable
fun BoxScope.KuromiDecorations() {
    // 左上角
    Column(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(16.dp)
    ) {
        Text(text = "🐰", fontSize = 32.sp)
        Text(text = "💗", fontSize = 20.sp)
    }
    
    // 右上角
    Column(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
    ) {
        Text(text = "🐰", fontSize = 32.sp)
        Text(text = "💗", fontSize = 20.sp)
        Text(text = "🎀", fontSize = 20.sp)
    }
    
    // 左下角
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
    ) {
        Text(text = "🐰", fontSize = 32.sp)
        Text(text = "💗", fontSize = 20.sp)
        Text(text = "🎀", fontSize = 20.sp)
        Text(text = "⭐", fontSize = 20.sp)
    }
    
    // 右下角
    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    ) {
        Text(text = "🐰", fontSize = 32.sp)
        Text(text = "💗", fontSize = 20.sp)
        Text(text = "🎀", fontSize = 20.sp)
        Text(text = "⭐", fontSize = 20.sp)
    }
}

/**
 * 底部操作栏
 */
@Composable
fun BottomOperationBar(
    lastPhotoUri: String?,
    onTakePhoto: () -> Unit,
    isCapturing: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFF5A8D4), // 左侧：粉色
                        Color(0xFFB89FE8)  // 右侧：紫色
                    )
                )
            )
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 相册缩略图
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { Log.d("BottomOperationBar", "点击相册") }
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "相册",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            
            // 拍照按钮
            ShutterButton(
                onClick = onTakePhoto,
                isCapturing = isCapturing
            )
            
            // 设置按钮
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { Log.d("BottomOperationBar", "点击设置") }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "设置",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 拍照按钮（3层同心圆环 + 粉色圆形背景 + 库洛米头像 + 呼吸动画）
 */
@Composable
fun ShutterButton(
    onClick: () -> Unit,
    isCapturing: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shutter")
    
    // 呼吸动画
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shutter_scale"
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable(enabled = !isCapturing, onClick = onClick)
    ) {
        // 外层3层同心圆环（呼吸动画）
        Canvas(modifier = Modifier.size(140.dp)) {
            repeat(3) { index ->
                val radius = (50 + index * 15).dp.toPx() * scale
                drawCircle(
                    color = Color(0xFFEC4899).copy(alpha = 0.3f - index * 0.1f),
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        
        // 中层粉色圆形背景
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = if (isCapturing) Color(0xFFB89FE8) else Color(0xFFEC4899),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // 内层库洛米头像
            Text(
                text = if (isCapturing) "⏳" else "🐰",
                fontSize = 48.sp
            )
        }
    }
}
