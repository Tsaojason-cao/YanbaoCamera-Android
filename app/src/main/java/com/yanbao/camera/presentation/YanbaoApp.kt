package com.yanbao.camera.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.presentation.camera.CameraScreen
import com.yanbao.camera.presentation.gallery.GalleryScreen
import com.yanbao.camera.presentation.home.HomeScreen
import com.yanbao.camera.presentation.profile.ProfileScreen
import com.yanbao.camera.presentation.profile.ProfileViewModel
import com.yanbao.camera.presentation.recommend.RecommendScreen
import com.yanbao.camera.presentation.editor.EditorScreen
import com.yanbao.camera.core.util.verifyYanbaoUi
import androidx.compose.ui.platform.LocalContext
import com.yanbao.camera.presentation.theme.YanbaoPink

/**
 * 雁宝AI相机主应用框架
 * 
 * 包含 5 个底部导航标签：
 * 1. 首页 (Home)
 * 2. 相机 (Camera)
 * 3. 相册 (Gallery)
 * 4. 推荐 (Recommend)
 * 5. 我的 (Profile/Settings)
 */
@Composable
fun YanbaoApp() {
    var selectedTab by remember { mutableIntStateOf(0) } // ✅ 默認选中首頁
    
    // 🚨 核心：共享 ProfileViewModel 实例，确保数据同步
    val profileViewModel: ProfileViewModel = hiltViewModel()
    
    // 🚨 UI 还原度自檢
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        verifyYanbaoUi(context)
    }
    
    Scaffold(
        bottomBar = {
            YanbaoBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> {
                    // 🚨 核心：从 ProfileViewModel 读取真实头像
                    val profile by profileViewModel.profile.collectAsState()
                    
                    HomeScreen(
                        onCameraClick = { selectedTab = 2 },      // ✅ 拍攝在 index 2
                        onEditorClick = { selectedTab = 3 },      // ✅ 编辑在 index 3
                        onGalleryClick = { selectedTab = 5 },     // ✅ 相册功能
                        onRecommendClick = { selectedTab = 1 },   // ✅ 推薦在 index 1
                        onProfileClick = { selectedTab = 4 },     // ✅ 我的在 index 4
                        avatarUri = profile.avatarUri
                    )
                }
                1 -> RecommendScreen()  // ✅ 推薦
                2 -> CameraScreen()     // ✅ 拍攝
                3 -> EditorScreen()     // ✅ 编辑
                4 -> ProfileScreen()    // ✅ 我的
                5 -> GalleryScreen()    // ✅ 相册
            }
        }
    }
}

/**
 * 底部导航栏
 * 
 * 设计规范：
 * - 背景：毛玻璃效果（透明度 15%，模糊度 16dp）
 * - 图标：选中态填充粉色渐变
 * - 文字：选中态粉色，未选中态白色 50% 透明
 */
@Composable
fun YanbaoBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        BottomNavItem("首页", Icons.Default.Home),        // Home
        BottomNavItem("推荐", Icons.Default.Explore),     // Explore (推薦)
        BottomNavItem("拍攝", Icons.Default.CameraAlt),  // 拍攝 (中間大按鈕)
        BottomNavItem("编辑", Icons.Default.Edit),       // Editor (编辑)
        BottomNavItem("我的", Icons.Default.Person)      // Profile
    )
    
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x26FFFFFF), // 15% 白色透明
                        Color(0x40FFFFFF)  // 25% 白色透明
                    )
                )
            ),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) {
        tabs.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selectedTab == index) {
                            YanbaoPink // ✅ 使用正確的粉色 #FFB6C1
                        } else {
                            Color.White.copy(alpha = 0.5f)
                        }
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (selectedTab == index) {
                            YanbaoPink // ✅ 使用正確的粉色 #FFB6C1
                        } else {
                            Color.White.copy(alpha = 0.5f)
                        }
                    )
                },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = YanbaoPink, // ✅ 使用正確的粉色
                    selectedTextColor = YanbaoPink, // ✅ 使用正確的粉色
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/**
 * 底部导航项数据类
 */
data class BottomNavItem(
    val label: String,
    val icon: ImageVector
)
