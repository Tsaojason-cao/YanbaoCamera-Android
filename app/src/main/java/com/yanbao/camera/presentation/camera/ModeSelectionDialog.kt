package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.KUROMI_PINK

/**
 * 相机模式选择弹窗 — 严格对应 07_camera_03_modes.png
 *
 * 布局：
 *  标题：← 相机模式  🏠
 *  内容：2列×3行 毛玻璃大卡片
 *    照片 | 视频
 *    人像 | 全景
 *    电影效果 | 慢动作
 *  底部：[应用] 按钮
 *
 * 背景：深粉紫渐变 + 光晕效果
 */
@Composable
fun ModeSelectionDialog(
    onDismiss: () -> Unit = {},
    onModeSelected: (String) -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2D1B4E),
                            Color(0xFF1A0A2E)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            KUROMI_PINK.copy(alpha = 0.8f),
                            Color(0xFF9D4EDD).copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // 顶部导航
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_kuromi),
                        contentDescription = "返回",
                        tint = KUROMI_PINK,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onDismiss() }
                    )
                    Text(
                        text = "相机模式",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_home_kuromi),
                        contentDescription = "主页",
                        tint = KUROMI_PINK,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2列×3行 模式卡片
                val modes = listOf(
                    Triple(R.drawable.ic_camera_kuromi, "照片", "Capture high-quality still\nimages with advanced YanBao\nAI enhancements and\noptimized settings."),
                    Triple(R.drawable.ic_mode_video_kuromi, "视频", "Record smooth videos in\nvarious resolutions and frame\nrates, with AI stabilization."),
                    Triple(R.drawable.ic_mode_beauty, "人像", "Create professional-looking\nportraits with artistic bokeh\nand studio lighting effects."),
                    Triple(R.drawable.ic_mode_basic, "全景", "Capture sweeping, wide-angle\nlandscape photos by moving\nthe camera across the scene."),
                    Triple(R.drawable.ic_mode_master, "电影效果", "Record cinematic videos with\nshallow depth of field and\nautomatic focus racking."),
                    Triple(R.drawable.ic_mode_video, "慢动作", "Capture fast-moving action in\nstunning, detailed slow motion.")
                )

                var selectedMode by remember { mutableStateOf("照片") }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (col in 0..1) {
                                val idx = row * 2 + col
                                val (iconRes, label, desc) = modes[idx]
                                val isSelected = selectedMode == label
                                ModeCard(
                                    iconRes = iconRes,
                                    label = label,
                                    description = desc,
                                    isSelected = isSelected,
                                    onClick = { selectedMode = label },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 应用按钮
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    KUROMI_PINK.copy(alpha = 0.2f),
                                    Color(0xFF9D4EDD).copy(alpha = 0.2f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = KUROMI_PINK,
                            shape = RoundedCornerShape(26.dp)
                        )
                        .clickable { onModeSelected(selectedMode) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "应用",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * 单个模式卡片 — 毛玻璃 + 粉色霓虹边框
 * 对应 07_camera_03_modes.png 中每个方形卡片
 */
@Composable
fun ModeCard(
    iconRes: Int,
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected)
                    Brush.verticalGradient(
                        colors = listOf(
                            KUROMI_PINK.copy(alpha = 0.25f),
                            Color(0xFF9D4EDD).copy(alpha = 0.15f)
                        )
                    )
                else
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) KUROMI_PINK else KUROMI_PINK.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = KUROMI_PINK,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}
