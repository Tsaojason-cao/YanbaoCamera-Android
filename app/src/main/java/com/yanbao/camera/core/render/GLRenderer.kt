package com.yanbao.camera.core.render

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * OpenGL ES 3.0 渲染器
 * 
 * 負責加載 fragment_shader_29d.glsl 並綁定所有 29 個 Uniform 變量
 * 實現用戶要求的 18 個編輯工具的 GLSL 實時映射
 */
class GLRenderer(private val context: Context) {
    
    companion object {
        private const val TAG = "GLRenderer"
        
        // 頂點著色器
        private const val VERTEX_SHADER_CODE = """
            #version 300 es
            precision mediump float;
            
            in vec4 aPosition;
            in vec2 aTexCoord;
            
            out vec2 vTexCoord;
            
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """
    }
    
    private var programId = 0
    private var textureId = 0
    
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0
    
    // ========================================
    // 18 個編輯工具的 Uniform 句柄
    // ========================================
    
    // 基礎調節
    private var uBrightnessHandle = 0
    private var uContrastHandle = 0
    private var uSaturationHandle = 0
    private var uSharpenHandle = 0
    
    // 色彩進階
    private var uTemperatureHandle = 0
    private var uTintHandle = 0
    private var uHighlightsHandle = 0
    private var uShadowsHandle = 0
    
    // 光影特效
    private var uExposureHandle = 0
    private var uGrainHandle = 0
    private var uVignetteHandle = 0
    private var uFadeHandle = 0
    
    // 人像/AI
    private var uSmoothingHandle = 0
    private var uWhiteningHandle = 0
    private var uSpotRemovalHandle = 0
    
    // 29D 專用
    private var uChromaticRGBHandle = 0
    private var uDepthOfFieldHandle = 0
    private var uDynamicRangeHandle = 0
    
    // 其他 Uniform（從 fragment_shader_29d.glsl 中已有的）
    private var uVibranceHandle = 0
    private var uWhitesHandle = 0
    private var uBlacksHandle = 0
    private var uClarityHandle = 0
    private var uBlurHandle = 0
    private var uAiEnhanceHandle = 0
    
    // HSL 色相（8個）
    private var uHueRedHandle = 0
    private var uHueOrangeHandle = 0
    private var uHueYellowHandle = 0
    private var uHueGreenHandle = 0
    private var uHueAquaHandle = 0
    private var uHueBlueHandle = 0
    private var uHuePurpleHandle = 0
    private var uHueMagentaHandle = 0
    
    // 顏色科學（4個）
    private var uLumRedHandle = 0
    private var uLumOrangeHandle = 0
    private var uSatRedHandle = 0
    private var uSatGreenHandle = 0
    
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer
    
    // 當前參數值
    private val params = EditParams()
    
    init {
        initBuffers()
    }
    
    /**
     * 初始化頂點和紋理坐標緩衝區
     */
    private fun initBuffers() {
        // 頂點坐標（全屏四邊形）
        val vertices = floatArrayOf(
            -1f, -1f,  // 左下
             1f, -1f,  // 右下
            -1f,  1f,  // 左上
             1f,  1f   // 右上
        )
        
        // 紋理坐標
        val texCoords = floatArrayOf(
            0f, 1f,  // 左下
            1f, 1f,  // 右下
            0f, 0f,  // 左上
            1f, 0f   // 右上
        )
        
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
            .apply { position(0) }
        
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(texCoords)
            .apply { position(0) }
    }
    
    /**
     * 初始化 OpenGL 程序
     */
    fun initGL() {
        // 從 assets 加載片元著色器
        val fragmentShaderCode = loadShaderFromAssets("shaders/fragment_shader_29d.glsl")
        
        // 編譯著色器
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        // 創建程序
        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)
        GLES30.glLinkProgram(programId)
        
