package com.yanbao.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yanbao.camera.ui.theme.GlassAlpha
import com.yanbao.camera.ui.theme.GlassWhite

/**
 * 毛玻璃效果修饰符
 * 用于卡片、搜索栏、导航栏等元素
 * 
 * 设计规范：
 * - 背景：白色 20% 透明度
 * - 模糊：10dp
 * - 圆角：16dp
 */
fun Modifier.glassEffect(
    blurRadius: Float = 10f,
    cornerRadius: Int = 16,
    alpha: Float = GlassAlpha
): Modifier {
    return this
        .blur(radius = blurRadius.dp)
        .background(
            color = GlassWhite.copy(alpha = alpha),
            shape = RoundedCornerShape(cornerRadius.dp)
        )
}

/**
 * 毛玻璃卡片组件
 * 
 * 使用示例：
 * ```
 * GlassCard(
 *     modifier = Modifier
 *         .fillMaxWidth()
 *         .padding(16.dp)
 * ) {
 *     Text("内容")
 * }
 * ```
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    blurRadius: Float = 10f,
    cornerRadius: Int = 16,
    alpha: Float = GlassAlpha,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.glassEffect(
            blurRadius = blurRadius,
            cornerRadius = cornerRadius,
            alpha = alpha
        ),
        color = Color.Transparent,
        shape = RoundedCornerShape(cornerRadius.dp)
    ) {
        content()
    }
}

/**
 * 粉紫渐变背景
 * 
 * 用于所有屏幕的全局背景
 * 渐变方向：从上到下
 * 颜色：#A78BFA → #EC4899 → #F9A8D4
 */
fun Modifier.gradientBackground(
    startColor: Color = Color(0xFFA78BFA),    // 紫色
    middleColor: Color = Color(0xFFEC4899),  // 粉红色
    endColor: Color = Color(0xFFF9A8D4)      // 浅粉色
): Modifier {
    return this.background(
        brush = Brush.verticalGradient(
            colors = listOf(startColor, middleColor, endColor)
        )
    )
}
