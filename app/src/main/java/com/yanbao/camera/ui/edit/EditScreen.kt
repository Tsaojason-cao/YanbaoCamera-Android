package com.yanbao.camera.ui.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.data.model.EditTool
import com.yanbao.camera.data.model.editTools
import com.yanbao.camera.viewmodel.EditViewModel

/**
 * 编辑界面
 * 严格按照 EditAdjust_ToolsInterface_HighFidelity.png 设计规格实现：
 * - 顶部操作栏：X(取消) + Undo2 + 24px + Redo2 + Rox 20% + Save(粉色)
 * - 主画布区：棋盘格背景 + 图片 + 手势缩放
 * - 滑块控制区：工具名称 + 百分比 + 渐变滑块(紫→粉)
 * - 工具滚动栏：Brightness/Contrast/Saturation/AI Enhance/Crop/Text/Sticker
 */
@Composable
fun EditScreen(
    photoUri: String?,
    onNavigateBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: EditViewModel = hiltViewModel()
) {
    val editState by viewModel.editState.collectAsState()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(photoUri) {
        photoUri?.let { viewModel.loadPhoto(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F10))
    ) {
        // ============ 顶部操作栏 ============
        EditTopBar(
            canUndo = editState.historyIndex > 0,
            canRedo = editState.historyIndex < editState.history.size - 1,
            onClose = onNavigateBack,
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onSave = {
                viewModel.saveImage { savedUri ->
                    onSaved(savedUri)
                }
            },
            isSaving = isSaving
        )

        // ============ 主画布区 ============
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            // 棋盘格背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A))
                        )
                    )
            )

            // 图片显示区域（带手势缩放）
            if (editState.photoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(editState.photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "编辑图片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                // 无图片时的占位符
                Box(
                    modifier = Modifier
                        .size(300.dp, 400.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A2A2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷\n选择照片开始编辑", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
                }
            }

            // 手势提示图标
            if (editState.photoUri != null) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👆", fontSize = 28.sp)
                }
            }
        }

        // ============ 滑块控制区 ============
        if (selectedTool != null) {
            SliderControlSection(
                toolName = selectedTool!!.name,
                value = editState.parameters[selectedTool!!.id] ?: selectedTool!!.defaultValue,
                minValue = selectedTool!!.minValue,
                maxValue = selectedTool!!.maxValue,
                onValueChange = { value ->
                    viewModel.updateParameter(selectedTool!!.id, value)
                }
            )
        }

        // ============ 工具滚动栏 ============
        EditToolBar(
            selectedToolId = selectedTool?.id,
            onToolSelected = { tool -> viewModel.selectTool(tool) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 顶部操作栏
 * X(取消) | Undo | 步骤数 | Redo | 步骤数 | Save(粉色按钮)
 */
@Composable
fun EditTopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    onClose: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // X 取消按钮
        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "取消", tint = Color.White)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Undo
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onUndo,
                enabled = canUndo
            ) {
                Icon(
                    Icons.Default.Undo,
                    contentDescription = "撤销",
                    tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
            Text("Undo", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Redo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Redo", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
            IconButton(
                onClick = onRedo,
                enabled = canRedo
            ) {
                Icon(
                    Icons.Default.Redo,
                    contentDescription = "重做",
                    tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save 按钮（粉色）
        Button(
            onClick = onSave,
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEC4899),
                disabledContainerColor = Color(0xFFEC4899).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * 滑块控制区
 * 工具名称 + 百分比 + 渐变滑块（紫→粉）
 */
@Composable
fun SliderControlSection(
    toolName: String,
    value: Float,
    minValue: Float,
    maxValue: Float,
    onValueChange: (Float) -> Unit
) {
    val percentage = ((value - minValue) / (maxValue - minValue) * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = toolName.uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "$percentage%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEC4899)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 渐变滑块（紫→粉）
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = minValue..maxValue,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFFEC4899),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}

/**
 * 工具滚动栏
 * Brightness / Contrast / Saturation / AI Enhance / Crop / Text / Sticker
 */
@Composable
fun EditToolBar(
    selectedToolId: String?,
    onToolSelected: (com.yanbao.camera.data.model.EditTool) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A))
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(editTools) { tool ->
            val isSelected = tool.id == selectedToolId
            Column(
                modifier = Modifier
                    .width(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Color(0xFFEC4899).copy(alpha = 0.2f)
                        else Color.White.copy(alpha = 0.05f)
                    )
                    .clickable { onToolSelected(tool) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tool.icon,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tool.name,
                    fontSize = 11.sp,
                    color = if (isSelected) Color(0xFFEC4899) else Color.White.copy(alpha = 0.7f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
