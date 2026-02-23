package com.yanbao.camera.presentation.gallery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.KuromiPink
import com.yanbao.camera.ui.theme.ObsidianBlack
import com.yanbao.camera.ui.theme.YanbaoBrandTitle
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush

/**
 * 相册模块 - 分层过滤逻辑
 * 
 * 设计规范：
 * - 顶部：搜索与返回栏
 * - Tab：全部、雁宝记忆、大师、美人、29D
 * - 内容：4 列网格布局
 * - 底部：5 栏导航
 */
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = hiltViewModel(),
    onPhotoClick: (String) -> Unit = { photoId ->
        android.util.Log.d("GalleryScreen", "Photo clicked: $photoId")
    },
    onBackClick: () -> Unit = {}
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val filteredPhotos by viewModel.filteredPhotos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // --- 顶部搜索与返回栏 ---
        GalleryTopBar(
            onBackClick = onBackClick,
            onHomeClick = {},
            onSearchClick = {}
        )

        // --- 核心分层过滤 Tab (对应截图：全部、雁宝记忆、大师、美人、29D) ---
        ScrollableTabRow(
            selectedTabIndex = selectedTab.index,
            containerColor = Color.Transparent,
            contentColor = Color(0xFFFFB6C1),
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            GalleryTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { viewModel.onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.displayName,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // --- 瀑布流/网格展示区 (4 列) ---
        Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredPhotos, key = { it.id }) { photo ->
                    PhotoItem(
                        photo = photo,
                        onClick = { viewModel.onPhotoClick(photo) }
                    )
                }
            }
        }
    }
}

/**
 * 顶部搜索与返回栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTopBar(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xF20A0A0A), Color(0xCC0A0A0A))
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 返回按钮
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }
        // 中央品牌标识
        YanbaoBrandTitle(modifier = Modifier.align(Alignment.Center))
        // 右侧搜索
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 照片网格项
 */
@Composable
fun PhotoItem(
    photo: Photo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        border = BorderStroke(1.dp, Color(0xFFFFB6C1).copy(alpha = 0.5f)), // [OK] 粉色流光描边
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Box {
            AsyncImage(
                model = photo.path,
                contentDescription = "照片",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 如果是 29D 照片，右上角显示标识
            if (photo.hasMetadata) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(Color(0xFFFFB6C1), CircleShape)
                )
            }
        }
    }
}

/**
 * 相册 Tab 枚举
 */
enum class GalleryTab(val index: Int, val displayName: String) {
    ALL(0, "全部"),
    MEMORY(1, "雁宝记忆"),
    MASTER(2, "大师"),
    BEAUTY(3, "美人"),
    D29(4, "29D")
}

/**
 * 照片数据类
 */
data class Photo(
    val id: String,
    val path: String,
    val contentUri: String? = null,   // content:// URI，用于 Coil 加载和 EXIF 读取
    val hasMetadata: Boolean = false,
    val isMemory: Boolean = false,     // 是否为雁宝记忆照片
    val mode: String? = null
)
