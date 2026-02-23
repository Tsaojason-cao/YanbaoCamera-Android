package com.yanbao.camera.presentation.recommend

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yanbao.camera.R

/**
 * 推荐模块 - AI 推荐列表
 * 
 * 对标截图：41_recommend_list_masonry.png
 * 
 * 核心功能：
 * - 顶部 Tab：附近/最新/热门/评分
 * - 瀑布流卡片布局
 * - 每张卡片显示：照片/评分/标签/标题/地点/描述
 * - 库洛米装饰
 * - 收藏按钮
 */
@Composable
fun RecommendScreen(
    viewModel: RecommendViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val spots by viewModel.filteredSpots.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF9C7FD8), // 深紫
                        Color(0xFFD4A5E8), // 紫粉
                        Color(0xFFF5C8E4)  // 亮粉
                    )
                )
            )
    ) {
        // --- 顶部标题（含返回按钮） ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 0.dp)
        ) {
            // 返回上一层按钮
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "返回",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AI 推荐",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "发现你附近的完美拍摄地点",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // 右上角头像
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
            ) {
                Image(
                        painter = painterResource(id = R.drawable.kuromi),
                    contentDescription = "个人中心",
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(8.dp)
                )
            }
        }

        // --- 核心 Tab 切换 (对应截图：附近、最新、热门、评分) ---
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            edgePadding = 16.dp,
            indicator = {},
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            RecommendTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.onTabSelected(tab) },
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isSelected) Color(0xFFFFB6C1)
                            else Color.White.copy(alpha = 0.3f)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = tab.displayName,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 瀑布流卡片展示区 ---
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            items(spots) { spot ->
                PhotoSpotCard(spot = spot)
            }
        }
    }
}

/**
 * 照片机位卡片
 * 
 * 对标截图 41_recommend_list_masonry.png 中的卡片样式
 */
@Composable
fun PhotoSpotCard(spot: PhotoSpot) {
    var isBookmarked by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, Color(0xFFFFB6C1).copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 跳转详情 */ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.95f))
        ) {
            Column {
                // 照片区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    AsyncImage(
                        model = spot.imageUrl,
                        contentDescription = spot.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // 左上角库洛米徽章
                    Image(
                        painter = painterResource(id = spot.badgeIcon),
                        contentDescription = "徽章",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(40.dp)
                    )
                    
                    // 右上角收藏按钮
                    IconButton(
                        onClick = { isBookmarked = !isBookmarked },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "收藏",
                            tint = Color(0xFFAA66FF)
                        )
                    }
                }

                // 信息区域
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // 评分和标签
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 评分
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⭐",
                                fontSize = 14.sp
                            )
                            Text(
                                text = spot.rating.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }
                        
                        // 标签
                        Text(
                            text = spot.category,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier
                                .background(
                                    color = spot.categoryColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 标题
                    Text(
                        text = spot.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 地点
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📍",
                            fontSize = 12.sp
                        )
                        Text(
                            text = spot.location,
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 描述
                    Text(
                        text = spot.description,
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        maxLines = 2
                    )
                }
            }
            
            // 右下角库洛米装饰
            Image(
                painter = painterResource(id = R.drawable.kuromi),
                contentDescription = "库洛米装饰",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(32.dp)
            )
        }
    }
}

/**
 * 推荐 Tab 枚举
 */
enum class RecommendTab(val displayName: String) {
    NEARBY("附近"),
    LATEST("最新"),
    HOT("热门"),
    RATING("评分")
}

/**
 * 照片机位数据类
 */
data class PhotoSpot(
    val id: String,
    val title: String,
    val location: String,
    val description: String,
    val imageUrl: String,
    val rating: Float,
    val category: String,
    val categoryColor: Color,
    val badgeIcon: Int,
    val distance: Float? = null,
    val photoCount: Int = 0
)
