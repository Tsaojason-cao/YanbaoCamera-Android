package com.yanbao.camera.presentation.camera

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.camera.Camera2Manager
import com.yanbao.camera.data.database.AppDatabase
import com.yanbao.camera.data.database.MemoryEntity
import com.yanbao.camera.filter.MasterFilterManager
import com.yanbao.camera.render.Param29DRenderer
import com.yanbao.camera.sensor.ParallaxSensor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    // 当前模式
    private val _currentMode = MutableStateFlow<CameraMode>(CameraMode.BASIC)
    val currentMode: StateFlow<CameraMode> = _currentMode.asStateFlow()

    // 29D参数
    private val _params29D = MutableStateFlow(Param29D())
    val params29D: StateFlow<Param29D> = _params29D.asStateFlow()

    // 是否录制雁宝记忆
    private val _isRecordingMemory = MutableStateFlow(false)
    val isRecordingMemory: StateFlow<Boolean> = _isRecordingMemory.asStateFlow()

    // 快捷工具栏状态
    private val _flashMode = MutableStateFlow(0) // 0自动 1开 2关
    val flashMode: StateFlow<Int> = _flashMode.asStateFlow()
    private val _aspectRatio = MutableStateFlow(0) // 0 4:3, 1 16:9, 2 1:1
    val aspectRatio: StateFlow<Int> = _aspectRatio.asStateFlow()
    private val _timer = MutableStateFlow(0) // 0关 3s 10s
    val timer: StateFlow<Int> = _timer.asStateFlow()
    private val _lensFacing = MutableStateFlow(0) // 0后置 1前置
    val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

    // 相机硬件管理器
    private lateinit var cameraManager: Camera2Manager
    // 渲染器
    private var renderer: Param29DRenderer? = null
    // 传感器
    private var parallaxSensor: ParallaxSensor? = null
    // 滤镜管理器
    private lateinit var filterManager: MasterFilterManager

    // 是否正在录像
    val isRecordingState: Boolean get() = if (::cameraManager.isInitialized) cameraManager.isRecording() else false

    // 拍照触发器（供 Camera2PreviewView 监听）
    private val _captureRequested = MutableStateFlow(false)
    val captureRequested: StateFlow<Boolean> = _captureRequested.asStateFlow()

    fun resetCaptureRequest() {
        _captureRequested.value = false
    }

    fun initCameraManager(context: Context) {
        cameraManager = Camera2Manager(context).apply {
            onPhotoSaved = { file ->
                // 拍照成功，保存雁宝记忆
                viewModelScope.launch {
                    saveMemory(file.absolutePath)
                }
            }
            onRecordingStopped = { file ->
                viewModelScope.launch {
                    saveMemory(file.absolutePath, isVideo = true)
                }
            }
        }
        filterManager = MasterFilterManager(context)
        parallaxSensor = ParallaxSensor(context)
    }

    fun setRenderer(renderer: Param29DRenderer) {
        this.renderer = renderer
        // 初始化参数
        updateRendererParams()
    }

    fun startCamera(surfaceTexture: android.graphics.SurfaceTexture, width: Int, height: Int) {
        cameraManager.startCamera(surfaceTexture, width, height)
    }

    fun stopCamera() {
        if (::cameraManager.isInitialized) cameraManager.stopCamera()
    }

    fun setMode(mode: CameraMode) {
        _currentMode.value = mode
        // 根据模式切换传感器等
        if (mode == CameraMode.PARALLAX) {
            parallaxSensor?.start()
            viewModelScope.launch {
                parallaxSensor?.tilt?.collect { (x, y) ->
                    renderer?.setParallaxOffset(x, y)
                }
            }
        } else {
            parallaxSensor?.stop()
        }
    }

    fun takePhoto() {
        // 通过 captureRequested 触发 Camera2PreviewView 拍照
        _captureRequested.value = true
    }

    fun startVideo() {
        val file = createVideoFile()
        if (::cameraManager.isInitialized) cameraManager.startRecording(file)
    }

    fun stopVideo() {
        if (::cameraManager.isInitialized) cameraManager.stopRecording()
    }

    fun toggleMemoryRecording() {
        _isRecordingMemory.value = !_isRecordingMemory.value
    }

    fun setFlashMode(mode: Int) { _flashMode.value = mode }
    fun setAspectRatio(ratio: Int) { _aspectRatio.value = ratio }
    fun setTimer(seconds: Int) { _timer.value = seconds }
    fun flipLens() {
        _lensFacing.value = if (_lensFacing.value == 0) 1 else 0
        if (::cameraManager.isInitialized) cameraManager.flipLens()
    }

    // 29D参数更新
    fun update29DParam(block: Param29D.() -> Unit) {
        _params29D.value = _params29D.value.copy().apply(block)
        updateRendererParams()
        // 如果是专业模式，同步硬件参数
        if (currentMode.value == CameraMode.NATIVE && ::cameraManager.isInitialized) {
            val params = _params29D.value
            cameraManager.setIso(params.iso)
            cameraManager.setExposureTime(params.shutterSpeed.toLong())
            cameraManager.setEv(params.ev.toInt())
        }
    }

    private fun updateRendererParams() {
        renderer?.updateParams(_params29D.value.toFloatArray())
    }

    // 硬件直接控制（用于原相机模式）
    fun setHardwareIso(value: Float) {
        val iso = (value * 6300 + 100).toInt()
        if (::cameraManager.isInitialized) cameraManager.setIso(iso)
        update29DParam { this.iso = iso }
    }
    fun setHardwareExposure(value: Float) {
        val exp = (value * (30_000_000_000 - 1_000_000) + 1_000_000).toLong()
        if (::cameraManager.isInitialized) cameraManager.setExposureTime(exp)
        update29DParam { shutterSpeed = (1_000_000_000 / exp).toString() }
    }
    fun setHardwareEv(value: Float) {
        val ev = (value * 6 - 3).toInt()
        if (::cameraManager.isInitialized) cameraManager.setEv(ev)
        update29DParam { this.ev = ev.toFloat() }
    }

    // 保存雁宝记忆
    private fun saveMemory(photoPath: String, isVideo: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val params = _params29D.value
            val memory = MemoryEntity.fromParams29D(
                photoPath = photoPath,
                mode = currentMode.value.name,
                params = params,
                filterId = null,
                lat = null,
                lng = null,
                address = null,
                weather = null
            )
            db.memoryDao().insert(memory)
            Log.d("CameraViewModel", "Memory saved: $photoPath, isVideo=$isVideo")
        }
    }

    private fun createVideoFile(): File {
        val time = System.currentTimeMillis()
        val dir = getApplication<Application>().getExternalFilesDir(null) ?: getApplication<Application>().filesDir
        return File(dir, "VID_$time.mp4")
    }

    override fun onCleared() {
        super.onCleared()
        stopCamera()
        parallaxSensor?.stop()
    }
}
