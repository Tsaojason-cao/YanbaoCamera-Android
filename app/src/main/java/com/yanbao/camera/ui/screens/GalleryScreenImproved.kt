package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.components.KuromiCorners

/**
 * 改进的GalleryScreen - 完全匹配设计图
 * 包含相册网格、选择模式、排序等功能
 */
@Composable
fun GalleryScreenImproved(
    onNavigateBack: () -> Unit = {},
    onImageSelected: (String) -> Unit = {}
) {
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf(setOf<Int>()) }
    var sortBy by remember { mutableStateOf("最新") }
    
    // 模拟相册数据
    val galleryImages = (1..12).map { "image_$it" }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA78BFA),
                        Color(0xFFEC4899),
                        Color(0xFFF9A8D4)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部工具栏
            TopGalleryBar(
                onBack = onNavigateBack,
                isSelectionMode = isSelectionMode,
                onToggleSelection = { isSelectionMode = !isSelectionMode },
                sortBy = sortBy,
                onSortChange = { sortBy = it }
            )
            
            // 相册网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(galleryImages.size) { index ->
                    GalleryImageItem(
                        imageId = index,
                        isSelected = index in selectedImages,
                        isSelectionMode = isSelectionMode,
                        onSelect = {
                            selectedImages = if (index in selectedImages) {
                                selectedImages - index
                            } else {
                                selectedImages + index
                            }
                        },
                        onClick = {
                            if (isSelectionMode) {
                                selectedImages = if (index in selectedImages) {
                                    selectedImages - index
                                } else {
                                    selectedImages + index
                                }
                            } else {
                                onImageSelected(galleryImages[index])
                            }
                        }
                    )
                }
            }
        }
        
        // 库洛米装饰
        KuromiCorners()
        
        // 底部操作栏
        if (isSelectionMode && selectedImages.isNotEmpty()) {
            BottomGalleryBar(
                selectedCount = selectedImages.size,
                onDelete = {
                    selectedImages = emptySet()
                },
                onShare = {}
            )
        }
    }
}

/**
 * 顶部工具栏
 */
@Composable
private fun TopGalleryBar(
    onBack: () -> Unit,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    sortBy: String,
    onSortChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                "我的相册",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 排序按钮
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // 选择模式按钮
                IconButton(onClick = onToggleSelection) {
                    Icon(
                        if (isSelectionMode) Icons.Default.CheckCircle else Icons.Default.Circle,
                        contentDescription = "Selection Mode",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * 相册图片项
 */
@Composable
private fun GalleryImageItem(
    imageId: Int,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onSelect: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEC4899).copy(alpha = 0.3f))
            .clickable { onClick() }
    ) {
        // 图片占位符
        Icon(
            Icons.Default.Image,
            contentDescription = "Gallery Image",
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
        
        // 选择复选框
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        color = if (isSelected) Color(0xFFEC4899) else Color.White.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        // 图片编号
        Text(
            "#$imageId",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}

/**
 * 底部操作栏
 */
@Composable
private fun BottomGalleryBar(
    selectedCount: Int,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .align(Alignment.BottomCenter)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFF9A8D4).copy(alpha = 0.95f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 已选择计数
            Text(
                "已选择 $selectedCount 张",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 分享按钮
            Button(
                onClick = onShare,
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.3f)
                )
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    "分享",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            
            // 删除按钮
            Button(
                onClick = onDelete,
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEC4899)
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    "删除",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}
