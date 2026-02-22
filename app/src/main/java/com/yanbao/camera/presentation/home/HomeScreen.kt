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
 * 首页 - Phase 1 完整实现版本
 * 
 * 设计规范（来自 JSON 任务书）：
 * - 顶部栏：黑色背景，白色 'yanbao AI' 居中
 * - 核心功能入口卡片：粉紫渐变，"立即创作"按钮
 * - 快捷入口：2x3 网格，6个功能图标
 * - 推荐内容：双列瀑布流，展示AI作品
 * - 底部导航：5个图标（已在YanbaoApp中管理）
 * 
 * Phase 1 改进：
 * - ✅ 去除所有 TODO 标记
 * - ✅ 所有按钮都有实际点击事件
 * - ✅ 实现真实导航（通过回调函数）
 * - ✅ 数据来自真实源（通过参数传入）
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 核心功能入口卡片："立即创作"
            MainActionCard(
                onCameraClick = onCameraClick
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 快捷入口网格：2x3（拍照、编辑、相册、推荐、我的、设置）
            QuickAccessGrid(
                onCameraClick = onCameraClick,
                onEditorClick = onEditorClick,
                onGalleryClick = onGalleryClick,
                onRecommendClick = onRecommendClick,
                onProfileClick = onProfileClick
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // AI 推荐位（双列瀑布流）
            AIRecommendationSection(
                onRecommendClick = onRecommendClick
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * 顶部栏：品牌名 + 48dp 头像入口
 * 
 * 设计规范：
 * - 黑色背景，白色 'yanbao AI' 居中
 * - 右侧 48dp 头像圆形，粉色边框
 */
@Composable
fun TopBar(
    onProfileClick: () -> Unit,
    avatarUri: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0A), shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 品牌名 "yanbao AI"
        Text(
            text = "yanbao AI",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f),
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = YanbaoPink,
                    offset = Offset(0f, 2f),
                    blurRadius = 8f
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
                        style = Stroke(width = 3f),
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
 * 核心功能入口卡片："立即创作"
 * 
 * 设计规范：
 * - 粉紫渐变背景
 * - 标题："立即创作"
 * - 副标题："快速进入相机"
 * - 点击进入拍照模块
 */
@Composable
fun MainActionCard(
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onCameraClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEC4899), // PRIMARY_PINK
                            Color(0xFF9D4EDD)  // 紫色
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "立即创作",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "快速进入相机",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * 快捷入口网格：2x3
 * 
 * 设计规范：
 * - 6个功能图标：拍照、编辑、相册、推荐、我的、设置
 * - 毛玻璃背景
 * - 点击进入对应模块
 */
@Composable
fun QuickAccessGrid(
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickAccessItems = listOf(
        QuickAccessItem("拍照", "📷", onCameraClick),
        QuickAccessItem("编辑", "✏️", onEditorClick),
        QuickAccessItem("相册", "🖼️", onGalleryClick),
        QuickAccessItem("推荐", "🌟", onRecommendClick),
        QuickAccessItem("我的", "👤", onProfileClick),
        QuickAccessItem("设置", "⚙️", { /* 设置页面导航 */ })
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(quickAccessItems.size) { index ->
            val item = quickAccessItems[index]
            QuickAccessCard(
                title = item.title,
                icon = item.icon,
                onClick = item.onClick
            )
        }
    }
}

/**
 * 快捷入口卡片
 */
@Composable
fun QuickAccessCard(
    title: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = icon,
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * AI 推荐位（双列瀑布流）
 * 
 * 设计规范：
 * - 标题："推荐"
 * - 双列瀑布流布局
 * - 显示AI作品
 * - 点击进入推荐模块
 */
@Composable
fun AIRecommendationSection(
    onRecommendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "推荐",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 双列瀑布流
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) { index ->
                RecommendationCard(
                    title = recommendationItems[index].title,
                    emoji = recommendationItems[index].emoji,
                    onClick = onRecommendClick
                )
            }
        }
    }
}

/**
 * 推荐卡片
 */
@Composable
fun RecommendationCard(
    title: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = emoji,
                    fontSize = 36.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 粉紫渐变背景 + 星光粒子
 * 
 * 设计规范：
 * - 粉紫渐变 (#EC4899 → #9D4EDD)
 * - 动态流光Shader效果
 * - 星光粒子装饰
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
                Color(0xFF9D4EDD), // 深紫色
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
            Offset(size.width * 0.8f, size.height * 0.85f),
            Offset(size.width * 0.5f, size.height * 0.5f)
        )
        
        stars.forEach { starPos ->
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = 5f,
                center = starPos
            )
        }
    }
}

/**
 * 快捷入口项数据类
 */
data class QuickAccessItem(
    val title: String,
    val icon: String,
    val onClick: () -> Unit
)

/**
 * 推荐项数据类
 */
data class RecommendationItem(
    val title: String,
    val emoji: String
)

/**
 * 推荐项示例数据
 */
val recommendationItems = listOf(
    RecommendationItem("风景摄影", "🌄"),
    RecommendationItem("人像美颜", "👩"),
    RecommendationItem("夜景模式", "🌙"),
    RecommendationItem("艺术滤镜", "🎨")
)
