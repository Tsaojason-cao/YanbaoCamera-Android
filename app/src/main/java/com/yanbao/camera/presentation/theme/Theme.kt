package com.yanbao.camera.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════
// 雁寶 AI 相机 - 设计 Token（严格对照设计规范）
// 来源：JSON 任务书 + Gemini 审核意见 v1.1
// ═══════════════════════════════════════════════════════════════

// ── 品牌色 ──────────────────────────────────────────────────────
/** 库洛米品牌色 #EC4899 — 快门按钮、选态、数值气泡 */
val PRIMARY_PINK = Color(0xFFEC4899)

/** 全局底色 #0A0A0A — 控制舱背景、底部导航 */
val OBSIDIAN_BLACK = Color(0xFF0A0A0A)

/** 渐变终止色 #9D4EDD — 启动页、主功能卡片 */
val GRADIENT_END_PURPLE = Color(0xFF9D4EDD)

/** 浅紫 #A78BFA — 背景辅助色 */
val LIGHT_PURPLE = Color(0xFFA78BFA)

// ── 渐变 ─────────────────────────────────────────────────────────
/** GRADIENT_KUROMI: #EC4899 → #9D4EDD — 启动页、主功能卡片、进度条 */
val GRADIENT_KUROMI = Brush.linearGradient(
    colors = listOf(PRIMARY_PINK, GRADIENT_END_PURPLE)
)

val GRADIENT_KUROMI_VERTICAL = Brush.verticalGradient(
    colors = listOf(PRIMARY_PINK, GRADIENT_END_PURPLE)
)

// ── 尺寸 Token ───────────────────────────────────────────────────
/** CORNER_RADIUS: 24.dp — 全局卡片圆角、按钮圆角、面板边缘 */
val CORNER_RADIUS = 24.dp

/** BLUR_SIGMA: 40.dp — 28% 控制舱高斯模糊强度 */
val BLUR_SIGMA = 40.dp

// ── 布局比例 ─────────────────────────────────────────────────────
/** 相机预览区占比 72% */
const val CAMERA_PREVIEW_WEIGHT = 0.72f

/** 相机控制舱占比 28% */
const val CAMERA_CONTROL_WEIGHT = 0.28f

// ── 向后兼容别名（不破坏已有引用）───────────────────────────────
val YanbaoPink = PRIMARY_PINK
val YanbaoPurple = GRADIENT_END_PURPLE
val YanbaoDeepBg = OBSIDIAN_BLACK

val YanbaoMainGradient = GRADIENT_KUROMI_VERTICAL

// ═══════════════════════════════════════════════════════════════
// Material Theme
// ═══════════════════════════════════════════════════════════════
private val DarkColorScheme = darkColorScheme(
    primary = PRIMARY_PINK,
    secondary = GRADIENT_END_PURPLE,
    tertiary = LIGHT_PURPLE,
    background = OBSIDIAN_BLACK,
    surface = Color(0xFF16213E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PRIMARY_PINK,
    secondary = GRADIENT_END_PURPLE,
    tertiary = LIGHT_PURPLE,
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
