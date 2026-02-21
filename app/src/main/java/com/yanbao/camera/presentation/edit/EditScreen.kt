package com.yanbao.camera.presentation.edit

import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 编辑工具类型
 */
enum class EditTool(val displayName: String, val icon: String) {
    RGB_CURVE("RGB曲线", "📈"),
    HSL("HSL调节", "🎨"),
    EXPOSURE("曝光", "☀️"),
    VIBRANCE("鲜明度", "✨"),
    HIGHLIGHT_SHADOW("高光/阴影", "🌓"),
    CONTRAST("对比度", "⚖️"),
    BRIGHTNESS("亮度", "💡"),
    BLACK_POINT("黑点值", "⚫"),
    SATURATION("饱和度", "🌈"),
    TEMPERATURE("色温", "🌡️"),
    TINT("色调", "🎭"),
    SHARPEN("锐化", "🔪"),
    CLARITY("清晰度", "🔍"),
    DENOISE("降噪", "🧹"),
    VIGNETTE("晕影", "🌑"),
    GRAIN("颗粒感", "🌾"),
    LENS_CORRECTION("镜头校正", "📐"),
    DEHAZE("除雾", "🌫️")
}

/**
 * 编辑模块主界面
 */
@Composable
fun EditScreen(
    bitmap: Bitmap? = null,
    onBack: () -> Unit = {
        android.util.Log.d("EditScreen", "Back button clicked")
    },
    onSave: (Bitmap) -> Unit = { editedBitmap ->
        android.util.Log.d("EditScreen", "Save button clicked")
        // TODO: 保存编辑后的图片
    }
) {
    var selectedTool by remember { mutableStateOf<EditTool?>(null) }
    var previewBitmap by remember { mutableStateOf(bitmap) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        // 顶部：工具栏
        TopAppBar(
            title = {
                Text(
                    text = "编辑",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("←", fontSize = 24.sp, color = Color.White)
                }
            },
            actions = {
                TextButton(onClick = {
                    previewBitmap?.let { onSave(it) }
                }) {
                    Text("保存", color = Color(0xFFEC4899))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2A2A2A)
            )
        )
        
        // 中间：预览区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            previewBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Preview",
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                Text(
                    text = "无预览图片",
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
        
        // 底部：工具选择器 + 参数面板
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xFF2A2A2A))
                .padding(16.dp)
        ) {
            // 工具选择器
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(EditTool.values().toList()) { tool ->
                    ToolButton(
                        tool = tool,
                        isSelected = tool == selectedTool,
                        onClick = { selectedTool = tool }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 参数面板
            selectedTool?.let { tool ->
                when (tool) {
                    EditTool.RGB_CURVE -> {
                        RGBCurveEditorScreen(
                            bitmap = previewBitmap,
                            onApply = { points ->
                                // 应用 RGB 曲线
                                previewBitmap = applyRGBCurve(previewBitmap, points)
                            }
                        )
                    }
                    else -> {
                        // 其他工具的参数面板
                        ToolParameterPanel(
                            tool = tool,
                            onValueChanged = { value ->
                                // 应用工具效果
                                previewBitmap = applyToolEffect(previewBitmap, tool, value)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 工具按钮
 */
@Composable
fun ToolButton(
    tool: EditTool,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color(0xFFEC4899).copy(alpha = 0.3f)
    } else {
        Color(0xFF3A3A3A)
    }
    
    val textColor = if (isSelected) {
        Color(0xFFEC4899)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    Column(
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = tool.icon,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tool.displayName,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 工具参数面板
 */
@Composable
fun ToolParameterPanel(
    tool: EditTool,
    onValueChanged: (Float) -> Unit
) {
    var value by remember { mutableFloatStateOf(0f) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = tool.displayName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "-100",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
            
            Slider(
                value = value,
                onValueChange = {
                    value = it
                    onValueChanged(it)
                },
                valueRange = -100f..100f,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFEC4899),
                    activeTrackColor = Color(0xFFEC4899),
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
            
            Text(
                text = "+100",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "当前值: ${value.toInt()}",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * 应用 RGB 曲线
 */
private fun applyRGBCurve(bitmap: Bitmap?, points: List<PointF>): Bitmap? {
    // 这里应该调用 RGBCurveRenderer 进行 OpenGL 渲染
    // 暂时返回原图
    return bitmap
}

/**
 * 应用工具效果
 */
private fun applyToolEffect(bitmap: Bitmap?, tool: EditTool, value: Float): Bitmap? {
    // 这里应该调用对应的 Shader 进行渲染
    // 暂时返回原图
    return bitmap
}
