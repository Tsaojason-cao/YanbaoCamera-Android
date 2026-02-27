package com.yanbao.camera.presentation.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.R

// ─────────────────────────────────────────────
// 设计规范颜色常量（严格对标设计图）
// ─────────────────────────────────────────────
private val BG_BLACK      = Color(0xFF0A0A0A)
private val BRAND_PINK    = Color(0xFFEC4899)
private val CARROT_ORANGE = Color(0xFFF97316)
private val NAV_PINK      = Color(0xFFEC4899)
private val NAV_GRAY      = Color(0xFF666666)
private val TEXT_WHITE    = Color(0xFFFFFFFF)
private val TEXT_GRAY     = Color(0xFF888888)
private val CARD_BG       = Color(0xFF1A1A1A)

/**
 * 首页主界面 — 1:1 还原 HOME_M2_01_main.png
 *
 * 布局规范：
 * - 顶部：摄颜Logo（雁宝小图标+「摄颜 SheYan」）左对齐 + 右侧铃铛
 * - 横向滚动快捷入口：雁宝记忆/大师渲染/29D渲染/2.9D视差（彩色渐变卡片）
 * - 「今日推荐」标题 + See all
 * - 2×2图片网格（带模式标签、点赞数、用户头像）
 * - 底部导航：首页(兔子)/编辑/[熊掌FAB]/推荐/我的（5项）
 */
