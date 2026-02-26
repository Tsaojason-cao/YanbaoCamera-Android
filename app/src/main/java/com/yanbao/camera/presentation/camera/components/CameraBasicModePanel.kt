package com.yanbao.camera.presentation.camera.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
// 品牌色
// ─────────────────────────────────────────────────────────────────────────────
private val YanbaoPink = Color(0xFFEC4899)
private val CarrotOrange = Color(0xFFF97316)
private val ObsidianBlack = Color(0xFF0A0A0A)
private val GlassWhite = Color(0x33FFFFFF)

// ─────────────────────────────────────────────────────────────────────────────
// 数据类
// ─────────────────────────────────────────────────────────────────────────────

enum class AspectRatio(val label: String, val ratio: Float) {
    RATIO_1_1("1:1", 1f),
    RATIO_3_4("3:4", 3f / 4f),
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_9_16("9:16", 9f / 16f),
    RATIO_FULL("全幅", 0f)
}

enum class FlashMode(val label: String) {
    OFF("关"),
    AUTO("自动"),
    ON("开"),
    TORCH("常亮")
}

enum class TimerMode(val label: String, val seconds: Int) {
    OFF("关", 0),
    TIMER_3("3秒", 3),
    TIMER_5("5秒", 5),
    TIMER_10("10秒", 10)
}

enum class GridMode(val label: String) {
    NONE("无"),
    RULE_OF_THIRDS("三分"),
    SQUARE("方形"),
    GOLDEN_RATIO("黄金"),
    DIAGONAL("对角")
}

// ─────────────────────────────────────────────────────────────────────────────
// 相机基本模式控制面板
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 相机基本模式控制面板
 *
 * 对标竞品（B612/轻颜/美颜相机）补全的功能：
 * - 画幅比例切换（1:1 / 3:4 / 4:3 / 9:16 / 全幅）
 * - 定时拍摄（3/5/10秒）
 * - 闪光灯模式（关/自动/开/常亮）
 * - 构图线（三分/方形/黄金比例/对角线）
 * - 水平仪（Canvas 绘制，实时倾斜角度显示）
 * - HDR 开关
 * - 连拍模式
 */
