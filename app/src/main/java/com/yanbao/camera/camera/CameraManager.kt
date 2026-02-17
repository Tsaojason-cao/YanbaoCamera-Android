package com.yanbao.camera.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraManager - 管理相机功能
 * 负责相机预览、拍照、切换摄像头等功能
 */
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    private var currentLensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    
    companion object {
        private const val TAG = "CameraManager"
    }
    
    /**
     * 初始化相机
     */
    fun initializeCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.result
                bindCameraUseCases(previewView)
            } catch (exc: Exception) {
                Log.e(TAG, "Camera initialization failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    /**
     * 绑定相机用例
     */
    private fun bindCameraUseCases(previewView: PreviewView) {
        val cameraProvider = cameraProvider ?: return
        
        // 创建Preview用例
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(previewView.display.rotation)
            .build()
        
        // 创建ImageCapture用例
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(previewView.display.rotation)
            .build()
        
        // 创建CameraSelector
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(currentLensFacing)
            .build()
        
        try {
            // 解绑所有用例
            cameraProvider.unbindAll()
            
            // 绑定用例到生命周期
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            
            // 连接预览
            preview?.setSurfaceProvider(previewView.surfaceProvider)
            
            Log.d(TAG, "Camera binding successful")
        } catch (exc: Exception) {
            Log.e(TAG, "Camera binding failed", exc)
        }
    }
    
    /**
     * 拍照
     */
    fun takePhoto(
        outputFile: java.io.File,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val imageCapture = imageCapture ?: return
        
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo saved: ${output.savedUri}")
                    onSuccess(output.savedUri?.toString() ?: outputFile.absolutePath)
                }
                
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exc)
                    onError(exc)
                }
            }
        )
    }
    
    /**
     * 切换摄像头
     */
    fun switchCamera(previewView: PreviewView) {
        currentLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        bindCameraUseCases(previewView)
    }
    
    /**
     * 设置闪光灯模式
     */
    fun setFlashMode(mode: Int) {
        flashMode = mode
        imageCapture?.flashMode = mode
    }
    
    /**
     * 获取当前闪光灯模式
     */
    fun getFlashMode(): Int = flashMode
    
    /**
     * 获取当前摄像头方向
     */
    fun getCurrentLensFacing(): Int = currentLensFacing
    
    /**
     * 释放资源
     */
    fun release() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}
