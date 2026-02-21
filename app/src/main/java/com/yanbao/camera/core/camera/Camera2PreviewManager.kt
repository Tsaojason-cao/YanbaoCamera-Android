package com.yanbao.camera.core.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Camera2 预览层管理器（优化版）
 * 
 * 新增功能：
 * - 前后摄像头切换
 * - 闪光灯控制（自动/开/关）
 * - 预览尺寸选择
 * 
 * 严格遵循Android Camera2 API开发规范：
 * 1. 必须实例化CaptureRequest.Builder
 * 2. 必须通过Surface配置预览输出
 * 3. 严禁使用任何模拟的预览数据流
 */
class Camera2PreviewManager(private val context: Context) {
    
    companion object {
        private const val TAG = "Camera2PreviewManager"
        
        // 预设分辨率
        val PREVIEW_SIZES = listOf(
            Size(1920, 1080),  // Full HD
            Size(1280, 720),   // HD
            Size(640, 480)     // VGA
        )
    }
    
    // 闪光灯模式
    enum class FlashMode {
        AUTO,   // 自动
        ON,     // 开启
        OFF     // 关闭
    }
    
    // 摄像头朝向
    enum class CameraFacing {
        BACK,   // 后置
        FRONT   // 前置
    }
    
    // Camera2 核心组件
    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    
    // 后台线程
    private val backgroundThread = HandlerThread("CameraBackground").apply { start() }
    private val backgroundHandler = Handler(backgroundThread.looper)
    
    // 状态标志
    private var isPreviewActive = false
    private var currentCameraFacing = CameraFacing.BACK
    private var currentFlashMode = FlashMode.AUTO
    private var currentPreviewSize = PREVIEW_SIZES[0]
    private var currentSurface: Surface? = null
    
