package com.yanbao.camera.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 相册模块 - EXIF 元数据显示
 * 
 * 设计规范：
 * - 背景：粉紫渐变
 * - 顶部：yanbao AI 品牌标识
 * - 内容：网格布局，点击显示 EXIF 元数据
 */
@Composable
fun GalleryScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8B7FD8), // 深紫
                        Color(0xFFB89FE8), // 紫粉
                        Color(0xFFF5A8D4)  // 亮粉
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部品牌标识
            Text(
                text = "yanbao AI",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 16.dp)
            )
            
            // 相册网格（示例数据）
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items((1..30).toList()) { index ->
                    PhotoCard(index = index)
                }
            }
        }
    }
}

/**
 * 照片卡片
 * 
 * 设计规范：
 * - 正方形
 * - 毛玻璃背景
 * - 显示 EXIF 元数据（ISO、快门速度、GPS）
 */
@Composable
fun PhotoCard(index: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x40FFFFFF), // 25% 白色透明
                            Color(0x26FFFFFF)  // 15% 白色透明
                        )
                    )
                )
        ) {
            // EXIF 元数据显示
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
            ) {
                Text(
                    text = "ISO ${400 + index * 100}",
                    fontSize = 8.sp,
                    color = Color.White
                )
                Text(
                    text = "1/${60 + index * 10}s",
                    fontSize = 8.sp,
                    color = Color.White
                )
            }
        }
    }
}
