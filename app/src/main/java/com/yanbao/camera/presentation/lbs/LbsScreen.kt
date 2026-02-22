package com.yanbao.camera.presentation.lbs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanbao.camera.R

// 颜色定义
private val KUROMI_PINK = Color(0xFFEC4899)
private val KUROMI_PURPLE = Color(0xFF9D4EDD)
private val OBSIDIAN_BLACK = Color(0xFF0A0A0A)

/**
 * LBS 推荐模块主界面
 * 使用自定义地图占位（不依赖 Google Maps SDK）
 * 包含：地图视图、地点标记、底部收缩/展开面板、地点卡片
 */
@Composable
fun LbsScreen(
    viewModel: LbsViewModel = hiltViewModel(),
    onApplyFilter: (String) -> Unit = {}
) {
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val isPanelExpanded by viewModel.isPanelExpanded.collectAsStateWithLifecycle()

    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val panelHeight = if (isPanelExpanded) screenHeight * 0.6f else screenHeight * 0.25f

    Box(modifier = Modifier.fillMaxSize().background(OBSIDIAN_BLACK)) {

        // Layer 0: 地图占位（深色网格模拟地图）
        MapPlaceholder(
            locations = locations,
            selectedLocation = selectedLocation,
            onMarkerClick = { viewModel.selectLocation(it) },
            modifier = Modifier.fillMaxSize()
        )

        // 顶部品牌标识
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "yanbao AI",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(KUROMI_PINK, KUROMI_PURPLE)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        alpha = 0f
                    )
            )
        }

        // 当前选中地点的悬浮卡片
        selectedLocation?.let { location ->
            LocationCard(
                location = location,
                onDismiss = { viewModel.clearSelectedLocation() },
                onApplyFilter = { viewModel.applyFilter(it) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp)
            )
        }

        // Layer 1: 底部面板
        LbsBottomPanel(
            isExpanded = isPanelExpanded,
            panelHeight = panelHeight,
            locations = locations,
            onToggle = { viewModel.togglePanel() },
            onLocationClick = { viewModel.selectLocation(it) },
            onApplyFilter = { viewModel.applyFilter(it) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * 地图占位组件（模拟深色地图背景）
 */
@Composable
private fun MapPlaceholder(
    locations: List<LocationItem>,
    selectedLocation: LocationItem?,
    onMarkerClick: (LocationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Color(0xFF1A1A2E))
    ) {
        // 模拟地图网格线
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = Color(0xFF2A2A4A)
            val step = 60f
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(x, 0f), end = androidx.compose.ui.geometry.Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }

        // 地图道路模拟
        Text(
            text = "📍 台北市",
            color = Color(0xFF4A4A6A),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 200.dp)
        )

        // 地点标记
        locations.forEachIndexed { index, location ->
            val xOffset = (index * 120 + 80).dp
            val yOffset = (index * 80 + 200).dp
            KuromiMarker(
                location = location,
                isSelected = selectedLocation?.id == location.id,
                onClick = { onMarkerClick(location) },
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
            )
        }

        // 用户当前位置标记
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-50).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color(0xFF4FC3F7), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF4FC3F7).copy(alpha = 0.3f), CircleShape)
                    .align(Alignment.Center)
            )
        }
    }
}

/**
 * 库洛米风格地点标记
 */
@Composable
private fun KuromiMarker(
    location: LocationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标记气泡（带小耳朵）
        Box(
            modifier = Modifier
                .size(if (isSelected) 56.dp else 44.dp)
                .background(
                    if (isSelected) KUROMI_PINK else Color(0xFF2D1B4E),
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // 小耳朵装饰
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(KUROMI_PINK, CircleShape))
                Box(modifier = Modifier.size(8.dp).background(KUROMI_PINK, CircleShape))
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_location_kuromi),
                contentDescription = location.name,
                tint = if (isSelected) Color.White else KUROMI_PINK,
                modifier = Modifier.size(20.dp)
            )
        }
        // 地点名称
        Text(
            text = location.name,
            color = if (isSelected) KUROMI_PINK else Color.White,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

/**
 * 地点悬浮卡片
 */
@Composable
fun LocationCard(
    location: LocationItem,
    onDismiss: () -> Unit,
    onApplyFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = OBSIDIAN_BLACK.copy(alpha = 0.92f),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 缩略图占位
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2D1B4E), Color(0xFF4A1A3A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📸", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Text(
                                text = if (index < location.rating.toInt()) "★" else "☆",
                                color = if (index < location.rating.toInt()) KUROMI_PINK else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = " · ${location.distance}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Text("✕", color = Color.Gray, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = KUROMI_PINK.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "✨ 推荐滤镜：${location.filterSuggestion}",
                    color = KUROMI_PINK,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("查看详情")
                }
                Button(
                    onClick = { onApplyFilter(location.filterSuggestion) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KUROMI_PINK),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("应用滤镜")
                }
            }
        }
    }
}

/**
 * 底部面板（收缩/展开）
 */
@Composable
fun LbsBottomPanel(
    isExpanded: Boolean,
    panelHeight: androidx.compose.ui.unit.Dp,
    locations: List<LocationItem>,
    onToggle: () -> Unit,
    onLocationClick: (LocationItem) -> Unit,
    onApplyFilter: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(panelHeight),
        color = OBSIDIAN_BLACK.copy(alpha = 0.92f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 拖拽指示条
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 面板头部
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isExpanded) "附近热门地点" else "推荐摘要",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isExpanded) {
                        Text(
                            text = "台北101、西门町、象山步道...",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                Surface(
                    color = KUROMI_PINK.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Text(
                        text = if (isExpanded) "▼" else "▲",
                        color = KUROMI_PINK,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(locations) { location ->
                        LocationListItem(
                            location = location,
                            onClick = { onLocationClick(location) },
                            onApply = { onApplyFilter(location.filterSuggestion) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 地点列表项
 */
@Composable
fun LocationListItem(
    location: LocationItem,
    onClick: () -> Unit,
    onApply: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 缩略图
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF2D1B4E), Color(0xFF4A1A3A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("📍", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Text(
                        text = if (index < location.rating.toInt()) "★" else "☆",
                        color = if (index < location.rating.toInt()) KUROMI_PINK else Color.Gray,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = " · ${location.distance}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Text(
                text = location.filterSuggestion,
                color = KUROMI_PINK.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
        Button(
            onClick = onApply,
            colors = ButtonDefaults.buttonColors(containerColor = KUROMI_PINK),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(text = "应用", fontSize = 12.sp, color = Color.White)
        }
    }
}
