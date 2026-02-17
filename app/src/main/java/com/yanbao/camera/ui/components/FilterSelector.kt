package com.yanbao.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.model.Filter
import com.yanbao.camera.ui.theme.AccentPink
import com.yanbao.camera.ui.theme.ButtonPrimary
import com.yanbao.camera.ui.theme.GlassAlpha
import com.yanbao.camera.ui.theme.GlassWhite
import com.yanbao.camera.ui.theme.ProgressPrimary
import com.yanbao.camera.ui.theme.TextDark
import com.yanbao.camera.ui.theme.TextWhite

/**
 * 滤镜选择器组件
 * 
 * 用于在相机和编辑屏幕中选择和调节滤镜
 */
@Composable
fun FilterSelector(
    filters: List<Filter>,
    selectedFilter: Filter,
    intensity: Float = 1.0f,
    onFilterSelected: (Filter) -> Unit,
    onIntensityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 16)
            .padding(16.dp)
    ) {
        // 滤镜名称
        Text(
            text = "滤镜",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 滤镜列表（水平滚动）
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            items(filters) { filter ->
                FilterItem(
                    filter = filter,
                    isSelected = filter.id == selectedFilter.id,
                    onSelected = { onFilterSelected(filter) },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }
        
        // 强度调节滑块
        Text(
            text = "强度: ${(intensity * 100).toInt()}%",
            fontSize = 12.sp,
            color = TextWhite,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Slider(
            value = intensity,
            onValueChange = onIntensityChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            valueRange = 0f..1f,
            steps = 10,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = ProgressPrimary,
                activeTrackColor = ProgressPrimary,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * 单个滤镜项目
 */
@Composable
fun FilterItem(
    filter: Filter,
    isSelected: Boolean = false,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onSelected),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 滤镜预览框
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    color = if (isSelected) ButtonPrimary else GlassWhite.copy(alpha = GlassAlpha),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = filter.name.take(1),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextWhite else TextDark,
                textAlign = TextAlign.Center
            )
        }
        
        // 滤镜名称
        Text(
            text = filter.name,
            fontSize = 11.sp,
            color = TextWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
        )
        
        // 选中指示器
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(AccentPink)
            )
        }
    }
}

/**
 * 滤镜强度调节器
 */
@Composable
fun FilterIntensityControl(
    intensity: Float,
    onIntensityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 12)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "强度",
                fontSize = 12.sp,
                color = TextWhite,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "${(intensity * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ProgressPrimary
            )
        }
        
        Slider(
            value = intensity,
            onValueChange = onIntensityChanged,
            modifier = Modifier.fillMaxWidth(),
            valueRange = 0f..1f,
            steps = 20,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = ProgressPrimary,
                activeTrackColor = ProgressPrimary,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}
