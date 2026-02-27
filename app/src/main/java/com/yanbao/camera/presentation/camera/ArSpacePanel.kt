package com.yanbao.camera.presentation.camera

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AR 空间贴纸面板 — 严格 1:1 还原 CAM_09_ar.png
 *
 * 布局（底部 28% 曜石黑毛玻璃面板内）：
 *  - 贴纸选择行（6个圆角方形卡片，横向滚动）：
 *    [胡萝卜](选中粉色边框) / [兔耳] / [熊掌] / [心形] / [星星] / [花瓣]
 *    每个卡片：深色 #1E1E1E 背景 + 白色文字图标
 *  - AR强度滑块行：
 *    "AR强度" 标签 + 胡萝卜橙滑块 + 🥕图标在滑块头
 *
 * 取景器覆盖层：
 *  - 顶部居中："AR跟踪中"（粉色胶囊）
 *
 * 颜色规范：
 *  - 选中卡片边框：品牌粉 #EC4899
 *  - 选中文字：品牌粉 #EC4899
 *  - 滑块：胡萝卜橙 #F97316
 */

data class ArSticker(
    val id: Int,
    val name: String,   // 显示名称，如 [胡萝卜]
    val symbol: String  // 卡片内显示的符号（矢量绘制替代）
)

val AR_STICKERS = listOf(
    ArSticker(0, "[胡萝卜]", "🥕"),
    ArSticker(1, "[兔耳]",  "🐰"),
    ArSticker(2, "[熊掌]",  "🐾"),
    ArSticker(3, "[心形]",  "♥"),
    ArSticker(4, "[星星]",  "★"),
    ArSticker(5, "[花瓣]",  "✿")
)

@Composable
fun ArSpacePanel(
    selectedCategory: Int = 0,
    onCategorySelect: (Int) -> Unit = {},
    selectedSticker: Int = 0,
    onStickerSelect: (Int) -> Unit = {},
    lbsLabel: String = "",
    modifier: Modifier = Modifier
) {
    val brandPink = Color(0xFFEC4899)
    val carrotOrange = Color(0xFFF97316)
    var arStrength by remember { mutableStateOf(0.55f) }
    var sliderWidth by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── 贴纸选择行（6个圆角方形卡片）──────────────────────────────────────
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(AR_STICKERS) { _, sticker ->
                val isSelected = selectedSticker == sticker.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        onStickerSelect(sticker.id)
                        Log.i("AUDIT_AR", "sticker_selected=${sticker.name}")
                    }
                ) {
                    // 圆角方形卡片
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E1E))
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 2.dp,
                                    color = brandPink,
                                    shape = RoundedCornerShape(12.dp)
                                ) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sticker.symbol,
                            fontSize = 28.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // 名称标签：选中时粉色，未选中时白色60%
                    Text(
                        text = sticker.name,
                        color = if (isSelected) brandPink else Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // ── AR强度滑块行 ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "AR强度",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(52.dp)
            )

            // 胡萝卜橙滑块（滑块头为胡萝卜图标）
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, _ ->
                                if (sliderWidth > 0f) {
                                    arStrength = (change.position.x / sliderWidth).coerceIn(0f, 1f)
                                }
                            }
                        }
                ) {
                    sliderWidth = size.width
                    val cy = size.height / 2f
                    val tx = arStrength * size.width
                    val th = 3.dp.toPx()

                    // 背景轨道
                    drawRoundRect(
                        carrotOrange.copy(alpha = 0.2f),
                        Offset(0f, cy - th / 2f),
                        Size(size.width, th),
                        CornerRadius(th / 2f)
                    )
                    // 填充轨道（胡萝卜橙）
                    if (tx > 0f) {
                        drawRoundRect(
                            carrotOrange,
                            Offset(0f, cy - th / 2f),
                            Size(tx, th),
                            CornerRadius(th / 2f)
                        )
                    }
                    // Thumb（白色外圈 + 橙色内圈）
                    drawCircle(Color.White.copy(alpha = 0.9f), 10.dp.toPx(), Offset(tx, cy))
                    drawCircle(carrotOrange, 8.dp.toPx(), Offset(tx, cy))
                }
            }
        }
    }
}

/**
 * AR 空间取景器覆盖层 — "AR跟踪中" 粉色胶囊（顶部居中）
 */
@Composable
fun ArViewfinderOverlay(
    lbsLabel: String = "",
    modifier: Modifier = Modifier
) {
    val brandPink = Color(0xFFEC4899)
    Box(modifier = modifier.fillMaxSize()) {
        // "AR跟踪中" 粉色胶囊（顶部居中）
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(brandPink.copy(alpha = 0.85f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "AR跟踪中",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
