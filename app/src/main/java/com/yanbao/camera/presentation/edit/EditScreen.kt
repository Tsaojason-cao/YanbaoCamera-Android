package com.yanbao.camera.presentation.edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanbao.camera.R

// 颜色定义（复用相机模块的）
private val KUROMI_PINK = Color(0xFFEC4899)
private val OBSIDIAN_BLACK = Color(0xFF0A0A0A)

@Composable
fun EditScreen(
    viewModel: EditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedTool by viewModel.selectedTool.collectAsStateWithLifecycle()
    val showMemoryPanel by viewModel.showMemoryPanel.collectAsStateWithLifecycle()
    val brightness by viewModel.brightness.collectAsStateWithLifecycle()
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val previewHeight = screenHeight * 0.7f
    val controlHeight = screenHeight * 0.3f

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        // Layer 0: 预览区
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight)
                .background(Color.DarkGray)
        ) {
            // 占位图片（实际应加载用户图片）
            Image(
                painter = painterResource(id = R.drawable.placeholder_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 品牌标识
            Text(
                text = "yanbao AI",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .graphicsLayer { alpha = 0.9f }
            )

            // 缩放比例提示
            Text(
                text = "100%",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Layer 1: 控制面板（曜石黑毛玻璃）
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(controlHeight)
                .align(Alignment.BottomCenter),
            color = OBSIDIAN_BLACK.copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // 顶部工具栏
                TopToolbar(
                    onBackClick = onNavigateBack,
                    onMemoryClick = { viewModel.toggleMemoryPanel() },
                    onBatchClick = { /* 批量编辑 */ }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 分类导航
                CategoryTabs(
                    categories = ToolCategory.values().toList(),
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 工具网格
                ToolGrid(
                    tools = editTools.filter { it.category == selectedCategory },
                    selectedTool = selectedTool,
                    onToolSelected = { viewModel.selectTool(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 参数调节区
                ParameterPanel(
                    selectedTool = selectedTool,
                    brightness = brightness,
                    onBrightnessChange = { viewModel.setBrightness(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 底部操作栏
                BottomActionBar(
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    onCompare = { /* 对比预览 */ },
                    onSave = { viewModel.save() }
                )
            }
        }

        // 雁宝记忆弹窗（Layer 2）
        if (showMemoryPanel) {
            MemoryPanel(
                onDismiss = { viewModel.toggleMemoryPanel() },
                onMemorySelect = { viewModel.applyMemory("") }
            )
        }
    }
}

@Composable
private fun TopToolbar(
    onBackClick: () -> Unit,
    onMemoryClick: () -> Unit,
    onBatchClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "返回",
                tint = Color.White
            )
        }
        Text(
            text = "编辑",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Row {
            IconButton(onClick = onMemoryClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_memory),
                    contentDescription = "记忆",
                    tint = KUROMI_PINK
                )
            }
            IconButton(onClick = onBatchClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_batch),
                    contentDescription = "批量",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun CategoryTabs(
    categories: List<ToolCategory>,
    selectedCategory: ToolCategory,
    onCategorySelected: (ToolCategory) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onCategorySelected(category) }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = category.displayName,
                    color = if (category == selectedCategory) KUROMI_PINK else Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (category == selectedCategory) FontWeight.Bold else FontWeight.Normal
                )
                if (category == selectedCategory) {
                    Spacer(modifier = Modifier.height(2.dp))
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
}

@Composable
private fun ToolGrid(
    tools: List<EditTool>,
    selectedTool: EditTool?,
    onToolSelected: (EditTool) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(tools) { tool ->
            Column(
                modifier = Modifier
                    .width(64.dp)
                    .clickable { onToolSelected(tool) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (tool == selectedTool)
                                Brush.verticalGradient(listOf(KUROMI_PINK, Color(0xFF9D4EDD)))
                            else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .padding(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = tool.iconRes),
                        contentDescription = tool.name,
                        tint = if (tool == selectedTool) Color.White else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                    )
                }
                Text(
                    text = tool.name,
                    color = if (tool == selectedTool) KUROMI_PINK else Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ParameterPanel(
    selectedTool: EditTool?,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        when (selectedTool?.id) {
            "brightness" -> {
                ParamSlider(
                    name = "亮度",
                    value = brightness,
                    onValueChange = onBrightnessChange,
                    valueRange = 0f..1f,
                    formatValue = { "${(it * 100).toInt()}%" }
                )
            }
            else -> {
                Text(
                    text = selectedTool?.let { "调节 ${it.name}" } ?: "请选择工具",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun ParamSlider(
    name: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    formatValue: (Float) -> String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, color = Color.White, fontSize = 14.sp)
            Text(text = formatValue(value), color = KUROMI_PINK, fontSize = 14.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = KUROMI_PINK,
                activeTrackColor = KUROMI_PINK,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun BottomActionBar(
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onCompare: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            IconButton(onClick = onUndo) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_undo),
                    contentDescription = "撤销",
                    tint = Color.White
                )
            }
            IconButton(onClick = onRedo) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_redo),
                    contentDescription = "重做",
                    tint = Color.White
                )
            }
        }
        Button(
            onClick = onCompare,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(text = "对比", color = KUROMI_PINK)
        }
        Button(
            onClick = onSave,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KUROMI_PINK)
        ) {
            Text(text = "保存", color = Color.White)
        }
    }
}

@Composable
private fun MemoryPanel(
    onDismiss: () -> Unit,
    onMemorySelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = OBSIDIAN_BLACK.copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "雁宝记忆",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                repeat(2) {
                    MemoryCard(
                        title = "台北101·夜景",
                        params = "ISO 800 · 大师·东京",
                        onClick = onMemorySelect
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(text = "取消", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun MemoryCard(
    title: String,
    params: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = params, color = KUROMI_PINK, fontSize = 12.sp)
        }
    }
}
