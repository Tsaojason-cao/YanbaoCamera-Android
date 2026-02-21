package com.yanbao.camera.presentation.gallery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.yanbao.camera.R

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
    navController: NavController,
    viewModel: GalleryViewModel = hiltViewModel()
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
            onBackClick = { navController.navigateUp() },
            onHomeClick = { navController.navigate("home") },
            onSearchClick = { /* 搜索逻辑 */ }
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
                items(filteredPhotos) { photo ->
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
    TopAppBar(
        title = {
            Text(
                text = "相册",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "主页",
                    tint = Color.White
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1A1A1A)
        )
    )
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
        border = BorderStroke(1.dp, Color(0xFFFFB6C1).copy(alpha = 0.5f)), // ✅ 粉色流光描边
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
    val hasMetadata: Boolean = false,
    val mode: String? = null
)
