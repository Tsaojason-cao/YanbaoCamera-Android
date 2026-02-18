package com.yanbao.camera.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yanbao.camera.ui.screens.SplashScreen
import com.yanbao.camera.ui.screens.HomeScreen
import com.yanbao.camera.ui.screens.CameraScreen
import com.yanbao.camera.ui.screens.EditScreen
import com.yanbao.camera.ui.screens.GalleryScreen
import com.yanbao.camera.ui.screens.RecommendScreen
import com.yanbao.camera.ui.screens.ProfileScreen
import com.yanbao.camera.ui.screens.LoginScreen
import com.yanbao.camera.ui.screens.RegisterScreen

/**
 * 应用导航路由定义
 */
object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
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
    startDestination: String = NavRoutes.SPLASH,
    onRouteChanged: (String) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(navController)
        }
        composable(NavRoutes.REGISTER) {
            RegisterScreen(navController)
        }
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.HOME) {
            HomeScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        composable(NavRoutes.CAMERA) {
            CameraScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        composable(NavRoutes.EDIT) {
            EditScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        composable(NavRoutes.GALLERY) {
            GalleryScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        composable(NavRoutes.RECOMMEND) {
            RecommendScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        composable(NavRoutes.PROFILE) {
            ProfileScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(NavRoutes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
    }
}
