package com.yanbao.camera.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 水平对齐修饰符 - 用于Row中的元素
 */
fun Modifier.align(alignment: Alignment.Vertical): Modifier {
    return this.then(
        object : LayoutModifier {
            override fun MeasureScope.measure(
                measurable: Measurable,
                constraints: Constraints
            ): MeasureResult {
                val placeable = measurable.measure(constraints)
                return layout(placeable.width, placeable.height) {
                    when (alignment) {
                        Alignment.Top -> placeable.place(0, 0)
                        Alignment.CenterVertically -> placeable.place(0, (height - placeable.height) / 2)
                        Alignment.Bottom -> placeable.place(0, height - placeable.height)
                        else -> placeable.place(0, 0)
                    }
                }
            }
        }
    )
}

/**
 * 权重修饰符 - 用于Row/Column中的元素
 */
fun Modifier.weight(weight: Float, fill: Boolean = true): Modifier {
    return this.then(
        object : LayoutModifier {
            override fun MeasureScope.measure(
                measurable: Measurable,
                constraints: Constraints
            ): MeasureResult {
                val placeable = if (fill) {
                    measurable.measure(constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity))
                } else {
                    measurable.measure(constraints)
                }
                return layout(placeable.width, placeable.height) {
                    placeable.place(0, 0)
                }
            }
        }
    )
}

/**
 * 填充修饰符 - 用于Row中的元素
 */
fun RowScope.Modifier.fillMaxWidth(fraction: Float = 1f): Modifier {
    return this.weight(fraction)
}

/**
 * 填充修饰符 - 用于Column中的元素
 */
fun ColumnScope.Modifier.fillMaxHeight(fraction: Float = 1f): Modifier {
    return this.weight(fraction)
}
