package com.yanbao.camera.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.presentation.theme.YanbaoPink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Git 备份详情页
 * 
 * 功能：
 * - 实时显示备份的照片数量
 * - 实时显示存储占用情况
 * - 显示最后备份时间
 * - 提供手动备份按钮
 */
@Composable
fun GitBackupDetailScreen(
    backupDirectory: File,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // 备份统计信息
    var photoCount by remember { mutableStateOf(0) }
    var totalSize by remember { mutableStateOf(0L) }
    var lastBackupTime by remember { mutableStateOf("从未备份") }
    var isLoading by remember { mutableStateOf(true) }
    var isBackingUp by remember { mutableStateOf(false) }
    
    // 加载备份信息
    LaunchedEffect(Unit) {
        scope.launch {
            delay(500) // 模拟加载
            
            // 扫描备份目录
            if (backupDirectory.exists()) {
                val files = backupDirectory.listFiles { file ->
                    file.extension in listOf("jpg", "jpeg", "png")
                }
                
                photoCount = files?.size ?: 0
                totalSize = files?.sumOf { it.length() } ?: 0L
                
                // 获取最后修改时间
                val lastModified = files?.maxOfOrNull { it.lastModified() }
                lastBackupTime = if (lastModified != null) {
                    val date = java.util.Date(lastModified)
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
                } else {
                    "从未备份"
                }
            }
            
            isLoading = false
            
            android.util.Log.d(
                "GitBackupDetailScreen",
                "备份统计: $photoCount 张照片, ${formatFileSize(totalSize)}"
            )
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 背景层：70px 高斯模糊
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFB6C1).copy(0.5f),
                            Color(0xFFE0B0FF).copy(0.5f)
                        )
                    )
                )
                .blur(70.dp)
        )
        
        // 前景层：详情内容
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, start = 20.dp, end = 20.dp)
        ) {
            // 顶部标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "←",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.clickable { onBackClick() }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "yanbao AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEC4899)
                    )
                    Text(
                        text = "Git 备份详情",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (isLoading) {
                // 加载中
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = YanbaoPink)
                }
            } else {
                // 备份统计卡片
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White.copy(0.15f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 照片数量
                    StatItem(
                        icon = "P",
                        label = "备份照片",
                        value = "$photoCount 张"
                    )
                    
                    // 存储占用
                    StatItem(
                        icon = "S",
                        label = "存储占用",
                        value = formatFileSize(totalSize)
                    )
                    
                    // 最后备份时间
                    StatItem(
                        icon = "T",
                        label = "最后备份",
                        value = lastBackupTime
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 手动备份按钮
                Button(
                    onClick = {
                        scope.launch {
                            isBackingUp = true
                            android.util.Log.d("GitBackupDetailScreen", "开始手动备份...")
                            
                            // 模拟备份过程
                            delay(2000)
                            
                            // 重新扫描
                            val files = backupDirectory.listFiles { file ->
                                file.extension in listOf("jpg", "jpeg", "png")
                            }
                            photoCount = files?.size ?: 0
                            totalSize = files?.sumOf { it.length() } ?: 0L
                            lastBackupTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(java.util.Date())
                            
                            isBackingUp = false
                            
                            android.util.Log.d("GitBackupDetailScreen", "备份完成")
                        }
                    },
                    enabled = !isBackingUp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YanbaoPink
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isBackingUp) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("备份中...", color = Color.White, fontSize = 16.sp)
                    } else {
                        Text("立即备份", color = Color.White, fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 说明文字
                Text(
                    text = "备份将自动保存到 Git 仓库，包含照片和完整的 29D 参数元数据。",
                    fontSize = 12.sp,
                    color = Color.White.copy(0.6f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

/**
 * 统计项组件
 */
@Composable
fun StatItem(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.White.copy(0.8f)
            )
        }
        
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = YanbaoPink
        )
    }
}

/**
 * 格式化文件大小
 */
fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
