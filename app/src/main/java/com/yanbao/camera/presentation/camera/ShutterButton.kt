package com.yanbao.camera.presentation.camera

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.yanbao.camera.ui.theme.KUROMI_PINK

/**
 * 快门按钮 — 严格按照 ui_main_camera.png 还原
 *
 * 特征：
 * - 外层：粉色光晕（radialGradient）
 * - 中层：粉色圆环（Stroke 2dp）
 * - 顶部：两个库洛米耳朵（圆角三角形）
 * - 内层：白色圆形（拍照模式）/ 红色圆角方形（录像模式）
 * - 光晕呼吸动画（无限循环）
 */
@Composable
fun ShutterButton(
    onClick: () -> Unit,
    isVideoMode: Boolean,
    isRecording: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 呼吸光晕动画
    val infiniteTransition = rememberInfiniteTransition(label = "shutter_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .drawBehind {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension / 2f

                // 外层呼吸光晕
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KUROMI_PINK.copy(alpha = glowAlpha),
                            KUROMI_PINK.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = r
                    )
                )

                // 粉色圆环（Stroke）
                drawCircle(
                    color = KUROMI_PINK.copy(alpha = 0.90f),
                    radius = r - 4.dp.toPx(),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // 库洛米耳朵（左耳）
                val earW = 9.dp.toPx()
                val earH = 10.dp.toPx()
                val earOffX = 10.dp.toPx()
                val earTopY = cy - r + 1.dp.toPx()

                val leftEar = Path().apply {
                    moveTo(cx - earOffX - earW / 2, earTopY + earH)
                    quadraticBezierTo(
                        cx - earOffX - earW, earTopY,
                        cx - earOffX, earTopY + earH * 0.3f
                    )
                    quadraticBezierTo(
                        cx - earOffX + earW * 0.2f, earTopY + earH * 0.6f,
                        cx - earOffX - earW / 2, earTopY + earH
                    )
                    close()
                }
                drawPath(leftEar, color = KUROMI_PINK.copy(alpha = 0.90f))

                // 库洛米耳朵（右耳）
                val rightEar = Path().apply {
                    moveTo(cx + earOffX + earW / 2, earTopY + earH)
                    quadraticBezierTo(
                        cx + earOffX + earW, earTopY,
                        cx + earOffX, earTopY + earH * 0.3f
                    )
                    quadraticBezierTo(
                        cx + earOffX - earW * 0.2f, earTopY + earH * 0.6f,
                        cx + earOffX + earW / 2, earTopY + earH
                    )
                    close()
                }
                drawPath(rightEar, color = KUROMI_PINK.copy(alpha = 0.90f))
            }
            .clickable { onClick() }
    ) {
        // 内层：拍照=白色圆形，录像中=红色圆角方形，录像待机=红色圆形
        Box(
            modifier = Modifier
                .size(
                    when {
                        isVideoMode && isRecording -> 28.dp
                        isVideoMode -> 40.dp
                        else -> 54.dp
                    }
                )
                .background(
                    color = when {
                        isVideoMode -> Color.Red
                        else -> Color.White
                    },
                    shape = when {
                        isVideoMode && isRecording -> RoundedCornerShape(6.dp)
                        else -> CircleShape
                    }
                )
                .align(Alignment.Center)
        )
    }
}
