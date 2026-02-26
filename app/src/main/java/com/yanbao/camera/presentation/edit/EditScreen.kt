package com.yanbao.camera.presentation.edit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.yanbao.camera.R

// 颜色定义
private val KUROMI_PINK = Color(0xFFEC4899)
private val OBSIDIAN_BLACK = Color(0xFF0A0A0A)

@Composable
fun EditScreen(
    viewModel: EditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedTool by viewModel.selectedTool.collectAsStateWithLifecycle()
    val showMemoryPanel by viewModel.showMemoryPanel.collectAsStateWithLifecycle()
    val editParams by viewModel.params.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val currentPhotoUri by viewModel.currentPhotoUri.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()

    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val previewHeight = screenHeight * 0.65f
    val controlHeight = screenHeight * 0.35f

    // 照片选择器
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setCurrentPhoto(uri.toString())
            }
        }
    }

    // 保存结果 Toast
    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                android.widget.Toast.makeText(context, "[OK] 已保存到相册", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.resetSaveState()
            }
            is SaveState.Error -> {
                android.widget.Toast.makeText(context, "[ERR] 保存失败：${(saveState as SaveState.Error).message}", android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {

        // ─── Layer 0: 预览区（真实照片加载）──────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight)
                .background(Color(0xFF1A1A1A))
        ) {
            if (currentPhotoUri != null) {
                // 真实照片：通过 Coil 加载 content:// 或 file:// URI
                AsyncImage(
                    model = Uri.parse(currentPhotoUri),
                    contentDescription = "编辑中的照片",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // 实时应用亮度/对比度/饱和度视觉反馈（近似）
                            alpha = 0.5f + editParams.brightness * 0.5f
                            scaleX = 0.95f + editParams.contrast * 0.1f
                            scaleY = 0.95f + editParams.contrast * 0.1f
                        }
                )
            } else {
                // 未选择照片时：显示选择按钮
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_yanbao_gallery),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "从相册选择照片开始编辑",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(KUROMI_PINK, Color(0xFF9D4EDD))
                                )
                            )
                            .clickable {
                                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                photoPickerLauncher.launch(intent)
                            }
                            .padding(horizontal = 28.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "选择照片",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

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

            // 保存中进度指示
            if (saveState is SaveState.Saving) {
                CircularProgressIndicator(
                    color = KUROMI_PINK,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
        }

        // ─── Layer 1: 控制面板（曜石黑毛玻璃）────────────────────────────
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
                    onPickPhoto = {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        photoPickerLauncher.launch(intent)
                    },
                    onMemoryClick = { viewModel.toggleMemoryPanel() }
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

                // 参数调节区（根据选中工具动态显示）
                ParameterPanel(
                    selectedTool = selectedTool,
                    editParams = editParams,
                    onBrightnessChange = { viewModel.setBrightness(it) },
                    onContrastChange = { viewModel.setContrast(it) },
                    onSaturationChange = { viewModel.setSaturation(it) },
                    onTemperatureChange = { viewModel.setTemperature(it) },
                    onSharpnessChange = { viewModel.setSharpness(it) },
                    onFilterIntensityChange = { viewModel.setFilterIntensity(it) }
                )

                Spacer(modifier = Modifier.weight(1f))

                // 底部操作栏
                BottomActionBar(
                    canUndo = canUndo,
                    canRedo = canRedo,
                    isSaving = saveState is SaveState.Saving,
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    onCompare = { /* 对比预览：切换显示原图 */ },
                    onSave = { viewModel.save(context) }
                )
            }
        }

        // ─── Layer 2: 雁宝记忆弹窗 ───────────────────────────────────────
        if (showMemoryPanel) {
            MemoryPanel(
                memories = memories,
                onDismiss = { viewModel.toggleMemoryPanel() },
                onMemorySelect = { memoryId -> viewModel.applyMemory(memoryId) }
            )
        }
    }
}

