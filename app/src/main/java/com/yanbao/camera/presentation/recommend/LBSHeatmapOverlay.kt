package com.yanbao.camera.presentation.recommend

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.presentation.theme.YanbaoPink
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 推荐模块 - LBS 热力图浮窗
 * 
 * 功能：
 * - 在相机主界面添加"推荐"入口，点击弹出浮窗
 * - 显示 91 国滤镜的 LBS 热力图
 * - 显示当前坐标最匹配的 29D 参数包
 */
@Composable
fun LBSHeatmapOverlay(
    visible: Boolean,
    currentLocation: Pair<Double, Double>?, // (纬度, 经度)
    onDismiss: () -> Unit,
    onApplyParameters: (params: Map<String, Float>) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        ) {
            // 主内容区
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .background(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .blur(25.dp)
                    .padding(20.dp)
            ) {
                // 顶部：标题 + 关闭按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "91 国滤镜热力图",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Text(
                            text = "X",
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 当前位置信息
                currentLocation?.let { (lat, lng) ->
                    Text(
                        text = "当前位置：${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}",
                        fontSize = 14.sp,
                        color = Color.White.copy(0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 热力图
                HeatmapCanvas(
                    currentLocation = currentLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 推荐参数
                val recommendedParams = getRecommendedParameters(currentLocation)
                
                Text(
                    text = "推荐参数包",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.Black.copy(0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    recommendedParams.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                fontSize = 14.sp,
                                color = Color.White.copy(0.7f)
                            )
                            Text(
                                text = String.format("%.2f", value),
                                fontSize = 14.sp,
                                color = YanbaoPink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 应用按钮
                Button(
                    onClick = {
                        onApplyParameters(recommendedParams)
                        android.util.Log.d("LBSHeatmapOverlay", "应用参数包: $recommendedParams")
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YanbaoPink
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用参数", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * 热力图画布
 * 
 * 显示 91 国滤镜的 LBS 热力分布
 */
@Composable
fun HeatmapCanvas(
    currentLocation: Pair<Double, Double>?,
    modifier: Modifier = Modifier
) {
    // 模拟 91 个热点位置（实际应从数据库读取）
    val hotspots = remember {
        List(91) { index ->
            val lat = -90.0 + (180.0 * index / 91.0) + (Math.random() - 0.5) * 10
            val lng = -180.0 + (360.0 * index / 91.0) + (Math.random() - 0.5) * 10
            val intensity = (Math.random() * 0.5 + 0.5).toFloat() // 0.5 - 1.0
            Triple(lat, lng, intensity)
        }
    }
    
    Canvas(
        modifier = modifier
            .background(Color.Black.copy(0.3f), RoundedCornerShape(12.dp))
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // 绘制世界地图轮廓（简化版）
        drawRect(
            color = Color.White.copy(0.1f),
            size = size
        )
        
        // 绘制热点
        hotspots.forEach { (lat, lng, intensity) ->
            val x = ((lng + 180.0) / 360.0 * canvasWidth).toFloat()
            val y = ((90.0 - lat) / 180.0 * canvasHeight).toFloat()
            
            // 热力圆圈（颜色从黄到红）
            val color = Color(
                red = 1f,
                green = 1f - intensity,
                blue = 0f,
                alpha = intensity * 0.6f
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0f)
                    ),
                    center = Offset(x, y),
                    radius = 30f
                ),
                radius = 30f,
                center = Offset(x, y)
            )
        }
        
        // 绘制当前位置标记
        currentLocation?.let { (lat, lng) ->
            val x = ((lng + 180.0) / 360.0 * canvasWidth).toFloat()
            val y = ((90.0 - lat) / 180.0 * canvasHeight).toFloat()
            
            // 蓝色脉冲圆圈
            drawCircle(
                color = Color.Cyan,
                radius = 8f,
                center = Offset(x, y)
            )
            
            drawCircle(
                color = Color.Cyan.copy(alpha = 0.3f),
                radius = 16f,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * 根据当前位置获取推荐的 29D 参数包
 * 
 * 实际应用中，应从数据库查询最近的热点参数
 */
fun getRecommendedParameters(location: Pair<Double, Double>?): Map<String, Float> {
    // 模拟推荐参数（实际应从数据库读取）
    return mapOf(
        "曝光补偿" to 0.3f,
        "对比度" to 1.2f,
        "饱和度" to 1.1f,
        "色温" to 5500f,
        "锐度" to 0.8f,
        "暗角" to 0.2f,
        "颗粒感" to 0.15f,
        "色调偏移" to 0.05f
    )
}
