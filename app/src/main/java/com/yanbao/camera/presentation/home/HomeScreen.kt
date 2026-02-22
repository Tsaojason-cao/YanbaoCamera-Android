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

// ── 设计稿背景渐变色 ──────────────────────────────
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
                .padding(bottom = 88.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── 顶部区域 ──
            TopBar(uiState = uiState)

            Spacer(modifier = Modifier.height(20.dp))

            // ── 四功能按钮 ──
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title    = "拍照",
                        iconRes  = R.drawable.ic_tab_camera_kuromi,
                        gradient = listOf(Color(0xFFEC4899), Color(0xFF9D4EDD)),
                        onClick  = { navController.navigate("camera") },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title    = "编辑",
                        iconRes  = R.drawable.ic_tab_edit_kuromi,
                        gradient = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A)),
                        onClick  = { navController.navigate("editor") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        title    = "AI推荐",
                        iconRes  = R.drawable.ic_tab_recommend_kuromi,
                        gradient = listOf(Color(0xFFD4A020), Color(0xFFB88010)),
                        onClick  = { navController.navigate("lbs") },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title    = "相册",
                        iconRes  = R.drawable.ic_tab_album_kuromi,
                        gradient = listOf(Color(0xFF90C0E8), Color(0xFF5090C0)),
                        onClick  = { navController.navigate("gallery") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── 最近活动 ──
            SectionHeader(
                title    = "最近活动",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.recentActivities) { activity ->
                    RecentActivityCard(activity)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── 热门地点 ──
            SectionHeader(
                title    = "热门地点",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.popularPlaces) { place ->
                    PopularPlaceCard(
                        place   = place,
                        onClick = { navController.navigate("lbs") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── 底部导航栏 ──
        HomeBottomNavigation(
            selectedItem   = "home",
            onItemSelected = { route ->
                if (route == "home") navController.popBackStack("home", inclusive = false)
                else navController.navigate(route)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 顶部区域
// ─────────────────────────────────────────────────────────────
@Composable
private fun TopBar(uiState: HomeUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // 库洛米耳朵
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter           = painterResource(R.drawable.ic_kuromi_ear_left),
                contentDescription = null,
                modifier          = Modifier.size(40.dp)
            )
            Image(
                painter           = painterResource(R.drawable.ic_kuromi_ear_right),
                contentDescription = null,
                modifier          = Modifier.size(40.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 16.dp, end = 16.dp)
        ) {
            // yanbao AI — 右上角黑色 18sp
            Text(
                text       = "yanbao AI",
                color      = Color.Black,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier   = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 问候 + 头像 + 天气
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // 左：问候语（真实时间逻辑，来自 ViewModel）
                Column {
                    Text(
                        text       = uiState.greeting,
                        fontSize   = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.Black
                    )
                    Text(
                        text     = uiState.greetingSub,
                        fontSize = 14.sp,
                        color    = Color(0xFF444444)
                    )
                }

                // 中：动漫头像 88dp，紫色描边
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9B59B6))
                ) {
                    Image(
                        painter           = painterResource(R.drawable.avatar_anime),
                        contentDescription = "用户头像",
                        modifier          = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        contentScale      = ContentScale.Crop
                    )
                }

                // 右：天气（真实逻辑，温度接 API 后替换）
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text     = "☀️ ${uiState.temperature}°C",
                        fontSize = 18.sp,
                        color    = Color(0xFF222222)
                    )
                    Text(
                        text     = uiState.weatherDesc,
                        fontSize = 13.sp,
                        color    = Color(0xFF555555)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 功能按钮卡片
// 高度 140dp，圆角 24dp，库洛米图标 64dp + 文字 20sp
// ─────────────────────────────────────────────────────────────
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
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter           = painterResource(id = iconRes),
                contentDescription = title,
                modifier          = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text       = title,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 区块标题行
// ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = title,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.Black
        )
        Icon(
            painter           = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint              = Color(0xFF666666),
            modifier          = Modifier.size(20.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 最近活动卡片
// 宽 230dp，高 96dp，库洛米图标 52dp
// ─────────────────────────────────────────────────────────────
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier  = Modifier
            .width(230.dp)
            .height(96.dp)
            .clickable { },
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xBBFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter           = painterResource(R.drawable.ic_tab_home_kuromi),
                contentDescription = null,
                modifier          = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text     = activity.time,
                        fontSize = 11.sp,
                        color    = Color(0xFF888888)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text     = activity.description,
                    fontSize = 14.sp,
                    color    = Color(0xFF222222),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 热门地点卡片
// 宽 170dp，图片区 120dp，圆角 18dp，库洛米贴纸 48dp
// ─────────────────────────────────────────────────────────────
@Composable
private fun PopularPlaceCard(place: PopularPlace, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .width(170.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val photoRes = when (place.name) {
                    "台北101"  -> R.drawable.place_taipei101
                    "台南波场" -> R.drawable.place_tainan
                    "北海坑境" -> R.drawable.place_hokkaido
                    else       -> R.drawable.place_taipei101
                }
                Image(
                    painter           = painterResource(photoRes),
                    contentDescription = place.name,
                    modifier          = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                    contentScale      = ContentScale.Crop
                )
                // 库洛米贴纸（右下角）
                val kuromiRes = when (place.name) {
                    "台北101"  -> R.drawable.ic_tab_home_kuromi
                    "台南波场" -> R.drawable.ic_tab_camera_kuromi
                    else       -> R.drawable.ic_tab_recommend_kuromi
                }
                Image(
                    painter           = painterResource(kuromiRes),
                    contentDescription = null,
                    modifier          = Modifier
                        .size(48.dp)
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text       = place.name,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 五颗星：金黄 #FFD700 / 灰色 #E0E0E0
                    Row {
                        repeat(5) { index ->
                            Icon(
                                painter           = painterResource(R.drawable.ic_star_filled),
                                contentDescription = null,
                                tint              = if (index < place.rating) Color(0xFFFFD700) else Color(0xFFE0E0E0),
                                modifier          = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(text = "📍", fontSize = 14.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 底部导航栏
// 高度 72dp，图标 28dp，文字 11sp，选中色 #EC4899
// ─────────────────────────────────────────────────────────────
@Composable
fun HomeBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier        = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape           = RoundedCornerShape(40.dp),
        color           = Color(0xFFFAE8F0),
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier       = Modifier.height(72.dp)
        ) {
            val items = listOf(
                Triple("首页",  R.drawable.ic_tab_home_kuromi,      "home"),
                Triple("拍照",  R.drawable.ic_tab_camera_kuromi,    "camera"),
                Triple("编辑",  R.drawable.ic_tab_edit_kuromi,      "editor"),
                Triple("相册",  R.drawable.ic_tab_album_kuromi,     "gallery"),
                Triple("推荐",  R.drawable.ic_tab_recommend_kuromi, "lbs"),
                Triple("我的",  R.drawable.ic_tab_profile_kuromi,   "profile")
            )
            items.forEach { (label, iconRes, route) ->
                NavigationBarItem(
                    selected = selectedItem == route,
                    onClick  = { onItemSelected(route) },
                    icon = {
                        Image(
                            painter           = painterResource(iconRes),
                            contentDescription = label,
                            modifier          = Modifier.size(28.dp)
                        )
                    },
                    label = {
                        Text(
                            text     = label,
                            fontSize = 11.sp,
                            color    = if (selectedItem == route) Color(0xFFEC4899) else Color(0xFF888888)
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

// ─────────────────────────────────────────────────────────────
// 数据类
// ─────────────────────────────────────────────────────────────
data class RecentActivity(val description: String, val time: String)

data class PopularPlace(val name: String, val rating: Int)

data class HomeUiState(
    val greeting:         String             = "早安！",
    val greetingSub:      String             = "今天也要拍出好照片哦",
    val temperature:      Int                = 28,
    val weatherDesc:      String             = "适合外拍",
    val motto:            String             = "今天也要拍出好照片哦",
    val recentActivities: List<RecentActivity> = emptyList(),
    val popularPlaces:    List<PopularPlace>   = emptyList()
)