@Composable
private fun TopToolbar(
    onBackClick: () -> Unit,
    onPickPhoto: () -> Unit,
    onMemoryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_yanbao_back),
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
            IconButton(onClick = onPickPhoto) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_yanbao_gallery),
                    contentDescription = "选择照片",
                    tint = Color.White
                )
            }
            IconButton(onClick = onMemoryClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_yanbao_memory),
                    contentDescription = "雁宝记忆",
                    tint = KUROMI_PINK
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
        items(categories, key = { it.hashCode() }) { category ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onCategorySelected(category) }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = category.displayName,
                    color = if (category == selectedCategory) KUROMI_PINK else Color.White,
                    fontSize = 14.sp,
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
        items(tools, key = { it.hashCode() }) { tool ->
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
                            else Brush.verticalGradient(
                                listOf(Color(0x33FFFFFF), Color(0x22FFFFFF))
                            )
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = tool.iconRes),
                        contentDescription = tool.name,
                        tint = if (tool == selectedTool) Color.White else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
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
    editParams: EditViewModel.EditParams,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onSharpnessChange: (Float) -> Unit,
    onFilterIntensityChange: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        when (selectedTool?.id) {
            "brightness" -> ParamSlider(
                name = "亮度", value = editParams.brightness,
                onValueChange = onBrightnessChange,
                formatValue = { "${((it - 0.5f) * 200).toInt()}" }
            )
            "contrast" -> ParamSlider(
                name = "对比度", value = editParams.contrast,
                onValueChange = onContrastChange,
                formatValue = { "${((it - 0.5f) * 200).toInt()}" }
            )
            "saturation" -> ParamSlider(
                name = "饱和度", value = editParams.saturation,
                onValueChange = onSaturationChange,
                formatValue = { "${((it - 0.5f) * 200).toInt()}" }
            )
            "temp" -> ParamSlider(
                name = "色温", value = editParams.temperature,
                onValueChange = onTemperatureChange,
                formatValue = { "${(it * 10000).toInt()}K" }
            )
            "sharpness" -> ParamSlider(
                name = "清晰度", value = editParams.sharpness,
                onValueChange = onSharpnessChange,
                formatValue = { "${(it * 100).toInt()}" }
            )
            "master" -> ParamSlider(
                name = "滤镜强度", value = editParams.filterIntensity,
                onValueChange = onFilterIntensityChange,
                formatValue = { "${(it * 100).toInt()}%" }
            )
            else -> {
                Text(
                    text = selectedTool?.let { "调节 ${it.name}" } ?: "请选择工具",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
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
    formatValue: (Float) -> String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, color = Color.White, fontSize = 12.sp)
            Text(text = formatValue(value), color = KUROMI_PINK, fontSize = 12.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = KUROMI_PINK,
                activeTrackColor = KUROMI_PINK,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BottomActionBar(
    canUndo: Boolean,
    canRedo: Boolean,
    isSaving: Boolean,
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
            IconButton(onClick = onUndo, enabled = canUndo) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_undo_kuromi),
                    contentDescription = "撤销",
                    tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
            IconButton(onClick = onRedo, enabled = canRedo) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_redo_kuromi),
                    contentDescription = "重做",
                    tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
        }
        TextButton(onClick = onCompare) {
            Text(text = "对比", color = KUROMI_PINK)
        }
        Button(
            onClick = onSave,
            enabled = !isSaving,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = KUROMI_PINK,
                disabledContainerColor = KUROMI_PINK.copy(alpha = 0.5f)
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "保存", color = Color.White)
            }
        }
    }
}

@Composable
private fun MemoryPanel(
    memories: List<MemoryItem>,
    onDismiss: () -> Unit,
    onMemorySelect: (String) -> Unit
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
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .clickable { /* 阻止穿透 */ },
            color = OBSIDIAN_BLACK.copy(alpha = 0.97f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "雁宝记忆",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${memories.size} 条",
                        color = KUROMI_PINK,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (memories.isEmpty()) {
                    // 空状态
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_yanbao_memory),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "暂无雁宝记忆\n拍摄后自动生成",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    // 真实记忆列表
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(memories, key = { it.hashCode() }) { memory ->
                            MemoryCard(
                                memory = memory,
                                onClick = { onMemorySelect(memory.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "取消", color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun MemoryCard(
    memory: MemoryItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1A))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 照片缩略图
        AsyncImage(
            model = if (memory.imagePath.startsWith("content://") || memory.imagePath.startsWith("file://"))
                Uri.parse(memory.imagePath) else memory.imagePath,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A2A2A))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = memory.locationName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${memory.shootingMode} · ${
                    java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(memory.timestamp))
                }",
                color = KUROMI_PINK,
                fontSize = 12.sp
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_apply),
            contentDescription = "套用",
            tint = KUROMI_PINK,
            modifier = Modifier.size(20.dp)
        )
    }
}
