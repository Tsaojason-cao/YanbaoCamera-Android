package com.yanbao.camera.presentation.recommend

/**
 * M6 推荐模块 - 1:1 对标设计图 REC_M6_01_main_feed.png
 *
 * 设计规范：
 * - 背景：曜石黑 #0A0A0A
 * - 主色：品牌粉 #EC4899（霓虹边框、激活Tab下划线、关注按钮）
 * - 副色：胡萝卜橙 #F97316（标签、一键Get按钮）
 * - 布局：顶部标题栏（推荐 + 搜索/筛选图标）→ Tab栏（推荐/关注/附近）
 *         → 大卡片（粉色霓虹边框 + 雁宝贴纸右上角 + 用户信息左下角）
 *         → 右侧操作栏（熊掌赞/评论/分享/收藏/更多）
 *         → 底部「🐾 一键 Get 同款参数」粉色霓虹边框大按钮
 */

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yanbao.camera.R

// ── 设计常量 ──────────────────────────────────────────────────────────────────
private val OBSIDIAN_BLACK = Color(0xFF0A0A0A)
private val BRAND_PINK     = Color(0xFFEC4899)
private val CARROT_ORANGE  = Color(0xFFF97316)

// ── 模拟数据 ──────────────────────────────────────────────────────────────────
private data class FeedPost(
    val id: String,
    val username: String,
    val spValue: String,
    val caption: String,
    val location: String,
    val tags: List<Pair<String, Color>>,
    val imageRes: Int,
    val likeCount: String,
    val commentCount: String,
    val shareCount: String,
    val isFollowing: Boolean = false
)

private val samplePosts = listOf(
    FeedPost(
        id = "1",
        username = "@春日摄影小桃",
        spValue = "16sp",
        caption = "京都樱花季，用大师滤镜拍出电影感。快来Get同款！",
        location = "日本庭园, 京都",
        tags = listOf("大师滤镜" to CARROT_ORANGE, "2.9D" to BRAND_PINK),
        imageRes = R.drawable.place_hokkaido,
        likeCount = "1.2k",
        commentCount = "245",
        shareCount = "560",
        isFollowing = false
    ),
    FeedPost(
        id = "2",
        username = "@台南旅拍达人",
        spValue = "12sp",
        caption = "台南古城巷弄，胶片风格让每张照片都像故事。",
        location = "赤崁楼, 台南",
        tags = listOf("胶片风" to CARROT_ORANGE, "1.8D" to BRAND_PINK),
        imageRes = R.drawable.place_tainan,
        likeCount = "3.4k",
        commentCount = "189",
        shareCount = "892",
        isFollowing = true
    ),
    FeedPost(
        id = "3",
        username = "@台北夜景控",
        spValue = "20sp",
        caption = "台北101夜景，用雁宝AI一键调出霓虹感！",
        location = "信义区, 台北",
        tags = listOf("夜景模式" to CARROT_ORANGE, "3.2D" to BRAND_PINK),
        imageRes = R.drawable.place_taipei101,
        likeCount = "5.6k",
        commentCount = "312",
        shareCount = "1.2k",
        isFollowing = false
    )
)

