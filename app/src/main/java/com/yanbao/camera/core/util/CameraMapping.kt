package com.yanbao.camera.core.util

import kotlin.math.pow

/**
 * 29D 刻度盘算法：将 UI 角度(0-360) 映射为物理硬件参数
 * 
 * 这是 yanbao AI 相机的核心算法，负责将用户在 UI 上的操作
 * 转换为 Camera2 API 可以理解的物理参数
 */
object CameraMapping {
    
    /**
     * 白平衡映射 (2000K - 10000K 非线性映射)
     * 
     * @param angle UI 角度 (0-360)
     * @return 色温值 (Kelvin)
     */
    fun mapAngleToKelvin(angle: Float): Int {
        val progress = angle / 360f
        return (2000 + (progress * 8000)).toInt()
    }
    
    /**
     * 快门速度映射 (1/8000s 到 30s)
     * 
     * 逻辑：采用对数映射，让长秒数和高快门在拨盘上分布均匀
     * 
     * @param angle UI 角度 (0-360)
     * @return 曝光时间 (纳秒)
     */
    fun mapAngleToShutter(angle: Float): Long {
        val shutters = listOf(
            1/8000.0, 1/4000.0, 1/2000.0, 1/1000.0, 1/500.0, 
            1/250.0, 1/125.0, 1/60.0, 1/30.0, 1/15.0, 
            1/8.0, 1/4.0, 0.5, 1.0, 2.0, 4.0, 8.0, 15.0, 30.0
        )
        val index = ((angle / 360f) * (shutters.size - 1)).toInt().coerceIn(0, shutters.size - 1)
        return (shutters[index] * 1_000_000_000L).toLong() // 转换为纳秒给 Camera2 API
    }
    
    /**
     * ISO 映射 (100 - 6400)
     * 
     * 逻辑：采用对数映射，让 ISO 在拨盘上分布均匀
     * 
     * @param angle UI 角度 (0-360)
     * @return ISO 值
     */
    fun mapAngleToISO(angle: Float): Int {
        val progress = angle / 360f
        // 对数映射：100, 200, 400, 800, 1600, 3200, 6400
        val logValue = 100 * (2.0.pow(progress * 6))
        return logValue.toInt().coerceIn(100, 6400)
    }
    
    /**
     * 光圈映射 (f/1.4 - f/22)
     * 
     * @param angle UI 角度 (0-360)
     * @return 光圈值
     */
    fun mapAngleToAperture(angle: Float): Float {
        val apertures = listOf(
            1.4f, 1.8f, 2.0f, 2.8f, 4.0f, 5.6f, 8.0f, 11.0f, 16.0f, 22.0f
        )
        val index = ((angle / 360f) * (apertures.size - 1)).toInt().coerceIn(0, apertures.size - 1)
        return apertures[index]
    }
    
    /**
     * 焦距映射 (0.5x - 10x)
     * 
     * @param angle UI 角度 (0-360)
     * @return 焦距倍数
     */
    fun mapAngleToZoom(angle: Float): Float {
        val progress = angle / 360f
        // 0.5x 到 10x 的非线性映射
        return 0.5f + (progress * 9.5f)
    }
    
    /**
     * 曝光补偿映射 (-3 EV 到 +3 EV)
     * 
     * @param angle UI 角度 (0-360)
     * @return 曝光补偿值 (EV)
     */
    fun mapAngleToEV(angle: Float): Float {
        val progress = angle / 360f
        return -3f + (progress * 6f)
    }
    
    /**
     * 获取快门速度的显示文本
     * 
     * @param nanoseconds 曝光时间 (纳秒)
     * @return 显示文本 (例如 "1/125" 或 "2s")
     */
    fun getShutterSpeedText(nanoseconds: Long): String {
        val seconds = nanoseconds / 1_000_000_000.0
        return when {
            seconds < 1.0 -> "1/${(1.0 / seconds).toInt()}"
            else -> "${seconds.toInt()}s"
        }
    }
    
    /**
     * 获取 ISO 的显示文本
     * 
     * @param iso ISO 值
     * @return 显示文本 (例如 "ISO 400")
     */
    fun getISOText(iso: Int): String {
        return "ISO $iso"
    }
    
    /**
     * 获取白平衡的显示文本
     * 
     * @param kelvin 色温值 (Kelvin)
     * @return 显示文本 (例如 "5500K")
     */
    fun getKelvinText(kelvin: Int): String {
        return "${kelvin}K"
    }
    
    /**
     * 获取光圈的显示文本
     * 
     * @param aperture 光圈值
     * @return 显示文本 (例如 "f/2.8")
     */
    fun getApertureText(aperture: Float): String {
        return "f/${"%.1f".format(aperture)}"
    }
    
    /**
     * 获取曝光补偿的显示文本
     * 
     * @param ev 曝光补偿值 (EV)
     * @return 显示文本 (例如 "+1.5 EV" 或 "-0.5 EV")
     */
    fun getEVText(ev: Float): String {
        val sign = if (ev >= 0) "+" else ""
        return "$sign${"%.1f".format(ev)} EV"
    }
}
