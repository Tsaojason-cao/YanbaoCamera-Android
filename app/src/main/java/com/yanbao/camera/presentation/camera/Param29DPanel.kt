package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.KUROMI_PINK
import com.yanbao.camera.ui.theme.KUROMI_PURPLE

/**
 * 参数面板 — 严格按照 ui_main_camera.png 底部区域还原
 *
 * 布局：
 *  - 顶部：4个 Tab（曝光 / 色彩 / 纹理 / 美颜），选中粉色下划线
 *  - 下方：3列水平排列的参数滑块（每列：参数名 + 值 + 刻度尺轨道 + 库洛米图标拇指）
 *
 * 设计图中可见的3个滑块（曝光Tab）：
 *   ISO 400  |  EV +0.0  |  K 5500
 */
@Composable
fun Param29DPanel(viewModel: CameraViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("曝光", "色彩", "纹理", "美颜")
    val params by viewModel.params29D.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // ── Tab 行 ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, title ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { selectedTab = index }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index) KUROMI_PINK else Color.White.copy(alpha = 0.55f),
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                    )
                    if (selectedTab == index) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(2.dp)
                                .background(KUROMI_PINK, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── 3列滑块区域 ──────────────────────────────────────────────────────
        when (selectedTab) {
            0 -> { // 曝光：ISO | EV | K(色温)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ISO
                    KuromiParamSlider(
                        label = "ISO",
                        value = params.iso / 6400f,
                        displayValue = "${params.iso}",
                        onValueChange = { viewModel.update29DParam { iso = (it * 6400).toInt().coerceIn(50, 6400) } },
                        modifier = Modifier.weight(1f)
                    )
                    // EV
                    KuromiParamSlider(
                        label = "EV",
                        value = (params.ev + 3f) / 6f,
                        displayValue = "${params.ev.format(1)} EV",
                        onValueChange = { viewModel.update29DParam { ev = (it * 6f - 3f).coerceIn(-3f, 3f) } },
                        modifier = Modifier.weight(1f)
                    )
                    // 色温
                    KuromiParamSlider(
                        label = "K",
                        value = (params.colorTemp - 2000f) / 8000f,
                        displayValue = "${params.colorTemp}",
                        onValueChange = { viewModel.update29DParam { colorTemp = (2000 + it * 8000).toInt().coerceIn(2000, 10000) } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            1 -> { // 色彩：饱和度 | 色调 | 色温
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KuromiParamSlider(
                        label = "饱和度",
                        value = params.saturation / 200f,
                        displayValue = "${params.saturation}%",
                        onValueChange = { viewModel.update29DParam { saturation = (it * 200).toInt() } },
                        modifier = Modifier.weight(1f)
                    )
                    KuromiParamSlider(
                        label = "色调",
                        value = (params.tint + 100f) / 200f,
                        displayValue = "${params.tint}",
                        onValueChange = { viewModel.update29DParam { tint = (it * 200 - 100).toInt() } },
                        modifier = Modifier.weight(1f)
                    )
                    KuromiParamSlider(
                        label = "色温",
                        value = (params.colorTemp - 2000f) / 8000f,
                        displayValue = "${params.colorTemp}K",
                        onValueChange = { viewModel.update29DParam { colorTemp = (2000 + it * 8000).toInt() } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            2 -> { // 纹理：锐度 | 降噪 | 暗角
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KuromiParamSlider(
                        label = "锐度",
                        value = params.sharpness / 100f,
                        displayValue = "${params.sharpness}",
                        onValueChange = { viewModel.update29DParam { sharpness = (it * 100).toInt() } },
                        modifier = Modifier.weight(1f)
                    )
                    KuromiParamSlider(
                        label = "降噪",
                        value = params.denoise / 100f,
                        displayValue = "${params.denoise}",
                        onValueChange = { viewModel.update29DParam { denoise = (it * 100).toInt() } },
                        modifier = Modifier.weight(1f)
                    )
                    KuromiParamSlider(
                        label = "暗角",
                        value = params.vignette / 100f,
                        displayValue = "${params.vignette}",
                        onValueChange = { viewModel.update29DParam { vignette = (it * 100).toInt() } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            3 -> { // 美颜：磨皮 | 美白 | 瘦脸
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KuromiParamSlider(
                        label = "磨皮",
                        value = params.skinSmooth / 100f,
                        displayValue = "${params.skinSmooth}%",
                        onValueChange = { viewModel.update29DParam { skinSmooth = (it * 100).toInt() } },
                        modifier = Modifier.weight(1f)
                    )
                    KuromiParamSlider(
                        label = "美白",
                        value = params.skinWhiten / 100f,
                        displayValue = "${params.skinWhiten}%",
                        onValueChange = { viewModel.update29DParam { skinWhiten = (it * 100).toInt() } },
                        modifier = Modifier.weight(1f)
                    )
                    KuromiParamSlider(
                        label = "瘦脸",
                        value = params.faceThin / 100f,
                        displayValue = "${params.faceThin}%",
                        onValueChange = { viewModel.update29DParam { faceThin = (it * 100).toInt() } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 库洛米风格参数滑块（单列）
// 设计：参数名（上） + 当前值（上右） + 刻度尺轨道（粉→紫渐变）+ 库洛米图标拇指
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun KuromiParamSlider(
    label: String,
    value: Float,
    displayValue: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 参数名 + 当前值
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = displayValue,
                color = KUROMI_PINK,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 刻度尺轨道（Canvas 绘制，粉→紫渐变 + 机械刻度线）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                val trackY = size.height * 0.72f
                val trackH = 4.dp.toPx()
                val trackW = size.width

                // 未激活轨道（灰色）
                drawRoundRect(
                    color = Color(0xFF444444),
                    topLeft = Offset(0f, trackY - trackH / 2),
                    size = androidx.compose.ui.geometry.Size(trackW, trackH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackH / 2)
                )

                // 激活轨道（粉→紫渐变）
                if (value > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(KUROMI_PINK, KUROMI_PURPLE),
                            startX = 0f,
                            endX = trackW * value
                        ),
                        topLeft = Offset(0f, trackY - trackH / 2),
                        size = androidx.compose.ui.geometry.Size(trackW * value, trackH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackH / 2)
                    )
                }

                // 刻度线（机械镜头感）
                val tickCount = 20
                for (i in 0..tickCount) {
                    val tx = trackW * i / tickCount
                    val isMajor = (i % 5 == 0)
                    val tickH = if (isMajor) 8.dp.toPx() else 5.dp.toPx()
                    val tickColor = if (i.toFloat() / tickCount <= value)
                        KUROMI_PINK.copy(alpha = 0.9f)
                    else
                        Color(0xFF666666)
                    drawLine(
                        color = tickColor,
                        start = Offset(tx, trackY - trackH / 2 - 2.dp.toPx()),
                        end = Offset(tx, trackY - trackH / 2 - 2.dp.toPx() - tickH),
                        strokeWidth = if (isMajor) 1.5.dp.toPx() else 0.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 拇指（库洛米图标，使用 Slider 原生拇指位置）
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = KUROMI_PINK,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                thumb = {
                    // 库洛米图标拇指
                    Icon(
                        painter = painterResource(R.drawable.ic_account_kuromi),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}

// 旧版 ParamSlider 保留兼容性（其他地方可能引用）
@Composable
fun ParamSlider(
    name: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    formatValue: (Float) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, color = Color.White, fontSize = 14.sp)
            Text(text = formatValue(value), color = KUROMI_PINK, fontSize = 14.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = KUROMI_PINK,
                activeTrackColor = KUROMI_PINK,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

private fun Float.format(digits: Int) = "%.${digits}f".format(this)
