package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yanbao.camera.ui.theme.KuromiPink

private val KUROMI_PINK = KuromiPink

@Composable
fun ShutterButton(
    onClick: () -> Unit,
    isVideoMode: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .drawBehind {
                // 最外层光晕
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(KUROMI_PINK.copy(alpha = 0.3f), Color.Transparent),
                        center = center,
                        radius = size.minDimension / 2
                    )
                )
                // 中间虚线圆环（简化，用实线代替）
                drawCircle(
                    color = KUROMI_PINK.copy(alpha = 0.8f),
                    radius = size.minDimension / 2 - 4.dp.toPx(),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isVideoMode) 36.dp else 56.dp)
                .background(if (isVideoMode) Color.Red else Color.White, CircleShape)
                .align(Alignment.Center)
        )
    }
}
