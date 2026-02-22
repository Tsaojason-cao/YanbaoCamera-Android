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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.yanbao.camera.R

// ─────────────────────────────────────────────────────────────
// 精确色值（严格对标 home_ui_design.png）
// ─────────────────────────────────────────────────────────────
private val BG_TOP       = Color(0xFF9B7FD4)   // 背景顶部：中紫
private val BG_MID       = Color(0xFFB88FC0)   // 背景中部：粉紫
private val BG_BOT       = Color(0xFFF0A0C0)   // 背景底部：粉红
private val BTN_CAM_1    = Color(0xFFE8A0CC)   // 拍照渐变起点（浅粉紫）
private val BTN_CAM_2    = Color(0xFFD060A0)   // 拍照渐变终点（深粉）
private val BTN_EDIT_1   = Color(0xFF2A2A2A)   // 编辑深炭黑
private val BTN_EDIT_2   = Color(0xFF1A1A1A)
private val BTN_AI_1     = Color(0xFFD4A020)   // AI推荐金色
private val BTN_AI_2     = Color(0xFFB88010)
private val BTN_ALB_1    = Color(0xFF90C0E8)   // 相册天蓝
private val BTN_ALB_2    = Color(0xFF5090C0)
private val NAV_BG       = Color(0xFFFAE8F0)   // 底部导航浅粉
private val CARD_BG      = Color(0xFFF8F0F5)   // 活动卡片背景（浅粉白）
private val PINK_ACCENT  = Color(0xFFEC4899)   // 品牌粉（选中态）
private val TEXT_DARK    = Color(0xFF1A1A1A)   // 深色文字
private val TEXT_GRAY    = Color(0xFF888888)   // 灰色文字
private val STAR_ON      = Color(0xFFFFCC00)   // 亮星
private val STAR_OFF     = Color(0xFFDDDDDD)   // 暗星
private val PIN_PINK     = Color(0xFFFF6699)   // 定位图标粉

// ─────────────────────────────────────────────────────────────
// 首页主入口
// ─────────────────────────────────────────────────────────────
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(BG_TOP, BG_MID, BG_BOT))
            )
    ) {
        // 散景光点装饰层
        BokehDecoration()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // ── 状态栏 ──
            TopStatusBar()

            // ── 问候区（早安！+ 头像 + 天气）──
            GreetingRow()

            Spacer(modifier = Modifier.height(20.dp))

            // ── 四功能按钮 ──
            FunctionButtonGrid(
                onCameraClick = onCameraClick,
                onEditorClick = onEditorClick,
                onRecommendClick = onRecommendClick,
                onGalleryClick = onGalleryClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── 最近活动 ──
            SectionHeader(title = "最近活动 ⭐")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.recentActivities) { activity ->
                    RecentActivityCard(activity = activity)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 热门地点 ──
            SectionHeader(title = "热门地点 🔥")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.popularPlaces) { place ->
                    PopularPlaceCard(place = place)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── 底部6标签导航 ──
        HomeBottomNavigation(
            selectedItem = "home",
            onItemSelected = { route ->
                when (route) {
                    "camera"  -> onCameraClick()
                    "editor"  -> onEditorClick()
                    "gallery" -> onGalleryClick()
                    "lbs"     -> onRecommendClick()
                    "profile" -> onProfileClick()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 散景光点装饰（模拟设计稿中的柔和光晕）
// ─────────────────────────────────────────────────────────────
@Composable
private fun BokehDecoration() {
    Box(modifier = Modifier.fillMaxSize()) {
        // 左上角光晕
        Box(
            modifier = Modifier
                .offset((-20).dp, 100.dp)
                .size(120.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x40FFFFFF), Color(0x00FFFFFF))
                    ),
                    CircleShape
                )
        )
        // 右侧光晕
        Box(
            modifier = Modifier
                .offset(300.dp, 300.dp)
                .size(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x30FFFFFF), Color(0x00FFFFFF))
                    ),
                    CircleShape
                )
        )
        // 左下光晕
        Box(
            modifier = Modifier
                .offset((-10).dp, 700.dp)
                .size(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x25FFFFFF), Color(0x00FFFFFF))
                    ),
                    CircleShape
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 状态栏（9:41 + 信号图标）
// ─────────────────────────────────────────────────────────────
@Composable
private fun TopStatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "9:41",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("▪▪▪", fontSize = 14.sp, color = Color.White)
            Text("▲", fontSize = 14.sp, color = Color.White)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 问候区（早安！💝 + 动漫头像 + ☀️ 28°C）
