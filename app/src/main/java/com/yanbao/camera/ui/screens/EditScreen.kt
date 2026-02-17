package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yanbao.camera.viewmodel.EditViewModel

@Composable
fun EditScreen(
    viewModel: EditViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val editSettings = viewModel.editSettings.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cancel"
                )
            }

            Text(
                text = "编辑",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { viewModel.saveEdit() }) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Save"
                )
            }
        }

        // Preview Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("图片预览")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Edit Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Brightness
            Text("亮度", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = editSettings.value.brightness,
                onValueChange = { viewModel.setBrightness(it) },
                valueRange = -100f..100f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contrast
            Text("对比度", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = editSettings.value.contrast,
                onValueChange = { viewModel.setContrast(it) },
                valueRange = -100f..100f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Saturation
            Text("饱和度", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = editSettings.value.saturation,
                onValueChange = { viewModel.setSaturation(it) },
                valueRange = -100f..100f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hue
            Text("色调", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = editSettings.value.hue,
                onValueChange = { viewModel.setHue(it) },
                valueRange = -180f..180f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Intensity
            Text("滤镜强度", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = editSettings.value.filterIntensity,
                onValueChange = { viewModel.setFilterIntensity(it) },
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
