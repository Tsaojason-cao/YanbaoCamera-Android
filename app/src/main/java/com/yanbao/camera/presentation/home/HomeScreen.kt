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
import com.yanbao.camera.R

// ─────────────────────────────────────────────
// 设计规范颜色常量
// ─────────────────────────────────────────────
private val BG_TOP     = Color(0xFFFDF5F7)
private val BG_BOTTOM  = Color(0xFFF9F0F5)
private val PINK_S     = Color(0xFFEC4899)
private val PINK_E     = Color(0xFF9D4EDD)
private val DARK_S     = Color(0xFF2A2A2A)
private val DARK_E     = Color(0xFF1A1A1A)
private val GOLD_S     = Color(0xFFD4A020)
private val GOLD_E     = Color(0xFFB88010)
private val BLUE_S     = Color(0xFF90C0E8)
private val BLUE_E     = Color(0xFF5090C0)
private val NAV_PINK   = Color(0xFFEC4899)
private val NAV_GRAY   = Color(0xFF888888)
private val TEXT_BLACK = Color(0xFF1A1A1A)
private val TEXT_GRAY  = Color(0xFF888888)

@Composable
fun HomeScreen(
    onCameraClick:    () -> Unit = {},
    onEditorClick:    () -> Unit = {},
    onGalleryClick:   () -> Unit = {},
    onRecommendClick: () -> Unit = {},
    onProfileClick:   () -> Unit = {},
    avatarUri:        String?    = null,
    viewModel:        HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BG_TOP, BG_BOTTOM)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ── 顶部：头像行 ──────────────────────────
            // 头像居中
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEDDFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter            = painterResource(R.drawable.avatar_user),
                        contentDescription = "头像",
                        modifier           = Modifier
                            .size(96.dp)
                            .clip(CircleShape),
                        contentScale       = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 顶部：第1行 早安！(左) + 28℃(右) ────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                // 左侧：早安！+ 副文字
                Column {
                    Text(
                        text       = uiState.greeting,
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TEXT_BLACK
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text     = uiState.greetingSub,
                        fontSize = 14.sp,
                        color    = TEXT_GRAY
                    )
                }
                // 右侧：yanbao AI + 温度 + 天气
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = "yanbao AI",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TEXT_GRAY
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text       = "${uiState.temperature}℃",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TEXT_BLACK
                    )
                    Text(
                        text     = uiState.weatherDesc,
                        fontSize = 12.sp,
                        color    = TEXT_GRAY
                    )
                }
            }

            // ── 功能卡片（2×2，高度固定 100.dp）──────
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier            = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title    = "拍照",
                        iconRes  = R.drawable.ic_tab_camera_kuromi,
                        gradient = listOf(PINK_S, PINK_E),
                        onClick  = onCameraClick,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title    = "编辑",
                        iconRes  = R.drawable.ic_tab_edit_kuromi,
                        gradient = listOf(DARK_S, DARK_E),
                        onClick  = onEditorClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title    = "AI推荐",
                        iconRes  = R.drawable.ic_tab_recommend_kuromi_nobg,
                        gradient = listOf(GOLD_S, GOLD_E),
                        onClick  = onRecommendClick,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title    = "相册",
                        iconRes  = R.drawable.ic_tab_album_kuromi,
                        gradient = listOf(BLUE_S, BLUE_E),
                        onClick  = onGalleryClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── 最近活动 ──────────────────────────────
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader(
                title    = "最近活动",
                modifier = Modifier.padding(horizontal = 16.dp),
                onMore   = onGalleryClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding        = PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.recentActivities, key = { it.description }) { activity ->
                    RecentActivityCard(activity)
                }
            }

            // ── 热门地点 ──────────────────────────────
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(
                title    = "热门地点",
                modifier = Modifier.padding(horizontal = 16.dp),
                onMore   = onRecommendClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding        = PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.popularPlaces, key = { it.name }) { place ->
                    PopularPlaceCard(place = place, onClick = onRecommendClick)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────
// 功能卡片：高度固定 100.dp
// ─────────────────────────────────────────────
@Composable
private fun ActionCard(
    title:    String,
    iconRes:  Int,
    gradient: List<Color>,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter            = painterResource(id = iconRes),
                contentDescription = title,
                modifier           = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text       = title,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────
// 区块标题
// ─────────────────────────────────────────────
@Composable
private fun SectionHeader(
    title:    String,
    modifier: Modifier = Modifier,
    onMore:   () -> Unit = {}
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text       = title,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = TEXT_BLACK
        )
        Icon(
            painter            = painterResource(R.drawable.ic_arrow_right_kuromi),
            contentDescription = null,
            tint               = TEXT_GRAY,
            modifier           = Modifier
                .size(20.dp)
                .clickable { onMore() }
        )
    }
}

