package com.yanbao.camera.presentation.editor

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.presentation.theme.YanbaoPink
import com.yanbao.camera.presentation.theme.YanbaoPurple

/**
 * Yanbao 18 工具矩阵编辑器
 * 
 * Obsidian Flux 设计方案 - Phase 2
 * 
 * 空间分层逻辑：
 * - Layer 0 (Z-0): 72% 编辑画布（支持双指缩放）
 * - Layer 1 (Z-1): 28% 功能抽屉（网格+抽屉双层逻辑）
 * 
 * 设计特征：
 * - 智能滑块：调节时出现对比分割线（左原图，右实时渲染）
 * - 功能矩阵：18 个线性粉色图标，选中工具有库洛米耳朵光晕
 * - 实时数值气泡：显示当前调节值（如：亮度 +15）
 */
@Composable
fun YanbaoEditScreen(
    imageUri: String? = null,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedToolIndex by remember { mutableIntStateOf(0) }
    var adjustmentValue by remember { mutableFloatStateOf(0.5f) }
    
    Box(modifier = modifier.fillMaxSize()) {
        
        // --- Layer 0: 编辑画布 (72%) ---
        EditCanvasLayer(
            imageUri = imageUri,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .align(Alignment.TopCenter)
        )
        
        // --- Layer 1: 功能抽屉 (28%) ---
        EditToolDrawer(
            selectedToolIndex = selectedToolIndex,
            adjustmentValue = adjustmentValue,
            onToolSelected = { selectedToolIndex = it },
            onValueChange = { adjustmentValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.28f)
                .align(Alignment.BottomCenter)
        )
        
        // 顶部返回按钮
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("←", fontSize = 24.sp, color = Color.White)
        }
    }
}

/**
 * Layer 0: 编辑画布层
 * 
 * 支持双指无级缩放
 * 缩放时 UI 自动半透明隐藏
 */
@Composable
fun EditCanvasLayer(
    imageUri: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // TODO: 集成图片显示和缩放
        Text(
            text = "编辑画布\n(支持双指缩放)",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 16.sp
        )
    }
}

/**
 * Layer 1: 功能抽屉
 * 
 * 采用"网格+抽屉"双层逻辑
 * 28% 底部区域
 */
@Composable
fun EditToolDrawer(
    selectedToolIndex: Int,
    adjustmentValue: Float,
    onToolSelected: (Int) -> Unit,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0A0A0A)) // 曜石黑背景
            .padding(bottom = 20.dp, top = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. 实时数值反馈气泡
        RealTimeValueBubble(
            value = adjustmentValue,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 2. 精细调节滑动轴（物理震动反馈）
        SmartAdjustmentSlider(
            value = adjustmentValue,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 3. 18 工具矩阵（横向滚动列表）
        ToolMatrixGrid(
            selectedToolIndex = selectedToolIndex,
            onToolSelected = onToolSelected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 实时数值反馈气泡
 * 
 * 显示当前调节值（如：亮度 +15）
 */
@Composable
fun RealTimeValueBubble(
    value: Float,
    modifier: Modifier = Modifier
) {
    val displayValue = ((value - 0.5f) * 100).toInt()
    val sign = if (displayValue >= 0) "+" else ""
    
    Box(
        modifier = modifier
            .background(
                color = YanbaoPink.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = YanbaoPink.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$sign$displayValue",
            color = YanbaoPink,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 精细调节滑动轴
 * 
 * 带有物理震动反馈
 * 调节时照片中央出现对比分割线
 */
@Composable
fun SmartAdjustmentSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = YanbaoPink,
            activeTrackColor = YanbaoPurple,
            inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
        )
    )
}

/**
 * 18 工具矩阵
 * 
 * 横向滚动列表
 * 线性粉色高亮，选中工具有库洛米耳朵光晕
 */
@Composable
fun ToolMatrixGrid(
    selectedToolIndex: Int,
    onToolSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        "亮度", "对比度", "饱和度", "锐化",
        "色温", "色调", "高光", "阴影",
        "曝光", "白平衡", "颗粒", "晕影",
        "色散", "畸变", "噪点", "清晰度",
        "自然", "29D"
    )
    
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(18) { index ->
            ToolIcon(
                label = tools.getOrElse(index) { "工具$index" },
                isSelected = selectedToolIndex == index,
                onClick = { onToolSelected(index) }
            )
        }
    }
}

/**
 * 工具图标
 * 
 * 选中时显示库洛米耳朵光晕
 */
@Composable
fun ToolIcon(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 库洛米耳朵光晕动画
        val glowAlpha by animateFloatAsState(
            targetValue = if (isSelected) 1f else 0f,
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            label = "glow_alpha"
        )
        
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (isSelected) YanbaoPink.copy(alpha = glowAlpha * 0.3f) else Color.Transparent,
                    shape = CircleShape
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) YanbaoPink else Color.Gray.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨",
                fontSize = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) YanbaoPink else Color.White.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
