package com.yanbao.camera.core.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 视频录制管理器
 * 
 * 使用 MediaRecorder 实现真实的视频录制功能
 * 
 * 核心功能:
 * 1. 配置 MediaRecorder
 * 2. 绑定到 CameraDevice 的输出 Surface
 * 3. 开始/停止录制
 * 4. 保存到 MediaStore
 */
class VideoRecorder(private val context: Context) {
    
    companion object {
        private const val TAG = "VideoRecorder"
    }
    
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording: Boolean = false
    private var outputFilePath: String? = null
    
    // 回调
    var onRecordingStarted: (() -> Unit)? = null
    var onRecordingStopped: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    
    /**
     * 准备 MediaRecorder
     * 
     * @return MediaRecorder 的 Surface，用于绑定到 CameraDevice
     */
    fun prepareRecorder(width: Int = 1920, height: Int = 1080): Surface? {
        try {
            // 创建临时文件
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val videoFile = File(context.getExternalFilesDir(null), "VIDEO_$timestamp.mp4")
            outputFilePath = videoFile.absolutePath
            
            // 创建 MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                // 设置音频源
                setAudioSource(MediaRecorder.AudioSource.MIC)
                
                // 设置视频源
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                
                // 设置输出格式
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                
                // 设置音频编码器
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                
                // 设置视频编码器
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                
                // 设置视频尺寸
                setVideoSize(width, height)
                
                // 设置视频帧率
                setVideoFrameRate(30)
                
                // 设置视频比特率
                setVideoEncodingBitRate(10_000_000)  // 10 Mbps
                
                // 设置输出文件
                setOutputFile(videoFile.absolutePath)
                
                // 准备录制
                prepare()
                
                Log.d(TAG, "MediaRecorder 已准备: ${videoFile.absolutePath}")
            }
            
            return mediaRecorder?.surface
            
        } catch (e: Exception) {
            Log.e(TAG, "准备 MediaRecorder 失败", e)
            onError?.invoke("准备录制失败: ${e.message}")
            releaseRecorder()
            return null
        }
    }
    
    /**
     * 开始录制
     */
    fun startRecording() {
        if (isRecording) {
            Log.w(TAG, "已经在录制中")
            return
        }
        
        try {
            mediaRecorder?.start()
            isRecording = true
            
            Log.d(TAG, "开始录制")
            AuditLogger.logRecordingStarted()
            onRecordingStarted?.invoke()
            
        } catch (e: Exception) {
            Log.e(TAG, "开始录制失败", e)
            onError?.invoke("开始录制失败: ${e.message}")
            releaseRecorder()
        }
    }
    
    /**
     * 停止录制
     */
    fun stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "没有在录制中")
            return
        }
        
        try {
            mediaRecorder?.stop()
            isRecording = false
            
            val filePath = outputFilePath
            if (filePath != null) {
                // 保存到 MediaStore
                saveToMediaStore(filePath)
                
                Log.d(TAG, "停止录制: $filePath")
                AuditLogger.logRecordingStopped(filePath)
                onRecordingStopped?.invoke(filePath)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "停止录制失败", e)
            onError?.invoke("停止录制失败: ${e.message}")
        } finally {
            releaseRecorder()
        }
    }
    
    /**
     * 释放 MediaRecorder
     */
    fun releaseRecorder() {
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            
            Log.d(TAG, "MediaRecorder 已释放")
            
        } catch (e: Exception) {
            Log.e(TAG, "释放 MediaRecorder 失败", e)
        }
    }
    
    /**
     * 保存视频到 MediaStore
     */
    private fun saveToMediaStore(filePath: String) {
        try {
            val videoFile = File(filePath)
            if (!videoFile.exists()) {
                Log.e(TAG, "视频文件不存在: $filePath")
                return
            }
            
            // 创建 ContentValues
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/YanbaoCamera")
            }
            
            // 插入到 MediaStore
            val uri = context.contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                values
            )
            
            if (uri != null) {
                // 复制文件内容
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    videoFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                Log.d(TAG, "✅ 视频已保存到 MediaStore: $uri")
            } else {
                Log.e(TAG, "❌ 保存视频到 MediaStore 失败")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "保存视频到 MediaStore 失败", e)
        }
    }
}
