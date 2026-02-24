package com.yanbao.camera.presentation.camera

import android.view.SurfaceView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.KUROMI_PINK
import com.yanbao.camera.ui.theme.OBSIDIAN_BLACK

/**
 * 相机主界面 — 严格对应 05_camera_01_home.png
 *
 * 布局：
 *  顶部工具栏（TopBar）：← 主页 ⚡ ⏱ 📷 🎬 [库洛米+延宝记忆] …
 *  Layer 0 (75%)  — Camera2PreviewView 取景器 + 白色对焦框
 *  Layer 1 (25%)  — 曜石黑毛玻璃控制面板
 *    快门行：相册圆形缩略图 | 库洛米快门(72dp) | 设定齿轮
 *    焦段栏：0.5x  1x(选中粉色)  2x  3x  5x
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onNavigateToGallery: () -> Unit = {},
    onNavigateToMemory: () -> Unit = {}
) {
    val selectedMode by viewModel.currentMode.collectAsState()
    val isRecordingMemory by viewModel.isRecordingMemory.collectAsState()

    var selectedZoom by remember { mutableStateOf("1x") }
    val zoomLevels = listOf("0.5x", "1x", "2x", "3x", "5x")

    var showSettingsPopup by remember { mutableStateOf(false) }
    var showMasterPopup by remember { mutableStateOf(false) }
    var showBeautyPopup by remember { mutableStateOf(false) }
    var show29DPanel by remember { mutableStateOf(false) }
    var showFiltersPopup by remember { mutableStateOf(false) }
    var showModesDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    // ── 2.9D 状态 ─────────────────────────────────────────────────────────────
    val parallaxStrength by viewModel.parallaxStrength.collectAsState()
    val parallaxPreset by viewModel.parallaxPreset.collectAsState()

    // ── 视频大师状态 ──────────────────────────────────────────────────────────
    val selectedFps by viewModel.selectedFps.collectAsState()
    val timelapseInterval by viewModel.timelapseInterval.collectAsState()
    val totalDuration by viewModel.totalDuration.collectAsState()

    // ── AR 空间状态 ───────────────────────────────────────────────────────────
    val arCategory by viewModel.arCategory.collectAsState()
    val arSticker by viewModel.arSticker.collectAsState()
    val lbsLabel by viewModel.lbsLabel.collectAsState()

    // ── 原相机手动控制状态 ────────────────────────────────────────────────────
    val nativeIso by viewModel.nativeIso.collectAsState()
    val nativeShutterNs by viewModel.nativeShutterNs.collectAsState()
    val nativeEv by viewModel.nativeEv.collectAsState()
    val nativeWhiteBalance by viewModel.nativeWhiteBalance.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val totalHeight = maxHeight
        val layer0Height = totalHeight * 0.75f
        val layer1Height = totalHeight * 0.25f

        // ── 顶部工具栏（黑底，粉色边框） ─────────────────────────────
        CameraTopBar(
            onBackClick = {},
            onHomeClick = {},
            onFlashClick = {},
            onTimerClick = {},
            onPhotoModeClick = {},
            onVideoModeClick = {},
            onMemoryClick = onNavigateToMemory,
            onMoreClick = { showSettingsPopup = true },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(10f)
        )

        // ── Layer 0: 取景器（75%） ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(layer0Height)
                .align(Alignment.TopCenter)
        ) {
            // 相机预览 SurfaceView
            AndroidView(
                factory = { ctx -> SurfaceView(ctx) },
                modifier = Modifier.fillMaxSize()
            )

            // 居中白色对焦框
            FocusFrame(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.Center)
            )

            // AR 空间取景器覆盖层（LBS 标签）
            if (selectedMode == CameraMode.AR) {
                ArViewfinderOverlay(
                    lbsLabel = lbsLabel,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 原相机参数覆盖层（f/1.8  1/125  ISO 400  5500K）
            if (selectedMode == CameraMode.NATIVE) {
                NativeParamsOverlay(
                    iso = nativeIso,
                    shutterNs = nativeShutterNs,
                    ev = nativeEv,
                    whiteBalance = nativeWhiteBalance,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 8.dp)
                )
            }
        }

        // ── Layer 1: 控制面板（曜石黑，25%） ─────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(layer1Height)
                .align(Alignment.BottomCenter),
            color = OBSIDIAN_BLACK.copy(alpha = 0.85f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // ── 模式选择栏 ─────────────────────────────────────────
                ModeSelectorRow(
                    modes = CameraMode.values().toList(),
                    selectedMode = selectedMode,
                    onModeSelected = { viewModel.setMode(it) }
                )

                // ── 模式专属控制面板 ───────────────────────────────────
                when (selectedMode) {
                    CameraMode.PARALLAX -> {
                        Param2_9DPanel(
                            parallaxStrength = parallaxStrength,
                            onParallaxStrengthChange = { viewModel.setParallaxStrength(it) },
                            selectedPreset = parallaxPreset,
                            onPresetSelect = { viewModel.setParallaxPreset(it) }
                        )
                    }
                    CameraMode.VIDEO -> {
                        VideoMasterPanel(
                            selectedFps = selectedFps,
                            onFpsSelect = { viewModel.setFps(it) },
                            timelapseInterval = timelapseInterval,
                            onTimelapseIntervalChange = { viewModel.setTimelapseInterval(it) },
                            totalDuration = totalDuration,
                            onTotalDurationChange = { viewModel.setTotalDuration(it) }
                        )
                    }
                    CameraMode.AR -> {
                        ArSpacePanel(
                            selectedCategory = arCategory,
                            onCategorySelect = { viewModel.setArCategory(it) },
                            selectedSticker = arSticker,
                            onStickerSelect = { viewModel.setArSticker(it) },
                            lbsLabel = lbsLabel
                        )
                    }
                    CameraMode.NATIVE -> {
                        NativeManualControls(
                            iso = nativeIso,
                            onIsoChange = { viewModel.setNativeIso(it) },
                            shutterNs = nativeShutterNs,
                            onShutterChange = { viewModel.setNativeShutter(it) },
                            ev = nativeEv,
                            onEvChange = { viewModel.setNativeEv(it) },
                            whiteBalance = nativeWhiteBalance,
                            onWhiteBalanceChange = { viewModel.setNativeWhiteBalance(it) }
                        )
                    }
                    else -> {
                        // 默认控制面板：快门行 + 焦段栏
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2A2A2A))
                                    .clickable { onNavigateToGallery() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("相册", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                            ShutterButton(
                                onClick = { viewModel.triggerCapture() },
                                isRecording = viewModel.isRecordingState,
                                modifier = Modifier.size(72.dp)
                            )
                            IconButton(
                                onClick = { showSettingsPopup = true },
                                modifier = Modifier.size(52.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_settings_kuromi),
                                    contentDescription = "设定",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        ZoomLevelBar(
                            levels = zoomLevels,
                            selected = selectedZoom,
                            onSelect = { selectedZoom = it }
                        )
                    }
                }

                // ── 模式专属快门行（PARALLAX / VIDEO / AR / NATIVE） ───
                if (selectedMode == CameraMode.PARALLAX ||
                    selectedMode == CameraMode.VIDEO ||
                    selectedMode == CameraMode.AR ||
                    selectedMode == CameraMode.NATIVE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2A2A2A))
                                .clickable { onNavigateToGallery() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("相册", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                        ShutterButton(
                            onClick = {
                                if (selectedMode == CameraMode.VIDEO) {
                                    if (viewModel.isRecordingState) viewModel.stopVideo()
                                    else viewModel.startVideo()
                                } else {
                                    viewModel.triggerCapture()
                                }
                            },
                            isRecording = selectedMode == CameraMode.VIDEO && viewModel.isRecordingState,
                            modifier = Modifier.size(72.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(KUROMI_PINK.copy(alpha = 0.15f))
                                .border(1.dp, KUROMI_PINK, RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedMode.displayName,
                                color = KUROMI_PINK,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ── 弹窗层 ────────────────────────────────────────────────────
        if (showSettingsPopup) {
            SettingsPopupButton(
                onMasterModeClick = { showSettingsPopup = false; showMasterPopup = true },
                onBeautyClick = { showSettingsPopup = false; showBeautyPopup = true },
                on29DClick = { showSettingsPopup = false; show29DPanel = true },
                onShareClick = { showSettingsPopup = false },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 60.dp, end = 16.dp)
            )
        }

        if (showMasterPopup) {
            MasterModePopup(
                onDismiss = { showMasterPopup = false },
                onApply = { _ -> showMasterPopup = false }
            )
        }
        if (showBeautyPopup) {
            BeautyPopup(
                onDismiss = { showBeautyPopup = false },
                onApply = { _ -> showBeautyPopup = false }
            )
        }

        if (show29DPanel) {
            Param29DPanel(viewModel = viewModel)
        }

        if (showModesDialog) {
            ModeSelectionDialog(
                onDismiss = { showModesDialog = false },
                onModeSelected = { showModesDialog = false }
            )
        }

        if (showCategoryDialog) {
            CategoryDialog(
                onDismiss = { showCategoryDialog = false }
            )
        }
    }
}

/**
 * 顶部工具栏 — 对应 05_camera_01_home.png 顶部粉色边框栏
 * ← 主页 ⚡ ⏱ 📷 🎬 [库洛米+延宝记忆] …
 */
