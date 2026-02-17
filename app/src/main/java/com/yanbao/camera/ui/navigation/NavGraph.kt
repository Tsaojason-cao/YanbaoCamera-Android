package com.yanbao.camera.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * 应用导航路由定义
 */
object NavRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val CAMERA = "camera"
    const val EDIT = "edit"
    const val GALLERY = "gallery"
    const val RECOMMEND = "recommend"
    const val PROFILE = "profile"
}

/**
 * 应用导航图
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = NavRoutes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.SPLASH) {
            // SplashScreen()
        }
        composable(NavRoutes.HOME) {
            // HomeScreen()
        }
        composable(NavRoutes.CAMERA) {
            // CameraScreen()
        }
        composable(NavRoutes.EDIT) {
            // EditScreen()
        }
        composable(NavRoutes.GALLERY) {
            // GalleryScreen()
        }
        composable(NavRoutes.RECOMMEND) {
            // RecommendScreen()
        }
        composable(NavRoutes.PROFILE) {
            // ProfileScreen()
        }
    }
}
