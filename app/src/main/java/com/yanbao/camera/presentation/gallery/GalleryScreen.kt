package com.yanbao.camera.presentation.gallery

import android.content.Intent
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.YanbaoBrandTitle

private val KUROMI_PINK = Color(0xFFEC4899)

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = hiltViewModel(),
    onPhotoClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val filteredPhotos by viewModel.filteredPhotos.collectAsStateWithLifecycle()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsStateWithLifecycle()
    val selectedPhotoIds by viewModel.selectedPhotoIds.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        if (isMultiSelectMode) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A))
                    .statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.clearSelection() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "取消多选", tint = Color.White)
                }
                Text(text = "已选 ${selectedPhotoIds.size} 张", color = KUROMI_PINK, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(48.dp))
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(colors = listOf(Color(0xF20A0A0A), Color(0xCC0A0A0A))))
                    .statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
                }
                YanbaoBrandTitle(modifier = Modifier.align(Alignment.Center))
                IconButton(onClick = { Log.d("GalleryScreen", "搜索") }, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "搜索", tint = Color.White)
                }
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab.index,
            containerColor = Color.Transparent,
            contentColor = KUROMI_PINK,
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

        Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(filteredPhotos, key = { it.id }) { photo ->
                    val isSelected = selectedPhotoIds.contains(photo.id)
                    PhotoGridItem(
                        photo = photo,
                        isMultiSelectMode = isMultiSelectMode,
                        isSelected = isSelected,
                        onClick = {
                            if (isMultiSelectMode) viewModel.togglePhotoSelection(photo.id)
                            else onPhotoClick(photo.id)
                        },
                        onLongClick = {
                            if (!isMultiSelectMode) {
                                viewModel.toggleMultiSelectMode()
                                viewModel.togglePhotoSelection(photo.id)
                            }
                        }
                    )
                }
            }
            if (filteredPhotos.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_album_kuromi), contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                    Text(text = "暂无照片", color = Color.White.copy(alpha = 0.4f), fontSize = 15.sp)
                }
            }
        }

        AnimatedVisibility(
            visible = isMultiSelectMode && selectedPhotoIds.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A))
                    .navigationBarsPadding().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { showDeleteDialog = true }) {
                    Icon(painter = painterResource(id = R.drawable.ic_delete_kuromi), contentDescription = "删除", tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("删除", color = Color(0xFFEF4444), fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                    val selectedPhotos = filteredPhotos.filter { selectedPhotoIds.contains(it.id) }
                    val uris = selectedPhotos.mapNotNull { it.contentUri?.let { u -> android.net.Uri.parse(u) } }
                    if (uris.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "image/*"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                        }
                        context.startActivity(Intent.createChooser(intent, "分享照片"))
                    }
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_share_kuromi), contentDescription = "分享", tint = KUROMI_PINK, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("分享", color = KUROMI_PINK, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                    Log.d("GalleryScreen", "收藏 ${selectedPhotoIds.size} 张照片")
                    viewModel.clearSelection()
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_favorite_kuromi), contentDescription = "收藏", tint = KUROMI_PINK, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("收藏", color = KUROMI_PINK, fontSize = 12.sp)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除照片", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("确定删除选中的 ${selectedPhotoIds.size} 张照片？此操作不可撤销。", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSelectedPhotos(); showDeleteDialog = false }) {
                    Text("删除", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消", color = KUROMI_PINK) }
            },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhotoGridItem(
    photo: Photo,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .then(if (isSelected) Modifier.border(2.dp, KUROMI_PINK, RoundedCornerShape(6.dp)) else Modifier)
    ) {
        AsyncImage(
            model = photo.contentUri ?: photo.path,
            contentDescription = "照片",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (photo.hasMetadata) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(12.dp).background(KUROMI_PINK, CircleShape))
        }
        if (isMultiSelectMode) {
            Box(
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(20.dp)
                    .background(if (isSelected) KUROMI_PINK else Color.Black.copy(alpha = 0.5f), CircleShape)
                    .border(1.5.dp, if (isSelected) KUROMI_PINK else Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        if (isSelected) Box(modifier = Modifier.fillMaxSize().background(KUROMI_PINK.copy(alpha = 0.2f)))
    }
}

enum class GalleryTab(val index: Int, val displayName: String) {
    ALL(0, "全部"),
    MEMORY(1, "雁宝记忆"),
    LBS(2, "推荐LBS")
}

data class Photo(
    val id: String,
    val path: String,
    val contentUri: String? = null,
    val hasMetadata: Boolean = false,
    val isMemory: Boolean = false,
    val mode: String? = null
)
