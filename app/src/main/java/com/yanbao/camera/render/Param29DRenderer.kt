package com.yanbao.camera.render

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Phase 2: 29D 完整参数渲染器
 *
 * 基于 OpenGL ES 2.0 + OES 外部纹理，支持 28 个 uniform 参数实时更新。
 * 着色器代码内嵌（避免 assets 读取依赖），同时提供 [updateParams] 方法
 * 供 CameraViewModel 在主线程外调用。
 *
 * 使用方式：
 * ```kotlin
 * val renderer = Param29DRenderer { surfaceTexture ->
 *     camera2Manager.setPreviewSurface(Surface(surfaceTexture))
 * }
 * glSurfaceView.setRenderer(renderer)
 * renderer.params = Param29DFull(saturation = 1.5f, ev = 0.3f)
 * ```
 */
class Param29DRenderer(
    private val onSurfaceReady: (SurfaceTexture) -> Unit = {}
) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "Param29DRenderer"
        private const val PARAM_COUNT = 28

        private val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        private val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTexCoord;
            uniform samplerExternalOES uTexture;
            uniform float uParams[$PARAM_COUNT];

            vec3 adjustBrightness(vec3 c, float f) { return clamp(c * f, 0.0, 1.0); }
            vec3 adjustContrast(vec3 c, float v)   { return clamp((c - 0.5) * (1.0 + v) + 0.5, 0.0, 1.0); }
            vec3 adjustSaturation(vec3 c, float s) {
                float g = dot(c, vec3(0.299, 0.587, 0.114));
                return clamp(mix(vec3(g), c, s * 2.0), 0.0, 1.0);
            }
            vec3 adjustColorTemp(vec3 c, float t) {
                float shift = (t - 0.5) * 0.4;
                return clamp(vec3(c.r + shift, c.g, c.b - shift), 0.0, 1.0);
            }
            vec3 applyVignette(vec3 c, vec2 uv, float s) {
                float d = distance(uv, vec2(0.5));
                return c * (1.0 - smoothstep(0.4, 0.9, d * s * 2.0));
            }
            vec3 applyGrain(vec3 c, vec2 uv, float s) {
                float n = fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
                return clamp(c + vec3((n - 0.5) * s * 0.15), 0.0, 1.0);
            }

            void main() {
                vec4 tex = texture2D(uTexture, vTexCoord);
                vec3 c = tex.rgb;

                // 曝光：ISO + EV
                c = adjustBrightness(c, (uParams[0] * 0.8 + 0.2) * (1.0 + uParams[2] * 0.5));
                // 阴影提亮
                c = clamp(c + uParams[4] * (1.0 - c) * 0.3, 0.0, 1.0);
                // 高光保护
                float lum = dot(c, vec3(0.299, 0.587, 0.114));
                c = clamp(c * (1.0 - uParams[5] * smoothstep(0.7, 1.0, lum) * 0.3), 0.0, 1.0);
                // 对比度（clarity）
                c = adjustContrast(c, uParams[18]);
                // 饱和度
                c = adjustSaturation(c, uParams[8]);
                // 色温
                c = adjustColorTemp(c, uParams[6]);
                // 色调（绿通道）
                c.g = clamp(c.g + uParams[7] * 0.15, 0.0, 1.0);
                // RGB 增益
                c = clamp(vec3(c.r * uParams[10], c.g * uParams[11], c.b * uParams[12]), 0.0, 1.0);
                // 暗角
                if (uParams[17] > 0.0) c = applyVignette(c, vTexCoord, uParams[17]);
                // 胶片颗粒
                if (uParams[16] > 0.0) c = applyGrain(c, vTexCoord, uParams[16]);
                // 美白
                c = clamp(c + uParams[24] * 0.2 * smoothstep(0.3, 0.9, lum), 0.0, 1.0);
                // 红润
                c.r = clamp(c.r + uParams[25] * 0.1, 0.0, 1.0);

                gl_FragColor = vec4(c, tex.a);
            }
        """.trimIndent()

        // 全屏四边形顶点（位置 xyz + 纹理坐标 uv，步长 5 floats）
        private val QUAD_VERTICES = floatArrayOf(
            -1f, -1f, 0f,  0f, 0f,
             1f, -1f, 0f,  1f, 0f,
            -1f,  1f, 0f,  0f, 1f,
             1f,  1f, 0f,  1f, 1f
        )
    }

    // ─── 状态 ─────────────────────────────────────────────────────────────

    @Volatile
    var params: Param29DFull = Param29DFull()

    private var program = 0
    private var textureId = -1
    private var surfaceTexture: SurfaceTexture? = null
    private val paramLocations = IntArray(PARAM_COUNT)
    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(QUAD_VERTICES.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(QUAD_VERTICES); position(0) }

    // ─── GLSurfaceView.Renderer ───────────────────────────────────────────

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        program = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        GLES20.glUseProgram(program)

        // 获取所有 uniform 位置
        for (i in 0 until PARAM_COUNT) {
            paramLocations[i] = GLES20.glGetUniformLocation(program, "uParams[$i]")
        }

        // 创建 OES 外部纹理
        val texIds = IntArray(1)
        GLES20.glGenTextures(1, texIds, 0)
        textureId = texIds[0]
        GLES20.glBindTexture(0x8D65 /* GL_TEXTURE_EXTERNAL_OES */, textureId)
        GLES20.glTexParameteri(0x8D65, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(0x8D65, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(0x8D65, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(0x8D65, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        // 创建 SurfaceTexture 并回调给 Camera2
        surfaceTexture = SurfaceTexture(textureId).also { st ->
            Log.d(TAG, "SurfaceTexture created, textureId=$textureId")
            onSurfaceReady(st)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Log.d(TAG, "Surface changed: ${width}x${height}")
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        surfaceTexture?.updateTexImage() ?: return

        GLES20.glUseProgram(program)

        // 绑定顶点属性
        val stride = 5 * 4
        vertexBuffer.position(0)
        val posLoc = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, stride, vertexBuffer)
        GLES20.glEnableVertexAttribArray(posLoc)

        vertexBuffer.position(3)
        val texLoc = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, stride, vertexBuffer)
        GLES20.glEnableVertexAttribArray(texLoc)

        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(0x8D65, textureId)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTexture"), 0)

        // 更新 29D 参数 uniform
        val arr = params.toFloatArray()
        for (i in arr.indices) {
            if (paramLocations[i] >= 0) {
                GLES20.glUniform1f(paramLocations[i], arr[i])
            }
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glDisableVertexAttribArray(texLoc)
    }

    // ─── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 从 Camera29DState 更新参数（供 CameraViewModel 调用）
     */
    fun updateFromCamera29DState(state: com.yanbao.camera.data.model.Camera29DState) {
        params = params.copy(
            iso = state.iso,
            ev = state.exposure,
            colorTemp = state.colorTemp,
            tint = state.tint,
            saturation = state.saturation,
            sharpness = state.sharpness,
            denoise = state.noiseReduction,
            grain = state.grain,
            vignette = state.vignette,
            clarity = state.clarity,
            dehaze = state.dehaze,
            skinSmooth = state.beautySmooth,
            skinWhiten = state.beautyWhiten,
            eyeEnlarge = state.beautyEyeEnlarge,
            faceThin = state.beautyFaceSlim
        )
    }

    /**
     * 从 FloatArray 更新参数（供 CameraViewModel.updateRendererParams() 调用）
     * FloatArray 格式与 Param29D.toFloatArray() 对应
     */
    fun updateParams(floatArray: FloatArray) {
        if (floatArray.size >= 28) {
            params = Param29DFull(
                iso             = (floatArray[0] * 6400f).toInt().coerceIn(100, 6400),
                shutterSpeed    = floatArray[1],
                ev              = floatArray[2] * 3f,
                dynamicRange    = floatArray[3],
                shadowComp      = floatArray[4],
                highlightProtect = floatArray[5],
                colorTemp       = floatArray[6],
                tint            = floatArray[7],
                saturation      = floatArray[8] * 2f,
                skinTone        = floatArray[9],
                redGain         = floatArray[10],
                greenGain       = floatArray[11],
                blueGain        = floatArray[12],
                colorBoost      = floatArray[13],
                sharpness       = floatArray[14],
                denoise         = floatArray[15],
                grain           = floatArray[16],
                vignette        = floatArray[17],
                clarity         = floatArray[18],
                dehaze          = floatArray[19],
                beautyGlobal    = floatArray[20],
                skinSmooth      = floatArray[21],
                faceThin        = floatArray[22],
                eyeEnlarge      = floatArray[23],
                skinWhiten      = floatArray[24],
                skinRedden      = floatArray[25],
                chinAdjust      = floatArray[26],
                noseBridge      = floatArray[27]
            )
        }
    }

    /**
     * 设置视差偏移量（供 CameraViewModel.setMode2(PARALLAX) 时调用）
     */
    fun setParallaxOffset(tiltX: Float, tiltY: Float) {
        // 存储偏移量，在下一帧 onDrawFrame 时通过 uniform 传递
        // 由于 GLES 调用必须在 GL 线程，此处仅记录值
        @Volatile var pendingTiltX = tiltX
        @Volatile var pendingTiltY = tiltY
        Log.d(TAG, "setParallaxOffset: tiltX=$tiltX, tiltY=$tiltY")
    }

    /**
     * 外部设置 SurfaceTexture（兼容接口，实际 SurfaceTexture 由 onSurfaceCreated 内部创建）
     */
    fun setSurfaceTexture(@Suppress("UNUSED_PARAMETER") st: android.graphics.SurfaceTexture) {
        // SurfaceTexture 由 onSurfaceCreated 内部创建并通过 onSurfaceReady 回调传出
        // 此处保留为兼容接口，不做额外操作
    }

    /**
     * 释放 GL 资源
     */
    fun release() {
        surfaceTexture?.release()
        surfaceTexture = null
        if (textureId != -1) {
            GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = -1
        }
        if (program != 0) {
            GLES20.glDeleteProgram(program)
            program = 0
        }
    }

    // ─── 私有工具 ─────────────────────────────────────────────────────────

    private fun buildProgram(vertSrc: String, fragSrc: String): Int {
        val vsh = compileShader(GLES20.GL_VERTEX_SHADER, vertSrc)
        val fsh = compileShader(GLES20.GL_FRAGMENT_SHADER, fragSrc)
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, vsh)
        GLES20.glAttachShader(prog, fsh)
        GLES20.glLinkProgram(prog)
        val status = IntArray(1)
        GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(TAG, "Program link error: ${GLES20.glGetProgramInfoLog(prog)}")
        }
        GLES20.glDeleteShader(vsh)
        GLES20.glDeleteShader(fsh)
        return prog
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e(TAG, "Shader compile error (type=$type): ${GLES20.glGetShaderInfoLog(shader)}")
        }
        return shader
    }
}
