package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.util.Log
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.KUROMI_PINK

/**
 * 美颜弹窗 — 严格对应 14_camera_08_beauty_popup.png
 *
 * 布局：
 *  标题："YanBao AI Beauty"（白色）
 *  内容：6个圆形预设按钮（横排）
 *    Natural（绿色，选中）/ Light / Medium / Strong / Custom / Reset
 *  底部：[取消] [应用] 按钮
 *
 * 背景：毛玻璃卡片 + 粉色霓虹边框
 */
data class BeautyPreset(
    val id: String,
    val label: String,
    val iconRes: Int,
    val iconColor: Color
)

@Composable
fun BeautyPopup(
    onDismiss: () -> Unit = {},
    onApply: (String) -> Unit = {}
) {
    val presets = listOf(
        BeautyPreset("natural", "Natural", R.drawable.ic_mode_beauty, Color(0xFF4CAF50)),
        BeautyPreset("light", "Light", R.drawable.ic_mode_basic, Color(0xFFFFC107)),
        BeautyPreset("medium", "Medium", R.drawable.ic_mode_master, Color(0xFFFF9800)),
        BeautyPreset("strong", "Strong", R.drawable.ic_mode_29d, KUROMI_PINK),
        BeautyPreset("custom", "Custom", R.drawable.ic_mode_ar, Color(0xFF9E9E9E)),
        BeautyPreset("reset", "Reset", R.drawable.ic_mode_memory, Color(0xFF9E9E9E))
    )

    var selectedPreset by remember { mutableStateOf("natural") }
    var skinSmooth by remember { mutableStateOf(0.6f) }
    var skinWhiten by remember { mutableStateOf(0.4f) }
    var faceThin by remember { mutableStateOf(0.3f) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1A1A2E).copy(alpha = 0.96f))
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(KUROMI_PINK, Color(0xFF9D4EDD))
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 标题
                Text(
                    text = "YanBao AI Beauty",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 6个预设按钮（横排 + 左右箭头）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 左箭头
                    Icon(
                        painter = painterResource(R.drawable.ic_back_kuromi),
                        contentDescription = "上一页",
                        tint = KUROMI_PINK,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { }
                    )

                    // 6个预设
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            val isSelected = selectedPreset == preset.id
                            BeautyPresetButton(
                                preset = preset,
                                isSelected = isSelected,
                                onClick = {
                                    selectedPreset = preset.id
                                    Log.d("AUDIT_BEAUTY", "preset_selected=${preset.id}")
                                }
                            )
                        }
                    }

                    // 右箭头
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_right_kuromi),
                        contentDescription = "下一页",
                        tint = KUROMI_PINK,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 磨皮/美白/瘦脸 NeonSlider（按指令四）
                NeonSlider(
                    value = skinSmooth,
                    onValueChange = {
                        skinSmooth = it
                        Log.d("AUDIT_BEAUTY", "smooth=${String.format("%.2f", it)}")
                    },
                    onLongPressReset = { skinSmooth = 0.6f },
                    label = "磨皮",
                    valueText = "${(skinSmooth * 100).toInt()}%"
                )
                NeonSlider(
                    value = skinWhiten,
                    onValueChange = {
                        skinWhiten = it
                        Log.d("AUDIT_BEAUTY", "whiten=${String.format("%.2f", it)}")
                    },
                    onLongPressReset = { skinWhiten = 0.4f },
                    label = "美白",
                    valueText = "${(skinWhiten * 100).toInt()}%"
                )
                NeonSlider(
                    value = faceThin,
                    onValueChange = {
                        faceThin = it
                        Log.d("AUDIT_BEAUTY", "slim=${String.format("%.2f", it)}")
                    },
                    onLongPressReset = { faceThin = 0.3f },
                    label = "瘦脸",
                    valueText = "${(faceThin * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "取消", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // 应用
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        KUROMI_PINK.copy(alpha = 0.4f),
                                        Color(0xFF9D4EDD).copy(alpha = 0.4f)
                                    )
                                )
                            )
                            .border(1.5.dp, KUROMI_PINK, RoundedCornerShape(24.dp))
                            .clickable {
                                Log.d("AUDIT_BEAUTY", "applied preset=$selectedPreset smooth=${(skinSmooth*100).toInt()} whiten=${(skinWhiten*100).toInt()} slim=${(faceThin*100).toInt()}")
                                onApply(selectedPreset)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "应用", color = KUROMI_PINK, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * 单个美颜预设圆形按钮
 */
@Composable
fun BeautyPresetButton(
    preset: BeautyPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) preset.iconColor.copy(alpha = 0.25f)
                    else Color.White.copy(alpha = 0.08f)
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) preset.iconColor else Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(preset.iconRes),
                contentDescription = preset.label,
                tint = if (isSelected) preset.iconColor else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = preset.label,
            color = if (isSelected) preset.iconColor else Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}
