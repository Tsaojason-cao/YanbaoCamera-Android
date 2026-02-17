package com.yanbao.camera.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.sqrt

/**
 * 双指缩放和平移手势处理
 * 
 * 用于编辑屏幕的图片预览
 */
@Composable
fun PinchToZoomModifier(
    onZoomChanged: (Float) -> Unit = {},
    onOffsetChanged: (Offset) -> Unit = {}
): Modifier {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    return Modifier
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            translationX = offset.x,
            translationY = offset.y
        )
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, gestureZoom, _ ->
                scale = (scale * gestureZoom).coerceIn(1f, 5f)
                offset += pan
                
                onZoomChanged(scale)
                onOffsetChanged(offset)
            }
        }
}

/**
 * 左右滑动切换手势处理
 * 
 * 用于相册详情页切换照片
 */
@Composable
fun SwipeToNavigateModifier(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    swipeThreshold: Float = 100f
): Modifier {
    return Modifier.pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consume()
            
            when {
                dragAmount.x > swipeThreshold -> onSwipeRight()
                dragAmount.x < -swipeThreshold -> onSwipeLeft()
            }
        }
    }
}

/**
 * 长按手势处理
 * 
 * 用于相册长按进入多选模式
 */
@Composable
fun LongPressModifier(
    onLongPress: () -> Unit = {}
): Modifier {
    return Modifier.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = { onLongPress() }
        )
    }
}

/**
 * 双击手势处理
 * 
 * 用于快速点赞或其他双击操作
 */
@Composable
fun DoubleTapModifier(
    onDoubleTap: () -> Unit = {}
): Modifier {
    return Modifier.pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = { onDoubleTap() }
        )
    }
}

/**
 * 缩放手势处理（相机预览）
 * 
 * 用于相机屏幕的双指捏合变焦
 */
@Composable
fun CameraZoomModifier(
    onZoomChanged: (Float) -> Unit = {}
): Modifier {
    var previousDistance = 0f
    
    return Modifier.pointerInput(Unit) {
        detectTransformGestures { _, _, zoom, _ ->
            onZoomChanged(zoom)
        }
    }
}

/**
 * 计算两点之间的距离
 */
fun distance(p1: Offset, p2: Offset): Float {
    return sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y))
}

/**
 * 手势反馈数据类
 */
data class GestureState(
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val rotation: Float = 0f,
    val isLongPress: Boolean = false,
    val isDoubleTap: Boolean = false
)

/**
 * 完整的手势处理组件
 */
@Composable
fun GestureHandler(
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    onZoom: (Float) -> Unit = {},
    content: @Composable () -> Unit
) {
    var gestureState by remember { mutableStateOf(GestureState()) }
    
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .graphicsLayer(
                scaleX = gestureState.scale,
                scaleY = gestureState.scale,
                translationX = gestureState.offset.x,
                translationY = gestureState.offset.y,
                rotationZ = gestureState.rotation
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    gestureState = gestureState.copy(
                        scale = (gestureState.scale * gestureZoom).coerceIn(1f, 5f),
                        offset = gestureState.offset + pan
                    )
                    onZoom(gestureState.scale)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    
                    when {
                        dragAmount.x > 100f -> onSwipeRight()
                        dragAmount.x < -100f -> onSwipeLeft()
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        gestureState = gestureState.copy(isLongPress = true)
                        onLongPress()
                    },
                    onDoubleTap = {
                        gestureState = gestureState.copy(isDoubleTap = true)
                        onDoubleTap()
                    }
                )
            }
    ) {
        content()
    }
}
