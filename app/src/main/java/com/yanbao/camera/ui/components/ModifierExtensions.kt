package com.yanbao.camera.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth as composeFillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight as composeFillMaxHeight
import androidx.compose.foundation.layout.weight as composeWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 水平对齐修饰符 - 用于Row中的元素
 * 使用Compose内置的align函数
 */
fun Modifier.align(alignment: Alignment.Vertical): Modifier {
    return this.then(Modifier.align(alignment))
}

/**
 * 权重修饰符 - 用于Row/Column中的元素
 * 使用Compose内置的weight函数
 */
fun Modifier.weight(weight: Float, fill: Boolean = true): Modifier {
    return this.then(composeWeight(weight, fill))
}

/**
 * 填充修饰符 - 用于Row中的元素
 */
fun RowScope.Modifier.fillMaxWidth(fraction: Float = 1f): Modifier {
    return this.composeFillMaxWidth(fraction)
}

/**
 * 填充修饰符 - 用于Column中的元素
 */
fun ColumnScope.Modifier.fillMaxHeight(fraction: Float = 1f): Modifier {
    return this.composeFillMaxHeight(fraction)
}
