package com.yanbao.camera.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.YanbaoTokens

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
                .padding(horizontal = 16.dp)
        ) {
            // 顶部品牌和问候区
            TopBar(
                uiState = uiState,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 拍照/编辑卡片
            ActionCards(
                onCameraClick = { navController.navigate("camera") },
                onEditClick = { navController.navigate("editor") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // AI推荐 + 相册双标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI推荐",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "相册",
                        fontSize = 16.sp,
                        color = Color(0xFF666666)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 最近活动横向列表
            RecentActivitiesRow(activities = uiState.recentActivities)

            Spacer(modifier = Modifier.height(24.dp))

            // 热门地点标题
            Text(
                text = "热门地点",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 热门地点横向列表
            PopularPlacesRow(places = uiState.popularPlaces)

            Spacer(modifier = Modifier.weight(1f))
        }

        // 底部导航栏（六个按钮）
        HomeBottomNavigation(
            selectedItem = "home",
            onItemSelected = { route ->
                when (route) {
                    "home" -> navController.popBackStack("home", inclusive = false)
                    else -> navController.navigate(route)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TopBar(
    uiState: HomeUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 库洛米耳朵装饰
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_kuromi_ear_left),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ic_kuromi_ear_right),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // 品牌标识
            Text(
                text = "yanbao AI",
                color = Color.Black,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.End)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "早安！",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = uiState.weatherDesc,
                        fontSize = 16.sp,
                        color = Color(0xFF888888)
                    )
                }
                Text(
                    text = "${uiState.temperature}℃",
                    fontSize = 18.sp,
                    color = Color(0xFF666666)
                )
            }
            Text(
                text = uiState.motto,
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA)
            )
        }
    }
}

@Composable
private fun ActionCards(
    onCameraClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionCard(
            title = "拍照",
            iconRes = R.drawable.ic_camera,
            gradient = listOf(Color(0xFFEC4899), Color(0xFF9D4EDD)),
            onClick = onCameraClick,
            modifier = Modifier.weight(1f)
        )
        ActionCard(
            title = "编辑",
            iconRes = R.drawable.ic_edit,
            gradient = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A)),
            onClick = onEditClick,
            modifier = Modifier.weight(1f)
        )
    }
}

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
            .height(100.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun RecentActivitiesRow(activities: List<RecentActivity>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(activities) { activity ->
            RecentActivityCard(activity)
        }
    }
}

@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
            .clickable { /* 跳转到活动详情 */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_tab_home_kuromi),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PopularPlacesRow(places: List<PopularPlace>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(places) { place ->
            PopularPlaceCard(place)
        }
    }
}

@Composable
private fun PopularPlaceCard(place: PopularPlace) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { /* 跳转到地点详情 */ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
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
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                val kuromiRes = when (place.name) {
                    "台北101"  -> R.drawable.ic_tab_home_kuromi
                    "台南波场" -> R.drawable.ic_tab_camera_kuromi
                    else       -> R.drawable.ic_tab_recommend_kuromi
                }
                Image(
                    painter = painterResource(id = kuromiRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = place.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star_filled),
                            contentDescription = null,
                            tint = if (index < place.rating) Color(0xFFFFD700) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Color(0xFFFAE8F0),
        tonalElevation = 0.dp,
        modifier = modifier.height(60.dp)
    ) {
        val items = listOf(
            BottomNavItem("首页", R.drawable.ic_tab_home_kuromi, "home"),
            BottomNavItem("拍照", R.drawable.ic_tab_camera_kuromi, "camera"),
            BottomNavItem("编辑", R.drawable.ic_tab_edit_kuromi, "editor"),
            BottomNavItem("相册", R.drawable.ic_tab_album_kuromi, "gallery"),
            BottomNavItem("推荐", R.drawable.ic_tab_recommend_kuromi, "lbs"),
            BottomNavItem("我的", R.drawable.ic_tab_profile_kuromi, "profile")
        )
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item.route,
                onClick = { onItemSelected(item.route) },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFEC4899),
                    selectedTextColor = Color(0xFFEC4899),
                    unselectedIconColor = Color(0xFF888888),
                    unselectedTextColor = Color(0xFF888888),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

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
    val temperature: Int = 28,
    val weatherDesc: String = "适合外拍",
    val motto: String = "今天也要拍出好照片哦",
    val recentActivities: List<RecentActivity> = emptyList(),
    val popularPlaces: List<PopularPlace> = emptyList()
)
