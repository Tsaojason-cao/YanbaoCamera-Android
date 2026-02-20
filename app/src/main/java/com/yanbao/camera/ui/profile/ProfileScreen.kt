package com.yanbao.camera.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
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
import com.yanbao.camera.viewmodel.ProfileViewModel

/**
 * 个人资料界面
 * 严格按照 07_profile/01_profile_main.png 设计规格实现：
 * - 城市背景横幅（粉紫渐变）+ 相机Logo
 * - 圆形头像（粉紫光晕边框）+ 在线状态绿点
 * - 用户名 + 皇冠图标 + ID
 * - 会员卡（玻璃磨砂效果）：会员编号 + 剩余天数 + 位置
 * - Works/Private/Favorites/Liked 标签栏
 * - 3列作品瀑布流（带点赞数）
 * 颜色规格：#A78BFA(紫) #EC4899(粉) #F9A8D4(浅粉) #0F0F10(黑)
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val works by viewModel.works.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F10))
    ) {
        // 顶部横幅区域
        ProfileHeader(
            userName = profile.userName,
            userId = profile.userId,
            memberNumber = profile.memberNumber,
            remainingDays = profile.remainingDays,
            location = profile.location
        )

        // 标签栏
        ProfileTabBar(
            selectedTab = selectedTab,
            onTabSelected = { viewModel.selectTab(it) }
        )

        // 作品网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(works) { work ->
                WorkGridItem(
                    colorStart = work.colorStart,
                    colorEnd = work.colorEnd,
                    likeCount = work.likeCount
                )
            }
        }
    }
}

/**
 * 个人资料头部
 * 城市背景横幅 + 头像 + 用户名 + 会员卡
 */
@Composable
fun ProfileHeader(
    userName: String,
    userId: String,
    memberNumber: String,
    remainingDays: Int,
    location: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        // 城市背景横幅（粉紫渐变）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                )
        ) {
            // 背景装饰：城市轮廓效果（用渐变模拟）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFA78BFA).copy(alpha = 0.3f),
                                Color(0xFFEC4899).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 相机Logo（右上角分享按钮）
            IconButton(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "分享",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 头像（叠加在横幅底部）
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 130.dp)
        ) {
            // 头像外圈光晕
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFA78BFA),
                                Color(0xFFEC4899)
                            )
                        ),
                        shape = CircleShape
                    )
            )
            // 头像内圈
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF2D1B69), Color(0xFF1A0A3C))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 40.sp)
            }
            // 在线状态绿点
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFF22C55E), CircleShape)
                    .padding(2.dp)
            )
        }

        // 用户名 + ID（头像下方）
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("👑", fontSize = 18.sp)
            }
            Text(
                text = "ID: $userId",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }

    // 会员卡（玻璃磨砂效果）
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFFA78BFA).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 皇冠图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFA78BFA), Color(0xFFEC4899))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("👑", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row {
                    Text(
                        text = "会员编号: $memberNumber",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "剩余天数: $remainingDays",
                        fontSize = 13.sp,
                        color = Color(0xFFEC4899),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "探索中: $location",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 标签栏：Works / Private / Favorites / Liked
 */
@Composable
fun ProfileTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Works", "Private", "Favorites", "Liked")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, tab ->
            Column(
                modifier = Modifier
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tab,
                    fontSize = 15.sp,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == index) Color(0xFFEC4899) else Color.White.copy(alpha = 0.6f)
                )
                if (selectedTab == index) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(Color(0xFFEC4899), RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }

    Divider(color = Color.White.copy(alpha = 0.1f))
}

/**
 * 作品网格项（带点赞数）
 */
@Composable
fun WorkGridItem(
    colorStart: Long,
    colorEnd: Long,
    likeCount: String
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(2.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(colorStart), Color(colorEnd))
                )
            )
    ) {
        // 底部点赞数
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("❤️", fontSize = 10.sp)
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = likeCount,
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
