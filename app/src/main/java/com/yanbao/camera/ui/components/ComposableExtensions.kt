package com.yanbao.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 主色渐变背景
 */
fun Modifier.PrimaryGradient(): Modifier {
    return this.background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFA78BFA),  // 紫色
                Color(0xFFEC4899),  // 粉红色
                Color(0xFFF9A8D4)   // 浅粉色
            )
        )
    )
}

/**
 * Material Design图标包装器
 */
@Composable
fun materialIcon(
    imageVector: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

/**
 * Kuromi占位符 - 用于加载状态或空状态
 */
@Composable
fun KuromiPlaceholder(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 100.dp
) {
    // 简单的占位符实现
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .background(
                color = Color(0xFFFFB6D9),
                shape = RoundedCornerShape(16.dp)
            )
    )
}
