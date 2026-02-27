package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 2.9D 视差控制面板 — 严格 1:1 还原 CAM_05_parallax.png
 *
 * 布局（底部 28% 曜石黑毛玻璃面板内）：
 *  - 3个参数滑块（左侧标签+数值，粉色轨道+橙色Thumb）：
 *    深度强度 60 / 前景虚化 40 / 视差幅度 75
 *  - 右侧：深度图预览（绿/蓝/橙色分割图，圆角方形 80dp）
 *
 * 颜色规范：
 *  - 滑块轨道：品牌粉 #EC4899
 *  - 数值文字：胡萝卜橙 #F97316
 *  - 深度图预览：绿色背景 + 蓝色人形 + 橙色色块
 */
@Composable
fun Param2_9DPanel(
    parallaxStrength: Float = 0.60f,
    onParallaxStrengthChange: (Float) -> Unit = {},
    selectedPreset: Int = 0,
    onPresetSelect: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val carrotOrange = Color(0xFFF97316)

    var depthStrength by remember { mutableStateOf(0.60f) }
    var foregroundBlur by remember { mutableStateOf(0.40f) }
    var parallaxAmplitude by remember { mutableStateOf(0.75f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── 左侧：3个参数滑块 ─────────────────────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ParallaxParamRow("深度强度", depthStrength) { depthStrength = it }
            ParallaxParamRow("前景虚化", foregroundBlur) { foregroundBlur = it }
            ParallaxParamRow("视差幅度", parallaxAmplitude) { parallaxAmplitude = it }
        }

        // ── 右侧：深度图预览 ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 绿色背景
                drawRect(Color(0xFF4CAF50), Offset.Zero, size)
                // 橙色右上色块
                drawRect(
                    Color(0xFFFF9800),
                    Offset(size.width * 0.5f, 0f),
                    Size(size.width * 0.5f, size.height * 0.5f)
                )
                // 蓝色人形（头部圆形+身体矩形）
                val cx = size.width * 0.35f
                val cy = size.height * 0.42f
                drawCircle(Color(0xFF1565C0), size.width * 0.12f, Offset(cx, cy - size.height * 0.2f))
                drawRoundRect(
                    Color(0xFF1565C0),
                    Offset(cx - size.width * 0.08f, cy - size.height * 0.08f),
                    Size(size.width * 0.16f, size.height * 0.35f),
                    CornerRadius(4.dp.toPx())
                )
            }
        }
    }
}

/**
 * 视差参数行（标签 + 粉色滑块 + 橙色数值）
 * 对应设计图：深度强度 ─────●──── 60
 */
@Composable
fun ParallaxParamRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val brandPink = Color(0xFFEC4899)
    val carrotOrange = Color(0xFFF97316)
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    val displayValue = (value * 100).toInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 标签
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(56.dp)
        )

        // 滑块（粉色轨道 + 橙色Thumb）
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        if (sliderWidth > 0f) {
                            onValueChange((change.position.x / sliderWidth).coerceIn(0f, 1f))
                        }
                    }
                }
        ) {
            sliderWidth = size.width
            val cy = size.height / 2f
            val tx = value * size.width
            val th = 3.dp.toPx()
            val tr = 8.dp.toPx()

            // 背景轨道（粉色低透明）
            drawRoundRect(
                brandPink.copy(alpha = 0.25f),
                Offset(0f, cy - th / 2f),
                Size(size.width, th),
                CornerRadius(th / 2f)
            )
            // 填充轨道（粉色）
            if (tx > 0f) {
                drawRoundRect(
                    brandPink,
                    Offset(0f, cy - th / 2f),
                    Size(tx, th),
                    CornerRadius(th / 2f)
                )
            }
            // Thumb（白色外圈 + 橙色内圈）
            drawCircle(Color.White.copy(alpha = 0.9f), tr + 2.dp.toPx(), Offset(tx, cy))
            drawCircle(carrotOrange, tr, Offset(tx, cy))
        }

        // 数值（橙色）
        Text(
            text = "$displayValue",
            color = carrotOrange,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp)
        )
    }
}

/**
 * 保留旧版兼容函数（避免编译错误）
 */
@Composable
fun ParallaxStrengthSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // 已被 ParallaxParamRow 替代，此处保留签名避免编译错误
    ParallaxParamRow("视差强度", value, onValueChange)
}
