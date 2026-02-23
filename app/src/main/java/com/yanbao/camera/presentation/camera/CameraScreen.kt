package com.yanbao.camera.presentation.camera

import android.Manifest
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.yanbao.camera.R
import com.yanbao.camera.core.camera.Camera2PreviewManager
import com.yanbao.camera.core.model.YanbaoMode
import com.yanbao.camera.core.utils.ImageSaver
import com.yanbao.camera.presentation.camera.components.*
import kotlinx.coroutines.launch

private val KUROMI_PINK = Color(0xFFEC4899)
private val OBSIDIAN = Color(0xFF0A0A0A)

/**
 * 相机主界面（满血版 v3）
 *
 * 功能：
 * - 9种 YanbaoMode 横向滑动切换
 * - 快捷工具栏：闪光灯 / 比例 / 定时 / 翻转
 * - 快门动画（白色闪光）
 * - 录像长按模式（红色录制指示 + 时长）
 * - 倒计时覆盖层
 * - 底部 ControlPanel 根据模式切换
 * - 顶部 yanbao AI 品牌标识
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ─── 状态收集 ─────────────────────────────────────────────────────────
    val yanbaoMode by viewModel.yanbaoMode.collectAsState()
    val flashMode by viewModel.flashMode.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val timerMode by viewModel.timerMode.collectAsState()
    val timerCountdown by viewModel.timerCountdown.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDurationSec.collectAsState()
    val shutterFlash by viewModel.shutterFlash.collectAsState()
    val capturePreviewUri by viewModel.capturePreviewUri.collectAsState()
    val isFrontCamera by viewModel.isFrontCamera.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val camera29DState by viewModel.camera29DState.collectAsState()
    val show29DPanel by viewModel.show29DPanel.collectAsState()

    // 权限
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (!cameraPermissionState.status.isGranted) {
        PermissionRequestScreen(onRequestPermission = { cameraPermissionState.launchPermissionRequest() })
        return
    }

    // ─── 快门闪光动画 ─────────────────────────────────────────────────────
    val shutterAlpha by animateFloatAsState(
        targetValue = if (shutterFlash) 0.8f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "shutter_flash"
    )

    Box(modifier = Modifier.fillMaxSize().background(OBSIDIAN)) {

        // ─── Layer 0：Camera2 预览 ────────────────────────────────────────
        Camera2PreviewView(
            onCaptureClick = { viewModel.takePhoto(context) },
            onPictureTaken = { bitmap ->
                scope.launch {
                    val uri = ImageSaver.saveBitmapToGallery(context, bitmap)
                    uri?.let { viewModel.savePhotoMetadata(it.toString()) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ─── 快门闪光叠加层 ───────────────────────────────────────────────
        if (shutterAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(shutterAlpha)
                    .background(Color.White)
            )
        }

        // ─── Layer 1：顶部控制区 ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            // 状态栏占位
            Spacer(modifier = Modifier.height(44.dp))

            // 品牌标识行
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                // 返回按钮（左）
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_kuromi),
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // yanbao AI 品牌标识（中）
                Text(
                    text = "yanbao AI",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                // 我的（右）
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile_kuromi),
                        contentDescription = "我的",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 快捷工具栏
            QuickToolbar(
                flashMode = flashMode,
                aspectRatio = aspectRatio,
                timerMode = timerMode,
                isRecording = isRecording,
                recordingDuration = recordingDuration,
                onFlashClick = { viewModel.cycleFlashMode() },
                onAspectClick = { viewModel.cycleAspectRatio() },
                onTimerClick = { viewModel.cycleTimerMode() },
                onFlipClick = { viewModel.flipCamera() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // ─── 焦段选择器（原相机模式）─────────────────────────────────────
        if (yanbaoMode == YanbaoMode.PRO) {
            ZoomSelector(
                currentZoom = zoomLevel,
                onZoomSelect = { viewModel.setZoomLevel(it) },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 220.dp)
            )
        }

        // ─── 倒计时覆盖层 ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = timerCountdown > 0,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(3.dp, KUROMI_PINK, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timerCountdown.toString(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = KUROMI_PINK
                )
            }
        }

        // ─── 拍照预览缩略图（右下角）─────────────────────────────────────
        capturePreviewUri?.let { uri ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 220.dp, end = 16.dp)
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(2.dp, KUROMI_PINK, RoundedCornerShape(10.dp))
                    .clickable { onGalleryClick() }
            ) {
                coil.compose.AsyncImage(
                    model = uri,
                    contentDescription = "最近拍摄",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // ─── Layer 1：底部控制面板 ────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            // 9种模式选择器
            YanbaoModeSelector(
                currentMode = yanbaoMode,
                onModeSelect = { viewModel.setYanbaoMode(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ControlPanel（根据模式切换内容）
            ControlPanel(
                mode = yanbaoMode,
                camera29DState = camera29DState,
                onParameterChange = { name, value -> viewModel.updateParameter(name, value) },
                onShutterClick = { viewModel.takePhoto(context) },
                onVideoClick = { viewModel.toggleRecording(context) },
                onGalleryClick = onGalleryClick,
                onFlipCamera = { viewModel.flipCamera() },
                lastPhotoUri = capturePreviewUri,
                isRecording = isRecording,
                modifier = Modifier.fillMaxWidth()
            )

            // 底部安全区
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── 快捷工具栏 ───────────────────────────────────────────────────────────────

@Composable
private fun QuickToolbar(
    flashMode: CameraViewModel.FlashMode,
    aspectRatio: CameraViewModel.AspectRatio,
    timerMode: CameraViewModel.TimerMode,
    isRecording: Boolean,
    recordingDuration: Int,
    onFlashClick: () -> Unit,
    onAspectClick: () -> Unit,
    onTimerClick: () -> Unit,
    onFlipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 录像时显示时长
        if (isRecording) {
            val min = recordingDuration / 60
            val sec = recordingDuration % 60
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "%02d:%02d".format(min, sec),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 闪光灯
        QuickToolItem(
            label = when (flashMode) {
                CameraViewModel.FlashMode.AUTO -> "自动"
                CameraViewModel.FlashMode.ON -> "开"
                CameraViewModel.FlashMode.OFF -> "关"
            },
            iconRes = R.drawable.ic_flash_kuromi,
            isActive = flashMode != CameraViewModel.FlashMode.OFF,
            onClick = onFlashClick
        )

        // 比例
        QuickToolItem(
            label = aspectRatio.label,
            iconRes = R.drawable.ic_aspect_ratio_kuromi,
            isActive = false,
            onClick = onAspectClick
        )

        // 定时
        QuickToolItem(
            label = timerMode.label,
            iconRes = R.drawable.ic_timer_kuromi,
            isActive = timerMode != CameraViewModel.TimerMode.OFF,
            onClick = onTimerClick
        )

        // 翻转
        QuickToolItem(
            label = "翻转",
            iconRes = R.drawable.ic_flip_camera_kuromi,
            isActive = false,
            onClick = onFlipClick
        )
    }
}

@Composable
private fun QuickToolItem(
    label: String,
    iconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = if (isActive) KUROMI_PINK else Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) KUROMI_PINK else Color.White.copy(alpha = 0.8f)
        )
    }
}

// ─── 9种模式选择器 ────────────────────────────────────────────────────────────

@Composable
private fun YanbaoModeSelector(
    currentMode: YanbaoMode,
    onModeSelect: (YanbaoMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = YanbaoMode.values().toList()

    LazyRow(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(modes, key = { it.name }) { mode ->
            val isSelected = mode == currentMode
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) KUROMI_PINK.copy(alpha = 0.25f)
                        else Color.Black.copy(alpha = 0.35f)
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) KUROMI_PINK else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onModeSelect(mode) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = mode.displayName,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) KUROMI_PINK else Color.White,
                    textAlign = TextAlign.Center
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(KUROMI_PINK)
                    )
                }
            }
        }
    }
}

// ─── 焦段选择器 ───────────────────────────────────────────────────────────────

@Composable
private fun ZoomSelector(
    currentZoom: Float,
    onZoomSelect: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomLevels = listOf(0.5f, 1.0f, 2.0f, 5.0f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        zoomLevels.forEach { zoom ->
            val isSelected = currentZoom == zoom
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) KUROMI_PINK else Color.Transparent)
                    .clickable { onZoomSelect(zoom) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (zoom < 1f) "${zoom}x" else "${zoom.toInt()}x",
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = Color.White
                )
            }
        }
    }
}

// ─── 权限请求界面 ─────────────────────────────────────────────────────────────

@Composable
private fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OBSIDIAN),
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
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = KUROMI_PINK)
            ) {
                Text(text = "授予权限", color = Color.White)
            }
        }
    }
}
