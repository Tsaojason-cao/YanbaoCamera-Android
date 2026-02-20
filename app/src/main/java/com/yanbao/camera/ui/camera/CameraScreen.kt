package com.yanbao.camera.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.data.model.CameraMode
import com.yanbao.camera.data.model.FlashMode
import com.yanbao.camera.data.model.GridType
import com.yanbao.camera.viewmodel.CameraViewModel

/**
 * 相机主界面
 * 严格按照 03_camera/01_camera_main.png 设计规格实现：
 * - 顶部控制栏：闪光灯(🔦)、设置(⚙️)、切换摄像头(🔄)
 * - 相机预览区：实时取景 + 3x3网格线 + 变焦滑块 + 焦点指示器
 * - 底部操作栏：相册缩略图 + 大圆形快门(粉色渐变) + 翻转摄像头
 * - 模式选择栏：NORMAL/BEAUTY/2.9D/AR/IPHONE/MASTER/MEMORY/VIDEO
 */
@Composable
fun CameraScreen(
    onNavigateToGallery: () -> Unit,
    onPhotoTaken: (String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraState by viewModel.cameraState.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val showFocusIndicator by viewModel.showFocusIndicator.collectAsState()
    val focusPosition by viewModel.focusPosition.collectAsState()

    // 保存 PreviewView 引用（用于翻转摄像头）
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }

    // 相机权限
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    // 快门按钮缩放动画
    var shutterPressed by remember { mutableStateOf(false) }
    val shutterScale by animateFloatAsState(
        targetValue = if (shutterPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "shutterScale"
    )

    // 变焦值
    var zoomLevel by remember { mutableStateOf(1.0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // 相机预览区（全屏）
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onPreviewViewReady = { pv ->
                    previewViewRef.value = pv
                    viewModel.startCamera(lifecycleOwner, pv)
                },
                isFrontCamera = cameraState.isFrontCamera,
                onTap = { x, y, w, h ->
                    viewModel.focusAt(x, y, w, h)
                }
            )

            // 网格线叠加层（使用gridType字段）
            if (cameraState.gridType != GridType.NONE) {
                GridOverlay(modifier = Modifier.fillMaxSize())
            }

            // 焦点指示器
            AnimatedVisibility(
                visible = showFocusIndicator,
                enter = fadeIn() + scaleIn(initialScale = 1.5f),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (focusPosition.x - 40).dp,
                                y = (focusPosition.y - 40).dp
                            )
                            .size(80.dp)
                            .border(2.dp, Color.Yellow, RoundedCornerShape(4.dp))
                    )
                }
            }

            // 变焦滑块（右侧垂直）
            ZoomSlider(
                zoomLevel = zoomLevel,
                onZoomChange = { zoom ->
                    zoomLevel = zoom
                    viewModel.setZoom(zoom)
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            )

            // 录制时长显示
            if (isRecording) {
                RecordingTimer(
                    duration = recordingDuration,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp)
                )
            }

        } else {
            // 无权限提示
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("需要相机权限", color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEC4899)
                        )
                    ) {
                        Text("授予权限")
                    }
                }
            }
        }

        // ============ 顶部控制栏 ============
        TopControlBar(
            flashMode = cameraState.flashMode,
            onFlashToggle = { viewModel.cycleFlashMode() },
            onSettingsClick = { /* TODO: 打开设置 */ },
            onFlipCamera = {
                previewViewRef.value?.let { pv ->
                    viewModel.flipCamera(lifecycleOwner, pv)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
        )

        // ============ 底部操作区 ============
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
        ) {
            // 模式选择栏
            CameraModeBar(
                currentMode = cameraState.currentMode,
                onModeSelected = { mode -> viewModel.selectMode(mode) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 底部操作栏（相册 + 快门 + 翻转）
            BottomActionBar(
                shutterScale = shutterScale,
                isRecording = isRecording,
                currentMode = cameraState.currentMode,
                onGalleryClick = onNavigateToGallery,
                onShutterPress = {
                    shutterPressed = true
                    if (cameraState.currentMode == CameraMode.VIDEO) {
                        if (isRecording) {
                            viewModel.stopRecording()
                        } else {
                            viewModel.startRecording(context)
                        }
                    } else {
                        viewModel.takePhoto(context) { uri ->
                            onPhotoTaken(uri)
                        }
                    }
                },
                onShutterRelease = { shutterPressed = false },
                onFlipCamera = {
                    previewViewRef.value?.let { pv ->
                        viewModel.flipCamera(lifecycleOwner, pv)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 真实 CameraX 预览组件
 * 通过 onPreviewViewReady 回调传出 PreviewView 引用
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onPreviewViewReady: (PreviewView) -> Unit,
    isFrontCamera: Boolean,
    onTap: (Float, Float, Float, Float) -> Unit
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        onPreviewViewReady(previewView)
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.pointerInput(Unit) {
            detectTransformGestures { _, _, _, _ ->
                // 手势缩放在 CameraViewModel 中处理
            }
        }
    )
}

/**
 * 3x3 网格线叠加层
 */
@Composable
fun GridOverlay(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val strokeWidth = 1.dp.toPx()
        val color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f)

        // 垂直线
        drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width / 3, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width / 3, size.height), strokeWidth = strokeWidth)
        drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width * 2 / 3, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width * 2 / 3, size.height), strokeWidth = strokeWidth)

        // 水平线
        drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, size.height / 3),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 3), strokeWidth = strokeWidth)
        drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, size.height * 2 / 3),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height * 2 / 3), strokeWidth = strokeWidth)
    }
}

