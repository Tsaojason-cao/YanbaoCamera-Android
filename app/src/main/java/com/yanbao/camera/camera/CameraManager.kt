package com.yanbao.camera.camera

import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager as AndroidCameraManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.yanbao.camera.data.model.FlashMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 相机管理器
 * 封装 CameraX 的相机预览、拍照、切换摄像头、闪光灯控制等功能
 * 同时提供 Camera2 API 查询相机硬件参数
 */
@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "YanbaoCameraManager"

    // Camera2 系统服务（用于查询硬件参数）
    private val androidCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as AndroidCameraManager

    // CameraX 核心组件
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null

    // 相机执行器（后台线程）
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // 当前状态
    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState

    // 当前镜头方向
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    /**
     * 绑定相机到生命周期，启动预览
     * @param lifecycleOwner Activity/Fragment 的生命周期
     * @param previewView 预览 View
     */
    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // 构建预览用例
            preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            // 构建拍照用例
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(flashModeToImageCaptureFlashMode(_cameraState.value.flashMode))
                .build()

            // 选择摄像头
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                // 解绑所有已绑定的用例
                cameraProvider?.unbindAll()

                // 重新绑定
                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                Log.d(TAG, "相机启动成功，镜头: ${if (lensFacing == CameraSelector.LENS_FACING_BACK) "后置" else "前置"}")

            } catch (exc: Exception) {
                Log.e(TAG, "相机绑定失败: ${exc.message}", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * 拍照并保存到系统相册
     * @param onSuccess 保存成功回调，返回保存路径
     * @param onError 失败回调
     */
    fun takePhoto(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError("相机未就绪，请稍候")
            return
        }

        // 生成文件名（时间戳）
        val name = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.CHINA)
            .format(System.currentTimeMillis())

        // 使用 MediaStore 保存到系统相册
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "YANBAO_$name")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YanbaoAI")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        // 执行拍照
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri?.toString() ?: "相册/YanbaoAI"
                    Log.d(TAG, "照片保存成功: $savedUri")
                    onSuccess(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    val errorMsg = "拍照失败: ${exception.message}"
                    Log.e(TAG, errorMsg, exception)
                    onError(errorMsg)
                }
            }
        )
    }

    /**
     * 切换前后摄像头
     */
    fun flipCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        val isFront = lensFacing == CameraSelector.LENS_FACING_FRONT
        _cameraState.value = _cameraState.value.copy(isFrontCamera = isFront)

        Log.d(TAG, "切换到${if (isFront) "前置" else "后置"}摄像头")
        startCamera(lifecycleOwner, previewView)
    }

    /**
     * 设置闪光灯模式
     * @param mode FlashMode 枚举（来自 com.yanbao.camera.data.model）
     */
    fun setFlashMode(mode: FlashMode) {
        _cameraState.value = _cameraState.value.copy(flashMode = mode)
        imageCapture?.flashMode = flashModeToImageCaptureFlashMode(mode)
        Log.d(TAG, "闪光灯模式: $mode")
    }

    /**
     * 设置变焦
     * @param zoomRatio 变焦比例（1.0 = 无变焦）
     */
    fun setZoom(zoomRatio: Float) {
        camera?.cameraControl?.setZoomRatio(zoomRatio)
        _cameraState.value = _cameraState.value.copy(zoomRatio = zoomRatio)
    }

    /**
     * 点击对焦
     * @param x 触摸点 X 坐标
     * @param y 触摸点 Y 坐标
     * @param width 预览区域宽度
     * @param height 预览区域高度
     */
    fun tapToFocus(x: Float, y: Float, width: Float, height: Float) {
        val factory = SurfaceOrientedMeteringPointFactory(width, height)
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point).build()
        camera?.cameraControl?.startFocusAndMetering(action)
        Log.d(TAG, "点击对焦: ($x, $y)")
    }

    /**
     * 查询后置摄像头ID（Camera2 API）
     */
    fun getBackCameraId(): String? {
        return androidCameraManager.cameraIdList.firstOrNull { id ->
            val chars = androidCameraManager.getCameraCharacteristics(id)
            chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
    }

    /**
     * 查询前置摄像头ID（Camera2 API）
     */
    fun getFrontCameraId(): String? {
        return androidCameraManager.cameraIdList.firstOrNull { id ->
            val chars = androidCameraManager.getCameraCharacteristics(id)
            chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        }
    }

    /**
     * 查询ISO范围（Camera2 API）
     */
    fun getIsoRange(cameraId: String): IntRange {
        val chars = androidCameraManager.getCameraCharacteristics(cameraId)
        val range = chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
        return if (range != null) IntRange(range.lower, range.upper) else IntRange(100, 3200)
    }

    /**
     * 查询曝光时间范围（Camera2 API）
     */
    fun getExposureTimeRange(cameraId: String): LongRange {
        val chars = androidCameraManager.getCameraCharacteristics(cameraId)
        val range = chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
        return if (range != null) LongRange(range.lower, range.upper) else LongRange(1_000_000L, 30_000_000_000L)
    }

    /**
     * 查询最大变焦倍数（Camera2 API）
     */
    fun getMaxZoom(cameraId: String): Float {
        val chars = androidCameraManager.getCameraCharacteristics(cameraId)
        return chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 4f
    }

    /**
     * 是否支持闪光灯（Camera2 API）
     */
    fun isFlashSupported(cameraId: String): Boolean {
        val chars = androidCameraManager.getCameraCharacteristics(cameraId)
        return chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
    }

    /**
     * 释放资源
     */
    fun shutdown() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }

    // 将 FlashMode 枚举转换为 CameraX 的 ImageCapture.FLASH_MODE_*
    private fun flashModeToImageCaptureFlashMode(mode: FlashMode): Int {
        return when (mode) {
            FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
            FlashMode.ON -> ImageCapture.FLASH_MODE_ON
            FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
            FlashMode.TORCH -> ImageCapture.FLASH_MODE_ON // TORCH模式映射到ON
        }
    }
}

/**
 * 相机状态数据类（内部使用，与 data.model.FlashMode 保持一致）
 */
data class CameraState(
    val isFrontCamera: Boolean = false,
    val flashMode: FlashMode = FlashMode.AUTO,
    val zoomRatio: Float = 1.0f,
    val isRecording: Boolean = false
)