@Composable
fun CameraBasicModePanel(
    aspectRatio: AspectRatio,
    flashMode: FlashMode,
    timerMode: TimerMode,
    gridMode: GridMode,
    isHdrEnabled: Boolean,
    isBurstEnabled: Boolean,
    tiltAngle: Float,  // 水平仪倾斜角度（度）
    onAspectRatioChange: (AspectRatio) -> Unit,
    onFlashModeChange: (FlashMode) -> Unit,
    onTimerModeChange: (TimerMode) -> Unit,
    onGridModeChange: (GridMode) -> Unit,
    onHdrToggle: () -> Unit,
    onBurstToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 第一行：画幅比例 ─────────────────────────────────────────────────
        AspectRatioSelector(
            selected = aspectRatio,
            onSelect = onAspectRatioChange
        )

        // ── 第二行：功能快捷按钮（闪光灯/定时/HDR/连拍/构图线）─────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 闪光灯
            CameraQuickToggle(
                label = "闪光",
                value = flashMode.label,
                isActive = flashMode != FlashMode.OFF,
                onClick = {
                    val next = FlashMode.values()[(flashMode.ordinal + 1) % FlashMode.values().size]
                    onFlashModeChange(next)
                }
            )
            // 定时
            CameraQuickToggle(
                label = "定时",
                value = timerMode.label,
                isActive = timerMode != TimerMode.OFF,
                onClick = {
                    val next = TimerMode.values()[(timerMode.ordinal + 1) % TimerMode.values().size]
                    onTimerModeChange(next)
                }
            )
            // HDR
            CameraQuickToggle(
                label = "HDR",
                value = if (isHdrEnabled) "开" else "关",
                isActive = isHdrEnabled,
                onClick = onHdrToggle
            )
            // 连拍
            CameraQuickToggle(
                label = "连拍",
                value = if (isBurstEnabled) "开" else "关",
                isActive = isBurstEnabled,
                onClick = onBurstToggle
            )
            // 构图线
            CameraQuickToggle(
                label = "构图",
                value = gridMode.label,
                isActive = gridMode != GridMode.NONE,
                onClick = {
                    val next = GridMode.values()[(gridMode.ordinal + 1) % GridMode.values().size]
                    onGridModeChange(next)
                }
            )
        }

        // ── 第三行：水平仪 ───────────────────────────────────────────────────
        HorizonLevelIndicator(tiltAngle = tiltAngle)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 画幅比例选择器
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AspectRatioSelector(
    selected: AspectRatio,
    onSelect: (AspectRatio) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AspectRatio.values().forEach { ratio ->
            val isSelected = ratio == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) YanbaoPink.copy(alpha = 0.2f)
                        else GlassWhite.copy(alpha = 0.1f)
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 0.5.dp,
                        color = if (isSelected) YanbaoPink else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(ratio) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = ratio.label,
                    color = if (isSelected) YanbaoPink else Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 相机快捷功能按钮
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CameraQuickToggle(
    label: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) YanbaoPink.copy(alpha = 0.15f)
                else Color.White.copy(alpha = 0.05f)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = value,
            color = if (isActive) YanbaoPink else Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 水平仪（Canvas 绘制）
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 水平仪组件
 *
 * 使用 Canvas 绘制：
 * - 中心固定刻度线（白色）
 * - 移动气泡（品牌粉，水平时变绿）
 * - 倾斜角度文字显示
 */
@Composable
fun HorizonLevelIndicator(
    tiltAngle: Float,
    modifier: Modifier = Modifier
) {
    val isLevel = abs(tiltAngle) < 1.5f
    val bubbleColor by animateColorAsState(
        targetValue = if (isLevel) Color(0xFF34C759) else YanbaoPink,
        animationSpec = tween(300),
        label = "bubble_color"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
        ) {
            val centerY = size.height / 2f
            val centerX = size.width / 2f
            val trackWidth = size.width * 0.85f
            val trackLeft = (size.width - trackWidth) / 2f
            val trackRight = trackLeft + trackWidth

            // 刻度轨道
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(trackLeft, centerY),
                end = Offset(trackRight, centerY),
                strokeWidth = 1.5.dp.toPx()
            )

            // 中心刻度（长）
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(centerX, centerY - 8.dp.toPx()),
                end = Offset(centerX, centerY + 8.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )

            // 两侧刻度（短）
            listOf(-0.25f, -0.125f, 0.125f, 0.25f).forEach { offset ->
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = Offset(centerX + trackWidth * offset, centerY - 4.dp.toPx()),
                    end = Offset(centerX + trackWidth * offset, centerY + 4.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 移动气泡（根据倾斜角度偏移）
            val maxOffset = trackWidth * 0.4f
            val bubbleOffset = (tiltAngle / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)
            val bubbleRadius = 6.dp.toPx()

            drawCircle(
                color = bubbleColor.copy(alpha = 0.3f),
                radius = bubbleRadius + 3.dp.toPx(),
                center = Offset(centerX + bubbleOffset, centerY)
            )
            drawCircle(
                color = bubbleColor,
                radius = bubbleRadius,
                center = Offset(centerX + bubbleOffset, centerY)
            )
        }

        // 角度文字
        Text(
            text = if (isLevel) "水平" else "${String.format("%.1f", abs(tiltAngle))}°",
            color = if (isLevel) Color(0xFF34C759) else Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 取景器构图线叠加层
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 取景器构图线叠加层（绘制在 CameraX PreviewView 上方）
 */
@Composable
fun ViewfinderGridOverlay(
    gridMode: GridMode,
    modifier: Modifier = Modifier
) {
    if (gridMode == GridMode.NONE) return

    Canvas(modifier = modifier.fillMaxSize()) {
        when (gridMode) {
            GridMode.RULE_OF_THIRDS -> drawRuleOfThirdsGrid()
            GridMode.SQUARE -> drawSquareGrid()
            GridMode.GOLDEN_RATIO -> drawGoldenRatioGrid()
            GridMode.DIAGONAL -> drawDiagonalGrid()
            GridMode.NONE -> Unit
        }
    }
}

private fun DrawScope.drawRuleOfThirdsGrid() {
    val gridColor = Color.White.copy(alpha = 0.35f)
    val strokeWidth = 0.8.dp.toPx()
    // 三分线（横）
    listOf(1f / 3f, 2f / 3f).forEach { ratio ->
        drawLine(gridColor, Offset(0f, size.height * ratio), Offset(size.width, size.height * ratio), strokeWidth)
    }
    // 三分线（竖）
    listOf(1f / 3f, 2f / 3f).forEach { ratio ->
        drawLine(gridColor, Offset(size.width * ratio, 0f), Offset(size.width * ratio, size.height), strokeWidth)
    }
    // 交叉点标记
    listOf(1f / 3f, 2f / 3f).forEach { xRatio ->
        listOf(1f / 3f, 2f / 3f).forEach { yRatio ->
            val cx = size.width * xRatio
            val cy = size.height * yRatio
            drawCircle(Color.White.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = Offset(cx, cy))
        }
    }
}

private fun DrawScope.drawSquareGrid() {
    val gridColor = Color.White.copy(alpha = 0.3f)
    val strokeWidth = 0.8.dp.toPx()
    val step = size.width / 4f
    var x = step
    while (x < size.width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth)
        x += step
    }
    val stepY = size.height / 4f
    var y = stepY
    while (y < size.height) {
        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth)
        y += stepY
    }
}

private fun DrawScope.drawGoldenRatioGrid() {
    val gridColor = Color(0xFFEC4899).copy(alpha = 0.4f)
    val strokeWidth = 0.8.dp.toPx()
    val phi = 0.618f
    // 黄金分割线
    listOf(phi, 1f - phi).forEach { ratio ->
        drawLine(gridColor, Offset(0f, size.height * ratio), Offset(size.width, size.height * ratio), strokeWidth)
        drawLine(gridColor, Offset(size.width * ratio, 0f), Offset(size.width * ratio, size.height), strokeWidth)
    }
}

private fun DrawScope.drawDiagonalGrid() {
    val gridColor = Color.White.copy(alpha = 0.3f)
    val strokeWidth = 0.8.dp.toPx()
    drawLine(gridColor, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth)
    drawLine(gridColor, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth)
    drawLine(gridColor, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height), strokeWidth)
    drawLine(gridColor, Offset(0f, size.height / 2f), Offset(size.width, size.height / 2f), strokeWidth)
}
