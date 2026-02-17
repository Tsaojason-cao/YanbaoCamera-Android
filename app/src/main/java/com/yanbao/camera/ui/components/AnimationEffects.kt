package com.yanbao.camera.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.yanbao.camera.ui.theme.AccentPink
import com.yanbao.camera.ui.theme.TextWhite
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

/**
 * 带动画效果的点赞按钮
 * 
 * 功能：
 * - 点击时心形放大再缩小
 * - 颜色变化（灰色 -> 粉色）
 * - 平滑的过渡动画
 */
@Composable
fun AnimatedLikeButton(
    isLiked: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    // 缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "like_scale"
    )
    
    // 颜色动画
    val iconColor by animateColorAsState(
        targetValue = if (isLiked) AccentPink else Color.Gray,
        animationSpec = tween(durationMillis = 300),
        label = "like_color"
    )
    
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                    // 重置按压状态
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(300)
                        isPressed = false
                    }
                }
            )
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "点赞",
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 带弹性缩放效果的导航栏图标
 * 
 * 功能：
 * - 选中时弹性缩放（0.9 -> 1.0）
 * - 平滑的过渡动画
 */
@Composable
fun AnimatedNavIcon(
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    icon: ImageVector,
    contentDescription: String = "",
    modifier: Modifier = Modifier
) {
    // 弹性缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.9f,
        animationSpec = tween(durationMillis = 300),
        label = "nav_scale"
    )
    
    // 颜色动画
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) AccentPink else Color.Gray,
        animationSpec = tween(durationMillis = 300),
        label = "nav_color"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 卡片进入动画
 * 
 * 功能：
 * - 从下方滑入
 * - 淡入效果
 */
@Composable
fun SlideInCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // 透明度动画
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 500),
        label = "card_alpha"
    )
    
    // Y轴偏移动画
    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 20.dp,
        animationSpec = tween(durationMillis = 500),
        label = "card_offset"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer(
                alpha = alpha,
                translationY = offsetY.value
            )
    ) {
        content()
    }
    
    // 在首次组合时触发动画
    androidx.compose.runtime.LaunchedEffect(Unit) {
        isVisible = true
    }
}

/**
 * 脉冲动画（用于加载状态）
 */
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = tween(durationMillis = 1000),
        label = "pulse"
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}

/**
 * 旋转加载动画
 */
@Composable
fun RotatingLoadingIndicator(
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = 360f,
        animationSpec = tween(durationMillis = 2000),
        label = "rotation"
    )
    
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(AccentPink.copy(alpha = 0.2f))
            .graphicsLayer(rotationZ = rotation),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
        )
    }
}

/**
 * 震动反馈（需要在实际设备上测试）
 */
@Composable
fun HapticFeedback(
    trigger: Boolean = false,
    onFeedback: () -> Unit = {}
) {
    androidx.compose.runtime.LaunchedEffect(trigger) {
        if (trigger) {
            onFeedback()
        }
    }
}

/**
 * 图形层扩展函数
 */
fun Modifier.graphicsLayer(
    alpha: Float = 1.0f,
    translationY: Float = 0f,
    rotationZ: Float = 0f
): Modifier {
    return this.then(
        Modifier.graphicsLayer(
            alpha = alpha,
            translationY = translationY,
            rotationZ = rotationZ
        )
    )
}

