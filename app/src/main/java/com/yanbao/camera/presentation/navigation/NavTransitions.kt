package com.yanbao.camera.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.navigation.NavBackStackEntry

/**
 * 雁寶AI相机统一页面切换动画
 *
 * 动画策略：
 * - 子页面（详情/编辑等）：从右侧滑入（300ms，EaseOutCubic）
 * - 子页面退出：向右侧滑出（250ms，EaseInCubic）
 * - Tab 主页面切换：淡入淡出（200ms，避免与手势返回冲突）
 * - 弹出返回：当前页向右滑出，前一页从左侧轻微位移恢复
 */
object NavTransitions {

    // 子页面路由集合（需要滑入动画）
    private val subPageRoutes = setOf(
        "photo_detail",
        "memory_detail",
        "yanbao_memory_detail",
        "profile_edit"
    )

    // 主 Tab 路由集合（淡入淡出）
    private val tabRoutes = setOf(
        "home", "camera", "editor", "gallery", "lbs", "profile"
    )

    /** 进入动画 */
    fun enterTransition(
        initialState: NavBackStackEntry,
        targetState: NavBackStackEntry
    ): EnterTransition {
        val targetRoute = targetState.destination.route?.substringBefore("/") ?: ""
        return when {
            subPageRoutes.any { targetRoute.startsWith(it) } -> {
                // 子页面从右侧滑入
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 200),
                    initialAlpha = 0.8f
                )
            }
            tabRoutes.contains(targetRoute) -> {
                // Tab 切换淡入
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = LinearOutSlowInEasing
                    )
                )
            }
            else -> {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    /** 退出动画（当前页面被新页面覆盖时） */
    fun exitTransition(
        initialState: NavBackStackEntry,
        targetState: NavBackStackEntry
    ): ExitTransition {
        val targetRoute = targetState.destination.route?.substringBefore("/") ?: ""
        return when {
            subPageRoutes.any { targetRoute.startsWith(it) } -> {
                // 当前页向左轻微位移（景深感）
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -(fullWidth / 4) },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200),
                    targetAlpha = 0.7f
                )
            }
            tabRoutes.contains(targetRoute) -> {
                // Tab 切换淡出
                fadeOut(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutLinearInEasing
                    )
                )
            }
            else -> {
                fadeOut(animationSpec = tween(durationMillis = 200))
            }
        }
    }

    /** 弹出返回时的进入动画（前一页面重新出现） */
    fun popEnterTransition(
        initialState: NavBackStackEntry,
        targetState: NavBackStackEntry
    ): EnterTransition {
        val initialRoute = initialState.destination.route?.substringBefore("/") ?: ""
        return when {
            subPageRoutes.any { initialRoute.startsWith(it) } -> {
                // 前一页从左侧轻微位移恢复（景深感）
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -(fullWidth / 4) },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 200),
                    initialAlpha = 0.7f
                )
            }
            else -> fadeIn(animationSpec = tween(durationMillis = 200))
        }
    }

    /** 弹出返回时的退出动画（当前子页面向右滑出） */
    fun popExitTransition(
        initialState: NavBackStackEntry,
        targetState: NavBackStackEntry
    ): ExitTransition {
        val initialRoute = initialState.destination.route?.substringBefore("/") ?: ""
        return when {
            subPageRoutes.any { initialRoute.startsWith(it) } -> {
                // 当前子页面向右滑出
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = 280,
                        easing = FastOutLinearInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200),
                    targetAlpha = 0.8f
                )
            }
            else -> fadeOut(animationSpec = tween(durationMillis = 200))
        }
    }
}
