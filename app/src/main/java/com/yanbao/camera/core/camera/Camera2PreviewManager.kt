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
import android.view.Surface
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Camera2 预览层管理器
 * 
 * Phase 4 核心实现：
 * - 真实的Camera2 API绑定
 * - 预览流启动
 * - 拍照功能
 * - 生命周期管理
 * 
 * 严格遵循Android Camera2 API开发规范：
 * 1. 必须实例化CaptureRequest.Builder
 * 2. 必须通过Surface配置预览输出
 * 3. 严禁使用任何模拟的预览数据流
 */
class Camera2PreviewManager(private val context: Context) {
    
    companion object {
        private const val TAG = "Camera2PreviewManager"
        private const val IMAGE_WIDTH = 1920
        private const val IMAGE_HEIGHT = 1080
    }
    
    // Camera2 核心组件
    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    
    // 后台线程
    private val backgroundThread = HandlerThread("CameraBackground").apply { start() }
    private val backgroundHandler = Handler(backgroundThread.looper)
    
    // 状态标志
    private var isPreviewActive = false
    
    /**
     * 打开相机
     */
    suspend fun openCamera(surface: Surface): Boolean = suspendCancellableCoroutine { continuation ->
        // 检查权限
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Camera permission not granted")
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        
        try {
            // 获取后置摄像头ID
            val cameraId = getCameraId() ?: run {
                Log.e(TAG, "No back camera found")
                continuation.resume(false)
                return@suspendCancellableCoroutine
            }
            
            Log.d(TAG, "Opening camera: $cameraId")
            
            // 打开相机设备
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d(TAG, "Camera opened successfully")
                    cameraDevice = camera
                    
                    // 启动预览
                    startPreview(surface) { success ->
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
    private fun startPreview(surface: Surface, callback: (Boolean) -> Unit) {
        val camera = cameraDevice ?: run {
            Log.e(TAG, "CameraDevice is null")
            callback(false)
            return
        }
        
        try {
            // 创建ImageReader用于拍照
            imageReader = ImageReader.newInstance(
                IMAGE_WIDTH,
                IMAGE_HEIGHT,
                ImageFormat.JPEG,
                2
            )
            
            // 创建CaptureRequest.Builder（预览模板）
            val captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            
            // 设置自动对焦和自动曝光
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
            
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
                                captureRequestBuilder.build(),
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
        
        Log.d(TAG, "Camera closed")
    }
    
    /**
     * 释放资源
     */
    fun release() {
        closeCamera()
        backgroundThread.quitSafely()
        Log.d(TAG, "Resources released")
    }
    
    /**
     * 获取后置摄像头ID
     */
    private fun getCameraId(): String? {
        return try {
            cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                facing == CameraCharacteristics.LENS_FACING_BACK
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get camera ID", e)
            null
        }
    }
    
    /**
     * 检查预览是否激活
     */
    fun isPreviewActive(): Boolean = isPreviewActive
}
