package com.yanbao.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yanbao.camera.data.model.CameraMode

/**
 * 相机模式选择弹窗
 * 设计图：02_camera_modes.png
 * 
 * 6 个模式卡片（3x2 网格）：
 * - 美颜：自然无瑕，素颜也美
 * - 大师：专业级色彩，电影质感
 * - 2.9D：探索三维世界，立体拍摄
 * - AR特效：虚拟互动，趣味自拍
 * - 雁宝记忆：记录珍贵瞬间，永久留存
 * - 原相机：还原真实，无滤镜直出
 */
@Composable
fun CameraModeSelectionDialog(
    onDismiss: () -> Unit,
    onModeSelected: (CameraMode) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 毛玻璃背景
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier
                        .blur(20.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x1AFFFFFF),
                                    Color(0x0DFFFFFF)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    // 标题栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(40.dp))
                        
                        Text(
                            text = "模式选择",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 模式网格（3x2）
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 第一行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ModeCard(
                                icon = "📷",
                                title = "美颜",
                                description = "自然无瑕，素\n颜也美",
                                gradientColors = listOf(Color(0xFFEC4899), Color(0xFFF9A8D4)),
                                onClick = { onModeSelected(CameraMode.BEAUTY) },
                                modifier = Modifier.weight(1f)
                            )
                            
                            ModeCard(
                                icon = "🎬",
                                title = "大师",
                                description = "专业级色彩，\n电影质感",
                                gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA)),
                                onClick = { onModeSelected(CameraMode.MASTER) },
                                modifier = Modifier.weight(1f)
                            )
                            
                            ModeCard(
                                icon = "📦",
                                title = "2.9D",
                                description = "探索三维世界，\n立体拍摄",
                                gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF67E8F9)),
                                onClick = { onModeSelected(CameraMode.TWO_DOT_NINE_D) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // 第二行
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ModeCard(
                                icon = "🕶️",
                                title = "AR特效",
                                description = "虚拟互动，趣\n味自拍",
                                gradientColors = listOf(Color(0xFF14B8A6), Color(0xFF5EEAD4)),
                                onClick = { onModeSelected(CameraMode.AR) },
                                modifier = Modifier.weight(1f)
                            )
                            
                            ModeCard(
                                icon = "💛",
                                title = "雁宝记忆",
                                description = "记录珍贵瞬间，\n永久留存",
                                gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
                                onClick = { onModeSelected(CameraMode.MEMORY) },
                                modifier = Modifier.weight(1f)
                            )
                            
                            ModeCard(
                                icon = "📸",
                                title = "原相机",
                                description = "还原真实，无\n滤镜直出",
                                gradientColors = listOf(Color(0xFF6B7280), Color(0xFF9CA3AF)),
                                onClick = { onModeSelected(CameraMode.NORMAL) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 模式卡片组件
 */
@Composable
fun ModeCard(
    icon: String,
    title: String,
    description: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(0.8f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradientColors[0].copy(alpha = 0.3f),
                            gradientColors[1].copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // 图标背景
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = icon,
                            fontSize = 32.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
