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
// 精确色值（严格对标 home_v6.png）
// ─────────────────────────────────────────────────────────────
private val BG_COLOR      = Color(0xFFF0C8D8)   // 整体背景：粉红
private val BTN_CAMERA    = Color(0xFFD81B7A)   // 拍照按钮：深品牌粉
private val BTN_EDITOR    = Color(0xFF1A1A1A)   // 编辑按钮：近黑
private val BTN_AI        = Color(0xFFB8860B)   // AI推荐：深金
private val BTN_ALBUM     = Color(0xFF5B9BD5)   // 相册：中蓝
private val CARD_BG       = Color(0xFFFFFFFF)   // 活动卡片：白
private val PLACE_CARD_BG = Color(0xFFFFFFFF)   // 地点卡片：白
private val PINK_ACCENT   = Color(0xFFEC4899)   // 选中态品牌粉
private val AVATAR_RING   = Color(0xFF9B59B6)   // 头像紫色描边
private val SECTION_TEXT  = Color(0xFF1A1A1A)   // 章节标题深色
private val STAR_ON       = Color(0xFFFFCC00)   // 亮星
private val STAR_OFF      = Color(0xFFDDDDDD)   // 暗星
private val HEART_PINK    = Color(0xFFEC4899)   // 收藏心形粉
private val NAV_BG        = Color(0xFFF5E6EB)   // 底部导航浅粉

// 热门地点卡片顶部色块
private val PLACE_BLUE    = Color(0xFFADD8F0)   // 台北101：天蓝
private val PLACE_GREEN   = Color(0xFFB0D8B0)   // 台南波场：浅绿
private val PLACE_PEACH   = Color(0xFFF5C8A8)   // 北海坑境：桃粉

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
            .background(BG_COLOR)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            // ── 状态栏区域（9:41 + yanbao AI + 28°C）──
            StatusBar()

            // ── 问候语 + 用户头像 ──
            GreetingSection()

            Spacer(modifier = Modifier.height(24.dp))

            // ── 四功能按钮 ──
            FunctionButtonGrid(
                onCameraClick = onCameraClick,
                onEditorClick = onEditorClick,
                onRecommendClick = onRecommendClick,
                onGalleryClick = onGalleryClick
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── 最近活动 ──
            SectionHeader(title = "最近活动")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.recentActivities) { activity ->
                    RecentActivityCard(activity = activity)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── 热门地点 ──
            SectionHeader(title = "热门地点")
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
// 状态栏（9:41 时间 + yanbao AI + 28°C）
// ─────────────────────────────────────────────────────────────
@Composable
private fun StatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "9:41",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333)
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "yanbao AI",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Text(
                text = "28°C",
                fontSize = 16.sp,
                color = Color(0xFF555555)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 问候语 + 用户头像（居中）
// ─────────────────────────────────────────────────────────────
@Composable
private fun GreetingSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // 问候文字
        Text(
            text = "早安！",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = "适合外拍",
            fontSize = 16.sp,
            color = Color(0xFF555555)
        )
        Text(
            text = "今天也要拍出好照片哦",
            fontSize = 14.sp,
            color = Color(0xFF888888)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 用户头像（居中，紫色描边圆形）
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // 紫色描边
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color.White, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .background(Color(0xFFEED6F5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "用户",
                        fontSize = 18.sp,
                        color = Color(0xFF9B59B6),
                        fontWeight = FontWeight.Medium
                    )
                }
                // 紫色描边环
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color.Transparent, CircleShape)
                ) {
                    // 通过 border 实现描边
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 四功能按钮（2×2 网格，无图标只有文字，大圆角胶囊）
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
            BigButton(
                modifier = Modifier.weight(1f),
                label = "拍照",
                color = BTN_CAMERA,
                onClick = onCameraClick
            )
            BigButton(
                modifier = Modifier.weight(1f),
                label = "编辑",
                color = BTN_EDITOR,
                onClick = onEditorClick
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigButton(
                modifier = Modifier.weight(1f),
                label = "AI推荐",
                color = BTN_AI,
                onClick = onRecommendClick
            )
            BigButton(
                modifier = Modifier.weight(1f),
                label = "相册",
                color = BTN_ALBUM,
                onClick = onGalleryClick
            )
        }
    }
}

@Composable
private fun BigButton(
    modifier: Modifier = Modifier,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(40.dp))   // 大圆角胶囊形
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = SECTION_TEXT
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = "更多",
            tint = Color(0xFF888888),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 最近活动卡片（白色圆角卡片，左侧紫色圆形头像）
// ─────────────────────────────────────────────────────────────
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CARD_BG),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧紫色圆形（库洛米头像占位）
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFD8B4E8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // 空圆形，与 home_v6 一致
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = activity.time,
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 热门地点卡片（白色圆角卡片，顶部色块+心形收藏+星级）
// ─────────────────────────────────────────────────────────────
@Composable
private fun PopularPlaceCard(place: PopularPlace) {
    val topColor = when (place.name) {
        "台北101"  -> PLACE_BLUE
        "台南波场" -> PLACE_GREEN
        "北海坑境" -> PLACE_PEACH
        else       -> Color(0xFFDDCCEE)
    }

    Card(
        modifier = Modifier
            .width(170.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PLACE_CARD_BG),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 顶部色块（含心形收藏按钮）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(topColor)
            ) {
                // 右上角粉色心形收藏
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(HEART_PINK, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_like),
                        contentDescription = "收藏",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            // 底部信息区
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = place.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    repeat(5) { index ->
                        Text(
                            text = "★",
                            fontSize = 16.sp,
                            color = if (index < place.rating) STAR_ON else STAR_OFF
                        )
                    }
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
        modifier = modifier.fillMaxWidth(),
        color = NAV_BG,
        shadowElevation = 8.dp
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
                        modifier = Modifier.size(if (isSelected) 40.dp else 36.dp),
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
                        color = if (isSelected) PINK_ACCENT else Color(0xFF888888)
                    )
                }
            }
        }
    }
}
