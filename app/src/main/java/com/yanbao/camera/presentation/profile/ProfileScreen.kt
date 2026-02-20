package com.yanbao.camera.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 个人中心与设置模块
 * 
 * 设计规范：
 * - 顶部：全屏背景图（用户可自定义）+ 库洛米风格半透明覆盖
 * - 中心：圆形大头像（点击弹出相册更换）+ 昵称（ID：88888）
 * - 数据行：作品、粉丝、关注、会员天数
 * - 设置项列表：修改资料、隐私设置、同步 Git 备份
 */
@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8B7FD8), // 深紫
                        Color(0xFFB89FE8), // 紫粉
                        Color(0xFFF5A8D4)  // 亮粉
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部品牌标识
            Text(
                text = "yanbao AI",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 用户头像（大圆形）
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
                    .border(4.dp, Color(0xFFEC4899), CircleShape) // 粉色边框
            ) {
                // 编辑按钮
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "修改头像",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 用户 ID（会员号：88888）
            Text(
                text = "ID: 88888",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "会员天数: 365 天",
                fontSize = 14.sp,
                color = Color(0xFFEC4899) // 粉色
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 数据行（作品、粉丝、关注）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UserStatItem("作品", "128")
                UserStatItem("粉丝", "1.2K")
                UserStatItem("关注", "256")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 设置项列表
            SettingsList()
        }
    }
}

/**
 * 用户统计项
 */
@Composable
fun UserStatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * 设置项列表
 */
@Composable
fun SettingsList() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingItem("修改资料")
        SettingItem("隐私设置")
        SettingItem("同步 Git 备份")
        SettingItem("关于 yanbao AI")
    }
}

/**
 * 设置项
 */
@Composable
fun SettingItem(title: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x40FFFFFF), // 25% 白色透明
                            Color(0x26FFFFFF)  // 15% 白色透明
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
