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
 * 美颜塑形内嵌面板 — 严格 1:1 还原 CAM_06_beauty.png
 *
 * 布局（底部 28% 曜石黑毛玻璃面板内）：
 *  - 分类标签（5个粉色胶囊）：皮肤 / 脸型 / 五官 / 亮眼 / 身材
 *  - 4个参数滑块（2行2列，胡萝卜橙）：
 *    [磨皮] 60 / [美白] 45
 *    [瘦脸] 35 / [大眼] 40
 *  - 预设按钮（4个深色胶囊）：自然(选中) / 精致 / 网红 / 清零
 *
 * 颜色规范：
 *  - 分类标签：粉色 #EC4899 实心胶囊
 *  - 滑块：胡萝卜橙 #F97316
 *  - 数值气泡：橙色圆形
 *  - 预设按钮：深色 #2A2A2A，选中时白色文字加粗
 */
@Composable
fun BeautyShapeModePanel(modifier: Modifier = Modifier) {
    val brandPink = Color(0xFFEC4899)
    val carrotOrange = Color(0xFFF97316)

    // 分类标签
    val categories = listOf("皮肤", "脸型", "五官", "亮眼", "身材")
    var selectedCategory by remember { mutableStateOf(0) }

    // 4个美颜参数
    var skinSmooth by remember { mutableStateOf(0.60f) }
    var skinWhiten by remember { mutableStateOf(0.45f) }
    var faceThin by remember { mutableStateOf(0.35f) }
    var eyeEnlarge by remember { mutableStateOf(0.40f) }

    // 预设
    val presets = listOf("自然", "精致", "网红", "清零")
    var selectedPreset by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── 分类标签（5个粉色胶囊）───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEachIndexed { index, label ->
                val isSelected = selectedCategory == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) brandPink else brandPink.copy(alpha = 0.25f))
                        .clickable { selectedCategory = index }
                        .padding(horizontal = 12.dp, vertical = 5.dp),
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

        // ── 4个参数滑块（2行2列）─────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 左列：磨皮
            BeautyParamSlider(
                label = "磨皮",
                value = skinSmooth,
                onValueChange = { skinSmooth = it },
                modifier = Modifier.weight(1f)
            )
            // 右列：美白
            BeautyParamSlider(
                label = "美白",
                value = skinWhiten,
                onValueChange = { skinWhiten = it },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 左列：瘦脸
            BeautyParamSlider(
                label = "瘦脸",
                value = faceThin,
                onValueChange = { faceThin = it },
                modifier = Modifier.weight(1f)
            )
            // 右列：大眼
            BeautyParamSlider(
                label = "大眼",
                value = eyeEnlarge,
                onValueChange = { eyeEnlarge = it },
                modifier = Modifier.weight(1f)
            )
        }

        // ── 预设按钮（4个深色胶囊）───────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEachIndexed { index, label ->
                val isSelected = selectedPreset == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF3A3A3A) else Color(0xFF2A2A2A))
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedPreset = index }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * 美颜参数滑块（带标签和橙色数值气泡）
 * 对应设计图：[磨皮] 60 / [美白] 45 / [瘦脸] 35 / [大眼] 40
 */
@Composable
fun BeautyParamSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val carrotOrange = Color(0xFFF97316)
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    val displayValue = (value * 100).toInt()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 标签行：[磨皮] 60（橙色气泡数值）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "[$label]",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            // 橙色圆形数值气泡
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
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
                .height(24.dp)
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
            val tr = 8.dp.toPx()

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
