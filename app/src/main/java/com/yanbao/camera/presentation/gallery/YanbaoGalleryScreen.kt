package com.yanbao.camera.presentation.gallery

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.presentation.theme.YanbaoPink
import com.yanbao.camera.presentation.theme.YanbaoPurple

/**
 * Yanbao Gallery Screen - Spatial Layering Architecture
 * 
 * 相冊模組 - 三層空間分層架構
 * 
 * 核心結構：三層空間定義 (Spatial Layout)
 * - 底層 (LBS 地圖層)：全屏背景，粉色發光點地圖
 * - 中層 (瀑布流層)：72/28 比例，雁宝记忆 + 一般相冊
 * - 頂層 (交互抽屜)：5 标签导航 + 功能切換
 * 
 * 功能模組切換邏輯：
 * - 雙指縮小 (Zoom Out)：一般相冊 → LBS 地圖（照片縮成發光圖釘）
 * - 雙指放大 (Zoom In)：進入雁宝记忆（全屏沉浸 + 70dp 高斯模糊）
 * 
 * 視覺規格：
 * - 雁宝记忆：大卡片流，漸變邊框 + 29D 参数注释
 * - 一般相冊：3 列网格，12dp 圓角，粉色光暈
 * - LBS 地圖：深黑背景 + 霓虹粉 (#FFB6C1) 座標點
 */
@Composable
fun YanbaoGalleryScreen(
    onPhotoClick: (String) -> Unit = { photoId ->
        android.util.Log.d("YanbaoGalleryScreen", "Photo clicked: $photoId")
        // 导航到照片详情页的逻辑由调用者处理
        // 这里只负责回调，实际导航由NavController处理
    },
    modifier: Modifier = Modifier
) {
    var currentMode by remember { mutableStateOf(GalleryMode.Normal) }
    
    Box(modifier = modifier.fillMaxSize()) {
        
        // Layer 1: 底層 LBS 地圖背景
        LbsMapView(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.4f)
        )
        
        // Layer 2: 中層可滾動的相冊內容
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部切換 Tab (雁宝记忆 | 一般 | LBS)
            YanbaoAlbumTabs(
                currentMode = currentMode,
                onModeChange = { currentMode = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 根據選擇切換模式
            when (currentMode) {
                GalleryMode.Memory -> MemoryCarousel(onPhotoClick)
                GalleryMode.Normal -> PhotoGrid(onPhotoClick)
                GalleryMode.Lbs -> LbsDetailList()
            }
        }
    }
}

/**
 * 相冊模式枚舉
 */
enum class GalleryMode {
    Memory,  // 雁宝记忆
    Normal,  // 一般相冊
    Lbs      // LBS 地圖
}

/**
 * Layer 1: LBS 地圖背景
 * 
 * 深黑背景 + 霓虹粉 (#FFB6C1) 座標點
 */
@Composable
fun LbsMapView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF1A1A1A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 模擬地圖發光點
        repeat(10) { index ->
            Box(
                modifier = Modifier
                    .offset(
                        x = (index * 40).dp,
                        y = (index * 30).dp
                    )
                    .size(8.dp)
                    .background(YanbaoPink, shape = RoundedCornerShape(50))
                    .blur(4.dp)
            )
        }
        
        Text(
            text = "LBS Map Layer\n(地圖背景)",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 14.sp
        )
    }
}

/**
 * 顶部切換 Tab
 * 
 * 雁宝记忆 | 一般 | LBS
 */
@Composable
fun YanbaoAlbumTabs(
    currentMode: GalleryMode,
    onModeChange: (GalleryMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xCC0A0A0A))
            .padding(vertical = 12.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabItem(
            label = "雁宝记忆",
            isSelected = currentMode == GalleryMode.Memory,
            onClick = { onModeChange(GalleryMode.Memory) }
        )
        
        TabItem(
            label = "一般",
            isSelected = currentMode == GalleryMode.Normal,
            onClick = { onModeChange(GalleryMode.Normal) }
        )
        
        TabItem(
            label = "LBS",
            isSelected = currentMode == GalleryMode.Lbs,
            onClick = { onModeChange(GalleryMode.Lbs) }
        )
    }
}

/**
 * Tab 項目
 */
@Composable
fun TabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        color = if (isSelected) YanbaoPink else Color.White.copy(alpha = 0.5f),
        fontSize = 16.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * 雁宝记忆大卡片流
 * 
 * 大卡片流，漸變邊框 + 29D 参数注释
 */
@Composable
fun MemoryCarousel(
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(5) { index ->
            MemoryCard(
                photoId = "memory_$index",
                location = "Tokyo, Japan",
                date = "2026.02.21",
                onClick = { onPhotoClick("memory_$index") }
            )
        }
    }
}

/**
 * 雁宝记忆卡片
 */
@Composable
fun MemoryCard(
    photoId: String,
    location: String,
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        YanbaoPink.copy(0.2f),
                        YanbaoPurple.copy(0.2f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(YanbaoPink, YanbaoPurple)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📷",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = location,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = date,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 一般相冊网格
 * 
 * 3 列网格，12dp 圓角，粉色光暈
 */
@Composable
fun PhotoGrid(
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(30) { index ->
            PhotoGridItem(
                photoId = "photo_$index",
                onClick = { onPhotoClick("photo_$index") }
            )
        }
    }
}

/**
 * 相冊网格項目
 */
@Composable
fun PhotoGridItem(
    photoId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                color = Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 0.5.dp,
                color = YanbaoPink.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "📷",
            fontSize = 24.sp
        )
    }
}

/**
 * LBS 詳細地點列表
 */
@Composable
fun LbsDetailList(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(10) { index ->
            LbsLocationItem(
                location = "Location $index",
                photoCount = (index + 1) * 3
            )
        }
    }
}

/**
 * LBS 地點項目
 */
@Composable
fun LbsLocationItem(
    location: String,
    photoCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "📍",
                fontSize = 20.sp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = location,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = "$photoCount 張",
            color = YanbaoPink,
            fontSize = 14.sp
        )
    }
}
