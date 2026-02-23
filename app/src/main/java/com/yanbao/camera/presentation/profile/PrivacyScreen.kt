package com.yanbao.camera.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val KUROMI_PINK = Color(0xFFEC4899)

@Composable
fun PrivacyScreen(onBackClick: () -> Unit = {}) {
    val items = listOf(
        "位置权限" to "用于 AI 推荐附近拍摄地点",
        "相机权限" to "用于拍照和录像功能",
        "存储权限" to "用于保存和读取照片",
        "网络权限" to "用于 AI 滤镜和 Git 备份",
        "数据收集" to "匿名使用数据，用于改善产品体验",
        "数据删除" to "可随时在此删除所有本地数据"
    )
    var locationEnabled by remember { mutableStateOf(true) }
    var analyticsEnabled by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A)).statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            Text("隐私设置", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("权限管理", color = KUROMI_PINK, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text("位置权限", color = Color.White, fontSize = 15.sp); Text("用于 AI 推荐附近拍摄地点", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp) }
                            Switch(checked = locationEnabled, onCheckedChange = { locationEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = KUROMI_PINK))
                        }
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column { Text("匿名数据收集", color = Color.White, fontSize = 15.sp); Text("帮助改善产品体验", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp) }
                            Switch(checked = analyticsEnabled, onCheckedChange = { analyticsEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = KUROMI_PINK))
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("隐私说明", color = KUROMI_PINK, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("雁宝 AI 相机承诺：\n• 所有照片数据仅存储在您的设备本地\n• Git 备份功能由您主动触发，数据存储在您的 GitHub 仓库\n• 我们不会向第三方出售您的任何数据\n• 您可以随时删除所有本地数据", color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}
