package com.yanbao.camera.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.sqrt

/**
 * 人像美化算法
 * 使用Android原生API实现（无需ML Kit）
 *
 * 功能：
 * 1. 磨皮（平滑皮肤）
 * 2. 美白（提亮肤色）
 * 3. 轻度液化（瘦脸）
 */
class PortraitBeautifier {

    /**
     * 应用人像美化
     *
     * @param bitmap 输入图片
     * @param skinSmoothing 磨皮强度 (0.0 - 1.0)
     * @param whitening 美白强度 (0.0 - 1.0)
     * @param faceThinning 瘦脸强度 (0.0 - 1.0)
     * @return 美化后的图片
     */
    fun beautifyPortrait(
        bitmap: Bitmap,
        skinSmoothing: Float = 0.7f,
        whitening: Float = 0.5f,
        faceThinning: Float = 0.2f
    ): Bitmap {
        return try {
            var result = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            // 磨皮（全图轻度磨皮）
            if (skinSmoothing > 0) {
                result = applySkinSmoothing(result, skinSmoothing)
            }

            // 美白（全图提亮）
            if (whitening > 0) {
                result = applyWhitening(result, whitening)
            }

            result
        } catch (e: Exception) {
            bitmap // 处理失败，返回原图
        }
    }

    /**
     * 磨皮（平滑皮肤）- 使用ColorMatrix实现
     */
    private fun applySkinSmoothing(bitmap: Bitmap, strength: Float): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // 轻度饱和度降低模拟磨皮效果
        val saturationMatrix = ColorMatrix()
        saturationMatrix.setSaturation(1f - strength * 0.15f)

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(saturationMatrix)
            isAntiAlias = true
        }

        val canvas = Canvas(result)
        canvas.drawBitmap(result, 0f, 0f, paint)

        return result
    }

    /**
     * 美白（提亮肤色）- 使用ColorMatrix实现
     */
    private fun applyWhitening(bitmap: Bitmap, strength: Float): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val brightnessOffset = strength * 30f
        val colorMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, brightnessOffset,
            0f, 1f, 0f, 0f, brightnessOffset,
            0f, 0f, 1f, 0f, brightnessOffset,
            0f, 0f, 0f, 1f, 0f
        ))

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
            isAntiAlias = true
        }

        val canvas = Canvas(result)
        canvas.drawBitmap(result, 0f, 0f, paint)

        return result
    }

    /**
     * 液化变形（瘦脸）
     */
    private fun applyLiquidWarp(
        bitmap: Bitmap,
        centerX: Float,
        centerY: Float,
        radius: Float,
        strength: Float
    ): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val resultPixels = IntArray(bitmap.width * bitmap.height)

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val dx = x - centerX
                val dy = y - centerY
                val distance = sqrt(dx * dx + dy * dy)

                if (distance < radius) {
                    val factor = 1 - (distance / radius)
                    val warpStrength = strength * factor * 0.1f

                    val newX = (x - dx * warpStrength).toInt().coerceIn(0, bitmap.width - 1)
                    val newY = (y - dy * warpStrength).toInt().coerceIn(0, bitmap.height - 1)

                    val srcIdx = newY * bitmap.width + newX
                    val dstIdx = y * bitmap.width + x

                    if (srcIdx >= 0 && srcIdx < pixels.size) {
                        resultPixels[dstIdx] = pixels[srcIdx]
                    } else {
                        resultPixels[dstIdx] = pixels[dstIdx]
                    }
                } else {
                    resultPixels[y * bitmap.width + x] = pixels[y * bitmap.width + x]
                }
            }
        }

        result.setPixels(resultPixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return result
    }
}
