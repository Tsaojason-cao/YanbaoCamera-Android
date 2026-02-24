package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.KUROMI_PINK
import com.yanbao.camera.ui.theme.OBSIDIAN_BLACK

@Composable
fun CameraScreen(
    onNavigateToGallery: () -> Unit = {}
) {
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val layer0Height = screenHeight * 0.75f
    val layer1Height = screenHeight * 0.25f

    // 临时选中的模式（第一阶段固定为BASIC）
    val selectedMode = CameraMode.BASIC

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Layer 0: 取景器（75%）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(layer0Height)
                .background(Color.DarkGray)
        ) {
            // 品牌标识
            Text(
                text = "yanbao AI",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
            // 左上角模式名
            Text(
                text = selectedMode.displayName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Layer 1: 底部控制面板（25%）
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(layer1Height)
                .align(Alignment.BottomCenter),
            color = OBSIDIAN_BLACK.copy(alpha = 0.85f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // 顶部快捷工具栏
                QuickToolbar()

                Spacer(modifier = Modifier.height(8.dp))

                // 中央主控区
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 左侧相册缩略图
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable { onNavigateToGallery() }
                    )

                    // 中央快门按钮
                    ShutterButton(
                        onClick = { /* 暂空 */ },
                        modifier = Modifier.size(72.dp)
                    )

                    // 右侧记忆按钮（占位）
                    IconButton(onClick = {}) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_memory),
                            contentDescription = "记忆",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 模式切换栏
                ModeSelectorRow(
                    modes = CameraMode.values().toList(),
                    selectedMode = selectedMode,
                    onModeSelected = {}
                )
            }
        }
    }
}
