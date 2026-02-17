package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.components.KuromiCorners

/**
 * 改进的EditScreen - 完全匹配设计图
 * 包含三层嵌套编辑（基础、滤镜、高级）
 */
@Composable
fun EditScreenImproved(
    imageUri: String = "",
    onNavigateBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(0f) }
    var selectedFilter by remember { mutableStateOf("原图") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA78BFA),
                        Color(0xFFEC4899),
                        Color(0xFFF9A8D4)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏
            TopEditBar(
                onBack = onNavigateBack,
                onCompare = {}
            )
            
            // 图片预览
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEC4899).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Image",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(80.dp)
                )
            }
            
            // 编辑选项卡
            EditTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
            
            // 编辑内容
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> BasicEditContent(
                        brightness = brightness,
                        onBrightnessChange = { brightness = it },
                        contrast = contrast,
                        onContrastChange = { contrast = it },
                        saturation = saturation,
                        onSaturationChange = { saturation = it }
                    )
                    1 -> FilterEditContent(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it }
                    )
                    2 -> AdvancedEditContent()
                }
            }
            
            // 底部按钮
            BottomEditBar(
                onShare = onShare,
                onSave = onSave
            )
        }
        
        // 库洛米装饰
        KuromiCorners()
    }
}

/**
 * 顶部工具栏
 */
@Composable
private fun TopEditBar(
    onBack: () -> Unit,
    onCompare: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                "编辑照片",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onCompare) {
                Icon(
                    Icons.Default.CompareArrows,
                    contentDescription = "Compare",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 编辑选项卡
 */
@Composable
private fun EditTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val tabs = listOf("基础", "滤镜", "高级")
        tabs.forEachIndexed { index, tab ->
            Button(
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == index) Color(0xFFEC4899) else Color.White.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    tab,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 基础编辑内容
 */
@Composable
private fun BasicEditContent(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    contrast: Float,
    onContrastChange: (Float) -> Unit,
    saturation: Float,
    onSaturationChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 亮度
        EditSlider(
            label = "亮度",
            value = brightness,
            onValueChange = onBrightnessChange,
            range = -100f..100f
        )
        
        // 对比度
        EditSlider(
            label = "对比度",
            value = contrast,
            onValueChange = onContrastChange,
            range = -100f..100f
        )
        
        // 饱和度
        EditSlider(
            label = "饱和度",
            value = saturation,
            onValueChange = onSaturationChange,
            range = -100f..100f
        )
    }
}

/**
 * 滤镜编辑内容
 */
@Composable
private fun FilterEditContent(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "选择滤镜",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val filters = listOf(
                "原图", "复古", "黑白", "冷调", "暖调",
                "夜景", "人像", "风景", "美食", "街拍"
            )
            items(filters) { filter ->
                FilterButton(
                    name = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

/**
 * 高级编辑内容
 */
@Composable
private fun AdvancedEditContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 曲线
        EditSlider(
            label = "曲线",
            value = 0f,
            onValueChange = {},
            range = -100f..100f
        )
        
        // HSL调节
        EditSlider(
            label = "色相",
            value = 0f,
            onValueChange = {},
            range = -180f..180f
        )
        
        EditSlider(
            label = "饱和度",
            value = 0f,
            onValueChange = {},
            range = -100f..100f
        )
        
        EditSlider(
            label = "亮度",
            value = 0f,
            onValueChange = {},
            range = -100f..100f
        )
        
        // 局部调整
        Text(
            "局部调整",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.3f)
            )
        ) {
            Text(
                "添加局部调整",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 编辑滑块
 */
@Composable
private fun EditSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                value.toInt().toString(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFEC4899),
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

/**
 * 滤镜按钮
 */
@Composable
private fun FilterButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isSelected) Color(0xFFEC4899) else Color.White.copy(alpha = 0.2f)
            )
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEC4899).copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "预览",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
        
        Text(
            name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * 底部按钮栏
 */
@Composable
private fun BottomEditBar(
    onShare: () -> Unit,
    onSave: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFF9A8D4).copy(alpha = 0.9f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分享按钮
            Button(
                onClick = onShare,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.3f)
                )
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    "分享",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 保存按钮
            Button(
                onClick = onSave,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEC4899)
                )
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Save",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    "保存",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
