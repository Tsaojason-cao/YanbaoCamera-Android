// app/src/main/java/com/yanbao/camera/presentation/camera/YanbaoCameraScreen.kt
package com.yanbao.camera.presentation.camera

import android.Manifest
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.yanbao.camera.R
import com.yanbao.camera.core.camera.Camera2PreviewManager
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────
// 设计规范颜色
// ─────────────────────────────────────────────
private val PinkHighlight  = Color(0xFFEC4899)
private val PurpleAccent   = Color(0xFF9D4EDD)
private val ObsidianBlack  = Color(0xFF0A0A0A)
private val PanelBg        = Color(0xFF111111)
private val SliderTrack    = Color(0xFF333333)
private val SliderThumb    = Color(0xFFEC4899)

// ─────────────────────────────────────────────
// 9 大拍摄模式定义
// ─────────────────────────────────────────────
enum class YanbaoCameraMode(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    BASIC    ("基本相机", "📷", "自动曝光，智能场景识别"),
    ORIGINAL ("原相机",  "🔍", "零处理直出，保留原始色彩"),
    MEMORY   ("雁宝记忆","✨", "AI 自动捕捉精彩瞬间"),
    PRO_29D  ("29D 专业","🎛️", "29 维参数全手动调节"),
    PRO_2_9D ("2.9D",    "🌟", "轻专业模式，8 维核心参数"),
    MASTER   ("大师滤镜","🎨", "从 master_seeds.json 加载滤镜"),
    BEAUTY   ("一键美颜","💄", "AI 磨皮 + 美白 + 大眼"),
    VIDEO    ("录像",    "🎬", "4K 60fps 视频录制"),
    AR       ("AR 空间", "🌐", "AR 贴纸与空间标注")
}

// ─────────────────────────────────────────────
// 29D 参数定义
// ─────────────────────────────────────────────
data class Param29D(
    val key: String,
    val label: String,
    val group: String,
    val min: Float,
    val max: Float,
    val default: Float,
    val unit: String = ""
)

val ALL_29D_PARAMS = listOf(
    // 基础曝光（5维）
    Param29D("brightness",      "亮度",     "基础曝光", -1f,  1f,   0f),
    Param29D("contrast",        "对比度",   "基础曝光", -1f,  1f,   0f),
    Param29D("exposure",        "曝光补偿", "基础曝光", -3f,  3f,   0f,  "EV"),
    Param29D("iso",             "ISO",      "基础曝光", 50f,  6400f,100f),
    Param29D("shutter",         "快门速度", "基础曝光", 0f,   1f,   0.5f,"s"),
    // 色彩（7维）
    Param29D("colorTemp",       "色温",     "色彩",     2000f,10000f,5500f,"K"),
    Param29D("tint",            "色调",     "色彩",     -150f,150f,  0f),
    Param29D("saturation",      "饱和度",   "色彩",     -1f,  1f,   0f),
    Param29D("vibrance",        "自然饱和度","色彩",    -1f,  1f,   0f),
    Param29D("hue",             "色相",     "色彩",     -180f,180f,  0f,  "°"),
    Param29D("sharpness",       "锐度",     "色彩",     0f,   1f,   0.5f),
    Param29D("clarity",         "清晰度",   "色彩",     -1f,  1f,   0f),
    // 色彩通道（7维）
    Param29D("red",             "红色",     "色彩通道", -1f,  1f,   0f),
    Param29D("green",           "绿色",     "色彩通道", -1f,  1f,   0f),
    Param29D("blue",            "蓝色",     "色彩通道", -1f,  1f,   0f),
    Param29D("cyan",            "青色",     "色彩通道", -1f,  1f,   0f),
    Param29D("magenta",         "品红",     "色彩通道", -1f,  1f,   0f),
    Param29D("yellow",          "黄色",     "色彩通道", -1f,  1f,   0f),
    Param29D("orange",          "橙色",     "色彩通道", -1f,  1f,   0f),
    // 明暗细节（4维）
    Param29D("highlights",      "高光",     "明暗细节", -1f,  1f,   0f),
    Param29D("shadows",         "阴影",     "明暗细节", -1f,  1f,   0f),
    Param29D("whites",          "白色",     "明暗细节", -1f,  1f,   0f),
    Param29D("blacks",          "黑色",     "明暗细节", -1f,  1f,   0f),
    // 质感（3维）
    Param29D("dehaze",          "去雾",     "质感",     -1f,  1f,   0f),
    Param29D("noiseReduction",  "降噪",     "质感",     0f,   1f,   0f),
    Param29D("grain",           "颗粒",     "质感",     0f,   1f,   0f),
    Param29D("vignette",        "暗角",     "质感",     -1f,  1f,   0f),
    // 美颜（2维）
    Param29D("beautySmooth",    "磨皮",     "美颜",     0f,   1f,   0f),
    Param29D("beautyWhiten",    "美白",     "美颜",     0f,   1f,   0f)
)

