package com.yanbao.camera.presentation.home

import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yanbao.camera.presentation.recommend.PhotoSpot
import com.yanbao.camera.presentation.recommend.RecommendViewModel
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════
// 强制色号（来自 pasted_content_2.txt 技术约束）
// ═══════════════════════════════════════════════════════════════
private val NEON_PINK     = Color(0xFFEC4899)
private val NEON_PURPLE   = Color(0xFFA855F7)
private val SOFT_BG_START = Color(0xFFF5A0C0)
private val SOFT_BG_END   = Color(0xFFD8B4FE)

/**
 * HomeScreen — 严格像素级还原 module2_kuromi_theme.png（满画面，无手机壳）
 *
 * 布局（从顶到底）：
 * 1. 天气毛玻璃卡片（早安！28°C 适合外拍）
 * 2. 用户头像（霓虹圆框，居中）
 * 3. 四彩色霓虹按钮：拍照 / 编辑 / AI推荐 / 相册（2×2）
 * 4. 最近活动（LazyRow，粉色发光边框卡片）
 * 5. 热门地点（LazyRow，台北101/台南波场/北海坑境）
 * 6. 底部导航（5标签，半透明粉色磨砂，中间库洛米图标）
 */
@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onProfileClick: () -> Unit,
    avatarUri: String? = null,
    modifier: Modifier = Modifier
) {
    val recommendViewModel: RecommendViewModel = hiltViewModel()
    val spots by recommendViewModel.filteredSpots.collectAsState()

    // 满画面粉紫径向渐变背景（Modifier.fillMaxSize().background(radialGradient)）
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFFB090E8),
                        0.25f to SOFT_BG_END,
                        0.55f to SOFT_BG_START,
                        1.00f to Color(0xFFF8C8DC)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // ── 1. 天气毛玻璃卡片 ──
            WeatherGlassCard()

            Spacer(modifier = Modifier.height(16.dp))

            // ── 2. 用户头像（霓虹圆框，居中）──
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                UserAvatarNeonBadge(avatarUri = avatarUri, onClick = onProfileClick)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 3. 四彩色霓虹按钮（2×2）──
            FunctionButtonGrid(
                onCameraClick = onCameraClick,
                onEditorClick = onEditorClick,
                onRecommendClick = onRecommendClick,
                onGalleryClick = onGalleryClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 4. 最近活动 ──
            SectionHeader(title = "最近活动 ⭐")
            Spacer(modifier = Modifier.height(10.dp))
            RecentActivityRow()

            Spacer(modifier = Modifier.height(20.dp))

            // ── 5. 热门地点 ──
            SectionHeader(title = "热门地点 🔥")
            Spacer(modifier = Modifier.height(10.dp))
            HotSpotsRow(spots = spots, onSpotClick = { onRecommendClick() })

            Spacer(modifier = Modifier.height(88.dp))
        }

        // ── 6. 底部导航（固定底部）──
        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = {},
            onCameraClick = onCameraClick,
            onRecommendClick = onRecommendClick,
            onGalleryClick = onGalleryClick,
            onProfileClick = onProfileClick
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 天气毛玻璃卡片
// ─────────────────────────────────────────────────────────────
@Composable
private fun WeatherGlassCard() {
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
                    Text(text = "早安！", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    Text(text = " ✦✦", fontSize = 16.sp, color = NEON_PINK)
                }
                Text(text = "今天也要拍出好照片哦 📷", fontSize = 13.sp, color = Color(0xFF666666))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "☀️", fontSize = 30.sp)
                Text(text = "28°C", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                Text(text = "适合外拍", fontSize = 11.sp, color = Color(0xFF888888))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 用户头像（霓虹圆框）
// ─────────────────────────────────────────────────────────────
@Composable
private fun UserAvatarNeonBadge(avatarUri: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .shadow(10.dp, CircleShape, spotColor = NEON_PURPLE, ambientColor = NEON_PINK)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(NEON_PURPLE, NEON_PINK)))
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FuncButtonCard(btn = btns[0], modifier = Modifier.weight(1f))
            FuncButtonCard(btn = btns[1], modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = btn.gradStart, ambientColor = btn.gradEnd)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(btn.gradStart, btn.gradEnd)))
            .border(BorderStroke(1.5.dp, Color(0x80FFFFFF)), RoundedCornerShape(24.dp))
            .clickable { btn.onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = btn.label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
private fun RecentActivityRow() {
    val activities = listOf(
        "you在台北101拍摄了新照片" to "Time 1s ago",
        "you在台北101拍摄了新照片" to "Time 1s ago",
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(activities) { (title, time) ->
            Row(
                modifier = Modifier
                    .width(220.dp)
                    .height(80.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = NEON_PINK, ambientColor = NEON_PINK)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xF0FFFFFF))
                    .border(1.5.dp, NEON_PINK.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDDDDDD))
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(text = "🕐 $time", fontSize = 11.sp, color = Color(0xFF999999))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 热门地点（横向，台北101/台南波场/北海坑境）
// ─────────────────────────────────────────────────────────────
@Composable
private fun HotSpotsRow(spots: List<PhotoSpot>, onSpotClick: (PhotoSpot) -> Unit) {
    val defaultSpots = listOf(
        PhotoSpot(id = "1", title = "台北101",  imageUrl = "", distance = 1.2, photoCount = 256),
        PhotoSpot(id = "2", title = "台南波场",  imageUrl = "", distance = 3.5, photoCount = 189),
        PhotoSpot(id = "3", title = "北海坑境",  imageUrl = "", distance = 5.8, photoCount = 142),
    )
    val displaySpots = if (spots.isNotEmpty()) spots.take(6) else defaultSpots
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(displaySpots) { spot ->
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { onSpotClick(spot) }
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFFDDDDDD)),
                    contentAlignment = Alignment.Center
                ) {
                    if (spot.imageUrl.isNotEmpty()) {
                        AsyncImage(model = spot.imageUrl, contentDescription = spot.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(NEON_PINK.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                            Text(text = "🐱", fontSize = 22.sp)
                        }
                    }
                }
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "★★★★★", fontSize = 12.sp, color = Color(0xFFFFD232))
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📍", fontSize = 11.sp)
                        Text(text = spot.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 底部导航（5标签，半透明粉色磨砂，中间库洛米图标）
// ─────────────────────────────────────────────────────────────
@Composable
private fun BottomNavBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xE8FFF0F8))
            .border(BorderStroke(1.dp, Color(0x50EC4899)), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabItem(label = "首页", icon = "🏠", isSelected = true,  onClick = onHomeClick)
            NavTabItem(label = "拍照", icon = "📷", isSelected = false, onClick = onCameraClick)
            // 中间：库洛米大头图标（突出，带霓虹圆框）
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-8).dp).clickable { onRecommendClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(10.dp, CircleShape, spotColor = NEON_PINK, ambientColor = NEON_PURPLE)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(NEON_PINK, NEON_PURPLE)))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🐱", fontSize = 26.sp)
                }
                Text(text = "推荐", fontSize = 10.sp, color = NEON_PINK, fontWeight = FontWeight.Bold)
            }
            NavTabItem(label = "相册", icon = "📖", isSelected = false, onClick = onGalleryClick)
            NavTabItem(label = "我的", icon = "👤", isSelected = false, onClick = onProfileClick)
        }
    }
}

