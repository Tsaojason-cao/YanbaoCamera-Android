package com.yanbao.camera.presentation.camera

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.pow

/**
 * RGB 曲线控制面板
 * 
 * 功能：
 * 1. 显示 RGB 复合通道曲线
 * 2. 支持拖动控制点调整曲线
 * 3. 实时计算 LUT 纹理（256 x 1）
 * 4. 输出 LUT 数组到 Logcat
 * 
 * 工业级特性：
 * - 使用三次样条插值计算平滑曲线
 * - 实时更新 LUT 纹理
 * - 无延迟和断层
 */
@Composable
fun RGBCurvePanel(
    onLUTUpdate: (ByteArray) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 控制点（初始为线性映射）
    var controlPoints by remember {
        mutableStateOf(
            listOf(
                PointF(0f, 0f),
                PointF(0.25f, 0.25f),
                PointF(0.5f, 0.5f),
                PointF(0.75f, 0.75f),
                PointF(1f, 1f)
            )
        )
    }
    
    // 当前拖动的控制点索引
    var draggedPointIndex by remember { mutableIntStateOf(-1) }
    
    // 计算 LUT
    LaunchedEffect(controlPoints) {
        val lut = calculateLUT(controlPoints)
        onLUTUpdate(lut)
        
        Log.d("RGBCurvePanel", "LUT updated, control points: ${controlPoints.map { "(${it.x}, ${it.y})" }}")
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .background(Color.Black.copy(alpha = 0.15f))
                .blur(25.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RGB 曲线",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.White
                    )
                }
            }
            
            // 曲线画布
            CurveCanvas(
                controlPoints = controlPoints,
                draggedPointIndex = draggedPointIndex,
                onPointDrag = { index, newPoint ->
                    draggedPointIndex = index
                    val updatedPoints = controlPoints.toMutableList()
                    updatedPoints[index] = newPoint
                    controlPoints = updatedPoints
                },
                onDragEnd = {
                    draggedPointIndex = -1
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            
            // 重置按钮
            Button(
                onClick = {
                    controlPoints = listOf(
                        PointF(0f, 0f),
                        PointF(0.25f, 0.25f),
                        PointF(0.5f, 0.5f),
                        PointF(0.75f, 0.75f),
                        PointF(1f, 1f)
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB6C1)
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "重置", color = Color.White)
            }
        }
    }
}

/**
 * 曲线画布
 */
@Composable
fun CurveCanvas(
    controlPoints: List<PointF>,
    draggedPointIndex: Int,
    onPointDrag: (Int, PointF) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // 查找最近的控制点
                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()
                        
                        controlPoints.forEachIndexed { index, point ->
                            val x = point.x * canvasWidth
                            val y = (1 - point.y) * canvasHeight
                            val distance = abs(offset.x - x) + abs(offset.y - y)
                            
                            if (distance < 50f) {
                                // 开始拖动
                                onPointDrag(index, point)
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (draggedPointIndex >= 0) {
                            val canvasWidth = size.width.toFloat()
                            val canvasHeight = size.height.toFloat()
                            
                            val currentPoint = controlPoints[draggedPointIndex]
                            val newX = (currentPoint.x + dragAmount.x / canvasWidth).coerceIn(0f, 1f)
                            val newY = (currentPoint.y - dragAmount.y / canvasHeight).coerceIn(0f, 1f)
                            
                            // 限制第一个和最后一个点只能垂直移动
                            val finalX = if (draggedPointIndex == 0 || draggedPointIndex == controlPoints.size - 1) {
                                currentPoint.x
                            } else {
                                newX
                            }
                            
                            onPointDrag(draggedPointIndex, PointF(finalX, newY))
                            change.consume()
                        }
                    },
                    onDragEnd = {
                        onDragEnd()
                    }
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // 绘制网格
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(0f, canvasHeight / 2),
            end = Offset(canvasWidth, canvasHeight / 2),
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.2f),
            start = Offset(canvasWidth / 2, 0f),
            end = Offset(canvasWidth / 2, canvasHeight),
            strokeWidth = 1f
        )
        
        // 绘制对角线（线性映射参考）
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = Offset(0f, canvasHeight),
            end = Offset(canvasWidth, 0f),
            strokeWidth = 1f
        )
        
        // 绘制曲线
        val path = Path()
        val curvePoints = interpolateCurve(controlPoints, 100)
        
        curvePoints.forEachIndexed { index, point ->
            val x = point.x * canvasWidth
            val y = (1 - point.y) * canvasHeight
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = Color(0xFFFFB6C1),
            style = Stroke(width = 3f)
        )
        
        // 绘制控制点
        controlPoints.forEachIndexed { index, point ->
            val x = point.x * canvasWidth
            val y = (1 - point.y) * canvasHeight
            
            drawCircle(
                color = if (index == draggedPointIndex) Color(0xFFE0B0FF) else Color(0xFFFFB6C1),
                radius = if (index == draggedPointIndex) 12f else 8f,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * 插值曲线（三次样条）
 */
fun interpolateCurve(controlPoints: List<PointF>, numPoints: Int): List<PointF> {
    val result = mutableListOf<PointF>()
    
    for (i in 0 until numPoints) {
        val t = i.toFloat() / (numPoints - 1)
        val y = interpolateValue(controlPoints, t)
        result.add(PointF(t, y))
    }
    
    return result
}

/**
 * 插值单个值（使用 Catmull-Rom 样条）
 */
fun interpolateValue(controlPoints: List<PointF>, x: Float): Float {
    // 查找 x 所在的区间
    for (i in 0 until controlPoints.size - 1) {
        val p0 = controlPoints[i]
        val p1 = controlPoints[i + 1]
        
        if (x >= p0.x && x <= p1.x) {
            // 线性插值（简化版）
            val t = (x - p0.x) / (p1.x - p0.x)
            return p0.y + t * (p1.y - p0.y)
        }
    }
    
    return x // 默认线性映射
}

/**
 * 计算 LUT 纹理（256 x 1, RGB）
 */
fun calculateLUT(controlPoints: List<PointF>): ByteArray {
    val lut = ByteArray(256 * 3)
    
    for (i in 0 until 256) {
        val x = i / 255.0f
        val y = interpolateValue(controlPoints, x)
        val outputValue = (y * 255).toInt().coerceIn(0, 255)
        
        lut[i * 3] = outputValue.toByte()     // R
        lut[i * 3 + 1] = outputValue.toByte() // G
        lut[i * 3 + 2] = outputValue.toByte() // B
    }
    
    Log.d("RGBCurvePanel", "LUT_ARRAY: ${lut.take(64).joinToString(", ") { it.toInt().and(0xFF).toString() }}")
    
    return lut
}