// ─────────────────────────────────────────────
// 主入口
// ─────────────────────────────────────────────
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun YanbaoCameraScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (cameraPermissionState.status !is PermissionStatus.Granted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status is PermissionStatus.Granted) {
        YanbaoCameraContent(modifier = modifier, onNavigateBack = onNavigateBack)
    } else {
        PermissionDeniedScreen(
            modifier = modifier,
            onRequest = { cameraPermissionState.launchPermissionRequest() }
        )
    }
}

@Composable
private fun PermissionDeniedScreen(modifier: Modifier, onRequest: () -> Unit) {
    Box(
        modifier = modifier.fillMaxSize().background(ObsidianBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("需要相机权限才能使用此功能", color = Color.White, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = PinkHighlight)
            ) { Text("授权相机权限", color = Color.White) }
        }
    }
}

// ─────────────────────────────────────────────
// 相机主界面
// ─────────────────────────────────────────────
@Composable
private fun YanbaoCameraContent(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var selectedMode  by remember { mutableStateOf(YanbaoCameraMode.BASIC) }
    var isFlashOn     by remember { mutableStateOf(false) }
    var show29DPanel  by remember { mutableStateOf(false) }
    var isRecording   by remember { mutableStateOf(false) }

    // 29D 参数状态（key -> value）
    val params29D = remember {
        mutableStateMapOf<String, Float>().also { map ->
            ALL_29D_PARAMS.forEach { p -> map[p.key] = p.default }
        }
    }

    val previewManager = remember { Camera2PreviewManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            Log.d("YanbaoCameraScreen", "释放 Camera2 资源")
            previewManager.release()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(ObsidianBlack)) {

        // ── Layer 0: Camera2 SurfaceView 全屏预览 ──
        Camera2SurfacePreview(
            previewManager = previewManager,
            modifier = Modifier.fillMaxSize()
        )

        // ── Layer 1: 顶部工具栏 ──────────────────
        TopToolbar(
            mode = selectedMode,
            isFlashOn = isFlashOn,
            onFlashToggle = {
                isFlashOn = !isFlashOn
                val flashMode = if (isFlashOn) Camera2PreviewManager.FlashMode.ON
                                else           Camera2PreviewManager.FlashMode.OFF
                previewManager.setFlashMode(flashMode)
                Log.d("YanbaoCameraScreen", "闪光灯: ${if (isFlashOn) "ON" else "OFF"}")
            },
            onBack = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        // ── Layer 2: 底部控制面板 ────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            // 模式 Tab 栏
            ModeTabBar(
                selectedMode = selectedMode,
                onModeSelected = { mode ->
                    selectedMode = mode
                    show29DPanel = (mode == YanbaoCameraMode.PRO_29D)
                    Log.d("YanbaoCameraScreen", "模式切换: ${mode.displayName}")
                }
            )

            // 底部控制行
            BottomControlRow(
                mode       = selectedMode,
                isFlashOn  = isFlashOn,
                isRecording = isRecording,
                show29DPanel = show29DPanel,
                on29DToggle = { show29DPanel = !show29DPanel },
                onCapture  = {
                    scope.launch {
                        if (selectedMode == YanbaoCameraMode.VIDEO) {
                            isRecording = !isRecording
                            Log.d("YanbaoCameraScreen", "录像: ${if (isRecording) "开始" else "停止"}")
                        } else {
                            Log.d("YanbaoCameraScreen", "拍照 - 模式: ${selectedMode.displayName}")
                            val bitmap = previewManager.takePicture()
                            if (bitmap != null) {
                                Log.i("YanbaoCameraScreen", "✅ 拍照成功: ${bitmap.width}x${bitmap.height}")
                                val uri = com.yanbao.camera.core.utils.ImageSaver.saveBitmapToGallery(context, bitmap)
                                Log.i("YanbaoCameraScreen", if (uri != null) "✅ 已保存: $uri" else "❌ 保存失败")
                            } else {
                                Log.e("YanbaoCameraScreen", "❌ 拍照失败")
                            }
                        }
                    }
                }
            )
        }

        // ── Layer 3: 29D 专业参数面板（滑入动画）──
        AnimatedVisibility(
            visible = show29DPanel,
            enter   = slideInVertically(initialOffsetY = { it }),
            exit    = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ProPanel29D(
                params    = params29D,
                onChanged = { key, value ->
                    params29D[key] = value
                    Log.d("YanbaoCameraScreen", "29D参数: $key = $value")
                },
                onDismiss = { show29DPanel = false }
            )
        }

        // ── Layer 4: 模式专属 UI 覆盖层 ──────────
        ModeOverlay(mode = selectedMode, params29D = params29D)
    }
}

