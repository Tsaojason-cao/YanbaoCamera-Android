package com.yanbao.camera.presentation.camera

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CameraDimens.PanelCornerRadius,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(CameraColors.GlassBgDark)
            .graphicsLayer {
                renderEffect = RenderEffect.createBlurEffect(
                    CameraDimens.BlurRadius.toPx(),
                    CameraDimens.BlurRadius.toPx(),
                    Shader.TileMode.CLAMP
                )
            }
            .then(Modifier.drawBehind {
                val strokeWidth = CameraDimens.NeonBorderWidth.toPx()
                val r = cornerRadius.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(CameraColors.KuromiPink, CameraColors.KuromiPurple)
                    ),
                    cornerRadius = CornerRadius(r),
                    style = Stroke(width = strokeWidth)
                )
            }),
        content = content
    )
}

@Composable
fun NeonSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onLongPressReset: (() -> Unit)? = null,
    label: String = "",
    valueText: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    color = CameraColors.LabelColor,
                    fontSize = CameraTextSizes.SliderLabel,
                    modifier = Modifier
                        .width(70.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onLongPressReset?.invoke() }
                            )
                        }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    colors = SliderDefaults.colors(
                        thumbColor = CameraColors.KuromiPink,
                        activeTrackColor = CameraColors.KuromiPink,
                        inactiveTrackColor = CameraColors.SliderTrack
                    )
                )
            }
            if (valueText.isNotEmpty()) {
                Text(
                    text = valueText,
                    color = CameraColors.KuromiPink,
                    fontSize = CameraTextSizes.SliderValue,
                    modifier = Modifier.width(60.dp)
                )
            }
        }
    }
}

@Composable
fun BottomActionButtons(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    cancelText: String = "取消",
    applyText: String = "应用",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 取消按钮 — 边框样式
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CameraColors.DarkPanel)
                .border(
                    width = CameraDimens.NeonBorderWidth,
                    brush = Brush.linearGradient(
                        listOf(CameraColors.KuromiPink, CameraColors.KuromiPurple)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable { onCancel() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cancelText,
                color = CameraColors.White70,
                fontSize = CameraTextSizes.Body,
                fontWeight = FontWeight.Medium
            )
        }
        // 应用按钮 — 粉→紫渐变填充
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(CameraColors.KuromiPink, CameraColors.KuromiPurple)
                    )
                )
                .clickable { onApply() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = applyText,
                color = CameraColors.White,
                fontSize = CameraTextSizes.Body,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
