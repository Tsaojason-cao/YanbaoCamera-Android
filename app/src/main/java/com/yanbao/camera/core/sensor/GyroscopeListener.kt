package com.yanbao.camera.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.yanbao.camera.core.util.AuditLogger

/**
 * 陀螺仪监听器 - 用于 2.9D 视差效果
 * 
 * 核心功能：
 * 1. 监听陀螺仪传感器数据
 * 2. 计算视差偏移量
 * 3. 通过审计日志验证真实性
 * 
 * 审计要求：
 * - 手机静止时，offsetX/Y 应接近 0
 * - 运动方向必须与数值变化方向一致
 * - 所有数据通过 AUDIT_2.9D 日志输出
 */
class GyroscopeListener(context: Context) : SensorEventListener {
    
    companion object {
        private const val TAG = "GyroscopeListener"
        
        // 灵敏度系数（可调节）
        private const val SENSITIVITY_FACTOR = 0.5f
        
        // 死区阈值（防止抖动）
        private const val DEAD_ZONE_THRESHOLD = 0.01f
    }
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private val gyroscopeSensor: Sensor? = 
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    
    // 当前视差偏移量
    private var currentOffsetX: Float = 0f
    private var currentOffsetY: Float = 0f
    
    // 回调
    var onParallaxOffsetChanged: ((Float, Float) -> Unit)? = null
    
    /**
     * 开始监听陀螺仪
     */
    fun start() {
        if (gyroscopeSensor == null) {
            Log.e(TAG, "设备不支持陀螺仪传感器")
            return
        }
        
        sensorManager.registerListener(
            this,
            gyroscopeSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        
        Log.d(TAG, "陀螺仪监听已启动")
    }
    
    /**
     * 停止监听陀螺仪
     */
    fun stop() {
        sensorManager.unregisterListener(this)
        Log.d(TAG, "陀螺仪监听已停止")
    }
    
    /**
     * 传感器数据变化回调
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_GYROSCOPE) {
            return
        }
        
        // 获取陀螺仪角速度（rad/s）
        val axisX = event.values[0] // 绕 X 轴旋转（上下倾斜）
        val axisY = event.values[1] // 绕 Y 轴旋转（左右倾斜）
        val axisZ = event.values[2] // 绕 Z 轴旋转（平面旋转）
        
        // 计算视差偏移量
        // 注意：Y 轴对应 X 方向偏移，X 轴对应 Y 方向偏移
        var offsetX = axisY * SENSITIVITY_FACTOR
        var offsetY = axisX * SENSITIVITY_FACTOR
        
        // 应用死区阈值（防止抖动）
        if (Math.abs(offsetX) < DEAD_ZONE_THRESHOLD) {
            offsetX = 0f
        }
        if (Math.abs(offsetY) < DEAD_ZONE_THRESHOLD) {
            offsetY = 0f
        }
        
        // 限制偏移范围（-1.0 到 1.0）
        offsetX = offsetX.coerceIn(-1.0f, 1.0f)
        offsetY = offsetY.coerceIn(-1.0f, 1.0f)
        
        // 更新当前偏移量
        currentOffsetX = offsetX
        currentOffsetY = offsetY
        
        // 审计日志：记录陀螺仪 → Shader Uniform 映射
        // 注意：为了避免刷屏，仅在偏移量变化较大时记录
        if (Math.abs(offsetX) > 0.1f || Math.abs(offsetY) > 0.1f) {
            AuditLogger.logParallaxEffect(event, offsetX, offsetY)
        }
        
        // 通知回调
        onParallaxOffsetChanged?.invoke(offsetX, offsetY)
    }
    
    /**
     * 传感器精度变化回调
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "传感器精度变化: $accuracy")
    }
    
    /**
     * 获取当前偏移量
     */
    fun getCurrentOffset(): Pair<Float, Float> {
        return Pair(currentOffsetX, currentOffsetY)
    }
    
    /**
     * 重置偏移量
     */
    fun resetOffset() {
        currentOffsetX = 0f
        currentOffsetY = 0f
        Log.d(TAG, "视差偏移量已重置")
    }
}
