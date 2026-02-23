package com.yanbao.camera.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val KUROMI_PINK_H = Color(0xFFEC4899)

@Composable
fun HelpScreen(onBackClick: () -> Unit = {}) {
    val faqs = listOf(
        "如何使用 29D 模式？" to "在相机界面底部模式栏选择「29D」，然后调整 ISO、快门、EV、色温四个参数滑块，点击快门拍摄即可。",
        "什么是雁宝记忆？" to "雁宝记忆是一种特殊的拍摄模式，会自动记录拍摄时的时间、地点、天气等信息，并生成专属的记忆相册。",
        "如何备份照片到 GitHub？" to "前往「我的」→「Git 同步备份」，首次使用需要配置 GitHub Token。配置完成后点击「立即备份」即可。",
        "如何使用 AI 推荐功能？" to "点击底部导航「推荐」，地图上会显示附近的热门拍摄地点。点击地点标记可查看详情并应用推荐滤镜。",
        "编辑模块支持哪些操作？" to "支持裁剪、旋转、翻转、透视、亮度、对比度、饱和度、色温、锐化、降噪、晕影、颗粒感等 18 种调整，以及撤销/重做功能。",
        "如何进行多选删除照片？" to "在相册界面长按任意照片进入多选模式，勾选需要删除的照片，点击底部「删除」按钮即可批量删除。"
    )
    var expandedIndex by remember { mutableIntStateOf(-1) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
            }
            Text(
                "帮助中心",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "常见问题",
                    color = KUROMI_PINK_H,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            itemsIndexed(faqs) { index, (question, answer) ->
                val isExpanded = expandedIndex == index
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedIndex = if (isExpanded) -1 else index }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                question,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = KUROMI_PINK_H,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                answer,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
