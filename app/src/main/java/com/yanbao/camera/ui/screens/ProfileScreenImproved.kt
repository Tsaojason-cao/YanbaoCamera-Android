package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.yanbao.camera.ui.components.KuromiCorners

/**
 * 改进的ProfileScreen - 完全匹配设计图
 * 包含用户信息、统计数据、作品展示、菜单等功能
 */
@Composable
fun ProfileScreenImproved(
    onNavigateBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // 模拟用户数据
    val userProfile = UserProfile(
        name = "雁宝摄影师",
        bio = "专业摄影师 | AI美化爱好者",
        photoCount = 234,
        followersCount = 5200,
        followingCount = 320,
        avatarUrl = ""
    )
    
    // 模拟作品数据
    val userPhotos = (1..9).map { "photo_$it" }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA78BFA),
                        Color(0xFFEC4899),
                        Color(0xFFF9A8D4)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏
            item {
                TopProfileBar(
                    onBack = onNavigateBack,
                    onSettings = onSettings
                )
            }
            
            // 用户头像和信息
            item {
                UserProfileHeader(
                    profile = userProfile,
                    onEditProfile = onEditProfile
                )
            }
            
            // 统计数据
            item {
                UserStatsRow(
                    photoCount = userProfile.photoCount,
                    followersCount = userProfile.followersCount,
                    followingCount = userProfile.followingCount
                )
            }
            
            // 选项卡
            item {
                ProfileTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
            
            // 作品网格
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 80.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userPhotos.size) { index ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEC4899).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Photo",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // 库洛米装饰
        KuromiCorners()
    }
}

/**
 * 用户资料数据类
 */
data class UserProfile(
    val name: String,
    val bio: String,
    val photoCount: Int,
    val followersCount: Int,
    val followingCount: Int,
    val avatarUrl: String
)

/**
 * 顶部工具栏
 */
@Composable
private fun TopProfileBar(
    onBack: () -> Unit,
    onSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                "个人资料",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 用户资料头部
 */
@Composable
private fun UserProfileHeader(
    profile: UserProfile,
    onEditProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 用户头像
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFEC4899).copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp)
            )
        }
        
        // 用户名
        Text(
            profile.name,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )
        
        // 个人简介
        Text(
            profile.bio,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        // 编辑按钮
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEC4899)
            )
        ) {
            Text(
                "编辑资料",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 用户统计数据行
 */
@Composable
private fun UserStatsRow(
    photoCount: Int,
    followersCount: Int,
    followingCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            label = "照片",
            value = photoCount.toString()
        )
        
        StatItem(
            label = "粉丝",
            value = followersCount.toString()
        )
        
        StatItem(
            label = "关注",
            value = followingCount.toString()
        )
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * 资料选项卡
 */
@Composable
private fun ProfileTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val tabs = listOf("作品", "收藏", "喜欢")
        tabs.forEachIndexed { index, tab ->
            Button(
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == index) Color(0xFFEC4899) else Color.White.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    tab,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
