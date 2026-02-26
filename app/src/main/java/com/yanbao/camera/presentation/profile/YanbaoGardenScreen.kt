package com.yanbao.camera.presentation.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.R

// ─── 品牌色 ──────────────────────────────────────────────────────────────────
private val BrandPink = Color(0xFFEC4899)
private val CarrotOrange = Color(0xFFF97316)
private val ObsidianBlack = Color(0xFF0A0A0A)
private val CardBg = Color(0xFF1A1A1A)
private val CardBg2 = Color(0xFF242424)
private val MutedGray = Color(0xFF4A4A4A)

/**
 * 雁宝园地主界面
 *
 * TikTok 风格个人中心 + 雁宝互动区域
 * 包含：
 *  - 顶部返回栏
 *  - 雁宝 IP 互动区（动画 + 喂食按钮）
 *  - 今日喂食进度条
 *  - 特权等级卡片
 *  - 已解锁特权列表
 *  - 分享获取额外次数入口
 */
@Composable
fun YanbaoGardenScreen(
    viewModel: YanbaoGardenViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    val privilege by viewModel.privilege.collectAsState()
    val todayStatus by viewModel.todayStatus.collectAsState()
    val yanbaoMood by viewModel.yanbaoMood.collectAsState()
    val isFeedAnimating by viewModel.isFeedAnimating.collectAsState()
    val feedMessage by viewModel.feedMessage.collectAsState()
    val showLevelUpDialog by viewModel.showLevelUpDialog.collectAsState()
    val levelUpData by viewModel.levelUpData.collectAsState()
    val showLimitDialog by viewModel.showLimitDialog.collectAsState()
    val limitMessage by viewModel.limitMessage.collectAsState()
    val totalFeedCount by viewModel.totalFeedCount.collectAsState()

    val currentLevel = privilege?.privilegeLevel ?: 0
    val progress = viewModel.calculateProgress(totalFeedCount, currentLevel)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ─── 顶部栏 ─────────────────────────────────────────────────
            GardenTopBar(onBack = onBack)

            // ─── 雁宝互动区 ─────────────────────────────────────────────
            YanbaoInteractionZone(
                mood = yanbaoMood,
                isFeedAnimating = isFeedAnimating,
                feedMessage = feedMessage,
                onFeedCarrot = { viewModel.feedCarrot() },
                todayStatus = todayStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ─── 今日喂食进度 ────────────────────────────────────────────
            TodayFeedProgressCard(
                todayStatus = todayStatus,
                onShareClick = {
                    onShare()
                    viewModel.feedCarrotByShare()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ─── 特权等级卡片 ────────────────────────────────────────────
            PrivilegeLevelCard(
                currentLevel = currentLevel,
                levelName = viewModel.getPrivilegeLevelName(currentLevel),
                totalFeedCount = totalFeedCount,
                progress = progress,
                nextLevelThreshold = viewModel.getNextLevelThreshold(currentLevel),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ─── 已解锁特权列表 ──────────────────────────────────────────
            UnlockedPrivilegesCard(
                currentLevel = currentLevel,
                isVipUnlocked = privilege?.isVipUnlocked ?: false,
                isWatermarkRemoved = privilege?.isWatermarkRemoved ?: false,
                isExclusiveStickerUnlocked = privilege?.isExclusiveStickerUnlocked ?: false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ─── 喂食历史统计 ────────────────────────────────────────────
            FeedStatsCard(
                totalFeedCount = totalFeedCount,
                totalPoints = privilege?.totalPoints ?: 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ─── 升级弹窗 ────────────────────────────────────────────────────
        if (showLevelUpDialog && levelUpData != null) {
            LevelUpDialog(
                data = levelUpData!!,
                onDismiss = { viewModel.dismissLevelUpDialog() }
            )
        }

        // ─── 次数耗尽弹窗 ────────────────────────────────────────────────
        if (showLimitDialog) {
            LimitReachedDialog(
                message = limitMessage,
                onShare = {
                    viewModel.dismissLimitDialog()
                    onShare()
                    viewModel.feedCarrotByShare()
                },
                onDismiss = { viewModel.dismissLimitDialog() }
            )
        }
    }
}

// ─── 顶部栏 ──────────────────────────────────────────────────────────────────

@Composable
private fun GardenTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_yanbao_back),
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "雁宝园地",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "喂食胡萝卜，解锁专属特权",
                fontSize = 12.sp,
                color = BrandPink.copy(alpha = 0.8f)
            )
        }
    }
}

// ─── 雁宝互动区 ──────────────────────────────────────────────────────────────

