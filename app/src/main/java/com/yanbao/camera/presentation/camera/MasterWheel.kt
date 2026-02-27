package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 大师模式滤镜转盘 — 严格 1:1 还原 CAM_03_master.png
 *
 * 布局（底部 28% 曜石黑毛玻璃面板内）：
 *  - 滤镜转盘（6个缩略图横向排列）：
 *    极光 / 晨曦 / 山林 / 海风 / 小清新 / 电影感(选中，粉色边框)
 *  - 滤镜强度：胡萝卜橙滑块 + 橙色气泡"75%"
 */
data class FilterPreset(val id: Int, val name: String, val emoji: String, val bgColor: Color)

val MASTER_FILTERS = listOf(
    FilterPreset(0, "极光", "🌌", Color(0xFF1A237E)),
    FilterPreset(1, "晨曦", "🌅", Color(0xFFFF7043)),
    FilterPreset(2, "山林", "🌲", Color(0xFF2E7D32)),
    FilterPreset(3, "海风", "🌊", Color(0xFF0277BD)),
    FilterPreset(4, "小清新", "🌸", Color(0xFFAD1457)),
    FilterPreset(5, "电影感", "🎬", Color(0xFF212121))
)

@Composable
fun MasterWheel(viewModel: CameraViewModel) {
    val brandPink = Color(0xFFEC4899)
    val carrotOrange = Color(0xFFF97316)
    var selectedFilter by remember { mutableStateOf(5) }
    var filterStrength by remember { mutableStateOf(0.75f) }
    var sliderWidth by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── 滤镜转盘（6个缩略图）────────────────────────────────────────────
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(MASTER_FILTERS) { index, filter ->
                val isSelected = selectedFilter == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedFilter = index }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(filter.bgColor.copy(alpha = 0.7f))
                            .then(
                                if (isSelected) Modifier.border(2.dp, brandPink, RoundedCornerShape(10.dp))
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(filter.emoji, fontSize = 24.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = filter.name,
                        color = if (isSelected) brandPink else Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // ── 滤镜强度滑块（胡萝卜橙）─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "滤镜强度",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(60.dp)
            )

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            filterStrength = (change.position.x / sliderWidth).coerceIn(0f, 1f)
                        }
                    }
            ) {
                sliderWidth = size.width
                val cy = size.height / 2f
                val tx = filterStrength * size.width
                val th = 3.dp.toPx()
                val tr = 10.dp.toPx()

                drawRoundRect(
                    Color.White.copy(alpha = 0.15f),
                    Offset(0f, cy - th / 2f),
                    Size(size.width, th),
                    CornerRadius(th / 2f)
                )
                if (tx > 0f) {
                    drawRoundRect(
                        carrotOrange,
                        Offset(0f, cy - th / 2f),
                        Size(tx, th),
                        CornerRadius(th / 2f)
                    )
                }
                drawCircle(Color.White.copy(alpha = 0.9f), tr + 2.dp.toPx(), Offset(tx, cy))
                drawCircle(carrotOrange, tr, Offset(tx, cy))
            }

            // 橙色气泡数值
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(carrotOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${(filterStrength * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
