package com.yanbao.camera.presentation.gallery

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.yanbao.camera.presentation.theme.YanbaoPink
import com.yanbao.camera.presentation.theme.YanbaoPurple

/**
 * Yanbao Memory Detail Screen - Specification v1.0
 * 
 * 雁宝记忆詳情頁
 * 
 * 核心：動態高斯模糊背景 + 29D 参数玻璃面板
 * 
 * 空間分層邏輯：
 * - Layer 1（底層）：80dp 高斯模糊動態背景
 * - Layer 2（中層）：全屏高清相片（支持雙指縮放）
 * - Layer 3（頂層）：底部 30% 曜石黑玻璃面板
 * 
 * 设计规范：
 * - 参数标签：10sp 灰白色 (#888888)
 * - 参数數值：16sp 粉色 (#FFB6C1) + Monospace
 * - 手勢：左右滑动切換相片，上下滑动隱藏/显示面板
 */
@Composable
fun YanbaoMemoryDetailScreen(
    photoUrl: String,
    location: String = "Shibuya, Tokyo",
    date: String = "2026.02.21",
    iso: String = "100",
    shutter: String = "1/250",
    aperture: String = "f/1.4",
    focus: String = "35mm",
    color29D: String = "+1.2",
    onBackClick: () -> Unit = {
        android.util.Log.d("YanbaoMemoryDetailScreen", "Back button clicked")
    },
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var isPanelVisible by remember { mutableStateOf(true) }
    
    // UI 淡出動畫（縮放時）
    val uiAlpha by animateFloatAsState(
        targetValue = if (scale > 1.2f) 0f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "ui_alpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        
        // Layer 1: 動態主色調模糊背景
        Image(
            painter = rememberAsyncImagePainter(model = photoUrl),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.6f
        )
        
        // Layer 2: 高清相片展示區（支持縮放交互）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = photoUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                contentScale = ContentScale.Fit
            )
        }
        
        // Layer 3: 底部 30% 專業参数控制艙
        if (isPanelVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f) // 嚴格 30% 佔比
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color(0xCC0A0A0A)) // 曜石黑半透明
                    .blur(30.dp)
                    .padding(20.dp)
                    .alpha(uiAlpha)
            ) {
                // 顶部：拍攝地點與日期
                LocationDateHeader(
                    location = location,
                    date = date
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 核心：29D 物理参数橫向捲軸
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { ParameterItem("ISO", iso) }
                    item { ParameterItem("Shutter", shutter) }
                    item { ParameterItem("Aperture", aperture) }
                    item { ParameterItem("Focus", focus) }
                    item { ParameterItem("29D-Color", color29D) }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 底部：Git 備份與 LBS 分享按鈕
                ActionButtonsRow()
            }
        }
        
        // 顶部返回按鈕
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .alpha(uiAlpha)
        ) {
            Text("←", fontSize = 24.sp, color = Color.White)
        }
        
        // Git 備份標識（右上角）
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(32.dp)
                .background(YanbaoPink.copy(alpha = 0.3f), CircleShape)
                .alpha(uiAlpha),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = YanbaoPink,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 拍攝地點與日期頭部
 */
@Composable
fun LocationDateHeader(
    location: String,
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 地點
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "📍",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = location,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 日期
        Text(
            text = date,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * 29D 参数項目
 * 
 * 類徕卡字體排版
 * 
 * @param label 参数标签（如 "ISO"）
 * @param value 参数數值（如 "100"）
 */
@Composable
fun ParameterItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 参数标签：10sp 灰白色 (#888888)
        Text(
            text = label,
            color = Color(0xFF888888),
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 参数數值：16sp 粉色 (#FFB6C1) + Monospace
        Text(
            text = value,
            color = YanbaoPink,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace // 等寬字體
        )
    }
}

/**
 * 操作按鈕行
 * 
 * Git 備份與 LBS 分享
 */
@Composable
fun ActionButtonsRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Git 備份按鈕
        ActionButton(
            icon = "☁️",
            label = "Git Backup",
            onClick = { /* Git 備份邏輯 */ }
        )
        
        // LBS 分享按鈕
        ActionButton(
            icon = "📍",
            label = "LBS Share",
            onClick = { /* LBS 分享邏輯 */ }
        )
        
        // 编辑按鈕
        ActionButton(
            icon = "✏️",
            label = "Edit",
            onClick = { /* 编辑邏輯 */ }
        )
    }
}

/**
 * 操作按鈕
 */
@Composable
fun ActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}
