package com.yanbao.camera.presentation.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.presentation.theme.YanbaoPink
import com.yanbao.camera.presentation.theme.YanbaoPurple

/**
 * Profile 编辑界面
 * 
 * 1:1 还原 settings_16_profile_edit.jpg
 * 
 * 核心功能：
 * - 背景墙选择器：HorizontalPager 横向滑动
 * - 当前选中项：粉色发光边框（3dp，alpha 0.8）
 * - 所有列表项高度 ≥ 48dp
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileEditScreen(
    onBackClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 背景层：70px 高斯模糊
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
                .blur(70.dp)
        )
        
        // 前景层：编辑内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, start = 20.dp, end = 20.dp)
        ) {
            // 顶部标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.clickable { onBackClick(); onBack() }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "yanbao AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 背景墙选择器
            Text(
                text = "背景墙",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BackgroundWallSelector()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 其他编辑项
            EditItems()
        }
    }
}

/**
 * 背景墙选择器
 * 
 * HorizontalPager 横向滑动
 * 当前选中项：粉色发光边框
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BackgroundWallSelector(
    modifier: Modifier = Modifier
) {
    val backgrounds = listOf(
        "渐变1" to listOf(Color(0xFFFFB6C1), Color(0xFFE0B0FF)),
        "渐变2" to listOf(Color(0xFFFF69B4), Color(0xFF9370DB)),
        "渐变3" to listOf(Color(0xFFFFDAB9), Color(0xFFFFB6C1)),
        "渐变4" to listOf(Color(0xFF87CEEB), Color(0xFF9370DB)),
        "渐变5" to listOf(Color(0xFF98FB98), Color(0xFF87CEEB))
    )
    
    val pagerState = rememberPagerState(pageCount = { backgrounds.size })
    
    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp
        ) { page ->
            val (name, colors) = backgrounds[page]
            val isSelected = pagerState.currentPage == page
            
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(160.dp)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 3.dp,
                                color = YanbaoPink.copy(alpha = 0.8f), // 粉色发光边框
                                shape = RoundedCornerShape(16.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(if (isSelected) 4.dp else 0.dp)
                    .background(
                        brush = Brush.verticalGradient(colors),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 页面指示器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(backgrounds.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 10.dp else 6.dp)
                        .background(
                            color = if (pagerState.currentPage == index) {
                                YanbaoPink
                            } else {
                                Color.White.copy(0.3f)
                            },
                            shape = RoundedCornerShape(50)
                        )
                )
                
                if (index < backgrounds.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

/**
 * 编辑项列表
 * 
 * 所有列表项高度 ≥ 48dp
 */
@Composable
fun EditItems(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val items = listOf(
            "修改昵称",
            "修改生日",
            "修改地区",
            "修改邮筱",
            "修改密码"
        )
        
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp) // 确保高度 ≥ 48dp
                    .background(
                        color = Color.White.copy(0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        android.util.Log.d("ProfileEditScreen", "Clicked: $item")
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    
                    Text(
                        text = "→",
                        color = Color.White.copy(0.5f),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
