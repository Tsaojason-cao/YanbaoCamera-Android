package com.yanbao.camera.core.engine

import android.util.Log
import com.yanbao.camera.data.lbs.HotLocation
import com.yanbao.camera.data.lbs.UserLocation
import kotlin.math.abs

/**
 * 大师模式 LBS 联动引擎
 * 
 * 🚨 核心逻辑：地理位置指纹锁定滤镜参数
 * 
 * 验收闭环：
 * - 模拟定位至"台北101" → 自动显示"101专属预设"
 * - 套用参数 → 相机29D拨盘自动旋转至预设值
 * - 画面色调自动转向高对比夜景模式
 * 
 * 使用方法：
 * ```kotlin
 * val engine = MasterLbsEngine()
 * val params = engine.autoApplyLbsFilter(currentLocation, nearbySpots)
 * if (params != null) {
 *     cameraViewModel.applyMasterParams(params)
 * }
 * ```
 */
object MasterLbsEngine {

    private const val TAG = "MasterLbsEngine"
    private const val TRIGGER_RADIUS_KM = 0.5 // 0.5km 内触发

    /**
     * 大师机位参数映射表
     * 
     * 每个标志性地点都有预设的硬件级参数
     */
    private val masterParameterStore = mapOf(
        "台北101" to MasterParams(
            name = "Master_101",
            displayName = "101 专属预设",
            kelvin = 3200,      // 色温：暖色调
            exposure = 0.5f,    // 曝光：+0.5 EV
            contrast = 20,      // 对比度：高对比
            saturation = 15,    // 饱和度：中等
            iso = 800,          // ISO：夜景
            shutterSpeed = "1/60" // 快门速度
        ),
        "东京塔" to MasterParams(
            name = "Master_Tokyo",
            displayName = "东京塔夜景",
            kelvin = 3400,
            exposure = 0.3f,
            contrast = 25,
            saturation = 20,
            iso = 1600,
            shutterSpeed = "1/30"
        ),
        "象山步道" to MasterParams(
            name = "Master_Xiangshan",
            displayName = "象山日落",
            kelvin = 4500,
            exposure = 0.8f,
            contrast = 15,
            saturation = 25,
            iso = 400,
            shutterSpeed = "1/125"
        ),
        "九份老街" to MasterParams(
            name = "Master_Jiufen",
            displayName = "九份红灯笼",
            kelvin = 2800,
            exposure = 0.2f,
            contrast = 30,
            saturation = 30,
            iso = 1200,
            shutterSpeed = "1/60"
        ),
        "日月潭" to MasterParams(
            name = "Master_SunMoonLake",
            displayName = "日月潭晨雾",
            kelvin = 5500,
            exposure = 0.0f,
            contrast = 10,
            saturation = 10,
            iso = 200,
            shutterSpeed = "1/250"
        )
    )

    /**
     * 自动应用 LBS 滤镜
     * 
     * @param currentLocation 用户当前位置
     * @param nearbySpots 附近热门地点列表
     * @return 匹配的大师参数，如果没有匹配则返回 null
     */
    fun autoApplyLbsFilter(
        currentLocation: UserLocation,
        nearbySpots: List<HotLocation>
    ): MasterParams? {
        // 1. 检索 0.5km 内的标志性机位参数
        val nearestSpot = nearbySpots
            .filter { it.distanceKm <= TRIGGER_RADIUS_KM }
            .minByOrNull { it.distanceKm }

        if (nearestSpot == null) {
            Log.d(TAG, "未检测到附近的大师机位")
            return null
        }

        // 2. 查找对应的大师参数
        val masterParams = masterParameterStore[nearestSpot.name]

        if (masterParams == null) {
            Log.d(TAG, "地点 ${nearestSpot.name} 暂无大师参数")
            return null
        }

        Log.i(TAG, "✅ 检测到大师机位: ${nearestSpot.name}")
        Log.i(TAG, "   距离: ${String.format("%.2f", nearestSpot.distanceKm)} km")
        Log.i(TAG, "   应用参数: ${masterParams.displayName}")
        Log.i(TAG, "   色温: ${masterParams.kelvin}K")
        Log.i(TAG, "   曝光: ${masterParams.exposure} EV")
        Log.i(TAG, "   ISO: ${masterParams.iso}")

        return masterParams
    }

    /**
     * 获取指定地点的大师参数
     */
    fun getMasterParamsBySpotName(spotName: String): MasterParams? {
        return masterParameterStore[spotName]
    }

    /**
     * 检查是否在大师机位范围内
     */
    fun isInMasterSpotRange(
        currentLocation: UserLocation,
        nearbySpots: List<HotLocation>
    ): Boolean {
        return nearbySpots.any { it.distanceKm <= TRIGGER_RADIUS_KM && masterParameterStore.containsKey(it.name) }
    }

    /**
     * 获取所有可用的大师参数
     */
    fun getAllMasterParams(): List<MasterParams> {
        return masterParameterStore.values.toList()
    }
}

/**
 * 大师模式参数
 * 
 * 包含硬件级的相机参数，用于自动套用到 29D 拨盘
 */
data class MasterParams(
    val name: String,           // 参数名称（如 Master_101）
    val displayName: String,    // 显示名称（如 "101 专属预设"）
    val kelvin: Int,            // 色温（K）
    val exposure: Float,        // 曝光补偿（EV）
    val contrast: Int,          // 对比度（-100 to 100）
    val saturation: Int,        // 饱和度（-100 to 100）
    val iso: Int,               // ISO 感光度
    val shutterSpeed: String    // 快门速度（如 "1/60"）
) {
    /**
     * 将快门速度字符串转换为纳秒
     * 
     * 例如：
     * - "1/8000" → 125000 ns
     * - "1/60" → 16666666 ns
     * - "30" → 30000000000 ns
     */
    fun getShutterSpeedNanos(): Long {
        return when {
            shutterSpeed.contains("/") -> {
                val parts = shutterSpeed.split("/")
                val numerator = parts[0].toFloat()
                val denominator = parts[1].toFloat()
                ((numerator / denominator) * 1_000_000_000).toLong()
            }
            else -> {
                (shutterSpeed.toFloat() * 1_000_000_000).toLong()
            }
        }
    }

    /**
     * 将色温转换为色温偏移（用于 Camera2 API）
     */
    fun getTemperatureOffset(): Int {
        // 色温映射：3000K → -100, 5500K → 0, 8000K → +100
        val baseKelvin = 5500
        return ((kelvin - baseKelvin) / 50).coerceIn(-100, 100)
    }
}
