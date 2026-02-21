package com.yanbao.camera.presentation.camera

import android.app.Application
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.render.Camera2GLRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 专业相机 ViewModel
 * 
 * 功能：
 * 1. 管理 Camera2 + OpenGL 渲染管线
 * 2. 提供专业模式参数控制（ISO、曝光、白平衡）
 * 3. 同步参数到 Camera2 硬件和 GLRenderer
 * 
 * 工业级特性：
 * - 使用 StateFlow 管理参数状态
 * - 参数变化时同时更新硬件和渲染器
 * - 记录 AUDIT_PARAMS 日志
 */
class ProCameraViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "ProCameraViewModel"
    }
    
    // Camera2 + OpenGL 渲染器
    private var glRenderer: Camera2GLRenderer? = null
    private var surfaceTexture: SurfaceTexture? = null
    
    // 专业模式参数
    private val _iso = MutableStateFlow(100)
    val iso: StateFlow<Int> = _iso.asStateFlow()
    
    private val _exposureTime = MutableStateFlow(10000000L) // 10ms
    val exposureTime: StateFlow<Long> = _exposureTime.asStateFlow()
    
    private val _whiteBalance = MutableStateFlow(5000) // 5000K
    val whiteBalance: StateFlow<Int> = _whiteBalance.asStateFlow()
    
    // 29D 参数
    private val _brightness = MutableStateFlow(0.0f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()
    
    private val _contrast = MutableStateFlow(1.0f)
    val contrast: StateFlow<Float> = _contrast.asStateFlow()
    
    private val _saturation = MutableStateFlow(1.0f)
    val saturation: StateFlow<Float> = _saturation.asStateFlow()
    
    // RGB 曲线 LUT
    private val _lutData = MutableStateFlow<ByteArray?>(null)
    val lutData: StateFlow<ByteArray?> = _lutData.asStateFlow()
    
    /**
     * 初始化 GLRenderer
     */
    fun initGLRenderer(renderer: Camera2GLRenderer) {
        glRenderer = renderer
        Log.d(TAG, "GLRenderer initialized")
    }
    
    /**
     * 设置 SurfaceTexture（由 GLRenderer 创建）
     */
    fun setSurfaceTexture(surfaceTexture: SurfaceTexture) {
        this.surfaceTexture = surfaceTexture
        Log.d(TAG, "SurfaceTexture set")
    }
    
    /**
     * 更新 ISO
     */
    fun updateISO(value: Int) {
        viewModelScope.launch {
            _iso.value = value
            
            // 同步到 GLRenderer
            glRenderer?.updateProParams(value, _exposureTime.value, _whiteBalance.value)
            
            Log.d(TAG, "AUDIT_PARAMS: ISO updated to $value")
        }
    }
    
    /**
     * 更新曝光时间
     */
    fun updateExposureTime(value: Long) {
        viewModelScope.launch {
            _exposureTime.value = value
            
            // 同步到 GLRenderer
            glRenderer?.updateProParams(_iso.value, value, _whiteBalance.value)
            
            Log.d(TAG, "AUDIT_PARAMS: ExposureTime updated to $value")
        }
    }
    
    /**
     * 更新白平衡
     */
    fun updateWhiteBalance(value: Int) {
        viewModelScope.launch {
            _whiteBalance.value = value
            
            // 同步到 GLRenderer
            glRenderer?.updateProParams(_iso.value, _exposureTime.value, value)
            
            Log.d(TAG, "AUDIT_PARAMS: WhiteBalance updated to $value")
        }
    }
    
    /**
     * 更新 29D 参数
     */
    fun update29DParams(
        brightness: Float? = null,
        contrast: Float? = null,
        saturation: Float? = null
    ) {
        viewModelScope.launch {
            brightness?.let { _brightness.value = it }
            contrast?.let { _contrast.value = it }
            saturation?.let { _saturation.value = it }
            
            // 同步到 GLRenderer
            glRenderer?.update29DParams(
                brightness = _brightness.value,
                contrast = _contrast.value,
                saturation = _saturation.value
            )
            
            Log.d(TAG, "AUDIT_PARAMS: 29D params updated")
        }
    }
    
    /**
     * 更新 RGB 曲线 LUT
     */
    fun updateLUT(lutData: ByteArray) {
        viewModelScope.launch {
            _lutData.value = lutData
            
            // 同步到 GLRenderer
            glRenderer?.updateLutTexture(lutData)
            
            Log.d(TAG, "AUDIT_PARAMS: LUT updated, size=${lutData.size}")
        }
    }
    
    /**
     * 获取 Camera2 硬件参数范围
     */
    fun getCameraHardwareRanges(): CameraHardwareRanges {
        val cameraManager = getApplication<Application>().getSystemService(CameraManager::class.java)
        val cameraId = cameraManager.cameraIdList[0]
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        
        val isoRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        val exposureTimeRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        
        return CameraHardwareRanges(
            isoMin = isoRange?.lower ?: 100,
            isoMax = isoRange?.upper ?: 6400,
            exposureTimeMin = exposureTimeRange?.lower ?: 125000L,
            exposureTimeMax = exposureTimeRange?.upper ?: 30000000000L
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        glRenderer?.release()
        surfaceTexture?.release()
        Log.d(TAG, "ViewModel cleared, resources released")
    }
}

/**
 * Camera2 硬件参数范围
 */
data class CameraHardwareRanges(
    val isoMin: Int,
    val isoMax: Int,
    val exposureTimeMin: Long,
    val exposureTimeMax: Long
)
