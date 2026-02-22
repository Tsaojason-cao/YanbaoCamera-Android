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
// 设计规范常量（严格遵循设计稿）
// ─────────────────────────────────────────────
private val BG_START   = Color(0xFFFDF5F7)   // 极简渐变顶色
private val BG_END     = Color(0xFFF9F0F5)   // 极简渐变底色
private val PINK_START = Color(0xFFEC4899)   // 拍照卡片渐变起
private val PINK_END   = Color(0xFF9D4EDD)   // 拍照卡片渐变终
private val DARK_START = Color(0xFF2A2A2A)   // 编辑卡片渐变起
private val DARK_END   = Color(0xFF1A1A1A)   // 编辑卡片渐变终
private val GOLD_START = Color(0xFFD4A020)   // AI推荐卡片渐变起
private val GOLD_END   = Color(0xFFB88010)   // AI推荐卡片渐变终
private val BLUE_START = Color(0xFF90C0E8)   // 相册卡片渐变起
private val BLUE_END   = Color(0xFF5090C0)   // 相册卡片渐变终
private val NAV_PINK   = Color(0xFFEC4899)   // 导航选中色
private val NAV_GRAY   = Color(0xFF888888)   // 导航未选中色
private val TEXT_BLACK = Color(0xFF1A1A1A)   // 主文字色
private val TEXT_GRAY  = Color(0xFF888888)   // 副文字色

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
            // 干净极简渐变背景，无散景光点
            .background(Brush.verticalGradient(listOf(BG_START, BG_END)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)   // 为底部导航留空
        ) {

            // ── 顶部区域 ──────────────────────────────
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 左：头像
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDCCEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.avatar_user),
                        contentDescription = "头像",
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // 右：品牌 + 天气
                Column(horizontalAlignment = Alignment.End) {
                    // 品牌标识（核心，不可遮挡）
                    Text(
                        text = "yanbao AI",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TEXT_GRAY,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_flash),
                            contentDescription = null,
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${uiState.temperature}℃",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TEXT_BLACK
                        )
                    }
                    Text(
                        text = uiState.weatherDesc,
                        fontSize = 12.sp,
                        color = TEXT_GRAY
                    )
                }
            }

            // ── 问候语 ────────────────────────────────
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = uiState.greeting,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_BLACK
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.greetingSub,
                    fontSize = 14.sp,
                    color = TEXT_GRAY
                )
            }

            // ── 功能卡片（2x2，严格 100.dp 高）────────
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title    = "拍照",
                        iconRes  = R.drawable.ic_tab_camera_kuromi,
                        gradient = listOf(PINK_START, PINK_END),
                        onClick  = onCameraClick,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title    = "编辑",
                        iconRes  = R.drawable.ic_tab_edit_kuromi,
                        gradient = listOf(DARK_START, DARK_END),
                        onClick  = onEditorClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title    = "AI推荐",
                        iconRes  = R.drawable.ic_tab_recommend_kuromi_nobg,
                        gradient = listOf(GOLD_START, GOLD_END),
                        onClick  = onRecommendClick,
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title    = "相册",
                        iconRes  = R.drawable.ic_tab_album_kuromi,
                        gradient = listOf(BLUE_START, BLUE_END),
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
                onMore   = onRecommendClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.recentActivities, key = { it.description }) { activity ->
                    RecentActivityCard(activity)
                }
            }

            // ── 热门地点 ──────────────────────────────
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader(
                title    = "热门地点",
                modifier = Modifier.padding(horizontal = 16.dp),
                onMore   = onRecommendClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(uiState.popularPlaces, key = { it.name }) { place ->
                    PopularPlaceCard(place = place, onClick = onRecommendClick)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 底部导航栏（悬浮在内容之上）────────────
        HomeBottomNav(
            onCameraClick    = onCameraClick,
            onEditorClick    = onEditorClick,
            onGalleryClick   = onGalleryClick,
            onRecommendClick = onRecommendClick,
            onProfileClick   = onProfileClick,
            modifier         = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────
// 功能卡片：高度固定 100.dp，圆角 20.dp
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
            .height(100.dp)                              // ← 严格固定 100.dp
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter           = painterResource(id = iconRes),
                contentDescription = title,
                modifier          = Modifier.size(40.dp)
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
// 区块标题行
// ─────────────────────────────────────────────
@Composable
private fun SectionHeader(
    title:    String,
    modifier: Modifier = Modifier,
    onMore:   () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = title,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = TEXT_BLACK
        )
        Icon(
            painter           = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint              = TEXT_GRAY,
            modifier          = Modifier
                .size(20.dp)
                .clickable { onMore() }
        )
    }
}

// ─────────────────────────────────────────────
// 最近活动卡片
// ─────────────────────────────────────────────
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier = Modifier.width(180.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0ECF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter           = painterResource(R.drawable.ic_tab_home_kuromi),
                        contentDescription = null,
                        modifier          = Modifier.size(32.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
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
                painter           = painterResource(photoRes),
                contentDescription = place.name,
                modifier          = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale      = ContentScale.Crop
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
                            text     = "★",
                            fontSize = 11.sp,
                            color    = if (index < place.rating) Color(0xFFFFD700)
                                       else Color(0xFFDDDDDD)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 底部导航栏：6 等宽标签，图标/文字缩小 20%
// ─────────────────────────────────────────────
@Composable
private fun HomeBottomNav(
    onCameraClick:    () -> Unit,
    onEditorClick:    () -> Unit,
    onGalleryClick:   () -> Unit,
    onRecommendClick: () -> Unit,
    onProfileClick:   () -> Unit,
    modifier:         Modifier = Modifier
) {
    Surface(
        modifier  = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(28.dp),
        color     = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // 首页（选中态）
            NavItem(iconRes = R.drawable.ic_tab_home_kuromi,     label = "首页",  selected = true,  onClick = {})
            NavItem(iconRes = R.drawable.ic_tab_camera_kuromi,   label = "拍照",  selected = false, onClick = onCameraClick)
            NavItem(iconRes = R.drawable.ic_tab_edit_kuromi,     label = "编辑",  selected = false, onClick = onEditorClick)
            NavItem(iconRes = R.drawable.ic_tab_album_kuromi,    label = "相册",  selected = false, onClick = onGalleryClick)
            NavItem(iconRes = R.drawable.ic_tab_recommend_kuromi_nobg, label = "推荐", selected = false, onClick = onRecommendClick)
            NavItem(iconRes = R.drawable.ic_tab_profile_kuromi,  label = "我的",  selected = false, onClick = onProfileClick)
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
    val tint = if (selected) NAV_PINK else NAV_GRAY
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter           = painterResource(iconRes),
            contentDescription = label,
            modifier          = Modifier.size(22.dp),   // 原 28.dp × 0.8 ≈ 22.dp
            colorFilter       = if (selected)
                androidx.compose.ui.graphics.ColorFilter.tint(NAV_PINK)
            else null
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text     = label,
            fontSize = 9.sp,                            // 原 11.sp × 0.8 ≈ 9.sp
            color    = tint,
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
    val recentActivities: List<RecentActivity> = emptyList(),
    val popularPlaces:    List<PopularPlace>   = emptyList()
)