        // 檢查鏈接狀態
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES30.glGetProgramInfoLog(programId)
            Log.e(TAG, "Program link error: $error")
            GLES30.glDeleteProgram(programId)
            programId = 0
            return
        }
        
        // 獲取屬性句柄
        positionHandle = GLES30.glGetAttribLocation(programId, "aPosition")
        texCoordHandle = GLES30.glGetAttribLocation(programId, "aTexCoord")
        textureHandle = GLES30.glGetUniformLocation(programId, "uTexture")
        
        // ========================================
        // 綁定 18 個編輯工具的 Uniform 變量
        // ========================================
        
        // 基礎調節
        uBrightnessHandle = GLES30.glGetUniformLocation(programId, "uBrightness")
        uContrastHandle = GLES30.glGetUniformLocation(programId, "uContrast")
        uSaturationHandle = GLES30.glGetUniformLocation(programId, "uSaturation")
        uSharpenHandle = GLES30.glGetUniformLocation(programId, "uSharpen")
        
        // 色彩進階
        uTemperatureHandle = GLES30.glGetUniformLocation(programId, "uTemperature")
        uTintHandle = GLES30.glGetUniformLocation(programId, "uTint")
        uHighlightsHandle = GLES30.glGetUniformLocation(programId, "uHighlights")
        uShadowsHandle = GLES30.glGetUniformLocation(programId, "uShadows")
        
        // 光影特效
        uExposureHandle = GLES30.glGetUniformLocation(programId, "uExposure")
        uGrainHandle = GLES30.glGetUniformLocation(programId, "uGrain")
        uVignetteHandle = GLES30.glGetUniformLocation(programId, "uVignette")
        uFadeHandle = GLES30.glGetUniformLocation(programId, "uFade")
        
        // 人像/AI
        uSmoothingHandle = GLES30.glGetUniformLocation(programId, "uSmoothing")
        uWhiteningHandle = GLES30.glGetUniformLocation(programId, "uWhitening")
        uSpotRemovalHandle = GLES30.glGetUniformLocation(programId, "uSpotRemoval")
        
        // 29D 專用
        uChromaticRGBHandle = GLES30.glGetUniformLocation(programId, "uChromaticRGB")
        uDepthOfFieldHandle = GLES30.glGetUniformLocation(programId, "uDepthOfField")
        uDynamicRangeHandle = GLES30.glGetUniformLocation(programId, "uDynamicRange")
        
        // 其他 Uniform
        uVibranceHandle = GLES30.glGetUniformLocation(programId, "uVibrance")
        uWhitesHandle = GLES30.glGetUniformLocation(programId, "uWhites")
        uBlacksHandle = GLES30.glGetUniformLocation(programId, "uBlacks")
        uClarityHandle = GLES30.glGetUniformLocation(programId, "uClarity")
        uBlurHandle = GLES30.glGetUniformLocation(programId, "uBlur")
        uAiEnhanceHandle = GLES30.glGetUniformLocation(programId, "uAiEnhance")
        
        // HSL 色相
        uHueRedHandle = GLES30.glGetUniformLocation(programId, "uHueRed")
        uHueOrangeHandle = GLES30.glGetUniformLocation(programId, "uHueOrange")
        uHueYellowHandle = GLES30.glGetUniformLocation(programId, "uHueYellow")
        uHueGreenHandle = GLES30.glGetUniformLocation(programId, "uHueGreen")
        uHueAquaHandle = GLES30.glGetUniformLocation(programId, "uHueAqua")
        uHueBlueHandle = GLES30.glGetUniformLocation(programId, "uHueBlue")
        uHuePurpleHandle = GLES30.glGetUniformLocation(programId, "uHuePurple")
        uHueMagentaHandle = GLES30.glGetUniformLocation(programId, "uHueMagenta")
        
        // 顏色科學
        uLumRedHandle = GLES30.glGetUniformLocation(programId, "uLumRed")
        uLumOrangeHandle = GLES30.glGetUniformLocation(programId, "uLumOrange")
        uSatRedHandle = GLES30.glGetUniformLocation(programId, "uSatRed")
        uSatGreenHandle = GLES30.glGetUniformLocation(programId, "uSatGreen")
        
        Log.d(TAG, "OpenGL initialized successfully")
        Log.d(TAG, "All 29 Uniform variables bound successfully")
    }
    
    /**
     * 從 assets 加載著色器代碼
     */
    private fun loadShaderFromAssets(filename: String): String {
        return context.assets.open(filename).bufferedReader().use { it.readText() }
    }
    
    /**
     * 加載著色器
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        
        // 檢查編譯狀態
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES30.glGetShaderInfoLog(shader)
            Log.e(TAG, "Shader compile error: $error")
            GLES30.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }
    
    /**
     * 更新參數
     */
    fun updateParams(params: EditParams) {
        this.params.copyFrom(params)
    }
    
    /**
     * 渲染圖像
     */
    fun render(bitmap: Bitmap): Bitmap {
        // 上傳原始圖像到紋理
        if (textureId == 0) {
            val textures = IntArray(1)
            GLES30.glGenTextures(1, textures, 0)
            textureId = textures[0]
        }
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        
        // 使用程序
        GLES30.glUseProgram(programId)
        
        // 綁定紋理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(textureHandle, 0)
        
        // ========================================
        // 設置所有 Uniform 變量
        // ========================================
        
        // 基礎調節
        GLES30.glUniform1f(uBrightnessHandle, params.brightness)
        GLES30.glUniform1f(uContrastHandle, params.contrast)
        GLES30.glUniform1f(uSaturationHandle, params.saturation)
        GLES30.glUniform1f(uSharpenHandle, params.sharpen)
        
        // 色彩進階
        GLES30.glUniform1f(uTemperatureHandle, params.temperature)
        GLES30.glUniform1f(uTintHandle, params.tint)
        GLES30.glUniform1f(uHighlightsHandle, params.highlights)
        GLES30.glUniform1f(uShadowsHandle, params.shadows)
        
        // 光影特效
        GLES30.glUniform1f(uExposureHandle, params.exposure)
        GLES30.glUniform1f(uGrainHandle, params.grain)
        GLES30.glUniform1f(uVignetteHandle, params.vignette)
        GLES30.glUniform1f(uFadeHandle, params.fade)
        
        // 人像/AI
        GLES30.glUniform1f(uSmoothingHandle, params.smoothing)
        GLES30.glUniform1f(uWhiteningHandle, params.whitening)
        GLES30.glUniform1f(uSpotRemovalHandle, params.spotRemoval)
        
        // 29D 專用
        GLES30.glUniform1f(uChromaticRGBHandle, params.chromaticRGB)
        GLES30.glUniform1f(uDepthOfFieldHandle, params.depthOfField)
        GLES30.glUniform1f(uDynamicRangeHandle, params.dynamicRange)
        
        // 其他 Uniform
        GLES30.glUniform1f(uVibranceHandle, params.vibrance)
        GLES30.glUniform1f(uWhitesHandle, params.whites)
        GLES30.glUniform1f(uBlacksHandle, params.blacks)
        GLES30.glUniform1f(uClarityHandle, params.clarity)
        GLES30.glUniform1f(uBlurHandle, params.blur)
        GLES30.glUniform1f(uAiEnhanceHandle, params.aiEnhance)
        
        // HSL 色相
        GLES30.glUniform1f(uHueRedHandle, params.hueRed)
        GLES30.glUniform1f(uHueOrangeHandle, params.hueOrange)
        GLES30.glUniform1f(uHueYellowHandle, params.hueYellow)
        GLES30.glUniform1f(uHueGreenHandle, params.hueGreen)
        GLES30.glUniform1f(uHueAquaHandle, params.hueAqua)
        GLES30.glUniform1f(uHueBlueHandle, params.hueBlue)
        GLES30.glUniform1f(uHuePurpleHandle, params.huePurple)
        GLES30.glUniform1f(uHueMagentaHandle, params.hueMagenta)
        
        // 顏色科學
        GLES30.glUniform1f(uLumRedHandle, params.lumRed)
        GLES30.glUniform1f(uLumOrangeHandle, params.lumOrange)
        GLES30.glUniform1f(uSatRedHandle, params.satRed)
        GLES30.glUniform1f(uSatGreenHandle, params.satGreen)
        
        // 綁定頂點和紋理坐標
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        
        GLES30.glEnableVertexAttribArray(texCoordHandle)
        GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 0, texCoordBuffer)
        
        // 繪製
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        
        // 讀取渲染結果
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val buffer = ByteBuffer.allocateDirect(bitmap.width * bitmap.height * 4)
        GLES30.glReadPixels(0, 0, bitmap.width, bitmap.height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer)
        resultBitmap.copyPixelsFromBuffer(buffer)
        
        return resultBitmap
    }
    
    /**
     * 釋放資源
     */
    fun release() {
        if (textureId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = 0
        }
        
        if (programId != 0) {
            GLES30.glDeleteProgram(programId)
            programId = 0
        }
    }
}

