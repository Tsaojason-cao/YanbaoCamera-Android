@file:OptIn(ExperimentalMaterial3Api::class)

package com.yanbao.camera.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log

/**
 * 2.9D 模式控制面板（按设计图 04_camera_29d.png 重写）
 */
@OptIn(ExperimentalMaterial3Api::class)
/**
 * 
 * 关键元素：
 * - 标题："✨ 2.9D模式" + "即时" 按钮
 * - 场景选择：人像、风景、艺术、复古（横向滚动）
 * - 4 个参数滑块：
 *   1. 🎨 颜感：0-100
 *   2. 🌓 对比度：0-100
 *   3. 💧 饱和度：0-100
 *   4. 🌡️ 色温：2000K-8000K
 * - 底部按钮：重置（灰色）、存储（粉紫渐变）
 * - 提示："+3个更多参数"
 */
@Composable
fun TwoDotNineDControls(
    modifier: Modifier = Modifier,
    onParametersChanged: (TwoDotNineDParameters) -> Unit = {}
) {
    var selectedScene by remember { mutableStateOf(TwoDotNineDScene.LANDSCAPE) }
    var colorSense by remember { mutableStateOf(20f) }
    var contrast by remember { mutableStateOf(35f) }
    var saturation by remember { mutableStateOf(50f) }
    var colorTemperature by remember { mutableStateOf(4500f) }
    
    // 实时更新参数
    LaunchedEffect(colorSense, contrast, saturation, colorTemperature) {
        val params = TwoDotNineDParameters(
            scene = selectedScene,
            colorSense = colorSense,
            contrast = contrast,
            saturation = saturation,
            colorTemperature = colorTemperature
        )
        onParametersChanged(params)
        Log.d("TwoDotNineD", "参数更新: $params")
    }
    
    // 毛玻璃背景面板
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .blur(10.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33000000),
                            Color(0x66000000)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✨",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "2.9D模式",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👁️",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "即时",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 场景选择（横向滚动）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TwoDotNineDScene.values().forEach { scene ->
                    SceneCard(
                        scene = scene,
                        isSelected = selectedScene == scene,
                        onClick = { 
                            selectedScene = scene
                            Log.d("TwoDotNineD", "场景切换: ${scene.displayName}")
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 场景指示器（小圆点）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .padding(horizontal = 2.dp)
                            .background(
                                color = if (index == TwoDotNineDScene.values().indexOf(selectedScene))
                                    Color(0xFFEC4899)
                                else
                                    Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 参数滑块 1：颜感
            ParameterSlider(
                icon = "🎨",
                label = "颜感",
                value = colorSense,
                onValueChange = { colorSense = it },
                valueRange = 0f..100f,
                displayValue = "${colorSense.toInt()}/100"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 参数滑块 2：对比度
            ParameterSlider(
                icon = "🌓",
                label = "对比度",
                value = contrast,
                onValueChange = { contrast = it },
                valueRange = 0f..100f,
                displayValue = "${contrast.toInt()}/100"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 参数滑块 3：饱和度
            ParameterSlider(
                icon = "💧",
                label = "饱和度",
                value = saturation,
                onValueChange = { saturation = it },
                valueRange = 0f..100f,
                displayValue = "${saturation.toInt()}/100"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 参数滑块 4：色温
            ParameterSlider(
                icon = "🌡️",
                label = "色温",
                value = colorTemperature,
                onValueChange = { colorTemperature = it },
                valueRange = 2000f..8000f,
                displayValue = "${colorTemperature.toInt()}K",
                gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // "+3个更多参数" 提示
            Text(
                text = "+3个更多参数",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 底部按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 重置按钮（灰色）
                Button(
                    onClick = {
                        colorSense = 20f
                        contrast = 35f
                        saturation = 50f
                        colorTemperature = 4500f
                        Log.d("TwoDotNineD", "参数已重置")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6B7280)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "重置",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // 存储按钮（粉紫渐变）
                Button(
                    onClick = {
                        Log.d("TwoDotNineD", "参数已存储")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFEC4899),
                                        Color(0xFFA78BFA)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "存储",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * 场景选择卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneCard(
    scene: TwoDotNineDScene,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) 
            Color(0xFFEC4899) 
        else 
            Color.White.copy(alpha = 0.2f)
    ) {
        Text(
            text = scene.displayName,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

/**
 * 参数滑块组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterSlider(
    icon: String,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    gradientColors: List<Color> = listOf(Color(0xFFEC4899), Color(0xFFA78BFA))
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.width(40.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 标签
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.width(80.dp)
        )
        
        // 滑块
        Slider(
            value = value,
            onValueChange = { 
                onValueChange(it)
                Log.d("TwoDotNineD", "$label: $displayValue")
            },
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            track = { sliderPositions ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            brush = Brush.horizontalGradient(gradientColors),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 数值显示
        Text(
            text = displayValue,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.width(60.dp)
        )
    }
}

/**
 * 2.9D 场景枚举
 */
enum class TwoDotNineDScene(val displayName: String) {
    PORTRAIT("人像"),
    LANDSCAPE("风景"),
    ART("艺术"),
    VINTAGE("复古")
}

/**
 * 2.9D 参数数据类
 */
data class TwoDotNineDParameters(
    val scene: TwoDotNineDScene,
    val colorSense: Float,      // 颜感 0-100
    val contrast: Float,         // 对比度 0-100
    val saturation: Float,       // 饱和度 0-100
    val colorTemperature: Float  // 色温 2000-8000K
)
