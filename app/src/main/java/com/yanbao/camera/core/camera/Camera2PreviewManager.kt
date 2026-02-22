package com.yanbao.camera.core.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.params.RggbChannelVector
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.Surface
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.ln

/**
 * 雁宝相机 Camera2 核心管理器（强健版）
 *
 * 架构职责：
 * 1. 管理 Camera2 硬件生命周期（打开/关闭/切换）
 * 2. 将预览帧输出到 GLSurfaceView 的 SurfaceTexture（供 OpenGL 实时渲染）
 * 3. 支持全手动参数控制：ISO / 快门 / 曝光补偿 / 白平衡 / 焦距
 * 4. 支持拍照（JPEG 输出）
 * 5. 支持闪光灯、前后摄像头切换、预览尺寸选择
 */
class Camera2PreviewManager(private val context: Context) {

    companion object {
        private const val TAG = "Camera2PreviewManager"
        val PREVIEW_SIZES = listOf(
            Size(1920, 1080),
            Size(1280, 720),
            Size(640, 480)
        )
        const val DEFAULT_ISO = 400
        const val DEFAULT_EXPOSURE_TIME_NS = 33_333_333L
        const val DEFAULT_WHITE_BALANCE_K = 5500
        const val DEFAULT_EV_COMPENSATION = 0
    }

    enum class FlashMode { AUTO, ON, OFF }
    enum class CameraFacing { BACK, FRONT }
    enum class ControlMode { AUTO, MANUAL }

    private val cameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null

    private val backgroundThread = HandlerThread("CameraBackground").apply { start() }
    private val backgroundHandler = Handler(backgroundThread.looper)

    private var isPreviewActive = false
    private var currentCameraFacing = CameraFacing.BACK
    private var currentFlashMode = FlashMode.AUTO
    private var currentPreviewSize = PREVIEW_SIZES[0]
    private var currentSurface: Surface? = null
    private var controlMode = ControlMode.AUTO

    private var manualISO: Int = DEFAULT_ISO
    private var manualExposureTimeNs: Long = DEFAULT_EXPOSURE_TIME_NS
    private var manualWhiteBalanceK: Int = DEFAULT_WHITE_BALANCE_K
    private var evCompensation: Int = DEFAULT_EV_COMPENSATION
    private var manualFocusDistance: Float = 0f
    private var isAutoFocus: Boolean = true

    private var isoRange: Range<Int>? = null
    private var exposureTimeRange: Range<Long>? = null
    private var evRange: Range<Int>? = null
    private var maxFocusDistance: Float = 0f

    var onPreviewStarted: (() -> Unit)? = null

