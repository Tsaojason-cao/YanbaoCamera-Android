package com.yanbao.camera.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
 * 首页 - 雁寶记忆瀑布流
 * 
 * 设计规范：
 * - 背景：粉紫渐变
 * - 顶部：yanbao AI 品牌标识
 * - 内容：非对称瀑布流卡片（显示拍摄照片 + LBS 标签）
 */
@Composable
fun HomeScreen() {
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
            
            // 雁寶记忆瀑布流（示例数据）
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items((1..10).toList()) { index ->
                    MemoryCard(
                        index = index,
                        location = "北京市朝阳区"
                    )
                }
            }
        }
    }
}

/**
 * 雁寶记忆卡片
 * 
 * 设计规范：
 * - 圆角 16dp
 * - 毛玻璃背景
 * - 右下角显示 LBS 位置标签
 */
@Composable
fun MemoryCard(
    index: Int,
    location: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height((150 + index * 20).dp) // 非对称高度
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
            // 右下角 LBS 标签
            Text(
                text = "📍 $location",
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
            
            // 右上角参数标签
            Text(
                text = "ISO 400",
                fontSize = 10.sp,
                color = Color(0xFFEC4899), // 粉色
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
    }
}
