package com.yanbao.camera.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.data.model.RecommendPost
import com.yanbao.camera.ui.theme.YanbaoGradient
import com.yanbao.camera.viewmodel.HomeViewModel

/**
 * 首页
 * 严格按照 02_home/home_main.png 设计规格实现：
 * - 粉紫渐变背景
 * - 毛玻璃搜索栏（圆角28dp，白色25%透明度）
 * - 推荐卡片流（毛玻璃卡片，圆角16dp）
 * - 底部导航栏（圆角28dp，宽度90%）
 */
@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToRecommend: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = YanbaoGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 搜索栏
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // 推荐卡片流
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    RecommendationCard(
                        post = post,
                        onLike = { viewModel.toggleLike(post.id) }
                    )
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFEC4899),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 搜索栏组件
 * 毛玻璃效果：白色25%透明度，圆角28dp，边框1dp白色40%透明度
 */
@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    var searchText by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (searchText.isEmpty()) "搜索用户、标签或照片..." else searchText,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "语音搜索",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 推荐卡片组件
 * 毛玻璃卡片：白色20%透明度，圆角16dp，边框1dp白色30%透明度
 */
@Composable
fun RecommendationCard(
    post: RecommendPost,
    onLike: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 用户信息头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 用户头像
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFA78BFA), Color(0xFFEC4899))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.userName.take(1),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${post.userName}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        if (post.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "✓",
                                fontSize = 12.sp,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                    Text(
                        text = post.location,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = post.timeAgo,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 作品图片区域（颜色占位符，实际使用 Coil 加载网络图片）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(post.placeholderColorStart),
                                Color(post.placeholderColorEnd)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📷",
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 描述文字
            Text(
                text = post.description,
                fontSize = 14.sp,
                color = Color.White,
                lineHeight = 21.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 交互按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 点赞
                InteractionButton(
                    icon = if (post.isLiked) "❤️" else "🤍",
                    count = post.likeCount,
                    onClick = onLike
                )
                // 评论
                InteractionButton(
                    icon = "💬",
                    count = post.commentCount,
                    onClick = {}
                )
                // 分享
                InteractionButton(
                    icon = "📤",
                    count = post.shareCount,
                    onClick = {}
                )
            }
        }
    }
}

/**
 * 交互按钮（点赞/评论/分享）
 */
@Composable
fun InteractionButton(
    icon: String,
    count: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Text(
            text = count,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
