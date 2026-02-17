package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
 * 改进的RecommendScreen - 完全匹配设计图
 * 包含推荐位置、地图集成、位置卡片等功能
 */
@Composable
fun RecommendScreenImproved(
    onNavigateBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<Int?>(null) }
    
    // 模拟推荐位置数据
    val recommendedLocations = listOf(
        RecommendLocation("故宫", "北京", "4.8★", "1.2km"),
        RecommendLocation("长城", "北京", "4.9★", "5.3km"),
        RecommendLocation("西湖", "杭州", "4.7★", "2.1km"),
        RecommendLocation("黄山", "安徽", "4.9★", "8.5km"),
        RecommendLocation("张家界", "湖南", "4.8★", "15.2km"),
        RecommendLocation("九寨沟", "四川", "4.9★", "25.3km")
    )
    
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏
            TopRecommendBar(
                onBack = onNavigateBack
            )
            
            // 搜索栏
            SearchRecommendBar(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it }
            )
            
            // 推荐位置列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(bottom = 80.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recommendedLocations) { location ->
                    RecommendLocationCard(
                        location = location,
                        isSelected = selectedLocation == recommendedLocations.indexOf(location),
                        onClick = {
                            selectedLocation = recommendedLocations.indexOf(location)
                        }
                    )
                }
            }
        }
        
        // 库洛米装饰
        KuromiCorners()
        
        // 底部地图预览
        if (selectedLocation != null) {
            BottomMapPreview(
                location = recommendedLocations[selectedLocation!!]
            )
        }
    }
}

/**
 * 推荐位置数据类
 */
data class RecommendLocation(
    val name: String,
    val city: String,
    val rating: String,
    val distance: String
)

/**
 * 顶部工具栏
 */
@Composable
private fun TopRecommendBar(
    onBack: () -> Unit
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
                "推荐位置",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Favorites",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 搜索栏
 */
@Composable
private fun SearchRecommendBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                color = Color.White.copy(alpha = 0.25f)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                "搜索位置...",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 推荐位置卡片
 */
@Composable
private fun RecommendLocationCard(
    location: RecommendLocation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isSelected) Color(0xFFEC4899).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 位置图标
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEC4899).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // 位置信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    location.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        location.city,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    
                    Text(
                        location.rating,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    
                    Text(
                        location.distance,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
            
            // 箭头
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 底部地图预览
 */
@Composable
private fun BottomMapPreview(
    location: RecommendLocation
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .align(Alignment.BottomCenter)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFF9A8D4).copy(alpha = 0.95f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 地图预览
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEC4899).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = "Map",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // 位置信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    location.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    location.city,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                
                Text(
                    "距离: ${location.distance}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            
            // 导航按钮
            Button(
                onClick = {},
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEC4899)
                )
            ) {
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = "Navigate",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    "导航",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}
