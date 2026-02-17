package com.yanbao.camera.ai

import android.graphics.Bitmap
import kotlin.math.pow

/**
 * 夜景增强算法
 * 
 * 功能：
 * 1. 检测光线条件
 * 2. 提升亮度
 * 3. 降低噪声
 * 4. 增强细节
 * 5. 调整色温（偏暖）
 */
class NightModeEnhancer {
    
    /**
     * 应用夜景增强
     * 
     * @param bitmap 输入图片
     * @param strength 增强强度 (0.0 - 2.0)，1.0为标准强度
     * @return 增强后的图片
     */
    fun enhanceNightMode(bitmap: Bitmap, strength: Float = 1.0f): Bitmap {
        // 1. 亮度提升
        val brightened = increaseBrightness(bitmap, strength * 0.4f)
        
        // 2. 噪声降低（使用中值滤波）
        val denoised = denoise(brightened)
        
        // 3. 细节增强（使用锐化）
        val enhanced = enhanceDetails(denoised, strength * 0.3f)
        
        // 4. 色温调整（偏暖）
        val colorCorrected = adjustColorTemperature(enhanced, 1200f)
        
        // 5. 对比度调整
        val contrastAdjusted = adjustContrast(colorCorrected, 1.1f)
        
        return contrastAdjusted
    }
    
    /**
     * 提升亮度
     */
    private fun increaseBrightness(bitmap: Bitmap, amount: Float): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = (pixel shr 24) and 0xFF
            var r = (pixel shr 16) and 0xFF
            var g = (pixel shr 8) and 0xFF
            var b = pixel and 0xFF
            
            // 使用指数函数提升亮度，暗部提升更多
            r = (r + (255 * amount * (1 - r / 255f))).coerceIn(0f, 255f).toInt()
            g = (g + (255 * amount * (1 - g / 255f))).coerceIn(0f, 255f).toInt()
            b = (b + (255 * amount * (1 - b / 255f))).coerceIn(0f, 255f).toInt()
            
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
        
        result.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return result
    }
    
    /**
     * 降低噪声（中值滤波）
     */
    private fun denoise(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val resultPixels = IntArray(width * height)
        
        // 3x3 中值滤波
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val neighbors = mutableListOf<Int>()
                
                // 收集周围9个像素
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val idx = (y + dy) * width + (x + dx)
                        neighbors.add(pixels[idx])
                    }
                }
                
                // 计算中值
                val medianR = getMedianChannel(neighbors, 16)
                val medianG = getMedianChannel(neighbors, 8)
                val medianB = getMedianChannel(neighbors, 0)
                val medianA = getMedianChannel(neighbors, 24)
                
                val idx = y * width + x
                resultPixels[idx] = (medianA shl 24) or (medianR shl 16) or (medianG shl 8) or medianB
            }
        }
        
        // 边界像素保持不变
        for (i in pixels.indices) {
            if (i < width || i >= width * (height - 1) || i % width == 0 || i % width == width - 1) {
                resultPixels[i] = pixels[i]
            }
        }
        
        result.setPixels(resultPixels, 0, width, 0, 0, width, height)
        return result
    }
    
    /**
     * 获取通道的中值
     */
    private fun getMedianChannel(pixels: List<Int>, shift: Int): Int {
        val values = pixels.map { (it shr shift) and 0xFF }.sorted()
        return values[values.size / 2]
    }
    
    /**
     * 增强细节（锐化）
     */
    private fun enhanceDetails(bitmap: Bitmap, strength: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val resultPixels = IntArray(width * height)
        
        // 锐化卷积核
        val kernel = arrayOf(
            intArrayOf(0, -1, 0),
            intArrayOf(-1, 5, -1),
            intArrayOf(0, -1, 0)
        )
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var r = 0
                var g = 0
                var b = 0
                
                // 应用卷积核
                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val idx = (y + ky) * width + (x + kx)
                        val pixel = pixels[idx]
                        val weight = kernel[ky + 1][kx + 1]
                        
                        r += ((pixel shr 16) and 0xFF) * weight
                        g += ((pixel shr 8) and 0xFF) * weight
                        b += (pixel and 0xFF) * weight
                    }
                }
                
                // 应用强度系数
                r = (r * strength).coerceIn(0f, 255f).toInt()
                g = (g * strength).coerceIn(0f, 255f).toInt()
                b = (b * strength).coerceIn(0f, 255f).toInt()
                
                val idx = y * width + x
                val a = (pixels[idx] shr 24) and 0xFF
                resultPixels[idx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        
        // 边界像素保持不变
        for (i in pixels.indices) {
            if (i < width || i >= width * (height - 1) || i % width == 0 || i % width == width - 1) {
                resultPixels[i] = pixels[i]
            }
        }
        
        result.setPixels(resultPixels, 0, width, 0, 0, width, height)
        return result
    }
    
    /**
     * 调整色温（偏暖）
     */
    private fun adjustColorTemperature(bitmap: Bitmap, kelvin: Float): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        // 计算色温调整系数
        val tempFactor = kelvin / 6500f  // 标准色温为6500K
        val rFactor = 1.0f + (tempFactor - 1.0f) * 0.3f  // 红色增加
        val bFactor = 1.0f - (tempFactor - 1.0f) * 0.2f  // 蓝色减少
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = (pixel shr 24) and 0xFF
            var r = (pixel shr 16) and 0xFF
            var g = (pixel shr 8) and 0xFF
            var b = pixel and 0xFF
            
            // 应用色温调整
            r = (r * rFactor).coerceIn(0f, 255f).toInt()
            b = (b * bFactor).coerceIn(0f, 255f).toInt()
            
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
        
        result.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return result
    }
    
    /**
     * 调整对比度
     */
    private fun adjustContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        val factor = (259f * (contrast + 255f)) / (255f * (259f - contrast))
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = (pixel shr 24) and 0xFF
            var r = (pixel shr 16) and 0xFF
            var g = (pixel shr 8) and 0xFF
            var b = pixel and 0xFF
            
            // 应用对比度调整
            r = (factor * (r - 128f) + 128f).coerceIn(0f, 255f).toInt()
            g = (factor * (g - 128f) + 128f).coerceIn(0f, 255f).toInt()
            b = (factor * (b - 128f) + 128f).coerceIn(0f, 255f).toInt()
            
            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
        
        result.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return result
    }
}
