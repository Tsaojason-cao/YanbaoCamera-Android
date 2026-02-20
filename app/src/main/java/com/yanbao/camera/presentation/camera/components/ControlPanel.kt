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
 * 大师预设
 */
@Composable
fun MasterPresets(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // 占位符
    }
}

/**
 * 美颜控制
 */
@Composable
fun BeautyControls(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // 占位符
    }
}

/**
 * 2.9D 参数面板
 */
@Composable
fun Param2_9DPanel(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // 占位符
    }
}

/**
 * 原相机控制
 */
@Composable
fun ProCameraControls(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // 占位符
    }
}

/**
 * 基本控制
 */
@Composable
fun BasicControls(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // 占位符
    }
}

/**
 * AR 控制
 */
@Composable
fun ARControls(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        // 占位符
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
