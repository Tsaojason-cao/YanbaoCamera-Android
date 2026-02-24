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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.KUROMI_PINK

/**
 * 相机设定浮动弹窗 — 严格对应 12_camera_06_settings_popup.png
 *
 * 布局：
 *  右侧浮动齿轮按钮（触发点）
 *  弹出卡片（毛玻璃 + 粉色霓虹边框）：
 *    1. 大师模式（相机图标）
 *    2. 一键美颜（笑脸图标）
 *    3. 29D参数（立方体图标）
 *    4. 分享（分享图标）
 *
 * 背景：模糊取景器 + 弹窗叠加
 */
@Composable
fun SettingsPopupButton(
    onMasterModeClick: () -> Unit = {},
    onBeautyClick: () -> Unit = {},
    on29DClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // 齿轮触发按钮
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .border(1.dp, KUROMI_PINK.copy(alpha = 0.6f), CircleShape)
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings_kuromi),
                contentDescription = "设定",
                tint = KUROMI_PINK,
                modifier = Modifier.size(22.dp)
            )
        }

        // 弹出菜单
        if (expanded) {
            Box(
                modifier = Modifier
                    .offset(x = (-160).dp, y = 8.dp)
                    .width(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A0828).copy(alpha = 0.95f))
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                KUROMI_PINK,
                                Color(0xFF9D4EDD)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SettingsPopupItem(
                        iconRes = R.drawable.ic_mode_master_kuromi,
                        label = "大师模式",
                        isFirst = true,
                        onClick = {
                            expanded = false
                            onMasterModeClick()
                        }
                    )
                    SettingsPopupItem(
                        iconRes = R.drawable.ic_mode_beauty_kuromi,
                        label = "一键美颜",
                        onClick = {
                            expanded = false
                            onBeautyClick()
                        }
                    )
                    SettingsPopupItem(
                        iconRes = R.drawable.ic_mode_29d_kuromi,
                        label = "29D参数",
                        onClick = {
                            expanded = false
                            on29DClick()
                        }
                    )
                    SettingsPopupItem(
                        iconRes = R.drawable.ic_share_kuromi,
                        label = "分享",
                        onClick = {
                            expanded = false
                            onShareClick()
                        }
                    )
                }
            }
        }
    }
}

/**
 * 设定弹窗单项 — 图标 + 文字，第一项高亮（粉色背景）
 */
@Composable
fun SettingsPopupItem(
    iconRes: Int,
    label: String,
    isFirst: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isFirst) KUROMI_PINK.copy(alpha = 0.15f)
                else Color.White.copy(alpha = 0.05f)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = if (isFirst) KUROMI_PINK else Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (isFirst) KUROMI_PINK else Color.White,
            fontSize = 14.sp,
            fontWeight = if (isFirst) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