    /**
     * 打开相机
     */
    suspend fun openCamera(
        surface: Surface,
        facing: CameraFacing = CameraFacing.BACK,
        previewSize: Size = PREVIEW_SIZES[0]
    ): Boolean = suspendCancellableCoroutine { continuation ->
        // 检查权限
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Camera permission not granted")
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        
        try {
            // 保存参数
            currentCameraFacing = facing
            currentPreviewSize = previewSize
            currentSurface = surface
            
            // 获取摄像头ID
            val cameraId = getCameraId(facing) ?: run {
                Log.e(TAG, "No ${facing.name} camera found")
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }
            
            Log.d(TAG, "Opening camera: $cameraId (${facing.name}, ${previewSize.width}x${previewSize.height})")
            
            // 打开相机设备
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d(TAG, "Camera opened successfully")
                    cameraDevice = camera
                    
                    // 启动预览
                    startPreview(surface, previewSize) { success ->
                        continuation.resume(success)
                    }
                }
                
                override fun onDisconnected(camera: CameraDevice) {
                    Log.w(TAG, "Camera disconnected")
                    camera.close()
                    cameraDevice = null
                    continuation.resume(false)
                }
                
                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(TAG, "Camera error: $error")
                    camera.close()
                    cameraDevice = null
                    continuation.resumeWithException(RuntimeException("Camera error: $error"))
                }
            }, backgroundHandler)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open camera", e)
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * 启动预览流
     */
    private fun startPreview(surface: Surface, previewSize: Size, callback: (Boolean) -> Unit) {
        val camera = cameraDevice ?: run {
            Log.e(TAG, "CameraDevice is null")
            callback(false)
            return
        }
        
        try {
            // 创建ImageReader用于拍照
            imageReader = ImageReader.newInstance(
                previewSize.width,
                previewSize.height,
                ImageFormat.JPEG,
                2
            )
            
            // 创建CaptureRequest.Builder（预览模板）
            captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)
            
            // 设置自动对焦
            captureRequestBuilder?.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            
            // 设置自动曝光
            captureRequestBuilder?.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
            
            // 应用闪光灯模式
            applyFlashMode(currentFlashMode)
            
            Log.d(TAG, "Creating capture session...")
            
            // 创建CameraCaptureSession
            camera.createCaptureSession(
                listOf(surface, imageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        Log.d(TAG, "Capture session configured")
                        captureSession = session
                        
                        try {
                            // 开始重复请求（预览流）
                            session.setRepeatingRequest(
                                captureRequestBuilder!!.build(),
                                null,
                                backgroundHandler
                            )
                            
                            isPreviewActive = true
                            Log.d(TAG, "Preview started successfully")
                            callback(true)
                            
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to start preview", e)
                            callback(false)
                        }
                    }
                    
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Capture session configuration failed")
                        callback(false)
                    }
                },
                backgroundHandler
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start preview", e)
            callback(false)
        }
    }
    
    /**
     * 切换摄像头
     */
    suspend fun switchCamera(): Boolean {
        val newFacing = if (currentCameraFacing == CameraFacing.BACK) {
            CameraFacing.FRONT
        } else {
            CameraFacing.BACK
        }
        
        Log.d(TAG, "Switching camera to ${newFacing.name}")
        
        // 关闭当前相机
        closeCamera()
        
        // 打开新相机
        val surface = currentSurface ?: run {
            Log.e(TAG, "Surface is null")
            return false
        }
        
        return openCamera(surface, newFacing, currentPreviewSize)
    }
    
    /**
     * 设置闪光灯模式
     */
    fun setFlashMode(mode: FlashMode) {
        Log.d(TAG, "Setting flash mode to ${mode.name}")
        currentFlashMode = mode
        applyFlashMode(mode)
        updatePreview()
    }
    
    /**
     * 应用闪光灯模式
     */
    private fun applyFlashMode(mode: FlashMode) {
        val builder = captureRequestBuilder ?: return
        
        when (mode) {
            FlashMode.AUTO -> {
                builder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                builder.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF
                )
            }
            FlashMode.ON -> {
                builder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON
                )
                builder.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_TORCH
                )
            }
            FlashMode.OFF -> {
                builder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON
                )
                builder.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF
                )
            }
        }
    }
    
    /**
     * 设置预览尺寸
     */
    suspend fun setPreviewSize(size: Size): Boolean {
        Log.d(TAG, "Setting preview size to ${size.width}x${size.height}")
        currentPreviewSize = size
        
        // 重新打开相机
        val surface = currentSurface ?: run {
            Log.e(TAG, "Surface is null")
            return false
        }
        
        closeCamera()
        return openCamera(surface, currentCameraFacing, size)
    }
    
    /**
     * 更新预览
     */
    private fun updatePreview() {
        val session = captureSession ?: return
        val builder = captureRequestBuilder ?: return
        
        try {
            session.setRepeatingRequest(
                builder.build(),
                null,
                backgroundHandler
            )
            Log.d(TAG, "Preview updated")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update preview", e)
        }
    }
    
    /**
     * 拍照
     */
    suspend fun takePicture(): Bitmap? = suspendCancellableCoroutine { continuation ->
        val camera = cameraDevice ?: run {
            Log.e(TAG, "CameraDevice is null")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        
        val session = captureSession ?: run {
            Log.e(TAG, "CaptureSession is null")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        
        val reader = imageReader ?: run {
            Log.e(TAG, "ImageReader is null")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        
        try {
            Log.d(TAG, "Taking picture...")
            
            // 设置ImageReader监听器
            reader.setOnImageAvailableListener({ imageReader ->
                val image = imageReader.acquireLatestImage()
                if (image != null) {
                    try {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        Log.d(TAG, "Picture captured: ${bitmap.width}x${bitmap.height}")
                        continuation.resume(bitmap)
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to decode image", e)
                        continuation.resume(null)
                    } finally {
                        image.close()
                    }
                } else {
                    Log.e(TAG, "Image is null")
                    continuation.resume(null)
                }
            }, backgroundHandler)
            
            // 创建拍照CaptureRequest
            val captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureRequestBuilder.addTarget(reader.surface)
            
            // 设置自动对焦和自动曝光
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
            
            // 应用闪光灯模式
            when (currentFlashMode) {
                FlashMode.ON -> {
                    captureRequestBuilder.set(
                        CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_SINGLE
                    )
                }
                else -> {
                    // AUTO和OFF使用默认设置
                }
            }
            
            // 执行拍照
            session.capture(
                captureRequestBuilder.build(),
                object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                    ) {
                        Log.d(TAG, "Capture completed")
                    }
                    
                    override fun onCaptureFailed(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        failure: CaptureFailure
                    ) {
                        Log.e(TAG, "Capture failed: ${failure.reason}")
                        continuation.resume(null)
                    }
                },
                backgroundHandler
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to take picture", e)
            continuation.resume(null)
        }
    }
    
    /**
     * 关闭相机
     */
    fun closeCamera() {
        Log.d(TAG, "Closing camera...")
        
        isPreviewActive = false
        
        captureSession?.close()
        captureSession = null
        
        cameraDevice?.close()
        cameraDevice = null
        
        imageReader?.close()
        imageReader = null
        
        captureRequestBuilder = null
        
        Log.d(TAG, "Camera closed")
    }
    
    /**
     * 释放资源
     */
    fun release() {
        closeCamera()
        backgroundThread.quitSafely()
        currentSurface = null
        Log.d(TAG, "Resources released")
    }
    
    /**
     * 获取摄像头ID
     */
    private fun getCameraId(facing: CameraFacing): String? {
        return try {
            val targetFacing = when (facing) {
                CameraFacing.BACK -> CameraCharacteristics.LENS_FACING_BACK
                CameraFacing.FRONT -> CameraCharacteristics.LENS_FACING_FRONT
            }
            
            cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                lensFacing == targetFacing
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get camera ID", e)
            null
        }
    }
    
    /**
     * 获取当前摄像头朝向
     */
    fun getCurrentCameraFacing(): CameraFacing = currentCameraFacing
    
    /**
     * 获取当前闪光灯模式
     */
    fun getCurrentFlashMode(): FlashMode = currentFlashMode
    
    /**
     * 获取当前预览尺寸
     */
    fun getCurrentPreviewSize(): Size = currentPreviewSize
    
    /**
     * 检查预览是否激活
     */
    fun isPreviewActive(): Boolean = isPreviewActive
    
    /**
     * 检查是否有前置摄像头
     */
    fun hasFrontCamera(): Boolean {
        return getCameraId(CameraFacing.FRONT) != null
    }
    
    /**
     * 检查是否支持闪光灯
     */
    fun hasFlash(): Boolean {
        return try {
            val cameraId = getCameraId(currentCameraFacing) ?: return false
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check flash support", e)
            false
        }
    }
}
