package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.R

@Composable
fun QuickToolbar(
    onFlashClick: () -> Unit = {},
    onAspectClick: () -> Unit = {},
    onTimerClick: () -> Unit = {},
    onFlipClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = onFlashClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_flash_auto),
                contentDescription = null,
                tint = Color.White
            )
        }
        IconButton(onClick = onAspectClick) {
            Text(
                text = "4:3",
                color = Color.White,
                fontSize = 14.sp
            )
        }
        IconButton(onClick = onTimerClick) {
            Text(
                text = "OFF",
                color = Color.White,
                fontSize = 14.sp
            )
        }
        IconButton(onClick = onFlipClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_flip),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
