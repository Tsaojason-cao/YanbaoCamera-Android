package com.yanbao.camera.render

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.yanbao.camera.data.model.Render29D
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * yanbao AI 29D 渲染引擎
 * 
 * 負責將 29D 参数矩陣通過 GPU Shader 實時映射到取景器畫面
 * 
 * 核心功能：
 * 1. 加載 GLSL Shader
 * 2. 將 29D 参数注入 Uniform 變量
 * 3. 實時渲染取景器畫面
 */
class Yanbao29DRenderEngine(private val context: Context) {
    
    private var shaderProgram: Int = 0
    private var params29DLocation: Int = 0
    private var textureLocation: Int = 0
    
    // 當前的 29D 参数
    private var current29DParams: FloatArray = Render29D.default().toFloatArray()
    
    // 環境自動偏移量（來自 LBS/AR 空間）
    private val environmentOffset = mutableMapOf<Int, Float>()
    
    init {
        initShader()
    }
    
    /**
     * 初始化 Shader
     */
    private fun initShader() {
        // 加載頂點著色器
        val vertexShaderCode = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()
        
        // 加載片段著色器（從 assets 讀取）
        val fragmentShaderCode = try {
            context.assets.open("shaders/Yanbao29DShader.glsl").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            // 如果文件不存在，使用簡化版本
            """
                precision highp float;
                uniform sampler2D inputTexture;
                uniform float params29D[29];
                varying vec2 vTexCoord;
                void main() {
                    vec4 color = texture2D(inputTexture, vTexCoord);
                    color.rgb *= params29D[0]; // D1: Exposure
                    gl_FragColor = color;
                }
            """.trimIndent()
        }
        
        // 編譯著色器
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        // 創建程序
        shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)
        
        // 獲取 Uniform 位置
        params29DLocation = GLES20.glGetUniformLocation(shaderProgram, "params29D")
        textureLocation = GLES20.glGetUniformLocation(shaderProgram, "inputTexture")
    }
    
    /**
     * 加載著色器
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
    
    /**
     * 更新 29D 参数（來自用戶调节或濾鏡切換）
     */
    fun update29DParams(render29D: Render29D) {
        current29DParams = render29D.toFloatArray()
        applyEnvironmentOffset(environmentOffset)
    }
    
    /**
     * 應用環境自動偏移量（來自 AR/LBS 空間）
     */
    fun applyEnvironmentOffset(offset: Map<Int, Float>) {
        environmentOffset.clear()
        environmentOffset.putAll(offset)
        
        // 將偏移量疊加到當前参数
        val finalParams = current29DParams.copyOf()
        offset.forEach { (index, value) ->
            if (index in finalParams.indices) {
                finalParams[index] += value
            }
        }
        
        // 注入到 Shader
        GLES20.glUseProgram(shaderProgram)
        GLES20.glUniform1fv(params29DLocation, 29, finalParams, 0)
    }
    
    /**
     * 更新單個維度的参数（實時调节）
     */
    fun updateDimension(index: Int, value: Float) {
        if (index in current29DParams.indices) {
            current29DParams[index] = value
            
            // 立即注入到 Shader（实现 16ms 內完成映射）
            GLES20.glUseProgram(shaderProgram)
            GLES20.glUniform1fv(params29DLocation, 29, current29DParams, 0)
        }
    }
    
    /**
     * 渲染畫面（將 29D 参数應用到輸入紋理）
     */
    fun renderFrame(inputTexture: Int): Int {
        GLES20.glUseProgram(shaderProgram)
        
        // 綁定輸入紋理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTexture)
        GLES20.glUniform1i(textureLocation, 0)
        
        // 注入 29D 参数
        GLES20.glUniform1fv(params29DLocation, 29, current29DParams, 0)
        
        // 繪製全屏四邊形
        drawFullScreenQuad()
        
        return inputTexture // 返回渲染後的紋理 ID
    }
    
    /**
     * 繪製全屏四邊形
     */
    private fun drawFullScreenQuad() {
        val vertices = floatArrayOf(
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f,
            -1f, 1f, 0f, 1f,
            1f, 1f, 1f, 1f
        )
        
        val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices)
        vertexBuffer.position(0)
        
        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition")
        val texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "aTexCoord")
        
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)
        vertexBuffer.position(2)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }
    
    /**
     * 獲取當前 29D 参数（用於保存到 JSON）
     */
    fun getCurrent29DParams(): FloatArray {
        return current29DParams.copyOf()
    }
    
    /**
     * 釋放資源
     */
    fun release() {
        if (shaderProgram != 0) {
            GLES20.glDeleteProgram(shaderProgram)
            shaderProgram = 0
        }
    }
}
