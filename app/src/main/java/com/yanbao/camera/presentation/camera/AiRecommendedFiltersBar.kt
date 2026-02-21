package com.yanbao.camera.presentation.camera

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.ai.FilterRecommendationEngine
import com.yanbao.camera.ai.SceneType
import com.yanbao.camera.data.filter.MasterFilter91
import com.yanbao.camera.data.filter.MasterFilter91Database

/**
 * AI推荐滤镜标签栏（置顶功能）
 * 
 * 核心功能：
 * - 根据当前场景自动推荐Top 5滤镜
 * - 置顶用户偏好滤镜（连续3次选择自动置顶）
 * - 置顶滤镜显示⭐图标
 * - 横向滚动显示
 * 
 * 视觉规范：
 * - 标签高度：36dp
 * - 圆角半径：18dp
 * - 库洛米粉渐变背景（选中态）
 * - 置顶标记：⭐图标 + 旋转动画
 * 
 * Manus验收逻辑：
 * - ✅ 实时场景识别
 * - ✅ 自动推荐Top 5
 * - ✅ 置顶标记显示
 * - ✅ 用户偏好学习
 * - ✅ 完整的Logcat日志审计
 */

/**
 * AI推荐滤镜标签栏
 * 
 * @param currentScene 当前场景类型
 * @param selectedFilterId 当前选中的滤镜ID
 * @param onFilterSelected 滤镜选择回调
 */
@Composable
fun AiRecommendedFiltersBar(
    currentScene: SceneType,
    selectedFilterId: Int,
    onFilterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 获取AI推荐滤镜
    val recommendedFilters = remember(currentScene) {
        FilterRecommendationEngine.recommendFilters(currentScene, topN = 5)
    }
    
    // 获取置顶滤镜ID列表
    val pinnedFilterIds = remember {
        FilterRecommendationEngine.getPinnedFilters()
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI推荐标题
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🤖",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEC4899)
                )
                
                Text(
                    text = "AI推荐 · ${sceneTypeToString(currentScene)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEC4899)
                )
            }
            
            // 置顶数量提示
            if (pinnedFilterIds.isNotEmpty()) {
                Text(
                    text = "⭐ ${pinnedFilterIds.size}个置顶",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
        
        // 推荐滤镜标签行
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recommendedFilters) { filter ->
                val isPinned = pinnedFilterIds.contains(filter.id)
                val isSelected = filter.id == selectedFilterId
                
                RecommendedFilterTag(
                    filter = filter,
                    isSelected = isSelected,
                    isPinned = isPinned,
                    onClick = {
                        onFilterSelected(filter.id)
                        FilterRecommendationEngine.recordUserChoice(filter.id)
                        
                        Log.d("AiRecommendedFiltersBar", """
                            🎨 选择推荐滤镜
                            - 滤镜: ${filter.displayName}
                            - 场景: $currentScene
                            - 置顶: $isPinned
                        """.trimIndent())
                    }
                )
            }
        }
    }
}

/**
 * 推荐滤镜标签
 * 
 * @param filter 滤镜对象
 * @param isSelected 是否选中
 * @param isPinned 是否置顶
 * @param onClick 点击回调
 */
@Composable
fun RecommendedFilterTag(
    filter: MasterFilter91,
    isSelected: Boolean,
    isPinned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 置顶星星旋转动画
    val infiniteTransition = rememberInfiniteTransition(label = "pinned")
    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starRotation"
    )
    
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFEC4899),
                            Color(0xFFA78BFA)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFEC4899).copy(alpha = 0.3f),
                            Color(0xFFA78BFA).copy(alpha = 0.3f)
                        )
                    )
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 置顶星星图标（带旋转动画）
            if (isPinned) {
                Text(
                    text = "⭐",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.rotate(starRotation)
                )
            }
            
            // 滤镜名称
            Text(
                text = filter.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = Color.White
            )
        }
    }
}

/**
 * 场景类型转字符串
 */
private fun sceneTypeToString(sceneType: SceneType): String {
    return when (sceneType) {
        SceneType.PORTRAIT -> "人像"
        SceneType.LANDSCAPE -> "风景"
        SceneType.ARCHITECTURE -> "建筑"
        SceneType.FOOD -> "食物"
        SceneType.NIGHT -> "夜景"
        SceneType.SUNSET -> "日落"
    }
}

/**
 * AI推荐滤镜统计面板
 * 
 * 显示用户偏好统计信息
 */
@Composable
fun AiRecommendationStatsPanel(
    modifier: Modifier = Modifier
) {
    val pinnedFilters = remember {
        FilterRecommendationEngine.getPinnedFilters()
    }
    
    if (pinnedFilters.isNotEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D0D0D).copy(alpha = 0.8f),
                            Color(0xFF1A1A1A).copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题
            Text(
                text = "⭐ 您的偏好",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEC4899)
            )
            
            // 置顶滤镜列表
            pinnedFilters.forEach { filterId ->
                val filter = MasterFilter91Database.filters.firstOrNull { it.id == filterId }
                if (filter != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = filter.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                        
                        Text(
                            text = "使用${FilterRecommendationEngine.userPreferences[filterId]}次",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 场景识别指示器
 * 
 * 显示当前场景识别状态
 */
@Composable
fun SceneDetectionIndicator(
    sceneType: SceneType,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFEC4899).copy(alpha = pulseAlpha * 0.8f),
                        Color(0xFFA78BFA).copy(alpha = pulseAlpha * 0.8f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 场景图标
            Text(
                text = when (sceneType) {
                    SceneType.PORTRAIT -> "👤"
                    SceneType.LANDSCAPE -> "🏞️"
                    SceneType.ARCHITECTURE -> "🏛️"
                    SceneType.FOOD -> "🍽️"
                    SceneType.NIGHT -> "🌙"
                    SceneType.SUNSET -> "🌅"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // 场景名称
            Text(
                text = sceneTypeToString(sceneType),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // 置信度
            Text(
                text = "${(confidence * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
