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
                .padding(bottom = 88.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── 顶部：yanbao AI + 天气 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Spacer(modifier = Modifier.width(1.dp)) // 左侧占位
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = "yanbao AI",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.Black
                    )
                    Text(
                        text     = "${uiState.temperature}°C",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color    = Color(0xFF333333)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 问候语 + 副标题 ──
            Text(
                text       = uiState.greeting,
                fontSize   = 40.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.Black
            )
            Text(
                text     = uiState.weatherDesc,
                fontSize = 16.sp,
                color    = Color(0xFF555555)
            )
            Text(
                text     = uiState.greetingSub,
                fontSize = 16.sp,
                color    = Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── 拍照 / 编辑 两个大按钮 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 拍照按钮（粉紫渐变，文字左 + 图标右）
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFEC4899), Color(0xFF9D4EDD))
                            )
                        )
                        .clickable { navController.navigate("camera") },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = "拍照",
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                        Image(
                            painter           = painterResource(R.drawable.ic_tab_camera_kuromi),
                            contentDescription = "拍照",
                            modifier          = Modifier.size(56.dp)
                        )
                    }
                }
                // 编辑按钮（深黑，文字左 + 图标右）
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF1A1A1A))
                        .clickable { navController.navigate("editor") },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = "编辑",
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                        Image(
                            painter           = painterResource(R.drawable.ic_tab_edit_kuromi),
                            contentDescription = "编辑",
                            modifier          = Modifier.size(56.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── AI推荐 标题 + 最近活动卡片 ──
            SectionHeader(title = "AI推荐") { navController.navigate("lbs") }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding        = PaddingValues(end = 4.dp)
            ) {
                items(uiState.recentActivities) { activity ->
                    RecentActivityCard(activity)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── 热门地点 ──
            SectionHeader(title = "热门地点") { navController.navigate("lbs") }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding        = PaddingValues(end = 4.dp)
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

// ─────────────────────────────────────────────
// 区块标题行（标题 + 箭头）
// ─────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, onMore: () -> Unit = {}) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = title,
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.Black
        )
        Icon(
            painter           = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint              = Color(0xFF666666),
            modifier          = Modifier
                .size(22.dp)
                .clickable { onMore() }
        )
    }
}

// ─────────────────────────────────────────────
// 最近活动卡片（白底半透明，库洛米图标，时间戳）
// ─────────────────────────────────────────────
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier  = Modifier
            .width(240.dp)
            .wrapContentHeight()
            .clickable { },
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xCCFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 库洛米图标（灰色占位框样式，对标截图）
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8E8E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter           = painterResource(R.drawable.ic_tab_home_kuromi),
                        contentDescription = null,
                        modifier          = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text     = activity.description,
                    fontSize = 15.sp,
                    color    = Color(0xFF222222),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text     = "Time ${activity.time}",
                fontSize = 13.sp,
                color    = Color(0xFF999999),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// ─────────────────────────────────────────────
// 热门地点卡片（真实景色图 + 库洛米贴纸 + 五星）
// ─────────────────────────────────────────────
@Composable
private fun PopularPlaceCard(place: PopularPlace, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
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
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale      = ContentScale.Crop
                )
                val kuromiRes = when (place.name) {
                    "台北101"  -> R.drawable.ic_tab_home_kuromi
                    "台南波场" -> R.drawable.ic_tab_camera_kuromi
                    else       -> R.drawable.ic_tab_recommend_kuromi
                }
                Image(
                    painter           = painterResource(kuromiRes),
                    contentDescription = null,
                    modifier          = Modifier
                        .size(44.dp)
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

// ─────────────────────────────────────────────
// 底部导航栏（浅粉胶囊，6个库洛米图标）
// ─────────────────────────────────────────────
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

// ─────────────────────────────────────────────
// 数据类
// ─────────────────────────────────────────────
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
