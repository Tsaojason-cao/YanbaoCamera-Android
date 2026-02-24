package com.yanbao.camera.presentation.camera

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.theme.KUROMI_PINK
import com.yanbao.camera.ui.theme.KUROMI_PURPLE

/**
 * 2.9D 视差控制面板 — 严格按照 18_camera_2.9d.png 底部区域还原
 *
 * 布局（底部 25% 毛玻璃面板）：
 *  - 标题：「2.9D 视差控制」
 *  - 视差强度滑块（机械刻度尺风格，0%~100%，默认 65%）
 *  - 场景预设：人像 / 风景 / 艺术（三按钮，选中粉色边框）
 *
 * 审计日志：AUDIT_2.9D: parallax_strength=0.65
 */
@Composable
fun Param2_9DPanel(
    parallaxStrength: Float,
    onParallaxStrengthChange: (Float) -> Unit,
    selectedPreset: Int,
    onPresetSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 标题 ─────────────────────────────────────────────────────────────
        Text(
            text = "2.9D 视差控制",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // ── 视差强度滑块 ──────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "视差强度",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(parallaxStrength * 100).toInt()}%",
                    color = KUROMI_PINK,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            ParallaxStrengthSlider(
                value = parallaxStrength,
                onValueChange = { newVal ->
                    onParallaxStrengthChange(newVal)
                    Log.i("AUDIT_2.9D", "parallax_strength=${String.format("%.2f", newVal)}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            )
        }

        // ── 场景预设 ──────────────────────────────────────────────────────────
        Text(
            text = "场景预设",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        val presets = listOf("人像", "风景", "艺术")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            presets.forEachIndexed { index, label ->
                val isSelected = selectedPreset == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isSelected) KUROMI_PINK.copy(alpha = 0.18f)
                            else Color.White.copy(alpha = 0.06f)
                        )
                        .then(
                            if (isSelected) Modifier.padding(1.dp) else Modifier
                        )
                        .clickable {
                            onPresetSelect(index)
                            Log.i("AUDIT_2.9D", "preset_selected=$label")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Border drawn via Canvas when selected
                    if (isSelected) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRoundRect(
                                color = KUROMI_PINK,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
                        }
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.25f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                    Text(
                        text = label,
                        color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * 机械刻度尺风格视差强度滑块
 * 粉→紫渐变填充轨道，白色圆形拇指
 */
@Composable
fun ParallaxStrengthSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var trackWidth by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    if (trackWidth > 0f) {
                        val newVal = (change.position.x / trackWidth).coerceIn(0f, 1f)
                        onValueChange(newVal)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            trackWidth = size.width
            val trackY = size.height / 2f
            val trackH = 8.dp.toPx()
            val thumbR = 14.dp.toPx()

            // 背景轨道
            drawRoundRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset(0f, trackY - trackH / 2),
                size = androidx.compose.ui.geometry.Size(size.width, trackH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackH / 2)
            )
            // 填充轨道（粉→紫渐变）
            val fillWidth = size.width * value
            if (fillWidth > 0f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(KUROMI_PINK, KUROMI_PURPLE),
                        startX = 0f,
                        endX = fillWidth
                    ),
                    topLeft = Offset(0f, trackY - trackH / 2),
                    size = androidx.compose.ui.geometry.Size(fillWidth, trackH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackH / 2)
                )
            }
            // 刻度线（21 条）
            for (i in 0..20) {
                val tickX = size.width * i / 20f
                val isMajor = i % 5 == 0
                val tickH = if (isMajor) 14.dp.toPx() else 9.dp.toPx()
                val tickColor = if (i.toFloat() / 20f <= value) KUROMI_PINK else Color.White.copy(alpha = 0.3f)
                drawLine(
                    color = tickColor,
                    start = Offset(tickX, trackY - trackH / 2 - 4.dp.toPx() - tickH),
                    end = Offset(tickX, trackY - trackH / 2 - 4.dp.toPx()),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            // 拇指（白色圆形）
            val thumbX = size.width * value
            drawCircle(
                color = KUROMI_PINK,
                radius = thumbR,
                center = Offset(thumbX, trackY)
            )
            drawCircle(
                color = Color.White,
                radius = thumbR - 4.dp.toPx(),
                center = Offset(thumbX, trackY)
            )
        }
    }
}
