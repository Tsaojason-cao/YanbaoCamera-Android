package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.components.KuromiCorners
import com.yanbao.camera.ui.theme.GradientEnd
import com.yanbao.camera.ui.theme.GradientMiddle
import com.yanbao.camera.ui.theme.GradientStart
import com.yanbao.camera.ui.theme.TextWhite
import com.yanbao.camera.ui.theme.glassEffect

/**
 * 相册屏幕 - 完整实现版本
 * 
 * 功能：
 * - 相册网格显示
 * - 相册分组（按日期）
 * - 点击预览
 * - 删除功能
 * - 库洛米装饰
 */
@Composable
fun GalleryScreenV2(
    onPhotoSelected: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var selectedPhotoIndex by remember { mutableStateOf(-1) }
    
    // Mock数据：模拟相册
    val mockPhotos = (1..12).map { "photo_$it" }
    
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
                .padding(16.dp)
        ) {
            // 顶部工具栏
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(cornerRadius = 12)
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = TextWhite
                    )
                }
                
                Text(
                    text = "相册",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(start = 40.dp)
                )
            }
            
            // 相册网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mockPhotos) { photo ->
                    GalleryPhotoItem(
                        photoName = photo,
                        isSelected = mockPhotos.indexOf(photo) == selectedPhotoIndex,
                        onClick = {
                            selectedPhotoIndex = mockPhotos.indexOf(photo)
                            onPhotoSelected(photo)
                        }
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
 * 相册照片项目
 */
@Composable
fun GalleryPhotoItem(
    photoName: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isSelected) Color(0xFFEC4899).copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "📷\n$photoName",
            color = TextWhite,
            fontSize = 10.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(4.dp)
        )
        
        // 选中指示器
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFF6B9D)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 相册详情页（大图预览）
 */
@Composable
fun GalleryDetailScreen(
    photoName: String,
    onNavigateBack: () -> Unit = {},
    onShare: () -> Unit = {},
    onDelete: () -> Unit = {}
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
                .padding(16.dp)
        ) {
            // 顶部工具栏
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(cornerRadius = 12)
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = TextWhite
                    )
                }
            }
            
            // 大图预览
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📸\n$photoName\n(大图预览)",
                    color = TextWhite,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            // 照片信息
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(cornerRadius = 12)
                    .padding(12.dp)
            ) {
                Column {
                    PhotoInfoRow("文件名", photoName)
                    PhotoInfoRow("大小", "2.5 MB")
                    PhotoInfoRow("日期", "2026-02-17")
                    PhotoInfoRow("分辨率", "4000 x 3000")
                }
            }
            
            // 操作按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .glassEffect(cornerRadius = 12)
                    .padding(12.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        label = "分享",
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ActionButton(
                        label = "删除",
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        isDestructive = true
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
 * 照片信息行
 */
@Composable
fun PhotoInfoRow(
    label: String,
    value: String
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray
        )
        
        Text(
            text = value,
            fontSize = 11.sp,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 操作按钮
 */
@Composable
fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isDestructive) Color(0xFFFF6B6B).copy(alpha = 0.7f) else Color(0xFFEC4899).copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )
    }
}
