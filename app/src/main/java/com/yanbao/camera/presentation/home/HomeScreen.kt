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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yanbao.camera.core.camera.Camera2PreviewManager
import com.yanbao.camera.presentation.recommend.PhotoSpot
import com.yanbao.camera.presentation.recommend.RecommendViewModel
import kotlinx.coroutines.launch

/**
 * HomeScreen — 严格对标 04_home_screen.png（去手机壳边框）
 *
 * 视觉规范：
 * - 背景：粉色渐变 #F5A0C0 → #FFD0E8
 * - 顶部：天气卡片（白色半透明圆角）
 * - 库洛米 Banner：深紫 #371464 圆角矩形 + 粉色霓虹边框
 * - 四宫格：Camera(紫)/Edit(粉红)/Recommend(金)/Gallery(青蓝) 圆形按钮
 * - Recent Activity：2张横向卡片
 * - Hot Spots：3张横向卡片（来自 RecommendViewModel）
 * - 底部导航：5标签（Home/Discover/Capture/Social/Profile）
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5A0C0), Color(0xFFFFD0E8))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(44.dp))
            WeatherCard()
            Spacer(modifier = Modifier.height(10.dp))
            KuromiBanner()
            Spacer(modifier = Modifier.height(16.dp))
            QuickAccessGrid(
                onCameraClick = onCameraClick,
                onEditorClick = onEditorClick,
                onRecommendClick = onRecommendClick,
                onGalleryClick = onGalleryClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            RecentActivitySection()
            Spacer(modifier = Modifier.height(16.dp))
            HotSpotsSection(
                spots = spots,
                onSpotClick = { spot ->
                    Log.d("HomeScreen", "Hot Spot: ${spot.title}")
                    onRecommendClick()
                }
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = {},
            onDiscoverClick = onRecommendClick,
            onCaptureClick = onCameraClick,
            onSocialClick = {},
            onProfileClick = onProfileClick
        )
    }
}

@Composable
private fun WeatherCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xCCFFFFFF))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Good morning!  28\u00b0C", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF50285A))
                Text(text = "Perfect for outdoor shooting", fontSize = 13.sp, color = Color(0xFF78406A))
            }
            Text(text = "\u2600\uFE0F", fontSize = 28.sp)
        }
    }
}

@Composable
private fun KuromiBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF371464))
            .border(BorderStroke(2.dp, Color(0xFFEC4899)), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val pink = Color(0xFFEC4899)
            for (i in 1..4) {
                drawCircle(color = pink.copy(alpha = 0.08f * i), radius = 60f * i, center = center)
            }
            val heartPositions = listOf(
                Pair(size.width * 0.15f, size.height * 0.3f),
                Pair(size.width * 0.85f, size.height * 0.3f),
                Pair(size.width * 0.10f, size.height * 0.7f),
                Pair(size.width * 0.90f, size.height * 0.7f),
            )
            for ((hx, hy) in heartPositions) {
                val offset = androidx.compose.ui.geometry.Offset(hx, hy)
                drawCircle(color = pink.copy(alpha = 0.6f), radius = 8f, center = offset)
                drawCircle(color = pink.copy(alpha = 0.3f), radius = 14f, center = offset, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
            }
        }
        Text(text = "\u2736 \u2736 \u2736", modifier = Modifier.align(Alignment.TopEnd).padding(12.dp), fontSize = 12.sp, color = Color(0xFFFFD232))
    }
}

private data class QuickItem(val label: String, val colorInner: Color, val colorOuter: Color, val onClick: () -> Unit)

@Composable
private fun QuickAccessGrid(
    onCameraClick: () -> Unit, onEditorClick: () -> Unit,
    onRecommendClick: () -> Unit, onGalleryClick: () -> Unit
) {
    val items = listOf(
        QuickItem("Camera",    Color(0xFF7840C8), Color(0xFFB478F0), onCameraClick),
        QuickItem("Edit",      Color(0xFFDC3C78), Color(0xFFF064A0), onEditorClick),
        QuickItem("Recommend", Color(0xFFC8A020), Color(0xFFF0C840), onRecommendClick),
        QuickItem("Gallery",   Color(0xFF2890DC), Color(0xFF50C0F0), onGalleryClick),
    )
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        items.forEach { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { item.onClick() }) {
                Box(
                    modifier = Modifier.size(64.dp).background(
                        Brush.radialGradient(colors = listOf(item.colorOuter.copy(alpha = 0.3f), Color.Transparent), radius = 48f), CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape).background(item.colorInner).border(2.dp, item.colorOuter, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = when (item.label) { "Camera" -> "\uD83D\uDCF7"; "Edit" -> "\u2728"; "Recommend" -> "\uD83D\uDCA1"; else -> "\uD83D\uDDBC" }, fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = item.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF50285A))
            }
        }
    }
}

@Composable
private fun RecentActivitySection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Recent Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActivityCard(timeLabel = "2 hours ago", bgColors = listOf(Color(0xFFB47850), Color(0xFF8060A0)), modifier = Modifier.weight(1f))
            ActivityCard(timeLabel = "Yesterday",   bgColors = listOf(Color(0xFFC8A078), Color(0xFF906878)), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ActivityCard(timeLabel: String, bgColors: List<Color>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(100.dp).clip(RoundedCornerShape(14.dp)).background(Brush.verticalGradient(colors = bgColors))) {
        Box(modifier = Modifier.fillMaxWidth().height(40.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xCC000000)))))
        Box(modifier = Modifier.size(24.dp).align(Alignment.BottomStart).padding(start = 6.dp, bottom = 6.dp).clip(CircleShape).background(Color(0xFF7040A0)).border(1.dp, Color(0xFFEC4899), CircleShape))
        Text(text = timeLabel, modifier = Modifier.align(Alignment.BottomStart).padding(start = 34.dp, bottom = 8.dp), fontSize = 10.sp, color = Color.White)
    }
}

@Composable
private fun HotSpotsSection(spots: List<PhotoSpot>, onSpotClick: (PhotoSpot) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Hot Spots", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 10.dp))
        val displaySpots = if (spots.isNotEmpty()) spots.take(6) else listOf(
            PhotoSpot(id = "1", title = "Kuromi Cafe",         imageUrl = "", distance = 1.5, photoCount = 128),
            PhotoSpot(id = "2", title = "Cherry Blossom Park", imageUrl = "", distance = 2.5, photoCount = 256),
            PhotoSpot(id = "3", title = "Neon Street",         imageUrl = "", distance = 1.5, photoCount = 89),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(end = 8.dp)) {
            items(displaySpots) { spot -> HotSpotCard(spot = spot, onClick = { onSpotClick(spot) }) }
        }
    }
}

@Composable
private fun HotSpotCard(spot: PhotoSpot, onClick: () -> Unit) {
    val bgColors = when {
        spot.title.contains("Cafe", ignoreCase = true)    -> listOf(Color(0xFF6428A0), Color(0xFF3C1464))
        spot.title.contains("Blossom", ignoreCase = true) -> listOf(Color(0xFFC8A0A8), Color(0xFF906878))
        else                                               -> listOf(Color(0xFF143C78), Color(0xFF0A1E50))
    }
    Box(modifier = Modifier.width(110.dp).height(110.dp).clip(RoundedCornerShape(12.dp)).background(Brush.verticalGradient(colors = bgColors)).clickable(onClick = onClick)) {
        if (spot.imageUrl.isNotEmpty()) {
            AsyncImage(model = spot.imageUrl, contentDescription = spot.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xDD000000)))))
        Text(text = spot.title, modifier = Modifier.align(Alignment.BottomStart).padding(start = 6.dp, bottom = 20.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
        if (spot.distance != null) {
            Row(modifier = Modifier.align(Alignment.BottomStart).padding(start = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.size(5.dp).background(Color(0xFFEC4899), CircleShape))
                Text(text = "${"%.1f".format(spot.distance)} km", fontSize = 9.sp, color = Color(0xFFDDDDDD))
            }
        }
    }
}

@Composable
private fun BottomNavBar(modifier: Modifier = Modifier, onHomeClick: () -> Unit, onDiscoverClick: () -> Unit, onCaptureClick: () -> Unit, onSocialClick: () -> Unit, onProfileClick: () -> Unit) {
    Box(modifier = modifier.fillMaxWidth().height(72.dp).background(Color(0xF0FFF0F8)).border(BorderStroke(1.dp, Color(0x30DC78A8)), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))) {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            NavItem(label = "Home",     icon = "\uD83D\uDC31", isSelected = true,  onClick = onHomeClick)
            NavItem(label = "Discover", icon = "\uD83D\uDD0D", isSelected = false, onClick = onDiscoverClick)
            Box(modifier = Modifier.size(56.dp).offset(y = (-12).dp).clip(CircleShape).background(Color(0xFF7840C8)).border(3.dp, Color.White, CircleShape).clickable(onClick = onCaptureClick), contentAlignment = Alignment.Center) {
                Text(text = "\uD83D\uDC31", fontSize = 24.sp)
            }
            NavItem(label = "Social",  icon = "\uD83D\uDCAC", isSelected = false, onClick = onSocialClick)
            NavItem(label = "Profile", icon = "\uD83D\uDC31", isSelected = false, onClick = onProfileClick)
        }
    }
}

@Composable
private fun NavItem(label: String, icon: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color(0xFFEC4899) else Color(0xFF8C6478)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 8.dp)) {
        Text(text = icon, fontSize = 20.sp)
        Text(text = label, fontSize = 10.sp, color = color, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun CameraPreviewComponent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val previewManager = remember { Camera2PreviewManager(context) }
    DisposableEffect(Unit) { onDispose { previewManager.release() } }
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) { scope.launch { previewManager.openCamera(holder.surface) } }
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
                Box(modifier = Modifier.background(Color(0xCC000000), RoundedCornerShape(4.dp)).border(0.5.dp, Color(0xFFEC4899), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(text = param, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFEC4899))
                }
            }
        }
    }
}

@Composable
fun YanbaoTopBar(avatarUri: String? = null, onAvatarClick: () -> Unit, onSettingsClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp).background(Color(0xFF0A0A0A)).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).border(1.5.dp, Color(0xFFEC4899), CircleShape).clip(CircleShape).background(Color(0xFF1A1A1A)).clickable { onAvatarClick() }, contentAlignment = Alignment.Center) {
            if (avatarUri != null) {
                AsyncImage(model = avatarUri, contentDescription = "头像", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(text = "Y", fontFamily = FontFamily.Monospace, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
            }
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(text = "yanbao AI", fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
fun RecommendHorizontalList(spots: List<PhotoSpot>, onSpotClick: (PhotoSpot) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(text = "Hot Spots Nearby", fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899), modifier = Modifier.padding(bottom = 8.dp))
        if (spots.isEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(4) {
                    Box(modifier = Modifier.width(140.dp).height(100.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF1A1A1A)), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFEC4899), modifier = Modifier.size(20.dp), strokeWidth = 1.5.dp)
                    }
                }
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(spots) { spot -> HotSpotCard(spot = spot, onClick = { onSpotClick(spot) }) }
            }
        }
    }
}
