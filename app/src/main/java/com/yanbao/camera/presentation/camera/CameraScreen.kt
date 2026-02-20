package com.yanbao.camera.presentation.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 相机主界面 - 1:1 还原设计图 + 完整流程和强逻辑
 * 
 * 设计图: 03_camera/01_camera_main.png
 * 
 * 流程逻辑:
 * 1. 权限检查 → 相机初始化 → 显示预览
 * 2. 拍照 → 保存到 MediaStore → 写入 YanbaoMemory 数据库 → 更新相册缩略图
 * 3. 切换摄像头 → 重新绑定相机 → 更新预览
 * 4. 切换闪光灯模式 → 更新 ImageCapture 配置
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    // 状态管理
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var lastPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var isCapturing by remember { mutableStateOf(false) }
    
    val previewView = remember { PreviewView(context) }
    
    // 相机初始化流程
    LaunchedEffect(cameraSelector, flashMode) {
        Log.d("CameraScreen", "开始初始化相机: cameraSelector=$cameraSelector, flashMode=$flashMode")
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // 1. 创建预览
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                // 2. 创建拍照用例
                imageCapture = ImageCapture.Builder()
                    .setFlashMode(flashMode)
                    .build()
                
                // 3. 解绑所有用例
                cameraProvider.unbindAll()
                
                // 4. 绑定用例到生命周期
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                
                Log.d("CameraScreen", "相机初始化成功")
            } catch (e: Exception) {
                Log.e("CameraScreen", "相机初始化失败", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    // 拍照流程
    fun takePhoto() {
        if (isCapturing) {
            Log.w("CameraScreen", "正在拍照中，忽略重复请求")
            return
        }
        
        val capture = imageCapture
        if (capture == null) {
            Log.e("CameraScreen", "ImageCapture 未初始化")
            return
        }
        
        isCapturing = true
        Log.d("CameraScreen", "开始拍照流程")
        
        // 1. 生成文件名
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        
        // 2. 创建 ContentValues
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/YanbaoCamera")
        }
        
        // 3. 创建输出选项
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()
        
        // 4. 执行拍照
        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    Log.d("CameraScreen", "照片已保存: $savedUri")
                    
                    // 5. 更新相册缩略图
                    lastPhotoUri = savedUri
                    
                    // 6. 写入 YanbaoMemory 数据库（TODO: 在后续阶段实现）
                    // saveToYanbaoMemoryDatabase(savedUri, camera29DState)
                    
                    isCapturing = false
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraScreen", "拍照失败: ${exception.message}", exception)
                    isCapturing = false
                }
            }
        )
    }
    
    // 切换摄像头流程
    fun switchCamera() {
        Log.d("CameraScreen", "切换摄像头")
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }
    
    // 切换闪光灯模式流程
    fun toggleFlashMode() {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_AUTO -> {
                Log.d("CameraScreen", "闪光灯: 自动 → 开启")
                ImageCapture.FLASH_MODE_ON
            }
            ImageCapture.FLASH_MODE_ON -> {
                Log.d("CameraScreen", "闪光灯: 开启 → 关闭")
                ImageCapture.FLASH_MODE_OFF
            }
            else -> {
                Log.d("CameraScreen", "闪光灯: 关闭 → 自动")
                ImageCapture.FLASH_MODE_AUTO
            }
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
        // 中央预览区
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.Center)
        ) {
            // 相机预览
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            
            // 四个角落的库洛米装饰
            KuromiDecorations()
        }
        
        // 顶部控制栏
        TopControlBar(
            flashMode = flashMode,
            onFlashModeChange = { toggleFlashMode() },
            onSwitchCamera = { switchCamera() }
        )
        
        // 底部操作栏
        BottomOperationBar(
            lastPhotoUri = lastPhotoUri,
            onTakePhoto = { takePhoto() },
            isCapturing = isCapturing,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * 顶部控制栏
 */
@Composable
fun TopControlBar(
    flashMode: Int,
    onFlashModeChange: () -> Unit,
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
                IconButton(onClick = onFlashModeChange) {
                    Icon(
                        imageVector = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                            else -> Icons.Default.FlashAuto
                        },
                        contentDescription = "闪光灯",
                        tint = Color.White
                    )
                }
                Text(
                    text = when (flashMode) {
                        ImageCapture.FLASH_MODE_AUTO -> "自动"
                        ImageCapture.FLASH_MODE_ON -> "开启"
                        else -> "关闭"
                    },
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
    lastPhotoUri: Uri?,
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
                if (lastPhotoUri != null) {
                    AsyncImage(
                        model = lastPhotoUri,
                        contentDescription = "相册",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    )
                }
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
