package com.yanbao.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.R

/**
 * 设计规范常量
 */
object DesignSpec {
    // 颜色
    val PrimaryPink = Color(0xFFEC4899)
    val LightPink = Color(0xFFF9A8D4)
    val PurpleLight = Color(0xFFA78BFA)
    val GradientBackground = listOf(
        Color(0xFFA78BFA),  // 紫色
        Color(0xFFEC4899),  // 粉红色
        Color(0xFFF9A8D4)   // 浅粉色
    )
    
    // 圆角
    val LargeCorner = 24.dp
    val MediumCorner = 20.dp
    val SmallCorner = 16.dp
    val TinyCorner = 12.dp
    
    // 间距（基于8dp网格）
    val Spacing2 = 2.dp
    val Spacing4 = 4.dp
    val Spacing8 = 8.dp
    val Spacing12 = 12.dp
    val Spacing16 = 16.dp
    val Spacing20 = 20.dp
    val Spacing24 = 24.dp
    val Spacing32 = 32.dp
    
    // 毛玻璃效果参数
    val GlassBlurRadius = 10.dp
    val GlassAlpha = 0.2f
}

/**
 * 粉紫渐变背景
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = DesignSpec.GradientBackground
                )
            )
    ) {
        content()
    }
}

/**
 * 毛玻璃效果卡片
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = DesignSpec.LargeCorner,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                color = Color.White.copy(alpha = DesignSpec.GlassAlpha)
            )
            .blur(DesignSpec.GlassBlurRadius)
    ) {
        content()
    }
}

/**
 * 库洛米角落装饰
 */
@Composable
fun KuromiCorners(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 左上角
        KuromiCorner(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(DesignSpec.Spacing8)
        )
        
        // 右上角
        KuromiCorner(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(DesignSpec.Spacing8)
        )
        
        // 左下角
        KuromiCorner(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(DesignSpec.Spacing8)
        )
        
        // 右下角
        KuromiCorner(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(DesignSpec.Spacing8)
        )
    }
}

/**
 * 单个库洛米角落
 */
@Composable
fun KuromiCorner(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 60.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = Color.White.copy(alpha = 0.7f),
                shape = RoundedCornerShape(DesignSpec.MediumCorner)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🩷",
            fontSize = 32.sp
        )
    }
}

/**
 * 底部导航栏
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(DesignSpec.Spacing8),
        cornerRadius = DesignSpec.MediumCorner
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSpec.Spacing8),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 编辑按钮
            BottomNavItem(
                icon = R.drawable.ic_edit,
                label = "编辑",
                isSelected = currentRoute == "edit",
                onClick = { onNavigate("edit") }
            )
            
            // 相册按钮
            BottomNavItem(
                icon = R.drawable.ic_gallery,
                label = "相册",
                isSelected = currentRoute == "gallery",
                onClick = { onNavigate("gallery") }
            )
            
            // 相机按钮
            BottomNavItem(
                icon = R.drawable.ic_camera,
                label = "相机",
                isSelected = currentRoute == "camera",
                onClick = { onNavigate("camera") }
            )
            
            // 推荐按钮
            BottomNavItem(
                icon = R.drawable.ic_recommend,
                label = "推荐",
                isSelected = currentRoute == "recommend",
                onClick = { onNavigate("recommend") }
            )
            
            // 我的按钮
            BottomNavItem(
                icon = R.drawable.ic_profile,
                label = "我的",
                isSelected = currentRoute == "profile",
                onClick = { onNavigate("profile") }
            )
        }
    }
}

/**
 * 底部导航栏单项
 */
@Composable
fun BottomNavItem(
    icon: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(DesignSpec.Spacing8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = if (isSelected) DesignSpec.PrimaryPink else Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) DesignSpec.PrimaryPink else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = DesignSpec.Spacing4)
        )
    }
}

/**
 * 搜索栏
 */
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    placeholder: String = "搜索...",
    onSearch: (String) -> Unit = {}
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(DesignSpec.Spacing16),
        cornerRadius = DesignSpec.MediumCorner
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSpec.Spacing12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "搜索",
                tint = Color.Gray,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = DesignSpec.Spacing8)
            )
            
            Text(
                text = placeholder,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 推荐卡片
 */
@Composable
fun RecommendCard(
    title: String,
    description: String,
    likes: Int = 0,
    comments: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignSpec.Spacing12)
            .clickable(onClick = onClick),
        cornerRadius = DesignSpec.LargeCorner
    ) {
        Column(
            modifier = Modifier.padding(DesignSpec.Spacing16)
        ) {
            // 用户信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = DesignSpec.Spacing12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = DesignSpec.PrimaryPink.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(DesignSpec.TinyCorner)
                        )
                )
                
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = DesignSpec.Spacing12)
                )
            }
            
            // 描述
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = DesignSpec.Spacing12)
            )
            
            // 互动数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignSpec.Spacing16)
            ) {
                Text(
                    text = "❤️ $likes",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "💬 $comments",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "📤 分享",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 分类筛选芯片
 */
@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) DesignSpec.PrimaryPink else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(DesignSpec.MediumCorner)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) DesignSpec.PrimaryPink else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(DesignSpec.MediumCorner)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = DesignSpec.Spacing12, vertical = DesignSpec.Spacing8),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