// ─────────────────────────────────────────────
// 顶部工具栏
// ─────────────────────────────────────────────
@Composable
private fun TopToolbar(
    mode: YanbaoCameraMode,
    isFlashOn: Boolean,
    onFlashToggle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 返回按钮
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart).size(36.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // 模式标题
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = mode.emoji + " " + mode.displayName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = mode.description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }

        // 闪光灯按钮
        IconButton(
            onClick = onFlashToggle,
            modifier = Modifier.align(Alignment.CenterEnd).size(36.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_flash),
                contentDescription = "闪光灯",
                tint = if (isFlashOn) PinkHighlight else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// 9 大模式 Tab 栏
// ─────────────────────────────────────────────
@Composable
private fun ModeTabBar(
    selectedMode: YanbaoCameraMode,
    onModeSelected: (YanbaoCameraMode) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedMode) {
        val index = YanbaoCameraMode.entries.indexOf(selectedMode)
        scope.launch { listState.animateScrollToItem(maxOf(0, index - 1)) }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(YanbaoCameraMode.entries) { _, mode ->
            val isSelected = mode == selectedMode
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) PinkHighlight else Color.Transparent,
                animationSpec = tween(200), label = "modeBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                animationSpec = tween(200), label = "modeText"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.displayName,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// 底部控制行
// ─────────────────────────────────────────────
@Composable
private fun BottomControlRow(
    mode: YanbaoCameraMode,
    isFlashOn: Boolean,
    isRecording: Boolean,
    show29DPanel: Boolean,
    on29DToggle: () -> Unit,
    onCapture: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                )
            )
            .padding(horizontal = 40.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：29D 开关（仅专业模式显示）或相册缩略图
        if (mode == YanbaoCameraMode.PRO_29D || mode == YanbaoCameraMode.PRO_2_9D) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (show29DPanel) PinkHighlight else Color.White.copy(alpha = 0.2f))
                    .clickable { on29DToggle() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "29D",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // 相册快捷入口
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_gallery),
                    contentDescription = "相册",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 中间：快门按钮
        ShutterButton(
            mode = mode,
            isRecording = isRecording,
            onCapture = onCapture
        )

        // 右侧：翻转相机
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .clickable {
                    Log.d("YanbaoCameraScreen", "翻转相机")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_camera),
                contentDescription = "翻转相机",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// 快门按钮
// ─────────────────────────────────────────────
@Composable
private fun ShutterButton(
    mode: YanbaoCameraMode,
    isRecording: Boolean,
    onCapture: () -> Unit
) {
    val isVideo = mode == YanbaoCameraMode.VIDEO
    val outerColor = if (isVideo && isRecording) Color.Red else Color.White
    val innerColor = if (isVideo) Color.Red else PinkHighlight

    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(outerColor)
            .border(width = 4.dp, color = PinkHighlight, shape = CircleShape)
            .clickable { onCapture() },
        contentAlignment = Alignment.Center
    ) {
        if (isVideo) {
            Box(
                modifier = Modifier
                    .size(if (isRecording) 24.dp else 36.dp)
                    .clip(if (isRecording) RoundedCornerShape(4.dp) else CircleShape)
                    .background(innerColor)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(innerColor.copy(alpha = 0.15f))
                    .border(width = 2.dp, color = innerColor, shape = CircleShape)
            )
        }
    }
}

