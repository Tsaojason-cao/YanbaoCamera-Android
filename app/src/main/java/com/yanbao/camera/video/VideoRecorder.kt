package com.yanbao.camera.video

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Phase 2: 视频大师录像器
 *
 * 封装 MediaRecorder，支持：
 * - 4K/1080P/720P 多分辨率
 * - 可变帧率（24/30/60fps）
 * - 延时摄影（通过 [TimelapseRecorder] 扩展）
 * - 录像时长计时
 * - 自动保存到 MediaStore（Android 10+）或 DCIM 目录
 *
 * 使用方式：
 * ```kotlin
 * val recorder = VideoRecorder(context)
 * val surface = recorder.prepare(quality = VideoQuality.FHD_30)
 * camera2Manager.addVideoSurface(surface)
 * recorder.start()
 * // ...
 * val savedPath = recorder.stop()
 * ```
 */
class VideoRecorder(private val context: Context) {

    companion object {
        private const val TAG = "VideoRecorder"
        private const val MIME_TYPE = "video/mp4"
        private const val DATE_FORMAT = "yyyyMMdd_HHmmss"
    }

    // ─── 状态 ─────────────────────────────────────────────────────────────

    enum class RecordState { IDLE, PREPARED, RECORDING, PAUSED, STOPPED }

    private val _state = MutableStateFlow(RecordState.IDLE)
    val state: StateFlow<RecordState> = _state.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var outputFilePath: String = ""
    private var startTimeMs: Long = 0L
    private var durationTimer: java.util.Timer? = null

    // ─── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 准备录像，返回用于 Camera2 预览的 Surface
     * @param quality 录像质量
     * @param audioEnabled 是否录制音频
     * @return 输入 Surface（传给 Camera2 的 createCaptureSession）
     */
    fun prepare(
        quality: VideoQuality = VideoQuality.FHD_30,
        audioEnabled: Boolean = true
    ): Surface {
        release()

        outputFilePath = createOutputFilePath()

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            if (audioEnabled) {
                setAudioSource(MediaRecorder.AudioSource.MIC)
            }
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            if (audioEnabled) {
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128_000)
                setAudioSamplingRate(44_100)
            }
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoSize(quality.width, quality.height)
            setVideoFrameRate(quality.fps)
            setVideoEncodingBitRate(quality.bitrate)
            setOutputFile(outputFilePath)
            prepare()
        }

        _state.value = RecordState.PREPARED
        Log.d(TAG, "VideoRecorder prepared: $quality → $outputFilePath")
        return mediaRecorder!!.surface
    }

    /**
     * 开始录像
     */
    fun start() {
        if (_state.value != RecordState.PREPARED) {
            Log.w(TAG, "start() called in wrong state: ${_state.value}")
            return
        }
        mediaRecorder?.start()
        startTimeMs = System.currentTimeMillis()
        _state.value = RecordState.RECORDING
        startDurationTimer()
        Log.d(TAG, "Recording started")
    }

    /**
     * 暂停录像（Android 7+）
     */
    fun pause() {
        if (_state.value != RecordState.RECORDING) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
            _state.value = RecordState.PAUSED
            stopDurationTimer()
            Log.d(TAG, "Recording paused")
        }
    }

    /**
     * 恢复录像（Android 7+）
     */
    fun resume() {
        if (_state.value != RecordState.PAUSED) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
            _state.value = RecordState.RECORDING
            startDurationTimer()
            Log.d(TAG, "Recording resumed")
        }
    }

    /**
     * 停止录像并保存文件
     * @return 保存的文件路径，失败返回 null
     */
    fun stop(): String? {
        if (_state.value !in listOf(RecordState.RECORDING, RecordState.PAUSED)) {
            Log.w(TAG, "stop() called in wrong state: ${_state.value}")
            return null
        }
        stopDurationTimer()
        return try {
            mediaRecorder?.stop()
            _state.value = RecordState.STOPPED
            saveToMediaStore()
            Log.d(TAG, "Recording stopped, saved to: $outputFilePath")
            outputFilePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording: ${e.message}")
            null
        } finally {
            release()
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        stopDurationTimer()
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Release error: ${e.message}")
        }
        mediaRecorder = null
        _state.value = RecordState.IDLE
        _durationMs.value = 0L
    }

    // ─── 私有方法 ─────────────────────────────────────────────────────────

    private fun createOutputFilePath(): String {
        val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        val fileName = "YanbaoVideo_$timestamp.mp4"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+：使用 MediaStore 路径
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            File(dir, fileName).absolutePath
        } else {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "YanbaoCamera"
            ).also { it.mkdirs() }
            File(dir, fileName).absolutePath
        }
    }

    private fun saveToMediaStore() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, File(outputFilePath).name)
            put(MediaStore.Video.Media.MIME_TYPE, MIME_TYPE)
            put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/YanbaoCamera")
            put(MediaStore.Video.Media.IS_PENDING, 0)
        }
        try {
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            Log.d(TAG, "Video saved to MediaStore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save to MediaStore: ${e.message}")
        }
    }

    private fun startDurationTimer() {
        durationTimer = java.util.Timer().also { timer ->
            timer.scheduleAtFixedRate(object : java.util.TimerTask() {
                override fun run() {
                    if (_state.value == RecordState.RECORDING) {
                        _durationMs.value = System.currentTimeMillis() - startTimeMs
                    }
                }
            }, 0L, 500L)
        }
    }

    private fun stopDurationTimer() {
        durationTimer?.cancel()
        durationTimer = null
    }
}

/**
 * 录像质量预设
 */
enum class VideoQuality(
    val width: Int,
    val height: Int,
    val fps: Int,
    val bitrate: Int
) {
    UHD_30(3840, 2160, 30, 50_000_000),   // 4K 30fps
    FHD_60(1920, 1080, 60, 20_000_000),   // 1080P 60fps
    FHD_30(1920, 1080, 30, 10_000_000),   // 1080P 30fps（默认）
    HD_30(1280, 720, 30, 5_000_000),      // 720P 30fps
    SD_30(640, 480, 30, 2_000_000);       // 480P 30fps

    override fun toString(): String = "${height}P ${fps}fps"
}
