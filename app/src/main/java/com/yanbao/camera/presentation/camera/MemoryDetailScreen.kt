package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.KUROMI_PINK

/**
 * 雁宝记忆详情页面 — 严格对应 09_camera_05_memory_detail.png 和 11_camera_final_05_memory_detail.png
 *
 * 布局：
 *  顶部：← 雁宝记忆  🏠
 *  上部 60%：照片卡片（毛玻璃边框）
 *  下部 30%：信息卡片（名称/日期/地点/心情/美德/外观）
 *  底部 10%：[取消] [应用] 按钮
 *
 * 背景：深紫色渐变 + 库洛米图案
 */
data class MemoryDetail(
    val name: String = "Kuromi's Adventure",
    val date: String = "2023年10月10日",
    val location: String = "东京",
    val mood: String = "开心",
    val virtue: String = "友爱",
    val appearance: String = "可爱和服",
    val photoUrl: String? = null
)

@Composable
fun MemoryDetailScreen(
    memory: MemoryDetail = MemoryDetail(),
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onApply: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2D1040),
                        Color(0xFF1A0828),
                        Color(0xFF2D1040)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // 顶部导航栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, KUROMI_PINK.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { onBackClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_kuromi),
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 标题
                Text(
                    text = "雁宝记忆",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // 主页按钮
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, KUROMI_PINK.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { onHomeClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_home_kuromi),
                        contentDescription = "主页",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 照片卡片 (60%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                KUROMI_PINK.copy(alpha = 0.7f),
                                Color(0xFF9D4EDD).copy(alpha = 0.5f)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    if (memory.photoUrl != null) {
                        AsyncImage(
                            model = memory.photoUrl,
                            contentDescription = memory.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_album_kuromi),
                            contentDescription = null,
                            tint = KUROMI_PINK.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 信息卡片 (30%)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                KUROMI_PINK.copy(alpha = 0.6f),
                                Color(0xFF9D4EDD).copy(alpha = 0.4f)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MemoryInfoRow(iconRes = R.drawable.ic_album_kuromi, label = "名称", value = memory.name)
                    MemoryInfoRow(iconRes = R.drawable.ic_timer_kuromi, label = "日期", value = memory.date)
                    MemoryInfoRow(iconRes = R.drawable.ic_location_kuromi, label = "地点", value = memory.location)
                    MemoryInfoRow(iconRes = R.drawable.ic_kuromi_small, label = "心情", value = memory.mood)
                    MemoryInfoRow(iconRes = R.drawable.ic_kuromi_mark, label = "美德", value = memory.virtue)
                    MemoryInfoRow(iconRes = R.drawable.ic_kuromi_small, label = "外观", value = memory.appearance)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 底部按钮 (10%)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 取消按钮
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, KUROMI_PINK.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_kuromi),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "取消", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // 应用按钮
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    KUROMI_PINK.copy(alpha = 0.3f),
                                    Color(0xFF9D4EDD).copy(alpha = 0.3f)
                                )
                            )
                        )
                        .border(1.5.dp, KUROMI_PINK, RoundedCornerShape(26.dp))
                        .clickable { onApply() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check_kuromi),
                            contentDescription = null,
                            tint = KUROMI_PINK,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "应用", color = KUROMI_PINK, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 记忆信息行 — 图标 + 标签 + 值
 */
@Composable
fun MemoryInfoRow(
    iconRes: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label：",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(48.dp)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = KUROMI_PINK.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}
