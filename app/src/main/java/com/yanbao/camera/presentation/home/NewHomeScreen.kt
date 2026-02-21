package com.yanbao.camera.presentation.home

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 首页 - 新设计图1:1还原
 * 
 * 设计要点：
 * 1. 粉紫渐变背景 + 星光粒子效果
 * 2. 4张毛玻璃卡片（2x2网格）：Camera、Editor、Gallery、Settings
 * 3. Hot Spots Nearby横向滚动卡片
 * 4. 中央大圆形相机按钮（粉紫渐变）
 * 5. 底部导航栏（5个标签）
 */
@Composable
fun NewHomeScreen(
    onNavigateToCamera: () -> Unit = {},
    onNavigateToEditor: () -> Unit = {},
    onNavigateToGallery: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onNavigateToCommunity: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFC0CB), // 粉色
                        Color(0xFFE0B0FF), // 淡紫色
                        Color(0xFFD8BFD8)  // 蓟色
                    )
                )
            )
    ) {
        // 星光粒子效果
        StarfieldBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部标题 + 头像
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp))
                
                Text(
                    text = "yanbao AI",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // 头像
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF87CEEB))
                        .clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👤",
                        fontSize = 24.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 4张毛玻璃卡片（2x2网格）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 第一行：Camera + Editor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GlassmorphismCard(
                        title = "Camera",
                        icon = "📷",
                        onClick = onNavigateToCamera,
                        modifier = Modifier.weight(1f)
                    )
                    GlassmorphismCard(
                        title = "Editor",
                        icon = "✏️",
                        onClick = onNavigateToEditor,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 第二行：Gallery + Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GlassmorphismCard(
                        title = "Gallery",
                        icon = "🖼️",
                        onClick = onNavigateToGallery,
                        modifier = Modifier.weight(1f)
                    )
                    GlassmorphismCard(
                        title = "Settings",
                        icon = "⚙️",
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Hot Spots Nearby
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hot Spots Nearby",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(hotSpots) { spot ->
                        HotSpotCard(spot = spot)
                    }
                }
            }
        }
        
        // 中央大圆形相机按钮
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF69B4),
                            Color(0xFFDA70D6)
                        )
                    )
                )
                .clickable { onNavigateToCamera() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📷",
                fontSize = 40.sp
            )
        }
        
        // 底部导航栏
        BottomNavigationBar(
            currentTab = "Home",
            onHomeClick = { /* 已在首页 */ },
            onExploreClick = onNavigateToExplore,
            onCameraClick = onNavigateToCamera,
            onCommunityClick = onNavigateToCommunity,
            onProfileClick = onNavigateToProfile,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * 星光粒子背景效果
 */
@Composable
fun StarfieldBackground() {
    val stars = remember {
        List(50) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                alpha = Random.nextFloat() * 0.5f + 0.3f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "starfield")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { star ->
            val x = star.x * size.width
            val y = star.y * size.height
            val alpha = star.alpha * twinkle
            
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = star.size,
                center = Offset(x, y)
            )
        }
    }
}

data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float
)

/**
 * 毛玻璃卡片
 */
@Composable
fun GlassmorphismCard(
    title: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                color = Color.White.copy(alpha = 0.2f)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = icon,
                fontSize = 48.sp
            )
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * Hot Spot卡片
 */
@Composable
fun HotSpotCard(spot: HotSpot) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = Color.White.copy(alpha = 0.3f)
            )
            .padding(8.dp)
    ) {
        // 照片预览
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = spot.emoji,
                fontSize = 48.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 标题
        Text(
            text = spot.title,
            fontSize = 14.sp,
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
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 底部导航栏
 */
@Composable
fun BottomNavigationBar(
    currentTab: String,
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onCameraClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                color = Color.White.copy(alpha = 0.2f)
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
            icon = "🧭",
            label = "Explore",
            isSelected = currentTab == "Explore",
            onClick = onExploreClick
        )
        // 中央相机按钮占位（实际按钮在上方）
        Spacer(modifier = Modifier.width(80.dp))
        BottomNavItem(
            icon = "👥",
            label = "Community",
            isSelected = currentTab == "Community",
            onClick = onCommunityClick
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
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFFFF69B4) else Color.White
        )
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
    HotSpot("Scenic Photo...", "🌅", 4),
    HotSpot("Youch Mouss...", "🏝️", 5),
    HotSpot("Photography", "📸", 4),
    HotSpot("Detine", "🏔️", 5)
)

/**
 * Canvas扩展（用于星光效果）
 */
@Composable
fun Canvas(modifier: Modifier, onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}
