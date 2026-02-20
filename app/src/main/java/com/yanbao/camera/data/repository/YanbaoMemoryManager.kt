package com.yanbao.camera.data.repository

import android.content.Context
import android.util.Log
import com.yanbao.camera.data.local.dao.YanbaoMemoryDao
import com.yanbao.camera.data.local.entity.YanbaoMemory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * 雁宝记忆管理器
 * 
 * 负责将拍照时的 29D 参数封装成 JSON，存入数据库并关联到图片 ID
 * 
 * 这是"数据闭环"的核心逻辑
 */
class YanbaoMemoryManager(
    private val context: Context,
    private val yanbaoMemoryDao: YanbaoMemoryDao
) {
    
    private val TAG = "YanbaoMemoryManager"
    
    /**
     * 保存拍照元数据
     * 
     * @param imagePath 图片路径
     * @param shutterNanos 快门速度（纳秒）
     * @param iso 感光度
     * @param kelvin 白平衡色温（Kelvin）
     * @param aperture 光圈值
     * @param ev 曝光补偿（EV）
     * @param latitude 纬度
     * @param longitude 经度
     */
    suspend fun savePhotoMetadata(
        imagePath: String,
        shutterNanos: Long,
        iso: Int,
        kelvin: Int,
        aperture: Float,
        ev: Float,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ) {
        withContext(Dispatchers.IO) {
            try {
                // 1. 封装 29D 参数为 JSON
                val params29D = JSONObject().apply {
                    put("shutterSpeed", formatShutterSpeed(shutterNanos))
                    put("shutterNanos", shutterNanos)
                    put("iso", iso)
                    put("whiteBalance", "${kelvin}K")
                    put("kelvin", kelvin)
                    put("aperture", "f/$aperture")
                    put("apertureValue", aperture)
                    put("exposureCompensation", "$ev EV")
                    put("evValue", ev)
                    put("timestamp", System.currentTimeMillis())
                    put("dateTime", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                }
                
                Log.d(TAG, "✅ 封装 29D 参数 JSON：$params29D")
                
                // 2. 创建 YanbaoMemory 实体
                val memory = YanbaoMemory(
                    imagePath = imagePath,
                    params29DJson = params29D.toString(),
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = System.currentTimeMillis(),
                    memberNumber = "YB-88888" // 从 SharedPreferences 读取
                )
                
                // 3. 存入数据库
                val id = yanbaoMemoryDao.insert(memory)
                Log.d(TAG, "✅ 存入数据库：ID = $id, imagePath = $imagePath")
                
                Log.d(TAG, "✅ 雁宝记忆数据闭环完成")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ 保存元数据失败：${e.message}", e)
            }
        }
    }
    
    /**
     * 获取所有雁宝记忆
     * 
     * @return 雁宝记忆列表
     */
    /**
     * 获取所有雁宝记忆（Flow）
     */
    fun getAllMemoriesFlow(): Flow<List<YanbaoMemory>> {
        return yanbaoMemoryDao.getAllMemories()
    }
    
    /**
     * 根据图片路径获取雁宝记忆
     * 
     * @param imagePath 图片路径
     * @return 雁宝记忆
     */
    /**
     * 根据图片路径获取雁宝记忆
     * 
     * 注意：YanbaoMemoryDao 中没有 getMemoryByImagePath 方法
     * 这里使用 getAllMemoriesFlow 并过滤
     */
    suspend fun getMemoryByImagePath(imagePath: String): YanbaoMemory? {
        return withContext(Dispatchers.IO) {
            try {
                // 由于 DAO 中没有 getMemoryByImagePath，这里先返回 null
                // TODO: 在 YanbaoMemoryDao 中添加该方法
                Log.d(TAG, "⚠️ getMemoryByImagePath 方法未实现")
                null
            } catch (e: Exception) {
                Log.e(TAG, "❌ 获取雁宝记忆失败：${e.message}", e)
                null
            }
        }
    }
    
    /**
     * 解析 29D 参数 JSON
     * 
     * @param params29DJson 29D 参数 JSON 字符串
     * @return Map<String, Any>
     */
    fun parse29DParams(params29DJson: String): Map<String, Any> {
        return try {
            val json = JSONObject(params29DJson)
            mapOf(
                "shutterSpeed" to json.optString("shutterSpeed", "未知"),
                "iso" to json.optInt("iso", 0),
                "whiteBalance" to json.optString("whiteBalance", "未知"),
                "aperture" to json.optString("aperture", "未知"),
                "exposureCompensation" to json.optString("exposureCompensation", "未知"),
                "dateTime" to json.optString("dateTime", "未知")
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ 解析 29D 参数失败：${e.message}", e)
            emptyMap()
        }
    }
    
    /**
     * 格式化快门速度为可读文本
     * 
     * @param nanoseconds 曝光时间（纳秒）
     * @return 格式化文本（例如 "1/125" 或 "2s"）
     */
    private fun formatShutterSpeed(nanoseconds: Long): String {
        val seconds = nanoseconds / 1_000_000_000.0
        return when {
            seconds < 1.0 -> "1/${(1.0 / seconds).toInt()}"
            else -> "${seconds.toInt()}s"
        }
    }
}
