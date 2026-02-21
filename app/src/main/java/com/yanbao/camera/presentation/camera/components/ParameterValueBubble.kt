package com.yanbao.camera.presentation.camera.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 29D 参数数值气泡
 * 
 * 当用户滑动参数滑块时，在画面上方弹出实时数值气泡
 * 
 * 规格：
 * - 毛玻璃效果（blur 25.dp）
 * - 粉紫渐变边框
 * - 等宽字体显示数值
 * - 2秒后自动消失
 */
@Composable
fun ParameterValueBubble(
    parameterName: String,
    value: String,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }

    // 2秒后自动隐藏
    LaunchedEffect(value) {
        visible = true
        delay(2000)
        visible = false
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xCC0A0A0A),
                    shape = RoundedCornerShape(16.dp)
                )
                .blur(25.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 参数名称
                Text(
                    text = parameterName,
                    color = Color(0xFFA78BFA), // 靈動紫
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                // 参数数值（等宽字体）
                Text(
                    text = value,
                    color = Color(0xFFEC4899), // 库洛米粉
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace // 等宽字体
                )
            }
        }
    }
}

/**
 * 29D 参数数值气泡管理器
 * 
 * 在画面上方显示当前正在调节的参数气泡
 */
@Composable
fun ParameterBubbleOverlay(
    currentParameter: Pair<String, String>?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        currentParameter?.let { (name, value) ->
            ParameterValueBubble(
                parameterName = name,
                value = value,
                modifier = Modifier.padding(top = 100.dp)
            )
        }
    }
}
