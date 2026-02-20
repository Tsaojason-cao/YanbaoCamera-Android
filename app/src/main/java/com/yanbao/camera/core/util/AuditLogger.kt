package com.yanbao.camera.core.util

import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.SensorEvent
import android.util.Log

/**
 * 审计日志工具类
 * 
 * 用于记录所有关键操作的真实硬件数据，防止伪造
 * 
 * 审计要求：
 * 1. 所有参数调整必须记录硬件返回值
 * 2. 陀螺仪数据必须映射到 Shader Uniform
 * 3. 拍照时必须记录 TotalCaptureResult
 * 4. 所有日志使用 AUDIT_* 前缀
 */
object AuditLogger {
    
    private const val TAG_CAMERA = "AUDIT_CAMERA"
    private const val TAG_PARAMS = "AUDIT_PARAMS"
    private const val TAG_2D9 = "AUDIT_2.9D"
    private const val TAG_MEMORY = "AUDIT_MEMORY"
    private const val TAG_MODE = "AUDIT_MODE"
    
    /**
     * 记录相机打开事件
     */
    fun logCameraOpen(cameraId: String, facing: String) {
        Log.d(TAG_CAMERA, "=== CAMERA OPENED ===")
        Log.d(TAG_CAMERA, "Camera ID: $cameraId")
        Log.d(TAG_CAMERA, "Facing: $facing")
        Log.d(TAG_CAMERA, "Timestamp: ${System.currentTimeMillis()}")
    }
    
    /**
     * 记录相机关闭事件
     */
    fun logCameraClose(cameraId: String) {
        Log.d(TAG_CAMERA, "=== CAMERA CLOSED ===")
        Log.d(TAG_CAMERA, "Camera ID: $cameraId")
        Log.d(TAG_CAMERA, "Timestamp: ${System.currentTimeMillis()}")
    }
    
    /**
     * 记录参数调整（UI 滑块 → 硬件）
     * 
     * @param paramName 参数名称
     * @param uiValue UI 显示的值
     * @param hardwareValue 实际发送到硬件的值
     */
    fun logParameterAdjustment(paramName: String, uiValue: Any, hardwareValue: Any) {
        Log.d(TAG_PARAMS, "=== PARAMETER ADJUSTED ===")
        Log.d(TAG_PARAMS, "Parameter: $paramName")
        Log.d(TAG_PARAMS, "UI Value: $uiValue")
        Log.d(TAG_PARAMS, "Hardware Value: $hardwareValue")
        Log.d(TAG_PARAMS, "Timestamp: ${System.currentTimeMillis()}")
    }
    
    /**
     * 记录拍照完成事件（包含硬件返回值）
     * 
     * @param result TotalCaptureResult 硬件返回值
     */
    fun logCaptureCompleted(result: TotalCaptureResult) {
        Log.d(TAG_CAMERA, "=== CAPTURE COMPLETED ===")
        
        // 审计关键参数：ISO
        val iso = result.get(CaptureResult.SENSOR_SENSITIVITY)
        Log.d(TAG_CAMERA, "HARDWARE_ISO: $iso")
        
        // 审计关键参数：曝光时间
        val exposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
        Log.d(TAG_CAMERA, "HARDWARE_EXPOSURE_TIME: ${exposureTime}ns")
        
        // 审计关键参数：白平衡
        val awbMode = result.get(CaptureResult.CONTROL_AWB_MODE)
        Log.d(TAG_CAMERA, "HARDWARE_AWB_MODE: $awbMode")
        
        // 审计关键参数：对焦距离
        val focusDistance = result.get(CaptureResult.LENS_FOCUS_DISTANCE)
        Log.d(TAG_CAMERA, "HARDWARE_FOCUS_DISTANCE: $focusDistance")
        
        // 审计关键参数：闪光灯
        val flashMode = result.get(CaptureResult.FLASH_MODE)
        Log.d(TAG_CAMERA, "HARDWARE_FLASH_MODE: $flashMode")
        
        Log.d(TAG_CAMERA, "Timestamp: ${System.currentTimeMillis()}")
    }
    
