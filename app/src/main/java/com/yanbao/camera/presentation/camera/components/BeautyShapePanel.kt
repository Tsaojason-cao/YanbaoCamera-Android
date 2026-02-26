package com.yanbao.camera.presentation.camera.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.data.model.BeautyShapeParams

// ─────────────────────────────────────────────────────────────────────────────
// 品牌色常量
// ─────────────────────────────────────────────────────────────────────────────
private val YanbaoPink = Color(0xFFEC4899)
private val YanbaoOrange = Color(0xFFF97316)
private val YanbaoBlack = Color(0xFF0A0A0A)
private val GlassBlack = Color(0xCC0A0A0A)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFF9CA3AF)

/**
 * 美颜塑形面板（满血版 v2.0）
 *
 * 对标竞品: B612、轻颜相机、Ulike、美颜相机、SNOW
 * 功能分类:
 *   - 皮肤 (10项): 磨皮/美白/祛痘/祛斑/去黑眼圈/去法令纹/去颈纹/缩毛孔/去红血丝/肤色
 *   - 脸型 (8项): 瘦脸/V脸/小脸/下颌骨/发际线/窄颧骨/下巴/额头
 *   - 五官 (11项): 大眼/眼距/眼角/眼睛倾斜/隆鼻/瘦鼻/鼻长/嘴型/嘴唇/白牙/牙齿矫正
 *   - 亮眼 (3项): 亮眼/眼白/眼神光
 *   - 身材 (4项): 长腿/瘦腰/瘦肩/丰胸
 */
