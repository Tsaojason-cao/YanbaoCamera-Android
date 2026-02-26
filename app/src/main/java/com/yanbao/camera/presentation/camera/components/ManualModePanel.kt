package com.yanbao.camera.presentation.camera.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// 品牌色
// ─────────────────────────────────────────────────────────────────────────────
private val YanbaoPink = Color(0xFFEC4899)
private val CarrotOrange = Color(0xFFF97316)

// ─────────────────────────────────────────────────────────────────────────────
// 手动参数数据类
// ─────────────────────────────────────────────────────────────────────────────

data class ManualCameraParams(
    val iso: Int = 100,                    // ISO: 50-6400
    val shutterSpeed: String = "1/125",    // 快门速度
    val whiteBalance: Int = 5500,          // 白平衡 K: 2000-10000
    val focusDistance: Float = 0f,         // 对焦距离: 0(无穷远)-1(最近)
    val exposureCompensation: Float = 0f   // 曝光补偿: -3.0 ~ +3.0
)

// 快门速度预设值
val SHUTTER_SPEEDS = listOf(
    "1/8000", "1/4000", "1/2000", "1/1000", "1/500", "1/250",
    "1/125", "1/60", "1/30", "1/15", "1/8", "1/4", "1/2", "1\"", "2\"", "4\""
)

// 白平衡预设
enum class WhiteBalancePreset(val label: String, val kelvin: Int) {
    AUTO("自动", 0),
    DAYLIGHT("日光", 5500),
    CLOUDY("阴天", 6500),
    TUNGSTEN("钨丝", 3200),
    FLUORESCENT("荧光", 4000),
    FLASH("闪光", 5500),
    SHADE("阴影", 7500)
}

// ─────────────────────────────────────────────────────────────────────────────
// 手动模式控制面板（iPhone 级别专业控制）
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 原相机手动模式控制面板
 *
 * 对标 iPhone ProRAW / ProCamera / Halide：
 * - ISO 刻度滑块（50-6400，对数刻度）
 * - 快门速度选择器（1/8000 ~ 4"）
 * - 白平衡（K值 + 预设快捷）
 * - 对焦距离（MF/AF 切换 + 距离刻度）
 * - 曝光补偿（-3 ~ +3 EV，胡萝卜 Thumb）
 *
 * UI 风格：极简刻度线，曜石黑背景，品牌粉高亮
 */
