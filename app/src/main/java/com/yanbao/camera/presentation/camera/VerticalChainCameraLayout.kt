package com.yanbao.camera.presentation.camera

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.compose.foundation.layout.fillMaxSize

/**
 * 相机布局 - Vertical Chain版本
 * 
 * 严格要求：
 * 1. 72%取景器 + 28%控制舱
 * 2. 控制舱内使用Vertical Chain等距排布（禁止组件堆叠）
 * 3. 实时40px高斯模糊RenderEffect（背景取景器画面变动透出）
 * 4. 适配iPhone 15 (标准) 和 iPhone 16 Pro Max (长屏)
 */
@Composable
fun VerticalChainCameraLayout(
    modifier: Modifier = Modifier,
    onCaptureClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onFilterClick: (String) -> Unit = {},
    onModeClick: (String) -> Unit = {}
) {
    var screenSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    
    // 计算72%和28%的精确高度
    val viewfinderHeight = remember(screenSize) {
        (screenSize.height * 0.72f).toInt()
    }
    val controlPanelHeight = remember(screenSize) {
        (screenSize.height * 0.28f).toInt()
    }
    
    Log.d("VerticalChainCameraLayout", "屏幕尺寸: ${screenSize.width}x${screenSize.height}")
    Log.d("VerticalChainCameraLayout", "取景器高度: $viewfinderHeight (72%)")
    Log.d("VerticalChainCameraLayout", "控制舱高度: $controlPanelHeight (28%)")
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { screenSize = it }
    ) {
        // 72% 取景器区域（全屏显示）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(density) { viewfinderHeight.toDp() })
                .align(Alignment.TopCenter)
        ) {
            // TODO: 真实Camera2预览层（AndroidView封装SurfaceView）
            // 这里使用占位符，实际开发中必须绑定Camera2的CaptureRequest
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            ) {
                Text(
                    text = "Camera2 Preview\n(Real-time)",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // LBS灵动定位点（顶部左侧）
            LbsLocationIndicatorCompact(
                location = "东京·涩谷",
                isLocating = false,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
            
            // AI场景识别标签（中心上方）
            AiSceneLabelCompact(
                scene = "推荐: 日本 - Tokyo Film",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp)
            )
        }
        
        // 28% 控制舱区域（实时毛玻璃模糊）
        ControlPanelWithVerticalChain(
            height = with(density) { controlPanelHeight.toDp() },
            onCaptureClick = onCaptureClick,
            onGalleryClick = onGalleryClick,
            onFilterClick = onFilterClick,
            onModeClick = onModeClick,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .applyRealTimeBlur() // 实时40px高斯模糊
        )
    }
}

/**
 * 控制面板 - Vertical Chain布局
 * 
 * 垂直链式排布（从上到下）：
 * 1. AI推荐滤镜行
 * 2. 91国滤镜预览网格
 * 3. 快门栏（参数气泡 + 快门按钮 + 分享按钮）
 * 4. 模式栏（9大模式横向滚动）
 * 5. 底部导航栏
 */
