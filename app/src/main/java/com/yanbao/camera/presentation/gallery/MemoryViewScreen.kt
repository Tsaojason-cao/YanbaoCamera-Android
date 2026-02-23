package com.yanbao.camera.presentation.gallery

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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yanbao.camera.R

private val KUROMI_PINK = Color(0xFFEC4899)
private val KUROMI_PURPLE = Color(0xFF9D4EDD)
private val OBSIDIAN_BLACK = Color(0xFF0A0A0A)

/**
 * 记忆视图 - 展示雁宝记忆详情
 * Layer 0: 模糊背景照片
 * Layer 1: 底部面板（参数摘要 + 一键套用 + 相关记忆卡片）
 */
@Composable
fun MemoryViewScreen(
    navController: NavController,
    memoryId: String,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val bottomSheetHeight = screenHeight * 0.35f

    // 模拟记忆数据
    val memories = listOf(
        MemoryItem("m1", "", "台北101", "ISO 800 · 大师·城市夜景", ExifData("800", "1/250s", "50mm", "6000K")),
        MemoryItem("m2", "", "西门町", "ISO 400 · 29D·街头潮流", ExifData("400", "1/125s", "35mm", "5500K")),
        MemoryItem("m3", "", "象山步道", "ISO 200 · 自然·风光", ExifData("200", "1/500s", "24mm", "5000K")),
        MemoryItem("m4", "", "九份老街", "ISO 1600 · 大师·怀旧", ExifData("1600", "1/60s", "50mm", "4500K")),
    )
    val memory = memories.find { it.id == memoryId } ?: memories.first()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Layer 0: 背景照片（模糊处理）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                    )
                )
        )

        // 顶部品牌标识
        Text(
            text = "yanbao AI",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        )

        // 返回按钮
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back_kuromi),
                contentDescription = "返回",
                tint = Color.White
            )
        }

        // Layer 1: 底部面板
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomSheetHeight)
                .align(Alignment.BottomCenter),
            color = OBSIDIAN_BLACK.copy(alpha = 0.92f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // 拖拽指示条
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 地点和参数摘要
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location_kuromi),
                        contentDescription = null,
                        tint = KUROMI_PINK,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = memory.location,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memory.params,
                    color = KUROMI_PINK,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // EXIF 参数行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MemoryParamChip("ISO ${memory.fullParams.iso}")
                    MemoryParamChip(memory.fullParams.shutter)
                    MemoryParamChip(memory.fullParams.focal)
                    MemoryParamChip(memory.fullParams.colorTemp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 一键套用按钮
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KUROMI_PINK
                    )
                ) {
                    Text(
                        text = "套用此参数拍摄",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 相关记忆卡片横向滑动
                Text(
                    text = "相关记忆",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(memories, key = { it.hashCode() }) { item ->
                        RelatedMemoryCard(item, navController)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryParamChip(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun RelatedMemoryCard(item: MemoryItem, navController: NavController) {
    Column(
        modifier = Modifier
            .width(90.dp)
            .clickable { navController.navigate("gallery/memory/${item.id}") }
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF2D1B4E), Color(0xFF4A1A3A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_album_kuromi),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.location,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1
        )
        Text(
            text = item.params.take(12) + "...",
            color = KUROMI_PINK,
            fontSize = 9.sp,
            maxLines = 1
        )
    }
}
