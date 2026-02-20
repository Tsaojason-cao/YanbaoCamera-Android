package com.yanbao.camera.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============ 雁宝品牌颜色规格 ============
// 来源：设计文档颜色规格
// #A78BFA - 主紫色
// #EC4899 - 主粉色
// #F9A8D4 - 浅粉色
// #0F0F10 - 主背景黑色

val YanbaoPurple = Color(0xFFA78BFA)
val YanbaoPink = Color(0xFFEC4899)
val YanbaoLightPink = Color(0xFFF9A8D4)
val YanbaoBlack = Color(0xFF0F0F10)
val YanbaoDarkBg = Color(0xFF1A1A2E)
val YanbaoCardBg = Color(0xFF16213E)

// 主渐变（粉紫渐变背景）
val YanbaoGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1A1A2E),
        Color(0xFF16213E),
        Color(0xFF0F3460)
    )
)

// 品牌渐变（按钮/强调元素）
val YanbaoBrandGradient = Brush.linearGradient(
    colors = listOf(YanbaoPurple, YanbaoPink)
)

// 暗色主题配色
private val DarkColorScheme = darkColorScheme(
    primary = YanbaoPink,
    secondary = YanbaoPurple,
    tertiary = YanbaoLightPink,
    background = YanbaoBlack,
    surface = Color(0xFF1A1A1A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun YanbaoTheme(
    darkTheme: Boolean = true, // 雁宝默认使用暗色主题
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
