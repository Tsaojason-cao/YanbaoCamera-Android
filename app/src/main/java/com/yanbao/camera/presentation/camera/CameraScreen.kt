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
import androidx.compose.ui.draw.blur
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

// ─── 品牌色常量 ───────────────────────────────────────────────────────────────
private val BRAND_PINK    = Color(0xFFEC4899)
private val CARROT_ORANGE = Color(0xFFF97316)
private val OBSIDIAN      = Color(0xFF1A1A1A)
private val OBSIDIAN_DARK = Color(0xFF0A0A0A)

/**
 * M3 相机主界面 — 严格 1:1 还原 CAM_01~CAM_09 设计稿
 *
 * ┌──────────────────────────────────┐
 * │  顶部快捷工具栏（TopQuickBar）      │  ~5%
 * ├──────────────────────────────────┤
 * │                                  │
 * │  取景器（CameraX PreviewView）     │  ~67%（合计72%）
 * │  + 取景器内叠加层（状态标签等）     │
 * │                                  │
 * ├──────────────────────────────────┤
 * │  模式拨盘（ModeSelectorRow）       │  ~6%（归入28%面板）
 * │  模式专属参数面板                  │  ~14%
 * │  快门行（熊熊/熊掌/切换）           │  ~8%
 * └──────────────────────────────────┘
 *
 * 控制面板高斯模糊：40dp（blur modifier）
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onNavigateToGallery: () -> Unit = {},
    onNavigateToMemory: () -> Unit = {}
) {
    val selectedMode    by viewModel.currentMode.collectAsState()
    val isRecording     by viewModel.isRecordingMemory.collectAsState()
    val flashMode       by viewModel.flashMode.collectAsState()
    val aspectRatio     by viewModel.aspectRatio.collectAsState()
    val timer           by viewModel.timer.collectAsState()
    val lensFacing      by viewModel.lensFacing.collectAsState()

    // ── 雁宝记忆：拍照前检查传入 JSON 参数包 ─────────────────────────────────
    val incomingMemoryParams by viewModel.incomingMemoryParams.collectAsState()

    var showSettingsPopup  by remember { mutableStateOf(false) }
    var showMasterPopup    by remember { mutableStateOf(false) }
    var showBeautyPopup    by remember { mutableStateOf(false) }
    var show29DPanel       by remember { mutableStateOf(false) }
    var showFiltersPopup   by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(OBSIDIAN_DARK)
    ) {
        val totalH   = maxHeight
        val viewfinderH = totalH * 0.72f   // 取景器 72%
        val panelH      = totalH * 0.28f   // 控制面板 28%

        Column(modifier = Modifier.fillMaxSize()) {

            // ── 取景器区域（72%）─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(viewfinderH)
            ) {
                // Camera2 PreviewView
                AndroidView(
                    factory = { ctx ->
                        SurfaceView(ctx).apply {
                            viewModel.initCameraManager(ctx)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 顶部快捷工具栏（悬浮在取景器上方）
                TopQuickBar(
                    flashMode    = flashMode,
                    timer        = timer,
                    aspectRatio  = aspectRatio,
                    currentMode  = selectedMode,
                    onFlashClick = { viewModel.setFlashMode((flashMode + 1) % 3) },
                    onTimerClick = { viewModel.setTimer(if (timer == 0) 3 else 0) },
                    onSettingsClick = { showSettingsPopup = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .zIndex(10f)
                )

                // 取景器内状态标签（AI渲染中 / 记忆匹配中 / AR跟踪中 / 深度捕捉中）
                ViewfinderStatusLabel(
                    mode = selectedMode,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, top = 56.dp)
                )

                // 雁宝记忆：顶部悬浮 JSON 参数显示
                if (selectedMode == CameraMode.MEMORY && incomingMemoryParams != null) {
                    MemoryParamsOverlay(
                        params = incomingMemoryParams!!,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 12.dp, top = 56.dp)
                    )
                }

                // 比例选择栏（CAM_05视差模式在取景器内顶部显示）
                if (selectedMode == CameraMode.PARALLAX) {
                    AspectRatioPills(
                        selected = aspectRatio,
                        onSelect = { viewModel.setAspectRatio(it) },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 48.dp)
                    )
                }
            }

            // ── 控制面板（28%，曜石黑毛玻璃，高斯模糊40dp）───────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(panelH)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                OBSIDIAN.copy(alpha = 0.92f),
                                OBSIDIAN_DARK.copy(alpha = 0.98f)
                            )
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 模式拨盘
                    ModeSelectorRow(
                        modes        = CameraMode.values().toList(),
                        selectedMode = selectedMode,
                        onModeSelected = { viewModel.setMode(it) }
                    )

                    // 模式专属参数面板
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (selectedMode) {
                            CameraMode.BASIC    -> CameraBasicModePanel()
                            CameraMode.NATIVE   -> NativeManualControls(viewModel = viewModel)
                            CameraMode.MASTER   -> MasterModeFilterWheel(viewModel = viewModel)
                            CameraMode.PARAM29D -> Param29DPanel(viewModel = viewModel)
                            CameraMode.PARALLAX -> Param2_9DPanel(viewModel = viewModel)
                            CameraMode.BEAUTY   -> BeautyModePanel()
                            CameraMode.VIDEO    -> VideoMasterPanel(isRecording = isRecording)
                            CameraMode.MEMORY   -> MemoryModePanel(
                                onApplyMemory = { viewModel.applyIncomingMemoryParams() },
                                onSelectOtherPhoto = onNavigateToGallery
                            )
                            CameraMode.AR       -> ARSpacePanel(viewModel = viewModel)
                        }
                    }

                    // 快门行
                    ShutterRow(
                        selectedMode = selectedMode,
                        isRecording  = isRecording,
                        onShutterClick = {
                            // 雁宝记忆：拍照前若有传入参数包则1:1覆盖
                            if (incomingMemoryParams != null) {
                                viewModel.applyIncomingMemoryParams()
                            }
                            viewModel.triggerCapture()
                        },
                        onGalleryClick = onNavigateToGallery,
                        onFlipClick    = { viewModel.flipCamera() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }

        // ── 底部导航栏 ────────────────────────────────────────────────────────
        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 顶部快捷工具栏
// ─────────────────────────────────────────────────────────────────────────────
/**
 * 顶部快捷工具栏 — 严格还原各模式设计图
 * CAM_01/08: 闪光灯(兔耳) + 定时(兔耳) + 设置(齿轮)
 * CAM_05:    闪光灯 + 定时 + 时间 + 设置 + 比例胶囊
 */
