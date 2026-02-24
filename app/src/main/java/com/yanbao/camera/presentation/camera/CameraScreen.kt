package com.yanbao.camera.presentation.camera

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.yanbao.camera.R
import com.yanbao.camera.core.utils.ImageSaver
import com.yanbao.camera.ui.theme.KUROMI_PINK
import com.yanbao.camera.ui.theme.OBSIDIAN_BLACK
import kotlinx.coroutines.launch

/**
 * 相机主界面 — 严格按照 ui_main_camera.png 1:1 像素级还原
 *
 * 布局（从上到下）：
 *  1. QuickToolbar        — 黑底，⚡AUTO / 4:3 / 定时器 / 相机翻转
 *  2. Camera2PreviewView  — 取景器，叠加：左上"29D PRO"粉色胶囊、右上"RAW"白色胶囊、
 *                           居中白色对焦框、底部 f/1.8 · 1/125 · ISO400
 *  3. ModeSelectorRow     — 2行 × 5列网格，图标+文字，选中项粉色胶囊高亮
 *  4. MainShutterRow      — 相册圆形缩略图(48dp) + 库洛米快门(72dp) + 模式胶囊按钮
 *  5. Param29DPanel       — 3列水平滑块（ISO / EV / K），刻度尺轨道，库洛米图标拇指
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onNavigateToGallery: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val isRecordingMemory by viewModel.isRecordingMemory.collectAsStateWithLifecycle()
    val params by viewModel.params29D.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var lastBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── 1. 顶部工具栏 ──────────────────────────────────────────────────
        QuickToolbar(viewModel = viewModel)

        // ── 2. 取景器 ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Camera2PreviewView(
                onCaptureClick = { viewModel.takePhoto() },
                onPictureTaken = { bmp ->
                    lastBitmap = bmp
                    scope.launch { ImageSaver.saveBitmapToGallery(context, bmp) }
                },
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )

            // 居中对焦框（白色圆角方框）
            FocusFrame(modifier = Modifier.align(Alignment.Center))

            // 左上角：模式胶囊徽章（粉色）
            ModeBadge(
                mode = currentMode,
                isRecording = isRecordingMemory,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 14.dp, top = 14.dp)
            )

            // 右上角：RAW 徽章（白色）
            RawBadge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 14.dp, top = 14.dp)
            )

            // 底部左侧：曝光参数（f/1.8 · 1/125 · ISO400）
            ExposureOverlay(
                params = params,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
            )
        }

        // ── 3. 底部控制面板（毛玻璃黑色背景）────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xD0000000), Color(0xF5000000))
                    )
                )
                .padding(top = 10.dp, bottom = 12.dp)
        ) {
            // 3a. 模式选择器（2行网格）
            ModeSelectorRow(
                modes = CameraMode.values().toList(),
                selectedMode = currentMode,
                onModeSelected = { viewModel.setMode(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3b. 快门行
            MainShutterRow(
                currentMode = currentMode,
                lastBitmap = lastBitmap,
                isRecordingState = viewModel.isRecordingState,
                onGalleryClick = onNavigateToGallery,
                onShutterClick = {
                    if (currentMode == CameraMode.VIDEO) {
                        if (viewModel.isRecordingState) viewModel.stopVideo()
                        else viewModel.startVideo()
                    } else {
                        viewModel.takePhoto()
                    }
                },
                onModeButtonClick = { viewModel.setMode(CameraMode.PARAM29D) }
            )

            // 3c. 动态参数面板
            when {
                currentMode.requires29DPanel -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Param29DPanel(viewModel = viewModel)
                }
                currentMode.requiresMasterWheel -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    MasterWheel(viewModel = viewModel)
                }
                currentMode.requiresNativeControls -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    NativeControls(viewModel = viewModel)
                }
                else -> Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 对焦框（白色圆角方框，与设计图一致）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FocusFrame(modifier: Modifier = Modifier) {
    val cornerLen = 22.dp
    val stroke = 2.dp
    Box(modifier = modifier.size(110.dp)) {
        // 左上
        Box(Modifier.align(Alignment.TopStart)) {
            Box(Modifier.width(cornerLen).height(stroke).background(Color.White))
            Box(Modifier.width(stroke).height(cornerLen).background(Color.White))
        }
        // 右上
        Box(Modifier.align(Alignment.TopEnd)) {
            Box(Modifier.width(cornerLen).height(stroke).background(Color.White).align(Alignment.TopEnd))
            Box(Modifier.width(stroke).height(cornerLen).background(Color.White).align(Alignment.TopEnd))
        }
        // 左下
        Box(Modifier.align(Alignment.BottomStart)) {
            Box(Modifier.width(cornerLen).height(stroke).background(Color.White).align(Alignment.BottomStart))
            Box(Modifier.width(stroke).height(cornerLen).background(Color.White).align(Alignment.BottomStart))
        }
        // 右下
        Box(Modifier.align(Alignment.BottomEnd)) {
            Box(Modifier.width(cornerLen).height(stroke).background(Color.White).align(Alignment.BottomEnd))
            Box(Modifier.width(stroke).height(cornerLen).background(Color.White).align(Alignment.BottomEnd))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 模式徽章（左上角粉色胶囊，如 "29D PRO"）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ModeBadge(
    mode: CameraMode,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec_blink")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_alpha"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(KUROMI_PINK)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = when (mode) {
                CameraMode.PARAM29D -> "29D PRO"
                CameraMode.MASTER   -> "大师"
                CameraMode.PARALLAX -> "2.9D"
                CameraMode.BEAUTY   -> "美颜"
                CameraMode.VIDEO    -> "视频"
                CameraMode.BASIC    -> "基本"
                CameraMode.NATIVE   -> "原相机"
                CameraMode.AR       -> "AR"
                CameraMode.MEMORY   -> "记忆"
            },
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .graphicsLayer { alpha = recAlpha }
                    .background(Color.White, CircleShape)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RAW 徽章（右上角白色胶囊）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RawBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "RAW",
            color = Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 曝光参数叠加（取景器底部左侧）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExposureOverlay(
    params: Param29D,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("f/1.8", "1/${params.shutterSpeed}", "ISO${params.iso}").forEach { txt ->
            Text(
                text = txt,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 快门行（相册缩略图 + 快门按钮 + 模式胶囊按钮）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MainShutterRow(
    currentMode: CameraMode,
    lastBitmap: Bitmap?,
    isRecordingState: Boolean,
    onGalleryClick: () -> Unit,
    onShutterClick: () -> Unit,
    onModeButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧：相册缩略图（48dp 圆形）
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2A2A))
                .border(2.dp, Color(0xFF555555), CircleShape)
                .clickable { onGalleryClick() },
            contentAlignment = Alignment.Center
        ) {
            if (lastBitmap != null) {
                AsyncImage(
                    model = lastBitmap,
                    contentDescription = "最近照片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_album_kuromi),
                    contentDescription = "相册",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // 中央：库洛米快门按钮（72dp）
        ShutterButton(
            onClick = onShutterClick,
            isVideoMode = currentMode == CameraMode.VIDEO,
            modifier = Modifier.size(80.dp)
        )

        // 右侧：模式胶囊按钮（粉色，显示当前模式名）
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(KUROMI_PINK)
                .clickable { onModeButtonClick() }
                .padding(horizontal = 22.dp, vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentMode.displayName,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
