package com.yanbao.camera.presentation.camera.components

import android.hardware.camera2.CaptureRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 滤镜效果数据类
 */
data class FilterEffect(
    val name: String,
    val displayName: String,
    val effectMode: Int
)

/**
 * 5种预设滤镜
 */
val FILTER_EFFECTS = listOf(
    FilterEffect("NONE", "原图", CaptureRequest.CONTROL_EFFECT_MODE_OFF),
    FilterEffect("MONO", "黑白", CaptureRequest.CONTROL_EFFECT_MODE_MONO),
    FilterEffect("SEPIA", "复古", CaptureRequest.CONTROL_EFFECT_MODE_SEPIA),
    FilterEffect("NEGATIVE", "反色", CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE),
    FilterEffect("SOLARIZE", "高对比", CaptureRequest.CONTROL_EFFECT_MODE_SOLARIZE)
)

/**
 * 滤镜选择器组件
 * 
 * @param selectedFilter 当前选中的滤镜
 * @param onFilterSelected 滤镜选择回调
 */
@Composable
fun FilterSelector(
    selectedFilter: FilterEffect,
    onFilterSelected: (FilterEffect) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "大师滤镜",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(FILTER_EFFECTS) { filter ->
                FilterItem(
                    filter = filter,
                    isSelected = filter.name == selectedFilter.name,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

/**
 * 滤镜项组件
 */
@Composable
private fun FilterItem(
    filter: FilterEffect,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 80.dp, height = 60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFFE91E63).copy(alpha = 0.3f)
                else Color.White.copy(alpha = 0.1f)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFE91E63) else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = filter.displayName,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
        )
    }
}
