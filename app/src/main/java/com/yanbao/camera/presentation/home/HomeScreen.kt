package com.yanbao.camera.presentation.home

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanbao.camera.R

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        // 顶部品牌和问候区
        TopBar(uiState = uiState)

        Spacer(modifier = Modifier.height(24.dp))

        // 拍照/编辑卡片
        ActionCards(
            onCameraClick = onCameraClick,
            onEditClick = onEditorClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // AI推荐 + 最近活动横向列表
        SectionHeader(title = "AI推荐")
        RecentActivitiesRow(activities = uiState.recentActivities)

        Spacer(modifier = Modifier.height(24.dp))

        // 热门地点 + 卡片列表
        SectionHeader(title = "热门地点")
        PopularPlacesRow(places = uiState.popularPlaces)

        Spacer(modifier = Modifier.weight(1f))
    }
}

// 顶部品牌和问候区
@Composable
private fun TopBar(uiState: HomeUiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
            Text(
                text = "早安！",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "${uiState.temperature}℃",
                fontSize = 18.sp,
                color = Color(0xFF666666)
            )
        }
        Text(
            text = uiState.weatherDesc,
            fontSize = 16.sp,
            color = Color(0xFF888888)
        )
        Text(
            text = uiState.motto,
            fontSize = 14.sp,
            color = Color(0xFFAAAAAA)
        )
    }
}

// 拍照/编辑卡片
@Composable
private fun ActionCards(
    onCameraClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 拍照卡片
        ActionCard(
            title = "拍照",
            iconRes = R.drawable.ic_camera,
            gradient = listOf(Color(0xFFEC4899), Color(0xFF9D4EDD)),
            onClick = onCameraClick,
            modifier = Modifier.weight(1f)
        )
        // 编辑卡片
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

// 章节标题（带更多箭头）
@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = "更多",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

// 最近活动横向列表
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
            .clickable { /* 后续实现活动详情 */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 缩略图占位
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = activity.description,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = activity.time,
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

// 热门地点横向列表
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
            .clickable { /* 后续实现地点详情 */ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // 图片占位（实际应加载图片）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFDDDDDD))
            )
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
