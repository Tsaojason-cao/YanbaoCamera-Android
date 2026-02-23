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
 * Phase 2: 2.9D 视差渲染器
 *
 * 在顶点着色器中通过 uniform vec2 uParallaxOffset 实现视差偏移，
 * 配合 [com.yanbao.camera.sensor.ParallaxSensor] 的陀螺仪数据实现
 * 手机倾斜时画面微移的 2.9D 视差效果。
 *
 * 视差强度通过 [parallaxScale] 控制，默认 0.05（5% 画面宽度）。
 */
class ParallaxRenderer(
    private val onSurfaceReady: (SurfaceTexture) -> Unit = {}
) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "ParallaxRenderer"

        private val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            uniform vec2 uParallaxOffset;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                // 视差偏移：纹理坐标随倾斜角度偏移
                vTexCoord = aTexCoord + uParallaxOffset;
            }
        """.trimIndent()

        private val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTexCoord;
            uniform samplerExternalOES uTexture;
            void main() {
                // 边界裁剪（避免视差偏移后出现黑边）
                vec2 uv = clamp(vTexCoord, 0.0, 1.0);
                gl_FragColor = texture2D(uTexture, uv);
            }
        """.trimIndent()

        // 全屏四边形（位置 + 纹理坐标，步长 5 floats）
        private val QUAD = floatArrayOf(
            -1f, -1f, 0f,  0f, 0f,
             1f, -1f, 0f,  1f, 0f,
            -1f,  1f, 0f,  0f, 1f,
             1f,  1f, 0f,  1f, 1f
        )
    }

    // ─── 状态 ─────────────────────────────────────────────────────────────

    /** 视差强度缩放系数（0=无视差, 0.1=强视差） */
    var parallaxScale: Float = 0.05f

    /** 当前视差偏移（由 ParallaxSensor 提供） */
    @Volatile
    private var offsetX: Float = 0f
    @Volatile
    private var offsetY: Float = 0f

    private var program = 0
    private var textureId = -1
    private var surfaceTexture: SurfaceTexture? = null
    private var parallaxOffsetLoc = -1

    private val vertexBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(QUAD.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(QUAD); position(0) }

    // ─── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 更新视差偏移（由 ParallaxSensor 回调调用）
     * @param tiltX 水平倾斜角（弧度）
     * @param tiltY 垂直倾斜角（弧度）
     */
    fun setParallaxOffset(tiltX: Float, tiltY: Float) {
        offsetX = tiltX * parallaxScale
        offsetY = tiltY * parallaxScale
    }

    // ─── GLSurfaceView.Renderer ───────────────────────────────────────────

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        program = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        GLES20.glUseProgram(program)

        parallaxOffsetLoc = GLES20.glGetUniformLocation(program, "uParallaxOffset")

        // 创建 OES 外部纹理
        val texIds = IntArray(1)
        GLES20.glGenTextures(1, texIds, 0)
        textureId = texIds[0]
        val GL_TEXTURE_EXTERNAL_OES = 0x8D65
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        surfaceTexture = SurfaceTexture(textureId).also { st ->
            Log.d(TAG, "ParallaxRenderer SurfaceTexture created")
            onSurfaceReady(st)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        surfaceTexture?.updateTexImage() ?: return

        GLES20.glUseProgram(program)

        // 设置视差偏移 uniform
        GLES20.glUniform2f(parallaxOffsetLoc, offsetX, offsetY)

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
