package com.yanbao.camera.ui.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log

/**
 * 美颜模式界面
 * 设计图：03_camera_beauty.png
 * 
 * 关键元素：
 * - 顶部标题："相机-一键美颜"
 * - 左上角：库洛米吉祥物
 * - 右上角：设置图标
 * - 预览区：实时人脸美颜效果
 * - 滤镜选择栏：6 种滤镜（自然、柔光、粉嫩、冷白、暖阳、清绿）
 * - 底部按钮："应用美颜"（粉紫渐变）
 */
@Composable
fun BeautyModeScreen(
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onApplyBeauty: (BeautyFilter) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(BeautyFilter.NATURAL) }
    
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
            BeautyTopBar(
                onBackClick = onBackClick,
                onSettingsClick = onSettingsClick
            )
            
            // 相机预览区（占据大部分空间）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                // 这里应该是真实的相机预览
                // 暂时用占位文字代替
                Text(
                    text = "相机预览区\n（实时美颜效果）",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light
                )
            }
            
            // 滤镜选择栏
            BeautyFilterSelector(
                selectedFilter = selectedFilter,
                onFilterSelected = { filter ->
                    selectedFilter = filter
                    Log.d("BeautyMode", "选中滤镜: ${filter.displayName}")
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 底部"应用美颜"按钮
            Button(
                onClick = {
                    onApplyBeauty(selectedFilter)
                    Log.d("BeautyMode", "应用美颜: ${selectedFilter.displayName}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFEC4899),
                                    Color(0xFFA78BFA)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "应用美颜",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        // 库洛米吉祥物装饰（四角）
        KuromiDecorations()
    }
}

/**
 * 顶部控制栏
 */
@Composable
fun BeautyTopBar(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：库洛米吉祥物
        Text(
            text = "🐰",
            fontSize = 40.sp,
            modifier = Modifier.clickable(onClick = onBackClick)
        )
        
        // 中间：标题
        Text(
            text = "相机-一键美颜",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        // 右侧：设置图标
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "设置",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * 滤镜选择器
 */
@Composable
fun BeautyFilterSelector(
    selectedFilter: BeautyFilter,
    onFilterSelected: (BeautyFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(16.dp)
    ) {
        // 滤镜名称栏（横向滚动）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BeautyFilter.values().forEach { filter ->
                Text(
                    text = filter.displayName,
                    fontSize = 16.sp,
                    fontWeight = if (filter == selectedFilter) FontWeight.Bold else FontWeight.Normal,
                    color = if (filter == selectedFilter) Color(0xFFEC4899) else Color.White,
                    modifier = Modifier
                        .clickable { onFilterSelected(filter) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 滤镜缩略图（横向滚动）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BeautyFilter.values().forEach { filter ->
                BeautyFilterThumbnail(
                    filter = filter,
                    isSelected = filter == selectedFilter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
    }
}

/**
 * 滤镜缩略图
 */
@Composable
fun BeautyFilterThumbnail(
    filter: BeautyFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        // 缩略图（圆角矩形）
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(filter.previewColor)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) Color(0xFFEC4899) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // 这里应该是真实的滤镜预览图
            // 暂时用颜色代替
            Text(
                text = filter.emoji,
                fontSize = 32.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 滤镜名称
        Text(
            text = filter.displayName,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFEC4899) else Color.White
        )
    }
}

/**
 * 库洛米吉祥物装饰
 */
@Composable
fun BoxScope.KuromiDecorations() {
    // 左上角
    Text(
        text = "🐰",
        fontSize = 60.sp,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
    )
    
    // 右上角
    Text(
        text = "🎀",
        fontSize = 50.sp,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
    )
    
    // 左下角
    Text(
        text = "🐰",
        fontSize = 60.sp,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp)
    )
    
    // 右下角
    Text(
        text = "🐰",
        fontSize = 60.sp,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
    )
}

/**
 * 美颜滤镜枚举
 */
enum class BeautyFilter(
    val displayName: String,
    val emoji: String,
    val previewColor: Color
) {
    NATURAL("自然", "🌸", Color(0xFFFFC0CB)),
    SOFT_LIGHT("柔光", "✨", Color(0xFFFFE4E1)),
    PINK("粉嫩", "💗", Color(0xFFFFB6C1)),
    COOL_WHITE("冷白", "❄️", Color(0xFFE0F7FA)),
    WARM("暖阳", "☀️", Color(0xFFFFE082)),
    FRESH_GREEN("清绿", "🌿", Color(0xFFC8E6C9))
}
