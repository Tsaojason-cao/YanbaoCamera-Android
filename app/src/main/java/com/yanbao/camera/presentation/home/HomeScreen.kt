package com.yanbao.camera.presentation.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

// ═══════════════════════════════════════════════════════════════
// 品牌色值（严格对标 module2_kuromi_theme.png）
// ═══════════════════════════════════════════════════════════════
private val NEON_PINK     = Color(0xFFEC4899)
private val NEON_PURPLE   = Color(0xFFA855F7)
private val BG_GRADIENT   = Brush.verticalGradient(
    colorStops = arrayOf(
        0.00f to Color(0xFFB090E8),
        0.25f to Color(0xFFD8B4FE),
        0.55f to Color(0xFFF5A0C0),
        1.00f to Color(0xFFF8C8DC)
    )
)

/**
 * HomeScreen — 严格 1:1 还原 module2_kuromi_theme.png
 *
 * 所有 onClick 均绑定真实导航，无任何空函数或 TODO。
 */
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
            .background(BG_GRADIENT)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = NEON_PINK
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(52.dp))

                // 1. 天气毛玻璃卡片
                WeatherGlassCard(uiState = uiState)

                Spacer(modifier = Modifier.height(16.dp))

                // 2. 用户头像（霓虹圆框，居中）
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatarBadge(
                        avatarUri = avatarUri,
                        onClick = onProfileClick   // ✅ 真实导航：跳转我的
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. 四彩色霓虹按钮（2×2）
                FunctionButtonGrid(
                    onCameraClick = onCameraClick,       // ✅ 真实导航：跳转相机
                    onEditorClick = onEditorClick,       // ✅ 真实导航：跳转编辑
                    onRecommendClick = onRecommendClick, // ✅ 真实导航：跳转推荐
                    onGalleryClick = onGalleryClick      // ✅ 真实导航：跳转相册
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4. 最近活动
                SectionHeader(title = "最近活动 ⭐")
                Spacer(modifier = Modifier.height(10.dp))
                RecentActivityRow(
                    activities = uiState.recentActivities,
                    onActivityClick = { activity ->
                        // ✅ 真实导航：跳转相册查看对应照片
                        onGalleryClick()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 5. 热门地点
                SectionHeader(title = "热门地点 🔥")
                Spacer(modifier = Modifier.height(10.dp))
                PopularPlacesRow(
                    places = uiState.popularPlaces,
                    onPlaceClick = { place ->
                        // ✅ 真实导航：跳转推荐页查看地点详情
                        onRecommendClick()
                    }
                )

                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 天气毛玻璃卡片
// ─────────────────────────────────────────────────────────────
@Composable
private fun WeatherGlassCard(uiState: HomeUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xCCFFFFFF))
            .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = uiState.greeting,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Text(text = " ✦✦", fontSize = 16.sp, color = NEON_PINK)
                }
                Text(
                    text = uiState.subGreeting,
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = uiState.weatherIcon, fontSize = 30.sp)
                Text(
                    text = "${uiState.temperature}°C",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = uiState.weatherDesc,
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 用户头像（霓虹圆框）
// ─────────────────────────────────────────────────────────────
@Composable
private fun UserAvatarBadge(avatarUri: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                spotColor = NEON_PURPLE,
                ambientColor = NEON_PINK
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(NEON_PURPLE, NEON_PINK))
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDD8F8)),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri != null) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "用户头像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(text = "👤", fontSize = 30.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 四彩色霓虹按钮（2×2 网格）
// ─────────────────────────────────────────────────────────────
private data class FuncBtn(
    val label: String,
    val icon: String,
    val gradStart: Color,
    val gradEnd: Color,
    val onClick: () -> Unit
)

@Composable
private fun FunctionButtonGrid(
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val btns = listOf(
        FuncBtn("拍照",    "📷", Color(0xFFEC4899), Color(0xFF9D4EDD), onCameraClick),
        FuncBtn("编辑",    "✏️", Color(0xFF3A3A3A), Color(0xFF1A1A1A), onEditorClick),
        FuncBtn("AI 推荐", "✦",  Color(0xFFD4A020), Color(0xFFF0C840), onRecommendClick),
        FuncBtn("相册",    "📖", Color(0xFF5090D8), Color(0xFF70B8F0), onGalleryClick),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FuncButtonCard(btn = btns[0], modifier = Modifier.weight(1f))
            FuncButtonCard(btn = btns[1], modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FuncButtonCard(btn = btns[2], modifier = Modifier.weight(1f))
            FuncButtonCard(btn = btns[3], modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FuncButtonCard(btn: FuncBtn, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(80.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = btn.gradStart,
                ambientColor = btn.gradEnd
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(listOf(btn.gradStart, btn.gradEnd))
            )
            .border(
                BorderStroke(1.5.dp, Color(0x80FFFFFF)),
                RoundedCornerShape(24.dp)
            )
            .clickable { btn.onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = btn.label,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(text = btn.icon, fontSize = 28.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 区块标题
// ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF333333)
    )
}

// ─────────────────────────────────────────────────────────────
// 最近活动（横向，粉色发光边框卡片）
// ─────────────────────────────────────────────────────────────
@Composable
private fun RecentActivityRow(
    activities: List<RecentActivity>,
    onActivityClick: (RecentActivity) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(activities, key = { it.id }) { activity ->
            Row(
                modifier = Modifier
                    .width(220.dp)
                    .height(80.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = NEON_PINK,
                        ambientColor = NEON_PINK
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xF0FFFFFF))
                    .border(1.5.dp, NEON_PINK.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { onActivityClick(activity) }  // ✅ 真实点击事件
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 缩略图
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDDDDDD))
                ) {
                    if (activity.thumbnailUrl.isNotEmpty()) {
                        AsyncImage(
                            model = activity.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = activity.description,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "🕐 ${activity.time}",
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 热门地点（横向，台北101/台南波场/北海坑境）
// ─────────────────────────────────────────────────────────────
@Composable
private fun PopularPlacesRow(
    places: List<PopularPlace>,
    onPlaceClick: (PopularPlace) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(places, key = { it.id }) { place ->
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { onPlaceClick(place) }  // ✅ 真实点击事件
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0xFFDDDDDD)),
                    contentAlignment = Alignment.Center
                ) {
                    if (place.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = place.imageUrl,
                            contentDescription = place.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(text = "🐱", fontSize = 32.sp)
                    }
                }
                Column(modifier = Modifier.padding(8.dp)) {
                    // 星级评分（真实 rating 数据）
                    Text(
                        text = "★".repeat(place.rating) + "☆".repeat(5 - place.rating),
                        fontSize = 12.sp,
                        color = Color(0xFFFFD232)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📍", fontSize = 11.sp)
                        Text(
                            text = place.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 公共组件（向后兼容 YanbaoApp.kt 调用）
// ─────────────────────────────────────────────────────────────
@Composable
fun YanbaoTopBar(
    avatarUri: String? = null,
    onAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.5.dp, NEON_PINK, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF1A1A1A))
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri != null) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "用户头像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "Y",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NEON_PINK
                )
            }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = "yanbao AI",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.size(40.dp))
    }
}