@Composable
private fun YanbaoInteractionZone(
    mood: YanbaoMood,
    isFeedAnimating: Boolean,
    feedMessage: String?,
    onFeedCarrot: () -> Unit,
    todayStatus: TodayFeedStatus,
    modifier: Modifier = Modifier
) {
    // 雁宝弹跳动画
    val bounceAnim = rememberInfiniteTransition(label = "yanbao_bounce")
    val bounceY by bounceAnim.animateFloat(
        initialValue = 0f,
        targetValue = if (isFeedAnimating) -20f else -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isFeedAnimating) 300 else 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_y"
    )

    // 喂食时的缩放动画
    val feedScale by animateFloatAsState(
        targetValue = if (isFeedAnimating) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "feed_scale"
    )

    // 粉色光晕动画
    val glowAlpha by bounceAnim.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isFeedAnimating) 0.6f else 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 雁宝形象区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandPink.copy(alpha = 0.15f),
                            ObsidianBlack
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // 粉色光晕背景
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                BrandPink.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 雁宝图片（根据心情切换）
            val yanbaoImageRes = when (mood) {
                YanbaoMood.HAPPY -> R.drawable.yanbao_happy
                YanbaoMood.PLAYFUL -> R.drawable.yanbao_playful
                YanbaoMood.CARING -> R.drawable.yanbao_caring
                YanbaoMood.THINKING -> R.drawable.yanbao_thinking
                YanbaoMood.COOL -> R.drawable.yanbao_cool
                YanbaoMood.NORMAL -> R.drawable.yanbao_jk_uniform
            }

            Image(
                painter = painterResource(yanbaoImageRes),
                contentDescription = "雁宝",
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        translationY = bounceY
                        scaleX = feedScale
                        scaleY = feedScale
                    },
                contentScale = ContentScale.Fit
            )

            // 喂食成功消息气泡
            AnimatedVisibility(
                visible = feedMessage != null,
                enter = fadeIn() + slideInVertically { -40 },
                exit = fadeOut() + slideOutVertically { -40 },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                feedMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(1.dp, BrandPink.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 心情标签
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandPink.copy(alpha = 0.2f))
                    .border(1.dp, BrandPink.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = mood.toDisplayName(),
                    color = BrandPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 喂食按钮
        val canFeed = todayStatus.canFeedNormal
        Button(
            onClick = { if (canFeed && !isFeedAnimating) onFeedCarrot() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canFeed) CarrotOrange else MutedGray,
                disabledContainerColor = MutedGray
            ),
            enabled = canFeed && !isFeedAnimating,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (canFeed) 8.dp else 0.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_favorite_kuromi),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (canFeed) "喂食胡萝卜" else "今日已喂完",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ─── 今日喂食进度卡片 ─────────────────────────────────────────────────────────

@Composable
private fun TodayFeedProgressCard(
    todayStatus: TodayFeedStatus,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "今日喂食",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${todayStatus.normalFeedUsed}/${todayStatus.normalFeedLimit}",
                    fontSize = 14.sp,
                    color = CarrotOrange,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 胡萝卜进度指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(todayStatus.normalFeedLimit) { index ->
                    val isFed = index < todayStatus.normalFeedUsed
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isFed) CarrotOrange
                                else Color.White.copy(alpha = 0.15f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 分享奖励区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandPink.copy(alpha = 0.08f))
                    .border(1.dp, BrandPink.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "分享给朋友",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "分享可额外获得最多 2 次（今日剩余 ${todayStatus.shareBonusLimit - todayStatus.shareBonusUsed} 次）",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                val canShareFeed = todayStatus.canFeedByShare
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (canShareFeed) BrandPink else MutedGray)
                        .clickable(enabled = canShareFeed) { onShareClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "去分享",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─── 特权等级卡片 ─────────────────────────────────────────────────────────────

@Composable
private fun PrivilegeLevelCard(
    currentLevel: Int,
    levelName: String,
    totalFeedCount: Int,
    progress: Float,
    nextLevelThreshold: Int,
    modifier: Modifier = Modifier
) {
    val levelColors = listOf(
        listOf(Color(0xFF4A4A4A), Color(0xFF6A6A6A)),           // 普通
        listOf(Color(0xFF9CA3AF), Color(0xFFD1D5DB)),           // 银爪
        listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),           // 金爪
        listOf(Color(0xFF818CF8), Color(0xFFA78BFA))            // 钻石爪
    )
    val colors = levelColors.getOrNull(currentLevel) ?: levelColors[0]
    val levelIcons = listOf("普", "银", "金", "钻")

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            colors[0].copy(alpha = 0.15f),
                            colors[1].copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 等级徽章
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(colors = colors)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = levelIcons.getOrNull(currentLevel) ?: "普",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = levelName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "累计喂食 $totalFeedCount 次",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }
                    // 积分显示
                    Column(horizontalAlignment = Alignment.End) {
                        Icon(
                            painter = painterResource(R.drawable.ic_star_filled),
                            contentDescription = null,
                            tint = colors[1],
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (currentLevel < 3) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 进度条
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "升级进度",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                        Text(
                            text = "还需 ${(nextLevelThreshold - totalFeedCount).coerceAtLeast(0)} 次",
                            fontSize = 12.sp,
                            color = colors[1]
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(colors = colors)
                                )
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "已达最高等级！感谢你对雁宝的爱护",
                        fontSize = 13.sp,
                        color = colors[1],
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── 已解锁特权列表 ───────────────────────────────────────────────────────────

@Composable
private fun UnlockedPrivilegesCard(
    currentLevel: Int,
    isVipUnlocked: Boolean,
    isWatermarkRemoved: Boolean,
    isExclusiveStickerUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val allPrivileges = listOf(
        PrivilegeItem("专属粉色主题", currentLevel >= 1, R.drawable.ic_yanbao_profile, "银爪解锁"),
        PrivilegeItem("雁宝贴纸包 Vol.1", isExclusiveStickerUnlocked, R.drawable.ic_tool_sticker_kuromi, "银爪解锁"),
        PrivilegeItem("去水印导出", isWatermarkRemoved, R.drawable.ic_watermark_kuromi, "金爪解锁"),
        PrivilegeItem("独家滤镜 x5", currentLevel >= 2, R.drawable.ic_filter, "金爪解锁"),
        PrivilegeItem("雁宝贴纸包 Vol.2", currentLevel >= 2, R.drawable.ic_tool_sticker_kuromi, "金爪解锁"),
        PrivilegeItem("VIP 功能解锁", isVipUnlocked, R.drawable.ic_member_badge, "钻石爪解锁"),
        PrivilegeItem("所有独家滤镜", currentLevel >= 3, R.drawable.ic_filter, "钻石爪解锁"),
        PrivilegeItem("优先推荐展示", currentLevel >= 3, R.drawable.ic_yanbao_recommend, "钻石爪解锁")
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "园地特权",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(14.dp))

            allPrivileges.forEach { item ->
                PrivilegeRow(item = item)
                if (item != allPrivileges.last()) {
                    Divider(
                        color = Color.White.copy(alpha = 0.06f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivilegeRow(item: PrivilegeItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (item.isUnlocked) BrandPink.copy(alpha = 0.15f)
                    else Color.White.copy(alpha = 0.05f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(item.iconRes),
                contentDescription = null,
                tint = if (item.isUnlocked) BrandPink else MutedGray,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 14.sp,
                color = if (item.isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
                fontWeight = if (item.isUnlocked) FontWeight.Medium else FontWeight.Normal
            )
            if (!item.isUnlocked) {
                Text(
                    text = item.unlockHint,
                    fontSize = 11.sp,
                    color = MutedGray
                )
            }
        }
        if (item.isUnlocked) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandPink.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "已解锁",
                    fontSize = 11.sp,
                    color = BrandPink,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_close_kuromi),
                contentDescription = null,
                tint = MutedGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─── 喂食统计卡片 ─────────────────────────────────────────────────────────────

@Composable
private fun FeedStatsCard(
    totalFeedCount: Int,
    totalPoints: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeedStatItem(
                value = totalFeedCount.toString(),
                label = "累计喂食",
                valueColor = CarrotOrange
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            FeedStatItem(
                value = totalPoints.toString(),
                label = "园地积分",
                valueColor = BrandPink
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            FeedStatItem(
                value = "${(totalFeedCount / 7).coerceAtLeast(0)}",
                label = "连续天数",
                valueColor = Color(0xFF34D399)
            )
        }
    }
}

@Composable
private fun FeedStatItem(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.55f)
        )
    }
}

// ─── 升级弹窗 ─────────────────────────────────────────────────────────────────

@Composable
private fun LevelUpDialog(
    data: LevelUpData,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                    )
                )
                .border(1.dp, BrandPink.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                .padding(28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 升级图标
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(BrandPink.copy(alpha = 0.4f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star_filled),
                        contentDescription = null,
                        tint = BrandPink,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "特权升级！",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "恭喜成为 ${data.levelName}",
                    fontSize = 15.sp,
                    color = BrandPink
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 解锁特权列表
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "新解锁特权",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    data.unlockedPrivileges.forEach { privilege ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(BrandPink)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = privilege,
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPink)
                ) {
                    Text(
                        text = "太棒了！",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ─── 次数耗尽弹窗 ─────────────────────────────────────────────────────────────

@Composable
private fun LimitReachedDialog(
    message: String,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, CarrotOrange.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.ic_yanbao_info),
                    contentDescription = null,
                    tint = CarrotOrange,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "今日胡萝卜已用完",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("明天再来", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPink)
                    ) {
                        Text("去分享", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── 辅助扩展函数 ─────────────────────────────────────────────────────────────

private fun YanbaoMood.toDisplayName(): String = when (this) {
    YanbaoMood.NORMAL -> "待机中"
    YanbaoMood.HAPPY -> "好开心"
    YanbaoMood.PLAYFUL -> "好俏皮"
    YanbaoMood.CARING -> "好温柔"
    YanbaoMood.THINKING -> "在思考"
    YanbaoMood.COOL -> "超酷炫"
}

private data class PrivilegeItem(
    val name: String,
    val isUnlocked: Boolean,
    val iconRes: Int,
    val unlockHint: String
)
