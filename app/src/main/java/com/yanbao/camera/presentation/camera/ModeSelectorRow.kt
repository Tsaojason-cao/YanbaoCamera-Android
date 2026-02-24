package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.theme.KUROMI_PINK

@Composable
fun ModeSelectorRow(
    modes: List<CameraMode>,
    selectedMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(modes) { mode ->
            Column(
                modifier = Modifier
                    .width(80.dp)
                    .clickable { onModeSelected(mode) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = mode.iconRes),
                    contentDescription = mode.displayName,
                    tint = if (mode == selectedMode) KUROMI_PINK else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = mode.displayName,
                    color = if (mode == selectedMode) KUROMI_PINK else Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}
