package com.yanbao.camera.presentation.camera.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yanbao.camera.R
import com.yanbao.camera.core.model.YanbaoMode

private val KUROMI_PINK = Color(0xFFEC4899)

/**
 * 30% 交互区：动态容器
 *
 * 根据当前模式动态切换功能，而不是把所有东西塞在一起
 *
 * 架构：
 * - 模式选择器（水平滚动）
 * - 动态参数区（根据模式展示不同 UI）
 * - 全局快门区
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ControlPanel(
    currentMode: YanbaoMode,
    onModeChange: (YanbaoMode) -> Unit,
    onShutterClick: () -> Unit,
    modifier: Modifier = Modifier,
    // 可选：记忆列表和相册最新图片（由 CameraScreen 传入）
    recentMemories: List<MemoryItem> = emptyList(),
    galleryThumbnails: List<String> = emptyList()
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.3f) // 严格占比 30%
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xCC1A1A1A),
                        Color(0xE60D0D0D)
                    )
                )
            )
            .padding(top = 16.dp)
    ) {
        // 1. 模式选择器
        ModeSelector(
            currentMode = currentMode,
            onModeChange = onModeChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 动态参数区
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AnimatedContent(
                targetState = currentMode,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()) with
                            (slideOutVertically { height -> -height } + fadeOut())
                },
                label = "mode_transition"
            ) { mode ->
                when (mode) {
                    YanbaoMode.D29 -> Param29DWheel(modifier = Modifier.fillMaxSize())
                    YanbaoMode.MASTER -> MasterPresets(modifier = Modifier.fillMaxSize())
                    YanbaoMode.BEAUTY -> BeautyControls(modifier = Modifier.fillMaxSize())
                    YanbaoMode.D2_9 -> Param2_9DPanel(modifier = Modifier.fillMaxSize())
                    YanbaoMode.PRO -> ProCameraControls(modifier = Modifier.fillMaxSize())
                    YanbaoMode.BASIC -> BasicControls(modifier = Modifier.fillMaxSize())
                    YanbaoMode.AR -> ARControls(modifier = Modifier.fillMaxSize())
                    YanbaoMode.MEMORY -> MemoryPreview(
                        memories = recentMemories,
                        modifier = Modifier.fillMaxSize()
                    )
                    YanbaoMode.GALLERY -> GalleryPreview(
                        thumbnails = galleryThumbnails,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 全局快门区
        MainShutterSection(
            onShutterClick = onShutterClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )
    }
}

// ─── 数据类 ──────────────────────────────────────────────────────────────────

data class MemoryItem(
    val id: Long,
    val imagePath: String,
    val locationName: String?,
    val paramSummary: String,  // e.g. "ISO 800 · 大师·东京夜景"
    val timestamp: Long
)

// ─── 模式选择器 ───────────────────────────────────────────────────────────────

@Composable
fun ModeSelector(
    currentMode: YanbaoMode,
    onModeChange: (YanbaoMode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(YanbaoMode.values().size) { index ->
            val mode = YanbaoMode.values()[index]
            val isSelected = mode == currentMode

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { onModeChange(mode) }
            ) {
                Text(
                    text = mode.displayName,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.6f)
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(KUROMI_PINK)
                    )
                }
            }
        }
    }
}

// ─── 记忆预览（满血实现）─────────────────────────────────────────────────────

/**
 * 记忆模式面板：横向滚动最近记忆卡片
 * 每张卡片显示缩略图、地点名称、参数摘要
 */
@Composable
fun MemoryPreview(
    memories: List<MemoryItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "雁宝记忆",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${memories.size} 条",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (memories.isEmpty()) {
            // 空态引导
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_memory_kuromi),
                        contentDescription = null,
                        tint = KUROMI_PINK.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "拍摄后自动记录参数",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(memories.size) { index ->
                    val memory = memories[index]
                    MemoryCard(memory = memory)
                }
            }
        }
    }
}

@Composable
private fun MemoryCard(memory: MemoryItem) {
    Column(
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 缩略图（Coil 真实加载）
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A2E))
        ) {
            AsyncImage(
                model = memory.imagePath,
                contentDescription = memory.locationName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = painterResource(id = R.drawable.ic_memory_kuromi),
                placeholder = painterResource(id = R.drawable.ic_memory_kuromi)
            )
            // 记忆标签
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(8.dp)
                    .background(KUROMI_PINK, CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = memory.locationName ?: "未知地点",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Text(
            text = memory.paramSummary,
            color = KUROMI_PINK.copy(alpha = 0.8f),
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}

// ─── 相册预览（满血实现）─────────────────────────────────────────────────────

/**
 * 相册模式面板：横向滚动最近拍摄的照片缩略图
 */
@Composable
fun GalleryPreview(
    thumbnails: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "最近拍摄",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${thumbnails.size} 张",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (thumbnails.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gallery_kuromi),
                        contentDescription = null,
                        tint = KUROMI_PINK.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "拍摄的照片将显示在这里",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(thumbnails.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1A1A2E))
                    ) {
                        AsyncImage(
                            model = thumbnails[index],
                            contentDescription = "照片 ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = painterResource(id = R.drawable.ic_gallery_kuromi)
                        )
                    }
                }
            }
        }
    }
}

