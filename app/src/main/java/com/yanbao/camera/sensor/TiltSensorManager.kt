package com.yanbao.camera.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.abs

/**
 * 倾斜传感器管理器
 * 用于 2.9D 模式，监听设备倾斜角度并计算视差偏移
 * 
 * 技术原理：
 * 1. 使用加速度计（Accelerometer）检测设备倾斜
 * 2. 将原始传感器数据转换为归一化的倾斜角度（-1 到 1）
 * 3. 应用低通滤波器平滑数据，避免抖动
 * 4. 通过回调将倾斜数据传递给 GLRenderer
 */
class TiltSensorManager(context: Context) : SensorEventListener {
    private val TAG = "TiltSensorManager"
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val accelerometer: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    // 倾斜数据回调
    private var onTiltChanged: ((tiltX: Float, tiltY: Float) -> Unit)? = null
    
    // 原始传感器数据
    private var rawTiltX = 0f
    private var rawTiltY = 0f
    
    // 平滑后的倾斜数据
    private var smoothTiltX = 0f
    private var smoothTiltY = 0f
    
    // 低通滤波器系数（0-1，越小越平滑）
    private val alpha = 0.8f
    
    // 是否正在监听
    private var isListening = false
    
    /**
     * 开始监听传感器
     */
    fun startListening() {
        if (isListening) {
            Log.w(TAG, "传感器已在监听中")
            return
        }
        
        if (accelerometer == null) {
            Log.e(TAG, "设备不支持加速度计")
            return
        }
        
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME  // 游戏级延迟（约 20ms）
        )
        
        isListening = true
        Log.d(TAG, "开始监听倾斜传感器")
    }
    
    /**
     * 停止监听传感器
     */
    fun stopListening() {
        if (!isListening) {
            return
        }
        
        sensorManager.unregisterListener(this)
        isListening = false
        Log.d(TAG, "停止监听倾斜传感器")
    }
    
    /**
     * 设置倾斜数据回调
     */
    fun setOnTiltChangedListener(listener: (tiltX: Float, tiltY: Float) -> Unit) {
        this.onTiltChanged = listener
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }
        
        // 读取加速度计数据
        // event.values[0]: X 轴加速度（左右倾斜）
        // event.values[1]: Y 轴加速度（前后倾斜）
        // event.values[2]: Z 轴加速度（垂直方向）
        
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        // 归一化倾斜角度（-1 到 1）
        // 重力加速度约为 9.8 m/s²，设备平放时 Z 轴约为 9.8
        // 设备倾斜时，X 和 Y 轴的值会增大
        rawTiltX = -x / 9.8f  // 取反，使倾斜方向与视差方向一致
        rawTiltY = y / 9.8f
        
        // 限制范围
        rawTiltX = rawTiltX.coerceIn(-1f, 1f)
        rawTiltY = rawTiltY.coerceIn(-1f, 1f)
        
        // 应用低通滤波器（平滑数据）
        smoothTiltX = alpha * rawTiltX + (1 - alpha) * smoothTiltX
        smoothTiltY = alpha * rawTiltY + (1 - alpha) * smoothTiltY
        
        // 过滤微小抖动（阈值：0.01）
        if (abs(smoothTiltX) < 0.01f) smoothTiltX = 0f
        if (abs(smoothTiltY) < 0.01f) smoothTiltY = 0f
        
        // 回调传递倾斜数据
        onTiltChanged?.invoke(smoothTiltX, smoothTiltY)
        
        // 日志输出（每 100 次采样输出一次，避免日志过多）
        if (System.currentTimeMillis() % 100 < 20) {
            Log.d(TAG, "倾斜数据: X=${String.format("%.2f", smoothTiltX)}, Y=${String.format("%.2f", smoothTiltY)}")
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 精度变化时的回调（通常不需要处理）
        Log.d(TAG, "传感器精度变化: $accuracy")
    }
    
    /**
     * 重置倾斜数据（用于校准）
     */
    fun reset() {
        smoothTiltX = 0f
        smoothTiltY = 0f
        rawTiltX = 0f
        rawTiltY = 0f
        Log.d(TAG, "倾斜数据已重置")
    }
    
    /**
     * 检查设备是否支持加速度计
     */
    fun isSupported(): Boolean {
        return accelerometer != null
    }
}