// ─────────────────────────────────────────────
// 29D 专业参数面板（底部抽屉）
// ─────────────────────────────────────────────
@Composable
private fun ProPanel29D(
    params: Map<String, Float>,
    onChanged: (String, Float) -> Unit,
    onDismiss: () -> Unit
) {
    val groups = ALL_29D_PARAMS.groupBy { it.group }
    var expandedGroup by remember { mutableStateOf("基础曝光") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.65f)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(PanelBg.copy(alpha = 0.97f))
    ) {
        // 面板把手
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
                    .clickable { onDismiss() }
            )
        }

        // 面板标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎛️ 29D 专业调优",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // 重置按钮
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable {
                            ALL_29D_PARAMS.forEach { p -> onChanged(p.key, p.default) }
                            Log.d("YanbaoCameraScreen", "29D 参数已重置")
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("重置", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                // 关闭按钮
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PinkHighlight.copy(alpha = 0.2f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("收起", color = PinkHighlight, fontSize = 12.sp)
                }
            }
        }

        // 参数组 Tab
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(groups.keys.toList()) { _, group ->
                val isActive = group == expandedGroup
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isActive) PinkHighlight else Color.White.copy(alpha = 0.1f))
                        .clickable { expandedGroup = group }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = group,
                        color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Divider(color = Color.White.copy(alpha = 0.1f))

        // 参数滑块列表
        val currentParams = groups[expandedGroup] ?: emptyList()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            currentParams.forEach { param ->
                val value = params[param.key] ?: param.default
                ParamSliderRow(
                    param  = param,
                    value  = value,
                    onChanged = { onChanged(param.key, it) }
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────
// 单个参数滑块行
// ─────────────────────────────────────────────
@Composable
private fun ParamSliderRow(
    param: Param29D,
    value: Float,
    onChanged: (Float) -> Unit
) {
    val displayValue = when {
        param.key == "iso"       -> value.toInt().toString()
        param.key == "colorTemp" -> "${value.toInt()}K"
        param.unit.isNotEmpty()  -> String.format("%.1f%s", value, param.unit)
        else                     -> String.format("%.2f", value)
    }

    // 归一化到 0..1 用于滑块显示
    val normalized = (value - param.min) / (param.max - param.min)
    val isChanged = value != param.default

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 修改指示点
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isChanged) PinkHighlight else Color.Transparent)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = param.label,
                    color = if (isChanged) Color.White else Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = if (isChanged) FontWeight.Medium else FontWeight.Normal
                )
            }
            Text(
                text = displayValue,
                color = if (isChanged) PinkHighlight else Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = normalized.coerceIn(0f, 1f),
            onValueChange = { norm ->
                val actual = param.min + norm * (param.max - param.min)
                onChanged(actual)
            },
            modifier = Modifier.fillMaxWidth().height(32.dp),
            colors = SliderDefaults.colors(
                thumbColor           = SliderThumb,
                activeTrackColor     = PinkHighlight,
                inactiveTrackColor   = SliderTrack,
                activeTickColor      = Color.Transparent,
                inactiveTickColor    = Color.Transparent
            )
        )
    }
}

// ─────────────────────────────────────────────
// 模式专属覆盖层
// ─────────────────────────────────────────────
@Composable
private fun ModeOverlay(
    mode: YanbaoCameraMode,
    params29D: Map<String, Float>
) {
    when (mode) {
        YanbaoCameraMode.PRO_29D -> {
            // 显示当前 ISO / 快门 / EV 数值气泡
            val iso     = (params29D["iso"]     ?: 100f).toInt()
            val shutter = params29D["shutter"]  ?: 0.5f
            val ev      = params29D["exposure"] ?: 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ParamBubble("ISO", "$iso")
                    ParamBubble("SS",  "1/${(1f / shutter.coerceAtLeast(0.001f)).toInt()}s")
                    ParamBubble("EV",  String.format("%+.1f", ev))
                }
            }
        }
        YanbaoCameraMode.BEAUTY -> {
            // 美颜模式：显示美颜强度提示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PinkHighlight.copy(alpha = 0.8f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("💄 AI 美颜已开启", color = Color.White, fontSize = 13.sp)
                }
            }
        }
        YanbaoCameraMode.AR -> {
            // AR 模式：显示 AR 提示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PurpleAccent.copy(alpha = 0.8f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("🌐 AR 空间模式", color = Color.White, fontSize = 13.sp)
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun ParamBubble(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .border(width = 1.dp, color = PinkHighlight.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
        Text(value, color = PinkHighlight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────
// Camera2 SurfaceView 预览
// ─────────────────────────────────────────────
@Composable
fun Camera2SurfacePreview(
    previewManager: Camera2PreviewManager,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        Log.d("Camera2SurfacePreview", "Surface 已创建，启动 Camera2 预览")
                        scope.launch {
                            try {
                                val success = previewManager.openCamera(holder.surface)
                                Log.i("Camera2SurfacePreview", if (success) "✅ Camera2 预览已启动" else "❌ Camera2 预览启动失败")
                            } catch (e: Exception) {
                                Log.e("Camera2SurfacePreview", "❌ Camera2 异常", e)
                            }
                        }
                    }
                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                        Log.d("Camera2SurfacePreview", "Surface 尺寸变化: ${width}x${height}")
                    }
                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        Log.d("Camera2SurfacePreview", "Surface 销毁，关闭相机")
                        previewManager.closeCamera()
                    }
                })
            }
        },
        modifier = modifier
    )
}

/**
 * 获取相机模式名称（保留向后兼容）
 */
fun getCameraModeName(index: Int): String = YanbaoCameraMode.entries.getOrNull(index)?.displayName ?: "未知模式"
