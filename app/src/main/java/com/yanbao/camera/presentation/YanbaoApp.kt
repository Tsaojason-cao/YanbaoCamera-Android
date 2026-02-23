package com.yanbao.camera.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yanbao.camera.R
import com.yanbao.camera.presentation.camera.CameraScreen
import com.yanbao.camera.presentation.editor.EditorScreen
import com.yanbao.camera.presentation.gallery.GalleryScreen
import com.yanbao.camera.presentation.gallery.PhotoDetailScreen
import com.yanbao.camera.presentation.gallery.MemoryViewScreen
import com.yanbao.camera.presentation.gallery.YanbaoMemoryDetailScreen
import com.yanbao.camera.presentation.home.HomeScreen
import com.yanbao.camera.presentation.lbs.LbsScreen
import com.yanbao.camera.presentation.profile.ProfileScreen
import com.yanbao.camera.presentation.profile.ProfileEditScreen
import com.yanbao.camera.presentation.profile.ProfileViewModel
import com.yanbao.camera.presentation.recommend.RecommendScreen
import com.yanbao.camera.presentation.theme.OBSIDIAN_BLACK
import com.yanbao.camera.presentation.theme.PRIMARY_PINK

/**
 * 雁寶AI相机主应用框架（NavController 版）
 *
 * 导航架构：
 * - 底部 Tab 导航（6个主页面）使用 NavController 管理
 * - 子页面（照片详情/记忆详情/编辑资料等）通过 NavController.navigate() 进入
 * - 所有子页面均有"返回上一层"按钮（← 左上角）
 * - Android 系统返回键自动 popBackStack()
 */
@Composable
fun YanbaoApp() {
    val navController = rememberNavController()

    // 共享 ProfileViewModel 实例，确保头像数据同步
    val profileViewModel: ProfileViewModel = hiltViewModel()

    // 监听当前路由，用于底部 Tab 高亮
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 判断当前是否在主 Tab 页面（显示底部导航栏）
    val topLevelRoutes = setOf("home", "camera", "editor", "gallery", "lbs", "profile")
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                YanbaoBottomNavigation(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        Log.d("YanbaoApp", "导航切换: $route")
                        navController.navigate(route) {
                            // 避免重复入栈，保持单一实例
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = OBSIDIAN_BLACK
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(OBSIDIAN_BLACK)
        ) {
            // ─── 主 Tab 页面 ──────────────────────────────────────────────

            composable("home") {
                val profile by profileViewModel.profile.collectAsState()
                HomeScreen(
                    onCameraClick = { navController.navigate("camera") },
                    onEditorClick = { navController.navigate("editor") },
                    onGalleryClick = { navController.navigate("gallery") },
                    onRecommendClick = { navController.navigate("lbs") },
                    onProfileClick = { navController.navigate("profile") },
                    avatarUri = profile.avatarUri?.toString()
                )
            }

            composable("camera") {
                CameraScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("editor") {
                EditorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("gallery") {
                GalleryScreen(
                    onPhotoClick = { photoId ->
                        navController.navigate("photo_detail/$photoId")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("lbs") {
                LbsScreen(
                    onBackClick = { navController.popBackStack() },
                    navController = navController
                )
            }

            composable("profile") {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onEditProfile = { navController.navigate("profile_edit") }
                )
            }

            // ─── 子页面（支持返回上一层）────────────────────────────────

            composable(
                route = "photo_detail/{photoId}",
                arguments = listOf(navArgument("photoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val photoId = backStackEntry.arguments?.getString("photoId") ?: ""
                PhotoDetailScreen(
                    photoId = photoId,
                    navController = navController
                )
            }

            composable(
                route = "memory_detail/{memoryId}",
                arguments = listOf(navArgument("memoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val memoryId = backStackEntry.arguments?.getString("memoryId") ?: ""
                MemoryViewScreen(
                    memoryId = memoryId,
                    navController = navController
                )
            }

            composable(
                route = "yanbao_memory_detail/{memoryId}",
                arguments = listOf(navArgument("memoryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val memoryId = backStackEntry.arguments?.getString("memoryId") ?: ""
                YanbaoMemoryDetailScreen(
                    photoUrl = memoryId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("profile_edit") {
                ProfileEditScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() }
                )
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
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf(
        BottomNavItem(label = "首页",  iconRes = R.drawable.ic_home,      route = "home"),
        BottomNavItem(label = "相机",  iconRes = R.drawable.ic_camera,    route = "camera"),
        BottomNavItem(label = "编辑",  iconRes = R.drawable.ic_edit,      route = "editor"),
        BottomNavItem(label = "相册",  iconRes = R.drawable.ic_gallery,   route = "gallery"),
        BottomNavItem(label = "推荐",  iconRes = R.drawable.ic_recommend, route = "lbs"),
        BottomNavItem(label = "我的",  iconRes = R.drawable.ic_profile,   route = "profile")
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x26FFFFFF),
                        Color(0x40FFFFFF)
                    )
                )
            ),
        containerColor = OBSIDIAN_BLACK,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { item ->
            val isSelected = currentRoute == item.route
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
                onClick = { onTabSelected(item.route) },
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

/**
 * 底部导航项数据类
 */
data class BottomNavItem(
    val label: String,
    val iconRes: Int,
    val route: String
)
