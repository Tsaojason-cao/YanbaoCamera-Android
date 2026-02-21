package com.yanbao.camera.presentation.gallery

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yanbao.camera.core.util.PhotoParams
import com.yanbao.camera.core.util.YanbaoExifParser

/**
 * 相册详情页 - 1:1 还原图 26
 * 
 * 核心功能：
 * - 从二进制文件读取真实 29D 参数（通过 YanbaoExifParser）
 * - 粉紫渐变流光描边
 * - 毛玻璃参数叠加层
 * - 库洛米水印（根据拍摄模式显示不同颜色）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryDetailScreen(
    photoPath: String,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    // 🚨 核心：从文件中读取真实参数，不使用数据库缓存
    val photoParams = remember(photoPath) {
        YanbaoExifParser.getPhotoMetadata(photoPath)
    }

    // 流光动画
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "照片详情",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "分享",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = Color(0xFFFF6B6B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            // 背景：照片全屏显示
            AsyncImage(
                model = photoPath,
                contentDescription = "照片",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // 核心：图 26 同款的粉紫渐变毛玻璃参数叠加层
            GalleryDetailOverlay(
                photoParams = photoParams,
                glowAlpha = glowAlpha,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )

            // 库洛米水印（根据拍摄模式显示不同颜色）
            KuromiWatermark(
                mode = photoParams.mode,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

/**
 * 参数叠加层 - 1:1 还原图 26 的视觉风格
 * 
 * 特性：
 * - 毛玻璃背景（黑色 60% 透明度 + 20dp 模糊）
 * - 粉紫渐变流光描边
 * - 真实的 29D 参数展示
 */
@Composable
fun GalleryDetailOverlay(
    photoParams: PhotoParams,
    glowAlpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .blur(20.dp) // 毛玻璃效果
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFB6C1).copy(alpha = glowAlpha), // 粉
                        Color(0xFFE0B0FF).copy(alpha = glowAlpha)  // 紫
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题
            Text(
                text = "📸 拍摄参数",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // 第一行：快门、ISO、色温
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                ParamItem(label = "快门", value = photoParams.shutter)
                ParamItem(label = "感光", value = photoParams.iso)
                ParamItem(label = "色温", value = photoParams.wb)
            }

            // 第二行：光圈、焦距、模式
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                ParamItem(label = "光圈", value = photoParams.aperture)
                ParamItem(label = "焦距", value = photoParams.focalLength)
                ParamItem(label = "模式", value = photoParams.mode)
            }

            // 如果有美颜参数，显示第三行
            if (photoParams.beautySmooth != 0 || photoParams.beautyWhite != 0 || photoParams.beautyBlemish != 0) {
                Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                
                Text(
                    text = "💄 美颜参数",
                    color = Color(0xFFFFB6C1),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ParamItem(label = "磨皮", value = formatBeautyValue(photoParams.beautySmooth))
                    ParamItem(label = "美白", value = formatBeautyValue(photoParams.beautyWhite))
                    ParamItem(label = "祛斑", value = formatBeautyValue(photoParams.beautyBlemish))
                }
            }

            // 位置信息
            if (photoParams.location.isNotEmpty() && photoParams.location != "无位置信息") {
                Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                
                Text(
                    text = "📍 ${photoParams.location}",
                    color = Color(0xFF10B981),
                    fontSize = 12.sp
                )
            }

            // 拍摄时间
            if (photoParams.dateTime.isNotEmpty() && photoParams.dateTime != "未知时间") {
                Text(
                    text = "🕒 ${photoParams.dateTime}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 单个参数项
 */
@Composable
fun ParamItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 库洛米水印（根据拍摄模式显示不同颜色）
 */
@Composable
fun KuromiWatermark(
    mode: String,
    modifier: Modifier = Modifier
) {
    val watermarkColor = when {
        mode.contains("大师", ignoreCase = true) || mode.contains("MASTER", ignoreCase = true) -> Color(0xFF3B82F6) // 蓝色
        mode.contains("美人", ignoreCase = true) || mode.contains("BEAUTY", ignoreCase = true) -> Color(0xFFFFB6C1) // 粉色
        mode.contains("29D", ignoreCase = true) -> Color(0xFFA78BFA) // 紫色
        mode.contains("雁宝记忆", ignoreCase = true) || mode.contains("MEMORY", ignoreCase = true) -> Color(0xFFFBBF24) // 金色
        else -> Color.White.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .background(
                color = watermarkColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "🎀 $mode",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 格式化美颜参数值
 */
private fun formatBeautyValue(value: Int): String {
    return if (value > 0) "+$value" else "$value"
}
