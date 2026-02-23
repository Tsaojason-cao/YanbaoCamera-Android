package com.yanbao.camera.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Phase 2: 延时摄影录像器
 *
 * 通过定时拍照 + MediaCodec 编码合成延时视频，支持：
 * - 可配置拍照间隔（1s/2s/5s/10s/30s/60s）
 * - 可配置输出帧率（24/30fps）
 * - 实时帧计数
 * - 预估最终视频时长
 *
 * 使用方式：
 * ```kotlin
 * val timelapse = TimelapseRecorder(context)
 * timelapse.start(
 *     captureIntervalMs = 2000L,
 *     outputFps = 30,
 *     captureCallback = { camera2Manager.takePicture() }
 * )
 * // ...
 * val videoPath = timelapse.stop()
 * ```
 */
class TimelapseRecorder(private val context: Context) {

    companion object {
        private const val TAG = "TimelapseRecorder"
        private const val OUTPUT_WIDTH = 1920
        private const val OUTPUT_HEIGHT = 1080
        private const val MIME_TYPE = "video/avc"
        private const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        private const val I_FRAME_INTERVAL = 1
    }

    // ─── 状态 ─────────────────────────────────────────────────────────────

    private val _frameCount = MutableStateFlow(0)
    val frameCount: StateFlow<Int> = _frameCount.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var captureJob: Job? = null

    private var encoder: MediaCodec? = null
    private var muxer: MediaMuxer? = null
    private var videoTrackIndex = -1
    private var outputFilePath = ""
    private var outputFps = 30
    private var presentationTimeUs = 0L

    // 帧缓冲（JPEG 字节数组列表）
    private val frameBuffer = mutableListOf<ByteArray>()

    // ─── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 开始延时摄影
     * @param captureIntervalMs 拍照间隔（毫秒）
     * @param outputFps 输出视频帧率
     * @param captureCallback 每次触发拍照的回调（应调用 Camera2 拍照并返回 JPEG 字节数组）
     */
    fun start(
        captureIntervalMs: Long = 2000L,
        outputFps: Int = 30,
        captureCallback: suspend () -> ByteArray?
    ) {
        if (_isRecording.value) {
            Log.w(TAG, "Already recording")
            return
        }
        this.outputFps = outputFps
        frameBuffer.clear()
        _frameCount.value = 0
        _isRecording.value = true

        captureJob = scope.launch {
            Log.d(TAG, "Timelapse started: interval=${captureIntervalMs}ms, fps=$outputFps")
            while (isActive && _isRecording.value) {
                val jpegBytes = captureCallback()
                if (jpegBytes != null) {
                    synchronized(frameBuffer) {
                        frameBuffer.add(jpegBytes)
                    }
                    _frameCount.value = frameBuffer.size
                    Log.d(TAG, "Frame ${frameBuffer.size} captured")
                }
                delay(captureIntervalMs)
            }
        }
    }

    /**
     * 停止延时摄影并合成视频
     * @return 输出视频文件路径，失败返回 null
     */
    suspend fun stop(): String? = withContext(Dispatchers.IO) {
        _isRecording.value = false
        captureJob?.cancel()
        captureJob = null

        val frames = synchronized(frameBuffer) { frameBuffer.toList() }
        if (frames.isEmpty()) {
            Log.w(TAG, "No frames captured")
            return@withContext null
        }

        Log.d(TAG, "Encoding ${frames.size} frames to video...")
        return@withContext encodeFramesToVideo(frames)
    }

    /**
     * 预估最终视频时长（秒）
     * @param captureIntervalMs 拍照间隔
     */
    fun estimatedDurationSec(captureIntervalMs: Long): Float {
        return _frameCount.value.toFloat() / outputFps
    }

    // ─── 私有方法 ─────────────────────────────────────────────────────────

