package com.yanbao.camera.beauty

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
 * Phase 2: 美颜渲染器
 *
 * 基于 OpenGL ES 2.0 实现以下美颜效果（全图版，无需人脸关键点）：
 * - 磨皮（双边模糊近似，通过多次采样实现）
 * - 美白（提亮高光区域）
 * - 红润（增加红色通道）
 * - 全局美颜强度控制
 *
 * 精准美颜（瘦脸、大眼）需配合 [FaceDetector] 提供的关键点，
 * 通过顶点变形或片段着色器扭曲实现（后续版本扩展）。
 */
class BeautyRenderer(
    private val onSurfaceReady: (SurfaceTexture) -> Unit = {}
) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "BeautyRenderer"
        private const val GL_TEXTURE_EXTERNAL_OES = 0x8D65

        private val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        /**
         * 美颜片段着色器
         *
         * 磨皮算法：9 点均值模糊（近似双边滤波）
         * - 采样周围 8 个像素 + 中心像素
         * - 根据亮度差异控制混合权重（保护边缘）
         */
        private val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTexCoord;
            uniform samplerExternalOES uTexture;

            // 美颜参数
            uniform float uSkinSmooth;    // 磨皮强度 0-1
            uniform float uSkinWhiten;    // 美白强度 0-1
            uniform float uSkinRedden;    // 红润强度 0-1
            uniform float uBeautyGlobal;  // 全局美颜权重 0-1
            uniform vec2 uTexelSize;      // 纹素大小（1/width, 1/height）

            // 亮度计算
            float luminance(vec3 c) {
                return dot(c, vec3(0.299, 0.587, 0.114));
            }

            // 9点均值模糊（磨皮）
            vec3 bilateralBlur(vec2 uv) {
                vec3 center = texture2D(uTexture, uv).rgb;
                float centerLum = luminance(center);
                vec3 sum = center;
                float weight = 1.0;

                // 8 邻域采样
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        vec2 offset = vec2(float(dx), float(dy)) * uTexelSize;
                        vec3 neighbor = texture2D(uTexture, uv + offset).rgb;
                        float neighborLum = luminance(neighbor);
                        // 亮度差异越大，权重越小（保护边缘）
                        float lumDiff = abs(centerLum - neighborLum);
                        float w = exp(-lumDiff * lumDiff * 50.0);
                        sum += neighbor * w;
                        weight += w;
                    }
                }
                return sum / weight;
            }

            void main() {
                vec4 texColor = texture2D(uTexture, vTexCoord);
                vec3 original = texColor.rgb;
                vec3 result = original;

                // 磨皮：将原始颜色与模糊颜色混合
                if (uSkinSmooth > 0.0) {
                    vec3 blurred = bilateralBlur(vTexCoord);
                    result = mix(result, blurred, uSkinSmooth * uBeautyGlobal);
                }

                // 美白：提亮高亮区域
                if (uSkinWhiten > 0.0) {
                    float lum = luminance(result);
                    float whitenAmount = uSkinWhiten * uBeautyGlobal * 0.25;
                    result = clamp(result + whitenAmount * smoothstep(0.3, 0.9, lum), 0.0, 1.0);
                }

                // 红润：增加红色通道
                if (uSkinRedden > 0.0) {
                    result.r = clamp(result.r + uSkinRedden * uBeautyGlobal * 0.12, 0.0, 1.0);
                }

                gl_FragColor = vec4(result, texColor.a);
            }
        """.trimIndent()

        private val QUAD = floatArrayOf(
            -1f, -1f, 0f,  0f, 0f,
             1f, -1f, 0f,  1f, 0f,
            -1f,  1f, 0f,  0f, 1f,
             1f,  1f, 0f,  1f, 1f
        )
    }

    // ─── 美颜参数（线程安全） ─────────────────────────────────────────────

    @Volatile var skinSmooth: Float = 0.6f
    @Volatile var skinWhiten: Float = 0.4f
    @Volatile var skinRedden: Float = 0.2f
    @Volatile var beautyGlobal: Float = 0.5f

    private var program = 0
    private var textureId = -1
    private var surfaceTexture: SurfaceTexture? = null
    private var viewportWidth = 1
    private var viewportHeight = 1

    // Uniform locations
    private var uSkinSmoothLoc = -1
    private var uSkinWhitenLoc = -1
    private var uSkinReddenLoc = -1
    private var uBeautyGlobalLoc = -1
    private var uTexelSizeLoc = -1

    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(QUAD.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(QUAD); position(0) }

    // ─── GLSurfaceView.Renderer ───────────────────────────────────────────

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        program = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        GLES20.glUseProgram(program)

        uSkinSmoothLoc  = GLES20.glGetUniformLocation(program, "uSkinSmooth")
        uSkinWhitenLoc  = GLES20.glGetUniformLocation(program, "uSkinWhiten")
        uSkinReddenLoc  = GLES20.glGetUniformLocation(program, "uSkinRedden")
        uBeautyGlobalLoc= GLES20.glGetUniformLocation(program, "uBeautyGlobal")
        uTexelSizeLoc   = GLES20.glGetUniformLocation(program, "uTexelSize")

        val texIds = IntArray(1)
        GLES20.glGenTextures(1, texIds, 0)
        textureId = texIds[0]
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        surfaceTexture = SurfaceTexture(textureId).also { st ->
            Log.d(TAG, "BeautyRenderer SurfaceTexture created")
            onSurfaceReady(st)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        viewportWidth = width
        viewportHeight = height
        Log.d(TAG, "BeautyRenderer surface: ${width}x${height}")
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        surfaceTexture?.updateTexImage() ?: return

        GLES20.glUseProgram(program)

        // 更新美颜 uniform
        GLES20.glUniform1f(uSkinSmoothLoc, skinSmooth)
        GLES20.glUniform1f(uSkinWhitenLoc, skinWhiten)
        GLES20.glUniform1f(uSkinReddenLoc, skinRedden)
        GLES20.glUniform1f(uBeautyGlobalLoc, beautyGlobal)
        GLES20.glUniform2f(uTexelSizeLoc,
            1f / viewportWidth.toFloat(),
            1f / viewportHeight.toFloat()
        )

        val stride = 5 * 4
        vertexBuffer.position(0)
        val posLoc = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, stride, vertexBuffer)
        GLES20.glEnableVertexAttribArray(posLoc)

        vertexBuffer.position(3)
        val texLoc = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, stride, vertexBuffer)
        GLES20.glEnableVertexAttribArray(texLoc)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTexture"), 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glDisableVertexAttribArray(texLoc)
    }

    // ─── 私有工具 ─────────────────────────────────────────────────────────

    private fun buildProgram(vertSrc: String, fragSrc: String): Int {
        val vsh = compileShader(GLES20.GL_VERTEX_SHADER, vertSrc)
        val fsh = compileShader(GLES20.GL_FRAGMENT_SHADER, fragSrc)
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, vsh)
        GLES20.glAttachShader(prog, fsh)
        GLES20.glLinkProgram(prog)
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
            Log.e(TAG, "Shader compile error: ${GLES20.glGetShaderInfoLog(shader)}")
        }
        return shader
    }

    fun release() {
        surfaceTexture?.release()
        surfaceTexture = null
        if (textureId != -1) GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
        if (program != 0) GLES20.glDeleteProgram(program)
    }
}
