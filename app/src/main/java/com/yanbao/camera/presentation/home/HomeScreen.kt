package com.yanbao.camera.presentation.home

import android.net.Uri
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
import com.yanbao.camera.R

// --- 设计稿背景渐变色 ---
private val BG_TOP = Color(0xFFAF96E3)
private val BG_MID = Color(0xFFC69ECE)
private val BG_BOT = Color(0xFFF3A0BE)

@Composable
fun HomeScreen(
    onCameraClick: () -> Unit = {},
    onEditorClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onRecommendClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    avatarUri: Uri? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(BG_TOP, BG_MID, BG_BOT)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- 顶部：头像 + 右侧天气信息 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 左侧：头像
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
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "☀", fontSize = 18.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${uiState.temperature}℃",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = uiState.weatherDesc,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 问候语 ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = uiState.greeting,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.greetingSub,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 四功能按钮 (2x2) ---
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title = "拍照",
                        iconRes = R.drawable.ic_tab_camera_kuromi,
                        gradient = listOf(Color(0xFFEC4899), Color(0xFF9D4EDD)),
                        onClick = onCameraClick,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "编辑",
                        iconRes = R.drawable.ic_tab_edit_kuromi,
                        gradient = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A)),
                        onClick = onEditorClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title = "AI推荐",
                        iconRes = R.drawable.ic_tab_recommend_kuromi_nobg,
                        gradient = listOf(Color(0xFFD4A020), Color(0xFFB88010)),
                        onClick = onRecommendClick,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "相册",
                        iconRes = R.drawable.ic_tab_album_kuromi,
                        gradient = listOf(Color(0xFF90C0E8), Color(0xFF5090C0)),
                        onClick = onGalleryClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 最近活动 ---
            SectionHeader(
                title = "最近活动",
                modifier = Modifier.padding(horizontal = 16.dp),
                onMore = onRecommendClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.recentActivities) { activity ->
                    RecentActivityCard(activity)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 热门地点 ---
            SectionHeader(
                title = "热门地点",
                modifier = Modifier.padding(horizontal = 16.dp),
                onMore = onRecommendClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.popularPlaces) { place ->
                    PopularPlaceCard(place = place, onClick = onRecommendClick)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- 功能按钮卡片 ---
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

// --- 区块标题行 ---
@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onMore: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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

// --- 最近活动卡片 ---
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier = Modifier
            .width(340.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCCFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    color = Color(0xFF333333),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = activity.time,
                fontSize = 20.sp,
                color = Color(0xFF888888),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// --- 热门地点卡片 ---
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
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    repeat(5) { index ->
                        Text(
                            text = "★",
                            fontSize = 22.sp,
                            color = if (index < place.rating) Color(0xFFFFD700) else Color(0xFFE0E0E0)
                        )
                    }
                }
            }
        }
    }
}

// --- 数据类 ---
data class RecentActivity(val description: String, val time: String)
data class PopularPlace(val name: String, val rating: Int)

data class HomeUiState(
    val greeting:         String               = "早安！",
    val greetingSub:      String               = "今天也要拍出好照片哦",
    val temperature:      Int                  = 28,
    val weatherDesc:      String               = "适合外拍",
    val recentActivities: List<RecentActivity> = emptyList(),
    val popularPlaces:    List<PopularPlace>   = emptyList()
)
