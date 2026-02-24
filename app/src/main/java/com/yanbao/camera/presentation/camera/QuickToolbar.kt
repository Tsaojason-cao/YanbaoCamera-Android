package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.KUROMI_PINK

/**
 * 顶部快捷工具栏 — 严格按照 ui_main_camera.png 还原
 *
 * 黑色背景，水平均分四个按钮：
 *  ⚡ AUTO（闪光灯）| 4:3（画面比例）| 定时器图标 | 相机翻转图标
 */
@Composable
fun QuickToolbar(viewModel: CameraViewModel) {
    val flashMode by viewModel.flashMode.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.aspectRatio.collectAsStateWithLifecycle()
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 闪光灯：图标 + "AUTO" 文字
        IconButton(onClick = { viewModel.setFlashMode((flashMode + 1) % 3) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    painter = painterResource(
                        when (flashMode) {
                            0 -> R.drawable.ic_flash_auto_kuromi
                            1 -> R.drawable.ic_flash_on_kuromi
                            else -> R.drawable.ic_flash_off_kuromi
                        }
                    ),
                    contentDescription = "闪光灯",
                    tint = if (flashMode == 1) KUROMI_PINK else Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = when (flashMode) { 0 -> "AUTO"; 1 -> "ON"; else -> "OFF" },
                    color = if (flashMode == 1) KUROMI_PINK else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 画面比例：4:3 / 16:9 / 1:1（带方框样式）
        IconButton(onClick = { viewModel.setAspectRatio((aspectRatio + 1) % 3) }) {
            Text(
                text = when (aspectRatio) { 0 -> "4:3"; 1 -> "16:9"; else -> "1:1" },
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 定时器图标
        IconButton(onClick = {
            viewModel.setTimer(when (timer) { 0 -> 3; 3 -> 10; else -> 0 })
        }) {
            Icon(
                painter = painterResource(R.drawable.ic_timer_kuromi),
                contentDescription = "定时器",
                tint = if (timer > 0) KUROMI_PINK else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // 前后摄像头翻转图标
        IconButton(onClick = { viewModel.flipLens() }) {
            Icon(
                painter = painterResource(R.drawable.ic_flip_camera_kuromi),
                contentDescription = "翻转摄像头",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
