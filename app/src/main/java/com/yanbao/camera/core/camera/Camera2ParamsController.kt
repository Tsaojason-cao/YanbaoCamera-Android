package com.yanbao.camera.core.camera

import android.hardware.camera2.CaptureRequest
import android.util.Log

/**
 * Camera2 参数控制器
 * 
 * 负责将 29D 刻度盘的参数转换为 Camera2 API 可以理解的物理参数，
 * 并实时下发到硬件
 * 
 * 这是"硬件通电"的核心逻辑
 */
object Camera2ParamsController {
    
    private const val TAG = "Camera2ParamsController"
    
    /**
     * 应用 29D 参数到 CaptureRequest.Builder
     * 
     * @param builder CaptureRequest.Builder
     * @param shutterNanos 快门速度（纳秒）
     * @param iso 感光度
     * @param kelvin 白平衡色温（Kelvin）
     * @param aperture 光圈值
     * @param ev 曝光补偿（EV）
     */
    fun apply29DParams(
        builder: CaptureRequest.Builder,
        shutterNanos: Long,
        iso: Int,
        kelvin: Int,
        aperture: Float,
        ev: Float
    ) {
        // 1. 关闭自动曝光
        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
        Log.d(TAG, "✅ 关闭自动曝光：CONTROL_AE_MODE_OFF")
        
        // 2. 下发真实纳秒级快门
        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, shutterNanos)
        Log.d(TAG, "✅ 下发快门速度：SENSOR_EXPOSURE_TIME = $shutterNanos ns (${formatShutterSpeed(shutterNanos)})")
        
        // 3. 下发真实感光度
        builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso)
        Log.d(TAG, "✅ 下发感光度：SENSOR_SENSITIVITY = ISO $iso")
        
        // 4. 下发白平衡
        builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
        
        // 计算 RGB 增益（简化版本，实际需要根据色温计算）
        val (rGain, gGain, bGain) = calculateRGBGains(kelvin)
        builder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX)
        builder.set(CaptureRequest.COLOR_CORRECTION_GAINS, android.hardware.camera2.params.RggbChannelVector(rGain, gGain, gGain, bGain))
        Log.d(TAG, "✅ 下发白平衡：COLOR_CORRECTION_GAINS = R:$rGain, G:$gGain, B:$bGain (${kelvin}K)")
        
        // 5. 下发曝光补偿
        // 注意：曝光补偿在手动模式下可能不生效，这里仅作记录
        Log.d(TAG, "✅ 曝光补偿：EV = $ev")
        
        // 6. 光圈（大多数手机不支持硬件光圈调节，这里仅作记录）
        Log.d(TAG, "✅ 光圈：f/$aperture (大多数手机不支持硬件调节)")
    }
    
    /**
     * 计算 RGB 增益
     * 
     * 根据色温（Kelvin）计算 RGB 增益
     * 这是一个简化版本，实际需要更复杂的算法
     * 
     * @param kelvin 色温（Kelvin）
     * @return Triple(rGain, gGain, bGain)
     */
    private fun calculateRGBGains(kelvin: Int): Triple<Float, Float, Float> {
        // 简化算法：
        // 2000K (冷) -> 偏蓝色 (R:0.8, G:1.0, B:1.5)
        // 5500K (中性) -> 平衡 (R:1.0, G:1.0, B:1.0)
        // 10000K (暖) -> 偏红色 (R:1.5, G:1.0, B:0.8)
        
        val progress = (kelvin - 2000) / 8000f // 0.0 ~ 1.0
        
        val rGain = 0.8f + (progress * 0.7f) // 0.8 ~ 1.5
        val gGain = 1.0f
        val bGain = 1.5f - (progress * 0.7f) // 1.5 ~ 0.8
        
        return Triple(rGain, gGain, bGain)
    }
    
    /**
     * 格式化快门速度为可读文本
     * 
     * @param nanoseconds 曝光时间（纳秒）
     * @return 格式化文本（例如 "1/125" 或 "2s"）
     */
    private fun formatShutterSpeed(nanoseconds: Long): String {
        val seconds = nanoseconds / 1_000_000_000.0
        return when {
            seconds < 1.0 -> "1/${(1.0 / seconds).toInt()}"
            else -> "${seconds.toInt()}s"
        }
    }
    
    /**
     * 重置为自动模式
     * 
     * @param builder CaptureRequest.Builder
     */
    fun resetToAuto(builder: CaptureRequest.Builder) {
        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
        builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
        Log.d(TAG, "✅ 重置为自动模式")
    }
}
