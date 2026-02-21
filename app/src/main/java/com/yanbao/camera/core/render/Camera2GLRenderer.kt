package com.yanbao.camera.core.render

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Camera2 + OpenGL ES 3.0 渲染器
 * 
 * 功能：
 * 1. 创建 SurfaceTexture 用于接收 Camera2 预览帧
 * 2. 加载并编译 YanbaoFilter.frag 和 Parallax.vert
 * 3. 将 Camera2 预览帧渲染到屏幕，并应用 29D 参数
 * 4. 支持实时更新 uniform 参数（ISO、曝光、白平衡等）
 * 
 * 工业级特性：
 * - 使用 OES 纹理接收 Camera2 预览
 * - 支持陀螺仪数据驱动 2.9D 视差
 * - 支持 RGB 曲线 LUT 纹理
 */
class Camera2GLRenderer(
    private val context: Context,
    private val onSurfaceTextureReady: (SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer {
    
    companion object {
        private const val TAG = "Camera2GLRenderer"
        
        // 顶点坐标（全屏四边形）
        private val VERTEX_COORDS = floatArrayOf(
            -1.0f, -1.0f,  // 左下
             1.0f, -1.0f,  // 右下
            -1.0f,  1.0f,  // 左上
             1.0f,  1.0f   // 右上
        )
        
        // 纹理坐标
        private val TEXTURE_COORDS = floatArrayOf(
            0.0f, 1.0f,  // 左下
            1.0f, 1.0f,  // 右下
            0.0f, 0.0f,  // 左上
            1.0f, 0.0f   // 右上
        )
    }
    
    // OpenGL 资源
    private var programId = 0
    private var oesTextureId = 0
    private var surfaceTexture: SurfaceTexture? = null
    
    // Vertex Buffer Objects
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureBuffer: FloatBuffer
    
    // Shader 句柄
    private var aPositionHandle = 0
    private var aTexCoordHandle = 0
    private var uTextureHandle = 0
    private var uLutTextureHandle = 0
    
    // 29D 参数 Uniform 句柄
    private var uISOHandle = 0
    private var uExposureTimeHandle = 0
    private var uWhiteBalanceHandle = 0
    private var uBrightnessHandle = 0
    private var uContrastHandle = 0
    private var uSaturationHandle = 0
    private var uSharpenHandle = 0
    private var uTemperatureHandle = 0
    private var uTintHandle = 0
    private var uHighlightsHandle = 0
    private var uShadowsHandle = 0
    private var uExposureHandle = 0
    private var uGrainHandle = 0
    private var uVignetteHandle = 0
    private var uFadeHandle = 0
    
    // 2.9D 视差参数
    private var uParallaxOffsetXHandle = 0
    private var uParallaxOffsetYHandle = 0
    
    // 当前参数值
    private var iso = 100
    private var exposureTime = 10000000L // 10ms
    private var whiteBalance = 5000 // 5000K
    private var brightness = 0.0f
    private var contrast = 1.0f
    private var saturation = 1.0f
    private var sharpen = 0.0f
    private var temperature = 0.0f
    private var tint = 0.0f
    private var highlights = 0.0f
    private var shadows = 0.0f
    private var exposure = 0.0f
    private var grain = 0.0f
    private var vignette = 0.0f
    private var fade = 0.0f
    
    // 2.9D 视差偏移（由陀螺仪驱动）
    private var parallaxOffsetX = 0.0f
    private var parallaxOffsetY = 0.0f
    
    // LUT 纹理 ID
    private var lutTextureId = 0
    
    init {
        // 初始化 Vertex Buffer
        vertexBuffer = ByteBuffer.allocateDirect(VERTEX_COORDS.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(VERTEX_COORDS)
        vertexBuffer.position(0)
        
        // 初始化 Texture Buffer
        textureBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDS.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(TEXTURE_COORDS)
        textureBuffer.position(0)
    }
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated")
        
        // 创建 OES 纹理
        oesTextureId = createOESTexture()
        
        // 创建 SurfaceTexture
        surfaceTexture = SurfaceTexture(oesTextureId).apply {
            setOnFrameAvailableListener {
                // 通知 GLSurfaceView 有新帧可用
            }
        }
        
        // 通知外部 SurfaceTexture 已准备好
        onSurfaceTextureReady(surfaceTexture!!)
        
        // 加载 Shader
        val vertexShader = loadShaderFromAssets("shaders/Parallax.vert")
        val fragmentShader = loadShaderFromAssets("shaders/YanbaoFilter.frag")
        
        programId = createProgram(vertexShader, fragmentShader)
        
        if (programId == 0) {
            Log.e(TAG, "Failed to create program")
            return
        }
        
        // 获取句柄
        aPositionHandle = GLES30.glGetAttribLocation(programId, "aPosition")
        aTexCoordHandle = GLES30.glGetAttribLocation(programId, "aTexCoord")
        uTextureHandle = GLES30.glGetUniformLocation(programId, "uTexture")
        uLutTextureHandle = GLES30.glGetUniformLocation(programId, "uLutTexture")
        
        // 获取 29D 参数句柄
        uISOHandle = GLES30.glGetUniformLocation(programId, "uISO")
        uExposureTimeHandle = GLES30.glGetUniformLocation(programId, "uExposureTime")
        uWhiteBalanceHandle = GLES30.glGetUniformLocation(programId, "uWhiteBalance")
        uBrightnessHandle = GLES30.glGetUniformLocation(programId, "uBrightness")
        uContrastHandle = GLES30.glGetUniformLocation(programId, "uContrast")
        uSaturationHandle = GLES30.glGetUniformLocation(programId, "uSaturation")
        uSharpenHandle = GLES30.glGetUniformLocation(programId, "uSharpen")
        uTemperatureHandle = GLES30.glGetUniformLocation(programId, "uTemperature")
        uTintHandle = GLES30.glGetUniformLocation(programId, "uTint")
        uHighlightsHandle = GLES30.glGetUniformLocation(programId, "uHighlights")
        uShadowsHandle = GLES30.glGetUniformLocation(programId, "uShadows")
        uExposureHandle = GLES30.glGetUniformLocation(programId, "uExposure")
        uGrainHandle = GLES30.glGetUniformLocation(programId, "uGrain")
        uVignetteHandle = GLES30.glGetUniformLocation(programId, "uVignette")
        uFadeHandle = GLES30.glGetUniformLocation(programId, "uFade")
        
        // 获取 2.9D 视差句柄
        uParallaxOffsetXHandle = GLES30.glGetUniformLocation(programId, "uParallaxOffsetX")
        uParallaxOffsetYHandle = GLES30.glGetUniformLocation(programId, "uParallaxOffsetY")
        
        Log.d(TAG, "Shader program created successfully, programId=$programId")
        Log.d(TAG, "aPositionHandle=$aPositionHandle, aTexCoordHandle=$aTexCoordHandle")
        Log.d(TAG, "uTextureHandle=$uTextureHandle, uLutTextureHandle=$uLutTextureHandle")
        
        // 创建默认 LUT 纹理
        lutTextureId = createDefaultLutTexture()
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: width=$width, height=$height")
        GLES30.glViewport(0, 0, width, height)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        // 更新 SurfaceTexture
        surfaceTexture?.updateTexImage()
        
        // 清屏
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        
        // 使用 Shader 程序
        GLES30.glUseProgram(programId)
        
        // 绑定 OES 纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        GLES30.glUniform1i(uTextureHandle, 0)
        
        // 绑定 LUT 纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lutTextureId)
        GLES30.glUniform1i(uLutTextureHandle, 1)
        
        // 设置 29D 参数
        GLES30.glUniform1i(uISOHandle, iso)
        GLES30.glUniform1f(uExposureTimeHandle, exposureTime / 1000000000.0f) // 转换为秒
        GLES30.glUniform1f(uWhiteBalanceHandle, whiteBalance / 1000.0f) // 转换为 K
        GLES30.glUniform1f(uBrightnessHandle, brightness)
        GLES30.glUniform1f(uContrastHandle, contrast)
        GLES30.glUniform1f(uSaturationHandle, saturation)
        GLES30.glUniform1f(uSharpenHandle, sharpen)
        GLES30.glUniform1f(uTemperatureHandle, temperature)
        GLES30.glUniform1f(uTintHandle, tint)
        GLES30.glUniform1f(uHighlightsHandle, highlights)
        GLES30.glUniform1f(uShadowsHandle, shadows)
        GLES30.glUniform1f(uExposureHandle, exposure)
        GLES30.glUniform1f(uGrainHandle, grain)
        GLES30.glUniform1f(uVignetteHandle, vignette)
        GLES30.glUniform1f(uFadeHandle, fade)
        
        // 设置 2.9D 视差偏移
        GLES30.glUniform1f(uParallaxOffsetXHandle, parallaxOffsetX)
        GLES30.glUniform1f(uParallaxOffsetYHandle, parallaxOffsetY)
        
        // 设置顶点属性
        GLES30.glEnableVertexAttribArray(aPositionHandle)
        GLES30.glVertexAttribPointer(aPositionHandle, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        
        GLES30.glEnableVertexAttribArray(aTexCoordHandle)
        GLES30.glVertexAttribPointer(aTexCoordHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
        
        // 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        
        // 禁用顶点属性
        GLES30.glDisableVertexAttribArray(aPositionHandle)
        GLES30.glDisableVertexAttribArray(aTexCoordHandle)
    }
    
    /**
     * 更新专业模式参数
     */
    fun updateProParams(iso: Int, exposureTime: Long, whiteBalance: Int) {
        this.iso = iso
        this.exposureTime = exposureTime
        this.whiteBalance = whiteBalance
        
        Log.d(TAG, "AUDIT_PARAMS: ISO=$iso, ExposureTime=$exposureTime, WhiteBalance=$whiteBalance")
    }
    
    /**
     * 更新 29D 参数
     */
    fun update29DParams(
        brightness: Float = 0.0f,
        contrast: Float = 1.0f,
        saturation: Float = 1.0f,
        sharpen: Float = 0.0f,
        temperature: Float = 0.0f,
        tint: Float = 0.0f,
        highlights: Float = 0.0f,
        shadows: Float = 0.0f,
        exposure: Float = 0.0f,
        grain: Float = 0.0f,
        vignette: Float = 0.0f,
        fade: Float = 0.0f
    ) {
        this.brightness = brightness
        this.contrast = contrast
        this.saturation = saturation
        this.sharpen = sharpen
        this.temperature = temperature
        this.tint = tint
        this.highlights = highlights
        this.shadows = shadows
        this.exposure = exposure
        this.grain = grain
        this.vignette = vignette
        this.fade = fade
    }
    
    /**
     * 更新 2.9D 视差偏移（由陀螺仪驱动）
     */
    fun updateParallaxOffset(offsetX: Float, offsetY: Float) {
        this.parallaxOffsetX = offsetX
        this.parallaxOffsetY = offsetY
    }
    
    /**
     * 更新 RGB 曲线 LUT 纹理
     */
    fun updateLutTexture(lutData: ByteArray) {
        Log.d(TAG, "Updating LUT texture, size=${lutData.size}")
        Log.d(TAG, "LUT_ARRAY: ${lutData.take(64).joinToString(", ")}")
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lutTextureId)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGB,
            256, 1,
            0,
            GLES30.GL_RGB,
            GLES30.GL_UNSIGNED_BYTE,
            ByteBuffer.wrap(lutData)
        )
    }
    
    /**
     * 创建 OES 纹理
     */
    private fun createOESTexture(): Int {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        val textureId = textures[0]
        
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        
        return textureId
    }
    
    /**
     * 创建默认 LUT 纹理（线性映射）
     */
    private fun createDefaultLutTexture(): Int {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        val textureId = textures[0]
        
        // 创建线性 LUT（256 x 1）
        val lutData = ByteArray(256 * 3)
        for (i in 0 until 256) {
            lutData[i * 3] = i.toByte()     // R
            lutData[i * 3 + 1] = i.toByte() // G
            lutData[i * 3 + 2] = i.toByte() // B
        }
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGB,
            256, 1,
            0,
            GLES30.GL_RGB,
            GLES30.GL_UNSIGNED_BYTE,
            ByteBuffer.wrap(lutData)
        )
        
        return textureId
    }
    
    /**
     * 从 assets 加载 Shader
     */
    private fun loadShaderFromAssets(path: String): String {
        return try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load shader from $path", e)
            ""
        }
    }
    
    /**
     * 创建 Shader 程序
     */
    private fun createProgram(vertexShaderCode: String, fragmentShaderCode: String): Int {
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        if (vertexShader == 0 || fragmentShader == 0) {
            return 0
        }
        
        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)
        GLES30.glLinkProgram(program)
        
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Failed to link program: ${GLES30.glGetProgramInfoLog(program)}")
            GLES30.glDeleteProgram(program)
            return 0
        }
        
        return program
    }
    
    /**
     * 加载 Shader
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Failed to compile shader: ${GLES30.glGetShaderInfoLog(shader)}")
            GLES30.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }
    
    /**
     * 释放资源
     */
    fun release() {
        surfaceTexture?.release()
        surfaceTexture = null
        
        if (programId != 0) {
            GLES30.glDeleteProgram(programId)
            programId = 0
        }
        
        if (oesTextureId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(oesTextureId), 0)
            oesTextureId = 0
        }
        
        if (lutTextureId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(lutTextureId), 0)
            lutTextureId = 0
        }
    }
}
