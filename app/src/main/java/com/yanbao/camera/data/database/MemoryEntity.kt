package com.yanbao.camera.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 记忆实体 - Phase 1 简化版
 *
 * 存储拍照时的完整参数快照，包括：
 * - 照片路径
 * - 拍摄模式
 * - 29D 参数 JSON（兼容 Camera29DState）
 * - 滤镜 ID
 * - 地理位置
 * - 天气
 */
@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,                      // 照片本地路径
    val thumbnailPath: String? = null,           // 缩略图路径
    val mode: String,                            // 拍摄模式，如 "29D"、"Master"
    val params29DJson: String,                   // 29D 参数 JSON 字符串
    val filterId: String? = null,                // 大师滤镜 ID
    val beautyParamsJson: String? = null,        // 美颜参数 JSON
    val locationLat: Double? = null,             // 纬度
    val locationLng: Double? = null,             // 经度
    val locationAddress: String? = null,         // 地址名称
    val weather: String? = null,                 // 天气
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 将 params29DJson 解析为 Map<String, Float>
     */
    fun toParamsMap(): Map<String, Float> {
        return try {
            val type = object : TypeToken<Map<String, Float>>() {}.type
            Gson().fromJson(params29DJson, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    companion object {
        /**
         * 从 Param29D 对象创建 MemoryEntity（供 CameraViewModel 直接调用）
         */
        fun fromParams29D(
            photoPath: String,
            mode: String,
            params: com.yanbao.camera.presentation.camera.Param29D,
            filterId: String? = null,
            lat: Double? = null,
            lng: Double? = null,
            address: String? = null,
            weather: String? = null
        ): MemoryEntity {
            val json = com.google.gson.Gson().toJson(params)
            return MemoryEntity(
                photoPath = photoPath,
                mode = mode,
                params29DJson = json,
                filterId = filterId,
                locationLat = lat,
                locationLng = lng,
                locationAddress = address,
                weather = weather
            )
        }

        /**
         * 从参数 Map 创建 MemoryEntity
         */
        fun fromParamsMap(
            photoPath: String,
            mode: String,
            params: Map<String, Float>,
            filterId: String? = null,
            lat: Double? = null,
            lng: Double? = null,
            address: String? = null,
            weather: String? = null
        ): MemoryEntity {
            val json = Gson().toJson(params)
            return MemoryEntity(
                photoPath = photoPath,
                mode = mode,
                params29DJson = json,
                filterId = filterId,
                locationLat = lat,
                locationLng = lng,
                locationAddress = address,
                weather = weather
            )
        }

        /**
         * 从 JSON 字符串直接创建 MemoryEntity
         */
        fun fromJson(
            photoPath: String,
            mode: String,
            paramsJson: String,
            filterId: String? = null,
            lat: Double? = null,
            lng: Double? = null,
            address: String? = null,
            weather: String? = null
        ): MemoryEntity {
            return MemoryEntity(
                photoPath = photoPath,
                mode = mode,
                params29DJson = paramsJson,
                filterId = filterId,
                locationLat = lat,
                locationLng = lng,
                locationAddress = address,
                weather = weather
            )
        }
    }
}
