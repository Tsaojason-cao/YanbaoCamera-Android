package com.yanbao.camera.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ✅ 1:1 设计稿色值 - 嚴格执行
val YanbaoPink = Color(0xFFFFB6C1)      // 淺粉
val YanbaoPurple = Color(0xFFE0B0FF)    // 淡紫
val YanbaoDeepBg = Color(0xFF121212)    // 沉浸式暗黑背景

// 垂直漸變流光 (用於启动頁和首頁背景)
val YanbaoMainGradient = Brush.verticalGradient(
    colors = listOf(YanbaoPink, YanbaoPurple)
)

private val DarkColorScheme = darkColorScheme(
    primary = YanbaoPink,           // 修正：使用 #FFB6C1
    secondary = YanbaoPurple,       // 修正：使用 #E0B0FF
    tertiary = Color(0xFFF9A8D4),
    background = YanbaoDeepBg,      // 修正：使用 #121212
    surface = Color(0xFF16213E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = YanbaoPink,           // 修正：使用 #FFB6C1
    secondary = YanbaoPurple,       // 修正：使用 #E0B0FF
    tertiary = Color(0xFFF9A8D4),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun YanbaoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
