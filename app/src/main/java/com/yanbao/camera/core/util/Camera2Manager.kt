package com.yanbao.camera.core.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

/**
 * Camera2 管理器 - 工业级相机实现
 * 
 * 核心功能:
 * 1. 使用 Camera2 API 手动控制相机硬件
 * 2. 支持预览（TEMPLATE_PREVIEW）和拍照（TEMPLATE_STILL_CAPTURE）
 * 3. 通过 ImageReader 获取 JPEG 数据
 * 4. 支持切换前后摄像头
 * 5. 支持闪光灯控制
 * 
 * 技术要点:
 * - CameraDevice.createCaptureRequest() 创建捕获请求
 * - CaptureRequest.Builder.addTarget() 添加输出目标
 * - CameraCaptureSession.setRepeatingRequest() 持续预览
 * - ImageReader.OnImageAvailableListener 获取图像数据
 */
class Camera2Manager(private val context: Context) {
    
    companion object {
        private const val TAG = "Camera2Manager"
        private const val MAX_PREVIEW_WIDTH = 1920
        private const val MAX_PREVIEW_HEIGHT = 1080
    }
    
    // Camera2 核心组件
    private var cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    
    // 相机参数
    private var cameraId: String = "0" // 默认后置摄像头
    private var previewSize: Size = Size(1920, 1080)
    private var flashMode: Int = CaptureRequest.FLASH_MODE_OFF
    
    // 模式参数
    private var currentMode: String = "PHOTO"
    private var portraitBlurStrength: Float = 0.5f  // 人像模式虚化强度
    private var filterEffect: Int = CaptureRequest.CONTROL_EFFECT_MODE_OFF  // 滤镜效果
    
    // 后台线程
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    
    // 回调
    var onPreviewSurfaceReady: ((Surface) -> Unit)? = null
    var onPhotoSaved: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    
    // MediaRecorder 录像
    private var mediaRecorder: android.media.MediaRecorder? = null
    private var isRecording: Boolean = false
    var onRecordingStarted: (() -> Unit)? = null
    var onRecordingStopped: ((String) -> Unit)? = null
    
    /**
     * 启动后台线程
     */
    private fun startBackgroundThread() {
        Log.d(TAG, "启动后台线程")
        backgroundThread = HandlerThread("Camera2Background").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }
    
