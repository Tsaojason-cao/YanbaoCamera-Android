package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * 雁宝记忆选择页面 — 严格对应 08_camera_04_memory_select.png 和 10_camera_final_04_memory_select.png
 *
 * 布局：
 *  顶部：库洛米图标 + "雁宝记忆" 标题（粉色霓虹）+ 返回/主页
 *  内容：记忆列表（缩略图 + 名称/日期/地点 + 选择按钮）
 *  底部：[+ 创建新记忆] 粉色大按钮
 *
 * 背景：库洛米图案深紫色
 */
data class MemoryItem(
    val id: String,
    val name: String,
    val date: String,
    val location: String,
    val thumbnailUrl: String? = null
)

@Composable
fun MemorySelectScreen(
    memories: List<MemoryItem> = sampleMemories,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMemorySelected: (MemoryItem) -> Unit = {},
    onCreateMemory: () -> Unit = {}
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
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部导航栏
            MemoryTopBar(
                onBackClick = onBackClick,
                onHomeClick = onHomeClick
            )

            // 记忆列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(memories) { memory ->
                    MemoryListItem(
                        memory = memory,
                        onSelect = { onMemorySelected(memory) }
                    )
                }
            }

            // 底部创建按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                KUROMI_PINK.copy(alpha = 0.9f),
                                Color(0xFF9D4EDD).copy(alpha = 0.7f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = KUROMI_PINK,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .clickable { onCreateMemory() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 左侧库洛米图标
                    Icon(
                        painter = painterResource(R.drawable.ic_kuromi_mascot),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ 创建新记忆",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // 右侧库洛米图标
                    Icon(
                        painter = painterResource(R.drawable.ic_kuromi_mascot),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/**
 * 雁宝记忆顶部导航栏 — 带库洛米图标和粉色霓虹标题
 */
@Composable
fun MemoryTopBar(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部库洛米图标
        Icon(
            painter = painterResource(R.drawable.ic_kuromi_mascot),
            contentDescription = null,
            tint = KUROMI_PINK,
            modifier = Modifier
                .size(48.dp)
                .padding(top = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
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
                Text(text = "返回", color = Color.White, fontSize = 10.sp)
            }

            // 标题
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(
                            colors = listOf(KUROMI_PINK, Color(0xFF9D4EDD))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "雁宝记忆",
                    color = KUROMI_PINK,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 主页按钮
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
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
                Text(text = "主页", color = Color.White, fontSize = 10.sp)
            }
        }
    }
}

/**
 * 单条记忆列表项 — 缩略图 + 信息 + 选择按钮
 */
@Composable
fun MemoryListItem(
    memory: MemoryItem,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(
                1.dp,
                KUROMI_PINK.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 缩略图
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, KUROMI_PINK.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            if (memory.thumbnailUrl != null) {
                AsyncImage(
                    model = memory.thumbnailUrl,
                    contentDescription = memory.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_album_kuromi),
                    contentDescription = null,
                    tint = KUROMI_PINK,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 记忆信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "记忆名称：${memory.name}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "日期：${memory.date}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "地点：${memory.location}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 选择按钮
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Transparent)
                .border(
                    1.5.dp,
                    KUROMI_PINK,
                    RoundedCornerShape(20.dp)
                )
                .clickable { onSelect() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "选择",
                color = KUROMI_PINK,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// 示例数据
val sampleMemories = listOf(
    MemoryItem("1", "东京旅行", "2023年10月27日", "日本东京"),
    MemoryItem("2", "上海迪士尼", "2023年5月15日", "中国上海"),
    MemoryItem("3", "首尔咖啡馆", "2023年1月5日", "韩国首尔"),
    MemoryItem("4", "首尔咖啡馆", "2023年5月17日", "韩国首尔")
)
