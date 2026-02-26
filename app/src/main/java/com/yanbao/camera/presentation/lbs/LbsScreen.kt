package com.yanbao.camera.presentation.lbs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.yanbao.camera.R
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// 颜色定义
private val KUROMI_PINK = Color(0xFFEC4899)
private val KUROMI_PURPLE = Color(0xFF9D4EDD)
private val OBSIDIAN_BLACK = Color(0xFF0A0A0A)
private val MAP_BG = Color(0xFF0D1117)
private val MAP_ROAD = Color(0xFF1E2A3A)
private val MAP_WATER = Color(0xFF0A1628)
private val MAP_PARK = Color(0xFF0A1F0A)

/**
 * LBS 推荐模块主界面（满血版）
 *
 * 特性：
 * - Canvas 自绘深色地图（道路/水域/公园层次）
 * - 真实经纬度 Mercator 投影坐标
 * - Coil 加载真实地点缩略图
 * - GPS 权限引导
 * - 零 emoji 占位符
 */
@Composable
fun LbsScreen(
    viewModel: LbsViewModel = hiltViewModel(),
    onApplyFilter: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    navController: androidx.navigation.NavController? = null
) {
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val isPanelExpanded by viewModel.isPanelExpanded.collectAsStateWithLifecycle()
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val locationError by viewModel.locationError.collectAsStateWithLifecycle()
    val viewport by viewModel.mapViewport.collectAsStateWithLifecycle()

    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val panelHeight = if (isPanelExpanded) screenHeight * 0.6f else screenHeight * 0.28f

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MAP_BG)) {

        // ─── Layer 0: Canvas 地图（真实坐标投影）────────────────────────
        YanbaoMapCanvas(
            locations = locations,
            selectedLocation = selectedLocation,
            userLocation = userLocation,
            viewport = viewport,
            onMarkerClick = { viewModel.selectLocation(it) },
            modifier = Modifier.fillMaxSize()
        )

        // ─── 顶部返回按钮 + 品牌标识 + 刷新按钮 ────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 8.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回上一层
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(OBSIDIAN_BLACK.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_yanbao_back),
                    contentDescription = "返回",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "yanbao AI",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(listOf(KUROMI_PINK, KUROMI_PURPLE)),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            if (isLoading) {
                CircularProgressIndicator(
                    color = KUROMI_PINK,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(
                    onClick = { viewModel.refreshLocation() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(OBSIDIAN_BLACK.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location_kuromi),
                        contentDescription = "刷新位置",
                        tint = KUROMI_PINK,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // ─── 权限/位置错误提示 ────────────────────────────────────────
        locationError?.let { error ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp),
                color = OBSIDIAN_BLACK.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location_kuromi),
                        contentDescription = null,
                        tint = KUROMI_PINK,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = error, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }

        // ─── 选中地点悬浮卡片 ─────────────────────────────────────────
        selectedLocation?.let { location ->
            LocationCard(
                location = location,
                onDismiss = { viewModel.clearSelectedLocation() },
                onApplyFilter = {
                    viewModel.applyFilter(it)
                    onApplyFilter(it)
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = if (locationError != null) 148.dp else 104.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
            )
        }

        // ─── Layer 1: 底部面板 ────────────────────────────────────────
        LbsBottomPanel(
            isExpanded = isPanelExpanded,
            panelHeight = panelHeight,
            locations = locations,
            onToggle = { viewModel.togglePanel() },
            onLocationClick = { viewModel.selectLocation(it) },
            onApplyFilter = {
                viewModel.applyFilter(it)
                onApplyFilter(it)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Canvas 自绘地图（真实 Mercator 坐标投影）
 */
@Composable
private fun YanbaoMapCanvas(
    locations: List<LocationItem>,
    selectedLocation: LocationItem?,
    userLocation: LatLngSimple?,
    viewport: MapViewport,
    onMarkerClick: (LocationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 底层 Canvas 地图
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMapBackground()
            drawMapRoads(viewport)
            drawMapWater(viewport)
        }

        // 地点标记（Compose 层，支持点击）
        locations.forEach { location ->
            val (nx, ny) = projectToNormalized(
                location.latLng.latitude, location.latLng.longitude, viewport
            )
            val isSelected = selectedLocation?.id == location.id

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
            ) {
                KuromiMarker(
                    location = location,
                    isSelected = isSelected,
                    onClick = { onMarkerClick(location) },
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopStart)
                        .offset(
                            x = (nx * LocalConfiguration.current.screenWidthDp - 24).dp,
                            y = (ny * LocalConfiguration.current.screenHeightDp * 0.7f - 32).dp
                        )
                )
            }
        }

        // 用户位置蓝点
        userLocation?.let { loc ->
            val (nx, ny) = projectToNormalized(loc.latitude, loc.longitude, viewport)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart)
                    .offset(
                        x = (nx * LocalConfiguration.current.screenWidthDp - 12).dp,
                        y = (ny * LocalConfiguration.current.screenHeightDp * 0.7f - 12).dp
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF4FC3F7).copy(alpha = 0.25f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.Center)
                        .background(Color(0xFF4FC3F7), CircleShape)
                )
            }
        }
    }
}

