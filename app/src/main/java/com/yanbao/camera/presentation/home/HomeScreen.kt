package com.yanbao.camera.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

// --- 设计稿背景渐变色 ---
private val BG_GRADIENT = Brush.verticalGradient(
    colors = listOf(Color(0xFFFDF5F7), Color(0xFFF9F0F5))
)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG_GRADIENT)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- 顶部布局 ---
            TopBar(uiState)

            // --- 功能区 ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- 拍照/编辑卡片 ---
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title = "拍照",
                        iconRes = R.drawable.ic_tab_camera_kuromi, // 假设图标为白色
                        gradient = listOf(Color(0xFFEC4899), Color(0xFF9D4EDD)),
                        onClick = { navController.navigate("camera") },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "编辑",
                        iconRes = R.drawable.ic_tab_edit_kuromi, // 假设图标为白色
                        gradient = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A)),
                        onClick = { navController.navigate("editor") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- AI推荐/相册行 ---
                SectionHeader(title = "AI推荐", subtitle = "相册") { navController.navigate("gallery") }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- 最近活动 ---
            SectionHeader(title = "最近活动", modifier = Modifier.padding(horizontal = 16.dp)) { navController.navigate("lbs") }
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.recentActivities) { activity ->
                    RecentActivityCard(activity)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 热门地点 ---
            SectionHeader(title = "热门地点", modifier = Modifier.padding(horizontal = 16.dp)) { navController.navigate("lbs") }
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.popularPlaces) { place ->
                    PopularPlaceCard(place) { navController.navigate("lbs") }
                }
            }

            Spacer(modifier = Modifier.height(120.dp)) // 底部导航栏的间距
        }

        // --- 底部导航栏 ---
        HomeBottomNavigation(
            selectedItem = "home",
            onItemSelected = { route ->
                if (route != "home") navController.navigate(route)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun TopBar(uiState: HomeUiState) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        // 第一行：早安, yanbao AI, 28℃
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = uiState.greeting,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "yanbao AI",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${uiState.temperature}℃",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 第二行：问候语, 适合外拍
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.greetingSub,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = uiState.weatherDesc,
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
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
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier, onMore: () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = if (subtitle != null) 18.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.clickable { onMore() }, verticalAlignment = Alignment.CenterVertically) {
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = "More",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = activity.description,
                fontSize = 14.sp,
                color = Color.Black,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = activity.time,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun PopularPlaceCard(place: PopularPlace, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(place.imageRes),
                contentDescription = place.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = place.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    repeat(5) { index ->
                        Text(
                            text = "★",
                            fontSize = 12.sp,
                            color = if (index < place.rating) Color(0xFFFFD700) else Color(0xFFE0E0E0)
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
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp).clip(RoundedCornerShape(32.dp)),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("首页", R.drawable.ic_tab_home_kuromi, "home"),
            Triple("拍照", R.drawable.ic_tab_camera_kuromi, "camera"),
            Triple("编辑", R.drawable.ic_tab_edit_kuromi, "editor"),
            Triple("相册", R.drawable.ic_tab_album_kuromi, "gallery"),
            Triple("推荐", R.drawable.ic_tab_recommend_kuromi, "lbs"),
            Triple("我的", R.drawable.ic_tab_profile_kuromi, "profile")
        )
        items.forEach { (label, iconRes, route) ->
            val isSelected = selectedItem == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(route) },
                icon = {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFEC4899),
                    unselectedIconColor = Color(0xFF888888),
                    selectedTextColor = Color(0xFFEC4899),
                    unselectedTextColor = Color(0xFF888888),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