/**
 * 編輯參數數據類
 */
data class EditParams(
    // 基礎調節
    var brightness: Float = 0f,
    var contrast: Float = 1f,
    var saturation: Float = 1f,
    var sharpen: Float = 0f,
    
    // 色彩進階
    var temperature: Float = 0f,
    var tint: Float = 0f,
    var highlights: Float = 0f,
    var shadows: Float = 0f,
    
    // 光影特效
    var exposure: Float = 0f,
    var grain: Float = 0f,
    var vignette: Float = 0f,
    var fade: Float = 0f,
    
    // 人像/AI
    var smoothing: Float = 0f,
    var whitening: Float = 0f,
    var spotRemoval: Float = 0f,
    
    // 29D 專用
    var chromaticRGB: Float = 0f,
    var depthOfField: Float = 0f,
    var dynamicRange: Float = 0f,
    
    // 其他
    var vibrance: Float = 0f,
    var whites: Float = 0f,
    var blacks: Float = 0f,
    var clarity: Float = 0f,
    var blur: Float = 0f,
    var aiEnhance: Float = 0f,
    
    // HSL 色相
    var hueRed: Float = 0f,
    var hueOrange: Float = 0f,
    var hueYellow: Float = 0f,
    var hueGreen: Float = 0f,
    var hueAqua: Float = 0f,
    var hueBlue: Float = 0f,
    var huePurple: Float = 0f,
    var hueMagenta: Float = 0f,
    
    // 顏色科學
    var lumRed: Float = 0f,
    var lumOrange: Float = 0f,
    var satRed: Float = 0f,
    var satGreen: Float = 0f
) {
    fun copyFrom(other: EditParams) {
        brightness = other.brightness
        contrast = other.contrast
        saturation = other.saturation
        sharpen = other.sharpen
        temperature = other.temperature
        tint = other.tint
        highlights = other.highlights
        shadows = other.shadows
        exposure = other.exposure
        grain = other.grain
        vignette = other.vignette
        fade = other.fade
        smoothing = other.smoothing
        whitening = other.whitening
        spotRemoval = other.spotRemoval
        chromaticRGB = other.chromaticRGB
        depthOfField = other.depthOfField
        dynamicRange = other.dynamicRange
        vibrance = other.vibrance
        whites = other.whites
        blacks = other.blacks
        clarity = other.clarity
        blur = other.blur
        aiEnhance = other.aiEnhance
        hueRed = other.hueRed
        hueOrange = other.hueOrange
        hueYellow = other.hueYellow
        hueGreen = other.hueGreen
        hueAqua = other.hueAqua
        hueBlue = other.hueBlue
        huePurple = other.huePurple
        hueMagenta = other.hueMagenta
        lumRed = other.lumRed
        lumOrange = other.lumOrange
        satRed = other.satRed
        satGreen = other.satGreen
    }
}
