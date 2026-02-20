package com.yanbao.camera.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 29D 参数状态矩阵
 * 
 * 严禁占位符，所有参数都有真实的初始值和范围
 * 使用 StateFlow 管理，确保滑块一动，预览立即响应
 */
@Serializable
data class Camera29DState(
    // 基础曝光参数
    val brightness: Float = 0.5f,        // 亮度 [0.0, 1.0]
    val contrast: Float = 0.5f,          // 对比度 [0.0, 1.0]
    val saturation: Float = 0.5f,        // 饱和度 [0.0, 1.0]
    val sharpness: Float = 0.5f,         // 锐度 [0.0, 1.0]
    val exposure: Float = 0.5f,          // 曝光 [0.0, 1.0]
    
    // 色彩参数
    val colorTemp: Float = 5000f,        // 色温 [2000, 10000] K
    val tint: Float = 0.5f,              // 色调 [0.0, 1.0]
    val hue: Float = 0.5f,               // 色相 [0.0, 1.0]
    val vibrance: Float = 0.5f,          // 自然饱和度 [0.0, 1.0]
    
    // 高级色彩通道
    val red: Float = 0.5f,               // 红色通道 [0.0, 1.0]
    val green: Float = 0.5f,             // 绿色通道 [0.0, 1.0]
    val blue: Float = 0.5f,              // 蓝色通道 [0.0, 1.0]
    val cyan: Float = 0.5f,              // 青色通道 [0.0, 1.0]
    val magenta: Float = 0.5f,           // 品红通道 [0.0, 1.0]
    val yellow: Float = 0.5f,            // 黄色通道 [0.0, 1.0]
    val orange: Float = 0.5f,            // 橙色通道 [0.0, 1.0]
    
    // 明暗细节
    val highlights: Float = 0.5f,        // 高光 [0.0, 1.0]
    val shadows: Float = 0.5f,           // 阴影 [0.0, 1.0]
    val whites: Float = 0.5f,            // 白色 [0.0, 1.0]
    val blacks: Float = 0.5f,            // 黑色 [0.0, 1.0]
    
    // 细节与清晰度
    val clarity: Float = 0.5f,           // 清晰度 [0.0, 1.0]
    val dehaze: Float = 0.5f,            // 去雾 [0.0, 1.0]
    val noiseReduction: Float = 0.5f,    // 降噪 [0.0, 1.0]
    val grain: Float = 0.0f,             // 颗粒 [0.0, 1.0]
    val vignette: Float = 0.0f,          // 暗角 [0.0, 1.0]
    
    // 美颜参数
    val beautySmooth: Float = 0.0f,      // 磨皮 [0.0, 1.0]
    val beautyWhiten: Float = 0.0f,      // 美白 [0.0, 1.0]
    val beautyEyeEnlarge: Float = 0.0f,  // 大眼 [0.0, 1.0]
    val beautyFaceSlim: Float = 0.0f,    // 瘦脸 [0.0, 1.0]
    
    // 相机硬件参数（Camera2 API）
    val iso: Int = 400,                  // ISO [100, 6400]
    val exposureTime: Long = 1000000L,   // 快门速度（纳秒）[1/8000s, 30s]
    val whiteBalance: Int = 5500,        // 白平衡（K值）[2000, 10000]
    val focusMode: String = "AUTO",      // 对焦模式 ["AUTO", "MANUAL"]
    val zoomRatio: Float = 1.0f,         // 变焦比例 [0.5, 10.0]
    
    // 特殊模式
    val masterFilterId: String? = null,  // 大师滤镜 ID
    val is2Dot9DEnabled: Boolean = false, // 2.9D 模式开关
    val parallaxOffset: Pair<Float, Float> = 0f to 0f // 2.9D 视差偏移
) {
    /**
     * 转换为 JSON 字符串（用于存储到数据库）
     */
    fun toJson(): String {
        return Json.encodeToString(this)
    }
    
    companion object {
        /**
         * 从 JSON 字符串解析
         */
        fun fromJson(json: String): Camera29DState {
            return Json.decodeFromString(json)
        }
        
        /**
         * 获取参数的显示名称
         */
        fun getParameterDisplayName(name: String): String {
            return when (name) {
                "brightness" -> "亮度"
                "contrast" -> "对比度"
                "saturation" -> "饱和度"
                "sharpness" -> "锐度"
                "exposure" -> "曝光"
                "colorTemp" -> "色温"
                "tint" -> "色调"
                "hue" -> "色相"
                "vibrance" -> "自然饱和度"
                "red" -> "红色"
                "green" -> "绿色"
                "blue" -> "蓝色"
                "cyan" -> "青色"
                "magenta" -> "品红"
                "yellow" -> "黄色"
                "orange" -> "橙色"
                "highlights" -> "高光"
                "shadows" -> "阴影"
                "whites" -> "白色"
                "blacks" -> "黑色"
                "clarity" -> "清晰度"
                "dehaze" -> "去雾"
                "noiseReduction" -> "降噪"
                "grain" -> "颗粒"
                "vignette" -> "暗角"
                "beautySmooth" -> "磨皮"
                "beautyWhiten" -> "美白"
                "beautyEyeEnlarge" -> "大眼"
                "beautyFaceSlim" -> "瘦脸"
                "iso" -> "ISO"
                "exposureTime" -> "快门速度"
                "whiteBalance" -> "白平衡"
                "focusMode" -> "对焦模式"
                "zoomRatio" -> "变焦"
                else -> name
            }
        }
    }
}

/**
 * 相机模式枚举
 */
enum class CameraMode(val displayName: String) {
    PHOTO("拍照"),
    VIDEO("录像"),
    PORTRAIT("人像"),
    NIGHT("夜景"),
    PROFESSIONAL("专业"),
    PANORAMA("全景"),
    TIMELAPSE("延时"),
    MASTER_FILTER("大师滤镜"),
    MODE_29D("2.9D")
}
