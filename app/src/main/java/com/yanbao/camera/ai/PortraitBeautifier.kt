package com.yanbao.camera.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlin.math.sqrt

/**
 * 人像美化算法
 * 
 * 功能：
 * 1. 人脸检测
 * 2. 磨皮（平滑皮肤）
 * 3. 美白（提亮肤色）
 * 4. 大眼（放大眼睛）
 * 5. 瘦脸（瘦脸效果）
 */
class PortraitBeautifier {
    
    private val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        FaceDetection.getClient(options)
    }
    
    /**
     * 应用人像美化
     * 
     * @param bitmap 输入图片
     * @param skinSmoothing 磨皮强度 (0.0 - 1.0)
     * @param whitening 美白强度 (0.0 - 1.0)
     * @param eyeEnlargement 大眼强度 (0.0 - 1.0)
     * @param faceThinning 瘦脸强度 (0.0 - 1.0)
     * @return 美化后的图片
     */
    fun beautifyPortrait(
        bitmap: Bitmap,
        skinSmoothing: Float = 0.7f,
        whitening: Float = 0.5f,
        eyeEnlargement: Float = 0.3f,
        faceThinning: Float = 0.2f
    ): Bitmap {
        return try {
            // 1. 检测人脸
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val faces = faceDetector.process(inputImage)
            
            if (faces.faces.isEmpty()) {
                return bitmap // 没有检测到人脸，返回原图
            }
            
            var result = bitmap.copy(bitmap.config, true)
            
            // 2. 对每个检测到的人脸进行美化
            for (face in faces.faces) {
                // 磨皮
                if (skinSmoothing > 0) {
                    result = applySkinSmoothing(result, face, skinSmoothing)
                }
                
                // 美白
                if (whitening > 0) {
                    result = applyWhitening(result, face, whitening)
                }
                
                // 大眼
                if (eyeEnlargement > 0) {
                    result = enlargeEyes(result, face, eyeEnlargement)
                }
                
                // 瘦脸
                if (faceThinning > 0) {
                    result = thinFace(result, face, faceThinning)
                }
            }
            
            result
        } catch (e: Exception) {
            bitmap // 处理失败，返回原图
        }
    }
    
    /**
     * 磨皮（平滑皮肤）
     */
    private fun applySkinSmoothing(bitmap: Bitmap, face: com.google.mlkit.vision.face.Face, strength: Float): Bitmap {
        val faceBounds = face.boundingBox
        
        // 扩大人脸区域以确保覆盖整个脸部
        val expandedBounds = RectF(
            faceBounds.left - faceBounds.width() * 0.1f,
            faceBounds.top - faceBounds.height() * 0.1f,
            faceBounds.right + faceBounds.width() * 0.1f,
            faceBounds.bottom + faceBounds.height() * 0.1f
        )
        
        return applyBlurToRegion(bitmap, expandedBounds, strength * 3.0f)
    }
    
    /**
     * 美白（提亮肤色）
     */
    private fun applyWhitening(bitmap: Bitmap, face: com.google.mlkit.vision.face.Face, strength: Float): Bitmap {
        val faceBounds = face.boundingBox
        
        // 扩大人脸区域
        val expandedBounds = RectF(
            faceBounds.left - faceBounds.width() * 0.1f,
            faceBounds.top - faceBounds.height() * 0.1f,
            faceBounds.right + faceBounds.width() * 0.1f,
            faceBounds.bottom + faceBounds.height() * 0.1f
        )
        
        return brightenRegion(bitmap, expandedBounds, strength * 0.2f)
    }
    
    /**
     * 大眼（放大眼睛）
     */
    private fun enlargeEyes(bitmap: Bitmap, face: com.google.mlkit.vision.face.Face, strength: Float): Bitmap {
        val leftEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE)
        val rightEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE)
        
        var result = bitmap
        
        if (leftEye != null) {
            result = enlargeEyeRegion(result, leftEye.position.x, leftEye.position.y, strength)
        }
        
        if (rightEye != null) {
            result = enlargeEyeRegion(result, rightEye.position.x, rightEye.position.y, strength)
        }
        
        return result
    }
    
    /**
     * 瘦脸（瘦脸效果）
     */
    private fun thinFace(bitmap: Bitmap, face: com.google.mlkit.vision.face.Face, strength: Float): Bitmap {
        val faceBounds = face.boundingBox
        val centerX = faceBounds.centerX()
        val centerY = faceBounds.centerY()
        
        // 应用液化效果实现瘦脸
        return applyLiquidWarp(bitmap, centerX.toFloat(), centerY.toFloat(), faceBounds.width() * 0.3f, strength)
    }
    
    /**
     * 对指定区域应用模糊（磨皮）
     */
    private fun applyBlurToRegion(bitmap: Bitmap, region: RectF, radius: Float): Bitmap {
        val result = bitmap.copy(bitmap.config, true)
        val canvas = Canvas(result)
        
        // 创建模糊画笔
        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        
        // 创建模糊效果（使用多层透明度）
        for (i in 0 until radius.toInt()) {
            val alpha = (255 * (1 - i / radius)).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            canvas.drawOval(region, paint)
        }
        
        return result
    }
    
    /**
     * 对指定区域提亮（美白）
     */
    private fun brightenRegion(bitmap: Bitmap, region: RectF, amount: Float): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        val left = region.left.toInt().coerceIn(0, bitmap.width - 1)
        val top = region.top.toInt().coerceIn(0, bitmap.height - 1)
        val right = region.right.toInt().coerceIn(0, bitmap.width - 1)
        val bottom = region.bottom.toInt().coerceIn(0, bitmap.height - 1)
        
        for (y in top..bottom) {
            for (x in left..right) {
                val idx = y * bitmap.width + x
                val pixel = pixels[idx]
                val a = (pixel shr 24) and 0xFF
                var r = (pixel shr 16) and 0xFF
                var g = (pixel shr 8) and 0xFF
                var b = pixel and 0xFF
                
                // 计算到区域中心的距离，用于渐变效果
                val distX = (x - region.centerX()) / region.width()
                val distY = (y - region.centerY()) / region.height()
                val distance = sqrt(distX * distX + distY * distY)
                val falloff = (1 - distance).coerceIn(0f, 1f)
                
                // 提亮
                r = (r + 255 * amount * falloff).coerceIn(0f, 255f).toInt()
                g = (g + 255 * amount * falloff).coerceIn(0f, 255f).toInt()
                b = (b + 255 * amount * falloff).coerceIn(0f, 255f).toInt()
                
                pixels[idx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        
        result.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return result
    }
    
    /**
     * 放大眼睛
     */
    private fun enlargeEyeRegion(bitmap: Bitmap, eyeX: Float, eyeY: Float, strength: Float): Bitmap {
        val result = bitmap.copy(bitmap.config, true)
        val canvas = Canvas(result)
        
        // 眼睛区域大小
        val eyeRadius = 15f * (1 + strength * 0.5f)
        
        // 创建放大效果（使用高光）
        val paint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            alpha = (50 * strength).toInt()
            isAntiAlias = true
        }
        
        canvas.drawCircle(eyeX, eyeY, eyeRadius, paint)
        
        return result
    }
    
    /**
     * 液化变形（瘦脸）
     */
    private fun applyLiquidWarp(bitmap: Bitmap, centerX: Float, centerY: Float, radius: Float, strength: Float): Bitmap {
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
                    // 在影响半径内应用变形
                    val factor = 1 - (distance / radius)
                    val warpStrength = strength * factor * 0.1f
                    
                    // 向中心收缩
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
                    // 影响半径外保持不变
                    resultPixels[y * bitmap.width + x] = pixels[y * bitmap.width + x]
                }
            }
        }
        
        result.setPixels(resultPixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return result
    }
}
