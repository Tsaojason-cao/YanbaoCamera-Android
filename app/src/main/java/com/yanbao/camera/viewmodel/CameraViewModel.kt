package com.yanbao.camera.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.model.CameraSettings
import com.yanbao.camera.model.FlashMode
import com.yanbao.camera.model.WhiteBalance
import com.yanbao.camera.model.FocusMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 相机ViewModel - 管理相机状态和操作
 */
class CameraViewModel : ViewModel() {

    private val _cameraSettings = MutableStateFlow(CameraSettings())
    val cameraSettings: StateFlow<CameraSettings> = _cameraSettings

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _isFocusing = MutableStateFlow(false)
    val isFocusing: StateFlow<Boolean> = _isFocusing

    /**
     * 切换前后摄像头
     */
    fun toggleCamera() {
        viewModelScope.launch {
            val current = _cameraSettings.value
            _cameraSettings.value = current.copy(isFrontCamera = !current.isFrontCamera)
        }
    }

    /**
     * 设置闪光灯模式
     */
    fun setFlashMode(mode: FlashMode) {
        viewModelScope.launch {
            val current = _cameraSettings.value
            _cameraSettings.value = current.copy(flashMode = mode)
        }
    }

    /**
     * 设置ISO
     */
    fun setISO(iso: Int) {
        viewModelScope.launch {
            val current = _cameraSettings.value
            _cameraSettings.value = current.copy(iso = iso.coerceIn(100, 6400))
        }
    }

    /**
     * 设置快门速度
     */
    fun setShutterSpeed(speed: Float) {
        viewModelScope.launch {
            val current = _cameraSettings.value
            _cameraSettings.value = current.copy(shutterSpeed = speed)
        }
    }

    /**
     * 设置白平衡
     */
    fun setWhiteBalance(wb: WhiteBalance) {
        viewModelScope.launch {
            val current = _cameraSettings.value
            _cameraSettings.value = current.copy(whiteBalance = wb)
        }
    }

    /**
     * 设置曝光补偿
     */
    fun setExposureCompensation(ev: Float) {
        viewModelScope.launch {
            val current = _cameraSettings.value
            _cameraSettings.value = current.copy(exposureCompensation = ev.coerceIn(-3f, 3f))
        }
    }

    /**
     * 设置对焦模式
     */
    fun setFocusMode(mode: FocusMode) {
        viewModelScope.launch {
            val current = _cameraSettings.value
            _cameraSettings.value = current.copy(focusMode = mode)
        }
    }

    /**
     * 开始对焦
     */
    fun startFocus() {
        viewModelScope.launch {
            _isFocusing.value = true
            // 模拟对焦延迟
            kotlinx.coroutines.delay(500)
            _isFocusing.value = false
        }
    }

    /**
     * 拍照
     */
    fun takePhoto() {
        viewModelScope.launch {
            // TODO: 实现拍照逻辑
        }
    }

    /**
     * 开始/停止录视频
     */
    fun toggleRecording() {
        viewModelScope.launch {
            _isRecording.value = !_isRecording.value
        }
    }

    /**
     * 重置相机设置
     */
    fun resetSettings() {
        viewModelScope.launch {
            _cameraSettings.value = CameraSettings()
        }
    }
}
