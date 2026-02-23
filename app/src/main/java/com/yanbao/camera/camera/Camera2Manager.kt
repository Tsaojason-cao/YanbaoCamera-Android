package com.yanbao.camera.camera

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class Camera2Manager(private val context: Context) {

    private var cameraDevice: CameraDevice? = null
    private var cameraId: String? = null
    private var previewSize = Size(1920, 1080)
    private var captureSession: CameraCaptureSession? = null
    private var previewRequest: CaptureRequest? = null
    private var previewSurface: Surface? = null
    private var imageReader: ImageReader? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    // 当前相机参数
    var iso = 100
        private set
    var exposureTime = 30_000_000L // 1/30秒
        private set
    var ev = 0
        private set
    var whiteBalance = CaptureRequest.CONTROL_AWB_MODE_AUTO
        private set
    var lensFacing = CameraCharacteristics.LENS_FACING_BACK
        private set

    // 录像相关
    private var mediaRecorder: MediaRecorder? = null
    private var isRecordingFlag = false

    // 回调接口
    var onPhotoSaved: ((File) -> Unit)? = null
    var onRecordingStopped: ((File) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun isRecording(): Boolean = isRecordingFlag

    fun startCamera(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        startBackgroundThread()
        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
        previewSurface = Surface(surfaceTexture)

        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = manager.cameraIdList.firstOrNull { id ->
                val chars = manager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.LENS_FACING) == lensFacing
            } ?: manager.cameraIdList[0]

            manager.openCamera(cameraId!!, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    onError?.invoke("相机错误: $error")
                }
            }, backgroundHandler)
        } catch (e: SecurityException) {
            onError?.invoke("相机权限被拒绝")
        } catch (e: Exception) {
            onError?.invoke("无法打开相机: ${e.message}")
        }
    }

    private fun createPreviewSession() {
        try {
            val camera = cameraDevice ?: return

            // 创建预览请求
            val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewSurface?.let { previewRequestBuilder.addTarget(it) }

            // 应用当前参数
            applyParameters(previewRequestBuilder)

            previewRequest = previewRequestBuilder.build()

            // 创建 ImageReader 用于拍照
            imageReader = ImageReader.newInstance(
                previewSize.width, previewSize.height,
                ImageFormat.JPEG, 2
            )
            imageReader?.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                image?.let {
                    val buffer: ByteBuffer = it.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)

                    val file = createImageFile()
                    FileOutputStream(file).use { output ->
                        output.write(bytes)
                    }

                    it.close()
                    onPhotoSaved?.invoke(file)
                }
            }, backgroundHandler)

            // 创建捕获会话
            val targets = mutableListOf<Surface>().apply {
                previewSurface?.let { add(it) }
                imageReader?.surface?.let { add(it) }
            }

            camera.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    try {
                        session.setRepeatingRequest(previewRequest!!, null, backgroundHandler)
                    } catch (e: Exception) {
                        onError?.invoke("预览失败: ${e.message}")
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    onError?.invoke("配置相机会话失败")
                }
            }, backgroundHandler)

        } catch (e: Exception) {
            onError?.invoke("创建预览失败: ${e.message}")
        }
    }

    fun takePhoto() {
        captureSession?.let { session ->
            val captureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder?.addTarget(imageReader!!.surface)
            applyParameters(captureBuilder!!)
            // 设置自动对焦
            captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
            session.capture(captureBuilder.build(), null, backgroundHandler)
        }
    }

    fun setIso(value: Int) {
        iso = value.coerceIn(100, 6400)
        updateParameters()
    }

    fun setExposureTime(value: Long) {
        exposureTime = value.coerceIn(1_000_000L, 10_000_000_000L) // 1/1000s ~ 10s
        updateParameters()
    }

    fun setEv(value: Int) {
        ev = value.coerceIn(-3, 3)
        updateParameters()
    }

    fun setWhiteBalance(mode: Int) {
        whiteBalance = mode
        updateParameters()
    }

    fun flipLens() {
        lensFacing = if (lensFacing == CameraCharacteristics.LENS_FACING_BACK)
            CameraCharacteristics.LENS_FACING_FRONT else CameraCharacteristics.LENS_FACING_BACK
        val st = previewSurface?.let { SurfaceTexture(0) }
        stopCamera()
        if (st != null) {
            startCamera(st, previewSize.width, previewSize.height)
        }
    }

    private fun applyParameters(builder: CaptureRequest.Builder) {
        // 关闭自动曝光，手动控制
        builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
        builder.set(CaptureRequest.CONTROL_AWB_MODE, whiteBalance)
        builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso)
        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposureTime)
        builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ev)
        // 对焦模式设为连续自动
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
    }

    private fun updateParameters() {
        try {
            val session = captureSession ?: return
            val builder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) ?: return
            previewSurface?.let { builder.addTarget(it) }
            applyParameters(builder)
            val request = builder.build()
            session.setRepeatingRequest(request, null, backgroundHandler)
            // 输出日志供审计
            Log.d("AUDIT_CAMERA", "ISO=$iso, Exposure=$exposureTime, EV=$ev")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 录像功能
    fun startRecording(outputFile: File): Boolean {
        if (cameraDevice == null) return false
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(outputFile.absolutePath)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoSize(1920, 1080)
                setVideoFrameRate(30)
                setVideoEncodingBitRate(10_000_000)
                prepare()
            }
            mediaRecorder?.start()
            isRecordingFlag = true
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    fun stopRecording() {
        val outputFile = File(context.getExternalFilesDir(null) ?: context.filesDir, "last_recording.mp4")
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecordingFlag = false
        onRecordingStopped?.invoke(outputFile)
        // 重新开始预览
        createPreviewSession()
    }

    fun getRecordingSurface(): Surface? = mediaRecorder?.surface

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(storageDir, "IMG_$timestamp.jpg")
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    fun stopCamera() {
        try {
            captureSession?.close()
            cameraDevice?.close()
            imageReader?.close()
            captureSession = null
            cameraDevice = null
            imageReader = null
            backgroundThread?.quitSafely()
            backgroundThread = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