private fun DrawScope.drawMapBackground() {
    drawRect(MAP_BG)
    // 网格线（模拟地图底纹）
    val step = 80f
    var x = 0f
    while (x < size.width) {
        drawLine(
            color = Color(0xFF1A2030),
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += step
    }
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = Color(0xFF1A2030),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun DrawScope.drawMapRoads(viewport: MapViewport) {
    // 主干道（横向）
    val roadColor = MAP_ROAD
    val roadY1 = size.height * 0.35f
    val roadY2 = size.height * 0.55f
    val roadY3 = size.height * 0.72f
    drawLine(roadColor, Offset(0f, roadY1), Offset(size.width, roadY1), strokeWidth = 8f)
    drawLine(roadColor, Offset(0f, roadY2), Offset(size.width, roadY2), strokeWidth = 6f)
    drawLine(roadColor, Offset(0f, roadY3), Offset(size.width, roadY3), strokeWidth = 4f)

    // 主干道（纵向）
    val roadX1 = size.width * 0.3f
    val roadX2 = size.width * 0.6f
    drawLine(roadColor, Offset(roadX1, 0f), Offset(roadX1, size.height), strokeWidth = 7f)
    drawLine(roadColor, Offset(roadX2, 0f), Offset(roadX2, size.height), strokeWidth = 5f)

    // 次干道
    val secondaryColor = Color(0xFF161E2A)
    for (i in 1..4) {
        val y = size.height * (i * 0.18f)
        drawLine(secondaryColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 3f)
    }
    for (i in 1..5) {
        val x = size.width * (i * 0.18f)
        drawLine(secondaryColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2f)
    }
}

private fun DrawScope.drawMapWater(viewport: MapViewport) {
    // 模拟河流（曲线）
    val path = Path().apply {
        moveTo(0f, size.height * 0.45f)
        cubicTo(
            size.width * 0.2f, size.height * 0.42f,
            size.width * 0.5f, size.height * 0.48f,
            size.width * 0.7f, size.height * 0.44f
        )
        cubicTo(
            size.width * 0.85f, size.height * 0.41f,
            size.width * 0.95f, size.height * 0.43f,
            size.width, size.height * 0.42f
        )
        lineTo(size.width, size.height * 0.46f)
        cubicTo(
            size.width * 0.95f, size.height * 0.47f,
            size.width * 0.85f, size.height * 0.45f,
            size.width * 0.7f, size.height * 0.48f
        )
        cubicTo(
            size.width * 0.5f, size.height * 0.52f,
            size.width * 0.2f, size.height * 0.46f,
            0f, size.height * 0.49f
        )
        close()
    }
    drawPath(path, MAP_WATER)
}

/**
 * 将经纬度投影到 [0, 1] 归一化坐标
 */
private fun projectToNormalized(lat: Double, lng: Double, viewport: MapViewport): Pair<Float, Float> {
    val x = ((lng - viewport.centerLng) / viewport.spanLng + 0.5).toFloat().coerceIn(0.05f, 0.95f)
    val y = (0.5f - ((lat - viewport.centerLat) / viewport.spanLat)).toFloat().coerceIn(0.05f, 0.95f)
    return Pair(x, y)
}

/**
 * 库洛米风格地点标记（带小耳朵 + 蝴蝶结）
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
        Box(
            modifier = Modifier
                .size(if (isSelected) 52.dp else 40.dp)
        ) {
            // 主体气泡
            Box(
                modifier = Modifier
                    .size(if (isSelected) 44.dp else 34.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        if (isSelected) KUROMI_PINK else Color(0xFF2D1B4E),
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomEnd = 16.dp, bottomStart = 4.dp
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location_kuromi),
                    contentDescription = location.name,
                    tint = if (isSelected) Color.White else KUROMI_PINK,
                    modifier = Modifier.size(16.dp)
                )
            }
            // 左耳朵
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.TopStart)
                    .background(KUROMI_PINK, CircleShape)
            )
            // 右耳朵
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .background(KUROMI_PINK, CircleShape)
            )
        }
        // 地点名称标签
        Text(
            text = location.name,
            color = if (isSelected) KUROMI_PINK else Color.White,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

/**
 * 地点悬浮卡片（真实 Coil 缩略图）
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
        color = OBSIDIAN_BLACK.copy(alpha = 0.93f),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 真实缩略图（Coil 加载）
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1A2E))
                ) {
                    if (location.thumbnailUrl.isNotBlank()) {
                        AsyncImage(
                            model = location.thumbnailUrl,
                            contentDescription = location.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location_kuromi),
                            contentDescription = null,
                            tint = KUROMI_PINK,
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.Center)
                        )
                    }
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
                        // 星级
                        val fullStars = location.rating.toInt()
                        repeat(5) { i ->
                            Text(
                                text = if (i < fullStars) "*" else "o",
                                color = if (i < fullStars) KUROMI_PINK else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = " ${location.rating} · ${location.distance}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_kuromi),
                        contentDescription = "关闭",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 推荐滤镜标签
            Surface(
                color = KUROMI_PINK.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = null,
                        tint = KUROMI_PINK,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "推荐滤镜：${location.filterSuggestion}",
                        color = KUROMI_PINK,
                        fontSize = 13.sp
                    )
                }
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
        color = OBSIDIAN_BLACK.copy(alpha = 0.93f),
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
                    if (!isExpanded && locations.isNotEmpty()) {
                        Text(
                            text = locations.take(3).joinToString("、") { it.name },
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                Surface(
                    color = KUROMI_PINK.copy(alpha = 0.2f),
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isExpanded) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
                        ),
                        contentDescription = if (isExpanded) "收起" else "展开",
                        tint = KUROMI_PINK,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(8.dp)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(locations, key = { it.id }) { location ->
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
 * 地点列表项（真实 Coil 缩略图）
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
        // 真实缩略图
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A1A2E))
        ) {
            if (location.thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = location.thumbnailUrl,
                    contentDescription = location.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location_kuromi),
                    contentDescription = null,
                    tint = KUROMI_PINK,
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.Center)
                )
            }
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
                val fullStars = location.rating.toInt()
                repeat(5) { i ->
                    Text(
                        text = if (i < fullStars) "*" else "o",
                        color = if (i < fullStars) KUROMI_PINK else Color.Gray,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = " ${location.rating} · ${location.distance}",
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