    suspend fun openCamera(
        surface: Surface,
        facing: CameraFacing = CameraFacing.BACK,
        previewSize: Size = PREVIEW_SIZES[0]
    ): Boolean = suspendCancellableCoroutine { continuation ->
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        try {
            currentCameraFacing = facing
            currentPreviewSize = previewSize
            currentSurface = surface
            val cameraId = getCameraId(facing) ?: run {
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }
            cacheHardwareCapabilities(cameraId)
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    startPreview(surface, previewSize) { success -> continuation.resume(success) }
                }
                override fun onDisconnected(camera: CameraDevice) {
                    camera.close(); cameraDevice = null; continuation.resume(false)
                }
                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close(); cameraDevice = null
                    continuation.resumeWithException(RuntimeException("Camera error: $error"))
                }
            }, backgroundHandler)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    fun closeCamera() {
        isPreviewActive = false
        captureSession?.close(); captureSession = null
        cameraDevice?.close(); cameraDevice = null
        imageReader?.close(); imageReader = null
        previewRequestBuilder = null
    }

    fun release() {
        closeCamera()
        backgroundThread.quitSafely()
        currentSurface = null
    }

    suspend fun switchCamera(): Boolean {
        val newFacing = if (currentCameraFacing == CameraFacing.BACK) CameraFacing.FRONT else CameraFacing.BACK
        closeCamera()
        val surface = currentSurface ?: return false
        return openCamera(surface, newFacing, currentPreviewSize)
    }

    fun setFlashMode(mode: FlashMode) {
        currentFlashMode = mode
        applyFlashToBuilder(previewRequestBuilder ?: return)
        refreshPreview()
    }

    suspend fun setPreviewSize(size: Size): Boolean {
        currentPreviewSize = size
        val surface = currentSurface ?: return false
        closeCamera()
        return openCamera(surface, currentCameraFacing, size)
    }

    fun setControlMode(mode: ControlMode) {
        controlMode = mode
        applyAllParamsToBuilder()
        refreshPreview()
    }

    fun setISO(iso: Int) {
        manualISO = iso.coerceIn(isoRange?.lower ?: 100, isoRange?.upper ?: 6400)
        if (controlMode == ControlMode.MANUAL) { applyAllParamsToBuilder(); refreshPreview() }
        Log.d(TAG, "ISO: $manualISO")
    }

    fun setExposureTime(exposureTimeNs: Long) {
        manualExposureTimeNs = exposureTimeNs.coerceIn(
            exposureTimeRange?.lower ?: 125_000L,
            exposureTimeRange?.upper ?: 30_000_000_000L
        )
        if (controlMode == ControlMode.MANUAL) { applyAllParamsToBuilder(); refreshPreview() }
        Log.d(TAG, "Shutter: ${manualExposureTimeNs}ns")
    }

    fun setEVCompensation(ev: Int) {
        evCompensation = ev.coerceIn(evRange?.lower ?: -3, evRange?.upper ?: 3)
        if (controlMode == ControlMode.AUTO) { applyAllParamsToBuilder(); refreshPreview() }
        Log.d(TAG, "EV: $evCompensation")
    }

    fun setWhiteBalance(kelvin: Int) {
        manualWhiteBalanceK = kelvin.coerceIn(2000, 10000)
        if (controlMode == ControlMode.MANUAL) { applyAllParamsToBuilder(); refreshPreview() }
        Log.d(TAG, "WB: ${manualWhiteBalanceK}K")
    }

    fun setFocusDistance(normalizedDistance: Float) {
        isAutoFocus = false
        manualFocusDistance = (normalizedDistance * maxFocusDistance).coerceIn(0f, maxFocusDistance)
        applyFocusToBuilder(previewRequestBuilder ?: return)
        refreshPreview()
    }

    fun setAutoFocus() {
        isAutoFocus = true
        applyFocusToBuilder(previewRequestBuilder ?: return)
        refreshPreview()
    }

    /** 批量更新 29D 硬件参数（ISO + 快门 + 白平衡，一次性下发） */
    fun update29DHardwareParams(iso: Int, exposureTimeNs: Long, whiteBalanceK: Int) {
        manualISO = iso.coerceIn(isoRange?.lower ?: 100, isoRange?.upper ?: 6400)
        manualExposureTimeNs = exposureTimeNs.coerceIn(
            exposureTimeRange?.lower ?: 125_000L,
            exposureTimeRange?.upper ?: 30_000_000_000L
        )
        manualWhiteBalanceK = whiteBalanceK.coerceIn(2000, 10000)
        applyAllParamsToBuilder()
        refreshPreview()
        Log.d(TAG, "29D 硬件批量下发: ISO=$manualISO, Shutter=${manualExposureTimeNs}ns, WB=${manualWhiteBalanceK}K")
    }

    suspend fun takePicture(): Bitmap? = suspendCancellableCoroutine { continuation ->
        val camera = cameraDevice ?: run { continuation.resume(null); return@suspendCancellableCoroutine }
        val session = captureSession ?: run { continuation.resume(null); return@suspendCancellableCoroutine }
        try {
            val reader = ImageReader.newInstance(
                currentPreviewSize.width, currentPreviewSize.height, ImageFormat.JPEG, 1
            )
            reader.setOnImageAvailableListener({ r ->
                val image = r.acquireLatestImage()
                try {
                    if (image != null) {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        continuation.resume(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                    } else continuation.resume(null)
                } finally { image?.close(); reader.close() }
            }, backgroundHandler)

            val captureBuilder = camera.createCaptureRequest(
                if (controlMode == ControlMode.MANUAL) CameraDevice.TEMPLATE_MANUAL
                else CameraDevice.TEMPLATE_STILL_CAPTURE
            )
            captureBuilder.addTarget(reader.surface)
            applyAllParamsToBuilder(captureBuilder)
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90)

            session.capture(captureBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureFailed(s: CameraCaptureSession, r: CaptureRequest, f: CaptureFailure) {
                    continuation.resume(null)
                }
            }, backgroundHandler)
        } catch (e: Exception) {
            Log.e(TAG, "拍照失败", e)
            continuation.resume(null)
        }
    }

    fun getISORange(): Range<Int> = isoRange ?: Range(100, 6400)
    fun getExposureTimeRange(): Range<Long> = exposureTimeRange ?: Range(125_000L, 30_000_000_000L)
    fun getEVRange(): Range<Int> = evRange ?: Range(-3, 3)
    fun getMaxFocusDistance(): Float = maxFocusDistance
    fun isPreviewActive(): Boolean = isPreviewActive
    fun getCurrentCameraFacing(): CameraFacing = currentCameraFacing
    fun getCurrentFlashMode(): FlashMode = currentFlashMode
    fun getCurrentPreviewSize(): Size = currentPreviewSize
    fun getCurrentISO(): Int = manualISO
    fun getCurrentExposureTimeNs(): Long = manualExposureTimeNs
    fun getCurrentWhiteBalanceK(): Int = manualWhiteBalanceK
    fun getCurrentEV(): Int = evCompensation
    fun hasFrontCamera(): Boolean = getCameraId(CameraFacing.FRONT) != null
    fun hasFlash(): Boolean = try {
        val id = getCameraId(currentCameraFacing) ?: return false
        cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    } catch (e: Exception) { false }

    private fun startPreview(surface: Surface, previewSize: Size, callback: (Boolean) -> Unit) {
        val camera = cameraDevice ?: run { callback(false); return }
        try {
            imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.JPEG, 2)
            previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
                applyAllParamsToBuilder(this)
            }
            camera.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        try {
                            session.setRepeatingRequest(previewRequestBuilder!!.build(), null, backgroundHandler)
                            isPreviewActive = true
                            onPreviewStarted?.invoke()
                            callback(true)
                        } catch (e: Exception) { callback(false) }
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) { callback(false) }
                },
                backgroundHandler
            )
        } catch (e: Exception) { Log.e(TAG, "startPreview 失败", e); callback(false) }
    }

    private fun applyAllParamsToBuilder(builder: CaptureRequest.Builder? = previewRequestBuilder) {
        val b = builder ?: return
        when (controlMode) {
            ControlMode.AUTO -> {
                b.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                b.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                b.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
                b.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, evCompensation)
            }
            ControlMode.MANUAL -> {
                b.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_OFF)
                b.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                b.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF)
                b.set(CaptureRequest.SENSOR_SENSITIVITY, manualISO)
                b.set(CaptureRequest.SENSOR_EXPOSURE_TIME, manualExposureTimeNs)
                val (rGain, bGain) = kelvinToRGBGain(manualWhiteBalanceK)
                b.set(CaptureRequest.COLOR_CORRECTION_GAINS, RggbChannelVector(rGain, 1.0f, 1.0f, bGain))
                b.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX)
            }
        }
        applyFocusToBuilder(b)
        applyFlashToBuilder(b)
    }

    private fun applyFocusToBuilder(b: CaptureRequest.Builder) {
        if (isAutoFocus) {
            b.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        } else {
            b.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
            b.set(CaptureRequest.LENS_FOCUS_DISTANCE, manualFocusDistance)
        }
    }

    private fun applyFlashToBuilder(b: CaptureRequest.Builder) {
        when (currentFlashMode) {
            FlashMode.AUTO -> b.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            FlashMode.ON  -> b.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
            FlashMode.OFF -> b.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
        }
    }

    private fun refreshPreview() {
        try { captureSession?.setRepeatingRequest(previewRequestBuilder!!.build(), null, backgroundHandler) }
        catch (e: Exception) { Log.e(TAG, "refreshPreview 失败", e) }
    }

    private fun cacheHardwareCapabilities(cameraId: String) {
        try {
            val c = cameraManager.getCameraCharacteristics(cameraId)
            isoRange = c.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            exposureTimeRange = c.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            evRange = c.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
            maxFocusDistance = c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) ?: 0f
        } catch (e: Exception) { Log.e(TAG, "cacheHardwareCapabilities 失败", e) }
    }

    private fun getCameraId(facing: CameraFacing): String? {
        val target = when (facing) {
            CameraFacing.BACK -> CameraCharacteristics.LENS_FACING_BACK
            CameraFacing.FRONT -> CameraCharacteristics.LENS_FACING_FRONT
        }
        return try {
            cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == target
            }
        } catch (e: Exception) { null }
    }

    /** 色温 K → RGB Gain（Planckian locus 近似） */
    private fun kelvinToRGBGain(kelvin: Int): Pair<Float, Float> {
        val k = kelvin.toFloat()
        val rGain = if (k <= 6600f) 1.0f
        else (329.698727446f * Math.pow((k / 100.0 - 60.0), -0.1332047592).toFloat()).coerceIn(0.5f, 2.0f)
        val bGain = when {
            k <= 2000f -> 0.5f
            k >= 6600f -> 1.0f
            else -> (138.5177312231f * ln((k / 100.0 - 10.0)).toFloat() - 305.0447927307f / 255f).coerceIn(0.5f, 2.0f)
        }
        return Pair(rGain, bGain)
    }
}
