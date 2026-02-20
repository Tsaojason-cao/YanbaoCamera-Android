package com.yanbao.camera.core.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 相机管理器
 * 
 * Phase 1: 使用 CameraX 进行快速验证
 * Phase 3: 切换到 Camera2 API 进行深度控制
 * 
 * 设计原则：
 * - 预留 CameraControl 和 CameraInfo 接口
 * - 确保 Phase 3 切换时 UI 层无需重构
 */
class CameraManager {
    
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    // Phase 3 预留接口：Camera2 深度控制
    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
    
    /**
     * 启动相机预览
     * 
     * @param context Android Context
     * @param lifecycleOwner Lifecycle Owner
     * @param previewView PreviewView
     * @param lensFacing 镜头方向（前置/后置）
     */
    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                // 预览
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // 拍照
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()
                
                // 镜头选择
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                
                // 解绑所有用例
                cameraProvider?.unbindAll()
                
                // 绑定生命周期
                val camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                
                // Phase 3 预留接口：保存 CameraControl 和 CameraInfo
                cameraControl = camera?.cameraControl
                cameraInfo = camera?.cameraInfo
                
                Log.d(TAG, "Camera started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    /**
     * 拍照并保存到 MediaStore
     * 
     * @param context Android Context
     * @param camera29DStateJson 29D 参数 JSON（用于写入数据库）
     * @param cameraMode 拍摄模式
     * @param onResult 回调函数（成功/失败，Uri/错误信息）
     */
    fun takePhoto(
        context: Context,
        camera29DStateJson: String = "{}",
        cameraMode: String = "Photo",
        onResult: (Boolean, String, Uri?) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onResult(false, "Camera not initialized", null)
            return
        }
        
        // 生成文件名
        val timestamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val fileName = "IMG_$timestamp.jpg"
        
        // 创建 ContentValues（保存到 MediaStore）
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/YanbaoCamera")
        }
        
        // 输出选项
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()
        
        // 拍照
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    Log.d(TAG, "Photo saved: $savedUri")
                    
                    // 写入 YanbaoMemory 数据库
                    savedUri?.let { uri ->
                        saveToYanbaoMemoryDatabase(
                            context = context,
                            imagePath = uri.toString(),
                            camera29DStateJson = camera29DStateJson,
                            cameraMode = cameraMode
                        )
                    }
                    
                    ContextCompat.getMainExecutor(context).execute {
                        Toast.makeText(
                            context,
                            "照片已保存: $fileName",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                    onResult(true, savedUri.toString(), savedUri)
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exception)
                    
                    ContextCompat.getMainExecutor(context).execute {
                        Toast.makeText(
                            context,
                            "拍照失败: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                    onResult(false, exception.message ?: "Unknown error", null)
                }
            }
        )
    }
    
    /**
     * Phase 3 预留接口：获取 CameraControl
     * 用于深度控制相机参数（ISO、快门、白平衡等）
     */
    fun getCameraControl(): CameraControl? = cameraControl
    
    /**
     * Phase 3 预留接口：获取 CameraInfo
     * 用于查询相机能力和状态
     */
    fun getCameraInfo(): CameraInfo? = cameraInfo
    
    /**
     * 关闭相机
     */
    fun shutdown() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
    
    /**
     * 保存到 YanbaoMemory 数据库
     */
    private fun saveToYanbaoMemoryDatabase(
        context: Context,
        imagePath: String,
        camera29DStateJson: String,
        cameraMode: String
    ) {
        try {
            val database = com.yanbao.camera.data.local.YanbaoMemoryDatabase.getDatabase(context)
            val dao = database.yanbaoMemoryDao()
            
            // 创建 YanbaoMemory 实例（使用 Mock 数据）
            val memory = com.yanbao.camera.data.local.entity.YanbaoMemoryFactory.createMock(
                imagePath = imagePath,
                parameterSnapshotJson = camera29DStateJson
            ).copy(shootingMode = cameraMode)
            
            // 异步写入数据库
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val id = dao.insert(memory)
                Log.d(TAG, "YanbaoMemory saved to database with ID: $id")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save YanbaoMemory to database", e)
        }
    }
    
    companion object {
        private const val TAG = "CameraManager"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}