// ─────────────────────────────────────────────
// 最近活动卡片：宽 200.dp，文字完整显示
// ─────────────────────────────────────────────
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier  = Modifier
            .width(200.dp)
            .height(90.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0ECF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter            = painterResource(R.drawable.ic_tab_home_kuromi),
                        contentDescription = null,
                        modifier           = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text     = activity.description,
                    fontSize = 12.sp,
                    color    = TEXT_BLACK,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text     = activity.time,
                fontSize = 11.sp,
                color    = TEXT_GRAY,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

// ─────────────────────────────────────────────
// 热门地点卡片
// ─────────────────────────────────────────────
@Composable
private fun PopularPlaceCard(place: PopularPlace, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val photoRes = when (place.name) {
                "台北101"  -> R.drawable.place_taipei101
                "台南波场" -> R.drawable.place_tainan
                "北海坑境" -> R.drawable.place_hokkaido
                else       -> R.drawable.place_taipei101
            }
            Image(
                painter            = painterResource(photoRes),
                contentDescription = place.name,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale       = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text       = place.name,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TEXT_BLACK
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    repeat(5) { index ->
                        Text(
                            text  = "*",
                            fontSize = 11.sp,
                            color = if (index < place.rating) Color(0xFFFFD700)
                                    else Color(0xFFE0E0E0)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 底部导航栏：6 等宽，图标 24.dp，文字 10.sp
// ─────────────────────────────────────────────
@Composable
private fun HomeBottomNav(
    onHomeClick:      () -> Unit = {},
    onCameraClick:    () -> Unit,
    onEditorClick:    () -> Unit,
    onGalleryClick:   () -> Unit,
    onRecommendClick: () -> Unit,
    onProfileClick:   () -> Unit,
    modifier:         Modifier = Modifier
) {
    Surface(
        modifier        = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape           = RoundedCornerShape(28.dp),
        color           = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            NavItem(iconRes = R.drawable.ic_tab_home_kuromi,           label = "首页", selected = true,  onClick = onHomeClick)
            NavItem(iconRes = R.drawable.ic_tab_camera_kuromi,         label = "拍照", selected = false, onClick = onCameraClick)
            NavItem(iconRes = R.drawable.ic_tab_edit_kuromi,           label = "编辑", selected = false, onClick = onEditorClick)
            NavItem(iconRes = R.drawable.ic_tab_album_kuromi,          label = "相册", selected = false, onClick = onGalleryClick)
            NavItem(iconRes = R.drawable.ic_tab_recommend_kuromi_nobg, label = "推荐", selected = false, onClick = onRecommendClick)
            NavItem(iconRes = R.drawable.ic_tab_profile_kuromi,        label = "我的", selected = false, onClick = onProfileClick)
        }
    }
}

@Composable
private fun NavItem(
    iconRes:  Int,
    label:    String,
    selected: Boolean,
    onClick:  () -> Unit
) {
    Column(
        modifier            = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter            = painterResource(iconRes),
            contentDescription = label,
            modifier           = Modifier.size(24.dp),
            colorFilter        = if (selected)
                androidx.compose.ui.graphics.ColorFilter.tint(NAV_PINK)
            else null
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text       = label,
            fontSize   = 10.sp,
            color      = if (selected) NAV_PINK else NAV_GRAY,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
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
    val recentActivities: List<RecentActivity> = listOf(
        RecentActivity("你在台北101拍摄了新照片", "1s ago"),
        RecentActivity("你在西门町逛了逛了", "10m ago")
    ),
    val popularPlaces:    List<PopularPlace>   = listOf(
        PopularPlace("台北101", 5),
        PopularPlace("台南波场", 5),
        PopularPlace("北海坑境", 5)
    )
)
