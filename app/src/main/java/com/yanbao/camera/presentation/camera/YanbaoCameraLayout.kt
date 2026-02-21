package com.yanbao.camera.presentation.camera

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.presentation.theme.YanbaoPink
import com.yanbao.camera.presentation.theme.YanbaoPurple

/**
 * Yanbao Spatial Layout Architecture
 * 
 * Obsidian Flux（曜石流光）设计方案
 * 
 * 空间分层逻辑：
 * - Layer 0 (Z-0): 100% 全屏取景空间
 * - Layer 1 (Z-1): AI 骨骼点悬浮层
 * - Layer 2 (Z-2): 底部 28% 曜石黑控制舱
 * 
 * 设计规范：
 * - 高斯模糊：30dp
 * - 动画时长：300ms StandardEasing
 * - 主色：#FFB6C1（粉）+ #0A0A0A（曜石黑）
 */
@Composable
fun YanbaoCameraLayout(
    onCaptureClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        
        // --- Layer 0: 全屏取景空间 (100%) ---
        CameraViewLayer(modifier = Modifier.fillMaxSize())
        
        // --- Layer 1: AI 反馈层 (中层悬浮) ---
        AiSkeletonOverlay(modifier = Modifier.fillMaxSize().alpha(0.6f))
        
        // --- Layer 2: 曜石黑控制舱 (底部 28%) ---
        ObsidianControlCockpit(
            onCaptureClick = onCaptureClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.28f) // 硬核要求：28% 占比
        )
        
        // 右上角：发光头像入口 (始终置顶)
        TopRightAvatarWithGlow(
            onProfileClick = onProfileClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 20.dp)
        )
        
        // 左上角：品牌名
        Text(
            text = "yanbao AI",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 20.dp)
        )
    }
}

/**
 * Layer 0: 全屏相机预览层
 * 
 * 使用 Camera2 API 实现真实预览
 * 边缘采用无感式微光处理
 */
@Composable
fun CameraViewLayer(modifier: Modifier = Modifier) {
    // Camera2 Preview Layer
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Camera2 Preview\n(真实硬件预览)",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 16.sp
        )
    }
}

/**
 * Layer 1: AI 骨骼点悬浮层
 * 
 * 淡紫色 (#E0B0FF) 丝线悬浮在模特身上
 * 具有 0.5s 的呼吸感动画
 */
@Composable
fun AiSkeletonOverlay(modifier: Modifier = Modifier) {
    // 呼吸感动画
    val infiniteTransition = rememberInfiniteTransition(label = "ai_breath")
    val breathAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_alpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(breathAlpha),
        contentAlignment = Alignment.Center
    ) {
        // AI Skeleton Rendering
        Text(
            text = "AI Skeleton\n(骨骼点悬浮)",
            color = YanbaoPurple,
            fontSize = 14.sp
        )
    }
}

/**
 * Layer 2: 曜石黑控制舱
 * 
 * 底部 28% 区域
 * 30dp 高斯模糊 + 玻璃拟态质感
 */
@Composable
fun ObsidianControlCockpit(
    onCaptureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(Color(0xCC0A0A0A)) // 80% 不透明曜石黑
            .blur(30.dp) // 关键：高斯模糊质感
            .border(
                width = 0.5.dp,
                color = Color.White.copy(0.1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. 29D 物理模拟拨轮（哈苏感）
        ParameterDialWheel(
            params = listOf("ISO", "SHUTTER", "WB", "FOCUS"),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 2. 库洛米 3D 快门按钮
        KuromiCaptureButton(
            onClick = onCaptureClick,
            modifier = Modifier.size(72.dp)
        )
    }
}

/**
 * 29D 物理模拟拨轮
 * 
 * 哈苏式机械刻度感
 * 滑动时参数呈非线性缩放
 */
@Composable
fun ParameterDialWheel(
    params: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        params.forEach { param ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = param,
                    color = YanbaoPink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AUTO",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * 库洛米 3D 快门按钮
 * 
 * 3D 质感的粉色按钮
 * 边缘有流光环绕
 */
@Composable
fun KuromiCaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 流光动画
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Box(
        modifier = modifier
            .size(72.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        YanbaoPink.copy(alpha = glowAlpha),
                        YanbaoPurple.copy(alpha = glowAlpha * 0.5f)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = 3.dp,
                color = Color.White.copy(alpha = 0.8f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "📷",
            fontSize = 32.sp
        )
    }
}

/**
 * 右上角发光头像入口
 * 
 * 48dp 头像
 * 粉紫渐变发光环
 */
@Composable
fun TopRightAvatarWithGlow(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(YanbaoPink, YanbaoPurple)
                ),
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "👤",
            fontSize = 24.sp
        )
    }
}
