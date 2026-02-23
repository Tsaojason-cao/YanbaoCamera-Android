package com.yanbao.camera.presentation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.yanbao.camera.presentation.navigation.NavTransitions
import com.yanbao.camera.presentation.navigation.SwipeBackContainer
import com.yanbao.camera.presentation.profile.ProfileScreen
import com.yanbao.camera.presentation.profile.ProfileEditScreen
import com.yanbao.camera.presentation.profile.ProfileViewModel
import com.yanbao.camera.presentation.recommend.RecommendScreen
import com.yanbao.camera.presentation.theme.OBSIDIAN_BLACK
import com.yanbao.camera.presentation.theme.PRIMARY_PINK
import com.yanbao.camera.presentation.profile.PrivacyScreen
import com.yanbao.camera.presentation.profile.HelpScreen
import com.yanbao.camera.presentation.profile.AboutScreen

/**
 * 雁寶AI相机主应用框架（NavController + 手势返回 + 页面切换动画）
 *
 * 导航架构：
 * - 底部 Tab 导航（6个主页面）：淡入淡出切换，launchSingleTop + saveState
 * - 子页面（照片详情/记忆详情/编辑资料等）：右侧滑入/滑出动画
 * - 手势返回：SwipeBackContainer 包裹所有子页面，左边缘滑动触发 popBackStack()
 * - Android 系统返回键：BackHandler 全局处理
 * - Android 14 预测性返回：AndroidManifest 启用 enableOnBackInvokedCallback
 */
