package com.yanbao.camera.presentation.editor

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.data.filter.MasterFilter91
import com.yanbao.camera.data.filter.MasterFilter91Database

/**
 * 91国大师滤镜 - 编辑模块网格
 * 
 * 核心功能：
 * - 底部面板网格布局（5列 x 18行 = 90个可见 + 1个原图）
 * - 点击滤镜后显示29D参数解析
 * - 实时预览渲染
 * - 选中态库洛米粉流光效果
 * 
 * 视觉表现：
 * - 在编辑界面底部28%控制舱区域
 * - 网格布局展示91个滤镜缩略图
 * - 点击滤镜后，在滤镜下方显示29D参数气泡
 * - 选中态带库洛米粉流光效果
 */
@Composable
fun MasterFilterGrid(
    selectedFilterId: Int,
    onFilterSelected: (MasterFilter91) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = remember { MasterFilter91Database.filters }
    
    // 当前选中的滤镜
    var selectedFilter by remember { mutableStateOf(
        filters.firstOrNull { it.id == selectedFilterId } ?: filters[0]
    ) }
    
    // 是否显示29D参数面板
    var showParameters by remember { mutableStateOf(false) }
    
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
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.8f),
                        Color.Black.copy(alpha = 0.6f)
                    )
                )
            )
            .blur(25.dp) // 毛玻璃效果
    ) {
        // 滤镜网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                val isSelected = filter.id == selectedFilter.id
                
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
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
                                        Color.White.copy(alpha = 0.15f),
                                        Color.White.copy(alpha = 0.05f)
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
                                    radius = size.minDimension / 2 + 15f,
                                    center = Offset(size.width / 2, size.height / 2)
                                )
                            }
                        }
                        .clickable {
                            selectedFilter = filter
                            showParameters = true
                            onFilterSelected(filter)
                            
                            Log.d("MasterFilterGrid", "🎨 滤镜选中: ${filter.displayName}")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // 国家代码
                        Text(
                            text = filter.countryCode,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 滤镜名称
                        Text(
                            text = filter.filterName,
                            fontSize = 8.sp,
                            fontWeight = if (isSelected) FontWeight.Normal else FontWeight.Light,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
        
        // 29D参数面板（选中滤镜时显示）
        if (showParameters) {
            MasterFilter29DParametersPanel(
                filter = selectedFilter,
                onDismiss = { showParameters = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

/**
 * 29D参数解析面板
 * 
 * 显示当前滤镜的29D参数矩阵：
 * - D1-D5: 基础色调参数
 * - D6-D29: 高级渲染参数
 */
@Composable
fun MasterFilter29DParametersPanel(
    filter: MasterFilter91,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A).copy(alpha = 0.95f),
                        Color(0xFF0D0D0D).copy(alpha = 0.95f)
                    )
                )
            )
            .padding(12.dp)
    ) {
        // 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filter.displayName} - 29D参数矩阵",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEC4899)
            )
            
            Text(
                text = "✕",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.clickable { onDismiss() }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 参数网格（5行 x 6列）
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 分5行显示29个参数
            for (row in 0..4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0..5) {
                        val index = row * 6 + col
                        if (index < 29) {
                            ParameterBubble(
                                parameterName = "D${index + 1}",
                                parameterValue = filter.parameters[index],
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单个参数气泡
 */
@Composable
fun ParameterBubble(
    parameterName: String,
    parameterValue: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFA78BFA).copy(alpha = 0.3f),
                        Color(0xFFEC4899).copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$parameterName: ${String.format("%.2f", parameterValue)}",
            fontSize = 8.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )
    }
}
