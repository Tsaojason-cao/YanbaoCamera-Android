package com.yanbao.camera.data.model

import kotlinx.serialization.Serializable

/**
 * yanbao AI: 29D Memory Schema v1.0
 * 
 * 每張照片的"记忆包 (Memory Package)"
 * 包含 29D 參數矩陣、Git Commit ID、LBS 坐標等核心數據
 */

@Serializable
data class YanbaoMemorySchema(
    val version: String = "1.0",
    val metadata: Metadata,
    val cameraConfig: CameraConfig,
    val editHistory: List<EditStep> = emptyList()
)

@Serializable
data class Metadata(
    val uid: String, // YB-888888
    val commitId: String, // Git SHA1
    val timestamp: String, // ISO 8601 格式
    val location: Location
)

@Serializable
data class Location(
    val lbsCoord: List<Double>, // [longitude, latitude]
    val name: String // "Tokyo, Shinjuku"
)

@Serializable
data class CameraConfig(
    val mode: String, // "29D_MASTER", "BEAUTY", "AR_SPACE", etc.
    val hardware: HardwareParams,
    val render29D: Render29D
)

@Serializable
data class HardwareParams(
    val iso: Int,
    val shutterSpeed: String, // "1/250"
    val aperture: String, // "f/1.8"
    val focalLength: String // "35mm"
)

/**
 * 29D 參數矩陣
 * 
 * D1-D5: 物理光影（曝光、高光壓縮、暗部補償、冷暖、色偏）
 * D6-D15: 色彩空間（RGB 交叉通道、飽和度梯度、肤色保護、色相偏移）
 * D16-D25: 物理紋理（顆粒、色散、衍射、銳化、邊緣衰減）
 * D26-D29: AI 骨骼/空間（臉部折疊度、骨骼比例、空間深度、LBS 環境光補償）
 */
@Serializable
data class Render29D(
    val lightDim1To5: List<Float>, // D1-D5: 物理光影
    val colorMatrix6To15: List<Float>, // D6-D15: 色彩空間
    val texture16To25: List<Float>, // D16-D25: 物理紋理
    val aiBone26To29: List<Float>, // D26-D29: AI 骨骼/空間
    val masterFilterId: String // "KUROMI_DREAM_91"
) {
    /**
     * 將 29D 參數展平為 Float 數組（供 GPU Shader 使用）
     */
    fun toFloatArray(): FloatArray {
        return (lightDim1To5 + colorMatrix6To15 + texture16To25 + aiBone26To29).toFloatArray()
    }
    
    companion object {
        /**
         * 創建默認的 29D 參數（中性參數）
         */
        fun default() = Render29D(
            lightDim1To5 = listOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 曝光=1.0，其他為0
            colorMatrix6To15 = listOf(1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
            texture16To25 = listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
            aiBone26To29 = listOf(1.0f, 1.0f, 1.0f, 1.0f), // 骨骼比例=1.0（無調整）
            masterFilterId = "NONE"
        )
    }
}

@Serializable
data class EditStep(
    val step: Int,
    val tool: String, // "2.9D_OFFSET", "BRIGHTNESS", etc.
    val value: String, // "+0.15"
    val timestamp: String
)

/**
 * 9 大模式枚舉
 */
enum class YanbaoMode(val displayName: String) {
    MEMORY("雁宝记忆"),
    MASTER("大師模式"),
    RENDER_29D("29D 渲染"),
    AR_SPACE("AR 空間"),
    ORIGINAL("原相機"),
    VIDEO_MASTER("視頻大師"),
    BEAUTY("美顏/塑形"),
    BASIC("基本模式"),
    PRO("專業模式");
    
    companion object {
        fun fromString(mode: String): YanbaoMode {
            return values().find { it.name == mode } ?: BASIC
        }
    }
}
