package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 视频大师面板 — 严格 1:1 还原 CAM_07_video.png
 *
 * 布局（底部 28% 曜石黑毛玻璃面板内）：
 *  - 分辨率+帧率选择行（6个胶囊）：
 *    4K(粉色选中) / 1080P / 720P  |  24fps / 30fps / 60fps(粉色选中)
 *  - 3个参数滑块（粉色轨道）：
 *    电影模式 开启 / 防抖级别 80 / 音量 65
 *
 * 颜色规范：
 *  - 选中胶囊：品牌粉 #EC4899 实心
 *  - 未选中胶囊：深色 #2A2A2A
 *  - 滑块轨道：品牌粉 #EC4899
 *  - 参数数值：粉色 #EC4899
 */
@Composable
fun VideoMasterPanel(
    isRecording: Boolean = false,
    modifier: Modifier = Modifier
) {
    val brandPink = Color(0xFFEC4899)

    val resolutions = listOf("4K", "1080P", "720P")
    val frameRates = listOf("24fps", "30fps", "60fps")
    var selectedRes by remember { mutableStateOf(0) }    // 4K 默认
    var selectedFps by remember { mutableStateOf(2) }    // 60fps 默认

    var cinemaMode by remember { mutableStateOf(0.80f) }  // 电影模式 开启 (高值=开启)
    var stabilizer by remember { mutableStateOf(0.80f) }  // 防抖级别 80
    var volume by remember { mutableStateOf(0.65f) }      // 音量 65

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── 分辨率 + 帧率选择行 ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 分辨率（3个）
            resolutions.forEachIndexed { index, label ->
                val isSelected = selectedRes == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) brandPink else Color(0xFF2A2A2A))
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedRes = index }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 帧率（3个）
            frameRates.forEachIndexed { index, label ->
                val isSelected = selectedFps == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) brandPink else Color(0xFF2A2A2A))
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedFps = index }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // ── 3个参数滑块（横向排列）──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VideoParamSlider(
                label = "电影模式",
                value = cinemaMode,
                valueLabel = if (cinemaMode > 0.5f) "开启" else "关闭",
                onValueChange = { cinemaMode = it },
                modifier = Modifier.weight(1f)
            )
            VideoParamSlider(
                label = "防抖级别",
                value = stabilizer,
                valueLabel = "${(stabilizer * 100).toInt()}",
                onValueChange = { stabilizer = it },
                modifier = Modifier.weight(1f)
            )
            VideoParamSlider(
                label = "音量",
                value = volume,
                valueLabel = "${(volume * 100).toInt()}",
                onValueChange = { volume = it },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 视频参数滑块（标签+粉色数值+粉色轨道）
 */
@Composable
fun VideoParamSlider(
    label: String,
    value: Float,
    valueLabel: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val brandPink = Color(0xFFEC4899)
    var sliderWidth by remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 标签行：电影模式 开启
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueLabel,
                color = brandPink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 粉色滑块
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        if (sliderWidth > 0f) {
                            onValueChange((change.position.x / sliderWidth).coerceIn(0f, 1f))
                        }
                    }
                }
        ) {
            sliderWidth = size.width
            val cy = size.height / 2f
            val tx = value * size.width
            val th = 3.dp.toPx()
            val tr = 8.dp.toPx()

            // 背景轨道
            drawRoundRect(
                brandPink.copy(alpha = 0.2f),
                Offset(0f, cy - th / 2f),
                Size(size.width, th),
                CornerRadius(th / 2f)
            )
            // 填充轨道（粉色）
            if (tx > 0f) {
                drawRoundRect(
                    brandPink,
                    Offset(0f, cy - th / 2f),
                    Size(tx, th),
                    CornerRadius(th / 2f)
                )
            }
            // Thumb（白色外圈 + 粉色内圈）
            drawCircle(Color.White.copy(alpha = 0.9f), tr + 2.dp.toPx(), Offset(tx, cy))
            drawCircle(brandPink, tr, Offset(tx, cy))
        }
    }
}
