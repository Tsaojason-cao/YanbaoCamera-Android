package com.yanbao.camera.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Cyber-Cute Glass 主题系统
 * 品牌核心：紫色(#A78BFA) → 粉色(#EC4899) 渐变
 * 视觉特征：毛玻璃效果 + 极细/极粗字体对比 + 呼吸感动画
 */

// Cyber-Cute Glass 配色方案（深色主题为主）
private val YanbaoDarkColorScheme = darkColorScheme(
    // 主色：紫色渐变起点
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF7C3AED),
    onPrimaryContainer = Color(0xFFEDE9FE),
    
    // 次要色：粉色渐变终点
    secondary = Color(0xFFEC4899),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDB2777),
    onSecondaryContainer = Color(0xFFFCE7F3),
    
    // 第三色：青色点缀
    tertiary = Color(0xFF06B6D4),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF0891B2),
    onTertiaryContainer = Color(0xFFCFFAFE),
    
    // 错误色
    error = Color(0xFFF87171),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFDC2626),
    onErrorContainer = Color(0xFFFEE2E2),
    
    // 背景：深黑色（毛玻璃底色）
    background = Color(0xFF0F0F10),
    onBackground = Color(0xFFF9FAFB),
    
    // 表面：半透明白色（毛玻璃效果）
    surface = Color(0xFF1A1A1C),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF27272A),
    onSurfaceVariant = Color(0xFFD1D5DB),
    
    // 轮廓
    outline = Color(0xFF52525B),
    outlineVariant = Color(0xFF3F3F46),
    
    // 遮罩
    scrim = Color(0xFF000000),
    
    // 反色
    inverseSurface = Color(0xFFE5E7EB),
    inverseOnSurface = Color(0xFF1F2937),
    inversePrimary = Color(0xFF7C3AED)
)

// 浅色主题（保留但不作为主要使用）
private val YanbaoLightColorScheme = darkColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF5B21B6),
    
    secondary = Color(0xFFDB2777),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFCE7F3),
    onSecondaryContainer = Color(0xFF9F1239),
    
    tertiary = Color(0xFF0891B2),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCFFAFE),
    onTertiaryContainer = Color(0xFF164E63),
    
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF18181B),
    
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18181B),
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = Color(0xFF52525B),
    
    outline = Color(0xFFD4D4D8),
    outlineVariant = Color(0xFFE4E4E7),
    
    scrim = Color(0xFF000000),
    
    inverseSurface = Color(0xFF27272A),
    inverseOnSurface = Color(0xFFF4F4F5),
    inversePrimary = Color(0xFFA78BFA)
)

/**
 * 雁寶 AI 相机主题
 * 默认强制使用深色主题（Cyber-Cute Glass 风格）
 */
@Composable
fun YanbaoTheme(
    darkTheme: Boolean = true, // 强制深色主题
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) YanbaoDarkColorScheme else YanbaoLightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 状态栏透明，显示渐变背景
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            // 深色主题下使用浅色图标
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = YanbaoTypography,
        content = content
    )
}

/**
 * 毛玻璃效果颜色（用于 Modifier.background）
 * 使用方式：Modifier.background(GlassBackground)
 */
val GlassBackground = Color(0x0DFFFFFF) // 5% 白色透明度

/**
 * 品牌渐变色（紫 → 粉）
 * 使用方式：Modifier.background(BrandGradient)
 */
val BrandGradientColors = listOf(
    Color(0xFFA78BFA), // 紫色
    Color(0xFFEC4899)  // 粉色
)
