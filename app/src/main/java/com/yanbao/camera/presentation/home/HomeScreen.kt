package com.yanbao.camera.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.presentation.theme.YanbaoPink
import kotlinx.coroutines.delay

/**
 * 首页 - 无手机框版本
 * 
 * Phase 1 优化：
 * - 去除手机框装饰
 * - 底部导航改为6个标签：首页、拍照、编辑、相册、推荐、我的
 * - AI推荐位高度：160dp → 120dp
 * - 新增Hot Spots Nearby横向滚动
 */
@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onProfileClick: () -> Unit,
    avatarUri: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. 粉紫渐变背景 + 星光粒子
        PurpleFlowingBackground()
        
        // 2. 内容层
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 顶部：品牌名 + 头像入口
            TopBar(
                onProfileClick = onProfileClick,
                avatarUri = avatarUri
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 四宫格功能卡片
            MainFeatureGrid(
                onCameraClick = onCameraClick,
                onEditorClick = onEditorClick,
                onGalleryClick = onGalleryClick,
                onSettingsClick = { /* TODO: 设置页面 */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Hot Spots Nearby（新增）
            HotSpotsNearby()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // AI 推荐位（压缩高度至120dp）
            AIRecommendationBanner(
                onRecommendClick = onRecommendClick,
                modifier = Modifier.height(120.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // 3. 底部导航栏已在YanbaoApp中统一管理，此处不需要重复添加
    }
}

/**
 * 顶部栏：品牌名 + 48dp 头像入口
 */
@Composable
fun TopBar(
    onProfileClick: () -> Unit,
    avatarUri: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 品牌名 "yanbao AI"
        Text(
            text = "yanbao AI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = YanbaoPink,
                    offset = Offset(0f, 4f),
                    blurRadius = 12f
                )
            )
        )
        
        // 48dp 头像入口
        Box(
            modifier = Modifier
                .size(48.dp)
                .drawBehind {
                    drawCircle(
                        color = YanbaoPink,
                        style = Stroke(width = 4f),
                        alpha = 0.8f
                    )
                }
                .clip(CircleShape)
                .clickable { onProfileClick() }
                .background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri != null) {
                coil.compose.AsyncImage(
                    model = avatarUri,
                    contentDescription = "User Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Text(
                    text = "👤",
                    fontSize = 24.sp
                )
            }
        }
    }
}

/**
 * 四宫格功能卡片
 */
@Composable
fun MainFeatureGrid(
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Camera
        item {
            FeatureCard(
                title = "Camera",
                subtitle = "Capture moments",
                icon = "📷",
                backgroundColor = Color(0xFFD4B0FF),
                onClick = onCameraClick
            )
        }
        
        // Editor
        item {
            FeatureCard(
                title = "Editor",
                subtitle = "Create magic",
                icon = "✨",
                backgroundColor = Color(0xFFC0A0FF),
                onClick = onEditorClick
            )
        }
        
        // Gallery
        item {
            FeatureCard(
                title = "Gallery",
                subtitle = "View memories",
                icon = "🖼️",
                backgroundColor = Color(0xFFB090FF),
                onClick = onGalleryClick
            )
        }
        
        // Settings
        item {
            FeatureCard(
                title = "Settings",
                subtitle = "Customize app",
                icon = "⚙️",
                backgroundColor = Color(0xFFA080FF),
                onClick = onSettingsClick
            )
        }
    }
}

/**
 * 功能卡片
 */
@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 图标
                Text(
                    text = icon,
                    fontSize = 48.sp
                )
                
                // 标题和副标题
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Hot Spots Nearby（新增）
 */
@Composable
fun HotSpotsNearby(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Hot Spots Nearby",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hotSpots) { spot ->
                HotSpotCard(spot = spot)
            }
        }
    }
}

/**
 * Hot Spot卡片
 */
@Composable
fun HotSpotCard(spot: HotSpot) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 照片预览
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = spot.emoji,
                    fontSize = 32.sp
                )
            }
            
            // 标题
            Text(
                text = spot.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1
            )
            
            // 星级评分
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(spot.rating) {
                    Text(
                        text = "⭐",
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * AI 推荐位（压缩至120dp）
 */
@Composable
fun AIRecommendationBanner(
    onRecommendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRecommendClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0B0FF).copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AI 推荐",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Personalized for you",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 底部导航栏（6个标签）
 */
@Composable
fun BottomNavigationBar(
    currentTab: String,
    onHomeClick: () -> Unit,
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                color = Color(0xFF1A1A1A).copy(alpha = 0.9f)
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = "🏠",
            label = "Home",
            isSelected = currentTab == "Home",
            onClick = onHomeClick
        )
        BottomNavItem(
            icon = "📷",
            label = "Camera",
            isSelected = currentTab == "Camera",
            onClick = onCameraClick
        )
        BottomNavItem(
            icon = "✏️",
            label = "Editor",
            isSelected = currentTab == "Editor",
            onClick = onEditorClick
        )
        BottomNavItem(
            icon = "🖼️",
            label = "Gallery",
            isSelected = currentTab == "Gallery",
            onClick = onGalleryClick
        )
        BottomNavItem(
            icon = "🌟",
            label = "Recommend",
            isSelected = currentTab == "Recommend",
            onClick = onRecommendClick
        )
        BottomNavItem(
            icon = "👤",
            label = "Profile",
            isSelected = currentTab == "Profile",
            onClick = onProfileClick
        )
    }
}

/**
 * 底部导航栏单项
 */
@Composable
fun BottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isSelected) YanbaoPink else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 粉紫渐变背景 + 星光粒子
 */
@Composable
fun PurpleFlowingBackground(modifier: Modifier = Modifier) {
    var offset by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // 60 FPS
            offset = (offset + 0.5f) % 1000f
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // 绘制流光渐变背景
        val gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFFA78BFA), // 浅紫色
                Color(0xFFEC4899), // 粉色
                Color(0xFFA78BFA)  // 浅紫色
            ),
            start = Offset(offset, offset),
            end = Offset(size.width + offset, size.height + offset)
        )
        
        drawRect(brush = gradient)
        
        // 绘制星星装饰
        val stars = listOf(
            Offset(size.width * 0.1f, size.height * 0.1f),
            Offset(size.width * 0.3f, size.height * 0.2f),
            Offset(size.width * 0.7f, size.height * 0.15f),
            Offset(size.width * 0.9f, size.height * 0.3f),
            Offset(size.width * 0.2f, size.height * 0.8f),
            Offset(size.width * 0.8f, size.height * 0.85f)
        )
        
        stars.forEach { starPos ->
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = 4f,
                center = starPos
            )
        }
    }
}

/**
 * Hot Spot数据类
 */
data class HotSpot(
    val title: String,
    val emoji: String,
    val rating: Int
)

/**
 * 示例数据
 */
val hotSpots = listOf(
    HotSpot("Scenic Photo", "🌅", 4),
    HotSpot("Youch Mouss", "🏝️", 5),
    HotSpot("Photography", "📸", 4),
    HotSpot("Detine", "🏔️", 5)
)
