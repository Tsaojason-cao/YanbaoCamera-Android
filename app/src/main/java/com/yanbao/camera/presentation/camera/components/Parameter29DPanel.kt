// app/src/main/java/com/yanbao/camera/presentation/camera/components/Parameter29DPanel.kt
package com.yanbao.camera.presentation.camera.components

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.presentation.camera.ALL_29D_PARAMS
import com.yanbao.camera.presentation.camera.Param29DDescriptor

// ─────────────────────────────────────────────
// 颜色常量
// ─────────────────────────────────────────────
private val PinkHighlight = Color(0xFFEC4899)
private val PanelBg       = Color(0xFF111111)
private val SliderTrack   = Color(0xFF333333)

/**
 * 29D 专业参数面板（独立组件版本）
 *
 * 支持：
 * - 5 个参数分组 Tab（基础曝光/色彩/色彩通道/明暗细节/质感+美颜）
 * - 29 个参数滑块，每个滑块实时输出 Logcat 日志
 * - 修改指示点（粉色圆点标记已调整的参数）
 * - 一键重置所有参数到默认值
 */
@Composable
fun Parameter29DPanel(
    params: Map<String, Float>,
    onParameterChange: (String, Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groups = ALL_29D_PARAMS.groupBy { it.group }
    var expandedGroup by remember { mutableStateOf(groups.keys.first()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.65f)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(PanelBg.copy(alpha = 0.97f))
    ) {
        // ── 把手 ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
                    .clickable { onDismiss() }
            )
        }

        // ── 标题栏 ────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CTRL️ 29D 专业调优",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${ALL_29D_PARAMS.count { (params[it.key] ?: it.default) != it.default }} / 29 项已调整",
                    color = PinkHighlight,
                    fontSize = 11.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip(
                    label = "重置",
                    bgColor = Color.White.copy(alpha = 0.1f),
                    textColor = Color.White.copy(alpha = 0.7f)
                ) {
                    ALL_29D_PARAMS.forEach { p -> onParameterChange(p.key, p.default) }
                    Log.d("Parameter29DPanel", "所有 29D 参数已重置为默认值")
                }
                ActionChip(
                    label = "收起",
                    bgColor = PinkHighlight.copy(alpha = 0.2f),
                    textColor = PinkHighlight
                ) { onDismiss() }
            }
        }

        // ── 分组 Tab ──────────────────────────────
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(groups.keys.toList()) { _, group ->
                val isActive = group == expandedGroup
                val changedCount = groups[group]?.count {
                    (params[it.key] ?: it.default) != it.default
                } ?: 0

                val bgColor by animateColorAsState(
                    targetValue = if (isActive) PinkHighlight else Color.White.copy(alpha = 0.1f),
                    animationSpec = tween(200), label = "groupBg"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor)
                        .clickable { expandedGroup = group }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = group,
                            color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                        if (changedCount > 0 && !isActive) {
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(PinkHighlight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$changedCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        // ── 参数滑块列表 ──────────────────────────
        val currentParams = groups[expandedGroup] ?: emptyList()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            currentParams.forEach { param ->
                val value = params[param.key] ?: param.default
                Param29DSliderRow(
                    param    = param,
                    value    = value,
                    onChanged = { newValue ->
                        onParameterChange(param.key, newValue)
                        // 实时 Logcat 输出（符合防欺诈协议）
                        Log.d("Parameter29DPanel", "参数变化: ${param.label} (${param.key}) = $newValue ${param.unit}")
                    }
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────
// 单个参数滑块行
// ─────────────────────────────────────────────
@Composable
fun Param29DSliderRow(
    param: Param29DDescriptor,
    value: Float,
    onChanged: (Float) -> Unit
) {
    val isChanged = value != param.default

    // 格式化显示值
    val displayValue = when {
        param.key == "iso"       -> "${value.toInt()}"
        param.key == "colorTemp" -> "${value.toInt()}K"
        param.key == "shutter"   -> {
            val denom = (1f / value.coerceAtLeast(0.001f)).toInt()
            if (denom > 1) "1/${denom}s" else "${String.format("%.1f", value)}s"
        }
        param.unit.isNotEmpty()  -> "${String.format("%.1f", value)}${param.unit}"
        else                     -> String.format("%+.2f", value)
    }

    // 归一化到 0..1 用于 Slider
    val normalized = ((value - param.min) / (param.max - param.min)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：修改指示点 + 参数名
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isChanged) PinkHighlight else Color.Transparent)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = param.label,
                    color = if (isChanged) Color.White else Color.White.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    fontWeight = if (isChanged) FontWeight.Medium else FontWeight.Normal
                )
            }
            // 右侧：当前值
            Text(
                text = displayValue,
                color = if (isChanged) PinkHighlight else Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = normalized,
            onValueChange = { norm ->
                val actual = param.min + norm * (param.max - param.min)
                onChanged(actual)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            colors = SliderDefaults.colors(
                thumbColor         = PinkHighlight,
                activeTrackColor   = PinkHighlight,
                inactiveTrackColor = SliderTrack,
                activeTickColor    = Color.Transparent,
                inactiveTickColor  = Color.Transparent
            )
        )
    }
}

// ─────────────────────────────────────────────
// 工具组件
// ─────────────────────────────────────────────
@Composable
private fun ActionChip(
    label: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, color = textColor, fontSize = 12.sp)
    }
}
