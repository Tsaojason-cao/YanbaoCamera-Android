package com.yanbao.camera.presentation.edit

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.pow

/**
 * RGB 曲线编辑器界面
 * 
 * 功能：
 * - 可拖动的控制点（R/G/B/全通道）
 * - 实时预览（拖动即变色）
 * - 网格参考线
 * - 三次样条插值
 */
@Composable
fun RGBCurveEditorScreen(
    bitmap: Bitmap? = null,
    onApply: (List<PointF>) -> Unit = {},
    viewModel: RGBCurveEditorViewModel = viewModel()
) {
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val curvePoints by viewModel.curvePoints.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
            .padding(16.dp)
    ) {
        // 顶部：标题 + 通道切换
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RGB 曲线",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // 通道切换按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurveChannel.values().forEach { channel ->
                    ChannelButton(
                        channel = channel,
                        isSelected = channel == selectedChannel,
                        onClick = { viewModel.selectChannel(channel) }
                    )
                }
            }
        }
        
        // 中间：曲线编辑器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2A2A2A))
        ) {
            CurveCanvas(
                points = curvePoints[selectedChannel] ?: getDefaultCurvePoints(),
                channel = selectedChannel,
                onPointsChanged = { newPoints ->
                    viewModel.updateCurvePoints(selectedChannel, newPoints)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 底部：操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.resetCurve(selectedChannel) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3A3A3A)
                )
            ) {
                Text("重置", color = Color.White)
            }
            
            Button(
                onClick = {
                    val allPoints = curvePoints[CurveChannel.RGB] ?: getDefaultCurvePoints()
                    onApply(allPoints)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEC4899)
                )
            ) {
                Text("应用", color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 提示文字
        Text(
            text = "拖动控制点调整曲线，实时预览色彩变化",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

/**
 * 曲线通道枚举
 */
enum class CurveChannel(val displayName: String, val color: Color) {
    RGB("RGB", Color.White),
    RED("R", Color(0xFFEF4444)),
    GREEN("G", Color(0xFF10B981)),
    BLUE("B", Color(0xFF3B82F6))
}

/**
 * 通道切换按钮
 */
@Composable
fun ChannelButton(
    channel: CurveChannel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        channel.color.copy(alpha = 0.3f)
    } else {
        Color(0xFF3A3A3A)
    }
    
    val textColor = if (isSelected) {
        channel.color
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onClick() },
                    onDrag = { _, _ -> }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = channel.displayName,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

/**
 * 曲线画布（可拖动控制点）
 */
@Composable
fun CurveCanvas(
    points: List<PointF>,
    channel: CurveChannel,
    onPointsChanged: (List<PointF>) -> Unit
) {
    var currentPoints by remember(points) { mutableStateOf(points) }
    var draggedPointIndex by remember { mutableStateOf<Int?>(null) }
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // 查找最近的控制点
                        val canvasSize = size.width
                        val touchX = offset.x / canvasSize
                        val touchY = 1f - (offset.y / canvasSize)
                        
                        draggedPointIndex = currentPoints.indexOfFirst { point ->
                            val dx = abs(point.x - touchX)
                            val dy = abs(point.y - touchY)
                            dx < 0.05f && dy < 0.05f
                        }
                    },
                    onDrag = { change, _ ->
                        draggedPointIndex?.let { index ->
                            val canvasSize = size.width
                            val newX = (change.position.x / canvasSize).coerceIn(0f, 1f)
                            val newY = (1f - (change.position.y / canvasSize)).coerceIn(0f, 1f)
                            
                            // 限制第一个和最后一个点只能垂直移动
                            val updatedPoints = currentPoints.toMutableList()
                            if (index == 0) {
                                updatedPoints[index] = PointF(0f, newY)
                            } else if (index == currentPoints.size - 1) {
                                updatedPoints[index] = PointF(1f, newY)
                            } else {
                                updatedPoints[index] = PointF(newX, newY)
                            }
                            
                            currentPoints = updatedPoints
                            onPointsChanged(updatedPoints)
                        }
                    },
                    onDragEnd = {
                        draggedPointIndex = null
                    }
                )
            }
    ) {
        val canvasSize = size.width
        
        // 绘制网格
        for (i in 0..4) {
            val pos = canvasSize * i / 4f
            
            // 垂直线
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(pos, 0f),
                end = Offset(pos, canvasSize),
                strokeWidth = 1.dp.toPx()
            )
            
            // 水平线
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(0f, pos),
                end = Offset(canvasSize, pos),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // 绘制对角线参考线
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(0f, canvasSize),
            end = Offset(canvasSize, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                floatArrayOf(10f, 10f)
            )
        )
        
        // 绘制曲线（使用三次样条插值）
        val interpolator = CubicSplineInterpolator(currentPoints)
        val curvePath = Path()
        
        for (i in 0..256) {
            val x = i / 256f
            val y = interpolator.interpolate(x)
            val canvasX = x * canvasSize
            val canvasY = (1f - y) * canvasSize
            
            if (i == 0) {
                curvePath.moveTo(canvasX, canvasY)
            } else {
                curvePath.lineTo(canvasX, canvasY)
            }
        }
        
        drawPath(
            path = curvePath,
            color = channel.color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // 绘制控制点
        currentPoints.forEachIndexed { index, point ->
            val canvasX = point.x * canvasSize
            val canvasY = (1f - point.y) * canvasSize
            
            // 外圈（白色）
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(canvasX, canvasY)
            )
            
            // 内圈（通道颜色）
            drawCircle(
                color = channel.color,
                radius = 6.dp.toPx(),
                center = Offset(canvasX, canvasY)
            )
            
            // 被拖动的点高亮
            if (index == draggedPointIndex) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 12.dp.toPx(),
                    center = Offset(canvasX, canvasY)
                )
            }
        }
    }
}

/**
 * RGB 曲线编辑器 ViewModel
 */
class RGBCurveEditorViewModel : ViewModel() {
    
    private val _selectedChannel = MutableStateFlow(CurveChannel.RGB)
    val selectedChannel: StateFlow<CurveChannel> = _selectedChannel
    
    private val _curvePoints = MutableStateFlow(
        mapOf(
            CurveChannel.RGB to getDefaultCurvePoints(),
            CurveChannel.RED to getDefaultCurvePoints(),
            CurveChannel.GREEN to getDefaultCurvePoints(),
            CurveChannel.BLUE to getDefaultCurvePoints()
        )
    )
    val curvePoints: StateFlow<Map<CurveChannel, List<PointF>>> = _curvePoints
    
    /**
     * 选择通道
     */
    fun selectChannel(channel: CurveChannel) {
        _selectedChannel.value = channel
    }
    
    /**
     * 更新曲线控制点
     */
    fun updateCurvePoints(channel: CurveChannel, points: List<PointF>) {
        val updated = _curvePoints.value.toMutableMap()
        updated[channel] = points
        _curvePoints.value = updated
    }
    
    /**
     * 重置曲线
     */
    fun resetCurve(channel: CurveChannel) {
        val updated = _curvePoints.value.toMutableMap()
        updated[channel] = getDefaultCurvePoints()
        _curvePoints.value = updated
    }
}
