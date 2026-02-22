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
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFDF5F7),
                        Color(0xFFF9F0F5)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)          // 为底部导航留空间
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── 顶部区域 ──
            TopBar(uiState = uiState)

            Spacer(modifier = Modifier.height(20.dp))

            // ── 拍照 / 编辑 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    title = "拍照",
                    iconRes = R.drawable.ic_camera,
                    gradient = listOf(Color(0xFFEC4899), Color(0xFF9D4EDD)),
                    onClick = { navController.navigate("camera") },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "编辑",
                    iconRes = R.drawable.ic_edit,
                    gradient = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A)),
                    onClick = { navController.navigate("editor") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── AI推荐 / 相册 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    title = "AI推荐",
                    iconRes = R.drawable.ic_recommend,
                    gradient = listOf(Color(0xFFD4A020), Color(0xFFB88010)),
                    onClick = { navController.navigate("lbs") },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "相册",
                    iconRes = R.drawable.ic_gallery,
                    gradient = listOf(Color(0xFF90C0E8), Color(0xFF5090C0)),
                    onClick = { navController.navigate("gallery") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 最近活动 标题 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最近活动",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 最近活动卡片 ──
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(uiState.recentActivities) { activity ->
                    RecentActivityCard(activity)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 热门地点 标题 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "热门地点",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 热门地点卡片 ──
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(uiState.popularPlaces) { place ->
                    PopularPlaceCard(
                        place = place,
                        onClick = { navController.navigate("lbs") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── 底部导航栏 ──
        HomeBottomNavigation(
            selectedItem = "home",
            onItemSelected = { route ->
                when (route) {
                    "home" -> navController.popBackStack("home", inclusive = false)
                    else   -> navController.navigate(route)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────
// 顶部区域
// ─────────────────────────────────────────────
@Composable
private fun TopBar(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // 库洛米耳朵
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_kuromi_ear_left),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_kuromi_ear_right),
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            // yanbao AI — 右上角，黑色 18sp
            Text(
                text = "yanbao AI",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 问候 + 头像 + 天气
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左：问候语
                Column {
                    Text(
                        text = "早安！",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = uiState.motto,
                        fontSize = 14.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }

                // 中：动漫头像（80dp，紫色描边）
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9B59B6))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.avatar_anime),
                        contentDescription = "用户头像",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // 右：天气
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "☀️ ${uiState.temperature}°C",
                        fontSize = 18.sp,
                        color = Color(0xFF444444)
                    )
                    Text(
                        text = uiState.weatherDesc,
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 功能按钮卡片
// 高度 130dp（与设计稿比例一致），圆角 24dp
// 文字 28sp，图标 52dp
// ─────────────────────────────────────────────
@Composable
private fun ActionCard(
    title: String,
    iconRes: Int,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────
// 最近活动卡片
// 宽 220dp，高 90dp，内边距 12dp
// 库洛米图标左侧，时间戳右上，描述文字右侧
// ─────────────────────────────────────────────
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(90.dp)
            .clickable { /* 跳转到活动详情 */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F0F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 库洛米图标 48dp
            Image(
                painter = painterResource(id = R.drawable.ic_tab_home_kuromi),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                // 时间戳（右上角）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "🕐 " + activity.time,
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // 描述
                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// 热门地点卡片
// 宽 160dp，图片区 110dp，圆角 16dp，内边距 8dp
// ─────────────────────────────────────────────
@Composable
private fun PopularPlaceCard(
    place: PopularPlace,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 景色图片 + 库洛米贴纸
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val photoRes = when (place.name) {
                    "台北101"  -> R.drawable.place_taipei101
                    "台南波场" -> R.drawable.place_tainan
                    "北海坑境" -> R.drawable.place_hokkaido
                    else       -> R.drawable.place_taipei101
                }
                Image(
                    painter = painterResource(id = photoRes),
                    contentDescription = place.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                // 库洛米贴纸（右下角，44dp）
                val kuromiRes = when (place.name) {
                    "台北101"  -> R.drawable.ic_tab_home_kuromi
                    "台南波场" -> R.drawable.ic_tab_camera_kuromi
                    else       -> R.drawable.ic_tab_recommend_kuromi
                }
                Image(
                    painter = painterResource(id = kuromiRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }
            // 地点名 + 星级 + 定位图标
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Text(
                    text = place.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        repeat(5) { index ->
                            Icon(
                                painter = painterResource(id = R.drawable.ic_star_filled),
                                contentDescription = null,
                                tint = if (index < place.rating) Color(0xFFFFD700) else Color(0xFFE0E0E0),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "📍",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 底部导航栏
// 高度 72dp，图标 28dp，文字 11sp，选中色 #EC4899
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
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(40.dp),
        color = Color(0xFFFAE8F0),
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(72.dp)
        ) {
            val items = listOf(
                BottomNavItem("首页", R.drawable.ic_tab_home_kuromi,      "home"),
                BottomNavItem("拍照", R.drawable.ic_tab_camera_kuromi,    "camera"),
                BottomNavItem("编辑", R.drawable.ic_tab_edit_kuromi,      "editor"),
                BottomNavItem("相册", R.drawable.ic_tab_album_kuromi,     "gallery"),
                BottomNavItem("推荐", R.drawable.ic_tab_recommend_kuromi, "lbs"),
                BottomNavItem("我的", R.drawable.ic_tab_profile_kuromi,   "profile")
            )
            items.forEach { item ->
                NavigationBarItem(
                    selected = selectedItem == item.route,
                    onClick  = { onItemSelected(item.route) },
                    icon = {
                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.label,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Color(0xFFEC4899),
                        selectedTextColor   = Color(0xFFEC4899),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888),
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
private data class BottomNavItem(val label: String, val iconRes: Int, val route: String)

data class RecentActivity(
    val description: String,
    val time: String
)

data class PopularPlace(
    val name: String,
    val rating: Int
)

data class HomeUiState(
    val temperature: Int    = 28,
    val weatherDesc: String = "适合外拍",
    val motto: String       = "今天也要拍出好照片哦",
    val recentActivities: List<RecentActivity> = emptyList(),
    val popularPlaces: List<PopularPlace>      = emptyList()
)
