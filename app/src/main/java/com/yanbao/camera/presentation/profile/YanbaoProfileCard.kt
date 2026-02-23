package com.yanbao.camera.presentation.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.presentation.theme.YanbaoPink
import com.yanbao.camera.presentation.theme.YanbaoPurple

/**
 * Yanbao 个人中心玻璃卡片
 * 
 * Obsidian Flux 设计方案 - Phase 3
 * 
 * 设计核心：
 * - Apple 样式的极简丝滑卡片
 * - 强调"YB"开头的身份尊贵感
 * 
 * 空间分层逻辑：
 * - 背景层：实时相机的深度模糊（70px Blur）
 * - 前景层：浮动式玻璃卡片，边框厚度仅 0.5dp
 * 
 * 视觉特征：
 * - 会员信息：ID 采用等宽字体排列（YB - 8 8 8 8 8 8）
 * - 交互动画：点击"修改背景"时，卡片翻转（300ms 丝滑过渡）
 */
@Composable
fun YanbaoProfileCard(
    uid: String,
    days: Int,
    avatarUri: String? = null,
    modifier: Modifier = Modifier
) {
    var isFlipped by remember { mutableStateOf(false) }
    
    // 翻转动画（300ms StandardEasing）
    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "card_flip"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(200.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 12f * density
            }
            .clickable { isFlipped = !isFlipped }
    ) {
        if (rotationY <= 90f) {
            // 正面：会员信息卡片
            ProfileCardFront(
                uid = uid,
                days = days,
                avatarUri = avatarUri
            )
        } else {
            // 背面：设置选项
            ProfileCardBack(
                modifier = Modifier.graphicsLayer { this.rotationY = 180f }
            )
        }
    }
}

/**
 * 个人中心卡片 - 正面
 * 
 * 粉紫渐变玻璃质感
 * UID 等宽字体排列
 */
@Composable
fun ProfileCardFront(
    uid: String,
    days: Int,
    avatarUri: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        YanbaoPink.copy(0.3f),
                        YanbaoPurple.copy(0.3f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(0.2f),
                shape = RoundedCornerShape(24.dp)
            )
            .blur(2.dp) // 轻微模糊增强玻璃质感
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 顶部：头像 + 用户名
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 可更换头像 + 库洛米粉呼吸灯效果
                val infiniteTransition = rememberInfiniteTransition(label = "breathing")
                val breathingAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "breathing_alpha"
                )
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(
                            width = 3.dp,
                            color = YanbaoPink.copy(alpha = breathingAlpha), // 库洛米粉呼吸灯
                            shape = CircleShape
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U",
                        fontSize = 32.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Yanbao User",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // UID 等宽字体排列（YB - 8 8 8 8 8 8）
                    Text(
                        text = formatUID(uid),
                        color = Color.White.copy(0.7f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace // 等宽字体
                    )
                }
            }
            
            // 底部：会员同行天数
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已同行 $days 天",
                    color = YanbaoPink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "*",
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * 个人中心卡片 - 背面
 * 
 * 设置选项（翻转后显示）
 */
@Composable
fun ProfileCardBack(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        YanbaoPurple.copy(0.3f),
                        YanbaoPink.copy(0.3f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(0.2f),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "S",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "设置选项",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "点击返回",
                color = Color.White.copy(0.7f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 格式化 UID
 * 
 * 将 UID 格式化为 "YB - 8 8 8 8 8 8" 样式
 * 
 * @param uid 原始 UID（如 "YB888888"）
 * @return 格式化后的 UID（如 "YB - 8 8 8 8 8 8"）
 */
private fun formatUID(uid: String): String {
    return if (uid.startsWith("YB") && uid.length >= 8) {
        val prefix = uid.substring(0, 2)
        val numbers = uid.substring(2).take(6)
        "$prefix - ${numbers.toCharArray().joinToString(" ")}"
    } else {
        uid
    }
}

/**
 * 个人中心完整界面
 * 
 * 背景：实时相机的深度模糊（70px Blur）
 * 前景：浮动式玻璃卡片
 */
@Composable
fun YanbaoProfileScreen(
    uid: String = "YB888888",
    days: Int = 365,
    avatarUri: String? = null,
    onBackClick: () -> Unit = {
        android.util.Log.d("YanbaoProfileScreen", "Back button clicked")
    },
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 背景层：实时相机深度模糊
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFB6C1).copy(0.5f),
                            Color(0xFFE0B0FF).copy(0.5f)
                        )
                    )
                )
                .blur(70.dp) // 深度模糊
        )
        
        // 前景层：玻璃卡片
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
        ) {
            // 顶部标题
            Text(
                text = "个人中心",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 会员卡片
            YanbaoProfileCard(
                uid = uid,
                days = days,
                avatarUri = avatarUri
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 其他设置项
            SettingsItems()
        }
        
        // 返回按钮
        androidx.compose.material3.IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text("←", fontSize = 24.sp, color = Color.White)
        }
    }
}

/**
 * 设置项列表
 */
@Composable
fun SettingsItems(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val items = listOf(
            "我的作品",
            "[*] 收藏夹",
            "通知设置",
            "主题设置",
            "ℹ️ 关于 Yanbao"
        )
        
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White.copy(0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        android.util.Log.d("YanbaoProfileCard", "菜单点击: $item")
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = item,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}