    private fun encodeFramesToVideo(frames: List<ByteArray>): String? {
        outputFilePath = createOutputFilePath()

        try {
            // 配置 MediaCodec H.264 编码器
            val format = MediaFormat.createVideoFormat(MIME_TYPE, OUTPUT_WIDTH, OUTPUT_HEIGHT).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(MediaFormat.KEY_BIT_RATE, 10_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, outputFps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
            }

            encoder = MediaCodec.createEncoderByType(MIME_TYPE).apply {
                configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                start()
            }

            muxer = MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            val frameDurationUs = 1_000_000L / outputFps
            presentationTimeUs = 0L

            val bufferInfo = MediaCodec.BufferInfo()

            frames.forEach { jpegBytes ->
                val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
                    ?.let { scaleBitmap(it, OUTPUT_WIDTH, OUTPUT_HEIGHT) }
                    ?: return@forEach

                val yuvBytes = bitmapToYuv420(bitmap)
                bitmap.recycle()

                // 输入帧
                val inputBufferIndex = encoder!!.dequeueInputBuffer(10_000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = encoder!!.getInputBuffer(inputBufferIndex)!!
                    inputBuffer.clear()
                    inputBuffer.put(yuvBytes)
                    encoder!!.queueInputBuffer(
                        inputBufferIndex, 0, yuvBytes.size,
                        presentationTimeUs, 0
                    )
                    presentationTimeUs += frameDurationUs
                }

                // 输出帧
                drainEncoder(bufferInfo, false)
            }

            // 发送 EOS
            val inputBufferIndex = encoder!!.dequeueInputBuffer(10_000)
            if (inputBufferIndex >= 0) {
                encoder!!.queueInputBuffer(inputBufferIndex, 0, 0, presentationTimeUs,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            }
            drainEncoder(bufferInfo, true)

            muxer!!.stop()
            Log.d(TAG, "Timelapse video saved: $outputFilePath")
            return outputFilePath

        } catch (e: Exception) {
            Log.e(TAG, "Encoding failed: ${e.message}", e)
            return null
        } finally {
            encoder?.stop()
            encoder?.release()
            encoder = null
            try { muxer?.release() } catch (e: Exception) { /* ignore */ }
            muxer = null
        }
    }

    private fun drainEncoder(bufferInfo: MediaCodec.BufferInfo, endOfStream: Boolean) {
        val enc = encoder ?: return
        val mux = muxer ?: return

        while (true) {
            val outputBufferIndex = enc.dequeueOutputBuffer(bufferInfo, 10_000)
            when {
                outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    videoTrackIndex = mux.addTrack(enc.outputFormat)
                    mux.start()
                }
                outputBufferIndex >= 0 -> {
                    val outputBuffer = enc.getOutputBuffer(outputBufferIndex)!!
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size > 0 && videoTrackIndex >= 0) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        mux.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo)
                    }
                    enc.releaseOutputBuffer(outputBufferIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return
                }
                else -> if (endOfStream) return else break
            }
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return if (bitmap.width == width && bitmap.height == height) bitmap
        else Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /**
     * 将 ARGB Bitmap 转换为 YUV420 字节数组（MediaCodec 输入格式）
     */
    private fun bitmapToYuv420(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val yuv = ByteArray(width * height * 3 / 2)
        var yIndex = 0
        var uvIndex = width * height

        for (j in 0 until height) {
            for (i in 0 until width) {
                val pixel = pixels[j * width + i]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv[yIndex++] = y.coerceIn(0, 255).toByte()

                if (j % 2 == 0 && i % 2 == 0) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                    yuv[uvIndex++] = u.coerceIn(0, 255).toByte()
                    yuv[uvIndex++] = v.coerceIn(0, 255).toByte()
                }
            }
        }
        return yuv
    }

    private fun createOutputFilePath(): String {
        val timestamp = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        val dir = File(context.getExternalFilesDir(null), "YanbaoTimelapse").also { it.mkdirs() }
        return File(dir, "Timelapse_$timestamp.mp4").absolutePath
    }
}
