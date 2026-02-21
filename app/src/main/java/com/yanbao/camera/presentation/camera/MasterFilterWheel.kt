package com.yanbao.camera.presentation.camera

import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.data.filter.MasterFilter91
import com.yanbao.camera.data.filter.MasterFilter91Database
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 91国大师滤镜 - LBS灵动拨盘
 * 
 * 核心功能：
 * - 横向滚动机械刻度拨盘
 * - LBS自动定位当前国家
 * - 左右滑动快速切换91国
 * - 实时渲染（毫秒级变色）
 * - 选中态库洛米粉流光效果
 * 
 * 视觉表现：
 * - 在28%控制舱上方，出现一个横向滚动的机械刻度撥盘
 * - 撥盘默认停留在当前国家（如：日本 - Tokyo Film）
 * - 左右滑动可快速切换91国方案
 * - 撥盘滚动的瞬间，取景器画面必须毫秒级变色
 */
@Composable
fun MasterFilterWheel(
    selectedFilterId: Int,
    onFilterSelected: (MasterFilter91) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filters = remember { MasterFilter91Database.filters }
    
    // 当前选中的滤镜索引
    var selectedIndex by remember { mutableStateOf(
        filters.indexOfFirst { it.id == selectedFilterId }.takeIf { it >= 0 } ?: 0
    ) }
    
    // 滚动偏移量
    var scrollOffset by remember { mutableStateOf(0f) }
    
    // 是否正在拖动
    var isDragging by remember { mutableStateOf(false) }
    
    // 库洛米粉流光动画
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    // LBS自动定位
    LaunchedEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        try {
            val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            if (location != null) {
                val nearestFilter = MasterFilter91Database.findNearestFilter(
                    location.latitude,
                    location.longitude
                )
                val nearestIndex = filters.indexOf(nearestFilter)
                if (nearestIndex >= 0) {
                    selectedIndex = nearestIndex
                    onFilterSelected(nearestFilter)
                    Log.d("MasterFilterWheel", "✅ LBS自动定位: ${nearestFilter.displayName}")
                }
            }
        } catch (e: SecurityException) {
            Log.w("MasterFilterWheel", "⚠️ 缺少位置权限，使用默认滤镜")
        }
    }
    
    // 自动居中动画
    LaunchedEffect(selectedIndex, isDragging) {
        if (!isDragging) {
            // 平滑滚动到选中位置
            val targetOffset = -selectedIndex * 120f
            val startOffset = scrollOffset
            val duration = 300
            val startTime = System.currentTimeMillis()
            
            while (System.currentTimeMillis() - startTime < duration) {
                val progress = (System.currentTimeMillis() - startTime).toFloat() / duration
                val easedProgress = EaseOutCubic.transform(progress)
                scrollOffset = startOffset + (targetOffset - startOffset) * easedProgress
                delay(16) // 60fps
            }
            scrollOffset = targetOffset
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.6f),
                        Color.Black.copy(alpha = 0.3f)
                    )
                )
            )
            .blur(25.dp) // 毛玻璃效果
    ) {
        // 机械刻度拨盘
        Row(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            // 计算最近的滤镜索引
                            val nearestIndex = (-scrollOffset / 120f).roundToInt()
                                .coerceIn(0, filters.size - 1)
                            selectedIndex = nearestIndex
                            onFilterSelected(filters[nearestIndex])
                            
                            Log.d("MasterFilterWheel", "🎯 滤镜切换: ${filters[nearestIndex].displayName}")
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scrollOffset += dragAmount
                            // 实时更新选中的滤镜（毫秒级响应）
                            val nearestIndex = (-scrollOffset / 120f).roundToInt()
                                .coerceIn(0, filters.size - 1)
                            if (nearestIndex != selectedIndex) {
                                selectedIndex = nearestIndex
                                onFilterSelected(filters[nearestIndex])
                                
                                Log.d("MasterFilterWheel", "⚡ 实时切换: ${filters[nearestIndex].displayName}")
                            }
                        }
                    )
                }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            filters.forEachIndexed { index, filter ->
                val offset = scrollOffset + index * 120f
                val isSelected = index == selectedIndex
                val distance = abs(offset)
                val scale = (1f - distance / 500f).coerceIn(0.6f, 1f)
                val alpha = (1f - distance / 500f).coerceIn(0.3f, 1f)
                
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(80.dp)
                        .padding(4.dp)
                        .offset(x = offset.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) {
                                // 库洛米粉流光效果
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFEC4899).copy(alpha = glowAlpha),
                                        Color(0xFFA78BFA).copy(alpha = glowAlpha)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.1f * alpha),
                                        Color.White.copy(alpha = 0.05f * alpha)
                                    )
                                )
                            }
                        )
                        .drawBehind {
                            if (isSelected) {
                                // 流光外圈
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFEC4899).copy(alpha = glowAlpha * 0.5f),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension / 2 + 20f,
                                    center = Offset(size.width / 2, size.height / 2)
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // 国旗 Emoji（简化版，使用国家代码首字母）
                        Text(
                            text = filter.countryCode,
                            fontSize = (20 * scale).sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = Color.White.copy(alpha = alpha)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 滤镜名称
                        Text(
                            text = filter.filterName,
                            fontSize = (10 * scale).sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = Color.White.copy(alpha = alpha),
                            maxLines = 1
                        )
                    }
                }
            }
        }
        
        // 中心指示器
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .height(100.dp)
                .background(Color(0xFFEC4899))
        )
        
        // 当前选中的滤镜名称（底部显示）
        Text(
            text = filters[selectedIndex].displayName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}
