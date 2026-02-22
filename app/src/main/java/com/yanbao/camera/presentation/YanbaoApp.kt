package com.yanbao.camera.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.R
import com.yanbao.camera.presentation.camera.CameraScreen
import com.yanbao.camera.presentation.editor.EditorScreen
import com.yanbao.camera.presentation.gallery.GalleryScreen
import com.yanbao.camera.presentation.home.HomeScreen
import com.yanbao.camera.presentation.profile.ProfileScreen
import com.yanbao.camera.presentation.profile.ProfileViewModel
import com.yanbao.camera.presentation.recommend.RecommendScreen
import com.yanbao.camera.presentation.theme.OBSIDIAN_BLACK
import com.yanbao.camera.presentation.theme.PRIMARY_PINK

/**
 * 雁寶AI相机主应用框架
 *
 * 防欺诈协议合规：
 * - ✅ 底部导航图标全部使用自定义矢量资源（R.drawable.ic_*），禁止 Icons.Default.*
 * - ✅ 6 个标签：首页、相机、编辑、相册、推荐、我的
 * - ✅ 选中态：PRIMARY_PINK (#EC4899)
 * - ✅ 未选中态：白色 50% 透明
 * - ✅ 所有标签点击均有真实导航逻辑
 */
@Composable
fun YanbaoApp() {
    var selectedTab by remember { mutableIntStateOf(0) }

    // 共享 ProfileViewModel 实例，确保头像数据同步
    val profileViewModel: ProfileViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            YanbaoBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    Log.d("YanbaoApp", "导航切换: $index (${getTabName(index)})")
                    selectedTab = index
                }
            )
        },
        containerColor = OBSIDIAN_BLACK
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(OBSIDIAN_BLACK)
        ) {
            when (selectedTab) {
                0 -> {
                    val profile by profileViewModel.profile.collectAsState()
                    HomeScreen(
                        onCameraClick = { selectedTab = 1 },
                        onEditorClick = { selectedTab = 2 },
                        onGalleryClick = { selectedTab = 3 },
                        onRecommendClick = { selectedTab = 4 },
                        onProfileClick = { selectedTab = 5 },
                        avatarUri = profile.avatarUri
                    )
                }
                1 -> CameraScreen()
                2 -> EditorScreen()
                3 -> GalleryScreen(
                    onPhotoClick = { photoId ->
                        Log.d("YanbaoApp", "照片详情导航: $photoId")
                    }
                )
                4 -> RecommendScreen()
                5 -> ProfileScreen()
            }
        }
    }
}

/**
 * 底部导航栏
 *
 * 设计规范（严格执行）：
 * - 背景：曜石黑 (#0A0A0A) + 15% 白色透明渐变
 * - 图标：全部使用 R.drawable.ic_* 自定义矢量资源
 * - 选中态：PRIMARY_PINK (#EC4899)
 * - 未选中态：白色 50% 透明
 * - 高度：80dp
 */
@Composable
fun YanbaoBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    // 6 个标签，全部使用自定义矢量图标（禁止 Icons.Default.*）
    val tabs = listOf(
        BottomNavItem(label = "首页",  iconRes = R.drawable.ic_home),
        BottomNavItem(label = "相机",  iconRes = R.drawable.ic_camera),
        BottomNavItem(label = "编辑",  iconRes = R.drawable.ic_edit),
        BottomNavItem(label = "相册",  iconRes = R.drawable.ic_gallery),
        BottomNavItem(label = "推荐",  iconRes = R.drawable.ic_recommend),
        BottomNavItem(label = "我的",  iconRes = R.drawable.ic_profile)
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x26FFFFFF), // 15% 白色
                        Color(0x40FFFFFF)  // 25% 白色
                    )
                )
            ),
        containerColor = OBSIDIAN_BLACK,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        tabs.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        tint = if (isSelected) PRIMARY_PINK else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected) PRIMARY_PINK else Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PRIMARY_PINK,
                    selectedTextColor = PRIMARY_PINK,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                )
            )
        }
    }
}

private fun getTabName(index: Int): String = when (index) {
    0 -> "首页"; 1 -> "相机"; 2 -> "编辑"
    3 -> "相册"; 4 -> "推荐"; 5 -> "我的"
    else -> "未知"
}

/**
 * 底部导航项数据类
 * 使用 Int 资源 ID，不使用 ImageVector（禁止 Material 默认图标）
 */
data class BottomNavItem(
    val label: String,
    val iconRes: Int
)