@Composable
fun YanbaoApp() {
    val navController = rememberNavController()

    // 共享 ProfileViewModel 实例，确保头像数据同步
    val profileViewModel: ProfileViewModel = hiltViewModel()

    // 监听当前路由，用于底部 Tab 高亮和手势返回开关
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 主 Tab 路由集合
    val topLevelRoutes = setOf("home", "camera", "editor", "gallery", "lbs", "profile")
    val showBottomBar = currentRoute in topLevelRoutes

    // 是否可以返回（子页面才启用手势返回）
    val canGoBack = remember(currentRoute) {
        currentRoute != null && currentRoute !in topLevelRoutes
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                YanbaoBottomNavigation(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        Log.d("YanbaoApp", "Tab 切换: $route")
                        navController.navigate(route) {
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

        // 手势返回容器：包裹整个 NavHost
        // 仅在子页面（canGoBack=true）时启用左边缘滑动手势
        SwipeBackContainer(
            enabled = canGoBack,
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(OBSIDIAN_BLACK),
                // 全局默认动画（Tab 切换）
                enterTransition = {
                    fadeIn(animationSpec = tween(200, easing = LinearOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(200, easing = LinearOutSlowInEasing))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
                }
            ) {
                // ─── 主 Tab 页面（淡入淡出） ──────────────────────────────

                composable(
                    route = "home",
                    enterTransition = {
                        fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                    }
                ) {
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

                composable(
                    route = "camera",
                    enterTransition = {
                        fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                    }
                ) {
                    CameraScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "editor",
                    enterTransition = {
                        fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                    }
                ) {
                    EditorScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "gallery",
                    enterTransition = {
                        fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                    }
                ) {
                    GalleryScreen(
                        onPhotoClick = { photoId ->
                            navController.navigate("photo_detail/$photoId")
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "lbs",
                    enterTransition = {
                        fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                    }
                ) {
                    LbsScreen(
                        onBackClick = { navController.popBackStack() },
                        navController = navController
                    )
                }

                composable(
                    route = "profile",
                    enterTransition = {
                        fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
                    }
                ) {
                    ProfileScreen(
                        onBackClick = { navController.popBackStack() },
                        onEditProfile = { navController.navigate("profile_edit") },
                        onPrivacy = { navController.navigate("privacy") },
                        onHelp = { navController.navigate("help") },
                        onAbout = { navController.navigate("about") }
                    )
                }

                // ─── 子页面（右侧滑入/滑出 + 手势返回） ─────────────────

                composable(
                    route = "photo_detail/{photoId}",
                    arguments = listOf(navArgument("photoId") { type = NavType.StringType }),
                    // 进入：从右侧滑入
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(200), initialAlpha = 0.8f)
                    },
                    // 退出（被新页面覆盖）：向左轻微位移
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200), targetAlpha = 0.7f)
                    },
                    // 弹出返回：前一页从左侧恢复
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(200), initialAlpha = 0.7f)
                    },
                    // 弹出退出：当前页向右滑出
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(280, easing = FastOutLinearInEasing)
                        ) + fadeOut(animationSpec = tween(200), targetAlpha = 0.8f)
                    }
                ) { backStackEntry ->
                    val photoId = backStackEntry.arguments?.getString("photoId") ?: ""
                    PhotoDetailScreen(
                        photoId = photoId,
                        navController = navController
                    )
                }

                composable(
                    route = "memory_detail/{memoryId}",
                    arguments = listOf(navArgument("memoryId") { type = NavType.StringType }),
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(200), initialAlpha = 0.8f)
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200), targetAlpha = 0.7f)
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(200), initialAlpha = 0.7f)
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(280, easing = FastOutLinearInEasing)
                        ) + fadeOut(animationSpec = tween(200), targetAlpha = 0.8f)
                    }
                ) { backStackEntry ->
                    val memoryId = backStackEntry.arguments?.getString("memoryId") ?: ""
                    MemoryViewScreen(
                        memoryId = memoryId,
                        navController = navController
                    )
                }

                composable(
                    route = "yanbao_memory_detail/{memoryId}",
                    arguments = listOf(navArgument("memoryId") { type = NavType.StringType }),
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(200), initialAlpha = 0.8f)
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200), targetAlpha = 0.7f)
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(200), initialAlpha = 0.7f)
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(280, easing = FastOutLinearInEasing)
                        ) + fadeOut(animationSpec = tween(200), targetAlpha = 0.8f)
                    }
                ) { backStackEntry ->
                    val memoryId = backStackEntry.arguments?.getString("memoryId") ?: ""
                    YanbaoMemoryDetailScreen(
                        photoUrl = memoryId,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "profile_edit",
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(200), initialAlpha = 0.8f)
                    }
                composable(route = "privacy") {
                    PrivacyScreen(onBackClick = { navController.popBackStack() })
                }
                composable(route = "help") {
                    HelpScreen(onBackClick = { navController.popBackStack() })
                }
                composable(route = "about") {
                    AboutScreen(onBackClick = { navController.popBackStack() })
                },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200), targetAlpha = 0.7f)
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(200), initialAlpha = 0.7f)
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(280, easing = FastOutLinearInEasing)
                        ) + fadeOut(animationSpec = tween(200), targetAlpha = 0.8f)
                    }
                ) {
                    ProfileEditScreen(
                        onBack = { navController.popBackStack() },
                        onSave = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

/**
 * 底部导航栏
 *
 * 设计规范：
 * - 背景：曜石黑 (#0A0A0A) + 毛玻璃渐变
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
        BottomNavItem(label = "首页",  iconRes = R.drawable.ic_home_kuromi,      route = "home"),
        BottomNavItem(label = "相机",  iconRes = R.drawable.ic_camera_kuromi,    route = "camera"),
        BottomNavItem(label = "编辑",  iconRes = R.drawable.ic_edit_kuromi,      route = "editor"),
        BottomNavItem(label = "相册",  iconRes = R.drawable.ic_album_kuromi,   route = "gallery"),
        BottomNavItem(label = "推荐",  iconRes = R.drawable.ic_recommend_kuromi, route = "lbs"),
        BottomNavItem(label = "我的",  iconRes = R.drawable.ic_profile_kuromi,   route = "profile")
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
