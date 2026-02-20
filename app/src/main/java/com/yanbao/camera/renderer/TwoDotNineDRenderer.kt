package com.yanbao.camera.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 2.9D 渲染器
 * 使用 OpenGL ES 2.0 实现 2.9D 视差效果
 * 
 * 技术原理：
 * 1. 接收传感器数据（陀螺仪/加速度计）
 * 2. 根据设备倾斜角度计算视差位移
 * 3. 使用 Fragment Shader 对图像进行位移和深度模拟
 * 4. 应用用户调节的参数（颜感、对比度、饱和度、色温）
 */
class TwoDotNineDRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private val TAG = "TwoDotNineDRenderer"
    
    // OpenGL 程序
    private var program = 0
    private var textureId = 0
    
    // Shader 变量句柄
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var mvpMatrixHandle = 0
    private var textureHandle = 0
    
    // 2.9D 参数句柄
    private var parallaxOffsetHandle = 0
    private var colorSenseHandle = 0
    private var contrastHandle = 0
    private var saturationHandle = 0
    private var colorTempHandle = 0
    
    // 顶点缓冲区
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer
    
    // 传感器数据（设备倾斜角度）
    private var tiltX = 0f  // X 轴倾斜（左右）
    private var tiltY = 0f  // Y 轴倾斜（上下）
    
    // 用户调节的 2.9D 参数
    private var colorSense = 0.2f      // 颜感：0-1
    private var contrast = 0.35f       // 对比度：0-1
    private var saturation = 0.5f      // 饱和度：0-1
    private var colorTemp = 4500f      // 色温：2000-8000K
    
    // 视差强度系数
    private val parallaxStrength = 0.05f
    
    // 顶点坐标（全屏四边形）
    private val vertexCoords = floatArrayOf(
        -1.0f, -1.0f,  // 左下
         1.0f, -1.0f,  // 右下
        -1.0f,  1.0f,  // 左上
         1.0f,  1.0f   // 右上
    )
    
    // 纹理坐标
    private val texCoords = floatArrayOf(
        0.0f, 1.0f,  // 左下
        1.0f, 1.0f,  // 右下
        0.0f, 0.0f,  // 左上
        1.0f, 0.0f   // 右上
    )
    
    init {
        // 初始化顶点缓冲区
        vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexCoords)
        vertexBuffer.position(0)
        
        // 初始化纹理坐标缓冲区
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(texCoords)
        texCoordBuffer.position(0)
    }
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        
        // 编译 Shader 并创建程序
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        
        // 获取变量句柄
        positionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        texCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
        textureHandle = GLES20.glGetUniformLocation(program, "u_Texture")
        
        // 2.9D 参数句柄
        parallaxOffsetHandle = GLES20.glGetUniformLocation(program, "u_ParallaxOffset")
        colorSenseHandle = GLES20.glGetUniformLocation(program, "u_ColorSense")
        contrastHandle = GLES20.glGetUniformLocation(program, "u_Contrast")
        saturationHandle = GLES20.glGetUniformLocation(program, "u_Saturation")
        colorTempHandle = GLES20.glGetUniformLocation(program, "u_ColorTemp")
        
        Log.d(TAG, "2.9D Renderer 初始化完成")
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        
        // 使用程序
        GLES20.glUseProgram(program)
        
        // 计算视差偏移（基于传感器数据）
        val parallaxX = tiltX * parallaxStrength
        val parallaxY = tiltY * parallaxStrength
        
        // 传递顶点坐标
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer
        )
        
        // 传递纹理坐标
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(
            texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer
        )
        
        // 传递 2.9D 参数到 Shader
        GLES20.glUniform2f(parallaxOffsetHandle, parallaxX, parallaxY)
        GLES20.glUniform1f(colorSenseHandle, colorSense)
        GLES20.glUniform1f(contrastHandle, contrast)
        GLES20.glUniform1f(saturationHandle, saturation)
        GLES20.glUniform1f(colorTempHandle, colorTemp)
        
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)
        
        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        
        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }
    
    /**
     * 更新传感器数据（由 SensorManager 调用）
     * @param tiltX X 轴倾斜角度（-1 到 1）
     * @param tiltY Y 轴倾斜角度（-1 到 1）
     */
    fun updateSensorData(tiltX: Float, tiltY: Float) {
        this.tiltX = tiltX
        this.tiltY = tiltY
        Log.d(TAG, "传感器数据更新: tiltX=$tiltX, tiltY=$tiltY, parallaxX=${tiltX * parallaxStrength}, parallaxY=${tiltY * parallaxStrength}")
    }
    
    /**
     * 更新 2.9D 参数（由 UI 调用）
     */
    fun update2DotNineDParams(
        colorSense: Float,
        contrast: Float,
        saturation: Float,
        colorTemp: Float
    ) {
        this.colorSense = colorSense
        this.contrast = contrast
        this.saturation = saturation
        this.colorTemp = colorTemp
        Log.d(TAG, "2.9D 参数更新: colorSense=$colorSense, contrast=$contrast, saturation=$saturation, colorTemp=$colorTemp")
    }
    
    /**
     * 加载纹理
     */
    fun loadTexture(bitmap: Bitmap): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        
        return textureId
    }
    
    /**
     * 编译 Shader
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        
        // 检查编译状态
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            Log.e(TAG, "Shader 编译失败: $error")
            GLES20.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }
    
    companion object {
        // 顶点着色器
        private const val vertexShaderCode = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_TexCoord;
            uniform mat4 u_MVPMatrix;
            
            void main() {
                gl_Position = a_Position;
                v_TexCoord = a_TexCoord;
            }
        """
        
        // 片段着色器（实现 2.9D 效果）
        private const val fragmentShaderCode = """
            precision mediump float;
            
            varying vec2 v_TexCoord;
            uniform sampler2D u_Texture;
            
            // 2.9D 参数
            uniform vec2 u_ParallaxOffset;    // 视差偏移（基于传感器）
            uniform float u_ColorSense;       // 颜感（0-1）
            uniform float u_Contrast;         // 对比度（0-1）
            uniform float u_Saturation;       // 饱和度（0-1）
            uniform float u_ColorTemp;        // 色温（2000-8000K）
            
            // RGB 转 HSV
            vec3 rgb2hsv(vec3 c) {
                vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
                vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
                vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
                float d = q.x - min(q.w, q.y);
                float e = 1.0e-10;
                return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
            }
            
            // HSV 转 RGB
            vec3 hsv2rgb(vec3 c) {
                vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
                vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
                return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
            }
            
            void main() {
                // 应用视差偏移（2.9D 核心效果）
                vec2 offsetCoord = v_TexCoord + u_ParallaxOffset;
                offsetCoord = clamp(offsetCoord, 0.0, 1.0);
                
                // 采样纹理
                vec4 color = texture2D(u_Texture, offsetCoord);
                
                // 应用对比度
                color.rgb = ((color.rgb - 0.5) * (1.0 + u_Contrast)) + 0.5;
                
                // 应用饱和度
                vec3 hsv = rgb2hsv(color.rgb);
                hsv.y *= (1.0 + u_Saturation);
                color.rgb = hsv2rgb(hsv);
                
                // 应用色温（简化版）
                float tempFactor = (u_ColorTemp - 5000.0) / 3000.0;  // -1 到 1
                if (tempFactor > 0.0) {
                    // 暖色调（增加红色）
                    color.r += tempFactor * 0.2;
                } else {
                    // 冷色调（增加蓝色）
                    color.b += abs(tempFactor) * 0.2;
                }
                
                // 应用颜感（整体色彩强度）
                color.rgb *= (1.0 + u_ColorSense * 0.5);
                
                gl_FragColor = clamp(color, 0.0, 1.0);
            }
        """
    }
}
