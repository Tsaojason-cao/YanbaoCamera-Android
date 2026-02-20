package com.yanbao.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * AR 特效模式叠加层
 * 显示 AR 贴纸、效果、动画、特效选择
 */
@Composable
fun ARModeOverlay(
    modifier: Modifier = Modifier,
    onStickerSelected: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedSticker by remember { mutableStateOf("") }
    
    val tabs = listOf("贴纸", "效果", "动画", "特效")
    val stickers = listOf(
        "酷洛米" to "🐰",
        "爱心" to "❤️",
        "蝴蝶结" to "🎀",
        "皇冠" to "👑",
        "闪烁" to "✨",
        "骷髅" to "💀"
    )
    
    Box(
        modifier = modifier
    ) {
        // 底部 AR 控制面板
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            // Tab 选择器
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFFEC4899) else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }
            
            // 贴纸选择器
            when (selectedTab) {
                0 -> {
                    // 贴纸
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        items(stickers) { (name, emoji) ->
                            StickerItem(
                                name = name,
                                emoji = emoji,
                                isSelected = name == selectedSticker,
                                onClick = {
                                    selectedSticker = name
                                    onStickerSelected(name)
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // 效果
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "效果功能开发中...",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                2 -> {
                    // 动画
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "动画功能开发中...",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                3 -> {
                    // 特效
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "特效功能开发中...",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StickerItem(
    name: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // 贴纸预览圆形
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFA78BFA),
                                Color(0xFFEC4899)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 贴纸名称
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFEC4899) else Color.White
        )
    }
}
