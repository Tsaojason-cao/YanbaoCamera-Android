package com.yanbao.camera.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

// ============================================================
// 雁宝 AI 统一设计系统
// ============================================================

/** 主色：库洛米粉 */
val KuromiPink = Color(0xFFEC4899)
/** 次色：深紫 */
val KuromiPurple = Color(0xFF9D4EDD)
/** 背景：曜石黑 */
val ObsidianBlack = Color(0xFF0A0A0A)
/** 深背景 */
val DeepBlack = Color(0xFF1A1A1A)
/** 冷白 */
val ColdWhite = Color(0xFFFFFFFF)
/** 灰色辅助文字 */
val GrayText = Color(0xFF9CA3AF)
/** 毛玻璃背景色 */
val GlassBackground = Color(0xD90A0A0A)  // 85% 透明度曜石黑
/** 毛玻璃浅色 */
val GlassLight = Color(0x33FFFFFF)        // 20% 白色

// ============================================================
// 统一圆角
// ============================================================
object YanbaoRadius {
    val card = 16.dp
    val button = 20.dp
    val panel = 24.dp
    val searchBar = 28.dp
    val chip = 12.dp
    val small = 8.dp
}

// ============================================================
// 统一间距
// ============================================================
object YanbaoSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

// ============================================================
// 统一字体样式
// ============================================================
object YanbaoTypography {
    val brandTitle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = ColdWhite,
        shadow = Shadow(
            color = KuromiPink.copy(alpha = 0.6f),
            offset = Offset(0f, 0f),
            blurRadius = 12f
        )
    )
    val heading = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = ColdWhite
    )
    val body = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = ColdWhite
    )
    val caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = GrayText
    )
    val label = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = ColdWhite
    )
}

// ============================================================
// 毛玻璃背景 Modifier 扩展
// ============================================================
fun Modifier.frostedGlass(
    blurRadius: Dp = 40.dp,
    backgroundColor: Color = GlassBackground
): Modifier = this
    .background(backgroundColor)
    .blur(blurRadius)

// ============================================================
// 品牌标识组件（带呼吸光效）
// ============================================================
@Composable
fun YanbaoBrandTitle(
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    var glowAlpha by remember { mutableFloatStateOf(0.4f) }

    LaunchedEffect(Unit) {
        var t = 0.0
        while (true) {
            t += 0.05
            glowAlpha = (0.4f + 0.4f * sin(t).toFloat()).coerceIn(0.1f, 0.9f)
            delay(50)
        }
    }

    Text(
        text = "yanbao AI",
        style = YanbaoTypography.brandTitle.copy(
            shadow = Shadow(
                color = KuromiPink.copy(alpha = glowAlpha),
                offset = Offset(0f, 0f),
                blurRadius = 16f
            )
        ),
        modifier = modifier
    )
}

// ============================================================
// 统一顶部品牌栏（带返回按钮插槽）
// ============================================================
@Composable
fun YanbaoTopBar(
    modifier: Modifier = Modifier,
    showBrand: Boolean = true,
    leadingContent: @Composable (BoxScope.() -> Unit)? = null,
    trailingContent: @Composable (BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = YanbaoSpacing.md, vertical = YanbaoSpacing.sm)
    ) {
        // 左侧内容（返回按钮等）
        if (leadingContent != null) {
            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                leadingContent()
            }
        }
        // 中央品牌标识
        if (showBrand) {
            YanbaoBrandTitle(modifier = Modifier.align(Alignment.Center))
        }
        // 右侧内容
        if (trailingContent != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                trailingContent()
            }
        }
    }
}

// ============================================================
// 29D 滑块渐变轨道颜色
// ============================================================
val Param29DTrackBrush = Brush.horizontalGradient(
    colors = listOf(KuromiPink, KuromiPurple)
)

// ============================================================
// 毛玻璃面板 Modifier（底部面板专用）
// ============================================================
fun Modifier.yanbaoBottomPanel(): Modifier = this
    .background(
        Brush.verticalGradient(
            colors = listOf(
                Color(0xCC0A0A0A),  // 80% 透明度
                Color(0xF20A0A0A)   // 95% 透明度
            )
        )
    )
    .drawBehind {
        // 顶部粉色分割线（1dp）
        drawLine(
            color = KuromiPink.copy(alpha = 0.3f),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2f
        )
    }
