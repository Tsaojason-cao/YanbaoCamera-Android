package com.yanbao.camera.presentation.camera

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * 雁宝品牌模式拨盘 —— 满血版
 *
 * 规范（1:1 还原设计稿）：
 * - 布局：水平滚动 Carousel，选中项居中
 * - 选中态标记：选中项上方绘制「粉色兔耳架」（Canvas 矢量绘制）
 * - 文字颜色：选中为品牌粉 (#EC4899)，未选中为白色 60% 透明
 * - 选中项底部有品牌粉下划线
 * - 切换动画：颜色渐变 + 兔耳架淡入
 *
 * 严禁使用 Emoji，所有图形均为 Canvas 矢量绘制。
 */
@Composable
fun ModeSelectorRow(
    modes: List<CameraMode>,
    selectedMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val selectedIndex = modes.indexOf(selectedMode).coerceAtLeast(0)

    LaunchedEffect(selectedIndex) {
        coroutineScope.launch {
            listState.animateScrollToItem(
                index = (selectedIndex - 2).coerceAtLeast(0),
                scrollOffset = 0
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(horizontal = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(modes) { index, mode ->
                val isSelected = mode == selectedMode

                val textColorAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.55f,
                    animationSpec = tween(durationMillis = 200),
                    label = "mode_text_alpha_$index"
                )
                val bunnyEarAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = tween(durationMillis = 250),
                    label = "bunny_ear_alpha_$index"
                )

                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(64.dp)
                        .pointerInput(mode) {
                            detectTapGestures(onTap = { onModeSelected(mode) })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 兔耳架标记（选中态，Canvas 绘制）
                        Canvas(
                            modifier = Modifier
                                .size(width = 40.dp, height = 20.dp)
                                .graphicsLayer { alpha = bunnyEarAlpha }
                        ) {
                            drawBunnyEarMarker(
                                color = Color(0xFFEC4899),
                                alpha = bunnyEarAlpha
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = mode.displayName,
                            color = if (isSelected)
                                Color(0xFFEC4899)
                            else
                                Color.White.copy(alpha = textColorAlpha),
                            fontSize = if (isSelected) 13.sp else 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(2.dp)
                                    .background(Color(0xFFEC4899))
                            )
                        } else {
                            Box(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }

        // 中心选中指示器（固定位置，半透明粉色背景）
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(80.dp)
                .fillMaxHeight()
                .background(Color(0xFFEC4899).copy(alpha = 0.08f))
        )
    }
}

/**
 * 绘制兔耳架标记
 *
 * 形状：两个竖立的椭圆弧（兔耳），底部连接横杆，整体像「U」形架子。
 * 颜色：品牌粉 (#EC4899)，严禁使用 Emoji。
 */
private fun DrawScope.drawBunnyEarMarker(
    color: Color,
    alpha: Float
) {
    val strokeWidth = 2.5.dp.toPx()
    val earColor = color.copy(alpha = alpha)
    val centerX = size.width / 2f
    val bottomY = size.height * 0.95f

    // 左耳（椭圆轮廓）
    val leftEarX = centerX - size.width * 0.25f
    drawOval(
        color = earColor,
        topLeft = Offset(leftEarX - size.width * 0.10f, 0f),
        size = Size(size.width * 0.20f, size.height * 0.80f),
        style = Stroke(width = strokeWidth)
    )

    // 右耳（椭圆轮廓）
    val rightEarX = centerX + size.width * 0.25f
    drawOval(
        color = earColor,
        topLeft = Offset(rightEarX - size.width * 0.10f, 0f),
        size = Size(size.width * 0.20f, size.height * 0.80f),
        style = Stroke(width = strokeWidth)
    )

    // 底部横杆
    drawLine(
        color = earColor,
        start = Offset(leftEarX - size.width * 0.10f, bottomY),
        end = Offset(rightEarX + size.width * 0.10f, bottomY),
        strokeWidth = strokeWidth
    )

    // 左耳内填充（浅粉色）
    drawOval(
        color = color.copy(alpha = alpha * 0.25f),
        topLeft = Offset(leftEarX - size.width * 0.08f, size.height * 0.05f),
        size = Size(size.width * 0.16f, size.height * 0.65f)
    )

    // 右耳内填充（浅粉色）
    drawOval(
        color = color.copy(alpha = alpha * 0.25f),
        topLeft = Offset(rightEarX - size.width * 0.08f, size.height * 0.05f),
        size = Size(size.width * 0.16f, size.height * 0.65f)
    )
}
