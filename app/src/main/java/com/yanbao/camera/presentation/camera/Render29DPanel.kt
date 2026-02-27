package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 29D渲染参数面板 — 严格 1:1 还原 CAM_04_29d_render.png
 *
 * 布局（底部 28% 曜石黑毛玻璃面板内）：
 *  - 4个参数滑块（2行2列，胡萝卜橙）：
 *    [光影维度] 85 / [材质维度] 70
 *    [色彩维度] 90 / [空间维度] 65
 *  - 数值气泡：橙色圆形气泡（85/70/90/65）
 *
 * 颜色规范：
 *  - 滑块轨道填充：胡萝卜橙 #F97316
 *  - 数值气泡：橙色圆形
 */
@Composable
fun Render29DPanel(modifier: Modifier = Modifier) {
    val carrotOrange = Color(0xFFF97316)

    var lightShadow by remember { mutableStateOf(0.85f) }
    var material by remember { mutableStateOf(0.70f) }
    var colorDim by remember { mutableStateOf(0.90f) }
    var spaceDim by remember { mutableStateOf(0.65f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 第一行：光影维度 + 材质维度
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RenderDimSlider(
                label = "光影维度",
                value = lightShadow,
                onValueChange = { lightShadow = it },
                modifier = Modifier.weight(1f)
            )
            RenderDimSlider(
                label = "材质维度",
                value = material,
                onValueChange = { material = it },
                modifier = Modifier.weight(1f)
            )
        }

        // 第二行：色彩维度 + 空间维度
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RenderDimSlider(
                label = "色彩维度",
                value = colorDim,
                onValueChange = { colorDim = it },
                modifier = Modifier.weight(1f)
            )
            RenderDimSlider(
                label = "空间维度",
                value = spaceDim,
                onValueChange = { spaceDim = it },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 渲染维度滑块（带方括号标签和橙色数值气泡）
 * 对应设计图：[光影维度] 85 / [材质维度] 70 / [色彩维度] 90 / [空间维度] 65
 */
@Composable
fun RenderDimSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val carrotOrange = Color(0xFFF97316)
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    val displayValue = (value * 100).toInt()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 标签行：[光影维度] + 橙色气泡数值
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "[$label]",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            // 橙色圆形数值气泡
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(carrotOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$displayValue",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 滑块
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        onValueChange((change.position.x / sliderWidth).coerceIn(0f, 1f))
                    }
                }
        ) {
            sliderWidth = size.width
            val cy = size.height / 2f
            val tx = value * size.width
            val th = 3.dp.toPx()
            val tr = 9.dp.toPx()

            // 背景轨道
            drawRoundRect(
                Color.White.copy(alpha = 0.15f),
                Offset(0f, cy - th / 2f),
                Size(size.width, th),
                CornerRadius(th / 2f)
            )
            // 填充轨道（胡萝卜橙）
            if (tx > 0f) {
                drawRoundRect(
                    carrotOrange,
                    Offset(0f, cy - th / 2f),
                    Size(tx, th),
                    CornerRadius(th / 2f)
                )
            }
            // Thumb（橙色圆形）
            drawCircle(Color.White.copy(alpha = 0.9f), tr + 2.dp.toPx(), Offset(tx, cy))
            drawCircle(carrotOrange, tr, Offset(tx, cy))
        }
    }
}