// ─────────────────────────────────────────────────────────────
@Composable
private fun GreetingRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：问候语
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "早安！💝",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "今天也要拍出好照片哦",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        // 中间：动漫头像（紫色描边圆形）
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(8.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // 紫色描边
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF9B59B6), CircleShape)
            )
            // 头像图片
            Image(
                painter = painterResource(id = R.drawable.avatar_anime),
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 右侧：天气
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "☀️ 28°C",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "适合外拍",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 四功能按钮（2×2 网格，含图标）
// ─────────────────────────────────────────────────────────────
@Composable
private fun FunctionButtonGrid(
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FunctionButton(
                modifier = Modifier.weight(1f),
                label = "拍照",
                iconRes = R.drawable.ic_camera,
                gradient = listOf(BTN_CAM_1, BTN_CAM_2),
                onClick = onCameraClick
            )
            FunctionButton(
                modifier = Modifier.weight(1f),
                label = "编辑",
                iconRes = R.drawable.ic_edit,
                gradient = listOf(BTN_EDIT_1, BTN_EDIT_2),
                onClick = onEditorClick
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FunctionButton(
                modifier = Modifier.weight(1f),
                label = "AI推荐",
                iconRes = R.drawable.ic_camera_kuromi,
                gradient = listOf(BTN_AI_1, BTN_AI_2),
                onClick = onRecommendClick
            )
            FunctionButton(
                modifier = Modifier.weight(1f),
                label = "相册",
                iconRes = R.drawable.ic_album_kuromi,
                gradient = listOf(BTN_ALB_1, BTN_ALB_2),
                onClick = onGalleryClick
            )
        }
    }
}

@Composable
private fun FunctionButton(
    modifier: Modifier = Modifier,
    label: String,
    iconRes: Int,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 章节标题（含右箭头）
// ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = "更多",
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 最近活动卡片（库洛米图标 + 描述 + 时间）
// ─────────────────────────────────────────────────────────────
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BG),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 库洛米图标
            Image(
                painter = painterResource(id = R.drawable.ic_tab_home_kuromi),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                // 时间（右对齐）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "🕐 ${activity.time}",
                        fontSize = 11.sp,
                        color = TEXT_GRAY
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activity.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TEXT_DARK,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 热门地点卡片（真实景色图 + 库洛米贴纸 + 星级 + 定位图标）
// ─────────────────────────────────────────────────────────────
@Composable
private fun PopularPlaceCard(place: PopularPlace) {
    val (photoRes, kuromiRes) = when (place.name) {
        "台北101"  -> Pair(R.drawable.place_taipei101, R.drawable.ic_tab_home_kuromi)
        "台南波场" -> Pair(R.drawable.place_tainan,    R.drawable.ic_tab_camera_kuromi)
        "北海坑境" -> Pair(R.drawable.place_hokkaido,  R.drawable.ic_tab_recommend_kuromi)
        else       -> Pair(R.drawable.place_taipei101, R.drawable.ic_tab_home_kuromi)
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                // 真实景色图
                Image(
                    painter = painterResource(id = photoRes),
                    contentDescription = place.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // 库洛米贴纸（右下角）
                Image(
                    painter = painterResource(id = kuromiRes),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(52.dp),
                    contentScale = ContentScale.Fit
                )
            }
            // 地点信息
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = place.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_DARK
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 星级
                    repeat(5) { index ->
                        Text(
                            text = "★",
                            fontSize = 16.sp,
                            color = if (index < place.rating) STAR_ON else STAR_OFF
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    // 定位图标
                    Text(
                        text = "📍",
                        fontSize = 16.sp,
                        color = PIN_PINK
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 底部6标签导航栏（库洛米主题图标，浅粉背景）
// ─────────────────────────────────────────────────────────────
@Composable
fun HomeBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        Triple("首页",  R.drawable.ic_tab_home_kuromi,      "home"),
        Triple("拍照",  R.drawable.ic_tab_camera_kuromi,    "camera"),
        Triple("编辑",  R.drawable.ic_tab_edit_kuromi,      "editor"),
        Triple("相册",  R.drawable.ic_tab_album_kuromi,     "gallery"),
        Triple("推荐",  R.drawable.ic_tab_recommend_kuromi, "lbs"),
        Triple("我的",  R.drawable.ic_tab_profile_kuromi,   "profile")
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(32.dp),
        color = NAV_BG,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (label, iconRes, route) ->
                val isSelected = selectedItem == route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemSelected(route) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(if (isSelected) 44.dp else 38.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = if (!isSelected)
                            ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        else null
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) PINK_ACCENT else TEXT_GRAY
                    )
                }
            }
        }
    }
}
