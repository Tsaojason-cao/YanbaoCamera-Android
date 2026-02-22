package com.yanbao.camera.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanbao.camera.R

// ─────────────────────────────────────────────────────────────
// 精确色值（从设计稿像素采样）
// ─────────────────────────────────────────────────────────────
private val BG_TOP       = Color(0xFFAF96E3)  // 背景顶部：淡紫
private val BG_MID       = Color(0xFFC69ECE)  // 背景中部：粉紫
private val BG_BOT       = Color(0xFFF3A0BE)  // 背景底部：粉红
private val BTN_CAM_1    = Color(0xFFE5ADDD)  // 拍照渐变起点
private val BTN_CAM_2    = Color(0xFFEC4899)  // 拍照渐变终点（品牌粉）
private val BTN_EDIT_1   = Color(0xFF343434)  // 编辑深炭黑
private val BTN_EDIT_2   = Color(0xFF1A1A1A)
private val BTN_AI_1     = Color(0xFFE0B757)  // AI推荐金色
private val BTN_AI_2     = Color(0xFFD4A017)
private val BTN_ALB_1    = Color(0xFF98CAF5)  // 相册天蓝
private val BTN_ALB_2    = Color(0xFF6BAED6)
private val NAV_BG       = Color(0xFFFAE2E6)  // 底部导航浅粉
private val CARD_BG      = Color(0xFFF6F3F2)  // 活动卡片背景
private val PINK_ACCENT  = Color(0xFFEC4899)  // 品牌粉（选中态）
private val AVATAR_RING  = Color(0xFF9B59B6)  // 头像紫色光晕
private val TEXT_DARK    = Color(0xFF2C1654)  // 深紫文字

// ─────────────────────────────────────────────────────────────
// 首页主入口
// ─────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onProfileClick: () -> Unit,
    avatarUri: String? = null,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(BG_TOP, BG_MID, BG_BOT)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // ── 顶部信息区 ──
            HomeTopBar()
            Spacer(modifier = Modifier.height(20.dp))
            // ── 四功能按钮 ──
            FunctionButtonGrid(
                onCameraClick = onCameraClick,
                onEditorClick = onEditorClick,
                onRecommendClick = onRecommendClick,
                onGalleryClick = onGalleryClick
            )
            Spacer(modifier = Modifier.height(24.dp))
            // ── 最近活动 ──
            SectionHeader(title = "最近活动 ⭐")
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PINK_ACCENT, strokeWidth = 2.dp)
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.recentActivities) { activity ->
                        RecentActivityCard(activity = activity)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            // ── 热门地点 ──
            SectionHeader(title = "热门地点 🔥")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.popularPlaces) { place ->
                    PopularPlaceCard(place = place)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 底部6标签导航 ──
        HomeBottomNavigation(
            selectedItem = "home",
            onItemSelected = { route ->
                when (route) {
                    "camera"  -> onCameraClick()
                    "editor"  -> onEditorClick()
                    "gallery" -> onGalleryClick()
                    "lbs"     -> onRecommendClick()
                    "profile" -> onProfileClick()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 顶部信息区（问候 + 头像 + 天气）
// ─────────────────────────────────────────────────────────────
@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：问候语
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "早安！💝",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "今天也要拍出好照片哦",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        // 中间：用户头像（紫色光晕圆框）
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(12.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(
                        Brush.radialGradient(colors = listOf(AVATAR_RING, Color(0xFF6C3483))),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFD7BDE2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 26.sp)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        // 右侧：天气
        Column(horizontalAlignment = Alignment.End) {
            Text("☀️ 28°C", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("适合外拍", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 四功能按钮（2×2 网格）
// ─────────────────────────────────────────────────────────────
@Composable
private fun FunctionButtonGrid(
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FunctionButton(
                modifier = Modifier.weight(1f),
                label = "拍照",
                icon = "📷",
                gradient = listOf(BTN_CAM_1, BTN_CAM_2),
                onClick = onCameraClick
            )
            FunctionButton(
                modifier = Modifier.weight(1f),
                label = "编辑",
                icon = "✏️",
                gradient = listOf(BTN_EDIT_1, BTN_EDIT_2),
                onClick = onEditorClick
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FunctionButton(
                modifier = Modifier.weight(1f),
                label = "AI推荐",
                icon = "✨",
                gradient = listOf(BTN_AI_1, BTN_AI_2),
                onClick = onRecommendClick
            )
            FunctionButton(
                modifier = Modifier.weight(1f),
                label = "相册",
                icon = "📖",
                gradient = listOf(BTN_ALB_1, BTN_ALB_2),
                onClick = onGalleryClick
            )
        }
    }
}

@Composable
private fun FunctionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 30.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 小节标题（含右箭头）
// ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TEXT_DARK
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = "更多",
            tint = Color(0xFF9B59B6),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 最近活动卡片
// ─────────────────────────────────────────────────────────────
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BG),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 库洛米缩略图
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8D5F0)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tab_home_kuromi),
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TEXT_DARK,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⏱ ${activity.time}",
                    fontSize = 11.sp,
                    color = Color(0xFF9B8FA8)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 热门地点卡片
// ─────────────────────────────────────────────────────────────
@Composable
private fun PopularPlaceCard(place: PopularPlace) {
    // 根据地点名称分配渐变色
    val (colorTop, colorBot) = when (place.name) {
        "台北101"  -> Pair(Color(0xFFFF9A5C), Color(0xFFFF6B35))
        "台南波场" -> Pair(Color(0xFF8B6914), Color(0xFF5C4A1E))
        "北海坑境" -> Pair(Color(0xFF4AADCF), Color(0xFF1A7A9E))
        else       -> Pair(Color(0xFFAA88CC), Color(0xFF7755AA))
    }
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(220.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 顶部景色渐变
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Brush.verticalGradient(colors = listOf(colorTop, colorBot)))
            )
            // 库洛米贴纸（右下角叠加）
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 90.dp, end = 8.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tab_camera_kuromi),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )
            }
            // 底部信息
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = place.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_DARK
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Text(
                            text = "★",
                            fontSize = 14.sp,
                            color = if (index < place.rating) Color(0xFFFFD700) else Color(0xFFDDD0E8)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("📍", fontSize = 13.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 底部6标签导航栏（库洛米主题图标）
// ─────────────────────────────────────────────────────────────
@Composable
fun HomeBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        Triple("首页",  R.drawable.ic_tab_home_kuromi,      "home"),
        Triple("拍照",  R.drawable.ic_tab_camera_kuromi,    "camera"),
        Triple("编辑",  R.drawable.ic_tab_edit_kuromi,      "editor"),
        Triple("相册",  R.drawable.ic_tab_album_kuromi,     "gallery"),
        Triple("推荐",  R.drawable.ic_tab_recommend_kuromi, "lbs"),
        Triple("我的",  R.drawable.ic_tab_profile_kuromi,   "profile")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(12.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(NAV_BG)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (label, iconRes, route) ->
                val isSelected = selectedItem == route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemSelected(route) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(if (isSelected) 40.dp else 34.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = if (!isSelected)
                            ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        else null
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) PINK_ACCENT else Color(0xFF9B8FA8)
                    )
                }
            }
        }
    }
}

private data class BottomNavItem(val label: String, val iconRes: Int, val route: String)
