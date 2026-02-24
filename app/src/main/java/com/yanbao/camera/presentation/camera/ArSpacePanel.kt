package com.yanbao.camera.presentation.camera

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.theme.KUROMI_PINK
import com.yanbao.camera.ui.theme.KUROMI_PURPLE

/**
 * AR 空间贴纸面板 — 严格按照 20_camera_ar.png 底部区域还原
 *
 * 布局（底部 25% 毛玻璃面板）：
 *  - 标题：「AR 贴纸库」
 *  - 分类 Tab：全部 / 库洛米 / 表情 / 场景 / 节日（选中粉色下划线）
 *  - 贴纸列表：横向滚动，选中粉色圆圈边框
 *
 * 审计日志：AUDIT_AR: sticker_selected=库洛米, category=库洛米
 */

data class ArSticker(
    val id: Int,
    val name: String,
    val icon: String,
    val category: String
)

val AR_STICKERS = listOf(
    ArSticker(0, "库洛米", "K", "库洛米"),
    ArSticker(1, "星光", "*", "库洛米"),
    ArSticker(2, "爱心", "♥", "库洛米"),
    ArSticker(3, "樱花", "+", "场景"),
    ArSticker(4, "星星", "☆", "场景"),
    ArSticker(5, "蝴蝶结", "~", "库洛米"),
    ArSticker(6, "彩虹", "◎", "场景"),
    ArSticker(7, "月亮", "☽", "节日"),
    ArSticker(8, "礼物", "□", "节日"),
)

val AR_CATEGORIES = listOf("全部", "库洛米", "表情", "场景", "节日")

@Composable
fun ArSpacePanel(
    selectedCategory: Int,
    onCategorySelect: (Int) -> Unit,
    selectedSticker: Int,
    onStickerSelect: (Int) -> Unit,
    lbsLabel: String = "台北101  500m",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── 标题 ─────────────────────────────────────────────────────────────
        Text(
            text = "AR 贴纸库",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // ── LBS 标签（右上角已在取景器覆盖，此处仅作参考显示） ─────────────────
        // LBS label is displayed in the viewfinder overlay, not in the panel

        // ── 分类 Tab ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AR_CATEGORIES.forEachIndexed { index, category ->
                val isSelected = selectedCategory == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            onCategorySelect(index)
                            Log.i("AUDIT_AR", "category_selected=$category")
                        }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.55f),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(KUROMI_PINK, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }

        // ── 贴纸列表 ──────────────────────────────────────────────────────────
        val filteredStickers = if (selectedCategory == 0) AR_STICKERS
        else AR_STICKERS.filter { it.category == AR_CATEGORIES[selectedCategory] }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(filteredStickers) { _, sticker ->
                val isSelected = selectedSticker == sticker.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            onStickerSelect(sticker.id)
                            Log.i("AUDIT_AR", "sticker_selected=${sticker.name}, category=${sticker.category}")
                        }
                ) {
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background circle
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) KUROMI_PINK.copy(alpha = 0.18f)
                                    else Color.White.copy(alpha = 0.08f)
                                )
                        )
                        // Border
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.3f),
                                style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 1.dp.toPx())
                            )
                        }
                        Text(
                            text = sticker.icon,
                            color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.7f),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sticker.name,
                        color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * AR 空间取景器覆盖层 — LBS 标签 + 贴纸叠加
 */
@Composable
fun ArViewfinderOverlay(
    lbsLabel: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // LBS 标签（右上角）
        if (lbsLabel.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(KUROMI_PURPLE.copy(alpha = 0.55f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = lbsLabel,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
