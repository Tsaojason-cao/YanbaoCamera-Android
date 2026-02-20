package com.yanbao.camera.presentation.navigation

/**
 * 5 个核心导航目的地
 * 
 * 严格按照用户要求：主页、相机、相册、推荐、我的
 */
sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "主页")
    object Camera : Screen("camera", "相机")
    object Gallery : Screen("gallery", "相册")
    object Recommend : Screen("recommend", "推荐")
    object Profile : Screen("profile", "我的")
}
