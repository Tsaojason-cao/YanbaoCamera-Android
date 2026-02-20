package com.yanbao.camera.ui.gallery

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.yanbao.camera.data.model.GalleryPhoto
import com.yanbao.camera.ui.theme.YanbaoGradient
import com.yanbao.camera.viewmodel.GalleryViewModel

/**
 * 相册主界面
 * 严格按照 05_gallery/01_gallery_main.png 设计规格实现：
 * - 粉紫渐变背景
 * - 顶部标题栏（返回 + "相册"）
 * - 3列照片瀑布流（真实读取系统MediaStore）
 * - 点击照片进入预览
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onNavigateBack: () -> Unit,
    onPhotoSelected: (String) -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val photos by viewModel.photos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 存储权限
    var hasStoragePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_IMAGES
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasStoragePermission = granted
        if (granted) viewModel.loadPhotos()
    }

    LaunchedEffect(hasStoragePermission) {
        if (hasStoragePermission) {
            viewModel.loadPhotos()
        } else {
            val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else
                Manifest.permission.READ_EXTERNAL_STORAGE
            permissionLauncher.launch(permission)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = YanbaoGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部标题栏
            TopAppBar(
                title = {
                    Text(
                        text = "相册",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            if (!hasStoragePermission) {
                // 无权限提示
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("需要存储权限才能查看相册", color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                                    Manifest.permission.READ_MEDIA_IMAGES
                                else
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                permissionLauncher.launch(permission)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                        ) {
                            Text("授予权限")
                        }
                    }
                }
            } else if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFEC4899))
                }
            } else if (photos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("相册为空", color = Color.White, fontSize = 16.sp)
                }
            } else {
                // 3列照片网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        PhotoGridItem(
                            photo = photo,
                            onClick = { onPhotoSelected(photo.uri) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 照片网格项
 */
@Composable
fun PhotoGridItem(
    photo: GalleryPhoto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(2.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.uri)
                .crossfade(true)
                .build(),
            contentDescription = photo.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
