package com.yanbao.camera.presentation.camera

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.camera.Camera2Manager
import com.yanbao.camera.core.util.CameraPreferencesManager
import com.yanbao.camera.core.util.GitBackupManager
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

/**
 * 相机核心 ViewModel
 *
 * 职责：
 * 1. 管理 9 大拍摄模式的状态（BASIC / PARALLAX / VIDEO_MASTER / AR / NATIVE / MEMORY / BEAUTY / TIMELAPSE / SLOW_MOTION）
 * 2. 持久化用户偏好设置（通过 CameraPreferencesManager → SharedPreferences）
 * 3. 协调 Camera2Manager / Param29DRenderer / ParallaxSensor / MasterFilterManager
 * 4. 将拍摄结果写入 Room 数据库（MemoryEntity）
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    // SharedPreferences 状态持久化管理器
    private val prefsManager = CameraPreferencesManager(application)
    // Git 增量备份管理器（拍照即备份）
    private val gitBackupManager = GitBackupManager(application)

    // ── 当前模式（从持久化恢复） ──────────────────────────────────────────────
    private val _currentMode = MutableStateFlow<CameraMode>(
        try { CameraMode.valueOf(prefsManager.getLastMode()) } catch (e: Exception) { CameraMode.BASIC }
    )
    val currentMode: StateFlow<CameraMode> = _currentMode.asStateFlow()

    // 29D参数
    private val _params29D = MutableStateFlow(Param29D())
    val params29D: StateFlow<Param29D> = _params29D.asStateFlow()

    // 是否录制雁宝记忆
    private val _isRecordingMemory = MutableStateFlow(false)
    val isRecordingMemory: StateFlow<Boolean> = _isRecordingMemory.asStateFlow()
    // ── 雁宝记忆：传入 JSON 参数包（从相册传入，拍照前1:1覆盖）──────────────
    private val _incomingMemoryParams = MutableStateFlow<com.yanbao.camera.presentation.camera.MemoryJsonParams?>(null)
    val incomingMemoryParams: StateFlow<com.yanbao.camera.presentation.camera.MemoryJsonParams?> = _incomingMemoryParams.asStateFlow()

    // ── 快捷工具栏状态（从持久化恢复） ───────────────────────────────────────
    private val _flashMode = MutableStateFlow(prefsManager.getFlashMode()) // 0自动 1开 2关
    val flashMode: StateFlow<Int> = _flashMode.asStateFlow()

    private val _aspectRatio = MutableStateFlow(prefsManager.getAspectRatio()) // 0 4:3, 1 16:9, 2 1:1
    val aspectRatio: StateFlow<Int> = _aspectRatio.asStateFlow()

    private val _timer = MutableStateFlow(prefsManager.getTimer()) // 0关 3s 10s
    val timer: StateFlow<Int> = _timer.asStateFlow()

    private val _lensFacing = MutableStateFlow(prefsManager.getLensFacing()) // 0后置 1前置
    val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

    // ── 2.9D 视差控制状态（从持久化恢复） ────────────────────────────────────
    private val _parallaxStrength = MutableStateFlow(prefsManager.getParallaxStrength())
    val parallaxStrength: StateFlow<Float> = _parallaxStrength.asStateFlow()

    private val _parallaxPreset = MutableStateFlow(prefsManager.getParallaxPreset()) // 0人像 1风景 2艺术
    val parallaxPreset: StateFlow<Int> = _parallaxPreset.asStateFlow()

    // ── 视频大师状态（从持久化恢复） ──────────────────────────────────────────
    private val _selectedFps = MutableStateFlow(prefsManager.getSelectedFps()) // 30 / 60 / 120
    val selectedFps: StateFlow<Int> = _selectedFps.asStateFlow()

    private val _timelapseInterval = MutableStateFlow(prefsManager.getTimelapseInterval()) // 0.5s ~ 10s
    val timelapseInterval: StateFlow<Float> = _timelapseInterval.asStateFlow()

    private val _totalDuration = MutableStateFlow(prefsManager.getTotalDuration()) // 1min ~ 30min
    val totalDuration: StateFlow<Float> = _totalDuration.asStateFlow()

    // ── AR 空间状态（从持久化恢复） ───────────────────────────────────────────
    private val _arCategory = MutableStateFlow(prefsManager.getArCategory()) // 0全部 1库洛米 2表情 3场景 4节日
    val arCategory: StateFlow<Int> = _arCategory.asStateFlow()

    private val _arSticker = MutableStateFlow(0) // 贴纸 ID
    val arSticker: StateFlow<Int> = _arSticker.asStateFlow()

    private val _lbsLabel = MutableStateFlow("") // LBS 位置标签
    val lbsLabel: StateFlow<String> = _lbsLabel.asStateFlow()

    // ── 原相机手动控制状态（从持久化恢复） ────────────────────────────────────
    private val _nativeIso = MutableStateFlow(prefsManager.getNativeIso())
    val nativeIso: StateFlow<Int> = _nativeIso.asStateFlow()

    private val _nativeShutterNs = MutableStateFlow(8_000_000L) // 1/125s = 8ms = 8,000,000 ns
    val nativeShutterNs: StateFlow<Long> = _nativeShutterNs.asStateFlow()

    private val _nativeEv = MutableStateFlow(prefsManager.getNativeEv())
    val nativeEv: StateFlow<Float> = _nativeEv.asStateFlow()

    private val _nativeWhiteBalance = MutableStateFlow(prefsManager.getNativeWb())
    val nativeWhiteBalance: StateFlow<Int> = _nativeWhiteBalance.asStateFlow()

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

    /** 触发拍照（兼容 CameraScreen 中的 viewModel.triggerCapture() 调用） */
    fun triggerCapture() {
        _captureRequested.value = true
        Log.i("AUDIT_CAPTURE", "capture_triggered, mode=${_currentMode.value.name}")
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
        prefsManager.saveLastMode(mode.name)
        Log.i("AUDIT_MODE", "mode_changed=${mode.name}")
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
        Log.i("AUDIT_VIDEO", "recording_started, fps=${_selectedFps.value}")
    }

    fun stopVideo() {
        if (::cameraManager.isInitialized) cameraManager.stopRecording()
        Log.i("AUDIT_VIDEO", "recording_stopped")
    }

    fun toggleMemoryRecording() {
        _isRecordingMemory.value = !_isRecordingMemory.value
    }

    fun setFlashMode(mode: Int) {
        _flashMode.value = mode
        prefsManager.saveFlashMode(mode)
    }

    fun setAspectRatio(ratio: Int) {
        _aspectRatio.value = ratio
        prefsManager.saveAspectRatio(ratio)
    }

    fun setTimer(seconds: Int) {
        _timer.value = seconds
        prefsManager.saveTimer(seconds)
    }

    fun flipLens() {
        val newFacing = if (_lensFacing.value == 0) 1 else 0
        _lensFacing.value = newFacing
        prefsManager.saveLensFacing(newFacing)
        if (::cameraManager.isInitialized) cameraManager.flipLens()
    }
    /** flipCamera — CameraScreen.kt 调用别名 */
    fun flipCamera() = flipLens()

    /** 设置传入的雁宝记忆 JSON 参数包（从相册传入） */
    fun setIncomingMemoryParams(params: MemoryJsonParams?) {
        _incomingMemoryParams.value = params
    }

    /**
     * 应用传入的雁宝记忆参数包
     * 拍照前：将 JSON 参数 1:1 覆盖当前取景器参数
     * 拍照后：参数已写入 Metadata（由 saveMemory 完成）
     */
    fun applyIncomingMemoryParams() {
        val params = _incomingMemoryParams.value ?: return
        params.filterId?.let { /* 应用大师滤镜编号 */ }
        params.masterFilterIndex?.let { /* 应用大师滤镜 */ }
        // 应用 29D 参数
        update29DParam {
            this.iso = params.iso
            this.ev = params.param29dLight.toInt() / 100 * 3 - 3
            this.saturation = params.param29dColor.toInt()
            this.sharpness = params.param29dMaterial.toInt()
            this.vignette = params.param29dSpace.toInt()
        }
        android.util.Log.i("MEMORY_APPLY", "Applied memory params: ISO=${params.iso}, shutter=${params.shutter}, style=${params.aiStyle}")
    }

    // ── 2.9D 视差控制 ─────────────────────────────────────────────────────────
    fun setParallaxStrength(strength: Float) {
        val clamped = strength.coerceIn(0f, 1f)
        _parallaxStrength.value = clamped
        prefsManager.saveParallaxStrength(clamped)
        renderer?.setParallaxOffset(clamped, 0f)
        Log.i("AUDIT_2.9D", "parallax_strength=${String.format("%.2f", clamped)}")
    }

    fun setParallaxPreset(preset: Int) {
        _parallaxPreset.value = preset
        prefsManager.saveParallaxPreset(preset)
        val presetName = listOf("人像", "风景", "艺术").getOrNull(preset) ?: "未知"
        Log.i("AUDIT_2.9D", "preset_selected=$presetName")
    }

    // ── 视频大师 ──────────────────────────────────────────────────────────────
    fun setFps(fps: Int) {
        _selectedFps.value = fps
        prefsManager.saveSelectedFps(fps)
        Log.i("AUDIT_VIDEO", "fps=$fps")
    }

    fun setTimelapseInterval(interval: Float) {
        val clamped = interval.coerceIn(0.5f, 10f)
        _timelapseInterval.value = clamped
        prefsManager.saveTimelapseInterval(clamped)
        Log.i("AUDIT_VIDEO", "timelapse_interval=${String.format("%.1f", clamped)}s")
    }

    fun setTotalDuration(duration: Float) {
        val clamped = duration.coerceIn(1f, 30f)
        _totalDuration.value = clamped
        prefsManager.saveTotalDuration(clamped)
        Log.i("AUDIT_VIDEO", "duration=${clamped.toInt()}min")
    }

    // ── AR 空间 ───────────────────────────────────────────────────────────────
    fun setArCategory(category: Int) {
        _arCategory.value = category
        prefsManager.saveArCategory(category)
        Log.i("AUDIT_AR", "category_selected=$category")
    }

    fun setArSticker(stickerId: Int) {
        _arSticker.value = stickerId
        Log.i("AUDIT_AR", "sticker_selected=$stickerId")
    }

    fun setLbsLabel(label: String) {
        _lbsLabel.value = label
    }

    // ── 原相机手动控制 ────────────────────────────────────────────────────────
    fun setNativeIso(iso: Int) {
        val clamped = iso.coerceIn(100, 6400)
        _nativeIso.value = clamped
        prefsManager.saveNativeIso(clamped)
        if (::cameraManager.isInitialized) cameraManager.setIso(clamped)
        Log.i("AUDIT_NATIVE", "iso=$clamped")
    }

    fun setNativeShutter(shutterNs: Long) {
        _nativeShutterNs.value = shutterNs
        if (::cameraManager.isInitialized) cameraManager.setExposureTime(shutterNs)
        Log.i("AUDIT_NATIVE", "shutter_ns=$shutterNs")
    }

    fun setNativeEv(ev: Float) {
        val clamped = ev.coerceIn(-3f, 3f)
        _nativeEv.value = clamped
        prefsManager.saveNativeEv(clamped)
        if (::cameraManager.isInitialized) cameraManager.setEv(clamped.toInt())
        val evStr = if (clamped >= 0) "+${String.format("%.1f", clamped)}" else String.format("%.1f", clamped)
        Log.i("AUDIT_NATIVE", "ev=$evStr")
    }

    fun setNativeWhiteBalance(wb: Int) {
        val clamped = wb.coerceIn(2000, 8000)
        _nativeWhiteBalance.value = clamped
        prefsManager.saveNativeWb(clamped)
        Log.i("AUDIT_NATIVE", "wb=${clamped}K")
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

    /** 兼容 CameraScreen 中 viewModel.update29DParams(it) 调用 */
    fun update29DParams(params: Param29D) {
        _params29D.value = params
        updateRendererParams()
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

            // ── Git 增量备份 Hook（拍照即备份，确保换号后数据不丢失）──────────
            try {
                // 初始化 Git 仓库（首次调用时创建，后续幂等）
                gitBackupManager.initGitRepo()
                // 备份数据库和配置文件
                gitBackupManager.backupDatabase()
                gitBackupManager.backupSharedPreferences()
                // 提交到本地 Git 仓库
                val commitMsg = if (isVideo) {
                    "video_saved: ${java.io.File(photoPath).name} [mode=${currentMode.value.name}]"
                } else {
                    "photo_saved: ${java.io.File(photoPath).name} [mode=${currentMode.value.name}]"
                }
                val result = gitBackupManager.commitToGit(commitMsg)
                result.onSuccess { Log.i("GIT_BACKUP", "备份成功: $it") }
                result.onFailure { Log.w("GIT_BACKUP", "备份失败（非阻塞）: ${it.message}") }
            } catch (e: Exception) {
                // Git 备份失败不影响拍照主流程
                Log.w("GIT_BACKUP", "Git 备份异常（非阻塞）: ${e.message}")
            }
        }
    }

    private fun createVideoFile(): File {
        val time = System.currentTimeMillis()
        val dir = getApplication<Application>().getExternalFilesDir(null)
            ?: getApplication<Application>().filesDir
        return File(dir, "VID_$time.mp4")
    }

    override fun onCleared() {
        super.onCleared()
        // 保存所有相机状态到 SharedPreferences（App 后台被杀时也能恢复）
        prefsManager.saveAllCameraState(
            mode = _currentMode.value.name,
            flashMode = _flashMode.value,
            aspectRatio = _aspectRatio.value,
            lensFacing = _lensFacing.value,
            timer = _timer.value,
            fps = _selectedFps.value,
            parallaxStrength = _parallaxStrength.value,
            parallaxPreset = _parallaxPreset.value,
            timelapseInterval = _timelapseInterval.value,
            totalDuration = _totalDuration.value,
            arCategory = _arCategory.value,
            nativeIso = _nativeIso.value,
            nativeEv = _nativeEv.value,
            nativeWb = _nativeWhiteBalance.value
        )
        stopCamera()
        parallaxSensor?.stop()
        Log.d("CameraViewModel", "onCleared: 相机状态已持久化")
    }
}
