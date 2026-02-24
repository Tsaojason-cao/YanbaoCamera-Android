package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
 * 大师模式滤镜弹窗 — 严格对应 13_camera_07_master_popup.png
 *
 * 布局：
 *  标题："YanBao AI Master mode"（白色）
 *  内容：2行×4列 圆形滤镜缩略图（带预览图）+ 名称
 *    第1行：复古 / 赛博 / 清新 / 黑白
 *    第2行：暖阳 / 冷调 / 鲜艳 / 柔焦
 *  左右翻页箭头（< >）
 *  底部：[取消] [应用] 按钮
 *
 * 背景：毛玻璃卡片 + 粉色霓虹边框
 */
data class MasterFilter(
    val id: String,
    val name: String,
    val previewColor: Color  // 用颜色模拟滤镜预览
)

@Composable
fun MasterModePopup(
    onDismiss: () -> Unit = {},
    onApply: (String) -> Unit = {}
) {
    val filters = listOf(
        MasterFilter("vintage", "复古", Color(0xFFD4A574)),
        MasterFilter("cyber", "赛博", Color(0xFF4A1A6B)),
        MasterFilter("fresh", "清新", Color(0xFF7BC8A4)),
        MasterFilter("bw", "黑白", Color(0xFF808080)),
        MasterFilter("warm", "暖阳", Color(0xFFE8A44A)),
        MasterFilter("cool", "冷调", Color(0xFF4A7BC8)),
        MasterFilter("vivid", "鲜艳", Color(0xFFE84A6B)),
        MasterFilter("bokeh", "柔焦", Color(0xFFB8A4D4))
    )

    var selectedFilter by remember { mutableStateOf(filters[0].id) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1A0A2E).copy(alpha = 0.95f))
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(KUROMI_PINK, Color(0xFF9D4EDD))
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 标题
                Text(
                    text = "YanBao AI Master mode",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 滤镜网格 + 翻页箭头
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左箭头
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_left_kuromi),
                        contentDescription = "上一页",
                        tint = KUROMI_PINK,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { /* 翻页逻辑 */ }
                    )

                    // 2行×4列滤镜
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (row in 0..1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 0..3) {
                                    val filter = filters[row * 4 + col]
                                    val isSelected = selectedFilter == filter.id
                                    MasterFilterItem(
                                        filter = filter,
                                        isSelected = isSelected,
                                        onClick = { selectedFilter = filter.id }
                                    )
                                }
                            }
                        }
                    }

                    // 右箭头
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_right_kuromi),
                        contentDescription = "下一页",
                        tint = KUROMI_PINK,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { /* 翻页逻辑 */ }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, KUROMI_PINK, RoundedCornerShape(24.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "取消", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // 应用
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        KUROMI_PINK.copy(alpha = 0.3f),
                                        Color(0xFF9D4EDD).copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .border(1.5.dp, KUROMI_PINK, RoundedCornerShape(24.dp))
                            .clickable { onApply(selectedFilter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "应用", color = KUROMI_PINK, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * 单个滤镜圆形缩略图 + 名称
 */
@Composable
fun MasterFilterItem(
    filter: MasterFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(filter.previewColor)
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .clickable { onClick() }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = filter.name,
            color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
