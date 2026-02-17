package com.yanbao.camera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.yanbao.camera.model.CameraMode
import com.yanbao.camera.model.CameraParameters
import com.yanbao.camera.model.FilterPresets
import com.yanbao.camera.ui.components.CameraControlPanel
import com.yanbao.camera.ui.components.FilterSelector
import com.yanbao.camera.ui.components.KuromiCorners
import com.yanbao.camera.ui.theme.AccentPink
import com.yanbao.camera.ui.theme.GradientEnd
import com.yanbao.camera.ui.theme.GradientMiddle
import com.yanbao.camera.ui.theme.GradientStart
import com.yanbao.camera.ui.theme.TextWhite

/**
 * 相机屏幕 - 完整实现版本
 * 
 * 功能：
 * - 实时相机预览（占位符）
 * - 5种拍照模式
 * - 专业参数调节
 * - 20+实时滤镜
 * - 闪光灯、摄像头切换
 * - 库洛米装饰
 */
@Composable
fun CameraScreenV2(
    onPhotoTaken: (String) -> Unit = {},
    onNavigateToGallery: () -> Unit = {},
    onNavigateToEdit: () -> Unit = {}
) {
    var currentMode by remember { mutableStateOf(CameraMode.AUTO) }
    var isFlashOn by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var cameraParameters by remember { mutableStateOf(CameraParameters()) }
    var selectedFilter by remember { mutableStateOf(FilterPresets.filters.first()) }
    var filterIntensity by remember { mutableStateOf(1.0f) }
    var showControls by remember { mutableStateOf(true) }
    var showFilters by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                )
            )
    ) {
        // 相机预览区域（占位符）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp)
                .background(
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "📷 相机预览\n(CameraX集成中)",
                color = TextWhite,
                modifier = Modifier.padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        // 控制面板
        if (showControls) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                // 滤镜选择器
                if (showFilters) {
                    FilterSelector(
                        filters = FilterPresets.filters,
                        selectedFilter = selectedFilter,
                        intensity = filterIntensity,
                        onFilterSelected = { selectedFilter = it },
                        onIntensityChanged = { filterIntensity = it },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // 相机控制面板
                CameraControlPanel(
                    currentMode = currentMode,
                    isFlashOn = isFlashOn,
                    isFrontCamera = isFrontCamera,
                    parameters = cameraParameters,
                    onModeChanged = { currentMode = it },
                    onFlashToggled = { isFlashOn = !isFlashOn },
                    onCameraToggled = { isFrontCamera = !isFrontCamera },
                    onSettingsClicked = { showFilters = !showFilters }
                )
            }
        }
        
        // 拍照按钮（底部中央）
        FloatingActionButton(
            onClick = {
                onPhotoTaken("photo_${System.currentTimeMillis()}.jpg")
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            containerColor = AccentPink,
            contentColor = TextWhite
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "拍照",
                modifier = Modifier.padding(8.dp)
            )
        }
        
        // 库洛米装饰
        KuromiCorners(
            modifier = Modifier.fillMaxSize(),
            size = 60,
            showCorners = true
        )
    }
}
