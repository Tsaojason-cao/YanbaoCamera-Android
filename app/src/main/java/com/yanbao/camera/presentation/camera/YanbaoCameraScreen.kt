package com.yanbao.camera.presentation.camera

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.yanbao.camera.core.config.ThemeConfig

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize

/**
 * yanbao AI 拍照界面
 * 
 * 根據真實设计圖（unnamed.jpg）1:1 还原
 * 
 * 空間分層：
 * - Layer 0: 100% 全屏取景空間
 * - Layer 1: 顶部参数气泡 + 右上角發光頭像
 * - Layer 2: 底部 28% 曜石黑控制舱
 */
@Composable
fun YanbaoCameraScreen(
    onTakePhoto: () -> Unit = {}
) {
    val context = LocalContext.current
    val themeConfig = remember { ThemeConfig.load(context) }
    Box(modifier = Modifier.fillMaxSize()) {
        
        // === Layer 0: 全屏取景空間 (100%) ===
        CameraPreviewLayer(modifier = Modifier.fillMaxSize())
        
        // === Layer 1: 顶部参数气泡 ===
        TopParameterBubbles(modifier = Modifier.align(Alignment.TopStart))
        
        // === Layer 1: 右上角發光頭像 ===
        GlowingAvatarButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 20.dp)
        )
        
        // === Layer 2: 底部 28% 控制舱 ===
        BottomControlPanel(
            onTakePhoto = onTakePhoto,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(themeConfig.ui_logic.control_panel_ratio) // 从JSON读取28%
        )
    }
}

/**
 * Layer 0: 全屏取景空間（Camera2 预览）
 */
@Composable
fun CameraPreviewLayer(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Camera2 Preview",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 24.sp
        )
    }
}

/**
 * Layer 1: 顶部参数气泡
 * 
 * 显示：ISO 100、快門 1/250、焦距 35mm
 */
@Composable
fun TopParameterBubbles(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(top = 60.dp, start = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ISO 气泡
        ParameterBubble(
            label = "ISO 100",
            subtitle = "f2.0 自X"
        )
        
        // 快門气泡
        ParameterBubble(
            label = "1/250",
            subtitle = "2.9D±0.8"
        )
        
        // 焦距气泡
        ParameterBubble(
            label = "35mm",
            subtitle = "2.9D±0.8"
        )
    }
}

/**
 * 参数气泡組件
 */
@Composable
fun ParameterBubble(label: String, subtitle: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33000000)) // 半透明黑色
            .border(1.dp, Color(0xFFFFB6C1).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp
        )
    }
}

/**
 * Layer 1: 右上角發光頭像
 */
@Composable
fun GlowingAvatarButton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Box(
        modifier = modifier
            .size(48.dp)
            .drawBehind {
                // 粉色發光環
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFB6C1).copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension * 0.7f
                )
            }
            .clip(CircleShape)
            .background(Color(0xFF333333))
            .border(2.dp, Color(0xFFFFB6C1), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "👤",
            fontSize = 24.sp
        )
    }
}

/**
 * Layer 2: 底部 28% 控制舱
 */
@Composable
fun BottomControlPanel(
    onTakePhoto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeConfig = remember { ThemeConfig.load(context) }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color(0xCC0A0A0A)) // 曜石黑
            .blur(themeConfig.theme_palette.glass_blur_sigma.dp) // 从JSON读取40px高斯模糊
            .border(
                0.5.dp,
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 弧形刻度尺
        ArcScaleRuler()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 中央快門區域
        CentralShutterArea(onTakePhoto = onTakePhoto)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 底部导航欄
        BottomNavigation()
    }
}

/**
 * 弧形刻度尺（-100 到 +105）
 */
@Composable
fun ArcScaleRuler() {
    var currentValue by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .drawBehind {
                val centerX = size.width / 2
                val centerY = size.height
                val radius = size.width * 0.4f
                
                // 繪製弧形刻度線
                for (i in -100..105 step 5) {
                    val angle = (i / 205f) * 180f - 90f
                    val angleRad = Math.toRadians(angle.toDouble())
                    
                    val startX = centerX + (radius * cos(angleRad)).toFloat()
                    val startY = centerY + (radius * sin(angleRad)).toFloat()
                    val endX = centerX + ((radius - 10f) * cos(angleRad)).toFloat()
                    val endY = centerY + ((radius - 10f) * sin(angleRad)).toFloat()
                    
                    drawLine(
                        color = if (i == currentValue.toInt()) Color(0xFFFFB6C1) else Color.White.copy(alpha = 0.3f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (i % 20 == 0) 2f else 1f
                    )
                }
                
                // 繪製當前值指示器
                val currentAngle = (currentValue / 205f) * 180f - 90f
                val currentAngleRad = Math.toRadians(currentAngle.toDouble())
                val indicatorX = centerX + (radius * cos(currentAngleRad)).toFloat()
                val indicatorY = centerY + (radius * sin(currentAngleRad)).toFloat()
                
                drawCircle(
                    color = Color(0xFFFFB6C1),
                    radius = 6f,
                    center = Offset(indicatorX, indicatorY)
                )
            }
    ) {
        // 左側數值标签
        Text(
            text = "-100",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 40.dp)
        )
        
        // 中央數值标签
        Text(
            text = currentValue.toInt().toString(),
            color = Color(0xFFFFB6C1),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // 右側數值标签
        Text(
            text = "+105",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 40.dp)
        )
    }
}

/**
 * 中央快門區域
 */
@Composable
fun CentralShutterArea(
    onTakePhoto: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 模式切換标签
        ModeSwitchTabs()
        
        // 库洛米快門按鈕
        KuromiShutterButton(onClick = onTakePhoto)
        
        // Git Syncing 状态
        GitSyncingStatus()
    }
}

/**
 * 模式切換标签
 */
@Composable
fun ModeSwitchTabs() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "雁宝修飾",
            color = Color.White,
            fontSize = 12.sp
        )
        Text(
            text = "美顏",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
        Text(
            text = "29D",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
        Text(
            text = "更多",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}

/**
 * 库洛米快門按鈕
 */
@Composable
fun KuromiShutterButton(onClick: () -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition(label = "shutter_glow")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 36f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRadius"
    )
    
    Box(
        modifier = Modifier
            .size(72.dp)
            .clickable { onClick() }
            .drawBehind {
                // 粉紫流光環繞
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFB6C1),
                            Color(0xFFE0B0FF),
                            Color.Transparent
                        )
                    ),
                    radius = glowRadius
                )
            }
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFFB6C1), Color(0xFFE0B0FF))
                )
            )
            .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🐰",
            fontSize = 32.sp
        )
    }
}

/**
 * Git Syncing 状态
 */
@Composable
fun GitSyncingStatus() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "✓",
            color = Color(0xFFFFB6C1),
            fontSize = 12.sp
        )
        Text(
            text = "Git Syncing...",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * 底部导航欄
 */
@Composable
fun BottomNavigation() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(icon = "🖼️", label = "雁宝圖庫")
        NavItem(icon = "🧭", label = "推薦拍")
        NavItem(icon = "📷", label = "拍攝頁", isActive = true)
        NavItem(icon = "✨", label = "美具")
        NavItem(icon = "📤", label = "分享")
    }
}

/**
 * 导航項目
 */
@Composable
fun NavItem(icon: String, label: String, isActive: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            color = if (isActive) Color(0xFFFFB6C1) else Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (isActive) Color(0xFFFFB6C1) else Color.White.copy(alpha = 0.5f)
        )
    }
}