@Composable
fun ControlPanelWithVerticalChain(
    height: androidx.compose.ui.unit.Dp,
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFilterClick: (String) -> Unit,
    onModeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                color = Color(0xF20D0D0D) // 95%透明度曜石黑
            )
    ) {
        val (aiFilters, filterGrid, shutterBar, modeBar, navBar) = createRefs()
        
        // 创建Vertical Chain（等距排布）
        createVerticalChain(
            aiFilters, filterGrid, shutterBar, modeBar, navBar,
            chainStyle = androidx.constraintlayout.compose.ChainStyle.SpreadInside
        )
        
        // 1. AI推荐滤镜行
        AiRecommendedFiltersRow(
            filters = listOf("AI智能", "樱花粉", "复古胶片", "城市霓虹", "人像清晰"),
            onFilterClick = onFilterClick,
            modifier = Modifier.constrainAs(aiFilters) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
                .fillMaxWidth()
        )
        
        // 2. 91国滤镜预览网格（横向滚动）
        MasterFilterPreviewGrid(
            filters = listOf("Japan", "USA", "China", "UK", "France", "Korea", "Japan"),
            onFilterClick = onFilterClick,
            modifier = Modifier.constrainAs(filterGrid) {
                top.linkTo(aiFilters.bottom)
                bottom.linkTo(shutterBar.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
                .fillMaxWidth()
        )
        
        // 3. 快门栏
        ShutterBar(
            onCaptureClick = onCaptureClick,
            onGalleryClick = onGalleryClick,
            modifier = Modifier.constrainAs(shutterBar) {
                top.linkTo(filterGrid.bottom)
                bottom.linkTo(modeBar.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
                .fillMaxWidth()
        )
        
        // 4. 模式栏（9大模式）
        ModeBar(
            modes = listOf("照片", "视频", "人像", "夜景", "PRO", "全景", "慢动作", "延时", "大师"),
            currentMode = "照片",
            onModeClick = onModeClick,
            modifier = Modifier.constrainAs(modeBar) {
                top.linkTo(shutterBar.bottom)
                bottom.linkTo(navBar.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
                .fillMaxWidth()
        )
                // 5. 底部导航栏已在YanbaoApp中统一管理        .height(56.dp)
        )
    }
}

/**
 * 实时40px高斯模糊RenderEffect
 * 
 * 关键：背景取景器的画面变动必须能在模糊面板下透出来
 */
@Composable
fun Modifier.applyRealTimeBlur(): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.graphicsLayer {
            // 实时高斯模糊（40px）
            renderEffect = RenderEffect
                .createBlurEffect(
                    40f, // radiusX
                    40f, // radiusY
                    Shader.TileMode.CLAMP
                )
                .asComposeRenderEffect()
        }
    } else {
        // Android 12以下降级方案（半透明背景）
        this.background(Color(0xF20D0D0D))
    }
}

/**
 * AI推荐滤镜行
 */
@Composable
fun AiRecommendedFiltersRow(
    filters: List<String>,
    onFilterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFEC4899),
                                Color(0xFFA78BFA)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (filter == "AI智能") "⭐ $filter" else filter,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 91国滤镜预览网格（横向滚动）
 */
@Composable
fun MasterFilterPreviewGrid(
    filters: List<String>,
    onFilterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 快门栏
 */
@Composable
fun ShutterBar(
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：参数气泡
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ParameterBubble("ISO: 400")
            ParameterBubble("S: 1/125")
        }
        
        // 中间：快门按钮
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFEC4899),
                            Color(0xFFA78BFA)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📷",
                fontSize = 32.sp
            )
        }
        
        // 右侧：分享按钮
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔗",
                fontSize = 20.sp
            )
        }
    }
}

/**
 * 参数气泡
 */
@Composable
fun ParameterBubble(text: String) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFEC4899),
                        Color(0xFFA78BFA)
                    )
                )
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

/**
 * 模式栏（9大模式）
 */
@Composable
fun ModeBar(
    modes: List<String>,
    currentMode: String,
    onModeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        modes.forEach { mode ->
            Text(
                text = mode,
                color = if (mode == currentMode) Color(0xFFEC4899) else Color.White,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 底部导航栏
 */
@Composable
fun BottomNavigationBar(
    currentTab: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D0D))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        listOf("首页", "相机", "相册", "推荐", "我的").forEach { tab ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when(tab) {
                        "首页" -> "🏠"
                        "相机" -> "📷"
                        "相册" -> "🖼️"
                        "推荐" -> "⭐"
                        "我的" -> "👤"
                        else -> ""
                    },
                    fontSize = 20.sp
                )
                Text(
                    text = tab,
                    color = if (tab == currentTab) Color(0xFFEC4899) else Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * LBS灵动定位点（紧凑版）
 */
@Composable
fun LbsLocationIndicatorCompact(
    location: String,
    isLocating: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x80000000))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isLocating) Color(0xFF10B981) else Color(0xFFEC4899))
        )
        Text(
            text = location,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

/**
 * AI场景识别标签（紧凑版）
 */
@Composable
fun AiSceneLabelCompact(
    scene: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFEC4899),
                        Color(0xFFA78BFA)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "🤖 $scene",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