@Composable
fun HomeScreen(
    onCameraClick:    () -> Unit = {},
    onEditorClick:    () -> Unit = {},
    onGalleryClick:   () -> Unit = {},
    onRecommendClick: () -> Unit = {},
    onProfileClick:   () -> Unit = {},
    onGardenClick:    () -> Unit = {},
    avatarUri:        String?    = null,
    viewModel:        HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG_BLACK)
    ) {
        // ─── 主内容区（可滚动） ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 88.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(8.dp))

            // ── 顶部 Logo 栏 ──────────────────────────────────────────────
            HomeTopBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 横向滚动快捷入口：雁宝记忆/大师渲染/29D渲染/2.9D视差 ──────
            QuickEntryRow(
                onCameraClick    = onCameraClick,
                onEditorClick    = onEditorClick,
                onGalleryClick   = onGalleryClick,
                onRecommendClick = onRecommendClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── 今日推荐标题 + See all ─────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "今日推荐",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TEXT_WHITE
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.clickable { onRecommendClick() }
                ) {
                    Text(text = "See all", fontSize = 13.sp, color = TEXT_GRAY)
                    Text(text = " >",     fontSize = 13.sp, color = TEXT_GRAY)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 2×2 今日推荐图片网格 ──────────────────────────────────────
            RecommendGrid(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 底部导航由 YanbaoApp.kt 全局 Scaffold 统一管理
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 顶部 Logo 栏：摄颜小图标 + 「摄颜 SheYan」 + 右侧铃铛（带红点）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeTopBar(
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 设计图：雁宝小头像（黑兔+粉蝶结）+ 品牌字
            Image(
                painter            = painterResource(R.drawable.ic_kuromi_small),
                contentDescription = "摄颜",
                modifier           = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A)),
                contentScale       = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = "摄颜",   fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TEXT_WHITE)
                Text(text = "SheYan", fontSize = 11.sp, color = TEXT_GRAY)
            }
        }
        // 铃铛 + 红点
        Box(
            modifier         = Modifier.size(40.dp).clickable { onNotificationClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_notification),
                contentDescription = "通知",
                tint               = TEXT_WHITE,
                modifier           = Modifier.size(24.dp)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B30))
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 横向快捷入口卡片：雁宝记忆/大师渲染/29D渲染/2.9D视差
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickEntryRow(
    onCameraClick:    () -> Unit = {},
    onEditorClick:    () -> Unit = {},
    onGalleryClick:   () -> Unit = {},
    onRecommendClick: () -> Unit = {}
) {
    data class QEntry(val title: String, val iconRes: Int, val gs: Color, val ge: Color, val action: () -> Unit)
    val entries = listOf(
        QEntry("雁宝记忆", R.drawable.ic_memory_album,  Color(0xFFEC4899), Color(0xFFFF6B9D), onGalleryClick),
        QEntry("大师渲染", R.drawable.ic_master_render,  Color(0xFFF97316), Color(0xFFFF9A3C), onCameraClick),
        QEntry("29D渲染",  R.drawable.ic_29d_render,     Color(0xFF00BCD4), Color(0xFF26C6DA), onCameraClick),
        QEntry("2.9D视差", R.drawable.ic_29d_parallax,   Color(0xFF9C27B0), Color(0xFFBA68C8), onCameraClick)
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding        = PaddingValues(horizontal = 16.dp)
    ) {
        items(entries) { e ->
                // 设计图：标题在上、icon在下
                Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(listOf(e.gs, e.ge)))
                    .clickable { e.action() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(text = e.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(
                        painter            = painterResource(e.iconRes),
                        contentDescription = e.title,
                        modifier           = Modifier.size(44.dp),
                        contentScale       = ContentScale.Fit
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 今日推荐 2×2 图片网格（带模式标签、点赞数、用户头像）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RecommendGrid(modifier: Modifier = Modifier) {
    data class WCard(val imgRes: Int, val label: String, val lColor: Color, val likes: String, val avRes: Int)
    val cards = listOf(
        WCard(R.drawable.sample_photo_01, "2.9D", Color(0xFF9C27B0), "1.2w", R.drawable.avatar_user),
        WCard(R.drawable.sample_photo_02, "大师",  Color(0xFFF97316), "1.2w", R.drawable.avatar_user),
        WCard(R.drawable.sample_photo_03, "2.9D", Color(0xFF9C27B0), "1.5w", R.drawable.avatar_user),
        WCard(R.drawable.sample_photo_04, "大师",  Color(0xFFF97316), "1.3w", R.drawable.avatar_user)
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WorkCard(cards[0], Modifier.weight(1f))
            WorkCard(cards[1], Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WorkCard(cards[2], Modifier.weight(1f))
            WorkCard(cards[3], Modifier.weight(1f))
        }
    }
}

@Composable
private fun WorkCard(
    card: Any,
    modifier: Modifier = Modifier
) {
    data class WCard(val imgRes: Int, val label: String, val lColor: Color, val likes: String, val avRes: Int)
    val c = card as WCard
    Box(
        modifier = modifier
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(16.dp))
            .background(CARD_BG)
    ) {
        Image(painterResource(c.imgRes), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        // 模式标签（右上角）
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(c.lColor)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(c.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        // 底部：头像 + 点赞
        Row(
            modifier              = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Image(painterResource(c.avRes), null, Modifier.size(28.dp).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape), contentScale = ContentScale.Crop)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_heart), null, Modifier.size(14.dp), tint = BRAND_PINK)
                Spacer(Modifier.width(3.dp))
                Text(c.likes, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 底部导航栏 — 5项：首页/编辑/[熊掌FAB]/推荐/我的
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeBottomNav(
    onHomeClick:      () -> Unit = {},
    onCameraClick:    () -> Unit,
    onEditorClick:    () -> Unit,
    onRecommendClick: () -> Unit,
    onProfileClick:   () -> Unit,
    modifier:         Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF0A0A0A))
    ) {
        Divider(modifier = Modifier.fillMaxWidth(), color = Color(0xFF222222), thickness = 1.dp)
        Row(
            modifier              = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            NavItem(R.drawable.ic_yanbao_home,      "首页", true,  onHomeClick)
            NavItem(R.drawable.ic_yanbao_edit,      "编辑", false, onEditorClick)
            // 中间熊掌FAB（进入相机模块）
            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(BRAND_PINK.copy(alpha = 0.25f), Color.Transparent)))
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(8.dp, CircleShape, spotColor = BRAND_PINK)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                        .border(2.dp, BRAND_PINK, CircleShape)
                        .clickable { onCameraClick() },
                    contentAlignment = Alignment.Center
                ) {
                    // 设计图：熊掌图标+粉色霓虹圈
                    Icon(
                        painter = painterResource(R.drawable.ic_shutter_paw),
                        contentDescription = "进入相机",
                        tint = BRAND_PINK,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            NavItem(R.drawable.ic_yanbao_recommend, "推荐", false, onRecommendClick)
            NavItem(R.drawable.ic_yanbao_profile,   "我的", false, onProfileClick)
        }
    }
}

@Composable
private fun NavItem(iconRes: Int, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier            = Modifier.clickable { onClick() }.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painterResource(iconRes), label, Modifier.size(24.dp),
            colorFilter = if (selected) androidx.compose.ui.graphics.ColorFilter.tint(NAV_PINK) else null
        )
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = if (selected) NAV_PINK else NAV_GRAY, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 数据类
// ─────────────────────────────────────────────────────────────────────────────
data class RecentActivity(val description: String, val time: String)
data class PopularPlace(val name: String, val rating: Int)
data class HomeUiState(
    val greeting:         String               = "早安！",
    val greetingSub:      String               = "用雁宝的眼睛，看世界的美",
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
