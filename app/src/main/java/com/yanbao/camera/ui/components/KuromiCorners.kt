package com.yanbao.camera.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yanbao.camera.R

/**
 * 库洛米装饰角落组件
 * 
 * 在每个屏幕的四个角落添加库洛米角色装饰
 * 大小约 60×60dp，位置固定，不遮挡交互元素
 * 
 * 设计规范：
 * - 大小：60×60dp
 * - 位置：四个角落
 * - 资源：从设计图中提取的PNG图片
 * - 文件：kuromi_tl.png, kuromi_tr.png, kuromi_bl.png, kuromi_br.png
 */

/**
 * 四个角落的库洛米装饰
 * 
 * 使用示例：
 * ```
 * Box(modifier = Modifier.fillMaxSize()) {
 *     // 屏幕内容
 *     KuromiCorners()
 * }
 * ```
 */
@Composable
fun KuromiCorners(
    modifier: Modifier = Modifier,
    size: Int = 60,
    showCorners: Boolean = true
) {
    if (!showCorners) return
    
    Box(modifier = modifier.fillMaxSize()) {
        // 左上角
        Image(
            painter = painterResource(id = R.drawable.kuromi_tl),
            contentDescription = "Kuromi Top Left",
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(size.dp)
        )
        
        // 右上角
        Image(
            painter = painterResource(id = R.drawable.kuromi_tr),
            contentDescription = "Kuromi Top Right",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(size.dp)
        )
        
        // 左下角
        Image(
            painter = painterResource(id = R.drawable.kuromi_bl),
            contentDescription = "Kuromi Bottom Left",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(size.dp)
        )
        
        // 右下角
        Image(
            painter = painterResource(id = R.drawable.kuromi_br),
            contentDescription = "Kuromi Bottom Right",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(size.dp)
        )
    }
}

/**
 * 单个库洛米装饰（可选位置）
 * 
 * 使用示例：
 * ```
 * KuromiDecoration(
 *     position = KuromiPosition.TOP_START,
 *     size = 60
 * )
 * ```
 */
enum class KuromiPosition {
    TOP_START,
    TOP_END,
    BOTTOM_START,
    BOTTOM_END
}

@Composable
fun KuromiDecoration(
    modifier: Modifier = Modifier,
    position: KuromiPosition = KuromiPosition.TOP_START,
    size: Int = 60
) {
    Box(modifier = modifier.fillMaxSize()) {
        val (alignment, drawableId) = when (position) {
            KuromiPosition.TOP_START -> Alignment.TopStart to R.drawable.kuromi_tl
            KuromiPosition.TOP_END -> Alignment.TopEnd to R.drawable.kuromi_tr
            KuromiPosition.BOTTOM_START -> Alignment.BottomStart to R.drawable.kuromi_bl
            KuromiPosition.BOTTOM_END -> Alignment.BottomEnd to R.drawable.kuromi_br
        }
        
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = "Kuromi Decoration",
            modifier = Modifier
                .align(alignment)
                .size(size.dp)
        )
    }
}
