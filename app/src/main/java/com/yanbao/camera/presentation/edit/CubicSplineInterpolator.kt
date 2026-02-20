package com.yanbao.camera.presentation.edit

import android.graphics.PointF
import kotlin.math.pow

/**
 * 三次样条插值器
 * 
 * 用于生成平滑的 RGB 曲线映射表
 * 
 * 算法原理：
 * 1. 给定 n 个控制点 (x0, y0), (x1, y1), ..., (xn-1, yn-1)
 * 2. 在每两个相邻点之间构造一个三次多项式 S_i(x) = a_i + b_i*x + c_i*x^2 + d_i*x^3
 * 3. 确保在连接点处一阶导数和二阶导数连续
 */
class CubicSplineInterpolator(private val points: List<PointF>) {
    
    private val n = points.size
    private val a = FloatArray(n)
    private val b = FloatArray(n)
    private val c = FloatArray(n)
    private val d = FloatArray(n)
    
    init {
        require(points.size >= 2) { "至少需要 2 个控制点" }
        require(points.all { it.x in 0f..1f && it.y in 0f..1f }) { "控制点坐标必须在 [0, 1] 范围内" }
        
        // 按 x 坐标排序
        val sortedPoints = points.sortedBy { it.x }
        
        // 计算样条系数
        calculateSplineCoefficients(sortedPoints)
    }
    
    /**
     * 计算样条系数
     */
    private fun calculateSplineCoefficients(sortedPoints: List<PointF>) {
        val x = sortedPoints.map { it.x }.toFloatArray()
        val y = sortedPoints.map { it.y }.toFloatArray()
        
        // a_i = y_i
        for (i in 0 until n) {
            a[i] = y[i]
        }
        
        // 计算 h_i = x_{i+1} - x_i
        val h = FloatArray(n - 1)
        for (i in 0 until n - 1) {
            h[i] = x[i + 1] - x[i]
        }
        
        // 构造三对角矩阵求解 c_i（二阶导数）
        val alpha = FloatArray(n - 1)
        for (i in 1 until n - 1) {
            alpha[i] = (3f / h[i]) * (a[i + 1] - a[i]) - (3f / h[i - 1]) * (a[i] - a[i - 1])
        }
        
        // Thomas 算法求解三对角矩阵
        val l = FloatArray(n)
        val mu = FloatArray(n)
        val z = FloatArray(n)
        
        l[0] = 1f
        mu[0] = 0f
        z[0] = 0f
        
        for (i in 1 until n - 1) {
            l[i] = 2f * (x[i + 1] - x[i - 1]) - h[i - 1] * mu[i - 1]
            mu[i] = h[i] / l[i]
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i]
        }
        
        l[n - 1] = 1f
        z[n - 1] = 0f
        c[n - 1] = 0f
        
        // 回代求解 c, b, d
        for (j in n - 2 downTo 0) {
            c[j] = z[j] - mu[j] * c[j + 1]
            b[j] = (a[j + 1] - a[j]) / h[j] - h[j] * (c[j + 1] + 2f * c[j]) / 3f
            d[j] = (c[j + 1] - c[j]) / (3f * h[j])
        }
    }
    
    /**
     * 插值计算
     * 
     * @param x 输入值 (0.0 - 1.0)
     * @return 插值后的输出值 (0.0 - 1.0)
     */
    fun interpolate(x: Float): Float {
        if (x <= points.first().x) return points.first().y
        if (x >= points.last().x) return points.last().y
        
        // 找到 x 所在的区间 [x_i, x_{i+1}]
        val sortedPoints = points.sortedBy { it.x }
        var i = 0
        for (j in 0 until n - 1) {
            if (x >= sortedPoints[j].x && x <= sortedPoints[j + 1].x) {
                i = j
                break
            }
        }
        
        // 计算 dx = x - x_i
        val dx = x - sortedPoints[i].x
        
        // S_i(x) = a_i + b_i*dx + c_i*dx^2 + d_i*dx^3
        val result = a[i] + b[i] * dx + c[i] * dx.pow(2) + d[i] * dx.pow(3)
        
        // 限制在 [0, 1] 范围内
        return result.coerceIn(0f, 1f)
    }
    
    /**
     * 生成 256 阶 LUT (Look-Up Table)
     * 
     * @return ByteArray，长度为 256，每个元素表示映射后的值 (0-255)
     */
    fun generateLUT(): ByteArray {
        val lut = ByteArray(256)
        for (i in 0..255) {
            val input = i / 255f
            val output = interpolate(input)
            lut[i] = (output * 255).toInt().coerceIn(0, 255).toByte()
        }
        return lut
    }
}

/**
 * 生成默认的线性曲线控制点
 */
fun getDefaultCurvePoints(): List<PointF> {
    return listOf(
        PointF(0f, 0f),
        PointF(0.25f, 0.25f),
        PointF(0.5f, 0.5f),
        PointF(0.75f, 0.75f),
        PointF(1f, 1f)
    )
}
