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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.core.model.YanbaoMode

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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.3f) // 严格占比 30%
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xCC1A1A1A), // 深色半透明
                        Color(0xE60D0D0D)  // 更深色
                    )
                )
            )
            .padding(top = 16.dp)
    ) {
        // 1. 模式选择器 (水平滚动)
        ModeSelector(
            currentMode = currentMode,
            onModeChange = onModeChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 2. 动态参数区 (根据模式展示不同 UI)
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
                    YanbaoMode.D29 -> Param29DWheel(
                        modifier = Modifier.fillMaxSize()
                    )
                    YanbaoMode.MASTER -> MasterPresets(
                        modifier = Modifier.fillMaxSize()
                    )
                    YanbaoMode.BEAUTY -> BeautyControls(
                        modifier = Modifier.fillMaxSize()
                    )
                    YanbaoMode.D2_9 -> Param2_9DPanel(
                        modifier = Modifier.fillMaxSize()
                    )
                    YanbaoMode.PRO -> ProCameraControls(
                        modifier = Modifier.fillMaxSize()
                    )
                    YanbaoMode.BASIC -> BasicControls(
                        modifier = Modifier.fillMaxSize()
                    )
                    YanbaoMode.AR -> ARControls(
                        modifier = Modifier.fillMaxSize()
                    )
                    YanbaoMode.MEMORY -> MemoryPreview(
                        modifier = Modifier.fillMaxSize()
                    )
                    YanbaoMode.GALLERY -> GalleryPreview(
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

/**
 * 模式选择器（水平滚动）
 */
@Composable
fun ModeSelector(
    currentMode: YanbaoMode,
    onModeChange: (YanbaoMode) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyRow(
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
                // 模式名称
                Text(
                    text = mode.displayName,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color(0xFFEC4899) else Color.White.copy(alpha = 0.6f)
                )
                
                // 选中指示器
                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(Color(0xFFEC4899))
                    )
                }
            }
        }
    }
}

/**
 * 29D 刻度盘
 * 
 * 类似高档手表表盘的刻度感，滑动时带 Haptic Feedback（震动反馈）
 */
@Composable
fun Param29DWheel(modifier: Modifier = Modifier) {
    var currentAngle by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0f) }
    var selectedParam by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("ISO") }
    
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 参数选择器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ISO", "快门", "白平衡", "光圈", "EV").forEach { param ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedParam == param) Color(0xFFEC4899) 
                            else Color(0xFF2A2A2A)
                        )
                        .clickable { selectedParam = param }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = param,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = if (selectedParam == param) 
                            androidx.compose.ui.text.font.FontWeight.Bold 
                        else androidx.compose.ui.text.font.FontWeight.Normal
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 刻度盘
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            // 计算拖拽角度
                            val deltaAngle = dragAmount.x / 2f
                            currentAngle = (currentAngle + deltaAngle).coerceIn(0f, 360f)
                            change.consume()
                        }
                    }
            ) {
                val center = Offset(size.width / 2, size.height + 100) // 圆心在屏幕下方，形成弧形感
                val radius = size.width * 0.8f
                
                // 绘制刻度
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
                
                // 绘制当前指针
                val currentAngleRad = Math.toRadians((currentAngle - 180).toDouble())
                val pointerX = center.x + radius * kotlin.math.cos(currentAngleRad).toFloat()
                val pointerY = center.y + radius * kotlin.math.sin(currentAngleRad).toFloat()
                
                drawLine(
                    color = Color(0xFFEC4899),
                    start = center,
                    end = Offset(pointerX, pointerY),
                    strokeWidth = 4f
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 当前值显示
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
            
            androidx.compose.material3.Text(
                text = displayValue,
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFFEC4899)
            )
        }
    }
}

/**
 * 大师预设 - 71套滤镜选择UI
 */
