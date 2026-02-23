package com.yanbao.camera.ar

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Phase 2: AR 空间渲染器
 *
 * 基于 OpenGL ES 2.0 渲染 ARCore 检测到的平面网格，
 * 提供 AR 空间可视化效果（半透明平面 + 边框线框）。
 *
 * 功能：
 * - 渲染 ARCore 相机背景（直通预览）
 * - 渲染检测到的水平/垂直平面（半透明网格）
 * - 支持 AR 标注点放置（通过 [addAnchorPoint]）
 *
 * 使用方式：
 * ```kotlin
 * val arRenderer = ArRenderer(arSessionManager)
 * glSurfaceView.setRenderer(arRenderer)
 * ```
 */
class ArRenderer(
    private val arSessionManager: ArSessionManager
) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "ArRenderer"

        // 平面渲染着色器（半透明网格）
        private val PLANE_VERTEX_SHADER = """
            uniform mat4 uMvpMatrix;
            attribute vec4 aPosition;
            void main() {
                gl_Position = uMvpMatrix * aPosition;
            }
        """.trimIndent()

        private val PLANE_FRAGMENT_SHADER = """
            precision mediump float;
            uniform vec4 uColor;
            void main() {
                gl_FragColor = uColor;
            }
        """.trimIndent()

        // 相机背景着色器（OES 纹理直通）
        private val BG_VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        private val BG_FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTexCoord;
            uniform samplerExternalOES uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """.trimIndent()

        // 平面颜色（按类型区分）
        private val FLOOR_COLOR   = floatArrayOf(0.0f, 0.8f, 0.4f, 0.3f)  // 绿色半透明
        private val CEILING_COLOR = floatArrayOf(0.4f, 0.6f, 1.0f, 0.2f)  // 蓝色半透明
        private val WALL_COLOR    = floatArrayOf(1.0f, 0.6f, 0.0f, 0.25f) // 橙色半透明
    }

    // ─── 状态 ─────────────────────────────────────────────────────────────

    private var planeProgram = 0
    private var bgProgram = 0
    private var bgTextureId = -1

    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    // 标注点列表（世界坐标）
    private val anchorPoints = mutableListOf<FloatArray>()

    // 背景四边形
    private val bgQuad = floatArrayOf(
        -1f, -1f, 0f,  0f, 0f,
         1f, -1f, 0f,  1f, 0f,
        -1f,  1f, 0f,  0f, 1f,
         1f,  1f, 0f,  1f, 1f
    )
    private val bgBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(bgQuad.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(bgQuad); position(0) }

    // ─── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 添加 AR 标注点（世界坐标）
     */
    fun addAnchorPoint(x: Float, y: Float, z: Float) {
        anchorPoints.add(floatArrayOf(x, y, z))
        Log.d(TAG, "Anchor added at ($x, $y, $z), total=${anchorPoints.size}")
    }

    /**
     * 清除所有标注点
     */
    fun clearAnchorPoints() {
        anchorPoints.clear()
    }

    // ─── GLSurfaceView.Renderer ───────────────────────────────────────────

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        planeProgram = buildProgram(PLANE_VERTEX_SHADER, PLANE_FRAGMENT_SHADER)
        bgProgram = buildProgram(BG_VERTEX_SHADER, BG_FRAGMENT_SHADER)

        // 创建背景纹理（ARCore 相机预览）
        val texIds = IntArray(1)
        GLES20.glGenTextures(1, texIds, 0)
        bgTextureId = texIds[0]
        GLES20.glBindTexture(0x8D65 /* GL_TEXTURE_EXTERNAL_OES */, bgTextureId)
        GLES20.glTexParameteri(0x8D65, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(0x8D65, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // 将纹理 ID 传给 ARCore Session
        arSessionManager.getSession()?.setCameraTextureName(bgTextureId)

        Log.d(TAG, "ArRenderer surface created")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        // 设置透视投影矩阵
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 60f, aspect, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val frame = arSessionManager.update() ?: return

        // 渲染相机背景
        drawBackground(frame)

        // 仅在追踪状态下渲染 AR 内容
        if (frame.camera.trackingState != TrackingState.TRACKING) return

        // 获取视图矩阵
        frame.camera.getViewMatrix(viewMatrix, 0)

        // 渲染检测到的平面
        val session = arSessionManager.getSession() ?: return
        session.getAllTrackables(Plane::class.java)
            .filter { it.trackingState == TrackingState.TRACKING }
            .forEach { plane -> drawPlane(plane) }
    }

    // ─── 私有渲染方法 ─────────────────────────────────────────────────────

    private fun drawBackground(frame: Frame) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glUseProgram(bgProgram)

        val stride = 5 * 4
        bgBuffer.position(0)
        val posLoc = GLES20.glGetAttribLocation(bgProgram, "aPosition")
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, stride, bgBuffer)
        GLES20.glEnableVertexAttribArray(posLoc)

        bgBuffer.position(3)
        val texLoc = GLES20.glGetAttribLocation(bgProgram, "aTexCoord")
        GLES20.glVertexAttribPointer(texLoc, 2, GLES20.GL_FLOAT, false, stride, bgBuffer)
        GLES20.glEnableVertexAttribArray(texLoc)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(0x8D65, bgTextureId)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(bgProgram, "uTexture"), 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(posLoc)
        GLES20.glDisableVertexAttribArray(texLoc)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    private fun drawPlane(plane: Plane) {
        val color = when (plane.type) {
            Plane.Type.HORIZONTAL_UPWARD_FACING   -> FLOOR_COLOR
            Plane.Type.HORIZONTAL_DOWNWARD_FACING -> CEILING_COLOR
            Plane.Type.VERTICAL                   -> WALL_COLOR
            else                                  -> FLOOR_COLOR
        }

        // 获取平面多边形顶点
        val polygon = plane.polygon ?: return
        val vertexCount = polygon.limit() / 2
        if (vertexCount < 3) return

        // 构建顶点缓冲（XZ 平面，Y 从平面中心姿态获取）
        val centerPose = plane.centerPose
        val vertices = FloatBuffer.allocate(vertexCount * 3)
        for (i in 0 until vertexCount) {
            val x = polygon.get(i * 2)
            val z = polygon.get(i * 2 + 1)
            // 将局部坐标转换为世界坐标
            val worldPos = centerPose.transformPoint(floatArrayOf(x, 0f, z))
            vertices.put(worldPos[0])
            vertices.put(worldPos[1])
            vertices.put(worldPos[2])
        }
        vertices.position(0)

        GLES20.glUseProgram(planeProgram)

        // MVP 矩阵
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        val mvpLoc = GLES20.glGetUniformLocation(planeProgram, "uMvpMatrix")
        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvpMatrix, 0)

        // 颜色
        val colorLoc = GLES20.glGetUniformLocation(planeProgram, "uColor")
        GLES20.glUniform4fv(colorLoc, 1, color, 0)

        // 顶点
        val posLoc = GLES20.glGetAttribLocation(planeProgram, "aPosition")
        GLES20.glVertexAttribPointer(posLoc, 3, GLES20.GL_FLOAT, false, 0, vertices)
        GLES20.glEnableVertexAttribArray(posLoc)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount)
        GLES20.glDisableVertexAttribArray(posLoc)
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
}
