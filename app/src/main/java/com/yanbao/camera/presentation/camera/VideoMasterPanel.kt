package com.yanbao.camera.presentation.camera

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.theme.KUROMI_PINK
import com.yanbao.camera.ui.theme.KUROMI_PURPLE

/**
 * 视频大师控制面板 — 严格按照 19_camera_video_master.png 底部区域还原
 *
 * 布局（底部 25% 毛玻璃面板）：
 *  - 标题：「视频大师」
 *  - 帧率选择：30fps / 60fps / 120fps（三按钮，选中粉色边框）
 *  - 延时间隔滑块（0.5s ~ 10s，默认 2s）
 *  - 总时长滑块（1min ~ 30min，默认 5min）
 *
 * 审计日志：AUDIT_VIDEO: fps=60, timelapse_interval=2s, duration=5min
 */
@Composable
fun VideoMasterPanel(
    selectedFps: Int,
    onFpsSelect: (Int) -> Unit,
    timelapseInterval: Float,
    onTimelapseIntervalChange: (Float) -> Unit,
    totalDuration: Float,
    onTotalDurationChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── 标题 ─────────────────────────────────────────────────────────────
        Text(
            text = "视频大师",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // ── 帧率选择 ──────────────────────────────────────────────────────────
        val fpsList = listOf(30, 60, 120)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fpsList.forEach { fps ->
                val isSelected = selectedFps == fps
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isSelected) KUROMI_PINK.copy(alpha = 0.18f)
                            else Color.White.copy(alpha = 0.06f)
                        )
                        .clickable {
                            onFpsSelect(fps)
                            Log.i("AUDIT_VIDEO", "fps=$fps")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.25f),
                            cornerRadius = CornerRadius(22.dp.toPx()),
                            style = Stroke(width = if (isSelected) 2.dp.toPx() else 1.dp.toPx())
                        )
                    }
                    Text(
                        text = "${fps}fps",
                        color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // ── 延时间隔滑块 ──────────────────────────────────────────────────────
        VideoParamSlider(
            label = "延时间隔",
            valueText = "${String.format("%.1f", timelapseInterval)}s",
            value = (timelapseInterval - 0.5f) / 9.5f,
            onValueChange = { ratio ->
                val newVal = 0.5f + ratio * 9.5f
                onTimelapseIntervalChange(newVal)
                Log.i("AUDIT_VIDEO", "timelapse_interval=${String.format("%.1f", newVal)}s")
            }
        )

        // ── 总时长滑块 ────────────────────────────────────────────────────────
        VideoParamSlider(
            label = "总时长",
            valueText = "${totalDuration.toInt()}min",
            value = (totalDuration - 1f) / 29f,
            onValueChange = { ratio ->
                val newVal = 1f + ratio * 29f
                onTotalDurationChange(newVal)
                Log.i("AUDIT_VIDEO", "duration=${newVal.toInt()}min")
            }
        )
    }
}

/**
 * 视频参数滑块（机械刻度尺风格）
 */
@Composable
fun VideoParamSlider(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var trackWidth by remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueText,
                color = KUROMI_PINK,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
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
                val thumbR = 12.dp.toPx()

                // 背景轨道
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.15f),
                    topLeft = Offset(0f, trackY - trackH / 2),
                    size = Size(size.width, trackH),
                    cornerRadius = CornerRadius(trackH / 2)
                )
                // 填充轨道
                val fillWidth = size.width * value
                if (fillWidth > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(KUROMI_PINK, KUROMI_PURPLE),
                            startX = 0f,
                            endX = fillWidth
                        ),
                        topLeft = Offset(0f, trackY - trackH / 2),
                        size = Size(fillWidth, trackH),
                        cornerRadius = CornerRadius(trackH / 2)
                    )
                }
                // 刻度线
                for (i in 0..20) {
                    val tickX = size.width * i / 20f
                    val isMajor = i % 5 == 0
                    val tickH = if (isMajor) 12.dp.toPx() else 7.dp.toPx()
                    val tickColor = if (i.toFloat() / 20f <= value) KUROMI_PINK else Color.White.copy(alpha = 0.3f)
                    drawLine(
                        color = tickColor,
                        start = Offset(tickX, trackY - trackH / 2 - 3.dp.toPx() - tickH),
                        end = Offset(tickX, trackY - trackH / 2 - 3.dp.toPx()),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                // 拇指
                val thumbX = size.width * value
                drawCircle(color = KUROMI_PINK, radius = thumbR, center = Offset(thumbX, trackY))
                drawCircle(color = Color.White, radius = thumbR - 3.dp.toPx(), center = Offset(thumbX, trackY))
            }
        }
    }
}