@Composable
fun BeautyShapePanel(
    params: BeautyShapeParams,
    onParamsChange: (BeautyShapeParams) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(BeautyCategory.SKIN) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassBlack)
    ) {
        // ── 分类 Tab ──────────────────────────────────────────────────────
        BeautyCategoryTabs(
            selected = selectedCategory,
            onSelect = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── 参数滑块区域 ──────────────────────────────────────────────────
        AnimatedContent(
            targetState = selectedCategory,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                slideOutHorizontally { -it } + fadeOut()
            },
            label = "beauty_category"
        ) { category ->
            BeautySliderGrid(
                category = category,
                params = params,
                onParamsChange = onParamsChange
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── 底部：预设快捷键 ───────────────────────────────────────────────
        BeautyPresetRow(params = params, onParamsChange = onParamsChange)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 分类 Tab
// ─────────────────────────────────────────────────────────────────────────────

enum class BeautyCategory(val label: String, val icon: String) {
    SKIN("皮肤", "✦"),
    FACE_SHAPE("脸型", "◈"),
    FEATURES("五官", "◉"),
    EYES("亮眼", "◎"),
    BODY("身材", "◇")
}

@Composable
private fun BeautyCategoryTabs(
    selected: BeautyCategory,
    onSelect: (BeautyCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(BeautyCategory.values()) { category ->
            val isSelected = category == selected
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) YanbaoPink else Color.White.copy(alpha = 0.1f),
                label = "tab_bg"
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .clickable { onSelect(category) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.label,
                    color = if (isSelected) Color.White else TextGray,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 参数滑块网格
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BeautySliderGrid(
    category: BeautyCategory,
    params: BeautyShapeParams,
    onParamsChange: (BeautyShapeParams) -> Unit
) {
    val items = getBeautyItemsForCategory(category, params, onParamsChange)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    BeautySliderItem(
                        label = item.label,
                        value = item.value,
                        range = item.range,
                        onValueChange = item.onValueChange,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 如果行中只有一个元素，补充空白
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class BeautySliderItemData(
    val label: String,
    val value: Int,
    val range: IntRange = 0..100,
    val onValueChange: (Int) -> Unit
)

@Composable
private fun BeautySliderItem(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBubble by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // 标签 + 数值
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextGray,
                fontSize = 11.sp
            )
            // 数值气泡（黑色40%透明）
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (value >= 0) "+$value" else "$value",
                    color = if (value != 0) YanbaoPink else TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 胡萝卜滑块
        CarrotSlider(
            value = value.toFloat(),
            valueRange = range.first.toFloat()..range.last.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            onValueChangeFinished = { showBubble = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 胡萝卜形状滑块（Canvas 原生绘制）
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CarrotSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        modifier = modifier.height(32.dp),
        colors = SliderDefaults.colors(
            thumbColor = YanbaoOrange,
            activeTrackColor = YanbaoOrange.copy(alpha = 0.8f),
            inactiveTrackColor = Color.White.copy(alpha = 0.2f),
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        ),
        thumb = {
            // 胡萝卜形状 Thumb（Canvas 绘制）
            Canvas(
                modifier = Modifier.size(28.dp, 36.dp)
            ) {
                drawCarrotThumb(this)
            }
        }
    )
}

/**
 * 在 Canvas 上绘制胡萝卜形状的 Thumb
 * 胡萝卜 = 橙色三角形身体 + 绿色叶子顶部
 */
private fun drawCarrotThumb(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    // 胡萝卜身体（橙色圆角三角形）
    val bodyPath = Path().apply {
        moveTo(w / 2f, h * 0.95f)          // 底部尖端
        cubicTo(
            w * 0.1f, h * 0.7f,             // 左侧曲线
            w * 0.05f, h * 0.35f,            // 左上
            w / 2f, h * 0.35f               // 顶部中心
        )
        cubicTo(
            w * 0.95f, h * 0.35f,            // 右上
            w * 0.9f, h * 0.7f,              // 右侧曲线
            w / 2f, h * 0.95f               // 回到底部
        )
        close()
    }
    scope.drawPath(bodyPath, color = Color(0xFFF97316))

    // 胡萝卜叶子（绿色小叶片）
    val leafColor = Color(0xFF4ADE80)
    // 中间叶
    scope.drawLine(
        color = leafColor,
        start = Offset(w / 2f, h * 0.35f),
        end = Offset(w / 2f, h * 0.05f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
    // 左叶
    scope.drawLine(
        color = leafColor,
        start = Offset(w / 2f, h * 0.25f),
        end = Offset(w * 0.25f, h * 0.05f),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
    // 右叶
    scope.drawLine(
        color = leafColor,
        start = Offset(w / 2f, h * 0.25f),
        end = Offset(w * 0.75f, h * 0.05f),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )

    // 高光（白色半透明）
    scope.drawOval(
        color = Color.White.copy(alpha = 0.3f),
        topLeft = Offset(w * 0.3f, h * 0.4f),
        size = androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.15f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// 预设快捷键行
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BeautyPresetRow(
    params: BeautyShapeParams,
    onParamsChange: (BeautyShapeParams) -> Unit
) {
    val presets = listOf(
        "自然" to BeautyShapeParams(smoothness = 30, whitening = 20, faceThin = 15, eyeEnlarge = 10),
        "精致" to BeautyShapeParams(smoothness = 60, whitening = 40, faceThin = 30, eyeEnlarge = 25, noseThin = 20, vFace = 20),
        "网红" to BeautyShapeParams(smoothness = 80, whitening = 60, faceThin = 50, eyeEnlarge = 40, noseThin = 35, vFace = 40, legLengthen = 30),
        "清零" to BeautyShapeParams()
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(presets) { (name, preset) ->
            val isActive = name != "清零" && params == preset
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isActive) YanbaoPink.copy(alpha = 0.3f)
                        else Color.White.copy(alpha = 0.08f)
                    )
                    .border(
                        width = if (isActive) 1.dp else 0.dp,
                        color = if (isActive) YanbaoPink else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onParamsChange(preset) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = name,
                    color = if (isActive) YanbaoPink else TextGray,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 参数项生成函数
// ─────────────────────────────────────────────────────────────────────────────

private fun getBeautyItemsForCategory(
    category: BeautyCategory,
    params: BeautyShapeParams,
    onParamsChange: (BeautyShapeParams) -> Unit
): List<BeautySliderItemData> = when (category) {

    BeautyCategory.SKIN -> listOf(
        BeautySliderItemData("磨皮", params.smoothness) { onParamsChange(params.copy(smoothness = it)) },
        BeautySliderItemData("美白", params.whitening) { onParamsChange(params.copy(whitening = it)) },
        BeautySliderItemData("祛痘", params.acneRemoval) { onParamsChange(params.copy(acneRemoval = it)) },
        BeautySliderItemData("祛斑", params.blemishRemoval) { onParamsChange(params.copy(blemishRemoval = it)) },
        BeautySliderItemData("去黑眼圈", params.darkCircleRemoval) { onParamsChange(params.copy(darkCircleRemoval = it)) },
        BeautySliderItemData("去法令纹", params.nasolabialFolds) { onParamsChange(params.copy(nasolabialFolds = it)) },
        BeautySliderItemData("去颈纹", params.neckLines) { onParamsChange(params.copy(neckLines = it)) },
        BeautySliderItemData("缩毛孔", params.poreMinimizer) { onParamsChange(params.copy(poreMinimizer = it)) },
        BeautySliderItemData("去红血丝", params.rednessRemoval) { onParamsChange(params.copy(rednessRemoval = it)) },
        BeautySliderItemData("肤色调整", params.skinTone, -50..50) { onParamsChange(params.copy(skinTone = it)) }
    )

    BeautyCategory.FACE_SHAPE -> listOf(
        BeautySliderItemData("瘦脸", params.faceThin) { onParamsChange(params.copy(faceThin = it)) },
        BeautySliderItemData("V脸", params.vFace) { onParamsChange(params.copy(vFace = it)) },
        BeautySliderItemData("小脸", params.smallFace) { onParamsChange(params.copy(smallFace = it)) },
        BeautySliderItemData("瘦下颌骨", params.jawbone) { onParamsChange(params.copy(jawbone = it)) },
        BeautySliderItemData("发际线", params.hairline, -50..50) { onParamsChange(params.copy(hairline = it)) },
        BeautySliderItemData("窄颧骨", params.cheekbone) { onParamsChange(params.copy(cheekbone = it)) },
        BeautySliderItemData("下巴长度", params.chinLength, -50..50) { onParamsChange(params.copy(chinLength = it)) },
        BeautySliderItemData("额头高度", params.foreheadHeight, -50..50) { onParamsChange(params.copy(foreheadHeight = it)) }
    )

    BeautyCategory.FEATURES -> listOf(
        BeautySliderItemData("大眼", params.eyeEnlarge) { onParamsChange(params.copy(eyeEnlarge = it)) },
        BeautySliderItemData("眼距", params.eyeDistance, -50..50) { onParamsChange(params.copy(eyeDistance = it)) },
        BeautySliderItemData("眼角", params.eyeCorner) { onParamsChange(params.copy(eyeCorner = it)) },
        BeautySliderItemData("眼睛倾斜", params.eyeTilt, -50..50) { onParamsChange(params.copy(eyeTilt = it)) },
        BeautySliderItemData("隆鼻", params.noseLift) { onParamsChange(params.copy(noseLift = it)) },
        BeautySliderItemData("瘦鼻", params.noseThin) { onParamsChange(params.copy(noseThin = it)) },
        BeautySliderItemData("鼻子长度", params.noseLength, -50..50) { onParamsChange(params.copy(noseLength = it)) },
        BeautySliderItemData("嘴型", params.lipShape, -50..50) { onParamsChange(params.copy(lipShape = it)) },
        BeautySliderItemData("嘴唇大小", params.lipSize, -50..50) { onParamsChange(params.copy(lipSize = it)) },
        BeautySliderItemData("白牙", params.teethWhitening) { onParamsChange(params.copy(teethWhitening = it)) },
        BeautySliderItemData("牙齿矫正", params.teethStraighten) { onParamsChange(params.copy(teethStraighten = it)) }
    )

    BeautyCategory.EYES -> listOf(
        BeautySliderItemData("亮眼", params.eyeBrightness) { onParamsChange(params.copy(eyeBrightness = it)) },
        BeautySliderItemData("眼白", params.eyeWhitening) { onParamsChange(params.copy(eyeWhitening = it)) },
        BeautySliderItemData("眼神光", params.eyeHighlight) { onParamsChange(params.copy(eyeHighlight = it)) }
    )

    BeautyCategory.BODY -> listOf(
        BeautySliderItemData("长腿", params.legLengthen) { onParamsChange(params.copy(legLengthen = it)) },
        BeautySliderItemData("瘦腰", params.waistThin) { onParamsChange(params.copy(waistThin = it)) },
        BeautySliderItemData("瘦肩", params.shoulderThin) { onParamsChange(params.copy(shoulderThin = it)) },
        BeautySliderItemData("丰胸", params.bustEnhance) { onParamsChange(params.copy(bustEnhance = it)) }
    )
}
