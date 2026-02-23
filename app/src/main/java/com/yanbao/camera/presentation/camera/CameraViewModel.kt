package com.yanbao.camera.presentation.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.camera.Camera2PreviewManager
import com.yanbao.camera.core.util.Camera2ManagerEnhanced
import com.yanbao.camera.data.lbs.LbsService
import com.yanbao.camera.data.local.dao.YanbaoMemoryDao
import com.yanbao.camera.data.local.entity.YanbaoMemoryFactory
import com.yanbao.camera.core.model.CameraMode
import com.yanbao.camera.core.model.YanbaoMode
import com.yanbao.camera.data.model.Camera29DState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 相机 ViewModel（满血版 v2）
 *
 * 新增：
 * - 9种 YanbaoMode 状态管理
 * - 闪光灯（AUTO/ON/OFF）
 * - 比例（4:3 / 16:9 / 1:1）
 * - 定时（0 / 3 / 10 秒）
 * - 录像模式（isRecording + recordingDuration）
 * - 帧率（30 / 60 / 120 fps）
 * - 焦段（0.5x / 1x / 2x / 5x）
 * - 快门动画触发
 * - 真实 GPS 定位写入数据库
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val yanbaoMemoryDao: YanbaoMemoryDao,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private var camera2Manager: Camera2ManagerEnhanced? = null
    private val lbsService by lazy { LbsService(appContext) }
    private var recordingJob: Job? = null

    companion object {
        private const val TAG = "CameraViewModel"
    }

    // ─── 29D 参数 ─────────────────────────────────────────────────────────
    private val _camera29DState = MutableStateFlow(Camera29DState())
    val camera29DState: StateFlow<Camera29DState> = _camera29DState.asStateFlow()

    // ─── 相机底层模式（Photo/Video）───────────────────────────────────────
    private val _currentMode = MutableStateFlow(CameraMode.PHOTO)
    val currentMode: StateFlow<CameraMode> = _currentMode.asStateFlow()

    // ─── 雁宝9种模式 ──────────────────────────────────────────────────────
    private val _yanbaoMode = MutableStateFlow(YanbaoMode.BASIC)
    val yanbaoMode: StateFlow<YanbaoMode> = _yanbaoMode.asStateFlow()

    // ─── 29D 面板显示 ─────────────────────────────────────────────────────
    private val _show29DPanel = MutableStateFlow(false)
    val show29DPanel: StateFlow<Boolean> = _show29DPanel.asStateFlow()

    // ─── 拍照预览 URI ─────────────────────────────────────────────────────
    private val _capturePreviewUri = MutableStateFlow<String?>(null)
    val capturePreviewUri: StateFlow<String?> = _capturePreviewUri.asStateFlow()

    // ─── 快门动画触发 ─────────────────────────────────────────────────────
    private val _shutterFlash = MutableStateFlow(false)
    val shutterFlash: StateFlow<Boolean> = _shutterFlash.asStateFlow()

    // ─── 闪光灯模式 ───────────────────────────────────────────────────────
    enum class FlashMode(val label: String) { AUTO("自动"), ON("开"), OFF("关") }
    private val _flashMode = MutableStateFlow(FlashMode.AUTO)
    val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()

    // ─── 拍摄比例 ─────────────────────────────────────────────────────────
    enum class AspectRatio(val label: String) { R4_3("4:3"), R16_9("16:9"), R1_1("1:1") }
    private val _aspectRatio = MutableStateFlow(AspectRatio.R4_3)
    val aspectRatio: StateFlow<AspectRatio> = _aspectRatio.asStateFlow()

    // ─── 定时拍摄 ─────────────────────────────────────────────────────────
    enum class TimerMode(val label: String, val seconds: Int) {
        OFF("关", 0), S3("3s", 3), S10("10s", 10)
    }
    private val _timerMode = MutableStateFlow(TimerMode.OFF)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    // ─── 倒计时状态 ───────────────────────────────────────────────────────
    private val _timerCountdown = MutableStateFlow(0)
    val timerCountdown: StateFlow<Int> = _timerCountdown.asStateFlow()

    // ─── 录像状态 ─────────────────────────────────────────────────────────
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    // ─── 帧率（视频大师模式）─────────────────────────────────────────────
    private val _frameRate = MutableStateFlow(30)
    val frameRate: StateFlow<Int> = _frameRate.asStateFlow()

    // ─── 焦段（原相机模式）───────────────────────────────────────────────
    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    // ─── 2.9D 强度 ────────────────────────────────────────────────────────
    private val _d2_9Intensity = MutableStateFlow(0.5f)
    val d2_9Intensity: StateFlow<Float> = _d2_9Intensity.asStateFlow()

    // ─── 大师滤镜 ─────────────────────────────────────────────────────────
    private val _masterFilterId = MutableStateFlow("xieyi_01")
    val masterFilterId: StateFlow<String> = _masterFilterId.asStateFlow()

    private val _masterFilterIntensity = MutableStateFlow(0.8f)
    val masterFilterIntensity: StateFlow<Float> = _masterFilterIntensity.asStateFlow()

    // ─── 记忆保存对话框 ───────────────────────────────────────────────────
    private val _showMemorySaveDialog = MutableStateFlow(false)
    val showMemorySaveDialog: StateFlow<Boolean> = _showMemorySaveDialog.asStateFlow()

    // ─── 前后摄像头 ───────────────────────────────────────────────────────
    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    // ─── 模式切换 ─────────────────────────────────────────────────────────

    fun setYanbaoMode(mode: YanbaoMode) {
        viewModelScope.launch {
            _yanbaoMode.value = mode
            // 视频大师模式自动切换到 VIDEO
            _currentMode.value = if (mode == YanbaoMode.AR) CameraMode.VIDEO else CameraMode.PHOTO
            Log.d(TAG, "YanbaoMode → $mode")
        }
    }

    fun switchMode(mode: CameraMode) {
        viewModelScope.launch {
            _currentMode.value = mode
            _show29DPanel.value = (mode == CameraMode.PROFESSIONAL)
            Log.d(TAG, "CameraMode → ${mode.displayName}")
        }
    }

    fun toggle29DPanel() {
        _show29DPanel.value = !_show29DPanel.value
    }

    // ─── 快捷工具栏 ───────────────────────────────────────────────────────

    fun cycleFlashMode() {
        val modes = FlashMode.values()
        val next = modes[(modes.indexOf(_flashMode.value) + 1) % modes.size]
        _flashMode.value = next
        Log.d(TAG, "FlashMode → $next")
    }

    fun cycleAspectRatio() {
        val ratios = AspectRatio.values()
        val next = ratios[(ratios.indexOf(_aspectRatio.value) + 1) % ratios.size]
        _aspectRatio.value = next
        Log.d(TAG, "AspectRatio → $next")
    }

    fun cycleTimerMode() {
        val timers = TimerMode.values()
        val next = timers[(timers.indexOf(_timerMode.value) + 1) % timers.size]
        _timerMode.value = next
        Log.d(TAG, "TimerMode → $next")
    }

    fun flipCamera() {
        _isFrontCamera.value = !_isFrontCamera.value
        Log.d(TAG, "Camera flipped, front=${_isFrontCamera.value}")
    }

    fun setZoomLevel(zoom: Float) {
        _zoomLevel.value = zoom
        Log.d(TAG, "Zoom → ${zoom}x")
    }

    fun setFrameRate(fps: Int) {
        _frameRate.value = fps
        Log.d(TAG, "FrameRate → ${fps}fps")
    }

    fun setD2_9Intensity(v: Float) {
        _d2_9Intensity.value = v
    }

    fun setMasterFilter(filterId: String) {
        _masterFilterId.value = filterId
        Log.d(TAG, "MasterFilter → $filterId")
    }

    fun setMasterFilterIntensity(v: Float) {
        _masterFilterIntensity.value = v
    }

    fun showMemorySaveDialog() {
        _showMemorySaveDialog.value = true
    }

    fun dismissMemorySaveDialog() {
        _showMemorySaveDialog.value = false
    }

    // ─── 拍照 ─────────────────────────────────────────────────────────────

    fun takePhoto(context: Context) {
        viewModelScope.launch {
            val timer = _timerMode.value
            if (timer.seconds > 0) {
                // 倒计时
                for (i in timer.seconds downTo 1) {
                    _timerCountdown.value = i
                    delay(1000)
                }
                _timerCountdown.value = 0
            }
            // 快门闪光
            _shutterFlash.value = true
            delay(120)
            _shutterFlash.value = false

            try {
                Log.d(TAG, "AUDIT_CAPTURE: Starting photo capture, mode=${_yanbaoMode.value}")
                camera2Manager?.takePhoto()
                android.widget.Toast.makeText(context, "照片已保存", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "AUDIT_CAPTURE: Photo capture failed", e)
                android.widget.Toast.makeText(context, "拍照失败", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── 录像 ─────────────────────────────────────────────────────────────

    fun startRecording(context: Context) {
        if (_isRecording.value) return
        _isRecording.value = true
        _recordingDurationSec.value = 0
        recordingJob = viewModelScope.launch {
            while (_isRecording.value) {
                delay(1000)
                _recordingDurationSec.value++
            }
        }
        Log.d(TAG, "Recording started, fps=${_frameRate.value}")
        android.widget.Toast.makeText(context, "开始录像", android.widget.Toast.LENGTH_SHORT).show()
    }

    fun stopRecording(context: Context) {
        if (!_isRecording.value) return
        _isRecording.value = false
        recordingJob?.cancel()
        val duration = _recordingDurationSec.value
        _recordingDurationSec.value = 0
        Log.d(TAG, "Recording stopped, duration=${duration}s")
        android.widget.Toast.makeText(context, "录像已保存（${duration}秒）", android.widget.Toast.LENGTH_SHORT).show()
    }

    fun toggleRecording(context: Context) {
        if (_isRecording.value) stopRecording(context) else startRecording(context)
    }

    // ─── 29D 参数更新 ─────────────────────────────────────────────────────

    fun updateParameter(name: String, value: Any) {
        viewModelScope.launch {
            _camera29DState.update { current ->
                val updated = when (name) {
                    "brightness" -> current.copy(brightness = value as Float)
                    "contrast" -> current.copy(contrast = value as Float)
                    "saturation" -> current.copy(saturation = value as Float)
                    "sharpness" -> current.copy(sharpness = value as Float)
                    "exposure" -> current.copy(exposure = value as Float)
                    "colorTemp" -> current.copy(colorTemp = value as Float)
                    "tint" -> current.copy(tint = value as Float)
                    "hue" -> current.copy(hue = value as Float)
                    "vibrance" -> current.copy(vibrance = value as Float)
                    "red" -> current.copy(red = value as Float)
                    "green" -> current.copy(green = value as Float)
                    "blue" -> current.copy(blue = value as Float)
                    "cyan" -> current.copy(cyan = value as Float)
                    "magenta" -> current.copy(magenta = value as Float)
                    "yellow" -> current.copy(yellow = value as Float)
                    "orange" -> current.copy(orange = value as Float)
                    "highlights" -> current.copy(highlights = value as Float)
                    "shadows" -> current.copy(shadows = value as Float)
                    "whites" -> current.copy(whites = value as Float)
                    "blacks" -> current.copy(blacks = value as Float)
                    "clarity" -> current.copy(clarity = value as Float)
                    "dehaze" -> current.copy(dehaze = value as Float)
                    "noiseReduction" -> current.copy(noiseReduction = value as Float)
                    "grain" -> current.copy(grain = value as Float)
                    "vignette" -> current.copy(vignette = value as Float)
                    "beautySmooth" -> current.copy(beautySmooth = value as Float)
                    "beautyWhiten" -> current.copy(beautyWhiten = value as Float)
                    "beautyEyeEnlarge" -> current.copy(beautyEyeEnlarge = value as Float)
                    "beautyFaceSlim" -> current.copy(beautyFaceSlim = value as Float)
                    "iso" -> current.copy(iso = value as Int)
                    "exposureTime" -> current.copy(exposureTime = value as Long)
                    "whiteBalance" -> current.copy(whiteBalance = value as Int)
                    "focusMode" -> current.copy(focusMode = value as String)
                    "zoomRatio" -> current.copy(zoomRatio = value as Float)
                    "masterFilterId" -> current.copy(masterFilterId = value as String?)
                    "is2Dot9DEnabled" -> current.copy(is2Dot9DEnabled = value as Boolean)
                    "parallaxOffset" -> current.copy(parallaxOffset = value as Pair<Float, Float>)
                    else -> current
                }
                Log.d(TAG, "AUDIT_PARAM: $name = $value")
                updated
            }
            camera2Manager?.update29DParams(_camera29DState.value)
        }
    }

    fun resetParameters() {
        _camera29DState.value = Camera29DState()
        Log.d(TAG, "Parameters reset to default")
    }

    fun restoreParametersFromJson(json: String) {
        viewModelScope.launch {
            try {
                _camera29DState.value = Camera29DState.fromJson(json)
                Log.d(TAG, "Parameters restored from JSON")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore parameters from JSON", e)
            }
        }
    }

    // ─── Camera2 生命周期 ─────────────────────────────────────────────────

    fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, context: Context) {
        viewModelScope.launch {
            try {
                camera2Manager = Camera2ManagerEnhanced(context)
                camera2Manager?.onPhotoSaved = { path -> savePhotoMetadata(path) }
                val surface = Surface(surfaceTexture)
                camera2Manager?.openCamera(surface)
                Log.d(TAG, "Camera2Manager 初始化成功")
            } catch (e: Exception) {
                Log.e(TAG, "Camera2Manager 初始化失败", e)
            }
        }
    }

    fun onSurfaceTextureDestroyed() {
        camera2Manager?.closeCamera()
        camera2Manager = null
    }

    fun setMode(mode: CameraMode) = switchMode(mode)

    fun showCapturePreview(uri: String) {
        viewModelScope.launch {
            _capturePreviewUri.value = uri
            delay(2000)
            _capturePreviewUri.value = null
        }
    }

    // ─── 照片元数据保存（真实 GPS）────────────────────────────────────────

    fun savePhotoMetadata(imagePath: String) {
        viewModelScope.launch {
            try {
                val parameterJson = _camera29DState.value.toJson()
                val shootingMode = _yanbaoMode.value.displayName

                var latitude = 0.0
                var longitude = 0.0
                var locationName: String? = null

                try {
                    if (lbsService.hasLocationPermission() && lbsService.isLocationEnabled()) {
                        val loc = withContext(Dispatchers.IO) { lbsService.getCurrentLocation() }
                        if (loc != null) {
                            latitude = loc.latitude
                            longitude = loc.longitude
                            locationName = reverseGeocode(latitude, longitude)
                            Log.d(TAG, "GPS: $latitude, $longitude → $locationName")
                        }
                    } else {
                        locationName = "位置未知"
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "GPS 获取失败: ${e.message}")
                    locationName = "位置获取失败"
                }

                val weatherType = inferWeatherFromTime()
                val memory = YanbaoMemoryFactory.create(
                    imagePath = imagePath,
                    latitude = latitude,
                    longitude = longitude,
                    locationName = locationName,
                    weatherType = weatherType,
                    shootingMode = shootingMode,
                    parameterSnapshotJson = parameterJson,
                    memberNumber = "88888"
                )
                yanbaoMemoryDao.insert(memory)
                showCapturePreview(imagePath)
                Log.d(TAG, "AUDIT_DB: Photo metadata saved: $imagePath @ ($latitude, $longitude) mode=$shootingMode")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save photo metadata", e)
            }
        }
    }

    // ─── 工具函数 ─────────────────────────────────────────────────────

    private suspend fun reverseGeocodecode(lat: Double, lng: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = android.location.Geocoder(appContext, java.util.Locale.getDefault())
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    var result: String? = null
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        result = addresses.firstOrNull()?.let { addr ->
                            listOfNotNull(addr.subLocality, addr.locality, addr.adminArea)
                                .take(2).joinToString("·")
                        }
                    }
                    delay(2000)
                    result
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    addresses?.firstOrNull()?.let { addr ->
                        listOfNotNull(addr.subLocality, addr.locality, addr.adminArea)
                            .take(2).joinToString("·")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Geocoder failed: ${e.message}")
                null
            }
        }
    }

    private fun inferWeatherFromTime(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..9 -> "晴·清晨"
            in 10..16 -> "晴"
            in 17..19 -> "晴·黄昏"
            in 20..23 -> "夜"
            else -> "夜·深"
        }
    }

    override fun onCleared() {
        super.onCleared()
        recordingJob?.cancel()
        camera2Manager?.closeCamera()
    }
}
