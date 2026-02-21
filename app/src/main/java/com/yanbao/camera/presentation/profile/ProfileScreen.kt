package com.yanbao.camera.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter

/**
 * 个人中心界面
 * 
 * 功能：
 * - 大圆形头像（带描边）
 * - ID（88888）、会员号（YB-88888）、剩余天数
 * - 头像/背景/ID 修改
 * - SharedPreferences 持久化
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()
    
    // 头像选择器
    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateAvatar(it) }
    }
    
    // 背景选择器
    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateBackground(it) }
    }
    
    // ID 编辑对话框
    var showIdDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部：背景墙 + 头像
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // 背景墙
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFA78BFA),  // 紫色
                                    Color(0xFFEC4899)   // 粉色
                                )
                            )
                        )
                        .clickable {
                            backgroundLauncher.launch("image/*")
                        }
                ) {
                    // 如果有自定义背景，显示图片
                    profile.backgroundUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Background",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // 背景遮罩
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                }
                
                // 返回按钮
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Text("←", fontSize = 28.sp, color = Color.White)
                }
                
                // 头像
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 60.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .background(Color.Gray)
                            .clickable {
                                avatarLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        profile.avatarUri?.let { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } ?: run {
                            Text(
                                text = "📷",
                                fontSize = 48.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(70.dp))
            
            // 中间：用户信息
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 用户名
                Text(
                    text = profile.userName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // ID（可点击编辑）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showIdDialog = true }
                ) {
                    Text(
                        text = "ID: ${profile.userId}",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✏️",
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 会员信息卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2A2A2A)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "会员号",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = profile.memberNumber,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEC4899)
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "与雁宝同行",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${profile.daysWithYanbao} 天",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 位置信息
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = profile.location,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 底部：设置选项
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                SettingItem(
                    icon = "🎨",
                    title = "更换背景",
                    onClick = { backgroundLauncher.launch("image/*") }
                )
                
                SettingItem(
                    icon = "📸",
                    title = "更换头像",
                    onClick = { avatarLauncher.launch("image/*") }
                )
                
                SettingItem(
                    icon = "✏️",
                    title = "修改 ID",
                    onClick = { showIdDialog = true }
                )
                
                SettingItem(
                    icon = "💾",
                    title = "Git 同步备份",
                    onClick = { viewModel.performGitBackup() }
                )
                
                // 备份状态显示
                backupStatus?.let { status ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2A2A2A)
                        )
                    ) {
                        Text(
                            text = status,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
    
    // ID 编辑对话框
    if (showIdDialog) {
        EditIdDialog(
            currentId = profile.userId,
            onConfirm = { newId ->
                viewModel.updateUserId(newId)
                showIdDialog = false
            },
            onDismiss = { showIdDialog = false }
        )
    }
}

/**
 * 设置选项
 */
@Composable
fun SettingItem(
    icon: String,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A2A))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            fontSize = 24.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}

/**
 * ID 编辑对话框
 */
@Composable
fun EditIdDialog(
    currentId: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newId by remember { mutableStateOf(currentId) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("修改 ID", fontWeight = FontWeight.Bold)
        },
        text = {
            OutlinedTextField(
                value = newId,
                onValueChange = { newId = it },
                label = { Text("新 ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(newId) }) {
                Text("确认", color = Color(0xFFEC4899))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = Color(0xFF2A2A2A),
        textContentColor = Color.White
    )
}