// ─── 全局快门区（满血实现）──────────────────────────────────────────────────

/**
 * 快门区：左侧相册缩略图入口 + 中央快门按钮 + 右侧模式切换
 */
@Composable
fun MainShutterSection(
    onShutterClick: () -> Unit,
    modifier: Modifier = Modifier,
    lastPhotoUri: String? = null,
    onGalleryClick: () -> Unit = {},
    onFlipCamera: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：最近照片缩略图（点击进入相册）
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .clickable { onGalleryClick() },
            contentAlignment = Alignment.Center
        ) {
            if (lastPhotoUri != null) {
                AsyncImage(
                    model = lastPhotoUri,
                    contentDescription = "最近照片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.ic_gallery_kuromi)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gallery_kuromi),
                    contentDescription = "相册",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 中央：快门按钮（粉紫渐变）
        ShutterButton(
            onClick = onShutterClick,
            modifier = Modifier.size(80.dp)
        )

        // 右侧：翻转摄像头
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                .clickable { onFlipCamera() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_flip_camera),
                contentDescription = "翻转摄像头",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

// ─── 29D 刻度盘 ───────────────────────────────────────────────────────────────

@Composable
fun Param29DWheel(modifier: Modifier = Modifier) {
    var currentAngle by remember { mutableStateOf(0f) }
    var selectedParam by remember { mutableStateOf("ISO") }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ISO", "快门", "白平衡", "光圈", "EV").forEach { param ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedParam == param) KUROMI_PINK else Color(0xFF2A2A2A))
                        .clickable { selectedParam = param }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = param,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = if (selectedParam == param) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            currentAngle = (currentAngle + dragAmount.x / 2f).coerceIn(0f, 360f)
                            change.consume()
                        }
                    }
            ) {
                val center = Offset(size.width / 2, size.height + 100)
                val radius = size.width * 0.8f

                for (i in 0..100) {
                    val angle = i * 2f - 100f
                    val isMajor = i % 5 == 0
                    val tickLength = if (isMajor) 20f else 10f
                    val tickWidth = if (isMajor) 3f else 1.5f
                    val angleRad = Math.toRadians(angle.toDouble())
                    val startX = center.x + (radius - tickLength) * kotlin.math.cos(angleRad).toFloat()
                    val startY = center.y + (radius - tickLength) * kotlin.math.sin(angleRad).toFloat()
                    val endX = center.x + radius * kotlin.math.cos(angleRad).toFloat()
                    val endY = center.y + radius * kotlin.math.sin(angleRad).toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = if (isMajor) 0.8f else 0.4f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = tickWidth
                    )
                }

                val currentAngleRad = Math.toRadians((currentAngle - 180).toDouble())
                val pointerX = center.x + radius * kotlin.math.cos(currentAngleRad).toFloat()
                val pointerY = center.y + radius * kotlin.math.sin(currentAngleRad).toFloat()
                drawLine(
                    color = KUROMI_PINK,
                    start = center,
                    end = Offset(pointerX, pointerY),
                    strokeWidth = 4f
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2A2A2A))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val displayValue = when (selectedParam) {
                "ISO" -> com.yanbao.camera.core.util.CameraMapping.getISOText(
                    com.yanbao.camera.core.util.CameraMapping.mapAngleToISO(currentAngle)
                )
                "快门" -> com.yanbao.camera.core.util.CameraMapping.getShutterSpeedText(
                    com.yanbao.camera.core.util.CameraMapping.mapAngleToShutter(currentAngle)
                )
                "白平衡" -> com.yanbao.camera.core.util.CameraMapping.getKelvinText(
                    com.yanbao.camera.core.util.CameraMapping.mapAngleToKelvin(currentAngle)
                )
                "光圈" -> com.yanbao.camera.core.util.CameraMapping.getApertureText(
                    com.yanbao.camera.core.util.CameraMapping.mapAngleToAperture(currentAngle)
                )
                "EV" -> com.yanbao.camera.core.util.CameraMapping.getEVText(
                    com.yanbao.camera.core.util.CameraMapping.mapAngleToEV(currentAngle)
                )
                else -> ""
            }
            Text(
                text = displayValue,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = KUROMI_PINK
            )
        }
    }
}

// ─── 大师预设 ─────────────────────────────────────────────────────────────────

