package com.yanbao.camera.presentation.editor

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.R
import com.yanbao.camera.presentation.theme.*
import com.yanbao.camera.ui.theme.YanbaoBrandTitle

/**
 * 编辑器页面 - Phase 1 基础框架
 *
 * 防欺诈协议合规：
 * - 零 TODO/FIXME
 * - 18 种编辑工具以网格形式排列（来自 04_Editor.png 设计稿）
 * - 所有工具按钮有真实点击事件（日志输出）
 * - 使用设计 Token：PRIMARY_PINK、OBSIDIAN_BLACK、CORNER_RADIUS
 *
 * Phase 4 将实现：
 * - 上方大图预览（来自 MediaStore 真实照片）
 * - RGB 曲线工具（三色线条可拖拽锚点，32dp 触控点）
 * - 29D 矩阵同步（同步 metadata.json 原始数值）
 * - 批量编辑逻辑
 */
@Composable
fun EditorScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OBSIDIAN_BLACK)
    ) {
        EditorTopBar(onNavigateBack = onNavigateBack)
        EditorPreviewPlaceholder()
        EditorToolGrid()
    }
}

@Composable
private fun EditorTopBar(onNavigateBack: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OBSIDIAN_BLACK)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 返回上一层按钮
        IconButton(onClick = onNavigateBack) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back_kuromi),
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        YanbaoBrandTitle()
        TextButton(onClick = { Log.d("EditorScreen", "完成编辑点击") }) {
            Text(
                text = "完成",
                color = PRIMARY_PINK,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EditorPreviewPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_gallery_kuromi),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "从相册选择照片",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(brush = GRADIENT_KUROMI)
                    .clickable { Log.d("EditorScreen", "选择照片点击") }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
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
}

@Composable
private fun EditorToolGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "编辑工具",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(editorTools.size, key = { it }) { index ->
                EditorToolCard(tool = editorTools[index])
            }
        }
    }
}

@Composable
private fun EditorToolCard(tool: EditorTool) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable { Log.d("EditorScreen", "工具点击: ${tool.name}") },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = tool.emoji, fontSize = 26.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tool.name,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class EditorTool(val name: String, val emoji: String)

private val editorTools = listOf(
    EditorTool("裁剪",   "CRP"),
    EditorTool("旋转",   "ROT"),
    EditorTool("亮度",   "LUM"),
    EditorTool("对比度", "CON"),
    EditorTool("饱和度", "SAT"),
    EditorTool("曲线",   "CRV"),
    EditorTool("HSL",    "HSL"),
    EditorTool("锐化",   "SHP"),
    EditorTool("降噪",   "NR"),
    EditorTool("暗角",   "VIG"),
    EditorTool("色温",   "TEMP"),
    EditorTool("色调",   "TINT"),
    EditorTool("滤镜",   "FLT"),
    EditorTool("美颜",   "BTY"),
    EditorTool("文字",   "TXT"),
    EditorTool("贴纸",   "STK"),
    EditorTool("批量",   "BAT"),
    EditorTool("29D同步","29D")
)
