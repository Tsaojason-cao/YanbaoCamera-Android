package com.yanbao.camera.ui.recommend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.ui.theme.YanbaoGradient
import com.yanbao.camera.viewmodel.RecommendViewModel

/**
 * 推荐/发现界面
 * 严格按照 06_recommend/01_recommend_main.png 设计规格实现：
 * - 粉紫渐变背景
 * - 顶部搜索栏 + 通知图标
 * - 瀑布流布局（2列，不等高）
 */
@Composable
fun RecommendScreen(
    viewModel: RecommendViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = YanbaoGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "发现",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = Color.White
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "通知",
                        tint = Color.White
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFEC4899))
                }
            } else {
                // 瀑布流
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp
                ) {
                    items(posts) { post ->
                        RecommendCard(
                            colorStart = post.placeholderColorStart.toLong(),
                            colorEnd = post.placeholderColorEnd.toLong(),
                            userName = post.userName,
                            likeCount = post.likeCount,
                            height = (150 + (post.id.hashCode() % 100).coerceAtLeast(0)).dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendCard(
    colorStart: Long,
    colorEnd: Long,
    userName: String,
    likeCount: String,
    height: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(colorStart), Color(colorEnd))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = "@$userName",
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("❤️", fontSize = 10.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Text(likeCount, fontSize = 11.sp, color = Color.White)
            }
        }
    }
}
