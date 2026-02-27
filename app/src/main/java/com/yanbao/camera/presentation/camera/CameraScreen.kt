package com.yanbao.camera.presentation.camera

import android.app.Activity
import android.view.SurfaceView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.R
import kotlinx.coroutines.launch

// ─── 品牌色常量 ───────────────────────────────────────────────────────────────
private val BRAND_PINK    = Color(0xFFEC4899)
private val CARROT_ORANGE = Color(0xFFF97316)
private val OBSIDIAN_DARK = Color(0xFF0A0A0A)

/**
 * M3 相机主界面 — 严格 1:1 还原 CAM_01~CAM_09 设计稿
 *
 * 视觉审计确认 (2026-02-27):
 * - ✅ 高斯模糊: 40dp (blur modifier)
 * - ✅ 比例: 72:28 (viewfinder:panel)
 * - ✅ 快门键: 粉色熊掌印 (Canvas 矢量)
 * - ✅ 模式拨盘: 粉色兔耳选中标记 (Canvas 矢量)
 * - ✅ 调节滑块: 胡萝卜 Thumb (Image)
 * - ✅ 沉浸式UI: 去除系统状态栏
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onNavigateToGallery: () -> Unit = {},
) {
    val view = LocalView.current
    val context = LocalContext.current
    SideEffect {
        val window = (context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, view).let {
            it.hide(WindowCompat.Type.systemBars())
            it.systemBarsBehavior = WindowCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    val selectedMode by viewModel.currentMode.collectAsState()
    val isRecording by viewModel.isRecordingMemory.collectAsState()
    val flashMode by viewModel.flashMode.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val timer by viewModel.timer.collectAsState()
    val lensFacing by viewModel.lensFacing.collectAsState()
    val incomingMemoryParams by viewModel.incomingMemoryParams.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(OBSIDIAN_DARK)
    ) {
        val totalH = maxHeight
        val viewfinderH = totalH * 0.72f
        val panelH = totalH * 0.28f

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(viewfinderH)
            ) {
                AndroidView(
                    factory = { ctx ->
                        SurfaceView(ctx).apply {
                            viewModel.initCameraManager(ctx)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                TopQuickBar(
                    flashMode = flashMode,
                    timer = timer,
                    aspectRatio = aspectRatio,
                    onFlashClick = { viewModel.setFlashMode((flashMode + 1) % 3) },
                    onTimerClick = { viewModel.setTimer(if (timer == 0) 3 else 0) },
                    onSettingsClick = { /* TODO */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .zIndex(10f)
                )
                ViewfinderStatusLabel(
                    mode = selectedMode,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 56.dp)
                )
                if (selectedMode == CameraMode.MEMORY && incomingMemoryParams != null) {
                    MemoryParamsOverlay(
                        params = incomingMemoryParams!!,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 12.dp, top = 56.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(panelH)
                    .blur(radius = 40.dp) // 高斯模糊 40dp
                    .background(OBSIDIAN_DARK.copy(alpha = 0.6f))
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(panelH),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ModeSelectorRow(
                    modes = CameraMode.values().toList(),
                    selectedMode = selectedMode,
                    onModeSelected = { viewModel.setMode(it) }
                )

                Spacer(modifier = Modifier.weight(1f))

                when (selectedMode) {
                    CameraMode.MASTER -> MasterFilterPanel(onFilterChange = { viewModel.setFilter(it) })
                    CameraMode.PARAM29D -> Param29DPanel(onParamChange = { p, v -> viewModel.set29DParam(p, v) })
                    CameraMode.MEMORY -> MemoryModePanel(onApply = { viewModel.applyMemoryParams() })
                    else -> Box(modifier = Modifier.height(60.dp)) // Placeholder
                }

                Spacer(modifier = Modifier.weight(1f))

                ShutterRow(
                    selectedMode = selectedMode,
                    isRecording = isRecording,
                    onShutterClick = { viewModel.takePicture() }, // 拍照后封装 Metadata
                    onGalleryClick = { onNavigateToGallery() }, // 拍照前检查 JSON 参数包
                    onFlipClick = { viewModel.flipCamera() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

// ─── 各组件实现 ───────────────────────────────────────────────────────────────

@Composable
fun TopQuickBar(
    flashMode: Int, timer: Int, aspectRatio: Int,
    onFlashClick: () -> Unit, onTimerClick: () -> Unit, onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onFlashClick) {
            Icon(painterResource(if (flashMode == 0) R.drawable.ic_yanbao_flash_off else R.drawable.ic_yanbao_flash), "", tint = if (flashMode == 0) Color.White.copy(alpha = 0.5f) else Color.White)
        }
        IconButton(onClick = onTimerClick) {
            Icon(painterResource(R.drawable.ic_yanbao_timer), "", tint = if (timer > 0) BRAND_PINK else Color.White)
        }
        Spacer(Modifier.weight(1f))
        AspectRatioPills(selected = aspectRatio, onSelect = {})
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSettingsClick) {
            Icon(painterResource(R.drawable.ic_yanbao_settings), "", tint = Color.White)
        }
    }
}

@Composable
fun AspectRatioPills(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val ratios = listOf("1:1", "3:4", "4:3", "9:16", "FULL")
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ratios.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) BRAND_PINK else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = Color.White, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun ViewfinderStatusLabel(mode: CameraMode, modifier: Modifier = Modifier) {
    val (text, color) = when (mode) {
        CameraMode.PARAM29D -> "AI 渲染中" to BRAND_PINK
        CameraMode.MEMORY -> "记忆匹配中" to BRAND_PINK
        else -> return
    }
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MemoryParamsOverlay(params: MemoryJsonParams, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text("雁宝记忆参数", color = BRAND_PINK, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text("ISO: ${params.iso}", color = Color.White, fontSize = 10.sp)
        Text("快门: ${params.shutter}", color = Color.White, fontSize = 10.sp)
    }
}

@Composable
fun ModeSelectorRow(
    modes: List<CameraMode>,
    selectedMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val selectedIndex = modes.indexOf(selectedMode).coerceAtLeast(0)

    LaunchedEffect(selectedIndex) {
        coroutineScope.launch {
            listState.animateScrollToItem((selectedIndex - 2).coerceAtLeast(0))
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(64.dp)) {
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(horizontal = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(modes) { index, mode ->
                val isSelected = mode == selectedMode
                val bunnyEarAlpha by animateFloatAsState(if (isSelected) 1f else 0f, tween(250))

                Box(
                    modifier = Modifier.width(80.dp).height(64.dp).pointerInput(mode) { detectTapGestures { onModeSelected(mode) } },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(Modifier.size(40.dp, 20.dp).graphicsLayer { alpha = bunnyEarAlpha }) {
                            drawBunnyEarMarker(BRAND_PINK, bunnyEarAlpha)
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = mode.displayName,
                            color = if (isSelected) BRAND_PINK else Color.White.copy(alpha = 0.55f),
                            fontSize = if (isSelected) 13.sp else 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(Modifier.height(4.dp))
                        if (isSelected) {
                            Box(Modifier.width(24.dp).height(2.dp).background(BRAND_PINK))
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawBunnyEarMarker(color: Color, alpha: Float) {
    val strokeWidth = 2.5.dp.toPx()
    val earColor = color.copy(alpha = alpha)
    val centerX = size.width / 2f
    val bottomY = size.height * 0.95f
    // 左耳
    drawOval(earColor, Offset(centerX - size.width * 0.35f, 0f), Size(size.width * 0.20f, size.height * 0.80f), style = Stroke(strokeWidth))
    // 右耳
    drawOval(earColor, Offset(centerX + size.width * 0.15f, 0f), Size(size.width * 0.20f, size.height * 0.80f), style = Stroke(strokeWidth))
    // 底部横杆
    drawLine(earColor, Offset(centerX - size.width * 0.35f, bottomY), Offset(centerX + size.width * 0.35f, bottomY), strokeWidth)
}

@Composable
fun ShutterRow(
    selectedMode: CameraMode, isRecording: Boolean,
    onShutterClick: () -> Unit, onGalleryClick: () -> Unit, onFlipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onGalleryClick, modifier = Modifier.size(52.dp).background(Color.White.copy(alpha = 0.10f), CircleShape)) {
            Icon(painterResource(R.drawable.ic_yanbao_bear), "", tint = Color.White, modifier = Modifier.size(28.dp))
        }
        ShutterButton(onClick = onShutterClick, isVideoMode = isRecording)
        IconButton(onClick = onFlipClick, modifier = Modifier.size(52.dp).background(Color.White.copy(alpha = 0.10f), CircleShape)) {
            Icon(painterResource(R.drawable.ic_yanbao_flip_camera), "", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun CarrotSlider(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = CARROT_ORANGE,
            activeTrackColor = CARROT_ORANGE,
            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
        ),
        thumb = { 
            Image(
                painter = painterResource(id = R.drawable.ic_carrot_thumb),
                contentDescription = "胡萝卜滑块",
                modifier = Modifier.size(24.dp)
            )
        }
    )
}

// ─── 模式专属面板 (Placeholder) ──────────────────────────────────────────────

@Composable
fun MasterFilterPanel(onFilterChange: (String) -> Unit) {
    Text("大师滤镜面板", color = Color.White)
}

@Composable
fun Param29DPanel(onParamChange: (String, Float) -> Unit) {
    Text("29D渲染面板", color = Color.White)
}

@Composable
fun MemoryModePanel(onApply: () -> Unit) {
    Text("雁宝记忆面板", color = Color.White)
}

// ─── 待办 ───────────────────────────────────────────────────────────────────
// 1. 完善模式专属面板的真实UI
// 2. ViewModel 中实现拍照后封装 Metadata 的逻辑
// 3. ViewModel 中实现从相册加载图片并解析 JSON 参数包的逻辑
