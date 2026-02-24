package com.yanbao.camera.presentation.camera

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.theme.KUROMI_PINK
import com.yanbao.camera.ui.theme.KUROMI_PURPLE
import kotlin.math.log10
import kotlin.math.pow

/**
 * 原相机手动控制面板 — 严格按照 21_camera_manual_controls.png 底部区域还原
 *
 * 布局（底部 25% 毛玻璃面板）：
 *  - 标题：「手动控制」
 *  - ISO 滑块（100~6400，默认 400）
 *  - 快门滑块（1/8000s~30s，默认 1/125s）
 *  - EV 滑块（-3.0~+3.0，默认 +0.3）
 *  - 色温滑块（2000K~8000K，默认 5500K）
 *
 * 审计日志：AUDIT_NATIVE: iso=400, shutter=1/125, ev=+0.3, wb=5500K
 */
@Composable
fun NativeManualControls(
    iso: Int,
    onIsoChange: (Int) -> Unit,
    shutterNs: Long,
    onShutterChange: (Long) -> Unit,
    ev: Float,
    onEvChange: (Float) -> Unit,
    whiteBalance: Int,
    onWhiteBalanceChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── 标题 ─────────────────────────────────────────────────────────────
        Text(
            text = "手动控制",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // ── ISO 滑块 ──────────────────────────────────────────────────────────
        ManualParamSlider(
            label = "ISO",
            valueText = iso.toString(),
            value = isoToSlider(iso),
            onValueChange = { ratio ->
                val newIso = sliderToIso(ratio)
                onIsoChange(newIso)
                Log.i("AUDIT_NATIVE", "iso=$newIso")
            }
        )

        // ── 快门滑块 ──────────────────────────────────────────────────────────
        ManualParamSlider(
            label = "快门",
            valueText = formatShutter(shutterNs),
            value = shutterToSlider(shutterNs),
            onValueChange = { ratio ->
                val newShutter = sliderToShutter(ratio)
                onShutterChange(newShutter)
                Log.i("AUDIT_NATIVE", "shutter=${formatShutter(newShutter)}")
            }
        )

        // ── EV 滑块 ───────────────────────────────────────────────────────────
        ManualParamSlider(
            label = "EV",
            valueText = if (ev >= 0) "+${String.format("%.1f", ev)}" else String.format("%.1f", ev),
            value = (ev + 3f) / 6f,
            onValueChange = { ratio ->
                val newEv = -3f + ratio * 6f
                onEvChange(newEv)
                val evStr = if (newEv >= 0) "+${String.format("%.1f", newEv)}" else String.format("%.1f", newEv)
                Log.i("AUDIT_NATIVE", "ev=$evStr")
            }
        )

        // ── 色温滑块 ──────────────────────────────────────────────────────────
        ManualParamSlider(
            label = "色温",
            valueText = "${whiteBalance}K",
            value = (whiteBalance - 2000f) / 6000f,
            onValueChange = { ratio ->
                val newWb = (2000 + ratio * 6000).toInt()
                onWhiteBalanceChange(newWb)
                Log.i("AUDIT_NATIVE", "wb=${newWb}K")
            }
        )
    }
}

/**
 * 手动参数滑块（机械刻度尺风格，粉→紫渐变）
 */
@Composable
fun ManualParamSlider(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var trackWidth by remember { mutableFloatStateOf(0f) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 标签
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(40.dp)
        )
        // 滑块
        Box(
            modifier = Modifier
                .weight(1f)
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
                val trackH = 7.dp.toPx()
                val thumbR = 11.dp.toPx()

                // 背景轨道
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.15f),
                    topLeft = Offset(0f, trackY - trackH / 2),
                    size = Size(size.width, trackH),
                    cornerRadius = CornerRadius(trackH / 2)
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
                        size = Size(fillWidth, trackH),
                        cornerRadius = CornerRadius(trackH / 2)
                    )
                }
                // 刻度线（21 条）
                for (i in 0..20) {
                    val tickX = size.width * i / 20f
                    val isMajor = i % 5 == 0
                    val tickH = if (isMajor) 11.dp.toPx() else 7.dp.toPx()
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
        // 当前值
        Text(
            text = valueText,
            color = KUROMI_PINK,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(56.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

/**
 * 原相机取景器参数覆盖层 — 显示 f/1.8  1/125  ISO 400  5500K
 */
@Composable
fun NativeParamsOverlay(
    iso: Int,
    shutterNs: Long,
    ev: Float,
    whiteBalance: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("f/1.8", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(formatShutter(shutterNs), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("ISO $iso", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("${whiteBalance}K", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ── 工具函数 ──────────────────────────────────────────────────────────────────

fun isoToSlider(iso: Int): Float {
    val logMin = log10(100.0)
    val logMax = log10(6400.0)
    return ((log10(iso.toDouble()) - logMin) / (logMax - logMin)).toFloat().coerceIn(0f, 1f)
}

fun sliderToIso(ratio: Float): Int {
    val logMin = log10(100.0)
    val logMax = log10(6400.0)
    return 10.0.pow(logMin + ratio * (logMax - logMin)).toInt().coerceIn(100, 6400)
}

fun shutterToSlider(shutterNs: Long): Float {
    val minNs = 125_000L       // 1/8000s
    val maxNs = 30_000_000_000L // 30s
    val logMin = log10(minNs.toDouble())
    val logMax = log10(maxNs.toDouble())
    return ((log10(shutterNs.toDouble()) - logMin) / (logMax - logMin)).toFloat().coerceIn(0f, 1f)
}

fun sliderToShutter(ratio: Float): Long {
    val minNs = 125_000L
    val maxNs = 30_000_000_000L
    val logMin = log10(minNs.toDouble())
    val logMax = log10(maxNs.toDouble())
    return 10.0.pow(logMin + ratio * (logMax - logMin)).toLong().coerceIn(minNs, maxNs)
}

fun formatShutter(shutterNs: Long): String {
    val seconds = shutterNs / 1_000_000_000.0
    return when {
        seconds >= 1.0 -> String.format("%.1fs", seconds)
        else -> "1/${(1.0 / seconds).toInt()}"
    }
}
