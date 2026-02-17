package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.model.Post
import com.yanbao.camera.repository.MockDataRepository
import com.yanbao.camera.ui.components.KuromiCorners
import com.yanbao.camera.ui.theme.AccentPink
import com.yanbao.camera.ui.theme.GradientEnd
import com.yanbao.camera.ui.theme.GradientMiddle
import com.yanbao.camera.ui.theme.GradientStart
import com.yanbao.camera.ui.theme.ProgressPrimary
import com.yanbao.camera.ui.theme.TextWhite
import com.yanbao.camera.ui.components.glassEffect

/**
 * 推荐屏幕 - 完整实现版本
 * 
 * 功能：
 * - 搜索栏（毛玻璃效果）
 * - 推荐卡片列表
 * - 点赞功能
 * - 位置信息显示
 * - 库洛米装饰
 */
@Composable
fun RecommendScreenV2(
    onPostClicked: (Post) -> Unit = {},
    onMapClicked: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var likedPosts by remember { mutableStateOf(setOf<String>()) }
    
    val mockRepository = MockDataRepository()
    val allPosts = mockRepository.generateMockPosts(10)
    val filteredPosts = if (searchQuery.isEmpty()) {
        allPosts
    } else {
        allPosts.filter { 
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 搜索栏
            SearchBar(
                query = searchQuery,
                onQueryChanged = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            
            // 推荐卡片列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPosts) { post ->
                    RecommendPostCard(
                        post = post,
                        isLiked = likedPosts.contains(post.id),
                        onLikeToggled = {
                            likedPosts = if (likedPosts.contains(post.id)) {
                                likedPosts - post.id
                            } else {
                                likedPosts + post.id
                            }
                        },
                        onMapClicked = onMapClicked,
                        onClick = { onPostClicked(post) }
                    )
                }
            }
        }
        
        // 库洛米装饰
        KuromiCorners(
            modifier = Modifier.fillMaxSize(),
            size = 60,
            showCorners = true
        )
    }
}

/**
 * 搜索栏
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 12),
        placeholder = {
            Text(
                text = "搜索位置或摄影师...",
                fontSize = 12.sp,
                color = Color.Gray
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "搜索",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
            fontSize = 12.sp
        )
    )
}

/**
 * 推荐卡片
 */
@Composable
fun RecommendPostCard(
    post: Post,
    isLiked: Boolean = false,
    onLikeToggled: () -> Unit = {},
    onMapClicked: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 16)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            // 用户信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 用户头像（占位符）
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AccentPink),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.author.take(1),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = post.author,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    
                    Text(
                        text = post.timestamp,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                
                // 点赞按钮
                IconButton(
                    onClick = onLikeToggled,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "点赞",
                        tint = if (isLiked) AccentPink else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // 图片预览
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📸\n${post.title}",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            // 位置和互动信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onMapClicked)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "位置",
                        tint = ProgressPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Text(
                        text = post.location,
                        fontSize = 10.sp,
                        color = ProgressPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "❤️ ${post.likes}",
                        fontSize = 10.sp,
                        color = TextWhite
                    )
                    
                    Text(
                        text = "💬 ${post.comments}",
                        fontSize = 10.sp,
                        color = TextWhite
                    )
                }
            }
        }
    }
}
