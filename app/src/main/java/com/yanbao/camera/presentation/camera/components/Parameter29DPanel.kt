package com.yanbao.camera.presentation.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yanbao.camera.data.model.Camera29DState

/**
 * 29D 参数调节面板
 * 
 * 使用 LazyVerticalGrid 显示 29 个参数滑块
 * 每个滑块都通过 StateFlow 实时更新硬件参数
 */
@Composable
fun Parameter29DPanel(
    state: Camera29DState,
    onParameterChange: (String, Any) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .blur(16.dp) // 毛玻璃效果
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "29D 专业调优",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 基础曝光参数
                item { ParameterSlider("亮度", state.brightness) { onParameterChange("brightness", it) } }
                item { ParameterSlider("对比度", state.contrast) { onParameterChange("contrast", it) } }
                item { ParameterSlider("饱和度", state.saturation) { onParameterChange("saturation", it) } }
                item { ParameterSlider("锐度", state.sharpness) { onParameterChange("sharpness", it) } }
                item { ParameterSlider("曝光", state.exposure) { onParameterChange("exposure", it) } }
                
                // 色彩参数
                item { ParameterSlider("色温", state.colorTemp / 10000f) { onParameterChange("colorTemp", it * 10000f) } }
                item { ParameterSlider("色调", state.tint) { onParameterChange("tint", it) } }
                item { ParameterSlider("色相", state.hue) { onParameterChange("hue", it) } }
                item { ParameterSlider("自然饱和度", state.vibrance) { onParameterChange("vibrance", it) } }
                
                // 高级色彩通道
                item { ParameterSlider("红色", state.red) { onParameterChange("red", it) } }
                item { ParameterSlider("绿色", state.green) { onParameterChange("green", it) } }
                item { ParameterSlider("蓝色", state.blue) { onParameterChange("blue", it) } }
                item { ParameterSlider("青色", state.cyan) { onParameterChange("cyan", it) } }
                item { ParameterSlider("品红", state.magenta) { onParameterChange("magenta", it) } }
                item { ParameterSlider("黄色", state.yellow) { onParameterChange("yellow", it) } }
                item { ParameterSlider("橙色", state.orange) { onParameterChange("orange", it) } }
                
                // 明暗细节
                item { ParameterSlider("高光", state.highlights) { onParameterChange("highlights", it) } }
                item { ParameterSlider("阴影", state.shadows) { onParameterChange("shadows", it) } }
                item { ParameterSlider("白色", state.whites) { onParameterChange("whites", it) } }
                item { ParameterSlider("黑色", state.blacks) { onParameterChange("blacks", it) } }
                
                // 细节与清晰度
                item { ParameterSlider("清晰度", state.clarity) { onParameterChange("clarity", it) } }
                item { ParameterSlider("去雾", state.dehaze) { onParameterChange("dehaze", it) } }
                item { ParameterSlider("降噪", state.noiseReduction) { onParameterChange("noiseReduction", it) } }
                item { ParameterSlider("颗粒", state.grain) { onParameterChange("grain", it) } }
                item { ParameterSlider("暗角", state.vignette) { onParameterChange("vignette", it) } }
                
                // 美颜参数
                item { ParameterSlider("磨皮", state.beautySmooth) { onParameterChange("beautySmooth", it) } }
                item { ParameterSlider("美白", state.beautyWhiten) { onParameterChange("beautyWhiten", it) } }
                item { ParameterSlider("大眼", state.beautyEyeEnlarge) { onParameterChange("beautyEyeEnlarge", it) } }
                item { ParameterSlider("瘦脸", state.beautyFaceSlim) { onParameterChange("beautyFaceSlim", it) } }
            }
        }
    }
}

/**
 * 单个参数滑块
 */
@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