@Composable
fun TopQuickBar(
    flashMode: Int,
    timer: Int,
    aspectRatio: Int,
    currentMode: CameraMode,
    onFlashClick: () -> Unit,
    onTimerClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 闪光灯（兔耳图标）
        IconButton(onClick = onFlashClick, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(
                    if (flashMode == 0) R.drawable.ic_yanbao_flash_off else R.drawable.ic_yanbao_flash
                ),
                contentDescription = "闪光灯",
                tint = if (flashMode == 0) Color.White.copy(alpha = 0.5f) else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        // 定时（兔耳图标）
        IconButton(onClick = onTimerClick, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_yanbao_timer),
                contentDescription = "定时",
                tint = if (timer > 0) BRAND_PINK else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 比例胶囊（CAM_01/08等显示在顶部）
        if (currentMode != CameraMode.PARALLAX) {
            AspectRatioPills(
                selected = aspectRatio,
                onSelect = {},
                modifier = Modifier
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 设置（齿轮图标）
        IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_yanbao_settings),
                contentDescription = "设置",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 比例选择胶囊
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AspectRatioPills(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val ratios = listOf("1:1", "3:4", "4:3", "9:16", "FULL")
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
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
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 取景器内状态标签
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ViewfinderStatusLabel(mode: CameraMode, modifier: Modifier = Modifier) {
    val (text, color) = when (mode) {
        CameraMode.PARAM29D -> "AI 渲染中" to BRAND_PINK
        CameraMode.MEMORY   -> "记忆匹配中" to BRAND_PINK
        CameraMode.AR       -> "AR 跟踪中" to BRAND_PINK
        CameraMode.PARALLAX -> "深度捕捉中" to Color.White
        else -> return
    }
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 雁宝记忆：顶部悬浮 JSON 参数显示
// ─────────────────────────────────────────────────────────────────────────────
/**
 * 拍照前：若有相册传入的 JSON 参数包，在取景器右上角悬浮显示
 * 参数包含：ISO / Shutter / AI Style / 滤镜编号
 */
@Composable
fun MemoryParamsOverlay(
    params: MemoryJsonParams,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "雁宝记忆参数",
            color = BRAND_PINK,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text("ISO: ${params.iso}", color = Color.White, fontSize = 10.sp)
        Text("快门: ${params.shutter}", color = Color.White, fontSize = 10.sp)
        Text("AI风格: ${params.aiStyle}", color = Color.White, fontSize = 10.sp)
        if (params.filterId != null) {
            Text("滤镜: #${params.filterId}", color = CARROT_ORANGE, fontSize = 10.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 快门行（熊熊 / 熊掌快门 / 相机切换）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ShutterRow(
    selectedMode: CameraMode,
    isRecording: Boolean,
    onShutterClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFlipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左：熊熊图标（打开雁宝记忆相册）
        IconButton(
            onClick = onGalleryClick,
            modifier = Modifier
                .size(52.dp)
                .background(Color.White.copy(alpha = 0.10f), CircleShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_yanbao_bear),
                contentDescription = "雁宝记忆",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // 中：熊掌快门键（80dp，粉色，霓虹发光）
        ShutterButton(
            onClick = onShutterClick,
            isVideoMode = selectedMode == CameraMode.VIDEO && isRecording
        )

        // 右：相机切换（前后摄）— 设计图专用翻转icon
        IconButton(
            onClick = onFlipClick,
            modifier = Modifier
                .size(52.dp)
                .background(Color.White.copy(alpha = 0.10f), CircleShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_flip_camera),
                contentDescription = "切换镜头",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 底部导航栏
// ─────────────────────────────────────────────────────────────────────────────
/**
 * 底部导航栏 — 与M2 HomeScreen一致
 * 布局：首页 | 编辑 | [熊掌FAB—当前页，不再是拍照键] | 推荐 | 我的
 * 注：相机模块内已有ShutterRow作为唯一快门，底部导航FAB仅表示当前页面位置
 */
@Composable
fun BottomNavBar(
    onHomeClick:      () -> Unit = {},
    onEditorClick:    () -> Unit = {},
    onRecommendClick: () -> Unit = {},
    onProfileClick:   () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(OBSIDIAN_DARK)
    ) {
        // 顶部分割线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.10f))
                .align(Alignment.TopCenter)
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 首页
            CamNavItem(R.drawable.ic_yanbao_home, "首页", false, onHomeClick)
            // 编辑
            CamNavItem(R.drawable.ic_yanbao_edit, "编辑", false, onEditorClick)
            // 中间熊掌FAB（当前页标识，粉色发光）
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                // 外圈发光
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(BRAND_PINK.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                // 熊掌圆圈（当前页标识，不可点击）
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFF1A1A1A), CircleShape)
                        .border(2.dp, BRAND_PINK, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // 设计图：熊掌图标（当前页标识）
                    Icon(
                        painter = painterResource(R.drawable.ic_shutter_paw),
                        contentDescription = "相机模块",
                        tint = BRAND_PINK,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            // 推荐
            CamNavItem(R.drawable.ic_yanbao_recommend, "推荐", false, onRecommendClick)
            // 我的
            CamNavItem(R.drawable.ic_yanbao_profile, "我的", false, onProfileClick)
        }
    }
}

@Composable
private fun CamNavItem(iconRes: Int, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = if (selected) BRAND_PINK else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = if (selected) BRAND_PINK else Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 数据类：雁宝记忆 JSON 参数包
// ─────────────────────────────────────────────────────────────────────────────
/**
 * 雁宝记忆 JSON 参数包
 * 拍照前：从相册传入，1:1 覆盖当前取景器参数
 * 拍照后：将当前参数封装写入图片 Metadata
 */
data class MemoryJsonParams(
    val iso: Int = 200,
    val shutter: String = "1/250",
    val aiStyle: String = "自然",
    val filterId: Int? = null,
    val masterFilterIndex: Int? = null,
    val param29dLight: Float = 85f,
    val param29dColor: Float = 90f,
    val param29dMaterial: Float = 70f,
    val param29dSpace: Float = 65f
)

// ─────────────────────────────────────────────────────────────────────────────
// 对焦框（白色四角线）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FocusFrame(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val sw = 3.dp.toPx()
        val cl = size.width * 0.22f
        val c  = Color.White
        drawLine(c, Offset(0f, cl), Offset(0f, 0f), sw)
        drawLine(c, Offset(0f, 0f), Offset(cl, 0f), sw)
        drawLine(c, Offset(size.width - cl, 0f), Offset(size.width, 0f), sw)
        drawLine(c, Offset(size.width, 0f), Offset(size.width, cl), sw)
        drawLine(c, Offset(0f, size.height - cl), Offset(0f, size.height), sw)
        drawLine(c, Offset(0f, size.height), Offset(cl, size.height), sw)
        drawLine(c, Offset(size.width - cl, size.height), Offset(size.width, size.height), sw)
        drawLine(c, Offset(size.width, size.height - cl), Offset(size.width, size.height), sw)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 焦段切换栏（0.5x 1x 2x 3x 5x）
// ─────────────────────────────────────────────────────────────────────────────
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
                    .background(if (isSelected) BRAND_PINK else Color.Transparent)
                    .clickable { onSelect(level) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
