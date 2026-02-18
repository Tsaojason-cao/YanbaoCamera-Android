package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 个人中心屏幕 - 完全匹配设计图版本
 * 
 * 设计规范：
 * - 背景：粉紫渐变（#A78BFA → #EC4899 → #F9A8D4）
 * - 四个角落：库洛米角色（60×60px，70%透明度）
 * - 用户卡片：毛玻璃效果
 * - 统计框：霓虹灯效果（粉红色边框 + 发光）
 * - 功能按钮：霓虹灯效果
 * - 底部导航栏：5个按钮
 */
@Composable
fun ProfileScreen(onNavigate: (String) -> Unit = {}) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A1A5C),  // 深紫色
                        Color(0xFF6B2D7A),  // 紫色
                        Color(0xFF3D1047)   // 深紫色
                    )
                )
            )
    ) {
        // 左上库洛米（70%透明度）
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = 16.dp)
                .size(60.dp)
                .background(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🩷", fontSize = 32.sp)
        }
        
        // 右上库洛米（70%透明度）
        Box(
            modifier = Modifier
                .offset(x = (-16).dp, y = 16.dp)
                .align(Alignment.TopEnd)
                .size(60.dp)
                .background(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🩷", fontSize = 32.sp)
        }
        
        // 左下库洛米（70%透明度）
        Box(
            modifier = Modifier
                .offset(x = 16.dp, y = (-16).dp)
                .align(Alignment.BottomStart)
                .size(60.dp)
                .background(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🩷", fontSize = 32.sp)
        }
        
        // 右下库洛米（70%透明度）
        Box(
            modifier = Modifier
                .offset(x = (-16).dp, y = (-16).dp)
                .align(Alignment.BottomEnd)
                .size(60.dp)
                .background(
                    color = Color.White.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🩷", fontSize = 32.sp)
        }
        
        // 主内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)  // 为底部导航栏留出空间
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text(
                text = "YanBao AI",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
            
            // 用户头像（霓虹灯圆形）
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(
                        width = 3.dp,
                        color = Color(0xFFEC4899),
                        shape = CircleShape
                    )
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFFEC4899).copy(alpha = 0.5f),
                        spotColor = Color(0xFFEC4899).copy(alpha = 0.5f)
                    )
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .blur(5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🩷", fontSize = 60.sp)
            }
            
            // 用户名
            Text(
                text = "@Kuromi_Fan",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            
            // VIP标签
            Text(
                text = "VIP会员 👑",
                color = Color(0xFFFFD700),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // 加入时间
            Text(
                text = "加入时间：2023.10.20",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            
            // 统计框行（霓虹灯效果）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                // 拍摄数
                NeonStatBox(label = "拍摄数", value = "128")
                
                // 获赞数
                NeonStatBox(label = "获赞数", value = "3.5k")
                
                // 粉丝数
                NeonStatBox(label = "粉丝数", value = "2.1k")
            }
            
            // 功能按钮（霓虹灯效果）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // 第一行按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    NeonButton(
                        label = "⭐ 我的收藏",
                        modifier = Modifier.weight(1f)
                    )
                    NeonButton(
                        label = "🖼️ 我的相册",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 第二行按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    NeonButton(
                        label = "⚙️ 我的设置",
                        modifier = Modifier.weight(1f)
                    )
                    NeonButton(
                        label = "ℹ️ 关于",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // 底部导航栏（固定）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 首页
                IconButton(onClick = { onNavigate("home") }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "首页",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "首页",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // 拍照
                IconButton(onClick = { onNavigate("camera") }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "拍照",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "拍照",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // 推荐
                IconButton(onClick = { onNavigate("recommend") }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "推荐",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "推荐",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // 相册
                IconButton(onClick = { onNavigate("gallery") }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "相册",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "相册",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // 我的（高亮）
                IconButton(onClick = {}) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "我的",
                            tint = Color(0xFFEC4899),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "我的",
                            color = Color(0xFFEC4899),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 霓虹灯效果统计框
 */
@Composable
fun NeonStatBox(label: String, value: String) {
    Box(
        modifier = Modifier
            .size(width = 90.dp, height = 60.dp)
            .border(
                width = 2.dp,
                color = Color(0xFFEC4899),
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color(0xFFEC4899).copy(alpha = 0.4f),
                spotColor = Color(0xFFEC4899).copy(alpha = 0.4f)
            )
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color(0xFFEC4899),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * 霓虹灯效果按钮
 */
@Composable
fun NeonButton(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(48.dp)
            .border(
                width = 2.dp,
                color = Color(0xFFEC4899),
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFFEC4899).copy(alpha = 0.4f),
                spotColor = Color(0xFFEC4899).copy(alpha = 0.4f)
            )
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
