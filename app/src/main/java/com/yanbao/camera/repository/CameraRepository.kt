package com.yanbao.camera.repository

import com.yanbao.camera.model.CameraMode
import com.yanbao.camera.model.CameraParameters
import com.yanbao.camera.model.FocusMode
import com.yanbao.camera.model.WhiteBalance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 相机数据仓库
 * 
 * 管理相机参数、模式和状态
 */
class CameraRepository {
    
    private val _cameraMode = MutableStateFlow(CameraMode.AUTO)
    val cameraMode: StateFlow<CameraMode> = _cameraMode.asStateFlow()
    
    private val _cameraParameters = MutableStateFlow(CameraParameters())
    val cameraParameters: StateFlow<CameraParameters> = _cameraParameters.asStateFlow()
    
    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn.asStateFlow()
    
    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()
    
    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()
    
    /**
     * 设置相机模式
     */
    fun setCameraMode(mode: CameraMode) {
        _cameraMode.value = mode
    }
    
    /**
     * 获取当前相机模式
     */
    fun getCurrentMode(): CameraMode = _cameraMode.value
    
    /**
     * 设置ISO值
     */
    fun setISO(iso: Int) {
        val clampedISO = iso.coerceIn(100, 6400)
        _cameraParameters.value = _cameraParameters.value.copy(iso = clampedISO)
    }
    
    /**
     * 设置白平衡
     */
    fun setWhiteBalance(whiteBalance: WhiteBalance) {
        _cameraParameters.value = _cameraParameters.value.copy(whiteBalance = whiteBalance)
    }
    
    /**
     * 设置曝光补偿
     */
    fun setExposureCompensation(compensation: Float) {
        val clamped = compensation.coerceIn(-3f, 3f)
        _cameraParameters.value = _cameraParameters.value.copy(exposureCompensation = clamped)
    }
    
    /**
     * 设置对焦模式
     */
    fun setFocusMode(focusMode: FocusMode) {
        _cameraParameters.value = _cameraParameters.value.copy(focusMode = focusMode)
    }
    
    /**
     * 设置缩放级别
     */
    fun setZoomLevel(zoom: Float) {
        val clamped = zoom.coerceIn(1.0f, 10.0f)
        _zoomLevel.value = clamped
        _cameraParameters.value = _cameraParameters.value.copy(zoomLevel = clamped)
    }
    
    /**
     * 切换闪光灯
     */
    fun toggleFlash() {
        _isFlashOn.value = !_isFlashOn.value
    }
    
    /**
     * 切换前后摄像头
     */
    fun toggleCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
    }
    
    /**
     * 获取当前相机参数
     */
    fun getCurrentParameters(): CameraParameters = _cameraParameters.value
    
    /**
     * 重置相机参数
     */
    fun resetParameters() {
        _cameraParameters.value = CameraParameters()
        _zoomLevel.value = 1.0f
        _isFlashOn.value = false
    }
}
