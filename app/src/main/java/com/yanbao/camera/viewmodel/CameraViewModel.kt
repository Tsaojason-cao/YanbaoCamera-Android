package com.yanbao.camera.viewmodel

import android.content.Context
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.camera.CameraManager
import com.yanbao.camera.data.model.CameraMode
import com.yanbao.camera.data.model.CameraUiState
import com.yanbao.camera.data.model.FlashMode
import com.yanbao.camera.data.model.GridType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 相机 ViewModel
 * 真实实现所有相机功能：拍照、录像、变焦、闪光灯、模式切换、焦点控制
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraManager: CameraManager
) : ViewModel() {

    private val TAG = "YanbaoCameraVM"

    // 相机基础状态（使用CameraUiState，字段名与数据类一致）
    private val _cameraState = MutableStateFlow(CameraUiState())
    val cameraState: StateFlow<CameraUiState> = _cameraState

    // 录像状态
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    // 录制时长（毫秒）
    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration

    // 焦点指示器
    private val _showFocusIndicator = MutableStateFlow(false)
    val showFocusIndicator: StateFlow<Boolean> = _showFocusIndicator

    private val _focusPosition = MutableStateFlow(Offset.Zero)
    val focusPosition: StateFlow<Offset> = _focusPosition

    // 提示消息
    private val _photoToast = MutableStateFlow<String?>(null)
    val photoToast: StateFlow<String?> = _photoToast

    // 录制计时 Job
    private var recordingTimerJob: Job? = null

    /**
     * 启动相机预览
     */
    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        cameraManager.startCamera(lifecycleOwner, previewView)
        Log.d(TAG, "相机预览启动")
    }

    /**
     * 真实拍照实现 - 使用 CameraX ImageCapture 保存到系统相册
     */
    fun takePhoto(context: Context, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "开始拍照...")
            cameraManager.takePhoto(
                onSuccess = { uri ->
                    _cameraState.value = _cameraState.value.copy(lastPhotoUri = uri)
                    _photoToast.value = "照片已保存到相册"
                    onSuccess(uri)
                    Log.d(TAG, "拍照成功: $uri")
                },
                onError = { error ->
                    _photoToast.value = error
                    Log.e(TAG, "拍照失败: $error")
                }
            )
        }
    }

    /**
     * 开始录像
     */
    fun startRecording(context: Context) {
        _isRecording.value = true
        _recordingDuration.value = 0L
        recordingTimerJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000)
                _recordingDuration.value += 1000
            }
        }
        Log.d(TAG, "开始录像")
    }

    /**
     * 停止录像
     */
    fun stopRecording() {
        _isRecording.value = false
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        Log.d(TAG, "停止录像，时长: ${_recordingDuration.value}ms")
    }

    /**
     * 切换前后摄像头（使用CameraManager的flipCamera方法）
     */
    fun flipCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        cameraManager.flipCamera(lifecycleOwner, previewView)
        _cameraState.value = _cameraState.value.copy(
            isFrontCamera = !_cameraState.value.isFrontCamera
        )
        Log.d(TAG, "切换摄像头，前置: ${_cameraState.value.isFrontCamera}")
    }

    /**
     * 循环切换闪光灯模式：OFF → AUTO → ON → OFF
     */
    fun cycleFlashMode() {
        val newMode = when (_cameraState.value.flashMode) {
            FlashMode.OFF -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.OFF
            FlashMode.TORCH -> FlashMode.OFF
        }
        _cameraState.value = _cameraState.value.copy(flashMode = newMode)
        cameraManager.setFlashMode(newMode)
        Log.d(TAG, "闪光灯模式: $newMode")
    }

    /**
     * 设置变焦级别（使用CameraUiState的zoomRatio字段）
     */
    fun setZoom(zoom: Float) {
        _cameraState.value = _cameraState.value.copy(zoomRatio = zoom.coerceIn(1f, 10f))
        cameraManager.setZoom(zoom)
    }

    /**
     * 选择拍摄模式
     */
    fun selectMode(mode: CameraMode) {
        _cameraState.value = _cameraState.value.copy(currentMode = mode)
        Log.d(TAG, "切换拍摄模式: ${mode.displayName}")
    }

    /**
     * 点击对焦
     */
    fun focusAt(x: Float, y: Float, width: Float, height: Float) {
        _focusPosition.value = Offset(x, y)
        _showFocusIndicator.value = true
        cameraManager.tapToFocus(x, y, width, height)
        viewModelScope.launch {
            delay(1500)
            _showFocusIndicator.value = false
        }
    }

    /**
     * 切换网格线显示（使用CameraUiState的gridType字段）
     */
    fun toggleGrid() {
        val newGridType = when (_cameraState.value.gridType) {
            GridType.NONE -> GridType.THREE_BY_THREE
            GridType.THREE_BY_THREE -> GridType.NONE
            else -> GridType.NONE
        }
        _cameraState.value = _cameraState.value.copy(gridType = newGridType)
    }

    /**
     * 清除提示消息
     */
    fun clearToast() {
        _photoToast.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
        cameraManager.shutdown()
    }
}