// ── 主界面 ────────────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RecommendScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("推荐", "关注", "附近")
    val pagerState = rememberPagerState(pageCount = { samplePosts.size })
    val currentPost = samplePosts[pagerState.currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OBSIDIAN_BLACK)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── 顶部标题栏 ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "推荐",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(R.drawable.ic_yanbao_info),
                    contentDescription = "搜索",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_yanbao_master),
                    contentDescription = "筛选",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                )
            }

            // ── Tab 栏（推荐/关注/附近）──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                tabs.forEachIndexed { index, tab ->
                    Column(
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { selectedTab = index }
                            .padding(end = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tab,
                            fontSize = if (selectedTab == index) 16.sp else 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(if (selectedTab == index) 24.dp else 0.dp)
                                .height(2.dp)
                                .background(
                                    color = if (selectedTab == index) BRAND_PINK else Color.Transparent,
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 主内容区：大卡片 + 右侧操作栏 ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // 垂直滑动 Pager（TikTok 式）
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    FeedCard(post = samplePosts[page])
                }

                // 右侧操作栏（固定在右侧）
                ActionSidebar(
                    post = currentPost,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 0.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── 底部「一键 Get 同款参数」按钮 ─────────────────────────────
            GetParamsButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

// ── 主卡片 ────────────────────────────────────────────────────────────────────
@Composable
private fun FeedCard(post: FeedPost) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            BRAND_PINK.copy(alpha = glowAlpha),
                            BRAND_PINK.copy(alpha = glowAlpha * 0.6f),
                            BRAND_PINK.copy(alpha = glowAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            // 照片背景
            Image(
                painter = painterResource(id = post.imageRes),
                contentDescription = post.caption,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            // 底部渐变遮罩
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // 右上角雁宝摄影款贴纸
            Image(
                painter = painterResource(R.drawable.yanbao_jk_uniform),
                contentDescription = "雁宝",
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 4.dp)
            )

            // 左下角用户信息区域
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, end = 80.dp, bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BRAND_PINK.copy(alpha = 0.3f))
                            .border(1.dp, BRAND_PINK, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.avatar_anime),
                            contentDescription = "头像",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(
                        text = post.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = post.spValue,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(BRAND_PINK)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (post.isFollowing) "已关注" else "关注",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = post.caption,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_yanbao_29d),
                            contentDescription = "位置",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = post.location,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    post.tags.forEach { (tag, color) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 右侧操作栏 ────────────────────────────────────────────────────────────────
@Composable
private fun ActionSidebar(
    post: FeedPost,
    modifier: Modifier = Modifier
) {
    var isLiked by remember { mutableStateOf(false) }
    var isBookmarked by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .width(56.dp)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ActionItem(
            iconRes = R.drawable.ic_shutter,
            label = post.likeCount,
            isActive = isLiked,
            activeColor = BRAND_PINK,
            onClick = { isLiked = !isLiked }
        )
        ActionItem(
            iconRes = R.drawable.ic_yanbao_memory,
            label = post.commentCount,
            isActive = false,
            activeColor = BRAND_PINK,
            onClick = {}
        )
        ActionItem(
            iconRes = R.drawable.ic_yanbao_back,
            label = post.shareCount,
            isActive = false,
            activeColor = BRAND_PINK,
            onClick = {}
        )
        ActionItem(
            iconRes = R.drawable.ic_yanbao_gallery,
            label = "收藏",
            isActive = isBookmarked,
            activeColor = CARROT_ORANGE,
            onClick = { isBookmarked = !isBookmarked }
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {},
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "•••",
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActionItem(
    iconRes: Int,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = if (isActive) activeColor else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

// ── 底部「一键 Get 同款参数」按钮 ─────────────────────────────────────────────
@Composable
private fun GetParamsButton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "btn_neon")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btn_glow"
    )

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        BRAND_PINK.copy(alpha = glowAlpha),
                        BRAND_PINK.copy(alpha = glowAlpha * 0.5f),
                        BRAND_PINK.copy(alpha = glowAlpha)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .background(
                color = Color(0xFF0F0A0A),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {},
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_shutter),
                contentDescription = null,
                tint = BRAND_PINK,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "一键 Get 同款参数",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ── 保留旧的枚举和数据类（供 ViewModel 使用）────────────────────────────────
enum class RecommendTab(val displayName: String) {
    NEARBY("附近"),
    LATEST("最新"),
    HOT("热门"),
    RATING("评分")
}

data class PhotoSpot(
    val id: String,
    val title: String,
    val location: String,
    val description: String,
    val imageUrl: String,
    val rating: Float,
    val category: String,
    val categoryColor: Color,
    val badgeIcon: Int,
    val distance: Float? = null,
    val photoCount: Int = 0
)
