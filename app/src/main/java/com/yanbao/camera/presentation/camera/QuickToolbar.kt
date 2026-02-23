package com.yanbao.camera.presentation.camera

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanbao.camera.R

@Composable
fun QuickToolbar(viewModel: CameraViewModel) {
    val flashMode by viewModel.flashMode.collectAsStateWithLifecycle()
    val aspectRatio by viewModel.aspectRatio.collectAsStateWithLifecycle()
    val timer by viewModel.timer.collectAsStateWithLifecycle()
    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = { viewModel.setFlashMode((flashMode + 1) % 3) }) {
            Icon(
                painter = painterResource(
                    when (flashMode) {
                        0 -> R.drawable.ic_flash_auto
                        1 -> R.drawable.ic_flash_on
                        else -> R.drawable.ic_flash_off
                    }
                ),
                contentDescription = "闪光灯",
                tint = if (flashMode == 0) Color.Gray else Color.White
            )
        }
        IconButton(onClick = { viewModel.setAspectRatio((aspectRatio + 1) % 3) }) {
            Text(
                text = when (aspectRatio) {
                    0 -> "4:3"
                    1 -> "16:9"
                    else -> "1:1"
                },
                color = Color.White,
                fontSize = 14.sp
            )
        }
        IconButton(onClick = { viewModel.setTimer(if (timer == 0) 3 else if (timer == 3) 10 else 0) }) {
            Text(
                text = when (timer) {
                    3 -> "3s"
                    10 -> "10s"
                    else -> "OFF"
                },
                color = Color.White,
                fontSize = 14.sp
            )
        }
        IconButton(onClick = { viewModel.flipLens() }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_flip),
                contentDescription = "翻转",
                tint = Color.White
            )
        }
    }
}
