package com.yanbao.camera.presentation.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 手势返回容器
 *
 * 功能：
 * 1. 左边缘滑动检测（起始 x < edgeWidth，默认 24dp）
 * 2. 跟手位移动画（当前页面随手指右移）
 * 3. 背景页面同步缩放（营造景深感）
 * 4. 松手判断：位移 > 40% 屏幕宽 或 速度 > 800dp/s → 触发 onBack()
 * 5. 取消时弹簧回弹至原位
 *
 * 使用方式：
 * ```kotlin
 * SwipeBackContainer(
 *     enabled = canGoBack,
 *     onBack = { navController.popBackStack() }
 * ) {
 *     // 页面内容
 * }
 * ```
 */
@Composable
fun SwipeBackContainer(
    enabled: Boolean = true,
    onBack: () -> Unit,
    edgeWidthDp: Float = 24f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val edgeWidthPx = with(density) { edgeWidthDp.dp.toPx() }

    // 当前页面水平偏移量（px）
    val offsetX = remember { Animatable(0f) }
    // 是否正在手势滑动中
    var isDragging by remember { mutableStateOf(false) }
    // 是否从左边缘开始拖动
    var isEdgeDrag by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val velocityTracker = remember { VelocityTracker() }

    // 背景遮罩透明度（随偏移量变化，0f=完全遮罩，1f=完全透明）
    val scrimAlpha by remember {
        derivedStateOf { max(0f, 0.5f - (offsetX.value / screenWidthPx) * 0.5f) }
    }

    // 当前页面缩放（轻微缩放营造景深）
    val contentScale by remember {
        derivedStateOf { 1f - (offsetX.value / screenWidthPx) * 0.05f }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 背景遮罩层（前一页面的阴影感）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(scrimAlpha)
                .background(Color.Black)
        )

        // 当前页面内容（随手势偏移）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .scale(if (isDragging) contentScale else 1f)
                .then(
                    if (enabled) {
                        Modifier.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = { startOffset ->
                                    // 仅响应左边缘区域起始的拖动
                                    isEdgeDrag = startOffset.x < edgeWidthPx
                                    if (isEdgeDrag) {
                                        isDragging = true
                                        velocityTracker.resetTracking()
                                    }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    if (isEdgeDrag) {
                                        change.consume()
                                        velocityTracker.addPosition(
                                            change.uptimeMillis,
                                            change.position
                                        )
                                        val newOffset = (offsetX.value + dragAmount).coerceAtLeast(0f)
                                        coroutineScope.launch {
                                            offsetX.snapTo(newOffset)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (isEdgeDrag) {
                                        isDragging = false
                                        val velocity = velocityTracker.calculateVelocity().x
                                        val shouldGoBack =
                                            offsetX.value > screenWidthPx * 0.4f ||
                                            velocity > 800f

                                        coroutineScope.launch {
                                            if (shouldGoBack) {
                                                // 滑出屏幕后触发返回
                                                offsetX.animateTo(
                                                    targetValue = screenWidthPx,
                                                    animationSpec = SpringSpec(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                    )
                                                )
                                                onBack()
                                                offsetX.snapTo(0f)
                                            } else {
                                                // 回弹至原位
                                                offsetX.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = SpringSpec(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                    )
                                                )
                                            }
                                        }
                                        isEdgeDrag = false
                                    }
                                },
                                onDragCancel = {
                                    if (isEdgeDrag) {
                                        isDragging = false
                                        isEdgeDrag = false
                                        coroutineScope.launch {
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = SpringSpec(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    } else Modifier
                )
        ) {
            content()
        }
    }
}
