package com.yanbao.camera.beauty

import android.graphics.Bitmap
import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Phase 2: 美颜人脸检测器
 *
 * 封装 ML Kit Face Detection，提供：
 * - 人脸边界框
 * - 关键点（眼睛、鼻子、嘴巴、耳朵）
 * - 欧拉角（头部姿态）
 * - 笑脸/眼睛睁开概率
 *
 * 检测结果用于 [BeautyRenderer] 的精准美颜（瘦脸、大眼等）。
 */
class FaceDetector {

    companion object {
        private const val TAG = "FaceDetector"
    }

    /** ML Kit 人脸检测器（快速模式 + 全关键点） */
    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
    )

    // ─── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 检测 Bitmap 中的所有人脸（挂起函数，在协程中调用）
     * @return 检测到的人脸列表，失败返回空列表
     */
    suspend fun detectFaces(bitmap: Bitmap): List<FaceResult> =
        suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)
            detector.process(image)
                .addOnSuccessListener { faces ->
                    val results = faces.map { face -> face.toFaceResult() }
                    Log.d(TAG, "Detected ${results.size} faces")
                    cont.resume(results)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Face detection failed: ${e.message}")
                    cont.resume(emptyList())
                }
        }

    /**
     * 释放检测器资源
     */
    fun close() {
        detector.close()
    }

    // ─── 私有扩展 ─────────────────────────────────────────────────────────

    private fun Face.toFaceResult(): FaceResult {
        val landmarks = mapOf(
            "leftEye"    to getLandmark(FaceLandmark.LEFT_EYE)?.position,
            "rightEye"   to getLandmark(FaceLandmark.RIGHT_EYE)?.position,
            "noseTip"    to getLandmark(FaceLandmark.NOSE_BASE)?.position,
            "mouthLeft"  to getLandmark(FaceLandmark.MOUTH_LEFT)?.position,
            "mouthRight" to getLandmark(FaceLandmark.MOUTH_RIGHT)?.position,
            "mouthBottom" to getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position,
            "leftEar"    to getLandmark(FaceLandmark.LEFT_EAR)?.position,
            "rightEar"   to getLandmark(FaceLandmark.RIGHT_EAR)?.position,
            "leftCheek"  to getLandmark(FaceLandmark.LEFT_CHEEK)?.position,
            "rightCheek" to getLandmark(FaceLandmark.RIGHT_CHEEK)?.position
        ).filterValues { it != null }.mapValues { it.value!! }

        return FaceResult(
            trackingId = trackingId,
            boundingBox = android.graphics.RectF(
                boundingBox.left.toFloat(),
                boundingBox.top.toFloat(),
                boundingBox.right.toFloat(),
                boundingBox.bottom.toFloat()
            ),
            landmarks = landmarks,
            headEulerAngleX = headEulerAngleX,
            headEulerAngleY = headEulerAngleY,
            headEulerAngleZ = headEulerAngleZ,
            smilingProbability = smilingProbability ?: 0f,
            leftEyeOpenProbability = leftEyeOpenProbability ?: 1f,
            rightEyeOpenProbability = rightEyeOpenProbability ?: 1f
        )
    }
}

/**
 * 人脸检测结果数据类
 */
data class FaceResult(
    /** ML Kit 追踪 ID */
    val trackingId: Int?,

    /** 人脸边界框（像素坐标） */
    val boundingBox: android.graphics.RectF,

    /** 关键点坐标（像素坐标） */
    val landmarks: Map<String, PointF>,

    /** 头部俯仰角（X轴旋转，正值=抬头） */
    val headEulerAngleX: Float,

    /** 头部偏航角（Y轴旋转，正值=向右转） */
    val headEulerAngleY: Float,

    /** 头部滚转角（Z轴旋转，正值=向右倾） */
    val headEulerAngleZ: Float,

    /** 笑脸概率 0-1 */
    val smilingProbability: Float,

    /** 左眼睁开概率 0-1 */
    val leftEyeOpenProbability: Float,

    /** 右眼睁开概率 0-1 */
    val rightEyeOpenProbability: Float
) {
    /** 人脸中心点 */
    val center: PointF get() = PointF(boundingBox.centerX(), boundingBox.centerY())

    /** 人脸宽度（像素） */
    val width: Float get() = boundingBox.width()

    /** 人脸高度（像素） */
    val height: Float get() = boundingBox.height()

    /** 是否正面朝向（偏航角 < 30°） */
    val isFrontal: Boolean get() = kotlin.math.abs(headEulerAngleY) < 30f
}
