package com.yanbao.camera.presentation.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.yanbao.camera.R
import com.yanbao.camera.ui.theme.KUROMI_PINK

/**
 * 相机分类弹窗 — 严格对应 15_camera_09_category.png
 *
 * 布局：
 *  顶部：← 返回  "YanBao AI Master Mode"  🏠
 *  内容：2列×3行 毛玻璃方形按钮
 *    风景 | 夜景
 *    人像 | 复古
 *    美食 | 其他
 *  底部：[取消] 按钮
 *
 * 背景：深色毛玻璃 + 粉色霓虹边框
 */
data class CameraCategory(
    val id: String,
    val name: String,
    val iconRes: Int
)

@Composable
fun CategoryDialog(
    onDismiss: () -> Unit = {},
    onCategorySelected: (String) -> Unit = {}
) {
    val categories = listOf(
        CameraCategory("landscape", "风景", R.drawable.ic_mode_basic),
        CameraCategory("night", "夜景", R.drawable.ic_mode_29d),
        CameraCategory("portrait", "人像", R.drawable.ic_mode_beauty),
        CameraCategory("vintage", "复古", R.drawable.ic_mode_master),
        CameraCategory("food", "美食", R.drawable.ic_mode_memory),
        CameraCategory("other", "其他", R.drawable.ic_mode_ar)
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A2E).copy(alpha = 0.97f))
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(KUROMI_PINK, Color(0xFF9D4EDD))
                    ),
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // 顶部导航
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_kuromi),
                        contentDescription = "关闭",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onDismiss() }
                    )
                    Text(
                        text = "YanBao AI Master Mode",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_yanbao_camera),
                        contentDescription = "相机",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2列×3行 分类按钮
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (col in 0..1) {
                                val idx = row * 2 + col
                                val category = categories[idx]
                                CategoryButton(
                                    category = category,
                                    onClick = { onCategorySelected(category.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 取消按钮
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    KUROMI_PINK.copy(alpha = 0.2f),
                                    Color(0xFF9D4EDD).copy(alpha = 0.2f)
                                )
                            )
                        )
                        .border(1.5.dp, KUROMI_PINK, RoundedCornerShape(26.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "取消",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * 单个分类按钮 — 毛玻璃 + 粉色霓虹边框 + 大图标 + 文字
 */
@Composable
fun CategoryButton(
    category: CameraCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(
                1.5.dp,
                Brush.linearGradient(
                    colors = listOf(
                        KUROMI_PINK.copy(alpha = 0.7f),
                        Color(0xFF9D4EDD).copy(alpha = 0.4f)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(category.iconRes),
                contentDescription = category.name,
                tint = KUROMI_PINK,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
