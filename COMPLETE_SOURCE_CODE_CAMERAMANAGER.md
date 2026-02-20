# CameraManager.kt - 完整源码

## 文件路径
`app/src/main/java/com/yanbao/camera/camera/CameraManager.kt`

## 技术验证要点

### 1. Camera2 API 系统服务实例化（第 44 行）
```kotlin
private val androidCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as AndroidCameraManager
```
**证明：** 使用真实的 Android Camera2 系统服务，不是模拟对象。

---

### 2. CameraX Preview 和 ImageCapture 构建（第 74-82 行）
```kotlin
preview = Preview.Builder()
    .build()
    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

imageCapture = ImageCapture.Builder()
    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
    .setFlashMode(flashModeToImageCaptureFlashMode(_cameraState.value.flashMode))
    .build()
```
**证明：** 
- `setSurfaceProvider(previewView.surfaceProvider)` 将预览绑定到真实的 Surface
- `ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY` 使用最高质量拍照模式

---

### 3. bindToLifecycle - 真实硬件绑定（第 94-99 行）
```kotlin
camera = cameraProvider?.bindToLifecycle(
    lifecycleOwner,
    cameraSelector,
    preview,
    imageCapture
)
```
**证明：** 这是 CameraX 的核心方法，将相机硬件绑定到 Activity/Fragment 生命周期。底层调用 Camera2 API 的 `CameraDevice.createCaptureSession()`。

---

### 4. 拍照并保存到 MediaStore（第 114-160 行）
```kotlin
fun takePhoto(
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    // ... 省略部分代码 ...
    
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri?.toString() ?: "相册/YanbaoAI"
                Log.d(TAG, "照片保存成功: $savedUri")  // ← 这里会输出日志
                onSuccess(savedUri)
            }
            // ...
        }
    )
}
```
**证明：** 
- 第 149 行的 `Log.d(TAG, "照片保存成功: $savedUri")` 会在 Logcat 中输出真实的保存路径
- 使用 `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` 保存到系统相册

---

### 5. Camera2 API 硬件参数查询（第 216-265 行）

#### 查询后置摄像头 ID（第 216-221 行）
```kotlin
fun getBackCameraId(): String? {
    return androidCameraManager.cameraIdList.firstOrNull { id ->
        val chars = androidCameraManager.getCameraCharacteristics(id)
        chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
    }
}
```

#### 查询 ISO 范围（第 236-240 行）
```kotlin
fun getIsoRange(cameraId: String): IntRange {
    val chars = androidCameraManager.getCameraCharacteristics(cameraId)
    val range = chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
    return if (range != null) IntRange(range.lower, range.upper) else IntRange(100, 3200)
}
```

#### 查询曝光时间范围（第 245-249 行）
```kotlin
fun getExposureTimeRange(cameraId: String): LongRange {
    val chars = androidCameraManager.getCameraCharacteristics(cameraId)
    val range = chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
    return if (range != null) LongRange(range.lower, range.upper) else LongRange(1_000_000L, 30_000_000_000L)
}
```

**证明：** 
- 这些方法直接调用 Camera2 API 的 `getCameraCharacteristics()` 查询硬件能力
- 可以在"专业模式"中使用这些参数调节 ISO、曝光时间等

---

### 6. 变焦控制（第 193-196 行）
```kotlin
fun setZoom(zoomRatio: Float) {
    camera?.cameraControl?.setZoomRatio(zoomRatio)
    _cameraState.value = _cameraState.value.copy(zoomRatio = zoomRatio)
}
```
**证明：** 
- `camera?.cameraControl?.setZoomRatio(zoomRatio)` 调用 CameraX 的变焦控制
- 底层通过 Camera2 API 的 `CaptureRequest.SCALER_CROP_REGION` 实现

---

### 7. 点击对焦（第 205-211 行）
```kotlin
fun tapToFocus(x: Float, y: Float, width: Float, height: Float) {
    val factory = SurfaceOrientedMeteringPointFactory(width, height)
    val point = factory.createPoint(x, y)
    val action = FocusMeteringAction.Builder(point).build()
    camera?.cameraControl?.startFocusAndMetering(action)
    Log.d(TAG, "点击对焦: ($x, $y)")  // ← 这里会输出日志
}
```
**证明：** 
- 第 210 行的 `Log.d` 会在 Logcat 中输出对焦坐标
- 底层通过 Camera2 API 的 `CaptureRequest.CONTROL_AF_TRIGGER` 实现

---

## 完整源码

```kotlin
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
```

---

## 技术总结

### 真实性验证
1. **Camera2 API 系统服务**：第 44 行直接调用 `context.getSystemService(Context.CAMERA_SERVICE)`
2. **硬件绑定**：第 94-99 行的 `bindToLifecycle()` 是 CameraX 的核心方法，底层调用 Camera2 API
3. **拍照日志**：第 149 行会输出 `Log.d(TAG, "照片保存成功: $savedUri")`，可以在 Logcat 中验证
4. **对焦日志**：第 210 行会输出 `Log.d(TAG, "点击对焦: ($x, $y)")`
5. **硬件参数查询**：第 216-265 行提供了完整的 Camera2 API 硬件能力查询方法

### 29D 参数映射
**注意：** 当前代码中没有直接实现 29D 参数到 Camera2 Pipeline 的映射。29D 是一个自定义的图像处理模式，需要通过以下方式实现：

1. **方案 A**：在拍照后使用 RenderScript 或 OpenGL ES 进行后处理
2. **方案 B**：使用 Camera2 API 的 `CaptureRequest.Builder` 设置自定义参数（如 ISO、曝光时间、色温）
3. **方案 C**：集成第三方图像处理库（如 OpenCV）

**当前实现状态：** 29D 参数（颜感、对比度、饱和度、色温）已在 UI 层定义（TwoDotNineDControls.kt），但尚未连接到相机硬件。需要在后续开发中实现参数映射逻辑。
