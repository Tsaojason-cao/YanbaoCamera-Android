package com.yanbao.camera.presentation.recommend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
 * 推荐模块 - Cyber-Cute 社区
 * 
 * 设计规范：
 * - 背景：粉紫渐变
 * - 顶部：yanbao AI 品牌标识
 * - 内容：毛玻璃卡片瀑布流，带点赞/评论交互
 */
@Composable
fun RecommendScreen() {
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
            
            // 推荐内容瀑布流
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items((1..20).toList()) { index ->
                    RecommendCard(
                        index = index,
                        likes = 100 + index * 50,
                        comments = 10 + index * 5
                    )
                }
            }
        }
    }
}

/**
 * 推荐卡片
 * 
 * 设计规范：
 * - 圆角 16dp
 * - 毛玻璃背景
 * - 底部显示点赞/评论数
 */
@Composable
fun RecommendCard(
    index: Int,
    likes: Int,
    comments: Int
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height((180 + index * 15).dp) // 非对称高度
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
            // 底部交互栏
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 点赞
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "点赞",
                        tint = Color(0xFFEC4899), // 粉色
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$likes",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
                
                // 评论
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "评论",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$comments",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
