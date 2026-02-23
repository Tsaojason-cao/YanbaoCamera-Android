package com.yanbao.camera.render

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL ES 2.0 渲染器 - Phase 1 用户指定版本
 *
 * 功能：
 * 1. 创建 OES 纹理接收 Camera2 预览帧
 * 2. 实时应用亮度/对比度/饱和度 uniform 参数
 * 3. 支持 29D 参数实时预览效果
 *
 * 审计日志：参数变化输出 AUDIT_GL 标签
 */
class GLRenderer : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "GLRenderer"

        private val vertexShaderCode = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        private val fragmentShaderCode = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTexCoord;
            uniform samplerExternalOES sTexture;
            uniform float u_brightness;
            uniform float u_contrast;
            uniform float u_saturation;

            void main() {
                vec4 color = texture2D(sTexture, vTexCoord);
                // 亮度调整
                color.rgb += u_brightness;
                // 对比度调整
                color.rgb = (color.rgb - 0.5) * u_contrast + 0.5;
                // 饱和度调整（灰度混合）
                float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
                color.rgb = mix(vec3(gray), color.rgb, u_saturation);
                gl_FragColor = color;
            }
        """.trimIndent()
    }

    // 顶点坐标（全屏四边形，含纹理坐标）
    private val squareCoords = floatArrayOf(
        -1f, -1f, 0f, 0f, 0f, // 左下：position(3) + texCoord(2)
         1f, -1f, 0f, 1f, 0f, // 右下
        -1f,  1f, 0f, 0f, 1f, // 左上
         1f,  1f, 0f, 1f, 1f  // 右上
    )
    private val vertexStride = 5 * 4 // 5 floats * 4 bytes per float

    private var program = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0
    private var brightnessLoc = 0
    private var contrastLoc = 0
    private var saturationLoc = 0

    // 29D 参数（外部实时更新）
    @Volatile var brightness = 0f
    @Volatile var contrast = 1f
    @Volatile var saturation = 1f

    private var textureId = -1
    private var surfaceTexture: SurfaceTexture? = null
    private val transformMatrix = FloatArray(16)

    private lateinit var vertexBuffer: FloatBuffer

    // SurfaceTexture 就绪回调
    var onSurfaceTextureReady: ((SurfaceTexture) -> Unit)? = null

    fun setSurfaceTexture(st: SurfaceTexture) {
        this.surfaceTexture = st
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        // 编译着色器
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        textureHandle = GLES20.glGetUniformLocation(program, "sTexture")
        brightnessLoc = GLES20.glGetUniformLocation(program, "u_brightness")
        contrastLoc = GLES20.glGetUniformLocation(program, "u_contrast")
        saturationLoc = GLES20.glGetUniformLocation(program, "u_saturation")

        // 创建 OES 纹理
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // 创建 SurfaceTexture 并通知外部
        val st = SurfaceTexture(textureId)
        surfaceTexture = st
        onSurfaceTextureReady?.invoke(st)
        Log.d(TAG, "AUDIT_GL: SurfaceTexture created, textureId=$textureId")

        // 顶点缓冲区
        vertexBuffer = ByteBuffer.allocateDirect(squareCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(squareCoords)
                position(0)
            }
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Log.d(TAG, "AUDIT_GL: Surface changed to ${width}x${height}")
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        surfaceTexture?.updateTexImage()
        surfaceTexture?.getTransformMatrix(transformMatrix)

        GLES20.glUseProgram(program)

        // 设置顶点属性 - position
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        // 设置顶点属性 - texCoord
        vertexBuffer.position(3)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        // 绑定 OES 纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        // 设置 29D uniform 参数
        GLES20.glUniform1f(brightnessLoc, brightness)
        GLES20.glUniform1f(contrastLoc, contrast)
        GLES20.glUniform1f(saturationLoc, saturation)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    /**
     * 更新 29D 参数（线程安全，由 ViewModel 调用）
     */
    fun updateParams(brightness: Float, contrast: Float, saturation: Float) {
        this.brightness = brightness
        this.contrast = contrast
        this.saturation = saturation
        Log.d(TAG, "AUDIT_GL: Params updated - brightness=$brightness, contrast=$contrast, saturation=$saturation")
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            // 检查编译状态
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                val error = GLES20.glGetShaderInfoLog(shader)
                Log.e(TAG, "Shader compile error: $error")
                GLES20.glDeleteShader(shader)
            }
        }
    }
}
