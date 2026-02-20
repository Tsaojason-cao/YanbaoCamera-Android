package com.yanbao.camera.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 首页
 * 
 * 严格按照用户提供的图 3 设计：
 * - 顶部：品牌名 "yanbao AI" + 48dp 头像入口
 * - 中部：四宫格功能卡片（拍照/编辑器/相册/设置）
 * - 底部：160dp AI 推荐位
 * - 背景：紫色流光渐变
 */
@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRecommendClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. 紫色流光背景
        PurpleFlowingBackground()
        
        // 2. 内容层
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 顶部：品牌名 + 头像入口
            TopBar(onProfileClick = onProfileClick)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 四宫格功能卡片
            MainFeatureGrid(
                onCameraClick = onCameraClick,
                onEditorClick = onEditorClick,
                onGalleryClick = onGalleryClick,
                onSettingsClick = onSettingsClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // AI 推荐位
            AIRecommendationBanner(
                onRecommendClick = onRecommendClick,
                modifier = Modifier.height(160.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * 顶部栏：品牌名 + 48dp 头像入口
 */
@Composable
fun TopBar(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 品牌名 "yanbao AI"
        Text(
            text = "yanbao AI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color(0xFFEC4899),
                    offset = Offset(0f, 4f),
                    blurRadius = 12f
                )
            )
        )
        
        // 48dp 头像入口
        TopUserAction(onProfileClick = onProfileClick)
    }
}

/**
 * 48dp 头像入口
 * 
 * 点击后跳转"我的"页面
 */
@Composable
fun TopUserAction(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp) // 严格按照图 3 标注尺寸
            .clip(CircleShape)
            .border(2.dp, Color(0xFFE0B0FF), CircleShape) // 粉紫色发光描边
            .clickable { onProfileClick() }
            .background(Color(0xFF2A2A2A)),
        contentAlignment = Alignment.Center
    ) {
        // 将来集成 ProfileViewModel 加载真实头像
        Text(
            text = "👤",
            fontSize = 24.sp
        )
    }
}

/**
 * 四宫格功能卡片
 */
@Composable
fun MainFeatureGrid(
    onCameraClick: () -> Unit,
    onEditorClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 拍照 (Camera)
        item {
            FeatureCard(
                title = "拍照 (Camera)",
                subtitle = "Capture moments",
                icon = "📷",
                backgroundColor = Color(0xFFD4B0FF),
                onClick = onCameraClick
            )
        }
        
        // 编辑器 (Editor)
        item {
            FeatureCard(
                title = "编辑器 (Editor)",
                subtitle = "Create magic",
                icon = "✨",
                backgroundColor = Color(0xFFC0A0FF),
                onClick = onEditorClick
            )
        }
        
        // 相册 (Gallery)
        item {
            FeatureCard(
                title = "相册 (Gallery)",
                subtitle = "View memories",
                icon = "🖼️",
                backgroundColor = Color(0xFFB090FF),
                onClick = onGalleryClick
            )
        }
        
        // 设置 (Settings)
        item {
            FeatureCard(
                title = "设置 (Settings)",
                subtitle = "Customize app",
                icon = "⚙️",
                backgroundColor = Color(0xFFA080FF),
                onClick = onSettingsClick
            )
        }
    }
}

/**
 * 功能卡片
 */
@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 图标
                Text(
                    text = icon,
                    fontSize = 48.sp
                )
                
                // 标题和副标题
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 右下角库洛米装饰
            Text(
                text = "🐰",
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

/**
 * AI 推荐位（160dp）
 */
@Composable
fun AIRecommendationBanner(
    onRecommendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRecommendClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0B0FF).copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AI 推荐 (20sp)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Personalized for you",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            // 左右库洛米装饰
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "🐰", fontSize = 32.sp)
                Text(text = "🐰", fontSize = 32.sp)
            }
        }
    }
}

/**
 * 紫色流光背景
 * 
 * 动态渐变色在 45° 方向缓慢位移
 */
@Composable
fun PurpleFlowingBackground(modifier: Modifier = Modifier) {
    var offset by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // 60 FPS
            offset = (offset + 0.5f) % 1000f
        }
    }
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // 绘制流光渐变背景
        val gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFFA78BFA), // 浅紫色
                Color(0xFFEC4899), // 粉色
                Color(0xFFA78BFA)  // 浅紫色
            ),
            start = Offset(offset, offset),
            end = Offset(size.width + offset, size.height + offset)
        )
        
        drawRect(brush = gradient)
        
        // 绘制星星装饰
        val stars = listOf(
            Offset(size.width * 0.1f, size.height * 0.1f),
            Offset(size.width * 0.3f, size.height * 0.2f),
            Offset(size.width * 0.7f, size.height * 0.15f),
            Offset(size.width * 0.9f, size.height * 0.3f),
            Offset(size.width * 0.2f, size.height * 0.8f),
            Offset(size.width * 0.8f, size.height * 0.85f)
        )
        
        stars.forEach { starPos ->
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = 4f,
                center = starPos
            )
        }
    }
}
