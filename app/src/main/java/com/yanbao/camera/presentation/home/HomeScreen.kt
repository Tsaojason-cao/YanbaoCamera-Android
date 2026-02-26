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
// 设计规范颜色常量
// ─────────────────────────────────────────────
private val BG_TOP        = Color(0xFF0A0A0A)   // 曜石黑（主背景）
private val BG_BOTTOM     = Color(0xFF141414)
private val BRAND_PINK    = Color(0xFFEC4899)   // 品牌粉
private val CARROT_ORANGE = Color(0xFFF97316)   // 胡萝卜橙
private val PINK_S        = Color(0xFFEC4899)
private val PINK_E        = Color(0xFF9D4EDD)
private val DARK_S        = Color(0xFF2A2A2A)
private val DARK_E        = Color(0xFF1A1A1A)
private val GOLD_S        = Color(0xFFD4A020)
private val GOLD_E        = Color(0xFFB88010)
private val BLUE_S        = Color(0xFF1A6090)
private val BLUE_E        = Color(0xFF0E4060)
private val NAV_PINK      = Color(0xFFEC4899)
private val NAV_GRAY      = Color(0xFF666666)
private val TEXT_WHITE    = Color(0xFFFFFFFF)
private val TEXT_GRAY     = Color(0xFF888888)
private val CARD_BG       = Color(0xFF1A1A1A)

