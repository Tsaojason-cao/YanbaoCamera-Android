package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanbao.camera.ui.theme.KuromiPink

private val KUROMI_PINK = KuromiPink

@Composable
fun Param29DPanel(viewModel: CameraViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("曝光", "色彩", "纹理", "美颜")
    val params by viewModel.params29D.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(12.dp)
    ) {
        // Tab导航
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, title ->
                Text(
                    text = title,
                    color = if (selectedTab == index) KUROMI_PINK else Color.White,
                    fontSize = 16.sp,
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // 滑块列表
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            when (selectedTab) {
                0 -> { // 曝光
                    ParamSlider(
                        name = "ISO",
                        value = params.iso / 6400f,
                        onValueChange = { viewModel.update29DParam { iso = (it * 6400).toInt() } },
                        valueRange = 0f..1f,
                        formatValue = { "${(it * 6400).toInt()}" }
                    )
                    ParamSlider(
                        name = "快门",
                        value = params.shutterSpeed.toFloat() / 8000f,
                        onValueChange = { viewModel.update29DParam { shutterSpeed = (it * 8000).toInt().toString() } },
                        valueRange = 0f..1f,
                        formatValue = { "1/${(it * 8000).toInt()}" }
                    )
                    ParamSlider(
                        name = "EV",
                        value = (params.ev + 3f) / 6f,
                        onValueChange = { viewModel.update29DParam { ev = it * 6 - 3 } },
                        valueRange = 0f..1f,
                        formatValue = { "${String.format("%.1f", it * 6 - 3)} EV" }
                    )
                }
                1 -> { // 色彩
                    ParamSlider(
                        name = "色温",
                        value = (params.colorTemp - 2000f) / 8000f,
                        onValueChange = { viewModel.update29DParam { colorTemp = (2000 + it * 8000).toInt() } },
                        valueRange = 0f..1f,
                        formatValue = { "${(2000 + it * 8000).toInt()}K" }
                    )
                    ParamSlider(
                        name = "饱和度",
                        value = params.saturation / 200f,
                        onValueChange = { viewModel.update29DParam { saturation = (it * 200).toInt() } },
                        valueRange = 0f..1f,
                        formatValue = { "${(it * 200).toInt()}%" }
                    )
                }
                2 -> { // 纹理
                    ParamSlider(
                        name = "锐度",
                        value = params.sharpness / 100f,
                        onValueChange = { viewModel.update29DParam { sharpness = (it * 100).toInt() } },
                        valueRange = 0f..1f,
                        formatValue = { "${(it * 100).toInt()}%" }
                    )
                    ParamSlider(
                        name = "降噪",
                        value = params.denoise / 100f,
                        onValueChange = { viewModel.update29DParam { denoise = (it * 100).toInt() } },
                        valueRange = 0f..1f,
                        formatValue = { "${(it * 100).toInt()}%" }
                    )
                }
                3 -> { // 美颜
                    ParamSlider(
                        name = "全局",
                        value = params.beautyGlobal / 100f,
                        onValueChange = { viewModel.update29DParam { beautyGlobal = (it * 100).toInt() } },
                        valueRange = 0f..1f,
                        formatValue = { "${(it * 100).toInt()}%" }
                    )
                    ParamSlider(
                        name = "磨皮",
                        value = params.skinSmooth / 100f,
                        onValueChange = { viewModel.update29DParam { skinSmooth = (it * 100).toInt() } },
                        valueRange = 0f..1f,
                        formatValue = { "${(it * 100).toInt()}%" }
                    )
                }
            }
        }
    }
}

@Composable
fun ParamSlider(
    name: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    formatValue: (Float) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, color = Color.White, fontSize = 14.sp)
            Text(text = formatValue(value), color = KUROMI_PINK, fontSize = 14.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = KUROMI_PINK,
                activeTrackColor = KUROMI_PINK,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}
