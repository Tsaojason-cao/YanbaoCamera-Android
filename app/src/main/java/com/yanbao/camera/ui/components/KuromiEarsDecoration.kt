package com.yanbao.camera.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill

/**
 * 库洛米耳朵装饰
 * 显示在屏幕顶部两侧
 */
@Composable
fun KuromiEarsDecoration(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val earColor = Color(0xFF808080).copy(alpha = 0.3f)
            val earWidth = 120f
            val earHeight = 150f
            
            // 左耳
            val leftEarPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(earWidth * 0.6f, 0f)
                lineTo(earWidth * 0.4f, earHeight)
                lineTo(0f, earHeight * 0.8f)
                close()
            }
            drawPath(
                path = leftEarPath,
                color = earColor,
                style = Fill
            )
            
            // 右耳
            val rightEarPath = Path().apply {
                moveTo(size.width, 0f)
                lineTo(size.width - earWidth * 0.6f, 0f)
                lineTo(size.width - earWidth * 0.4f, earHeight)
                lineTo(size.width, earHeight * 0.8f)
                close()
            }
            drawPath(
                path = rightEarPath,
                color = earColor,
                style = Fill
            )
        }
    }
}
