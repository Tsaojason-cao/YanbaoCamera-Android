package com.yanbao.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yanbao.camera.ui.camera.CameraScreen
import com.yanbao.camera.ui.edit.EditScreen
import com.yanbao.camera.ui.gallery.GalleryScreen
import com.yanbao.camera.ui.home.HomeScreen
import com.yanbao.camera.ui.profile.ProfileScreen
import com.yanbao.camera.ui.recommend.RecommendScreen
import com.yanbao.camera.ui.theme.YanbaoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            YanbaoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }
}

// 底部导航路由定义
sealed class BottomNavRoute(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavRoute("home", "首页", Icons.Filled.Home)
    object Camera : BottomNavRoute("camera", "相机", Icons.Filled.CameraAlt)
    object Gallery : BottomNavRoute("gallery", "相册", Icons.Filled.Collections)
    object Recommend : BottomNavRoute("recommend", "推荐", Icons.Filled.Explore)
    object Profile : BottomNavRoute("profile", "我的", Icons.Filled.Person)
}

val bottomNavItems = listOf(
    BottomNavRoute.Home,
    BottomNavRoute.Camera,
    BottomNavRoute.Gallery,
    BottomNavRoute.Recommend,
    BottomNavRoute.Profile
)

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            YanbaoBottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = BottomNavRoute.Camera.route
            ) {
                composable(BottomNavRoute.Home.route) {
                    HomeScreen(
                        onNavigateToCamera = { navController.navigate(BottomNavRoute.Camera.route) },
                        onNavigateToRecommend = { navController.navigate(BottomNavRoute.Recommend.route) },
                        onNavigateToGallery = { navController.navigate(BottomNavRoute.Gallery.route) },
                        onNavigateToProfile = { navController.navigate(BottomNavRoute.Profile.route) }
                    )
                }
                composable(BottomNavRoute.Camera.route) {
                    CameraScreen(
                        onNavigateToGallery = { navController.navigate(BottomNavRoute.Gallery.route) },
                        onPhotoTaken = { uri -> navController.navigate("edit?uri=$uri") }
                    )
                }
                composable(BottomNavRoute.Gallery.route) {
                    GalleryScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onPhotoSelected = { uri -> navController.navigate("edit?uri=$uri") }
                    )
                }
                composable(BottomNavRoute.Recommend.route) {
                    RecommendScreen()
                }
                composable(BottomNavRoute.Profile.route) {
                    ProfileScreen()
                }
                composable("edit?uri={uri}") { backStackEntry ->
                    val uri = backStackEntry.arguments?.getString("uri")
                    EditScreen(
                        photoUri = uri,
                        onNavigateBack = { navController.popBackStack() },
                        onSaved = { navController.navigate(BottomNavRoute.Gallery.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun YanbaoBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF1A1A2E),
        contentColor = Color.White
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF6B9D),
                    selectedTextColor = Color(0xFFFF6B9D),
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = Color(0xFFFF6B9D).copy(alpha = 0.15f)
                )
            )
        }
    }
}