@Composable
private fun NavTabItem(label: String, icon: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) NEON_PINK else Color(0xFF888888)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Text(text = icon, fontSize = 20.sp)
        Text(text = label, fontSize = 11.sp, color = color, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

// ─────────────────────────────────────────────────────────────
// 公共组件（向后兼容）
// ─────────────────────────────────────────────────────────────
@Composable
fun YanbaoTopBar(
    avatarUri: String? = null,
    onAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).background(Color(0xFF0A0A0A)).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).border(1.5.dp, NEON_PINK, CircleShape).clip(CircleShape).background(Color(0xFF1A1A1A)).clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri != null) {
                AsyncImage(model = avatarUri, contentDescription = "用户头像", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(text = "Y", fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NEON_PINK)
            }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(text = "yanbao AI", fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
fun CameraPreviewComponent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val previewManager = remember { com.yanbao.camera.core.camera.Camera2PreviewManager(context) }
    DisposableEffect(Unit) { onDispose { previewManager.release() } }
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        scope.launch {
                            try { previewManager.openCamera(holder.surface) }
                            catch (e: Exception) { Log.e("CameraPreview", e.message ?: "") }
                        }
                    }
                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                    override fun surfaceDestroyed(holder: SurfaceHolder) { previewManager.closeCamera() }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun PreviewOverlayLabels() {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.align(Alignment.TopStart).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ISO 100", "S 1/250", "35mm").forEach { param ->
                Box(
                    modifier = Modifier.background(Color(0xCC000000), RoundedCornerShape(4.dp)).border(0.5.dp, NEON_PINK, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = param, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NEON_PINK)
                }
            }
        }
    }
}

@Composable
fun RecommendHorizontalList(spots: List<PhotoSpot>, onSpotClick: (PhotoSpot) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(text = "Hot Spots Nearby", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NEON_PINK, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(spots) { spot ->
                Column(
                    modifier = Modifier.width(140.dp).shadow(6.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(Color.White).clickable { onSpotClick(spot) }
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFFDDDDDD)), contentAlignment = Alignment.Center) {
                        if (spot.imageUrl.isNotEmpty()) {
                            AsyncImage(model = spot.imageUrl, contentDescription = spot.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Text(text = "📍", fontSize = 24.sp)
                        }
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "★★★★★", fontSize = 12.sp, color = Color(0xFFFFD232))
                        Text(text = spot.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
