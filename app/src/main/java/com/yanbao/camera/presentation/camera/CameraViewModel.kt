package com.yanbao.camera.presentation.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.util.Camera2ManagerEnhanced
import com.yanbao.camera.data.lbs.LbsService
import com.yanbao.camera.data.local.dao.YanbaoMemoryDao
import com.yanbao.camera.data.local.entity.YanbaoMemoryFactory
import com.yanbao.camera.core.model.CameraMode
import com.yanbao.camera.data.model.Camera29DState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 相机 ViewModel（满血版）
 *
 * 使用 StateFlow 管理 29D 参数状态
 * 确保滑块一动，预览立即响应
 *
 * 修复：
 * - savePhotoMetadata 改为真实 GPS 定位（FusedLocationProviderClient）
 * - 注入 ApplicationContext 以访问 LbsService
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val yanbaoMemoryDao: YanbaoMemoryDao,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private var camera2Manager: Camera2ManagerEnhanced? = null
    private val lbsService by lazy { LbsService(appContext) }

    companion object {
        private const val TAG = "CameraViewModel"
    }

    // 29D 参数状态
    private val _camera29DState = MutableStateFlow(Camera29DState())
    val camera29DState: StateFlow<Camera29DState> = _camera29DState.asStateFlow()

    // 当前相机模式
    private val _currentMode = MutableStateFlow(CameraMode.PHOTO)
    val currentMode: StateFlow<CameraMode> = _currentMode.asStateFlow()

    // 是否显示 29D 专业面板
    private val _show29DPanel = MutableStateFlow(false)
    val show29DPanel: StateFlow<Boolean> = _show29DPanel.asStateFlow()

    // 拍照反馈状态
    private val _capturePreviewUri = MutableStateFlow<String?>(null)
    val capturePreviewUri: StateFlow<String?> = _capturePreviewUri.asStateFlow()

    /**
     * 更新 29D 参数
     */
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
                Log.d(TAG, "Parameter updated: $name = $value")
                updated
            }
            camera2Manager?.update29DParams(_camera29DState.value)
        }
    }

    fun switchMode(mode: CameraMode) {
        viewModelScope.launch {
            _currentMode.value = mode
            _show29DPanel.value = (mode == CameraMode.PROFESSIONAL)
            Log.d(TAG, "Camera mode switched to: ${mode.displayName}")
        }
    }

    fun toggle29DPanel() {
        viewModelScope.launch {
            _show29DPanel.value = !_show29DPanel.value
        }
    }

    fun resetParameters() {
        viewModelScope.launch {
            _camera29DState.value = Camera29DState()
            Log.d(TAG, "All parameters reset to default")
        }
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

    fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, context: Context) {
        viewModelScope.launch {
            try {
                camera2Manager = Camera2ManagerEnhanced(context)
                camera2Manager?.onPhotoSaved = { path ->
                    savePhotoMetadata(path)
                }
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

    fun takePhoto(context: Context) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "AUDIT_CAPTURE: Starting photo capture")
                triggerVibration(context)
                camera2Manager?.takePhoto()
                Log.d(TAG, "AUDIT_CAPTURE: Photo capture triggered")
                android.widget.Toast.makeText(context, "照片已保存", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "AUDIT_CAPTURE: Photo capture failed", e)
                android.widget.Toast.makeText(context, "拍照失败", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun triggerVibration(context: Context) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            Log.e(TAG, "VIBRATION: Failed", e)
        }
    }

    fun showCapturePreview(uri: String) {
        viewModelScope.launch {
            _capturePreviewUri.value = uri
            kotlinx.coroutines.delay(2000)
            _capturePreviewUri.value = null
        }
    }

    /**
     * 拍照时存入 YanbaoMemory 数据库（满血版：真实 GPS 定位）
     *
     * 流程：
     * 1. 尝试通过 FusedLocationProviderClient 获取真实 GPS 坐标
     * 2. 若权限缺失或定位失败，降级为 (0.0, 0.0) + 错误标记
     * 3. 将真实坐标写入 YanbaoMemory
     */
    fun savePhotoMetadata(imagePath: String) {
        viewModelScope.launch {
            try {
                val parameterJson = _camera29DState.value.toJson()
                val shootingMode = _currentMode.value.displayName

                // ─── 真实 GPS 定位 ────────────────────────────────────────
                var latitude = 0.0
                var longitude = 0.0
                var locationName: String? = null

                try {
                    if (lbsService.hasLocationPermission() && lbsService.isLocationEnabled()) {
                        val loc = withContext(Dispatchers.IO) { lbsService.getCurrentLocation() }
                        if (loc != null) {
                            latitude = loc.latitude
                            longitude = loc.longitude
                            // 反向地理编码（Android Geocoder）
                            locationName = reverseGeocode(latitude, longitude)
                            Log.d(TAG, "GPS: $latitude, $longitude → $locationName")
                        }
                    } else {
                        Log.w(TAG, "GPS: 无位置权限或服务未开启，坐标设为 0,0")
                        locationName = "位置未知"
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "GPS 获取失败: ${e.message}")
                    locationName = "位置获取失败"
                }

                // ─── 天气（简化：根据时间推断，后续可接 API）────────────────
                val weatherType = inferWeatherFromTime()

                // ─── 写入数据库 ───────────────────────────────────────────
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

                // ─── 显示缩略图预览 ───────────────────────────────────────
                showCapturePreview(imagePath)

                Log.d(TAG, "Photo metadata saved: $imagePath @ ($latitude, $longitude)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save photo metadata", e)
            }
        }
    }

    /**
     * 反向地理编码（Android Geocoder）
     */
    private suspend fun reverseGeocode(lat: Double, lng: Double): String? {
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
                    // 等待回调（最多 2 秒）
                    kotlinx.coroutines.delay(2000)
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

    /**
     * 根据当前时间推断天气（简化版，后续可接 OpenWeather API）
     */
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
}