/**
 * 首页主界面
 *
 * 布局规范：
 * - 上层 72%：精选作品展示区 / 实时动态
 * - 下层 28%：功能控制区（四大入口）
 * - 正中心：超大粉色熊掌印相机入口按钮
 * - 悬浮挂件：右侧雁宝动态形象（提示胡萝卜喂养状态）
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
            .background(Brush.verticalGradient(listOf(BG_TOP, BG_BOTTOM)))
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

            // ── 顶部：三列布局 早安文字(左) | 品牌标语(中) | 头像(右) ────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // 左侧：早安！+ 副文字
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = uiState.greeting,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TEXT_WHITE
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text     = uiState.greetingSub,
                        fontSize = 12.sp,
                        color    = BRAND_PINK.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
                // 右侧：头像（粉色渐变圆圈边框）
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(listOf(BRAND_PINK, CARROT_ORANGE)),
                            shape = CircleShape
                        )
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter            = painterResource(R.drawable.avatar_user),
                        contentDescription = "头像",
                        modifier           = Modifier
                            .size(46.dp)
                            .clip(CircleShape),
                        contentScale       = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ═══════════════════════════════════════════════════════════
            // 72% 区域：精选作品展示 Banner
            // ═══════════════════════════════════════════════════════════
            FeaturedWorksBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.72f)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ═══════════════════════════════════════════════════════════
            // 28% 区域：功能控制区
            // 正中心超大熊掌印相机入口 + 左右侧边组件
            // ═══════════════════════════════════════════════════════════
            ControlPanel(
                onCameraClick    = onCameraClick,
                onGalleryClick   = onGalleryClick,
                onRecommendClick = onRecommendClick,
                modifier         = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── 快捷功能卡片（2×2） ──────────────────────────────────
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

            // ── 最近活动 ──────────────────────────────────────────────
            Spacer(modifier = Modifier.height(28.dp))
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

            // ── 热门地点 ──────────────────────────────────────────────
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

        // ─── 雁宝悬浮挂件（右侧固定，提示胡萝卜喂养状态） ─────────────
        YanbaoFloatingWidget(
            onGardenClick = onGardenClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 0.dp, top = 120.dp)
        )

        // ─── 底部导航栏 ──────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────────────────
// 72% 精选作品展示 Banner
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FeaturedWorksBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A0A1A),
                        Color(0xFF2A1040),
                        Color(0xFF1A0A2A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 背景粉色光晕
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BRAND_PINK.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 品牌标语
            Text(
                text       = "用雁宝的眼睛，看世界的美",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = TEXT_WHITE,
                textAlign  = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text      = "SheYan · AI Camera",
                fontSize  = 12.sp,
                color     = BRAND_PINK.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 雁宝形象
            Image(
                painter            = painterResource(R.drawable.yanbao_jk_uniform),
                contentDescription = "雁宝",
                modifier           = Modifier.size(100.dp),
                contentScale       = ContentScale.Fit
            )
        }

        // 右上角标签
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BRAND_PINK.copy(alpha = 0.2f))
                .border(1.dp, BRAND_PINK.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text      = "AI 加持",
                fontSize  = 10.sp,
                color     = BRAND_PINK,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 28% 控制面板：正中心熊掌相机入口 + 左侧相册 + 右侧推荐
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ControlPanel(
    onCameraClick:    () -> Unit,
    onGalleryClick:   () -> Unit,
    onRecommendClick: () -> Unit,
    modifier:         Modifier = Modifier
) {
    Row(
        modifier              = modifier.height(100.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // 左侧：熊耳边框相册入口
        BearEarGalleryButton(
            onClick  = onGalleryClick,
            modifier = Modifier.size(80.dp)
        )

        // 正中心：超大粉色熊掌印相机按钮（72dp）
        BearPawCameraButton(
            onClick  = onCameraClick,
            modifier = Modifier.size(80.dp)
        )

        // 右侧：推荐社区入口
        RecommendButton(
            onClick  = onRecommendClick,
            modifier = Modifier.size(80.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 超大粉色熊掌印相机按钮（核心入口）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BearPawCameraButton(
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    // 霓虹发光脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "paw_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue  = 0.8f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(
        modifier = modifier.clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 霓虹光晕（80dp，粉色）
        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                    alpha  = glowAlpha
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BRAND_PINK.copy(alpha = 0.5f),
                            BRAND_PINK.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )
        // 主按钮（72dp，粉色渐变）
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(BRAND_PINK, Color(0xFFBE185D))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // 熊掌印图标
            Icon(
                painter            = painterResource(R.drawable.ic_shutter_paw),
                contentDescription = "拍照",
                tint               = Color.White,
                modifier           = Modifier.size(36.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 熊耳边框相册入口按钮
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BearEarGalleryButton(
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CARD_BG)
            .border(1.5.dp, BRAND_PINK.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_tab_album_kuromi),
                contentDescription = "相册",
                tint               = BRAND_PINK,
                modifier           = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text      = "相册",
                fontSize  = 11.sp,
                color     = TEXT_WHITE.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 推荐社区入口按钮
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RecommendButton(
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CARD_BG)
            .border(1.5.dp, CARROT_ORANGE.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_tab_recommend_kuromi_nobg),
                contentDescription = "推荐",
                tint               = CARROT_ORANGE,
                modifier           = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text      = "推荐",
                fontSize  = 11.sp,
                color     = TEXT_WHITE.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 雁宝悬浮挂件（右侧固定，提示胡萝卜饥饿状态）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun YanbaoFloatingWidget(
    onGardenClick: () -> Unit,
    modifier:      Modifier = Modifier
) {
    // 上下浮动动画
    val infiniteTransition = rememberInfiniteTransition(label = "float_widget")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = -10f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    Column(
        modifier = modifier
            .width(64.dp)
            .clickable { onGardenClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 提示气泡
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .border(1.dp, CARROT_ORANGE.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text      = "肚子饿了",
                fontSize  = 9.sp,
                color     = CARROT_ORANGE,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 雁宝形象（浮动）
        Image(
            painter            = painterResource(R.drawable.yanbao_thinking),
            contentDescription = "雁宝",
            modifier           = Modifier
                .size(56.dp)
                .graphicsLayer { translationY = floatY },
            contentScale       = ContentScale.Fit
        )

        // 胡萝卜图标
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(CARROT_ORANGE),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter            = painterResource(R.drawable.ic_garden_carrot),
                contentDescription = "胡萝卜",
                tint               = Color.White,
                modifier           = Modifier.size(14.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 功能卡片：高度固定 100.dp
// ─────────────────────────────────────────────────────────────────────────────
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
                modifier           = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text       = title,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 区块标题
// ─────────────────────────────────────────────────────────────────────────────
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
            color      = TEXT_WHITE
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

// ─────────────────────────────────────────────────────────────────────────────
// 最近活动卡片
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RecentActivityCard(activity: RecentActivity) {
    Card(
        modifier  = Modifier
            .width(200.dp)
            .height(90.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CARD_BG),
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
                        .background(BRAND_PINK.copy(alpha = 0.15f)),
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
                    color    = TEXT_WHITE,
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

// ─────────────────────────────────────────────────────────────────────────────
// 热门地点卡片
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PopularPlaceCard(place: PopularPlace, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CARD_BG),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val photoRes = when (place.name) {
                "台北101"  -> R.drawable.place_taipei101
                "台南波场" -> R.drawable.place_tainan
                "北海坑境" -> R.drawable.place_hokkaido
                else       -> R.drawable.place_taipei101
            }
            Box {
                Image(
                    painter            = painterResource(photoRes),
                    contentDescription = place.name,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale       = ContentScale.Crop
                )
                Image(
                    painter            = painterResource(R.drawable.ic_marker_kuromi),
                    contentDescription = null,
                    modifier           = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text       = place.name,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TEXT_WHITE
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    repeat(5) { index ->
                        Text(
                            text     = if (index < place.rating) "★" else "☆",
                            fontSize = 11.sp,
                            color    = if (index < place.rating) CARROT_ORANGE else TEXT_GRAY
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 底部导航栏：6 等宽，图标 24.dp，文字 10.sp
// ─────────────────────────────────────────────────────────────────────────────
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
        color           = Color(0xFF1A1A1A),
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
