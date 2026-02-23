package com.yanbao.camera.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Phase 2: 2.9D 视差传感器
 *
 * 监听陀螺仪（GYROSCOPE）数据，通过积分计算倾斜角度，
 * 并通过低通滤波平滑输出，供 [ParallaxRenderer] 使用。
 *
 * 使用方式：
 * ```kotlin
 * val sensor = ParallaxSensor(context)
 * sensor.start()
 * // 在 Composable 中收集
 * val tilt by sensor.tilt.collectAsState()
 * // 传递给渲染器
 * renderer.setParallaxOffset(tilt.first * SCALE, tilt.second * SCALE)
 * ```
 */
class ParallaxSensor(context: Context) : SensorEventListener {

    companion object {
        private const val TAG = "ParallaxSensor"

        /** 低通滤波系数（0=完全平滑, 1=无滤波） */
        private const val ALPHA = 0.1f

        /** 最大倾斜角度（弧度），超出则截断 */
        private const val MAX_TILT = 0.5f

        /** 陀螺仪积分时间步长（秒），对应 SENSOR_DELAY_GAME ≈ 20ms */
        private const val DT = 0.02f

        /** 死区阈值，小于此值的角速度忽略（减少漂移） */
        private const val DEAD_ZONE = 0.01f
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    /** 当前倾斜角度（x轴=左右, y轴=上下），单位：弧度 */
    private val _tilt = MutableStateFlow(Pair(0f, 0f))
    val tilt: StateFlow<Pair<Float, Float>> = _tilt.asStateFlow()

    /** 是否正在监听 */
    private var isRunning = false

    /** 积分后的原始角度（未滤波） */
    private var rawTiltX = 0f
    private var rawTiltY = 0f

    /** 低通滤波后的角度 */
    private var smoothTiltX = 0f
    private var smoothTiltY = 0f

    // ─── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 开始监听陀螺仪
     */
    fun start() {
        if (isRunning) return
        if (gyroSensor == null) {
            Log.w(TAG, "Gyroscope sensor not available on this device")
            return
        }
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME)
        isRunning = true
        Log.d(TAG, "ParallaxSensor started")
    }

    /**
     * 停止监听并重置状态
     */
    fun stop() {
        sensorManager.unregisterListener(this)
        isRunning = false
        reset()
        Log.d(TAG, "ParallaxSensor stopped")
    }

    /**
     * 重置倾斜角度到中心
     */
    fun reset() {
        rawTiltX = 0f
        rawTiltY = 0f
        smoothTiltX = 0f
        smoothTiltY = 0f
        _tilt.value = Pair(0f, 0f)
    }

    /**
     * 是否有陀螺仪硬件
     */
    fun isAvailable(): Boolean = gyroSensor != null

    // ─── SensorEventListener ─────────────────────────────────────────────

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_GYROSCOPE) return

        val gx = event.values[0]  // 绕 X 轴角速度（上下倾斜）
        val gy = event.values[1]  // 绕 Y 轴角速度（左右倾斜）

        // 死区过滤
        val filteredGx = if (abs(gx) > DEAD_ZONE) gx else 0f
        val filteredGy = if (abs(gy) > DEAD_ZONE) gy else 0f

        // 积分：角速度 × 时间 = 角度增量
        rawTiltX = (rawTiltX + filteredGx * DT).coerceIn(-MAX_TILT, MAX_TILT)
        rawTiltY = (rawTiltY + filteredGy * DT).coerceIn(-MAX_TILT, MAX_TILT)

        // 低通滤波平滑
        smoothTiltX = smoothTiltX + ALPHA * (rawTiltX - smoothTiltX)
        smoothTiltY = smoothTiltY + ALPHA * (rawTiltY - smoothTiltY)

        _tilt.value = Pair(smoothTiltX, smoothTiltY)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 不需要处理精度变化
    }
}
