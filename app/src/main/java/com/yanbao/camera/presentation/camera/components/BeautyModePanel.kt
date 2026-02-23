package com.yanbao.camera.presentation.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 美颜模式面板（雁宝记忆模式）
 * 
 * 核心功能：
 * - 集成 BeautySliderWithValue 组件
 * - 显示磨皮、美白、祛斑三个滑块
 * - 实时回调参数变化
 */
@Composable
fun BeautyModePanel(
    beautyParams: BeautyParams,
    onBeautyParamsChange: (BeautyParams) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题
        Text(
            text = "BEAUTY 美颜调整",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // 三个美颜滑块
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 磨皮
            BeautySliderWithValue(
                label = "磨皮",
                emoji = "[*]",
                initialValue = beautyParams.smoothness,
                onValueChange = { value ->
                    onBeautyParamsChange(beautyParams.copy(smoothness = value))
                }
            )

            // 美白
            BeautySliderWithValue(
                label = "美白",
                emoji = "[*]",
                initialValue = beautyParams.whitening,
                onValueChange = { value ->
                    onBeautyParamsChange(beautyParams.copy(whitening = value))
                }
            )

            // 祛斑
            BeautySliderWithValue(
                label = "祛斑",
                emoji = "[*]",
                initialValue = beautyParams.blemishRemoval,
                onValueChange = { value ->
                    onBeautyParamsChange(beautyParams.copy(blemishRemoval = value))
                }
            )
        }

        // 提示文字
        Text(
            text = "参数将保存到照片 Exif 元数据",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
