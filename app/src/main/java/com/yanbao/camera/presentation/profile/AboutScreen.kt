package com.yanbao.camera.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yanbao.camera.R

private val KUROMI_PINK_A = Color(0xFFEC4899)
private val KUROMI_PURPLE_A = Color(0xFF9D4EDD)

@Composable
fun AboutScreen(onBackClick: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A)).statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            Text("关于雁宝 AI", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Brush.radialGradient(listOf(KUROMI_PURPLE_A, KUROMI_PINK_A))), contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(id = R.drawable.ic_camera_kuromi), contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(52.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("yanbao AI", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("版本 1.0.0 · Phase 1", fontSize = 14.sp, color = Color.White.copy(alpha = 0.5f))
                Text("库洛米朋克少女风格相机", fontSize = 14.sp, color = KUROMI_PINK_A)
            }
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
                        "开发团队" to "雁宝 AI 研发组",
                        "技术栈" to "Kotlin · Jetpack Compose · CameraX",
                        "AI 引擎" to "自研 29D 视差算法",
                        "设计风格" to "库洛米（Kuromi）朋克少女",
                        "开源协议" to "MIT License"
                    ).forEach { (key, value) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(key, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Text("© 2026 雁宝 AI · 保留所有权利", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}
