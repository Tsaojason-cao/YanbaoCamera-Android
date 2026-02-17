package com.yanbao.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ui.theme.AccentPink
import com.yanbao.camera.ui.theme.TextDark
import com.yanbao.camera.ui.theme.TextGray

/**
 * AI增强功能UI组件
 */

/**
 * AI增强按钮组
 */
@Composable
fun AIEnhancementButtons(
    onNightModeClick: () -> Unit,
    onPortraitBeautyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 夜景增强按钮
        AIButton(
            icon = Icons.Default.NightsStay,
            label = "夜景增强",
            onClick = onNightModeClick,
            modifier = Modifier.weight(1f)
        )
        
        // 人像美化按钮
        AIButton(
            icon = Icons.Default.Palette,
            label = "人像美化",
            onClick = onPortraitBeautyClick,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * AI功能按钮
 */
@Composable
fun AIButton(
    icon: androidx.compose.material.icons.Icons,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AccentPink.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AccentPink,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 夜景增强面板
 */
@Composable
fun NightModeEnhancementPanel(
    strength: Float = 1.0f,
    onStrengthChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "夜景增强",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 强度滑块
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "强度",
                fontSize = 12.sp,
                color = TextDark
            )
            Text(
                text = "${String.format("%.1f", strength)}x",
                fontSize = 12.sp,
                color = TextGray
            )
        }
        
        Slider(
            value = strength,
            onValueChange = onStrengthChanged,
            valueRange = 0f..2f,
            modifier = Modifier.fillMaxWidth(),
            steps = 19
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 说明文字
        Text(
            text = "提升暗光环境下的亮度和细节，自动降低噪声",
            fontSize = 11.sp,
            color = TextGray
        )
    }
}

/**
 * 人像美化面板
 */
@Composable
fun PortraitBeautyPanel(
    skinSmoothing: Float = 0.7f,
    whitening: Float = 0.5f,
    eyeEnlargement: Float = 0.3f,
    faceThinning: Float = 0.2f,
    onSkinSmoothingChanged: (Float) -> Unit,
    onWhiteningChanged: (Float) -> Unit,
    onEyeEnlargementChanged: (Float) -> Unit,
    onFaceThinningChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "人像美化",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 磨皮
        ParameterSlider(
            label = "磨皮",
            value = skinSmoothing,
            onValueChange = onSkinSmoothingChanged,
            description = "平滑皮肤纹理"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 美白
        ParameterSlider(
            label = "美白",
            value = whitening,
            onValueChange = onWhiteningChanged,
            description = "提亮肤色"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 大眼
        ParameterSlider(
            label = "大眼",
            value = eyeEnlargement,
            onValueChange = onEyeEnlargementChanged,
            description = "放大眼睛"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 瘦脸
        ParameterSlider(
            label = "瘦脸",
            value = faceThinning,
            onValueChange = onFaceThinningChanged,
            description = "修饰脸部线条"
        )
    }
}

/**
 * 参数调节滑块
 */
@Composable
fun ParameterSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    description: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextDark,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(value * 100).toInt()}%",
                fontSize = 12.sp,
                color = TextGray
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth(),
            steps = 9
        )
        
        if (description.isNotEmpty()) {
            Text(
                text = description,
                fontSize = 10.sp,
                color = TextGray
            )
        }
    }
}

/**
 * AI处理中指示器
 */
@Composable
fun AIProcessingIndicator(
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    if (isProcessing) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentPink.copy(alpha = 0.1f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "正在处理...",
                fontSize = 12.sp,
                color = AccentPink,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * AI功能提示卡片
 */
@Composable
fun AIFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.material.icons.Icons,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AccentPink,
            modifier = Modifier.size(24.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = TextGray
            )
        }
    }
}

/**
 * AI对比预览
 */
@Composable
fun AIComparisonPreview(
    beforeLabel: String = "原图",
    afterLabel: String = "处理后",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "效果对比",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 原图
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = beforeLabel,
                    fontSize = 10.sp,
                    color = TextGray
                )
            }
            
            // 处理后
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentPink.copy(alpha = 0.2f))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = afterLabel,
                    fontSize = 10.sp,
                    color = AccentPink,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
