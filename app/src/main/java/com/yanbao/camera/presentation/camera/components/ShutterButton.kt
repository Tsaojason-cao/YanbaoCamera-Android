package com.yanbao.camera.presentation.camera.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * 雁宝品牌快门按钮 —— 满血版
 *
 * 规范（1:1 还原设计稿）：
 * - 直径：72dp 本体，外层 80dp 含霓虹光晕
 * - 中心：品牌粉 (#EC4899) 熊掌印（Canvas Path 绘制，5 爪）
 * - 外圈：白色半透明 3dp 边框
 * - 按下态：缩放 0.92 弹性动画（Spring）
 * - 霓虹光晕：Radial Gradient 模拟 80dp 粉色辉光
 *
 * 严禁使用 Emoji，所有图形均为 Canvas 矢量绘制。
 */
@Composable
fun ShutterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "shutter_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(80.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f          // 40dp
            val buttonRadius = size.width * 0.45f      // 36dp 按钮本体
            val innerRadius = size.width * 0.38f       // 30.4dp 内圆

            // ── 1. 霓虹光晕（Radial Gradient 模拟）────────────────────────
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFEC4899).copy(alpha = 0.55f),
                        Color(0xFFEC4899).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )

            // ── 2. 外圈白色边框 ──────────────────────────────────────────
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = buttonRadius + 3.dp.toPx(),
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // ── 3. 按钮本体（品牌粉渐变）────────────────────────────────
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF6BB5),   // 亮粉
                        Color(0xFFEC4899),   // 品牌粉
                        Color(0xFFD4267A)    // 深粉
                    ),
                    center = Offset(center.x - buttonRadius * 0.2f, center.y - buttonRadius * 0.2f),
                    radius = buttonRadius * 1.2f
                ),
                radius = buttonRadius,
                center = center
            )

            // ── 4. 熊掌印（Canvas 矢量绘制，5 爪）──────────────────────
            drawBearPaw(
                center = center,
                pawRadius = innerRadius * 0.55f,
                color = Color.White.copy(alpha = 0.92f)
            )
        }
    }
}

/**
 * 绘制熊掌印
 *
 * 由 1 个大圆（掌心）+ 4 个小圆（趾垫）组成，
 * 完全使用 Canvas drawCircle 绘制，无需外部资源。
 *
 * @param center  掌心中心点
 * @param pawRadius 掌心半径
 * @param color   填充颜色
 */
private fun DrawScope.drawBearPaw(
    center: Offset,
    pawRadius: Float,
    color: Color
) {
    // 掌心（主圆，稍微偏下）
    val palmCenter = Offset(center.x, center.y + pawRadius * 0.15f)
    drawCircle(color = color, radius = pawRadius, center = palmCenter)

    // 趾垫：4 个趾垫均匀分布在掌心上方弧线
    val toeRadius = pawRadius * 0.38f
    val toeDistance = pawRadius * 1.05f

    val toeAngles = listOf(-135f, -100f, -80f, -45f)
    toeAngles.forEach { angleDeg ->
        val rad = Math.toRadians(angleDeg.toDouble())
        val toeCenter = Offset(
            x = palmCenter.x + (toeDistance * cos(rad)).toFloat(),
            y = palmCenter.y + (toeDistance * sin(rad)).toFloat()
        )
        drawCircle(color = color, radius = toeRadius, center = toeCenter)
    }
}
