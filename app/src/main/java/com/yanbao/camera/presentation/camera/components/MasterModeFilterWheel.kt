package com.yanbao.camera.presentation.camera.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yanbao.camera.core.util.MasterFilter
import com.yanbao.camera.core.util.MasterModeEngine

/**
 * 大师模式毛玻璃转盘组件
 * 
 * 核心功能：
 * - 半透明毛玻璃背景
 * - 横向滚动的滤镜预览缩略图
 * - 每个滤镜显示实时预览效果
 * - 点击后应用对应的硬件参数
 */
@Composable
fun MasterModeFilterWheel(
    selectedFilterId: String?,
    onFilterSelected: (MasterFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = remember { MasterModeEngine.getAllMasterFilters() }

    // 流光动画
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .blur(20.dp) // 毛玻璃效果
            .padding(16.dp)
    ) {
        // 标题
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ART 大师模式滤镜",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "基于 LBS 位置",
                color = Color(0xFF10B981),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 横向滚动的滤镜转盘
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(filters, key = { it.name }) { filter ->
                MasterFilterItem(
                    filter = filter,
                    isSelected = filter.id == selectedFilterId,
                    glowAlpha = glowAlpha,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

/**
 * 单个大师模式滤镜项
 */
@Composable
fun MasterFilterItem(
    filter: MasterFilter,
    isSelected: Boolean,
    glowAlpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(100.dp)
            .clickable(onClick = onClick)
    ) {
        // 滤镜预览缩略图
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.verticalGradient(filter.params.colorGradient),
                    shape = CircleShape
                )
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    brush = if (isSelected) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFB6C1).copy(alpha = glowAlpha),
                                Color(0xFFE0B0FF).copy(alpha = glowAlpha)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.3f)
                            )
                        )
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // 如果有缩略图 URL，显示图片；否则显示渐变色
            if (filter.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = filter.thumbnailUrl,
                    contentDescription = filter.name,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 显示位置 Emoji
                Text(
                    text = getLocationEmoji(filter.location),
                    fontSize = 32.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 滤镜名称
        Text(
            text = filter.name,
            color = if (isSelected) Color(0xFFFFB6C1) else Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )

        // 位置标签
        Text(
            text = filter.location,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp
        )

        // 如果选中，显示参数预览
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${filter.params.kelvin}K",
                color = Color(0xFF10B981),
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 根据位置返回对应的 Emoji
 */
private fun getLocationEmoji(location: String): String {
    return when {
        location.contains("台北") -> "CITY️"
        location.contains("东京") -> "TWR"
        location.contains("九份") -> "LNT"
        location.contains("日月潭") -> "LAKE"
        location.contains("太鲁阁") -> "MTN️"
        else -> "LOC"
    }
}
