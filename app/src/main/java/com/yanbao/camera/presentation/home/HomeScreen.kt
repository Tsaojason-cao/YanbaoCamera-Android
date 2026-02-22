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
import androidx.navigation.NavController
import com.yanbao.camera.R

// ── 设计稿背景渐变色 ──
private val BG_TOP = Color(0xFFAF96E3)
private val BG_MID = Color(0xFFC69ECE)
private val BG_BOT = Color(0xFFF3A0BE)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(BG_TOP, BG_MID, BG_BOT))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ── 顶部：左圆点 + 中头像 + 右yanbao AI/天气 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧装饰圆点
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9B59B6))
                )

                // 中间头像 120dp
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9B59B6)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.avatar_user),
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // 右侧：yanbao AI + 天气
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "yanbao AI",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "☀️", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${uiState.temperature}°C",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = uiState.weatherDesc,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 问候语 ──
            Text(
                text = uiState.greeting,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 72.sp
            )
            Text(
                text = uiState.greetingSub,
                fontSize = 22.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── 四功能按钮（2×2 网格，每格 200dp 高）──
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BigActionCard(
                        title = "拍照",
                        iconRes = R.drawable.ic_tab_camera_kuromi,
                        gradient = listOf(Color(0xFFEC4899), Color(0xFF9D4EDD)),
                        onClick = { navController.navigate("camera") },
                        modifier = Modifier.weight(1f)
                    )
                    BigActionCard(
                        title = "编辑",
                        iconRes = R.drawable.ic_tab_edit_kuromi,
                        gradient = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A)),
                        onClick = { navController.navigate("editor") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BigActionCard(
                        title = "AI推荐",
                        iconRes = R.drawable.ic_tab_recommend_kuromi_nobg,
                        gradient = listOf(Color(0xFFD4A020), Color(0xFFB88010)),
                        onClick = { navController.navigate("lbs") },
                        modifier = Modifier.weight(1f)
                    )
                    BigActionCard(
                        title = "相册",
                        iconRes = R.drawable.ic_tab_album_kuromi,
                        gradient = listOf(Color(0xFF90C0E8), Color(0xFF5090C0)),
                        onClick = { navController.navigate("gallery") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── 最近活动 ──
            SectionHeader(title = "最近活动") { navController.navigate("lbs") }
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(uiState.recentActivities) { activity ->
                    RecentActivityCard(activity)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ── 热门地点 ──
            SectionHeader(title = "热门地点") { navController.navigate("lbs") }
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(uiState.popularPlaces) { place ->
                    PopularPlaceCard(
                        place = place,
                        onClick = { navController.navigate("lbs") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── 底部导航栏 ──
        HomeBottomNavigation(
            selectedItem = "home",
            onItemSelected = { route ->
                if (route == "home") navController.popBackStack("home", inclusive = false)
                else navController.navigate(route)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────
// 大功能按钮卡片 — 高度 200dp，图标 100dp，文字 32sp
// ─────────────────────────────────────────────
@Composable
private fun BigActionCard(
    title: String,
    iconRes: Int,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────
// 区块标题行 — 字体 32sp
// ─────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, onMore: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 54.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Color(0xFF555555),
            modifier = Modifier
                .size(32.dp)
                .clickable { onMore() }
        )
    }
}

// ─────────────────────────────────────────────
// 最近活动卡片 — 宽 340dp，图标 88dp，文字 20sp
// ─────────────────────────────────────────────
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier = Modifier
            .width(340.dp)
            .wrapContentHeight()
            .clickable { },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCCFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 库洛米图标框
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_tab_home_kuromi),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = activity.description,
                    fontSize = 20.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Time ${activity.time}",
                fontSize = 24.sp,
                color = Color(0xFFEEEEEE),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// ─────────────────────────────────────────────
// 热门地点卡片 — 宽 260dp，图片区 200dp，贴纸 72dp
// ─────────────────────────────────────────────
@Composable
private fun PopularPlaceCard(place: PopularPlace, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val photoRes = when (place.name) {
                    "台北101"  -> R.drawable.place_taipei101
                    "台南波场" -> R.drawable.place_tainan
                    "北海坑境" -> R.drawable.place_hokkaido
                    else       -> R.drawable.place_taipei101
                }
                Image(
                    painter = painterResource(photoRes),
                    contentDescription = place.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    contentScale = ContentScale.Crop
                )
                val kuromiRes = when (place.name) {
                    "台北101"  -> R.drawable.ic_tab_home_kuromi
                    "台南波场" -> R.drawable.ic_tab_camera_kuromi
                    else       -> R.drawable.ic_tab_recommend_kuromi
                }
                Image(
                    painter = painterResource(kuromiRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                )
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    text = place.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 五颗星（使用 Text 字符，避免 ic_star_filled 占位符问题）
                    Row {
                        repeat(5) { index ->
                            Text(
                                text = "★",
                                fontSize = 22.sp,
                                color = if (index < place.rating) Color(0xFFFFD700) else Color(0xFFE0E0E0)
                            )
                        }
                    }
                    Text(text = "📍", fontSize = 22.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 底部导航栏 — 高度 96dp，图标 48dp（3倍），文字 16sp
// ─────────────────────────────────────────────
@Composable
fun HomeBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        shape = RoundedCornerShape(48.dp),
        color = Color(0xFFFAE8F0),
        shadowElevation = 10.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(96.dp)
        ) {
            val items = listOf(
                Triple("首页",  R.drawable.ic_tab_home_kuromi,      "home"),
                Triple("拍照",  R.drawable.ic_tab_camera_kuromi,    "camera"),
                Triple("编辑",  R.drawable.ic_tab_edit_kuromi,      "editor"),
                Triple("相册",  R.drawable.ic_tab_album_kuromi,     "gallery"),
                Triple("推荐",  R.drawable.ic_tab_recommend_kuromi_nobg, "lbs"),
                Triple("我的",  R.drawable.ic_tab_profile_kuromi,   "profile")
            )
            items.forEach { (label, iconRes, route) ->
                val selected = selectedItem == route
                NavigationBarItem(
                    selected = selected,
                    onClick  = { onItemSelected(route) },
                    icon = {
                        Image(
                            painter = painterResource(iconRes),
                            contentDescription = label,
                            modifier = Modifier.size(48.dp),
                            colorFilter = if (!selected) ColorFilter.colorMatrix(
                                ColorMatrix().apply { setToSaturation(0f) }
                            ) else null
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 16.sp,
                            color = if (selected) Color(0xFFEC4899) else Color(0xFF888888)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Color(0xFFEC4899),
                        unselectedIconColor = Color(0xFF888888),
                        indicatorColor      = Color.Transparent
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// 数据类
// ─────────────────────────────────────────────
data class RecentActivity(val description: String, val time: String)
data class PopularPlace(val name: String, val rating: Int)

data class HomeUiState(
    val greeting:         String               = "早安！",
    val greetingSub:      String               = "今天也要拍出好照片哦",
    val temperature:      Int                  = 28,
    val weatherDesc:      String               = "适合外拍",
    val motto:            String               = "今天也要拍出好照片哦",
    val recentActivities: List<RecentActivity> = emptyList(),
    val popularPlaces:    List<PopularPlace>   = emptyList()
)
