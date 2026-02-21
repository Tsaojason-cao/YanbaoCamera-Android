package com.yanbao.camera.core.util

import androidx.compose.ui.graphics.Color

/**
 * 大师模式核心引擎
 * 
 * 功能：
 * - 地理位置指纹映射算法
 * - 从 LBS 机位映射到真实的硬件级参数
 * - 防止假滤镜，确保参数真实下发到 Camera2 API
 */
object MasterModeEngine {

    /**
     * 根据地理位置获取硬件参数
     * 
     * @param spotName 机位名称（如"台北101"、"东京塔"）
     * @return 真实的硬件级参数
     */
    fun getHardwareParamsByLocation(spotName: String): CameraHardwareParams {
        return when (spotName) {
            "台北101", "台北101观景台" -> CameraHardwareParams(
                kelvin = 6500,    // 针对城市夜景调高色温
                contrast = 1.2f,  // 增加对比度
                saturation = 1.1f, // 提升饱和度
                exposureNanos = 1_000_000_000L / 30, // 1/30s 快门
                masterName = "Master_001",
                locationTag = "台北101",
                colorGradient = listOf(
                    Color(0xFF1E3A8A), // 深蓝
                    Color(0xFFFBBF24)  // 金黄
                )
            )
            "东京塔", "东京塔夜景台" -> CameraHardwareParams(
                kelvin = 3200,    // 暖黄色调
                contrast = 1.5f,
                saturation = 1.3f,
                exposureNanos = 1_000_000_000L / 60, // 1/60s 快门
                masterName = "Master_002",
                locationTag = "东京塔",
                colorGradient = listOf(
                    Color(0xFFDC2626), // 红色
                    Color(0xFFFBBF24)  // 金黄
                )
            )
            "九份老街" -> CameraHardwareParams(
                kelvin = 2800,    // 暖红色调
                contrast = 1.4f,
                saturation = 1.2f,
                exposureNanos = 1_000_000_000L / 50,
                masterName = "Master_003",
                locationTag = "九份老街",
                colorGradient = listOf(
                    Color(0xFFDC2626), // 红色
                    Color(0xFFF59E0B)  // 橙色
                )
            )
            "日月潭" -> CameraHardwareParams(
                kelvin = 5500,    // 自然光色调
                contrast = 1.1f,
                saturation = 1.0f,
                exposureNanos = 1_000_000_000L / 125,
                masterName = "Master_004",
                locationTag = "日月潭",
                colorGradient = listOf(
                    Color(0xFF0EA5E9), // 天蓝
                    Color(0xFF10B981)  // 绿色
                )
            )
            "太鲁阁国家公园" -> CameraHardwareParams(
                kelvin = 6000,    // 清晨光色调
                contrast = 1.3f,
                saturation = 1.1f,
                exposureNanos = 1_000_000_000L / 100,
                masterName = "Master_005",
                locationTag = "太鲁阁",
                colorGradient = listOf(
                    Color(0xFF059669), // 深绿
                    Color(0xFF10B981)  // 浅绿
                )
            )
            else -> CameraHardwareParams(
                kelvin = 5000,
                contrast = 1.0f,
                saturation = 1.0f,
                exposureNanos = 0L,
                masterName = "Master_Default",
                locationTag = "未知",
                colorGradient = listOf(
                    Color(0xFF6B7280), // 灰色
                    Color(0xFF9CA3AF)
                )
            )
        }
    }

    /**
     * 获取所有预设的大师模式滤镜
     */
    fun getAllMasterFilters(): List<MasterFilter> {
        return listOf(
            MasterFilter(
                id = "master_001",
                name = "Master_001",
                location = "台北101",
                params = getHardwareParamsByLocation("台北101"),
                thumbnailUrl = "",
                description = "城市夜景专用，高色温+高对比度"
            ),
            MasterFilter(
                id = "master_002",
                name = "Master_002",
                location = "东京塔",
                params = getHardwareParamsByLocation("东京塔"),
                thumbnailUrl = "",
                description = "暖黄色调，适合夜景拍摄"
            ),
            MasterFilter(
                id = "master_003",
                name = "Master_003",
                location = "九份老街",
                params = getHardwareParamsByLocation("九份老街"),
                thumbnailUrl = "",
                description = "暖红色调，古色古香"
            ),
            MasterFilter(
                id = "master_004",
                name = "Master_004",
                location = "日月潭",
                params = getHardwareParamsByLocation("日月潭"),
                thumbnailUrl = "",
                description = "自然光色调，湖光山色"
            ),
            MasterFilter(
                id = "master_005",
                name = "Master_005",
                location = "太鲁阁",
                params = getHardwareParamsByLocation("太鲁阁"),
                thumbnailUrl = "",
                description = "清晨光色调，峡谷景观"
            )
        )
    }
}

/**
 * 相机硬件参数数据类
 * 
 * 这些参数会真实下发到 Camera2 API
 */
data class CameraHardwareParams(
    val kelvin: Int,              // 色温（开尔文）
    val contrast: Float,          // 对比度（1.0 为正常）
    val saturation: Float,        // 饱和度（1.0 为正常）
    val exposureNanos: Long,      // 曝光时间（纳秒）
    val masterName: String = "",  // 大师模式名称
    val locationTag: String = "", // 地理位置标签
    val colorGradient: List<Color> = emptyList() // 背景渐变色
)

/**
 * 大师模式滤镜数据类
 */
data class MasterFilter(
    val id: String,
    val name: String,
    val location: String,
    val params: CameraHardwareParams,
    val thumbnailUrl: String,
    val description: String
)
