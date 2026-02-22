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
 * 2. 加载并编译 YanbaoFilter.frag（含完整 29D uniform）
 * 3. 将 Camera2 预览帧渲染到屏幕，并实时应用 29D 参数
 * 4. 支持实时更新所有 uniform 参数，延迟 < 16ms（60fps）
 *
 * 工业级特性：
 * - 使用 OES 纹理接收 Camera2 预览
 * - 支持陀螺仪数据驱动 2.9D 视差
 * - 支持 RGB 曲线 LUT 纹理
 * - 所有参数变化实时输出 Logcat（AUDIT_PARAMS 标签）
 */
class Camera2GLRenderer(
    private val context: Context,
    private val onSurfaceTextureReady: (SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "Camera2GLRenderer"

        // 顶点坐标（全屏四边形）
        private val VERTEX_COORDS = floatArrayOf(
            -1.0f, -1.0f,
             1.0f, -1.0f,
            -1.0f,  1.0f,
             1.0f,  1.0f
        )

        // 纹理坐标
        private val TEXTURE_COORDS = floatArrayOf(
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
        )
    }

    // OpenGL 资源
    private var programId = 0
    private var oesTextureId = 0
    private var surfaceTexture: SurfaceTexture? = null

    // Vertex Buffer Objects
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureBuffer: FloatBuffer

    // 基础 Shader 句柄
    private var aPositionHandle = 0
    private var aTexCoordHandle = 0
    private var uTextureHandle = 0
    private var uLutTextureHandle = 0
    private var uResolutionHandle = 0
    private var uTimeHandle = 0

    // ── Tab 1: 曝光 uniform 句柄 ──────────────────────────────
    private var h_ISO = 0
    private var h_ShutterSpeed = 0
    private var h_Exposure = 0
    private var h_DynamicRange = 0
    private var h_ShadowComp = 0
    private var h_HighlightProtect = 0

    // ── Tab 2: 色彩 uniform 句柄 ──────────────────────────────
    private var h_ColorTemp = 0
    private var h_Tint = 0
    private var h_Saturation = 0
    private var h_SkinTone = 0
    private var h_RedGain = 0
    private var h_GreenGain = 0
    private var h_BlueGain = 0
    private var h_ColorBoost = 0

    // ── Tab 3: 纹理 uniform 句柄 ──────────────────────────────
    private var h_Sharpness = 0
    private var h_Denoise = 0
    private var h_Grain = 0
    private var h_Vignette = 0
    private var h_Clarity = 0
    private var h_Dehaze = 0

    // ── Tab 4: 美颜 uniform 句柄 ──────────────────────────────
    private var h_BeautyGlobal = 0
    private var h_SkinSmooth = 0
    private var h_FaceThin = 0
    private var h_EyeEnlarge = 0
    private var h_SkinWhiten = 0
    private var h_SkinRedden = 0
    private var h_ChinAdjust = 0
    private var h_NoseBridge = 0

    // ── D29 ───────────────────────────────────────────────────
    private var h_LBSCompensation = 0

    // ── 2.9D 视差句柄 ─────────────────────────────────────────
    private var h_ParallaxOffsetX = 0
    private var h_ParallaxOffsetY = 0

    // LUT 纹理
    private var lutTextureId = 0

    // ── 当前参数值（Tab 1: 曝光）─────────────────────────────
    @Volatile private var p_ISO = 400
    @Volatile private var p_ShutterSpeed = 0.5f
    @Volatile private var p_Exposure = 0.0f
    @Volatile private var p_DynamicRange = 0.5f
    @Volatile private var p_ShadowComp = 0.3f
    @Volatile private var p_HighlightProtect = 0.3f

    // ── 当前参数值（Tab 2: 色彩）─────────────────────────────
    @Volatile private var p_ColorTemp = 0.4375f
    @Volatile private var p_Tint = 0.0f
    @Volatile private var p_Saturation = 1.0f
    @Volatile private var p_SkinTone = 0.5f
    @Volatile private var p_RedGain = 1.0f
    @Volatile private var p_GreenGain = 1.0f
    @Volatile private var p_BlueGain = 1.0f
    @Volatile private var p_ColorBoost = 0.0f

    // ── 当前参数值（Tab 3: 纹理）─────────────────────────────
    @Volatile private var p_Sharpness = 0.5f
    @Volatile private var p_Denoise = 0.3f
    @Volatile private var p_Grain = 0.0f
    @Volatile private var p_Vignette = 0.0f
    @Volatile private var p_Clarity = 0.0f
    @Volatile private var p_Dehaze = 0.0f

    // ── 当前参数值（Tab 4: 美颜）─────────────────────────────
    @Volatile private var p_BeautyGlobal = 0.5f
    @Volatile private var p_SkinSmooth = 0.6f
    @Volatile private var p_FaceThin = 0.3f
    @Volatile private var p_EyeEnlarge = 0.2f
    @Volatile private var p_SkinWhiten = 0.4f
    @Volatile private var p_SkinRedden = 0.2f
    @Volatile private var p_ChinAdjust = 0.0f
    @Volatile private var p_NoseBridge = 0.0f

    // ── D29 ───────────────────────────────────────────────────
    @Volatile private var p_LBSCompensation = 0.0f

    // ── 2.9D 视差 ─────────────────────────────────────────────
    @Volatile private var p_ParallaxOffsetX = 0.0f
    @Volatile private var p_ParallaxOffsetY = 0.0f

    // 渲染帧计数（用于 u_Time）
    private var frameCount = 0L

    // 视口尺寸
    private var viewportWidth = 1080
    private var viewportHeight = 1920

    init {
        vertexBuffer = ByteBuffer.allocateDirect(VERTEX_COORDS.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(VERTEX_COORDS)
        vertexBuffer.position(0)

        textureBuffer = ByteBuffer.allocateDirect(TEXTURE_COORDS.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(TEXTURE_COORDS)
        textureBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(TAG, "onSurfaceCreated")

        oesTextureId = createOESTexture()
        surfaceTexture = SurfaceTexture(oesTextureId).apply {
            setOnFrameAvailableListener { /* GLSurfaceView 会自动 requestRender */ }
        }
        onSurfaceTextureReady(surfaceTexture!!)

        val vertexShader   = loadShaderFromAssets("shaders/Parallax.vert")
        val fragmentShader = loadShaderFromAssets("shaders/YanbaoFilter.frag")
        programId = createProgram(vertexShader, fragmentShader)

        if (programId == 0) {
            Log.e(TAG, "Failed to create GL program")
            return
        }

        // 基础句柄
        aPositionHandle  = GLES30.glGetAttribLocation(programId,  "aPosition")
        aTexCoordHandle  = GLES30.glGetAttribLocation(programId,  "aTexCoord")
        uTextureHandle   = GLES30.glGetUniformLocation(programId, "uTexture")
        uLutTextureHandle = GLES30.glGetUniformLocation(programId, "uLutTexture")
        uResolutionHandle = GLES30.glGetUniformLocation(programId, "u_Resolution")
        uTimeHandle       = GLES30.glGetUniformLocation(programId, "u_Time")

        // Tab 1: 曝光
        h_ISO               = GLES30.glGetUniformLocation(programId, "u_ISO")
        h_ShutterSpeed      = GLES30.glGetUniformLocation(programId, "u_ShutterSpeed")
        h_Exposure          = GLES30.glGetUniformLocation(programId, "u_Exposure")
        h_DynamicRange      = GLES30.glGetUniformLocation(programId, "u_DynamicRange")
        h_ShadowComp        = GLES30.glGetUniformLocation(programId, "u_ShadowComp")
        h_HighlightProtect  = GLES30.glGetUniformLocation(programId, "u_HighlightProtect")

        // Tab 2: 色彩
        h_ColorTemp   = GLES30.glGetUniformLocation(programId, "u_ColorTemp")
        h_Tint        = GLES30.glGetUniformLocation(programId, "u_Tint")
        h_Saturation  = GLES30.glGetUniformLocation(programId, "u_Saturation")
        h_SkinTone    = GLES30.glGetUniformLocation(programId, "u_SkinTone")
        h_RedGain     = GLES30.glGetUniformLocation(programId, "u_RedGain")
        h_GreenGain   = GLES30.glGetUniformLocation(programId, "u_GreenGain")
        h_BlueGain    = GLES30.glGetUniformLocation(programId, "u_BlueGain")
        h_ColorBoost  = GLES30.glGetUniformLocation(programId, "u_ColorBoost")

        // Tab 3: 纹理
        h_Sharpness = GLES30.glGetUniformLocation(programId, "u_Sharpness")
        h_Denoise   = GLES30.glGetUniformLocation(programId, "u_Denoise")
        h_Grain     = GLES30.glGetUniformLocation(programId, "u_Grain")
        h_Vignette  = GLES30.glGetUniformLocation(programId, "u_Vignette")
        h_Clarity   = GLES30.glGetUniformLocation(programId, "u_Clarity")
        h_Dehaze    = GLES30.glGetUniformLocation(programId, "u_Dehaze")

        // Tab 4: 美颜
        h_BeautyGlobal = GLES30.glGetUniformLocation(programId, "u_BeautyGlobal")
        h_SkinSmooth   = GLES30.glGetUniformLocation(programId, "u_SkinSmooth")
        h_FaceThin     = GLES30.glGetUniformLocation(programId, "u_FaceThin")
        h_EyeEnlarge   = GLES30.glGetUniformLocation(programId, "u_EyeEnlarge")
        h_SkinWhiten   = GLES30.glGetUniformLocation(programId, "u_SkinWhiten")
        h_SkinRedden   = GLES30.glGetUniformLocation(programId, "u_SkinRedden")
        h_ChinAdjust   = GLES30.glGetUniformLocation(programId, "u_ChinAdjust")
        h_NoseBridge   = GLES30.glGetUniformLocation(programId, "u_NoseBridge")

        // D29
        h_LBSCompensation = GLES30.glGetUniformLocation(programId, "u_LBSCompensation")

        // 2.9D 视差
        h_ParallaxOffsetX = GLES30.glGetUniformLocation(programId, "u_ParallaxOffsetX")
        h_ParallaxOffsetY = GLES30.glGetUniformLocation(programId, "u_ParallaxOffsetY")

        lutTextureId = createDefaultLutTexture()

        Log.d(TAG, "GL program ready. programId=$programId")
        Log.d(TAG, "Uniform handles: h_Exposure=$h_Exposure, h_Saturation=$h_Saturation, h_Grain=$h_Grain, h_SkinSmooth=$h_SkinSmooth")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: ${width}x${height}")
        GLES30.glViewport(0, 0, width, height)
        viewportWidth = width
        viewportHeight = height
    }

    override fun onDrawFrame(gl: GL10?) {
        surfaceTexture?.updateTexImage()
        frameCount++

        GLES30.glClearColor(0f, 0f, 0f, 1f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(programId)

        // 绑定相机 OES 纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        GLES30.glUniform1i(uTextureHandle, 0)

        // 绑定 LUT 纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lutTextureId)
        GLES30.glUniform1i(uLutTextureHandle, 1)

        // 辅助参数
        GLES30.glUniform2f(uResolutionHandle, viewportWidth.toFloat(), viewportHeight.toFloat())
        GLES30.glUniform1f(uTimeHandle, frameCount.toFloat())

        // ── Tab 1: 曝光 ──────────────────────────────────────
        GLES30.glUniform1f(h_ISO,              p_ISO.toFloat())
        GLES30.glUniform1f(h_ShutterSpeed,     p_ShutterSpeed)
        GLES30.glUniform1f(h_Exposure,         p_Exposure)
        GLES30.glUniform1f(h_DynamicRange,     p_DynamicRange)
        GLES30.glUniform1f(h_ShadowComp,       p_ShadowComp)
        GLES30.glUniform1f(h_HighlightProtect, p_HighlightProtect)

        // ── Tab 2: 色彩 ──────────────────────────────────────
        GLES30.glUniform1f(h_ColorTemp,  p_ColorTemp)
        GLES30.glUniform1f(h_Tint,       p_Tint)
        GLES30.glUniform1f(h_Saturation, p_Saturation)
        GLES30.glUniform1f(h_SkinTone,   p_SkinTone)
        GLES30.glUniform1f(h_RedGain,    p_RedGain)
        GLES30.glUniform1f(h_GreenGain,  p_GreenGain)
        GLES30.glUniform1f(h_BlueGain,   p_BlueGain)
        GLES30.glUniform1f(h_ColorBoost, p_ColorBoost)

        // ── Tab 3: 纹理 ──────────────────────────────────────
        GLES30.glUniform1f(h_Sharpness, p_Sharpness)
        GLES30.glUniform1f(h_Denoise,   p_Denoise)
        GLES30.glUniform1f(h_Grain,     p_Grain)
        GLES30.glUniform1f(h_Vignette,  p_Vignette)
        GLES30.glUniform1f(h_Clarity,   p_Clarity)
        GLES30.glUniform1f(h_Dehaze,    p_Dehaze)

        // ── Tab 4: 美颜 ──────────────────────────────────────
        GLES30.glUniform1f(h_BeautyGlobal, p_BeautyGlobal)
        GLES30.glUniform1f(h_SkinSmooth,   p_SkinSmooth)
        GLES30.glUniform1f(h_FaceThin,     p_FaceThin)
        GLES30.glUniform1f(h_EyeEnlarge,   p_EyeEnlarge)
        GLES30.glUniform1f(h_SkinWhiten,   p_SkinWhiten)
        GLES30.glUniform1f(h_SkinRedden,   p_SkinRedden)
        GLES30.glUniform1f(h_ChinAdjust,   p_ChinAdjust)
        GLES30.glUniform1f(h_NoseBridge,   p_NoseBridge)

        // ── D29 ──────────────────────────────────────────────
        GLES30.glUniform1f(h_LBSCompensation, p_LBSCompensation)

        // ── 2.9D 视差 ─────────────────────────────────────────
        GLES30.glUniform1f(h_ParallaxOffsetX, p_ParallaxOffsetX)
        GLES30.glUniform1f(h_ParallaxOffsetY, p_ParallaxOffsetY)

        // 绘制
        GLES30.glEnableVertexAttribArray(aPositionHandle)
        GLES30.glVertexAttribPointer(aPositionHandle, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        GLES30.glEnableVertexAttribArray(aTexCoordHandle)
        GLES30.glVertexAttribPointer(aTexCoordHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

        GLES30.glDisableVertexAttribArray(aPositionHandle)
        GLES30.glDisableVertexAttribArray(aTexCoordHandle)
    }

    // ──────────────────────────────────────────────────────────
    // 公开 API：UI 滑块调用这些方法实现 1:1 双向绑定
    // 每次调用均输出 Logcat，格式：参数变化: <名称> (<英文>) = <值>
    // ──────────────────────────────────────────────────────────

    /** Tab 1: 曝光 — 6 个参数 */
    fun updateISO(value: Int) {
        p_ISO = value
        Log.d(TAG, "参数变化: ISO (iso) = $value")
    }

    fun updateShutterSpeed(value: Float) {
        p_ShutterSpeed = value
        Log.d(TAG, "参数变化: 快门速度 (shutterSpeed) = $value")
    }

    fun updateExposure(ev: Float) {
        p_Exposure = ev
        Log.d(TAG, "参数变化: 曝光补偿 (ev) = $ev EV")
    }

    fun updateDynamicRange(value: Float) {
        p_DynamicRange = value
        Log.d(TAG, "参数变化: 动态范围 (dynamicRange) = ${"%.1f".format(value * 100)}%")
    }

    fun updateShadowComp(value: Float) {
        p_ShadowComp = value
        Log.d(TAG, "参数变化: 阴影补偿 (shadowComp) = ${"%.1f".format(value * 100)}%")
    }

    fun updateHighlightProtect(value: Float) {
        p_HighlightProtect = value
        Log.d(TAG, "参数变化: 高光抑制 (highlightProtect) = ${"%.1f".format(value * 100)}%")
    }

    /** Tab 2: 色彩 — 8 个参数 */
    fun updateColorTemp(normalizedValue: Float) {
        p_ColorTemp = normalizedValue
        val kelvin = (2000 + normalizedValue * 8000).toInt()
        Log.d(TAG, "参数变化: 色温 (colorTemp) = ${kelvin}K")
    }

    fun updateTint(value: Float) {
        p_Tint = value
        Log.d(TAG, "参数变化: 色调 (tint) = ${"%.0f".format(value * 100)}")
    }

    fun updateSaturation(value: Float) {
        p_Saturation = value
        Log.d(TAG, "参数变化: 饱和度 (saturation) = ${"%.0f".format(value * 100)}%")
    }

    fun updateSkinTone(value: Float) {
        p_SkinTone = value
        Log.d(TAG, "参数变化: 肤色 (skinTone) = ${"%.1f".format(value * 100)}%")
    }

    fun updateRedGain(value: Float) {
        p_RedGain = value
        Log.d(TAG, "参数变化: 红色通道 (redGain) = ${"%.2f".format(value)}")
    }

    fun updateGreenGain(value: Float) {
        p_GreenGain = value
        Log.d(TAG, "参数变化: 绿色通道 (greenGain) = ${"%.2f".format(value)}")
    }

    fun updateBlueGain(value: Float) {
        p_BlueGain = value
        Log.d(TAG, "参数变化: 蓝色通道 (blueGain) = ${"%.2f".format(value)}")
    }

    fun updateColorBoost(value: Float) {
        p_ColorBoost = value
        Log.d(TAG, "参数变化: 色彩浓度 (colorBoost) = ${"%.1f".format(value * 100)}%")
    }

    /** Tab 3: 纹理 — 6 个参数 */
    fun updateSharpness(value: Float) {
        p_Sharpness = value
        Log.d(TAG, "参数变化: 锐度 (sharpness) = ${"%.1f".format(value * 100)}%")
    }

    fun updateDenoise(value: Float) {
        p_Denoise = value
        Log.d(TAG, "参数变化: 降噪 (denoise) = ${"%.1f".format(value * 100)}%")
    }

    fun updateGrain(value: Float) {
        p_Grain = value
        Log.d(TAG, "参数变化: 颗粒 (grain) = ${"%.1f".format(value * 100)}%")
    }

    fun updateVignette(value: Float) {
        p_Vignette = value
        Log.d(TAG, "参数变化: 暗角 (vignette) = ${"%.1f".format(value * 100)}%")
    }

    fun updateClarity(value: Float) {
        p_Clarity = value
        Log.d(TAG, "参数变化: 清晰度 (clarity) = ${"%.0f".format(value * 100)}")
    }

    fun updateDehaze(value: Float) {
        p_Dehaze = value
        Log.d(TAG, "参数变化: 去雾 (dehaze) = ${"%.1f".format(value * 100)}%")
    }

    /** Tab 4: 美颜 — 8 个参数 */
    fun updateBeautyGlobal(value: Float) {
        p_BeautyGlobal = value
        Log.d(TAG, "参数变化: 全局美颜 (beautyGlobal) = ${"%.1f".format(value * 100)}%")
    }

    fun updateSkinSmooth(value: Float) {
        p_SkinSmooth = value
        Log.d(TAG, "参数变化: 磨皮 (skinSmooth) = ${"%.1f".format(value * 100)}%")
    }

    fun updateFaceThin(value: Float) {
        p_FaceThin = value
        Log.d(TAG, "参数变化: 瘦脸 (faceThin) = ${"%.1f".format(value * 100)}%")
    }

    fun updateEyeEnlarge(value: Float) {
        p_EyeEnlarge = value
        Log.d(TAG, "参数变化: 大眼 (eyeEnlarge) = ${"%.1f".format(value * 100)}%")
    }

    fun updateSkinWhiten(value: Float) {
        p_SkinWhiten = value
        Log.d(TAG, "参数变化: 美白 (skinWhiten) = ${"%.1f".format(value * 100)}%")
    }

    fun updateSkinRedden(value: Float) {
        p_SkinRedden = value
        Log.d(TAG, "参数变化: 红润 (skinRedden) = ${"%.1f".format(value * 100)}%")
    }

    fun updateChinAdjust(value: Float) {
        p_ChinAdjust = value
        Log.d(TAG, "参数变化: 下巴 (chinAdjust) = ${"%.0f".format(value * 100)}")
    }

    fun updateNoseBridge(value: Float) {
        p_NoseBridge = value
        Log.d(TAG, "参数变化: 鼻梁 (noseBridge) = ${"%.0f".format(value * 100)}")
    }

    /** D29: LBS 环境光补偿 */
    fun updateLBSCompensation(value: Float) {
        p_LBSCompensation = value
        Log.d(TAG, "参数变化: LBS补偿 (lbsCompensation) = ${"%.2f".format(value)}")
    }

    /** 2.9D 视差（陀螺仪驱动） */
    fun updateParallaxOffset(offsetX: Float, offsetY: Float) {
        p_ParallaxOffsetX = offsetX
        p_ParallaxOffsetY = offsetY
    }

    /** 一键重置所有参数到默认值 */
    fun resetAll29DParams() {
        p_ISO = 400; p_ShutterSpeed = 0.5f; p_Exposure = 0.0f
        p_DynamicRange = 0.5f; p_ShadowComp = 0.3f; p_HighlightProtect = 0.3f
        p_ColorTemp = 0.4375f; p_Tint = 0.0f; p_Saturation = 1.0f
        p_SkinTone = 0.5f; p_RedGain = 1.0f; p_GreenGain = 1.0f
        p_BlueGain = 1.0f; p_ColorBoost = 0.0f
        p_Sharpness = 0.5f; p_Denoise = 0.3f; p_Grain = 0.0f
        p_Vignette = 0.0f; p_Clarity = 0.0f; p_Dehaze = 0.0f
        p_BeautyGlobal = 0.5f; p_SkinSmooth = 0.6f; p_FaceThin = 0.3f
        p_EyeEnlarge = 0.2f; p_SkinWhiten = 0.4f; p_SkinRedden = 0.2f
        p_ChinAdjust = 0.0f; p_NoseBridge = 0.0f; p_LBSCompensation = 0.0f
        Log.d(TAG, "AUDIT_PARAMS: 所有 29D 参数已重置为默认值")
    }

    /** 批量更新专业模式参数（Camera2 CaptureRequest 同步） */
    fun updateProParams(iso: Int, exposureTimeNs: Long, whiteBalanceK: Int) {
        p_ISO = iso
        // 将 Camera2 硬件快门时间映射到 [0,1]
        val shutterSec = exposureTimeNs / 1_000_000_000.0f
        p_ShutterSpeed = (shutterSec.coerceIn(1f / 8000f, 30f) / 30f)
        p_ColorTemp = ((whiteBalanceK - 2000f) / 8000f).coerceIn(0f, 1f)
        Log.d(TAG, "AUDIT_PARAMS: ISO=$iso, ExposureTime=${exposureTimeNs}ns (${shutterSec}s), WhiteBalance=${whiteBalanceK}K")
    }

    /** 更新 RGB 曲线 LUT 纹理 */
    fun updateLutTexture(lutData: ByteArray) {
        Log.d(TAG, "LUT_ARRAY: ${lutData.take(64).joinToString(", ")}")
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lutTextureId)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB, 256, 1, 0,
            GLES30.GL_RGB, GLES30.GL_UNSIGNED_BYTE, ByteBuffer.wrap(lutData)
        )
    }

    // ──────────────────────────────────────────────────────────
    // 私有辅助方法
    // ──────────────────────────────────────────────────────────

    private fun createOESTexture(): Int {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        val id = textures[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, id)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        return id
    }

    private fun createDefaultLutTexture(): Int {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        val id = textures[0]
        val lutData = ByteArray(256 * 3) { i -> (i / 3).toByte() }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, id)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB, 256, 1, 0,
            GLES30.GL_RGB, GLES30.GL_UNSIGNED_BYTE, ByteBuffer.wrap(lutData))
        return id
    }

    private fun loadShaderFromAssets(path: String): String {
        return try {
            context.assets.open(path).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load shader: $path", e)
            ""
        }
    }

    private fun createProgram(vertexCode: String, fragmentCode: String): Int {
        val vs = loadShader(GLES30.GL_VERTEX_SHADER, vertexCode)
        val fs = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentCode)
        if (vs == 0 || fs == 0) return 0

        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vs)
        GLES30.glAttachShader(program, fs)
        GLES30.glLinkProgram(program)

        val status = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(TAG, "Link error: ${GLES30.glGetProgramInfoLog(program)}")
            GLES30.glDeleteProgram(program)
            return 0
        }
        return program
    }

    private fun loadShader(type: Int, code: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, code)
        GLES30.glCompileShader(shader)
        val status = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(TAG, "Compile error: ${GLES30.glGetShaderInfoLog(shader)}")
            GLES30.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    fun release() {
        surfaceTexture?.release()
        surfaceTexture = null
        if (programId != 0) { GLES30.glDeleteProgram(programId); programId = 0 }
        if (oesTextureId != 0) { GLES30.glDeleteTextures(1, intArrayOf(oesTextureId), 0); oesTextureId = 0 }
        if (lutTextureId != 0) { GLES30.glDeleteTextures(1, intArrayOf(lutTextureId), 0); lutTextureId = 0 }
    }
}
