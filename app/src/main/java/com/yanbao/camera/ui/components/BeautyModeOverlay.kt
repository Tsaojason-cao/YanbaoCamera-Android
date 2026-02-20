package com.yanbao.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 美颜模式叠加层
 * 显示在相机预览之上，提供滤镜选择功能
 */
@Composable
fun BeautyModeOverlay(
    modifier: Modifier = Modifier,
    onFilterSelected: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("自然") }
    
    val filters = listOf(
        "自然" to Color(0xFFFFE5E5),
        "柔光" to Color(0xFFFFF0E5),
        "粉嫩" to Color(0xFFFFD5E5),
        "冷白" to Color(0xFFE5F5FF),
        "暖阳" to Color(0xFFFFE5D5),
        "清绿" to Color(0xFFE5FFE5)
    )
    
    Box(
        modifier = modifier
    ) {
        // 底部滤镜选择器
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 200.dp) // 留出空间给底部操作栏
        ) {
            // 滤镜标题
            Text(
                text = "美颜滤镜",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 24.dp, bottom = 12.dp)
            )
            
            // 滤镜横向滚动列表
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.6f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                items(filters) { (name, color) ->
                    FilterItem(
                        name = name,
                        color = color,
                        isSelected = name == selectedFilter,
                        onClick = {
                            selectedFilter = name
                            onFilterSelected(name)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterItem(
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // 滤镜预览圆形
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    brush = if (isSelected) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFA78BFA),
                                Color(0xFFEC4899)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(color, color)
                        )
                    }
                )
                .padding(if (isSelected) 3.dp else 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(color)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 滤镜名称
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFEC4899) else Color.White
        )
    }
}