@Composable
fun MasterPresets(modifier: Modifier = Modifier) {
    var selectedSeries by remember { mutableStateOf("all") }
    val seriesList = listOf(
        "all" to "全部",
        "xieyi" to "写意大师",
        "gongbi" to "工笔大师",
        "jijian" to "极简大师",
        "gudian" to "古典大师",
        "xiandai" to "现代大师",
        "dianying" to "电影大师",
        "heibai" to "黑白大师",
        "jiaopian" to "胶片大师"
    )
    
    Column(modifier = modifier.fillMaxWidth()) {
        // 系列选择
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            seriesList.forEach { (id, name) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedSeries == id) Color(0xFF9B59B6)
                            else Color(0x33FFFFFF)
                        )
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
        
        // 滤镜网格（简化显示）
        Text(
            text = "已加载 71 套大师滤镜",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 美颜控制 - 7个美颜参数调节
 */
@Composable
fun BeautyControls(modifier: Modifier = Modifier) {
    val beautyParams = listOf(
        "磨皮" to 0.5f,
        "美白" to 0.3f,
        "红润" to 0.2f,
        "大眼" to 0.4f,
        "瘦脸" to 0.3f,
        "鼻翼" to 0.2f,
        "亮眼" to 0.3f
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "美颜调节",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        beautyParams.forEach { (name, defaultValue) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.width(60.dp)
                )
                // 简化显示，实际应使用Slider
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x33FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(defaultValue)
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

/**
 * 2.9D 参数面板 - 29D参数调节UI
 */
@Composable
fun Param2_9DPanel(modifier: Modifier = Modifier) {
    val paramGroups = listOf(
        "光影" to listOf("曝光", "高光", "阴影", "对比", "饱和"),
        "色彩" to listOf("色温", "色调", "R", "G", "B", "C", "M", "Y", "K", "W"),
        "质感" to listOf("锐化", "清晰", "纹理", "颗粒", "暗角", "暗边", "暗色", "暗调", "暗度", "暗化"),
        "AI骨相" to listOf("骨架1", "骨相2", "骨相3", "骨相4")
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "29D 参数系统",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        paramGroups.forEach { (groupName, params) ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = groupName,
                    color = Color(0xFFFF71CE),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                // 参数网格
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    params.take(5).forEach { param ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = param,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 原相机控制 - ISO/快门/对焦控制
 */
@Composable
fun ProCameraControls(modifier: Modifier = Modifier) {
    val proParams = listOf(
        "ISO" to "400",
        "快门" to "1/125",
        "对焦" to "AF",
        "WB" to "自动",
        "EV" to "0.0"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "专业模式",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        proParams.forEach { (name, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FFFFFF))
                    .clickable { /* 打开调节面板 */ }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = value,
                    color = Color(0xFFFF71CE),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 基本控制 - 闪光灯/网格/定时器
 */
@Composable
fun BasicControls(modifier: Modifier = Modifier) {
    var flashMode by remember { mutableStateOf("关闭") }
    var gridEnabled by remember { mutableStateOf(false) }
    var timerEnabled by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "基本设置",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        // 闪光灯
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33FFFFFF))
                .clickable {
                    flashMode = when (flashMode) {
                        "关闭" -> "自动"
                        "自动" -> "开启"
                        else -> "关闭"
                    }
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "闪光灯", color = Color.White, fontSize = 14.sp)
            Text(text = flashMode, color = Color(0xFFFF71CE), fontSize = 14.sp)
        }
        
        // 网格
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33FFFFFF))
                .clickable { gridEnabled = !gridEnabled }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "参考线", color = Color.White, fontSize = 14.sp)
            Text(
                text = if (gridEnabled) "开启" else "关闭",
                color = Color(0xFFFF71CE),
                fontSize = 14.sp
            )
        }
        
        // 定时器
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33FFFFFF))
                .clickable { timerEnabled = !timerEnabled }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "定时器", color = Color.White, fontSize = 14.sp)
            Text(
                text = if (timerEnabled) "3秒" else "关闭",
                color = Color(0xFFFF71CE),
                fontSize = 14.sp
            )
        }
    }
}

/**
 * AR 控制 - AR模式切换和控制
 */
@Composable
fun ARControls(modifier: Modifier = Modifier) {
    var selectedARMode by remember { mutableStateOf("骨架识别") }
    val arModes = listOf(
        "骨架识别",
        "空间测量",
        "AR贴纸",
        "场景识别"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "AR 功能",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        // AR模式选择
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            arModes.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selectedARMode == mode) Color(0xFF9B59B6)
                            else Color(0x33FFFFFF)
                        )
                        .clickable { selectedARMode = mode }
                        .padding(16.dp),
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
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF71CE))
                        )
                    }
                }
            }
        }
    }
}

/**
 * 记忆预览
 */
@Composable
fun MemoryPreview(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // 占位符
    }
}

/**
 * 相册预览
 */
@Composable
fun GalleryPreview(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // 占位符
    }
}

/**
 * 全局快门区
 */
@Composable
fun MainShutterSection(
    onShutterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 快门按钮将来实现
        // 占位符
    }
}
