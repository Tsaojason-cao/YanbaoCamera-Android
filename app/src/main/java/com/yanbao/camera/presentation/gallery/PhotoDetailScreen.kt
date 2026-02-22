package com.yanbao.camera.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.yanbao.camera.R

private val KUROMI_PINK = Color(0xFFEC4899)
private val OBSIDIAN_BLACK = Color(0xFF0A0A0A)

/**
 * 照片详情页
 * Layer 0: 全屏照片预览
 * Layer 1: 底部毛玻璃操作栏（EXIF + 操作按钮）
 * Layer 2: 雁宝记忆标签（悬浮）
 */
@Composable
fun PhotoDetailScreen(
    navController: NavController,
    photoId: String,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    // 使用 ViewModel 中的 filteredPhotos（Photo 类型）
    val filteredPhotos by viewModel.filteredPhotos.collectAsStateWithLifecycle()
    val photo = filteredPhotos.find { it.id == photoId }
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val bottomSheetHeight = screenHeight * 0.28f

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Layer 0: 全屏照片预览
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("📸", fontSize = 80.sp)
        }

        // Layer 2: 左上角返回按钮
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "返回",
                tint = Color.White
            )
        }

        // 右上角更多菜单
        IconButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "更多",
                tint = Color.White
            )
        }

        // 雁宝记忆标签
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 64.dp, top = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = KUROMI_PINK
        ) {
            Text(
                text = "记忆",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // Layer 1: 底部操作栏
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(bottomSheetHeight)
                .align(Alignment.BottomCenter),
            color = OBSIDIAN_BLACK.copy(alpha = 0.92f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // 拖拽指示条
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // EXIF 信息网格
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ExifItem(label = "ISO", value = "800")
                    ExifItem(label = "快门", value = "1/250s")
                    ExifItem(label = "焦距", value = "50mm")
                    ExifItem(label = "色温", value = "6000K")
                }

                Spacer(modifier = Modifier.weight(1f))

                // 底部操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButton(icon = R.drawable.ic_edit, label = "编辑")
                    ActionButton(icon = R.drawable.ic_share, label = "分享")
                    ActionButton(icon = R.drawable.ic_delete, label = "删除")
                    ActionButton(icon = R.drawable.ic_favorite, label = "收藏", tint = KUROMI_PINK)
                    ActionButton(icon = R.drawable.ic_info, label = "信息")
                }
            }
        }
    }
}

@Composable
private fun ExifItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionButton(icon: Int, label: String, tint: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}
