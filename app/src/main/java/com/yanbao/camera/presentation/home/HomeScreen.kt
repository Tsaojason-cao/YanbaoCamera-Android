package com.yanbao.camera.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 首页逻辑中转站
 * 
 * 功能：
 * 1. 开始拍摄 → 进入相机模块
 * 2. 雁宝记忆 → 查看雁宝记忆相册
 * 3. 推荐 → 查看 LBS 推荐
 */
@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit = {},
    onNavigateToMemories: () -> Unit = {},
    onNavigateToRecommendations: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D), // 顶部：深黑色
                        Color(0xFF1A1A1A)  // 底部：浅黑色
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 标题
            Text(
                text = "yanbao AI",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            // 开始拍摄
            HomeCard(
                title = "开始拍摄",
                subtitle = "进入相机模块",
                icon = Icons.Default.CameraAlt,
                gradientColors = listOf(
                    Color(0xFFEC4899), // 粉色
                    Color(0xFFA78BFA)  // 紫色
                ),
                onClick = onNavigateToCamera
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 雁宝记忆
            HomeCard(
                title = "雁宝记忆",
                subtitle = "查看您的专属记忆",
                icon = Icons.Default.PhotoLibrary,
                gradientColors = listOf(
                    Color(0xFF6366F1), // 蓝紫色
                    Color(0xFF8B5CF6)  // 紫色
                ),
                onClick = onNavigateToMemories
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 推荐
            HomeCard(
                title = "推荐",
                subtitle = "基于位置的参数推荐",
                icon = Icons.Default.Explore,
                gradientColors = listOf(
                    Color(0xFF10B981), // 绿色
                    Color(0xFF059669)  // 深绿色
                ),
                onClick = onNavigateToRecommendations
            )
        }
    }
}

/**
 * 首页卡片
 */
@Composable
fun HomeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = gradientColors
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // 文字
                Column {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
