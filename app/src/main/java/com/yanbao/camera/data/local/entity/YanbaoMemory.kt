package com.yanbao.camera.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 雁宝记忆 (Yanbao Memory)
 * 
 * 核心资产流数据结构
 * 包含 LBS、29D 矩阵和回溯标记的完整定义
 * 
 * 拍照时必须将以下数据写入数据库：
 * - 经纬度（latitude, longitude）
 * - 天气信息（weatherType）
 * - 29D 参数 JSON（parameterSnapshotJson）
 * - 拍摄模式（shootingMode）
 * - 会员信息（memberNumber）
 */
@Entity(tableName = "yanbao_memory_table")
data class YanbaoMemory(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    
    // App 标识（English Name）
    val appName: String = "yanbao AI",
    
    @ColumnInfo(name = "image_path") 
    val imagePath: String,
    
    // 地理位置与环境（核心功能）
    val latitude: Double,
    val longitude: Double,
    val locationName: String?,
    val weatherType: String?, // 晴/雨/云
    
    // 拍摄模式记录
    val shootingMode: String, // "2.9D", "Master", "Professional", "Photo", etc.
    
    // 核心：29D 参数矩阵快照 (以 JSON 存储)
    val parameterSnapshotJson: String, 
    
    // 会员与权益信息
    val memberNumber: String = "88888",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * YanbaoMemory 工厂类
 * 用于创建 YanbaoMemory 实例
 */
object YanbaoMemoryFactory {
    /**
     * 创建 YanbaoMemory 实例（使用真实数据）
     * 
     * @param imagePath 照片路径
     * @param latitude 纬度（真实 GPS 或 Mock）
     * @param longitude 经度（真实 GPS 或 Mock）
     * @param locationName 位置名称
     * @param weatherType 天气类型
     * @param shootingMode 拍摄模式
     * @param parameterSnapshotJson 29D 参数 JSON
     * @param memberNumber 会员号
     */
    fun create(
        imagePath: String,
        latitude: Double,
        longitude: Double,
        locationName: String?,
        weatherType: String?,
        shootingMode: String,
        parameterSnapshotJson: String,
        memberNumber: String = "88888"
    ): YanbaoMemory {
        return YanbaoMemory(
            imagePath = imagePath,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            weatherType = weatherType,
            shootingMode = shootingMode,
            parameterSnapshotJson = parameterSnapshotJson,
            memberNumber = memberNumber,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * 创建 Mock YanbaoMemory 实例（Phase 1 使用）
     */
    fun createMock(imagePath: String, parameterSnapshotJson: String): YanbaoMemory {
        return create(
            imagePath = imagePath,
            latitude = 39.9042, // 北京天安门 Mock 数据
            longitude = 116.4074,
            locationName = "北京市东城区",
            weatherType = "晴",
            shootingMode = "Photo",
            parameterSnapshotJson = parameterSnapshotJson,
            memberNumber = "88888"
        )
    }
}
