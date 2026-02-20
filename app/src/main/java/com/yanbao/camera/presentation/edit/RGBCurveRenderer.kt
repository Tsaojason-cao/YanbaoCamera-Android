package com.yanbao.camera.presentation.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * RGB 曲线 OpenGL ES 3.0 渲染器
 * 
 * 使用 LUT (Look-Up Table) 纹理实现真实的像素级色彩映射
 */
class RGBCurveRenderer(private val context: Context) {
    
    companion object {
        private const val TAG = "RGBCurveRenderer"
        
        // 顶点着色器（从 res/raw/vertex_shader.vert 加载）
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
        
        // 片元着色器（从 res/raw/rgb_curve_shader.frag 加载）
        private const val FRAGMENT_SHADER_CODE = """
            #version 300 es
            precision mediump float;
            
            uniform sampler2D uTexture;
            uniform sampler2D uCurveLut;
            
            in vec2 vTexCoord;
            out vec4 fragColor;
            
            void main() {
                vec4 base = texture(uTexture, vTexCoord);
                
                // 使用原始 RGB 值作为 LUT 纹理坐标
                float r = texture(uCurveLut, vec2(base.r, 0.125)).r;
                float g = texture(uCurveLut, vec2(base.g, 0.375)).g;
                float b = texture(uCurveLut, vec2(base.b, 0.625)).b;
                
                fragColor = vec4(r, g, b, base.a);
            }
        """
    }
    
    private var programId = 0
    private var textureId = 0
    private var lutTextureId = 0
    
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0
    private var curveLutHandle = 0
    
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer
    
    // 当前曲线控制点
    private var curvePoints = getDefaultCurvePoints()
    
    init {
        initBuffers()
    }
    
    /**
     * 初始化顶点和纹理坐标缓冲区
     */
    private fun initBuffers() {
        // 顶点坐标（全屏四边形）
        val vertices = floatArrayOf(
            -1f, -1f,  // 左下
             1f, -1f,  // 右下
            -1f,  1f,  // 左上
             1f,  1f   // 右上
        )
        
        // 纹理坐标
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
        // 编译着色器
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        
        // 创建程序
        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)
        GLES30.glLinkProgram(programId)
        
        // 检查链接状态
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES30.glGetProgramInfoLog(programId)
            Log.e(TAG, "Program link error: $error")
            GLES30.glDeleteProgram(programId)
            programId = 0
            return
        }
        
        // 获取句柄
        positionHandle = GLES30.glGetAttribLocation(programId, "aPosition")
        texCoordHandle = GLES30.glGetAttribLocation(programId, "aTexCoord")
        textureHandle = GLES30.glGetUniformLocation(programId, "uTexture")
        curveLutHandle = GLES30.glGetUniformLocation(programId, "uCurveLut")
        
        // 生成 LUT 纹理
        generateLUTTexture()
        
        Log.d(TAG, "OpenGL initialized successfully")
    }
    
    /**
     * 加载着色器
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        
        // 检查编译状态
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
     * 生成 LUT 纹理
     */
    private fun generateLUTTexture() {
        // 使用三次样条插值生成 256 阶 LUT
        val interpolator = CubicSplineInterpolator(curvePoints)
        val lut = interpolator.generateLUT()
        
        // 创建 256x4 的纹理（R/G/B/全通道各一行）
        val lutBitmap = Bitmap.createBitmap(256, 4, Bitmap.Config.ARGB_8888)
        
        for (x in 0..255) {
            val value = lut[x].toInt() and 0xFF
            
            // R 通道（y=0）
            lutBitmap.setPixel(x, 0, (value shl 16) or 0xFF000000.toInt())
            
            // G 通道（y=1）
            lutBitmap.setPixel(x, 1, (value shl 8) or 0xFF000000.toInt())
            
            // B 通道（y=2）
            lutBitmap.setPixel(x, 2, value or 0xFF000000.toInt())
            
            // 全通道（y=3）
            lutBitmap.setPixel(x, 3, (value shl 16) or (value shl 8) or value or 0xFF000000.toInt())
        }
        
        // 上传到 GPU
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        lutTextureId = textures[0]
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lutTextureId)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, lutBitmap, 0)
        
        lutBitmap.recycle()
        
        Log.d(TAG, "LUT texture generated: $lutTextureId")
    }
    
    /**
     * 更新曲线控制点
     */
    fun updateCurvePoints(points: List<PointF>) {
        curvePoints = points
        generateLUTTexture()
    }
    
    /**
     * 渲染图像
     */
    fun render(bitmap: Bitmap): Bitmap {
        // 上传原始图像到纹理
        if (textureId == 0) {
            val textures = IntArray(1)
            GLES30.glGenTextures(1, textures, 0)
            textureId = textures[0]
        }
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        
        // 使用程序
        GLES30.glUseProgram(programId)
        
        // 绑定纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(textureHandle, 0)
        
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, lutTextureId)
        GLES30.glUniform1i(curveLutHandle, 1)
        
        // 绑定顶点和纹理坐标
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        
        GLES30.glEnableVertexAttribArray(texCoordHandle)
        GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 0, texCoordBuffer)
        
        // 绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        
        // 读取渲染结果
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val buffer = ByteBuffer.allocateDirect(bitmap.width * bitmap.height * 4)
        GLES30.glReadPixels(0, 0, bitmap.width, bitmap.height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer)
        resultBitmap.copyPixelsFromBuffer(buffer)
        
        return resultBitmap
    }
    
    /**
     * 释放资源
     */
    fun release() {
        if (textureId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = 0
        }
        
        if (lutTextureId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(lutTextureId), 0)
            lutTextureId = 0
        }
        
        if (programId != 0) {
            GLES30.glDeleteProgram(programId)
            programId = 0
        }
    }
}
