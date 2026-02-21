package com.yanbao.camera.presentation.gallery

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 相冊長按對比渲染器
 * 
 * 🚨 用戶要求：
 * 实现「長按對比」功能，實時對比编辑前後效果（調用雙緩衝紋理）
 * 
 * 驗收閉環：
 * - 長按照片 → 显示编辑前的原圖
 * - 鬆開手指 → 恢復显示编辑後的图片
 * - 使用雙緩衝紋理实现無延遲切換
 */
class CompareRenderer(private val context: Context) {
    
    companion object {
        private const val TAG = "CompareRenderer"
        
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
        
        // 片元著色器（支持雙緩衝紋理切換）
        private const val FRAGMENT_SHADER_CODE = """
            #version 300 es
            precision mediump float;
            
            uniform sampler2D uTextureOriginal;  // 原圖紋理
            uniform sampler2D uTextureEdited;    // 编辑後紋理
            uniform float uShowOriginal;         // 0.0 = 显示编辑後, 1.0 = 显示原圖
            
            in vec2 vTexCoord;
            out vec4 fragColor;
            
            void main() {
                vec4 originalColor = texture(uTextureOriginal, vTexCoord);
                vec4 editedColor = texture(uTextureEdited, vTexCoord);
                
                // 根據 uShowOriginal 混合兩個紋理
                fragColor = mix(editedColor, originalColor, uShowOriginal);
            }
        """
    }
    
    private var programId = 0
    private var originalTextureId = 0
    private var editedTextureId = 0
    
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var originalTextureHandle = 0
    private var editedTextureHandle = 0
    private var showOriginalHandle = 0
    
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer
    
    // 當前是否显示原圖
    private var showOriginal = false
    
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
        // 編譯著色器
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        
        // 創建程序
        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)
        GLES30.glLinkProgram(programId)
        
        // 检查鏈接状态
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES30.glGetProgramInfoLog(programId)
            Log.e(TAG, "Program link error: $error")
            GLES30.glDeleteProgram(programId)
            programId = 0
            return
        }
        
        // 獲取句柄
        positionHandle = GLES30.glGetAttribLocation(programId, "aPosition")
        texCoordHandle = GLES30.glGetAttribLocation(programId, "aTexCoord")
        originalTextureHandle = GLES30.glGetUniformLocation(programId, "uTextureOriginal")
        editedTextureHandle = GLES30.glGetUniformLocation(programId, "uTextureEdited")
        showOriginalHandle = GLES30.glGetUniformLocation(programId, "uShowOriginal")
        
        Log.d(TAG, "OpenGL initialized successfully")
    }
    
    /**
     * 加載著色器
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)
        
        // 检查編譯状态
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
     * 設置原圖紋理
     */
    fun setOriginalBitmap(bitmap: Bitmap) {
        if (originalTextureId == 0) {
            val textures = IntArray(1)
            GLES30.glGenTextures(1, textures, 0)
            originalTextureId = textures[0]
        }
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, originalTextureId)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        
        Log.d(TAG, "Original texture set: $originalTextureId")
    }
    
    /**
     * 設置编辑後紋理
     */
    fun setEditedBitmap(bitmap: Bitmap) {
        if (editedTextureId == 0) {
            val textures = IntArray(1)
            GLES30.glGenTextures(1, textures, 0)
            editedTextureId = textures[0]
        }
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, editedTextureId)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        
        Log.d(TAG, "Edited texture set: $editedTextureId")
    }
    
    /**
     * 設置是否显示原圖
     * 
     * @param show true = 显示原圖, false = 显示编辑後
     */
    fun setShowOriginal(show: Boolean) {
        showOriginal = show
        Log.d(TAG, "Show original: $showOriginal")
    }
    
    /**
     * 渲染
     */
    fun render() {
        if (originalTextureId == 0 || editedTextureId == 0) {
            Log.w(TAG, "Textures not set, skipping render")
            return
        }
        
        // 使用程序
        GLES30.glUseProgram(programId)
        
        // 綁定原圖紋理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, originalTextureId)
        GLES30.glUniform1i(originalTextureHandle, 0)
        
        // 綁定编辑後紋理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, editedTextureId)
        GLES30.glUniform1i(editedTextureHandle, 1)
        
        // 設置显示模式
        GLES30.glUniform1f(showOriginalHandle, if (showOriginal) 1.0f else 0.0f)
        
        // 綁定頂點和紋理坐標
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
        
        GLES30.glEnableVertexAttribArray(texCoordHandle)
        GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 0, texCoordBuffer)
        
        // 繪製
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }
    
    /**
     * 釋放資源
     */
    fun release() {
        if (originalTextureId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(originalTextureId), 0)
            originalTextureId = 0
        }
        
        if (editedTextureId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(editedTextureId), 0)
            editedTextureId = 0
        }
        
        if (programId != 0) {
            GLES30.glDeleteProgram(programId)
            programId = 0
        }
    }
}