@Composable
fun CameraTopBar(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onFlashClick: () -> Unit,
    onTimerClick: () -> Unit,
    onPhotoModeClick: () -> Unit,
    onVideoModeClick: () -> Unit,
    onMemoryClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.75f))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(KUROMI_PINK.copy(alpha = 0.7f), KUROMI_PINK.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
            )
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
                Icon(painterResource(R.drawable.ic_back_kuromi), "返回", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onHomeClick, modifier = Modifier.size(36.dp)) {
                Icon(painterResource(R.drawable.ic_home_kuromi), "主页", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onFlashClick, modifier = Modifier.size(36.dp)) {
                Icon(painterResource(R.drawable.ic_flash_kuromi), "闪光灯", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onTimerClick, modifier = Modifier.size(36.dp)) {
                Icon(painterResource(R.drawable.ic_timer_kuromi), "定时", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onPhotoModeClick, modifier = Modifier.size(36.dp)) {
                Icon(painterResource(R.drawable.ic_camera_kuromi), "拍照", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onVideoModeClick, modifier = Modifier.size(36.dp)) {
                Icon(painterResource(R.drawable.ic_mode_video_kuromi), "录像", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            // 延宝记忆（库洛米图标 + 文字）
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onMemoryClick() }
                    .padding(horizontal = 4.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_memory_kuromi),
                    "延宝记忆",
                    tint = KUROMI_PINK,
                    modifier = Modifier.size(18.dp)
                )
                Text("延宝记忆", color = KUROMI_PINK, fontSize = 8.sp, fontWeight = FontWeight.Medium)
            }
            // 更多
            IconButton(onClick = onMoreClick, modifier = Modifier.size(36.dp)) {
                Text("···", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * 白色圆角对焦框（4个角线）
 */
@Composable
fun FocusFrame(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val cornerLen = size.width * 0.25f
        val c = Color.White
        // 左上
        drawLine(c, Offset(0f, cornerLen), Offset(0f, 0f), strokeWidth)
        drawLine(c, Offset(0f, 0f), Offset(cornerLen, 0f), strokeWidth)
        // 右上
        drawLine(c, Offset(size.width - cornerLen, 0f), Offset(size.width, 0f), strokeWidth)
        drawLine(c, Offset(size.width, 0f), Offset(size.width, cornerLen), strokeWidth)
        // 左下
        drawLine(c, Offset(0f, size.height - cornerLen), Offset(0f, size.height), strokeWidth)
        drawLine(c, Offset(0f, size.height), Offset(cornerLen, size.height), strokeWidth)
        // 右下
        drawLine(c, Offset(size.width - cornerLen, size.height), Offset(size.width, size.height), strokeWidth)
        drawLine(c, Offset(size.width, size.height - cornerLen), Offset(size.width, size.height), strokeWidth)
    }
}

/**
 * 焦段切换栏 — 对应 05_camera_01_home.png 底部 0.5x 1x 2x 3x 5x
 */
@Composable
fun ZoomLevelBar(
    levels: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        levels.forEach { level ->
            val isSelected = level == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) KUROMI_PINK else Color.Transparent)
                    .clickable { onSelect(level) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