    /**
     * 记录 2.9D 视差效果（陀螺仪 → Shader）
     * 
     * 审计要求：
     * - 如果手机静止时 offsetX/Y 依然在跳变，即为伪造
     * - 运动方向必须与数值变化方向一致
     * 
     * @param sensorEvent 陀螺仪传感器事件
     * @param offsetX Shader Uniform 变量 offset_x
     * @param offsetY Shader Uniform 变量 offset_y
     */
    fun logParallaxEffect(sensorEvent: SensorEvent, offsetX: Float, offsetY: Float) {
        val axisX = sensorEvent.values[0]
        val axisY = sensorEvent.values[1]
        val axisZ = sensorEvent.values[2]
        
        Log.d(TAG_2D9, "=== 2.9D PARALLAX EFFECT ===")
        Log.d(TAG_2D9, "GYRO_INPUT: [x: $axisX, y: $axisY, z: $axisZ]")
        Log.d(TAG_2D9, "SHADER_UNIFORM: [offset_x: $offsetX, offset_y: $offsetY]")
        Log.d(TAG_2D9, "Timestamp: ${sensorEvent.timestamp}")
        
        // 审计警告：如果陀螺仪值接近0但偏移量仍在变化
        if (Math.abs(axisX) < 0.01f && Math.abs(axisY) < 0.01f) {
            if (Math.abs(offsetX) > 0.1f || Math.abs(offsetY) > 0.1f) {
                Log.w(TAG_2D9, "⚠️ WARNING: Device is stationary but offset is changing!")
                Log.w(TAG_2D9, "⚠️ Possible FAKE parallax effect detected!")
            }
        }
    }
    
    /**
     * 记录雁宝记忆存储事件
     * 
     * @param imagePath 照片路径
     * @param params29DJson 29D 参数 JSON
     * @param latitude GPS 纬度
     * @param longitude GPS 经度
     */
    fun logMemorySaved(
        imagePath: String,
        params29DJson: String,
        latitude: Double,
        longitude: Double
    ) {
        Log.d(TAG_MEMORY, "=== YANBAO MEMORY SAVED ===")
        Log.d(TAG_MEMORY, "Image Path: $imagePath")
        Log.d(TAG_MEMORY, "29D Params JSON Length: ${params29DJson.length} chars")
        Log.d(TAG_MEMORY, "GPS: [$latitude, $longitude]")
        Log.d(TAG_MEMORY, "Timestamp: ${System.currentTimeMillis()}")
        
        // 审计警告：如果 29D 参数为空
        if (params29DJson.isEmpty() || params29DJson == "{}") {
            Log.w(TAG_MEMORY, "⚠️ WARNING: 29D params JSON is empty!")
        }
        
        // 审计警告：如果 GPS 坐标为默认值
        if (latitude == 0.0 && longitude == 0.0) {
            Log.w(TAG_MEMORY, "⚠️ WARNING: GPS coordinates are default (0, 0)!")
        }
    }
    
    /**
     * 记录模式切换事件
     * 
     * @param fromMode 切换前的模式
     * @param toMode 切换后的模式
     */
    fun logModeSwitch(fromMode: String, toMode: String) {
        Log.d(TAG_MODE, "=== MODE SWITCHED ===")
        Log.d(TAG_MODE, "From: $fromMode")
        Log.d(TAG_MODE, "To: $toMode")
        Log.d(TAG_MODE, "Timestamp: ${System.currentTimeMillis()}")
    }
    
    /**
     * 记录模式特定参数应用
     * 
     * @param mode 相机模式
     * @param params 应用的参数列表
     */
    fun logModeParametersApplied(mode: String, params: Map<String, Any>) {
        Log.d(TAG_MODE, "=== MODE PARAMETERS APPLIED ===")
        Log.d(TAG_MODE, "Mode: $mode")
        params.forEach { (key, value) ->
            Log.d(TAG_MODE, "  $key: $value")
        }
        Log.d(TAG_MODE, "Timestamp: ${System.currentTimeMillis()}")
    }
    
    /**
     * 记录错误事件
     */
    fun logError(category: String, message: String, exception: Throwable? = null) {
        Log.e("AUDIT_ERROR", "=== ERROR ===")
        Log.e("AUDIT_ERROR", "Category: $category")
        Log.e("AUDIT_ERROR", "Message: $message")
        if (exception != null) {
            Log.e("AUDIT_ERROR", "Exception: ${exception.message}", exception)
        }
        Log.e("AUDIT_ERROR", "Timestamp: ${System.currentTimeMillis()}")
    }
    
    /**
     * 记录录像开始
     */
    fun logRecordingStarted() {
        Log.d("AUDIT_VIDEO", "=== RECORDING STARTED ===")
        Log.d("AUDIT_VIDEO", "Timestamp: ${System.currentTimeMillis()}")
    }
    
    /**
     * 记录录像停止
     */
    fun logRecordingStopped(filePath: String) {
        Log.d("AUDIT_VIDEO", "=== RECORDING STOPPED ===")
        Log.d("AUDIT_VIDEO", "File Path: $filePath")
        Log.d("AUDIT_VIDEO", "Timestamp: ${System.currentTimeMillis()}")
    }
}
