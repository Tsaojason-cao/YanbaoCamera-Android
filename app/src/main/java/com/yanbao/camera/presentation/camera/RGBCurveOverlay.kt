package com.yanbao.camera.presentation.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.presentation.theme.YanbaoPink
import kotlin.math.abs
import kotlin.math.pow

/**
 * RGB 曲线实时浮层
 * 
 * 核心改进：
 * - 取消独立页，改为底层取景器上方的半透明浮层（Overlay）
 * - 拖动 RGB 控制点时，通过 GPU Shader 实时映射到全屏画面
 * - 控制点尺寸从 24dp 增大至 32dp，解决难以拖动的问题
 * 
 * 技术栈：
 * - Jetpack Compose Box 容器实现空间分层
 * - 所有 29D 参数变化与 CameraViewModel 绑定
 */
@Composable
fun RGBCurveOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    onCurveChanged: (channel: String, controlPoints: List<Offset>) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)) // 半透明背景
        ) {
            // 主内容区：RGB 曲线面板
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f) // 占屏幕底部 60%
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .blur(25.dp) // 毛玻璃效果
                    .padding(20.dp)
            ) {
                // 顶部：标题 + 关闭按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RGB 曲线",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    // 关闭按钮（右上角 "X"）
                    IconButton(onClick = onDismiss) {
                        Text(
                            text = "X",
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 中间：RGB 曲线编辑器
                RGBCurveEditor(
                    onCurveChanged = onCurveChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 底部：操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            // 重置曲线
                            onCurveChanged("reset", emptyList())
                            android.util.Log.d("RGBCurveOverlay", "曲线已重置")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(0.2f)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("重置", color = Color.White)
                    }
                    
                    Button(
                        onClick = {
                            // 应用曲线
                            android.util.Log.d("RGBCurveOverlay", "曲线已应用")
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YanbaoPink
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("应用", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * RGB 曲线编辑器
 * 
 * 控制点尺寸：32dp（从 24dp 增大）
 * 实时反馈：拖动时输出到 Logcat
 */
@Composable
fun RGBCurveEditor(
    onCurveChanged: (channel: String, controlPoints: List<Offset>) -> Unit,
    modifier: Modifier = Modifier
) {
    // 每个通道的控制点（初始为对角线）
    var redPoints by remember {
        mutableStateOf(listOf(Offset(0f, 0f), Offset(0.5f, 0.5f), Offset(1f, 1f)))
    }
    var greenPoints by remember {
        mutableStateOf(listOf(Offset(0f, 0f), Offset(0.5f, 0.5f), Offset(1f, 1f)))
    }
    var bluePoints by remember {
        mutableStateOf(listOf(Offset(0f, 0f), Offset(0.5f, 0.5f), Offset(1f, 1f)))
    }
    
    var selectedChannel by remember { mutableStateOf("red") }
    
    Column(modifier = modifier) {
        // 通道选择器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("red" to "红", "green" to "绿", "blue" to "蓝").forEach { (channel, label) ->
                Button(
                    onClick = { selectedChannel = channel },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedChannel == channel) {
                            when (channel) {
                                "red" -> Color.Red
                                "green" -> Color.Green
                                "blue" -> Color.Blue
                                else -> Color.Gray
                            }
                        } else {
                            Color.White.copy(0.2f)
                        }
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label, color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 曲线画布
        val currentPoints = when (selectedChannel) {
            "red" -> redPoints
            "green" -> greenPoints
            "blue" -> bluePoints
            else -> redPoints
        }
        
        CurveCanvas(
            controlPoints = currentPoints,
            curveColor = when (selectedChannel) {
                "red" -> Color.Red
                "green" -> Color.Green
                "blue" -> Color.Blue
                else -> Color.Gray
            },
            onPointDragged = { index, newOffset ->
                val updatedPoints = currentPoints.toMutableList()
                updatedPoints[index] = newOffset
                
                when (selectedChannel) {
                    "red" -> {
                        redPoints = updatedPoints
                        onCurveChanged("red", updatedPoints)
                    }
                    "green" -> {
                        greenPoints = updatedPoints
                        onCurveChanged("green", updatedPoints)
                    }
                    "blue" -> {
                        bluePoints = updatedPoints
                        onCurveChanged("blue", updatedPoints)
                    }
                }
                
                // Logcat 审计日志
                android.util.Log.d(
                    "RGBCurveEditor",
                    "[$selectedChannel] 控制点 $index 移动到 (${newOffset.x}, ${newOffset.y})"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}

/**
 * 曲线画布
 * 
 * 控制点尺寸：32dp
 */
@Composable
fun CurveCanvas(
    controlPoints: List<Offset>,
    curveColor: Color,
    onPointDragged: (index: Int, newOffset: Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggedPointIndex by remember { mutableStateOf<Int?>(null) }
    
    Canvas(
        modifier = modifier
            .background(Color.Black.copy(0.3f), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // 检测是否点击了控制点
                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()
                        
                        controlPoints.forEachIndexed { index, point ->
                            val pointX = point.x * canvasWidth
                            val pointY = (1f - point.y) * canvasHeight
                            
                            if (abs(offset.x - pointX) < 48f && abs(offset.y - pointY) < 48f) {
                                draggedPointIndex = index
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        draggedPointIndex?.let { index ->
                            val canvasWidth = size.width.toFloat()
                            val canvasHeight = size.height.toFloat()
                            
                            val newX = (change.position.x / canvasWidth).coerceIn(0f, 1f)
                            val newY = (1f - change.position.y / canvasHeight).coerceIn(0f, 1f)
                            
                            onPointDragged(index, Offset(newX, newY))
                        }
                    },
                    onDragEnd = {
                        draggedPointIndex = null
                    }
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // 绘制网格
        for (i in 0..4) {
            val x = canvasWidth * i / 4f
            val y = canvasHeight * i / 4f
            
            drawLine(
                color = Color.White.copy(0.2f),
                start = Offset(x, 0f),
                end = Offset(x, canvasHeight),
                strokeWidth = 1f
            )
            
            drawLine(
                color = Color.White.copy(0.2f),
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1f
            )
        }
        
        // 绘制曲线（三次样条插值）
        val path = Path()
        val sortedPoints = controlPoints.sortedBy { it.x }
        
        sortedPoints.forEachIndexed { index, point ->
            val x = point.x * canvasWidth
            val y = (1f - point.y) * canvasHeight
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                // 简化版：直线连接（实际应用应使用三次样条插值）
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = curveColor,
            style = Stroke(width = 3f)
        )
        
        // 绘制控制点（32dp）
        controlPoints.forEach { point ->
            val x = point.x * canvasWidth
            val y = (1f - point.y) * canvasHeight
            
            drawCircle(
                color = curveColor,
                radius = 16.dp.toPx(), // 32dp 直径 = 16dp 半径
                center = Offset(x, y)
            )
            
            drawCircle(
                color = Color.White,
                radius = 12.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
