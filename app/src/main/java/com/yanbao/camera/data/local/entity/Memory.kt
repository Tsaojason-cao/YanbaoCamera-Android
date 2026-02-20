package com.yanbao.camera.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 雁宝记忆 - 数据资产流实体类
 * 
 * Phase 1: 定义完整字段结构（使用真实的 Double 和 String 类型）
 * Phase 4: 实现数据采集和参数回溯逻辑
 * 
 * 核心字段：
 * - photoUri: 照片 URI（String，非占位符）
 * - timestamp: 拍摄时间戳（Long）
 * - latitude: 纬度（Double，Phase 1 使用 Mock 数据，Phase 4 使用真实 GPS）
 * - longitude: 经度（Double，Phase 1 使用 Mock 数据，Phase 4 使用真实 GPS）
 * - weather: 天气信息（String，Phase 1 使用 Mock 数据，Phase 4 调用天气 API）
 * - params29D: 29维参数 JSON（String，Phase 3-4 实现）
 * - cameraMode: 拍摄模式（String）
 * - iso: ISO 值（Int，Phase 3 Camera2 API）
 * - shutterSpeed: 快门速度（Long，单位：纳秒，Phase 3 Camera2 API）
 * - whiteBalance: 白平衡（Int，Phase 3 Camera2 API）
 * - exposureCompensation: 曝光补偿（Int，Phase 3 Camera2 API）
 * - focusMode: 对焦模式（String，Phase 3 Camera2 API）
 * - zoomRatio: 变焦比例（Float，Phase 3 Camera2 API）
 */
@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey
    val id: String,
    
    // 照片信息
    val photoUri: String,
    val timestamp: Long,
    
    // 地理位置（LBS）
    val latitude: Double,
    val longitude: Double,
    val locationName: String? = null,
    
    // 天气信息
    val weather: String,
    val temperature: Double? = null,
    val weatherIcon: String? = null,
    
    // 29维参数（JSON 字符串）
    val params29D: String,
    
    // 相机模式
    val cameraMode: String,
    
    // 相机硬件参数（Phase 3 Camera2 API）
    val iso: Int? = null,
    val shutterSpeed: Long? = null,
    val whiteBalance: Int? = null,
    val exposureCompensation: Int? = null,
    val focusMode: String? = null,
    val zoomRatio: Float? = null
)

/**
 * Mock 数据工厂（Phase 1 使用）
 */
object MemoryMockFactory {
    fun createMockMemory(photoUri: String): Memory {
        return Memory(
            id = java.util.UUID.randomUUID().toString(),
            photoUri = photoUri,
            timestamp = System.currentTimeMillis(),
            latitude = 39.9042, // 北京天安门 Mock 数据
            longitude = 116.4074,
            locationName = "北京市东城区",
            weather = "晴",
            temperature = 22.5,
            weatherIcon = "☀️",
            params29D = """
                {
                    "brightness": 0.0,
                    "contrast": 0.0,
                    "saturation": 0.0,
                    "sharpness": 0.0,
                    "noise_reduction": 0.0,
                    "grain": 0.0,
                    "vignette": 0.0,
                    "beauty_smooth": 0.0,
                    "beauty_whiten": 0.0,
                    "beauty_eye_enlarge": 0.0,
                    "beauty_face_slim": 0.0,
                    "hue": 0.0,
                    "temperature": 0.0,
                    "tint": 0.0,
                    "exposure": 0.0,
                    "highlights": 0.0,
                    "shadows": 0.0,
                    "whites": 0.0,
                    "blacks": 0.0,
                    "clarity": 0.0,
                    "dehaze": 0.0,
                    "vibrance": 0.0,
                    "red": 0.0,
                    "green": 0.0,
                    "blue": 0.0,
                    "cyan": 0.0,
                    "magenta": 0.0,
                    "yellow": 0.0,
                    "orange": 0.0
                }
            """.trimIndent(),
            cameraMode = "PHOTO",
            iso = 100,
            shutterSpeed = 1000000000L / 60, // 1/60s
            whiteBalance = 5500,
            exposureCompensation = 0,
            focusMode = "AUTO",
            zoomRatio = 1.0f
        )
    }
}
