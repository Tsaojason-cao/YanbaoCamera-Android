package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
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
 * 雁宝记忆模式内嵌面板 — 严格 1:1 还原 CAM_08_memory.png
 *
 * 布局（底部 28% 曜石黑毛玻璃面板内）：
 *  - 记忆卡片（白色圆角卡片）：
 *    左：缩略图（樱花）
 *    右：MEMORY CARD 标题 + 4个标签（滤镜:晨曦 / ISO:200 / 快门:1/250 / 美颜:自然）
 *    按钮：[应用记忆参数](粉色实心) / [选择其他照片](白色描边)
 *  - 底部：🥕 记忆融合度 + 胡萝卜橙滑块（胡萝卜Thumb）
 *
 * 颜色规范：
 *  - 品牌粉：#EC4899
 *  - 胡萝卜橙：#F97316
 *  - 记忆卡片背景：白色 15% 透明
 */
@Composable
fun MemoryModePanel(
    onApplyMemory: () -> Unit = {},
    onSelectOtherPhoto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val brandPink = Color(0xFFEC4899)
    val carrotOrange = Color(0xFFF97316)
    var blendStrength by remember { mutableStateOf(0.5f) }
    var sliderWidth by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── 记忆卡片 ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 左：缩略图（樱花占位）
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFB7C5).copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌸", fontSize = 28.sp)
                }

                // 右：卡片信息
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // MEMORY CARD 标题
                    Text(
                        text = "MEMORY CARD",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // 4个参数标签（粉色描边胶囊）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("滤镜:晨曦", "ISO:200").forEach { tag ->
                            MemoryTag(tag)
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("快门:1/250", "美颜:自然").forEach { tag ->
                            MemoryTag(tag)
                        }
                    }

                    // 两个按钮
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 应用记忆参数（粉色实心）
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(brandPink)
                                .clickable { onApplyMemory() }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "应用记忆参数",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 选择其他照片（白色描边）
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .clickable { onSelectOtherPhoto() }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "选择其他照片",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ── 记忆融合度滑块 ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 🥕 胡萝卜图标
            Text("🥕", fontSize = 16.sp)

            Text(
                text = "记忆融合度",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            blendStrength = (change.position.x / sliderWidth).coerceIn(0f, 1f)
                        }
                    }
            ) {
                sliderWidth = size.width
                val cy = size.height / 2f
                val tx = blendStrength * size.width
                val th = 3.dp.toPx()
                val tr = 10.dp.toPx()

                // 背景轨道
                drawRoundRect(
                    Color.White.copy(alpha = 0.15f),
                    Offset(0f, cy - th / 2f),
                    Size(size.width, th),
                    CornerRadius(th / 2f)
                )
                // 填充轨道（胡萝卜橙）
                if (tx > 0f) {
                    drawRoundRect(
                        carrotOrange,
                        Offset(0f, cy - th / 2f),
                        Size(tx, th),
                        CornerRadius(th / 2f)
                    )
                }
                // 胡萝卜 Thumb
                drawCircle(Color.White.copy(alpha = 0.9f), tr + 2.dp.toPx(), Offset(tx, cy))
                drawCircle(carrotOrange, tr, Offset(tx, cy))
                // 胡萝卜叶子（绿色小点）
                val leafR = 3.dp.toPx()
                drawCircle(Color(0xFF4CAF50), leafR, Offset(tx, cy - tr - leafR))
            }
        }
    }
}

/**
 * 记忆参数标签（粉色描边胶囊）
 */
@Composable
fun MemoryTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFEC4899).copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
