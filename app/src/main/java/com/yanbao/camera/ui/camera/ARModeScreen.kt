package com.yanbao.camera.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log

/**
 * AR 特效模式界面
 * 设计图：05_camera_ar.png
 * 
 * 关键元素：
 * - 顶部控制栏：设置、翻转、比例、定时器、更多
 * - 标题："AR特效模式"
 * - 预览区：实时人脸识别 + AR 贴纸叠加
 * - 底部 Tab：贴纸（选中）、效果、动画、特效
 * - 贴纸选择器：横向滚动，6 种贴纸
 * - 显示 "Total AR Effects"
 */
@Composable
fun ARModeScreen(
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onStickerApplied: (ARSticker) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(ARTab.STICKER) }
    var selectedSticker by remember { mutableStateOf<ARSticker?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFC0CB), // 粉色
                        Color(0xFFE6E6FA)  // 淡紫色
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部控制栏
            ARTopBar(
                onBackClick = onBackClick,
                onSettingsClick = onSettingsClick
            )
            
            // 相机预览区（带 AR 效果）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                // 这里应该是真实的相机预览 + AR 贴纸渲染
                // 暂时用占位文字代替
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "AR 相机预览区",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedSticker != null) {
                        Text(
                            text = "当前贴纸: ${selectedSticker!!.emoji}",
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    }
                }
                
                // 库洛米吉祥物装饰（四角）
                ARKuromiDecorations()
            }
            
            // 底部控制面板
            ARBottomPanel(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    Log.d("ARMode", "切换 Tab: ${tab.displayName}")
                },
                selectedSticker = selectedSticker,
                onStickerSelected = { sticker ->
                    selectedSticker = sticker
                    onStickerApplied(sticker)
                    Log.d("ARMode", "选中贴纸: ${sticker.displayName}")
                }
            )
        }
    }
}

/**
 * 顶部控制栏
 */
@Composable
fun ARTopBar(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：库洛米吉祥物 + 设置
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🐰",
                    fontSize = 40.sp,
                    modifier = Modifier.clickable(onClick = onBackClick)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = Color.White
                    )
                }
            }
            
            // 中间：标题
            Text(
                text = "AR特效模式",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // 右侧：翻转、比例、定时器、更多
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {}) {
                    Text(text = "📷", fontSize = 20.sp)
                }
                IconButton(onClick = {}) {
                    Text(text = "4:3", fontSize = 14.sp, color = Color.White)
                }
                IconButton(onClick = {}) {
                    Text(text = "3s", fontSize = 14.sp, color = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 底部控制面板
 */
@Composable
fun ARBottomPanel(
    selectedTab: ARTab,
    onTabSelected: (ARTab) -> Unit,
    selectedSticker: ARSticker?,
    onStickerSelected: (ARSticker) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(16.dp)
    ) {
        // Tab 栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ARTab.values().forEach { tab ->
                Text(
                    text = tab.displayName,
                    fontSize = 16.sp,
                    fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Normal,
                    color = if (tab == selectedTab) Color(0xFFEC4899) else Color.White,
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 贴纸选择器（横向滚动）
        when (selectedTab) {
            ARTab.STICKER -> {
                ARStickerSelector(
                    selectedSticker = selectedSticker,
                    onStickerSelected = onStickerSelected
                )
            }
            else -> {
                // 其他 Tab 的内容（暂未实现）
                Text(
                    text = "${selectedTab.displayName} 功能开发中...",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // "Total AR Effects" 提示
        Text(
            text = "Total AR Effects",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

/**
 * 贴纸选择器
 */
@Composable
fun ARStickerSelector(
    selectedSticker: ARSticker?,
    onStickerSelected: (ARSticker) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ARSticker.values().forEach { sticker ->
            ARStickerItem(
                sticker = sticker,
                isSelected = sticker == selectedSticker,
                onClick = { onStickerSelected(sticker) }
            )
        }
    }
}

/**
 * 贴纸项
 */
@Composable
fun ARStickerItem(
    sticker: ARSticker,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // 贴纸图标（圆角矩形）
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) Color(0xFFEC4899) else Color.White.copy(alpha = 0.2f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = sticker.emoji,
                    fontSize = 40.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 贴纸名称
        Text(
            text = sticker.displayName,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFEC4899) else Color.White
        )
    }
}

/**
 * AR 库洛米装饰
 */
@Composable
fun BoxScope.ARKuromiDecorations() {
    // 左上角
    Text(
        text = "🐰",
        fontSize = 50.sp,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
    )
    
    // 右上角
    Text(
        text = "🐰",
        fontSize = 50.sp,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
    )
    
    // 左下角
    Text(
        text = "🐰",
        fontSize = 50.sp,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
    )
    
    // 右下角
    Text(
        text = "🐰",
        fontSize = 50.sp,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
    )
    
    // 中间装饰（爱心、星星等）
    Text(
        text = "💜",
        fontSize = 30.sp,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 100.dp, start = 50.dp)
    )
    
    Text(
        text = "✨",
        fontSize = 25.sp,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 150.dp, end = 80.dp)
    )
}

/**
 * AR Tab 枚举
 */
enum class ARTab(val displayName: String) {
    STICKER("贴纸"),
    EFFECT("效果"),
    ANIMATION("动画"),
    SPECIAL("特效")
}

/**
 * AR 贴纸枚举
 */
enum class ARSticker(
    val displayName: String,
    val emoji: String
) {
    KUROMI("酷洛米", "🐰"),
    HEART("爱心", "❤️"),
    BOW("蝴蝶结", "🎀"),
    CROWN("皇冠", "👑"),
    SPARKLE("闪烁", "✨"),
    SKULL("骷髅", "💀")
}
