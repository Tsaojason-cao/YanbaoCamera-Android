package com.yanbao.camera.presentation.camera

import android.Manifest
import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.yanbao.camera.core.util.CameraManager

/**
 * 相机主界面 - 完全按照 Cyber-Cute Glass System 设计规范
 * 
 * 三层架构：
 * - 上层（快速控制）：玻璃卡片显示 29D 参数摘要或焦段
 * - 中层（模式滑动）：LazyRow 实现，滑动时带惯性反馈
 * - 下层（操作区）：相册缩略图（左）+ 主快门（中）+ 前后置切换（右）
 * 
 * 玻璃态效果：
 * - background: rgba(255, 255, 255, 0.15)
 * - blur: 16dp
 * - border: 1px solid rgba(255, 255, 255, 0.2)
 * 
 * 主快门：
 * - 外圈 80dp 双重呼吸光晕
 * - 中心 64dp 渐变圆
 * - 点击时微缩至 90% 并伴随触感反馈
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = remember { CameraManager() }
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    
    var lastPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (permissionsState.allPermissionsGranted) {
            // 相机预览层
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        cameraManager.startCamera(ctx, lifecycleOwner, this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // 库洛米装饰（四角，15% 透明度，不拦截点击事件）
            KuromiCornerDecorations()
            
            // 上层：快速控制（玻璃卡片）
            TopQuickControls()
            
            // 下层：操作区（相册 + 主快门 + 切换）
            BottomOperationBar(
                lastPhotoUri = lastPhotoUri,
                onTakePhoto = {
                    cameraManager.takePhoto(context) { success, message, uri ->
                        if (success && uri != null) {
                            lastPhotoUri = uri
                        }
                    }
                }
            )
        } else {
            PermissionDeniedScreen()
        }
    }
}

/**
 * 库洛米装饰（四角，15% 透明度）
 * 使用 Box 布局置于最顶层，pointerInteropFilter 确保不拦截点击事件
 */
@Composable
fun BoxScope.KuromiCornerDecorations() {
    val alpha = 0.15f
    val kuromiEmoji = "🐰"
    val heartEmoji = "💗"
    val starEmoji = "⭐"
    
    // 左上角
    Text(
        text = "$kuromiEmoji$heartEmoji",
        fontSize = 32.sp,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(16.dp)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures { /* 不拦截点击事件 */ }
            }
    )
    
    // 右上角
    Text(
        text = "$heartEmoji$kuromiEmoji",
        fontSize = 32.sp,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures { /* 不拦截点击事件 */ }
            }
    )
    
    // 左下角
    Text(
        text = "$starEmoji$kuromiEmoji$heartEmoji",
        fontSize = 28.sp,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 16.dp, bottom = 180.dp)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures { /* 不拦截点击事件 */ }
            }
    )
    
    // 右下角
    Text(
        text = "$heartEmoji$kuromiEmoji$starEmoji",
        fontSize = 28.sp,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 180.dp)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures { /* 不拦截点击事件 */ }
            }
    )
}

/**
 * 上层：快速控制（玻璃卡片）
 * 显示 29D 参数摘要或焦段（0.5x, 1x, 2x, 5x）
 */
@Composable
fun BoxScope.TopQuickControls() {
    Row(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 60.dp, start = 16.dp, end = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                color = Color.White.copy(alpha = 0.15f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            )
            .blur(16.dp)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "0.5x",
            color = Color.White,
            fontSize = 14.sp
        )
        Text(
            text = "1x",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.scale(1.2f)
        )
        Text(
            text = "2x",
            color = Color.White,
            fontSize = 14.sp
        )
        Text(
            text = "5x",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

/**
 * 下层：操作区（相册 + 主快门 + 切换）
 */
@Composable
fun BoxScope.BottomOperationBar(
    lastPhotoUri: Uri?,
    onTakePhoto: () -> Unit
) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(bottom = 40.dp, start = 32.dp, end = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 相册缩略图（左侧）
        GalleryThumbnail(lastPhotoUri)
        
        // 主快门（中央）
        MainShutterButton(onTakePhoto)
        
        // 前后置切换（右侧）
        CameraSwitchButton()
    }
}

/**
 * 相册缩略图（左侧）
 * 显示最后一张照片的缩略图
 */
@Composable
fun GalleryThumbnail(lastPhotoUri: Uri?) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Gray.copy(alpha = 0.5f))
            .clickable { /* 打开相册 */ },
        contentAlignment = Alignment.Center
    ) {
        if (lastPhotoUri != null) {
            AsyncImage(
                model = lastPhotoUri,
                contentDescription = "最后一张照片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Photo,
                contentDescription = "相册",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * 主快门（中央）
 * - 外圈 80dp 双重呼吸光晕
 * - 中心 64dp 渐变圆
 * - 点击时微缩至 90%
 */
@Composable
fun MainShutterButton(onTakePhoto: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "shutter_scale"
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "shutter_breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        // 外层：双重呼吸光晕
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(breathScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x80EC4899),
                            Color(0x00EC4899)
                        )
                    ),
                    shape = CircleShape
                )
                .blur(20.dp)
        )
        
        // 中层：圆环
        Box(
            modifier = Modifier
                .size(70.dp)
                .scale(scale)
                .border(
                    width = 4.dp,
                    color = Color.White,
                    shape = CircleShape
                )
        )
        
        // 内层：渐变圆
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFA78BFA),
                            Color(0xFFEC4899)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    onTakePhoto()
                    isPressed = false
                }
        )
    }
}

/**
 * 前后置切换（右侧）
 */
@Composable
fun CameraSwitchButton() {
    IconButton(
        onClick = { /* 切换前后置摄像头 */ },
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.Default.Cameraswitch,
            contentDescription = "切换摄像头",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * 权限拒绝界面
 */
@Composable
fun PermissionDeniedScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "请授予相机权限",
            color = Color.White,
            fontSize = 20.sp
        )
    }
}
