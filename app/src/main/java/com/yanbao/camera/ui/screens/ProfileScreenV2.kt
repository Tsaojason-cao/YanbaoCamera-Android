package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.components.KuromiCorners
import com.yanbao.camera.ui.theme.AccentPink
import com.yanbao.camera.ui.theme.ButtonPrimary
import com.yanbao.camera.ui.theme.GradientEnd
import com.yanbao.camera.ui.theme.GradientMiddle
import com.yanbao.camera.ui.theme.GradientStart
import com.yanbao.camera.ui.theme.TextWhite
import com.yanbao.camera.ui.theme.glassEffect

/**
 * 个人资料屏幕 - 完整实现版本
 * 
 * 功能：
 * - 用户头像和基本信息
 * - 统计数据（照片数、粉丝数、关注数）
 * - 设置选项
 * - 库洛米装饰
 */
@Composable
fun ProfileScreenV2(
    onEditProfile: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 顶部工具栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "我的资料",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                
                IconButton(onClick = onSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "设置",
                        tint = TextWhite
                    )
                }
            }
            
            // 用户头像和基本信息
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(cornerRadius = 16)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 用户头像
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(AccentPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 48.sp
                        )
                    }
                    
                    // 用户名
                    Text(
                        text = "雁宝摄影师",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    
                    // 用户ID
                    Text(
                        text = "@yanbao_camera_2026",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    // 个人简介
                    Text(
                        text = "专业摄影爱好者 | 用雁宝记录美好时刻",
                        fontSize = 11.sp,
                        color = TextWhite.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    // 编辑资料按钮
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ButtonPrimary)
                            .clickable(onClick = onEditProfile)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "编辑",
                                tint = TextWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Text(
                                text = "编辑资料",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // 统计数据
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticCard(
                    label = "照片",
                    value = "128",
                    modifier = Modifier.weight(1f)
                )
                
                StatisticCard(
                    label = "粉丝",
                    value = "2.5K",
                    modifier = Modifier.weight(1f)
                )
                
                StatisticCard(
                    label = "关注",
                    value = "456",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 功能菜单
            Text(
                text = "功能",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            ProfileMenuItem(
                icon = "🎨",
                label = "我的作品",
                description = "查看所有上传的照片",
                onClick = {}
            )
            
            ProfileMenuItem(
                icon = "❤️",
                label = "收藏夹",
                description = "查看收藏的照片",
                onClick = {}
            )
            
            ProfileMenuItem(
                icon = "🔔",
                label = "通知",
                description = "查看最新通知",
                onClick = {}
            )
            
            ProfileMenuItem(
                icon = "⚙️",
                label = "设置",
                description = "应用设置和隐私",
                onClick = onSettings
            )
            
            ProfileMenuItem(
                icon = "ℹ️",
                label = "关于",
                description = "关于雁宝相机",
                onClick = {}
            )
            
            // 登出按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF6B6B).copy(alpha = 0.7f))
                    .clickable(onClick = onLogout)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = "登出",
                        tint = TextWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = "登出账户",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
        
        // 库洛米装饰
        KuromiCorners(
            modifier = Modifier.fillMaxSize(),
            size = 60,
            showCorners = true
        )
    }
}

/**
 * 统计卡片
 */
@Composable
fun StatisticCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .glassEffect(cornerRadius = 12)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AccentPink
            )
            
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextWhite.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 个人资料菜单项
 */
@Composable
fun ProfileMenuItem(
    icon: String,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 12)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                Column {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    
                    Text(
                        text = description,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Text(
                text = ">",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}