    /**
     * 停止后台线程
     */
    private fun stopBackgroundThread() {
        Log.d(TAG, "停止后台线程")
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, "停止后台线程失败", e)
        }
    }
    
    /**
     * 打开相机
     */
    fun openCamera(surface: Surface) {
        Log.d(TAG, "打开相机: cameraId=$cameraId")
        
        // 检查权限
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "没有相机权限")
            onError?.invoke("没有相机权限")
            return
        }
        
        // 启动后台线程
        startBackgroundThread()
        
        // 获取相机特性
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        
        // 选择预览尺寸
        previewSize = chooseOptimalSize(
            map?.getOutputSizes(SurfaceTexture::class.java) ?: emptyArray(),
            MAX_PREVIEW_WIDTH,
            MAX_PREVIEW_HEIGHT
        )
        Log.d(TAG, "预览尺寸: ${previewSize.width}x${previewSize.height}")
        
        // 创建 ImageReader
        imageReader = ImageReader.newInstance(
            previewSize.width,
            previewSize.height,
            ImageFormat.JPEG,
            2
        ).apply {
            setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
        }
        
        // 打开相机设备
        try {
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "打开相机失败", e)
            onError?.invoke("打开相机失败: ${e.message}")
        } catch (e: SecurityException) {
            Log.e(TAG, "没有相机权限", e)
            onError?.invoke("没有相机权限")
        }
    }
    
    /**
     * 相机设备状态回调
     */
    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "相机已打开")
            cameraDevice = camera
            createCameraPreviewSession()
        }
        
        override fun onDisconnected(camera: CameraDevice) {
            Log.w(TAG, "相机已断开")
            cameraDevice?.close()
            cameraDevice = null
        }
        
        override fun onError(camera: CameraDevice, error: Int) {
            Log.e(TAG, "相机错误: $error")
            cameraDevice?.close()
            cameraDevice = null
            onError?.invoke("相机错误: $error")
        }
    }
    
    /**
     * 创建相机预览会话
     */
    private fun createCameraPreviewSession() {
        Log.d(TAG, "创建相机预览会话")
        
        val device = cameraDevice
        val reader = imageReader
        
        if (device == null || reader == null) {
            Log.e(TAG, "CameraDevice 或 ImageReader 为空")
            return
        }
        
        try {
            // 创建预览请求构建器（关键：使用 TEMPLATE_PREVIEW）
            val previewRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            
            // 添加预览输出目标
            val previewSurface = reader.surface
            previewRequestBuilder.addTarget(previewSurface)
            
            // 设置自动对焦模式
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            
            // 设置自动曝光模式
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
            
            // 设置闪光灯模式
            previewRequestBuilder.set(CaptureRequest.FLASH_MODE, flashMode)
            
            // 应用模式参数
            applyModeParameters(previewRequestBuilder)
            
            // 创建捕获会话
            device.createCaptureSession(
                listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        Log.d(TAG, "捕获会话已配置")
                        
                        if (cameraDevice == null) {
                            Log.w(TAG, "CameraDevice 已关闭")
                            return
                        }
                        
                        cameraCaptureSession = session
                        
                        try {
                            // 开始持续预览（关键：setRepeatingRequest）
                            session.setRepeatingRequest(
                                previewRequestBuilder.build(),
                                null,
                                backgroundHandler
                            )
                            Log.d(TAG, "预览已启动")
                            
                            // 通知预览 Surface 已准备
                            onPreviewSurfaceReady?.invoke(previewSurface)
                        } catch (e: CameraAccessException) {
                            Log.e(TAG, "启动预览失败", e)
                            onError?.invoke("启动预览失败: ${e.message}")
                        }
                    }
                    
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "捕获会话配置失败")
                        onError?.invoke("捕获会话配置失败")
                    }
                },
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "创建预览会话失败", e)
            onError?.invoke("创建预览会话失败: ${e.message}")
        }
    }
    
    /**
     * 拍照
     */
    fun takePhoto() {
        Log.d(TAG, "开始拍照")
        
        val device = cameraDevice
        val reader = imageReader
        
        if (device == null || reader == null) {
            Log.e(TAG, "CameraDevice 或 ImageReader 为空")
            onError?.invoke("相机未初始化")
            return
        }
        
        try {
            // 创建拍照请求构建器（关键：使用 TEMPLATE_STILL_CAPTURE）
            val captureBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            
            // 添加拍照输出目标
            captureBuilder.addTarget(reader.surface)
            
            // 设置自动对焦模式
            captureBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            
            // 设置自动曝光模式
            captureBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
            
            // 设置闪光灯模式
            captureBuilder.set(CaptureRequest.FLASH_MODE, flashMode)
            
            // 设置 JPEG 方向（根据相机传感器方向）
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, sensorOrientation)
            
            // 执行拍照
            cameraCaptureSession?.capture(
                captureBuilder.build(),
                object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                    ) {
                        Log.d(TAG, "拍照完成")
                    }
                },
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "拍照失败", e)
            onError?.invoke("拍照失败: ${e.message}")
        }
    }
    
    /**
     * ImageReader 回调 - 获取 JPEG 数据
     */
    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        Log.d(TAG, "图像数据可用")
        
        val image = reader.acquireNextImage()
        if (image != null) {
            backgroundHandler?.post {
                saveImage(image)
                image.close()
            }
        }
    }
    
    /**
     * 保存图像到 MediaStore
     */
    private fun saveImage(image: Image) {
        Log.d(TAG, "保存图像")
        
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        
        // 生成文件名
        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val filename = "YanbaoCamera_$timestamp.jpg"
        
        // 保存到 MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/YanbaoCamera")
        }
        
        try {
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(bytes)
                    outputStream.flush()
                }
                Log.d(TAG, "图像已保存: $uri")
                onPhotoSaved?.invoke(uri.toString())
            } else {
                Log.e(TAG, "创建 MediaStore URI 失败")
                onError?.invoke("保存图像失败")
            }
        } catch (e: Exception) {
            Log.e(TAG, "保存图像失败", e)
            onError?.invoke("保存图像失败: ${e.message}")
        }
    }
    
    /**
     * 切换摄像头
     */
    fun switchCamera() {
        Log.d(TAG, "切换摄像头")
        
        // 关闭当前相机
        closeCamera()
        
        // 切换相机 ID
        cameraId = if (cameraId == "0") "1" else "0"
        
        // 重新打开相机
        // 注意：需要外部重新调用 openCamera()
    }
    
    /**
     * 设置闪光灯模式
     */
    fun setFlashMode(mode: Int) {
        Log.d(TAG, "设置闪光灯模式: $mode")
        flashMode = mode
    }
    
    /**
     * 设置相机模式
     */
    fun setMode(mode: String) {
        Log.d(TAG, "设置相机模式: $mode")
        currentMode = mode
    }
    
    /**
     * 设置人像模式虚化强度
     */
    fun setPortraitBlurStrength(strength: Float) {
        Log.d(TAG, "设置虚化强度: $strength")
        portraitBlurStrength = strength
    }
    
    /**
     * 设置滤镜效果
     */
    fun setFilterEffect(effect: Int) {
        Log.d(TAG, "设置滤镜效果: $effect")
        filterEffect = effect
    }
    
    /**
     * 关闭相机
     */
    fun closeCamera() {
        Log.d(TAG, "关闭相机")
        
        cameraCaptureSession?.close()
        cameraCaptureSession = null
        
        cameraDevice?.close()
        cameraDevice = null
        
        imageReader?.close()
        imageReader = null
        
        stopBackgroundThread()
    }
    
    /**
     * 选择最优预览尺寸
     */
    private fun chooseOptimalSize(
        choices: Array<Size>,
        maxWidth: Int,
        maxHeight: Int
    ): Size {
        val bigEnough = mutableListOf<Size>()
        
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight) {
                bigEnough.add(option)
            }
        }
        
        return if (bigEnough.isNotEmpty()) {
            bigEnough.maxByOrNull { it.width * it.height } ?: choices[0]
        } else {
            choices[0]
        }
    }
    
    /**
     * 应用模式参数到 CaptureRequest
     */
    private fun applyModeParameters(builder: CaptureRequest.Builder) {
        when (currentMode) {
            "PORTRAIT" -> {
                // 人像模式：使用 SEPIA 效果模拟美颜
                builder.set(
                    CaptureRequest.CONTROL_EFFECT_MODE,
                    CaptureRequest.CONTROL_EFFECT_MODE_SEPIA
                )
                Log.d(TAG, "应用人像模式参数")
            }
            "NIGHT" -> {
                // 夜景模式：设置长曝光时间 (500ms)
                builder.set(
                    CaptureRequest.SENSOR_EXPOSURE_TIME,
                    500_000_000L  // 500ms = 0.5s
                )
                // 锁定 ISO
                builder.set(
                    CaptureRequest.SENSOR_SENSITIVITY,
                    800  // ISO 800
                )
                // 关闭自动曝光
                builder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_OFF
                )
                Log.d(TAG, "应用夜景模式参数: 曝光=500ms, ISO=800")
            }
            "MASTER_FILTERS" -> {
                // 大师滤镜：应用选定的滤镜效果
                builder.set(
                    CaptureRequest.CONTROL_EFFECT_MODE,
                    filterEffect
                )
                Log.d(TAG, "应用滤镜效果: $filterEffect")
            }
            else -> {
                // 默认模式：不应用特殊效果
                builder.set(
                    CaptureRequest.CONTROL_EFFECT_MODE,
                    CaptureRequest.CONTROL_EFFECT_MODE_OFF
                )
            }
        }
    }
}
