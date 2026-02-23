package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yanbao.camera.ui.theme.KUROMI_PINK
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MasterWheel(viewModel: CameraViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "大师滤镜转盘",
            color = KUROMI_PINK,
            fontSize = 14.sp
        )
    }
}