/**
 * 变焦滑块（右侧垂直）
 */
@Composable
fun ZoomSlider(
    zoomLevel: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${String.format("%.1f", zoomLevel)}x",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = zoomLevel,
            onValueChange = onZoomChange,
            valueRange = 1f..10f,
            modifier = Modifier
                .height(150.dp)
                .width(40.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFEC4899),
                activeTrackColor = Color(0xFFEC4899),
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * 录制计时器
 */
@Composable
fun RecordingTimer(duration: Long, modifier: Modifier = Modifier) {
    val seconds = duration / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    val timeText = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)

    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            visible = !visible
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (visible) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Red, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = timeText,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 顶部控制栏
 * 左：闪光灯 + 设置；右：切换摄像头
 */
@Composable
fun TopControlBar(
    flashMode: FlashMode,
    onFlashToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 闪光灯按钮
        IconButton(onClick = onFlashToggle) {
            Icon(
                imageVector = when (flashMode) {
                    FlashMode.OFF -> Icons.Default.FlashOff
                    FlashMode.AUTO -> Icons.Default.FlashAuto
                    FlashMode.ON, FlashMode.TORCH -> Icons.Default.FlashOn
                },
                contentDescription = "闪光灯: ${flashMode.displayName}",
                tint = if (flashMode == FlashMode.OFF) Color.White.copy(alpha = 0.6f) else Color(0xFFFFD700),
                modifier = Modifier.size(28.dp)
            )
        }

        // 设置按钮
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 切换摄像头
        IconButton(onClick = onFlipCamera) {
            Icon(
                imageVector = Icons.Default.Flip,
                contentDescription = "切换摄像头",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * 拍摄模式选择栏
 * NORMAL | BEAUTY | 2.9D | AR | IPHONE | MASTER | MEMORY | VIDEO
 */
@Composable
fun CameraModeBar(
    currentMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(CameraMode.values()) { mode ->
            val isSelected = mode == currentMode
            Column(
                modifier = Modifier
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mode.displayName,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFFEC4899) else Color.White.copy(alpha = 0.7f)
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(Color(0xFFEC4899), CircleShape)
                    )
                }
            }
        }
    }
}

/**
 * 底部操作栏
 * 左：相册缩略图；中：大圆形快门（粉色渐变）；右：翻转摄像头
 */
@Composable
fun BottomActionBar(
    shutterScale: Float,
    isRecording: Boolean,
    currentMode: CameraMode,
    onGalleryClick: () -> Unit,
    onShutterPress: () -> Unit,
    onShutterRelease: () -> Unit,
    onFlipCamera: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左：相册缩略图
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .clickable { onGalleryClick() },
            contentAlignment = Alignment.Center
        ) {
            Text("🖼️", fontSize = 24.sp)
        }

        // 中：快门按钮
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(shutterScale),
            contentAlignment = Alignment.Center
        ) {
            // 外圈
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape)
            )
            // 内圈（快门）
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        brush = if (isRecording) {
                            Brush.radialGradient(listOf(Color.Red, Color(0xFFFF5252)))
                        } else {
                            Brush.radialGradient(
                                listOf(Color(0xFFEC4899), Color(0xFFF9A8D4))
                            )
                        },
                        shape = CircleShape
                    )
                    .clickable {
                        onShutterPress()
                        onShutterRelease()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        // 右：翻转摄像头
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .clickable { onFlipCamera() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Flip,
                contentDescription = "翻转",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