@Composable
fun ManualModePanel(
    params: ManualCameraParams,
    onIsoChange: (Int) -> Unit,
    onShutterChange: (String) -> Unit,
    onWhiteBalanceChange: (Int) -> Unit,
    onFocusChange: (Float) -> Unit,
    onEvChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedParam by remember { mutableStateOf("EV") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── 参数选择器标签行 ─────────────────────────────────────────────────
        ManualParamTabRow(
            selectedParam = selectedParam,
            params = params,
            onSelect = { selectedParam = it }
        )

        // ── 当前选中参数的刻度控制器 ─────────────────────────────────────────
        when (selectedParam) {
            "ISO" -> IsoDialControl(
                value = params.iso,
                onChange = onIsoChange
            )
            "SS" -> ShutterSpeedSelector(
                value = params.shutterSpeed,
                onChange = onShutterChange
            )
            "WB" -> WhiteBalanceControl(
                kelvin = params.whiteBalance,
                onChange = onWhiteBalanceChange
            )
            "AF" -> FocusDistanceControl(
                value = params.focusDistance,
                onChange = onFocusChange
            )
            "EV" -> ExposureCompensationControl(
                value = params.exposureCompensation,
                onChange = onEvChange
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 参数标签行
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ManualParamTabRow(
    selectedParam: String,
    params: ManualCameraParams,
    onSelect: (String) -> Unit
) {
    val paramLabels = listOf(
        "ISO" to params.iso.toString(),
        "SS" to params.shutterSpeed,
        "WB" to "${params.whiteBalance}K",
        "AF" to if (params.focusDistance == 0f) "∞" else String.format("%.1f", params.focusDistance),
        "EV" to (if (params.exposureCompensation >= 0) "+${String.format("%.1f", params.exposureCompensation)}" else String.format("%.1f", params.exposureCompensation))
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        paramLabels.forEach { (key, value) ->
            val isSelected = key == selectedParam
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) YanbaoPink.copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.05f)
                    )
                    .pointerInput(key) {
                        detectHorizontalDragGestures { _, _ -> }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = key,
                    color = if (isSelected) YanbaoPink else Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ISO 刻度拨盘
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun IsoDialControl(
    value: Int,
    onChange: (Int) -> Unit
) {
    val isoValues = listOf(50, 100, 200, 400, 800, 1600, 3200, 6400)
    val selectedIndex = isoValues.indexOfFirst { it >= value }.coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ISO  $value",
            color = YanbaoPink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TickMarkSlider(
            values = isoValues.map { it.toString() },
            selectedIndex = selectedIndex,
            onSelect = { onChange(isoValues[it]) },
            accentColor = YanbaoPink
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 快门速度选择器
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ShutterSpeedSelector(
    value: String,
    onChange: (String) -> Unit
) {
    val selectedIndex = SHUTTER_SPEEDS.indexOf(value).coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "快门  $value",
            color = YanbaoPink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TickMarkSlider(
            values = SHUTTER_SPEEDS,
            selectedIndex = selectedIndex,
            onSelect = { onChange(SHUTTER_SPEEDS[it]) },
            accentColor = YanbaoPink
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 白平衡控制器
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WhiteBalanceControl(
    kelvin: Int,
    onChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "白平衡  ${if (kelvin == 0) "自动" else "${kelvin}K"}",
            color = YanbaoPink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        // 白平衡预设快捷按钮
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(WhiteBalancePreset.values().toList()) { preset ->
                val isSelected = kelvin == preset.kelvin
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) YanbaoPink.copy(alpha = 0.25f)
                            else Color.White.copy(alpha = 0.08f)
                        )
                        .pointerInput(preset) {
                            detectHorizontalDragGestures { _, _ -> }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.label,
                        color = if (isSelected) YanbaoPink else Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 对焦距离控制器
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FocusDistanceControl(
    value: Float,
    onChange: (Float) -> Unit
) {
    val focusLabels = listOf("∞", "5m", "2m", "1m", "0.5m", "近")
    val selectedIndex = (value * (focusLabels.size - 1)).toInt().coerceIn(0, focusLabels.size - 1)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "对焦  ${focusLabels[selectedIndex]}",
            color = YanbaoPink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TickMarkSlider(
            values = focusLabels,
            selectedIndex = selectedIndex,
            onSelect = { onChange(it.toFloat() / (focusLabels.size - 1)) },
            accentColor = YanbaoPink
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 曝光补偿控制器（胡萝卜 Thumb）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExposureCompensationControl(
    value: Float,
    onChange: (Float) -> Unit
) {
    val evText = if (value >= 0) "+${String.format("%.1f", value)} EV" else "${String.format("%.1f", value)} EV"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "曝光补偿", color = YanbaoPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = evText, color = Color.White, fontSize = 12.sp)
        }

        // 曝光补偿滑块（-3.0 ~ +3.0，胡萝卜 Thumb）
        CarrotSlider(
            value = (value + 3f) / 6f,  // 归一化到 0-1
            onValueChange = { normalized -> onChange(normalized * 6f - 3f) },
            modifier = Modifier.fillMaxWidth().height(40.dp)
        )

        // EV 刻度标注
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("-3", "-2", "-1", "0", "+1", "+2", "+3").forEach { label ->
                Text(
                    text = label,
                    color = if (label == "0") YanbaoPink else Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 通用刻度线选择器
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TickMarkSlider(
    values: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    accentColor: Color
) {
    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(values.size) {
                    detectHorizontalDragGestures(
                        onDragEnd = { dragOffset = 0f },
                        onHorizontalDrag = { _, delta ->
                            dragOffset += delta
                            val itemWidth = size.width / values.size.toFloat()
                            val newIndex = (selectedIndex - (dragOffset / itemWidth).toInt())
                                .coerceIn(0, values.size - 1)
                            if (newIndex != selectedIndex) {
                                onSelect(newIndex)
                                dragOffset = 0f
                            }
                        }
                    )
                }
        ) {
            val itemWidth = size.width / values.size.toFloat()
            val centerY = size.height / 2f

            // 轨道线
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = 1.dp.toPx()
            )

            // 刻度和标签
            values.forEachIndexed { index, label ->
                val x = itemWidth * index + itemWidth / 2f
                val isSelected = index == selectedIndex
                val tickHeight = if (isSelected) 14.dp.toPx() else 8.dp.toPx()
                val tickColor = if (isSelected) accentColor else Color.White.copy(alpha = 0.4f)

                // 刻度线
                drawLine(
                    color = tickColor,
                    start = Offset(x, centerY - tickHeight / 2f),
                    end = Offset(x, centerY + tickHeight / 2f),
                    strokeWidth = if (isSelected) 2.dp.toPx() else 1.dp.toPx()
                )

                // 选中指示点
                if (isSelected) {
                    drawCircle(
                        color = accentColor,
                        radius = 3.dp.toPx(),
                        center = Offset(x, centerY + tickHeight / 2f + 5.dp.toPx())
                    )
                }
            }
        }

        // 标签文字（仅显示选中和相邻项）
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            values.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                val isNearby = kotlin.math.abs(index - selectedIndex) <= 2
                Text(
                    text = if (isNearby) label else "",
                    color = if (isSelected) accentColor else Color.White.copy(alpha = 0.4f),
                    fontSize = if (isSelected) 11.sp else 9.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 胡萝卜形状 Thumb 滑块
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 胡萝卜形状 Thumb 滑块
 *
 * 规范：
 * - Track：黑色轨道，左侧胡萝卜橙色填充
 * - Thumb：胡萝卜形状（Canvas 绘制，橙色锥形+绿色叶子）
 * - 拖动时：气泡实时显示数值（黑色 40% 透明）
 */
@Composable
fun CarrotSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    showBubble: Boolean = true
) {
    var isDragging by remember { mutableStateOf(false) }
    var sliderWidth by remember { mutableStateOf(0f) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onHorizontalDrag = { change, _ ->
                            val newValue = (change.position.x / sliderWidth).coerceIn(0f, 1f)
                            onValueChange(newValue)
                        }
                    )
                }
        ) {
            sliderWidth = size.width
            val centerY = size.height / 2f
            val thumbX = value * size.width
            val trackHeight = 4.dp.toPx()
            val thumbRadius = 14.dp.toPx()

            // 轨道背景
            drawRoundRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset(0f, centerY - trackHeight / 2f),
                size = androidx.compose.ui.geometry.Size(size.width, trackHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
            )

            // 轨道填充（胡萝卜橙）
            if (thumbX > 0f) {
                drawRoundRect(
                    color = CarrotOrange,
                    topLeft = Offset(0f, centerY - trackHeight / 2f),
                    size = androidx.compose.ui.geometry.Size(thumbX, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
                )
            }

            // 胡萝卜 Thumb
            drawCarrotThumb(
                center = Offset(thumbX, centerY),
                radius = thumbRadius
            )

            // 数值气泡（拖动时显示）
            if (isDragging && showBubble) {
                val bubbleWidth = 44.dp.toPx()
                val bubbleHeight = 24.dp.toPx()
                val bubbleY = centerY - thumbRadius - bubbleHeight - 4.dp.toPx()
                val bubbleX = (thumbX - bubbleWidth / 2f).coerceIn(0f, size.width - bubbleWidth)

                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.65f),
                    topLeft = Offset(bubbleX, bubbleY),
                    size = androidx.compose.ui.geometry.Size(bubbleWidth, bubbleHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )
            }
        }
    }
}

/**
 * 绘制胡萝卜形状 Thumb
 */
private fun DrawScope.drawCarrotThumb(
    center: Offset,
    radius: Float
) {
    // 外发光
    drawCircle(
        color = CarrotOrange.copy(alpha = 0.3f),
        radius = radius + 4.dp.toPx(),
        center = center
    )

    // 胡萝卜主体（橙色锥形，用圆形近似）
    drawCircle(
        color = CarrotOrange,
        radius = radius,
        center = center
    )

    // 胡萝卜纹路（白色横线）
    val lineColor = Color.White.copy(alpha = 0.4f)
    val lineStroke = 1.dp.toPx()
    listOf(-0.3f, 0f, 0.3f).forEach { offset ->
        drawLine(
            color = lineColor,
            start = Offset(center.x - radius * 0.6f, center.y + radius * offset),
            end = Offset(center.x + radius * 0.6f, center.y + radius * offset),
            strokeWidth = lineStroke
        )
    }

    // 胡萝卜叶子（绿色，顶部）
    val leafColor = Color(0xFF34C759)
    val leafY = center.y - radius + 2.dp.toPx()
    drawLine(
        color = leafColor,
        start = Offset(center.x, leafY),
        end = Offset(center.x - 4.dp.toPx(), leafY - 6.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = leafColor,
        start = Offset(center.x, leafY),
        end = Offset(center.x, leafY - 7.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = leafColor,
        start = Offset(center.x, leafY),
        end = Offset(center.x + 4.dp.toPx(), leafY - 6.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
}
