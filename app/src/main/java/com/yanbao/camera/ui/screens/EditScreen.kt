package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 编辑屏幕 - 照片编辑界面（设计图完全匹配版本）
 * 
 * 设计规范：
 * - 背景：粉紫渐变（#A78BFA → #EC4899 → #F9A8D4）
 * - 四个角落：库洛米角色（60×60px，70%透明度）
 * - 预览区域：75% 高度
 * - 控制区域：25% 高度（滑块控制）
 * - 底部导航栏：5个按钮
 * - 毛玻璃效果：所有控制面板
 */
@Composable
fun EditScreen(onNavigate: (String) -> Unit = {}) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA78BFA),  // 紫色
                        Color(0xFFEC4899),  // 粉红色
                        Color(0xFFF9A8D4)   // 浅粉色
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
        ) {
            // 预览区域（75%）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📷 照片预览",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 控制区域（25%）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .blur(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 亮度滑块
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "亮度",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.width(40.dp)
                        )
                        Slider(
                            value = 0.5f,
                            onValueChange = {},
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                    }
                    
                    // 对比度滑块
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "对比",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.width(40.dp)
                        )
                        Slider(
                            value = 0.5f,
                            onValueChange = {},
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                    }
                    
                    // 撽和度滑块
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "撽和",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.width(40.dp)
                        )
                        Slider(
                            value = 0.5f,
                            onValueChange = {},
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                    }
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
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .blur(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 编辑（高亮）
                IconButton(onClick = {}) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "编辑",
                            tint = Color(0xFFEC4899),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "编辑",
                            color = Color(0xFFEC4899),
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
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "相册",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // 相机
                IconButton(onClick = { onNavigate("camera") }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "相机",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "相机",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // 推荐
                IconButton(onClick = { onNavigate("home") }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "推荐",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "推荐",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // 我的
                IconButton(onClick = { onNavigate("profile") }) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "我的",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "我的",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
