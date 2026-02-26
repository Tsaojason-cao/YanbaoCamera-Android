package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.yanbao.camera.ui.theme.OBSIDIAN_BLACK

/**
 * 相机设定页面 — 严格对应 06_camera_02_settings.png
 *
 * 布局：
 *  顶部：← 设定  🏠
 *  内容：4个毛玻璃大按钮（竖排）
 *    1. 大师模式（图层堆叠图标）
 *    2. 一键美颜（人脸+星星图标）
 *    3. 29D参数（滑块图标）
 *    4. 分享（分享图标）
 *
 * 背景：深紫色科技感（渐变）
 */
@Composable
fun CameraSettingsScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onMasterModeClick: () -> Unit = {},
    onBeautyClick: () -> Unit = {},
    on29DClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0A2E),
                        Color(0xFF0D0618),
                        Color(0xFF1A0A2E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // 顶部导航栏
            SettingsTopBar(
                onBackClick = onBackClick,
                onHomeClick = onHomeClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4个功能按钮
            SettingsMenuButton(
                iconRes = R.drawable.ic_yanbao_master,
                label = "大师模式",
                onClick = onMasterModeClick
            )
            Spacer(modifier = Modifier.height(16.dp))

            SettingsMenuButton(
                iconRes = R.drawable.ic_yanbao_beauty,
                label = "一键美颜",
                onClick = onBeautyClick
            )
            Spacer(modifier = Modifier.height(16.dp))

            SettingsMenuButton(
                iconRes = R.drawable.ic_yanbao_29d,
                label = "29D参数",
                onClick = on29DClick
            )
            Spacer(modifier = Modifier.height(16.dp))

            SettingsMenuButton(
                iconRes = R.drawable.ic_yanbao_share,
                label = "分享",
                onClick = onShareClick
            )
        }
    }
}

/**
 * 设定页顶部导航栏
 */
@Composable
fun SettingsTopBar(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 返回按钮（圆形毛玻璃）
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, KUROMI_PINK.copy(alpha = 0.5f), CircleShape)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_yanbao_back),
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // 标题
        Text(
            text = "设定",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // 主页按钮（圆形毛玻璃）
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, KUROMI_PINK.copy(alpha = 0.5f), CircleShape)
                .clickable { onHomeClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_yanbao_home),
                contentDescription = "主页",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 设定页功能按钮 — 毛玻璃大按钮，粉色霓虹边框
 * 对应 06_camera_02_settings.png 中的每一行按钮
 */
@Composable
fun SettingsMenuButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        KUROMI_PINK.copy(alpha = 0.8f),
                        Color(0xFF9D4EDD).copy(alpha = 0.5f),
                        KUROMI_PINK.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = KUROMI_PINK,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
