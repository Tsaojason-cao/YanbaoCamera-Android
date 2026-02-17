package com.yanbao.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yanbao.camera.ui.components.YanbaoBottomNavigation
import com.yanbao.camera.ui.components.bottomNavItems
import com.yanbao.camera.ui.navigation.NavRoutes
import com.yanbao.camera.ui.screens.CameraScreen
import com.yanbao.camera.ui.screens.EditScreen
import com.yanbao.camera.ui.screens.GalleryScreen
import com.yanbao.camera.ui.screens.HomeScreen
import com.yanbao.camera.ui.screens.ProfileScreen
import com.yanbao.camera.ui.screens.RecommendScreen
import com.yanbao.camera.ui.screens.SplashScreen

@Composable
fun YanbaoApp() {
    val navController = rememberNavController()
    val showSplash = remember { mutableStateOf(true) }

    if (showSplash.value) {
        SplashScreen(
            onSplashFinished = {
                showSplash.value = false
                navController.navigate(NavRoutes.HOME) {
                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                }
            }
        )
    } else {
        Scaffold(
            bottomBar = {
                YanbaoBottomNavigation(
                    currentRoute = getCurrentRoute(navController),
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.HOME
                ) {
                    composable(NavRoutes.HOME) {
                        HomeScreen()
                    }
                    composable(NavRoutes.CAMERA) {
                        CameraScreen()
                    }
                    composable(NavRoutes.GALLERY) {
                        GalleryScreen()
                    }
                    composable(NavRoutes.RECOMMEND) {
                        RecommendScreen()
                    }
                    composable(NavRoutes.PROFILE) {
                        ProfileScreen()
                    }
                    composable(NavRoutes.EDIT) {
                        EditScreen(onBack = {
                            navController.popBackStack()
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun getCurrentRoute(navController: NavHostController): String {
    return navController.currentBackStackEntry?.destination?.route
        ?.let { route ->
            bottomNavItems.find { it.route == route }?.route ?: NavRoutes.HOME
        } ?: NavRoutes.HOME
}
