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
 * 滤镜列表弹窗 — 严格对应 16_camera_10_filters.png
 *
 * 布局：
 *  顶部：✕  "YanBao AI Master"（胶囊）  相机图标
 *  内容：2行×5列 方形滤镜缩略图 + 名称
 *    第1行：Vibrant（选中）/ Vintage / Cyberpunk / Warm / Cool
 *    第2行：B&W / Sepia / Pastel / Cinematic / Nature
 *  左右翻页箭头（< >）
 *  底部：[取消] [应用] 按钮
 *
 * 背景：毛玻璃卡片 + 粉色霓虹边框
 */
data class CameraFilter(
    val id: String,
    val name: String,
    val previewColor: Color
)

@Composable
fun FiltersScreen(
    onDismiss: () -> Unit = {},
    onApply: (String) -> Unit = {}
) {
    val filters = listOf(
        CameraFilter("vibrant", "Vibrant", Color(0xFFE8A44A)),
        CameraFilter("vintage", "Vintage", Color(0xFF8B7355)),
        CameraFilter("cyberpunk", "Cyberpunk", Color(0xFF1A0A3E)),
        CameraFilter("warm", "Warm", Color(0xFFD4956A)),
        CameraFilter("cool", "Cool", Color(0xFF6A9DD4)),
        CameraFilter("bw", "B&W", Color(0xFF505050)),
        CameraFilter("sepia", "Sepia", Color(0xFF8B6914)),
        CameraFilter("pastel", "Pastel", Color(0xFFD4A4C8)),
        CameraFilter("cinematic", "Cinematic", Color(0xFF1A2A3A)),
        CameraFilter("nature", "Nature", Color(0xFF4A8B4A))
    )

    var selectedFilter by remember { mutableStateOf("vibrant") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0D0D1A).copy(alpha = 0.97f))
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(KUROMI_PINK, Color(0xFF9D4EDD))
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                // 顶部导航
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_kuromi),
                        contentDescription = "关闭",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onDismiss() }
                    )

                    // 中央胶囊标题
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_camera_kuromi),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "YanBao AI Master",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        painter = painterResource(R.drawable.ic_camera_kuromi),
                        contentDescription = "相机",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 滤镜网格 + 翻页箭头
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_left_kuromi),
                        contentDescription = "上一页",
                        tint = KUROMI_PINK,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { }
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (row in 0..1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (col in 0..4) {
                                    val filter = filters[row * 5 + col]
                                    val isSelected = selectedFilter == filter.id
                                    FilterThumbnail(
                                        filter = filter,
                                        isSelected = isSelected,
                                        onClick = { selectedFilter = filter.id },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_right_kuromi),
                        contentDescription = "下一页",
                        tint = KUROMI_PINK,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.5.dp, KUROMI_PINK, RoundedCornerShape(24.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "取消", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

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
 * 单个滤镜方形缩略图
 */
@Composable
fun FilterThumbnail(
    filter: CameraFilter,
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
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(filter.previewColor)
                .border(
                    width = if (isSelected) 2.dp else 0.5.dp,
                    color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onClick() }
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = filter.name,
            color = if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