@Composable
fun MasterPresets(modifier: Modifier = Modifier) {
    var selectedSeries by remember { mutableStateOf("all") }
    val seriesList = listOf(
        "all" to "全部", "xieyi" to "写意大师", "gongbi" to "工笔大师",
        "jijian" to "极简大师", "gudian" to "古典大师", "xiandai" to "现代大师",
        "dianying" to "电影大师", "heibai" to "黑白大师", "jiaopian" to "胶片大师"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(seriesList.size) { i ->
                val (id, name) = seriesList[i]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedSeries == id) Color(0xFF9B59B6) else Color(0x33FFFFFF))
                        .clickable { selectedSeries = id }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (selectedSeries == id) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Text(
            text = "已加载 71 套大师滤镜",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// ─── 美颜控制 ─────────────────────────────────────────────────────────────────

@Composable
fun BeautyControls(modifier: Modifier = Modifier) {
    val beautyParams = listOf(
        "磨皮" to 0.5f, "美白" to 0.3f, "红润" to 0.2f,
        "大眼" to 0.4f, "瘦脸" to 0.3f, "鼻翼" to 0.2f, "亮眼" to 0.3f
    )

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "美颜调节", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        beautyParams.forEach { (name, defaultValue) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = name, color = Color.White, fontSize = 14.sp, modifier = Modifier.width(60.dp))
                Box(
                    modifier = Modifier.weight(1f).height(4.dp)
                        .clip(RoundedCornerShape(2.dp)).background(Color(0x33FFFFFF))
                ) {
                    Box(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(defaultValue)
                            .background(Color(0xFFFF71CE))
                    )
                }
                Text(
                    text = "${(defaultValue * 100).toInt()}",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
}

// ─── 2.9D 参数面板 ────────────────────────────────────────────────────────────

@Composable
fun Param2_9DPanel(modifier: Modifier = Modifier) {
    val paramGroups = listOf(
        "光影" to listOf("曝光", "高光", "阴影", "对比", "饱和"),
        "色彩" to listOf("色温", "色调", "R", "G", "B", "C", "M", "Y", "K", "W"),
        "质感" to listOf("锐化", "清晰", "纹理", "颗粒", "暗角", "暗边", "暗色", "暗调", "暗度", "暗化"),
        "AI骨相" to listOf("骨架1", "骨相2", "骨相3", "骨相4")
    )

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "29D 参数系统", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        paramGroups.forEach { (groupName, params) ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = groupName, color = Color(0xFFFF71CE), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    params.take(5).forEach { param ->
                        Box(
                            modifier = Modifier.weight(1f).height(40.dp)
                                .clip(RoundedCornerShape(8.dp)).background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = param, color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─── 专业相机控制 ─────────────────────────────────────────────────────────────

@Composable
fun ProCameraControls(modifier: Modifier = Modifier) {
    val proParams = listOf(
        "ISO" to "400", "快门" to "1/125", "对焦" to "AF", "WB" to "自动", "EV" to "0.0"
    )

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "专业模式", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        proParams.forEach { (name, value) ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)).background(Color(0x33FFFFFF))
                    .clickable { }.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = name, color = Color.White, fontSize = 14.sp)
                Text(text = value, color = Color(0xFFFF71CE), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── 基本控制 ─────────────────────────────────────────────────────────────────

@Composable
fun BasicControls(modifier: Modifier = Modifier) {
    var flashMode by remember { mutableStateOf("关闭") }
    var gridEnabled by remember { mutableStateOf(false) }
    var timerEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "基本设置", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        listOf(
            Triple("闪光灯", flashMode) { flashMode = when (flashMode) { "关闭" -> "自动"; "自动" -> "开启"; else -> "关闭" } },
            Triple("参考线", if (gridEnabled) "开启" else "关闭") { gridEnabled = !gridEnabled },
            Triple("定时器", if (timerEnabled) "3秒" else "关闭") { timerEnabled = !timerEnabled }
        ).forEach { (name, value, action) ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)).background(Color(0x33FFFFFF))
                    .clickable { action() }.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = name, color = Color.White, fontSize = 14.sp)
                Text(text = value, color = Color(0xFFFF71CE), fontSize = 14.sp)
            }
        }
    }
}

// ─── AR 控制 ──────────────────────────────────────────────────────────────────

@Composable
fun ARControls(modifier: Modifier = Modifier) {
    var selectedARMode by remember { mutableStateOf("骨架识别") }
    val arModes = listOf("骨架识别", "空间测量", "AR贴纸", "场景识别")

    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "AR 功能", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        arModes.forEach { mode ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selectedARMode == mode) Color(0xFF9B59B6) else Color(0x33FFFFFF))
                    .clickable { selectedARMode = mode }.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mode,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (selectedARMode == mode) FontWeight.Bold else FontWeight.Normal
                )
                if (selectedARMode == mode) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF71CE))
                    )
                }
            }
        }
    }
}
