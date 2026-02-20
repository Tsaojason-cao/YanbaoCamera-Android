package com.yanbao.camera.presentation.camera

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yanbao.camera.core.util.CameraManager
import com.yanbao.camera.data.model.CameraMode

/**
 * 相机主界面 - Cyber-Cute 旗舰版
 * 
 * UI 布局：
 * - 顶部状态栏：闪光灯（毛玻璃圆扣）+ "yanbao AI"
 * - 中央取景器：全屏预览 + 四角库洛米线性轮廓装饰（15% 透明度）
 * - 29D 悬浮窗：左侧垂直玻璃小标签（ISO, EV, Saturation 等）
 * - 9大模式滚动条：快门上方水平滚动，选中时文字变大 + 粉色阴影
 * - 底部操作区：雁宝记忆缩略图 + 渐变发光大快门 + 前后置切换
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = remember { CameraManager() }
    
    var lastPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val currentMode by viewModel.currentMode.collectAsState()
    val camera29DState by viewModel.camera29DState.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
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
        
        // 顶部状态栏
        TopStatusBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )
        
        // 29D 悬浮窗（左侧）
        Param29DFloatingWindow(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            camera29DState = camera29DState
        )
        
        // 9大模式滚动条（快门上方）
        ModeScrollBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 180.dp),
            currentMode = currentMode,
            onModeSelected = { viewModel.switchMode(it) }
        )
        
        // 底部操作区
        BottomOperationBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            lastPhotoUri = lastPhotoUri,
            onTakePhoto = {
                cameraManager.takePhoto(context) { success, message, uri ->
                    if (success && uri != null) {
                        lastPhotoUri = uri
                        Toast.makeText(context, "照片已保存", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "拍照失败: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

/**
 * 顶部状态栏
 * 左右分布：[左] 闪光灯（毛玻璃圆扣）[中] yanbao AI
 */
@Composable
fun TopStatusBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：闪光灯（毛玻璃圆扣）
            GlassButton(
                icon = "⚡",
                onClick = { /* 切换闪光灯 */ }
            )
            
            // 中间：yanbao AI
            Text(
                text = "yanbao AI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // 右侧：更多（毛玻璃圆扣）
            GlassButton(
                icon = "⋯",
                onClick = { /* 打开更多菜单 */ }
            )
        }
    }
}

/**
 * 毛玻璃圆扣按钮
 */
@Composable
fun GlassButton(
    icon: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            color = Color.White
        )
    }
}

/**
 * 29D 悬浮窗（左侧垂直玻璃小标签）
 * 实时显示当前：ISO, EV, Saturation 等核心参数
 */
@Composable
fun Param29DFloatingWindow(
    modifier: Modifier = Modifier,
    camera29DState: com.yanbao.camera.data.model.Camera29DState
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 显示 5 个核心参数
        listOf(
            "ISO" to camera29DState.iso.toString(),
            "EV" to String.format("%.1f", (camera29DState.exposure - 0.5f) * 6),
            "饱和度" to String.format("%.0f%%", camera29DState.saturation * 100),
            "对比度" to String.format("%.0f%%", camera29DState.contrast * 100),
            "锐度" to String.format("%.0f%%", camera29DState.sharpness * 100)
        ).forEach { (label, value) ->
            GlassTag(label = label, value = value)
        }
    }
}

/**
 * 玻璃小标签
 */
@Composable
fun GlassTag(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * 9大模式滚动条
 * 水平滚动，选中时文字变大 + 粉色阴影
 */
@Composable
fun ModeScrollBar(
    modifier: Modifier = Modifier,
    currentMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit
) {
    val modes = CameraMode.values().toList()
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(horizontal = 32.dp)
    ) {
        items(modes) { mode ->
            val isSelected = mode == currentMode
            
            Text(
                text = mode.displayName,
                fontSize = if (isSelected) 20.sp else 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFFEC4899) else Color.White,
                modifier = Modifier
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 8.dp)
            )
        }
    }
}

/**
 * 底部操作区
 * 三点式分布：[左] 雁宝记忆缩略图 [中] 渐变发光大快门 [右] 前后置切换
 */
@Composable
fun BottomOperationBar(
    modifier: Modifier = Modifier,
    lastPhotoUri: Uri?,
    onTakePhoto: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：雁宝记忆缩略图
        GalleryThumbnail(lastPhotoUri = lastPhotoUri)
        
        // 中间：渐变发光大快门
        GradientGlowingShutterButton(onTakePhoto = onTakePhoto)
        
        // 右侧：前后置切换
        GlassButton(
            icon = "🔄",
            onClick = { /* 切换前后置摄像头 */ }
        )
    }
}

/**
 * 雁宝记忆缩略图
 */
@Composable
fun GalleryThumbnail(lastPhotoUri: Uri?) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (lastPhotoUri != null) {
            AsyncImage(
                model = lastPhotoUri,
                contentDescription = "最后一张照片",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "📷",
                fontSize = 32.sp
            )
        }
    }
}

/**
 * 渐变发光大快门
 * 外圈：双重呼吸光晕
 * 中层：圆环
 * 内层：渐变圆 + 点击缩放 90%
 */
@Composable
fun GradientGlowingShutterButton(onTakePhoto: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "shutter")
    
    // 呼吸动画
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )
    
    var isPressed by remember { mutableStateOf(false) }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        // 外层：双重呼吸光晕
        repeat(2) { index ->
            Box(
                modifier = Modifier
                    .size((90 + index * 20).dp)
                    .scale(breathScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFEC4899).copy(alpha = 0.6f * (1 - index * 0.3f)),
                                Color(0xFFEC4899).copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .blur((15 + index * 10).dp)
            )
        }
        
        // 中层：圆环
        Box(
            modifier = Modifier
                .size(80.dp)
                .border(4.dp, Color.White, CircleShape)
        )
        
        // 内层：渐变圆
        Box(
            modifier = Modifier
                .size(70.dp)
                .scale(if (isPressed) 0.9f else 1.0f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFA78BFA),
                            Color(0xFFEC4899)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable {
                    isPressed = true
                    onTakePhoto()
                    isPressed = false
                }
        )
    }
}

/**
 * 库洛米装饰（四角，15% 透明度）
 * 使用 Box 布局置于最顶层，不拦截点击事件
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
    )
    
    // 右上角
    Text(
        text = "$heartEmoji$kuromiEmoji",
        fontSize = 32.sp,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
            .alpha(alpha)
    )
    
    // 左下角
    Text(
        text = "$starEmoji$kuromiEmoji",
        fontSize = 28.sp,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(16.dp)
            .alpha(alpha)
    )
    
    // 右下角
    Text(
        text = "$kuromiEmoji$starEmoji",
        fontSize = 28.sp,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .alpha(alpha)
    )
}
