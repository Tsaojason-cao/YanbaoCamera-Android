package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * 摄颜品牌快门键 — 严格 1:1 还原 M3 设计稿
 *
 * 规格（视觉审计确认）：
 *  - 整体尺寸：80dp（含光晕区域）
 *  - 外层霓虹光晕：品牌粉 #EC4899，radial gradient 3层，半径 40dp
 *  - 中层圆环：品牌粉 #EC4899，描边 2.5dp，半径 30dp
 *  - 内圆背景：品牌粉 #EC4899 实心，半径 26dp
 *  - 熊掌印：白色 Canvas 矢量，1大掌心(椭圆)+4小趾(圆)
 *
 * 视频录制中：内圆红色 #FF3B30，中心白色停止方块
 */
@Composable
fun ShutterButton(
    onClick: () -> Unit,
    isVideoMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val brandPink = Color(0xFFEC4899)
    val videoRed  = Color(0xFFFF3B30)
    val glowColor = if (isVideoMode) videoRed else brandPink

    Box(
        modifier = modifier
            .size(80.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            val cx = size.width  / 2f
            val cy = size.height / 2f

            // 外层霓虹光晕
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.40f),
                        glowColor.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = size.minDimension / 2f
                ),
                radius = size.minDimension / 2f,
                center = Offset(cx, cy)
            )

            // 中层圆环
            drawCircle(
                color = glowColor.copy(alpha = 0.90f),
                radius = 30.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // 内圆实心
            drawCircle(
                color = glowColor,
                radius = 26.dp.toPx(),
                center = Offset(cx, cy)
            )

            // 中心图案
            if (isVideoMode) drawStopSquare(cx, cy)
            else             drawPawPrint(cx, cy)
        }
    }
}

/** 白色熊掌印：1个椭圆掌心 + 4个圆趾 */
private fun DrawScope.drawPawPrint(cx: Float, cy: Float) {
    val white = Color.White
    // 掌心（椭圆）
    drawOval(
        color = white,
        topLeft = Offset(cx - 8.5.dp.toPx(), cy - 5.5.dp.toPx()),
        size = Size(17.dp.toPx(), 15.dp.toPx())
    )
    // 4个趾
    val toeR = 3.5.dp.toPx()
    listOf(
        Offset(cx - 9.dp.toPx(),  cy - 11.dp.toPx()),
        Offset(cx - 3.dp.toPx(),  cy - 14.dp.toPx()),
        Offset(cx + 3.dp.toPx(),  cy - 14.dp.toPx()),
        Offset(cx + 9.dp.toPx(),  cy - 11.dp.toPx())
    ).forEach { drawCircle(color = white, radius = toeR, center = it) }
}

/** 白色停止方块（视频录制中） */
private fun DrawScope.drawStopSquare(cx: Float, cy: Float) {
    val half = 7.dp.toPx()
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx - half, cy - half),
        size = Size(half * 2, half * 2),
        cornerRadius = CornerRadius(2.dp.toPx())
    )
}
