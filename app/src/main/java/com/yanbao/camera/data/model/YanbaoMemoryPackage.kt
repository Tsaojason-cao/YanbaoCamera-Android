package com.yanbao.camera.data.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * 雁宝记忆参数包 (YanbaoMemoryPackage)
 *
 * 这是"雁宝记忆"功能的核心数据结构。
 * 每次拍照后，当前所有模式的参数都会被序列化为此 JSON 包，
 * 存入数据库并通过 Git 备份到云端。
 *
 * 反向操作：从相册选择旧照片时，可从其 Metadata 中反序列化此包，
 * 并 1:1 恢复到当前取景器，实现"拍出跟上次一模一样质感"。
 *
 * 版本: 2.0 (满血版，对标 B612/轻颜/VSCO/ProCamera)
 */
data class YanbaoMemoryPackage(

    // ── 基础信息 ──────────────────────────────────────────────────────────
    @SerializedName("version") val version: String = "2.0",
    @SerializedName("packageId") val packageId: String = "",
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("shootingMode") val shootingMode: String = "BASIC",
    @SerializedName("imagePath") val imagePath: String = "",
    @SerializedName("thumbnailBase64") val thumbnailBase64: String = "",

    // ── 相机基本参数 (BasicCameraParams) ─────────────────────────────────
    @SerializedName("basic") val basic: BasicCameraParams = BasicCameraParams(),

    // ── 原相机手动参数 (ManualCameraParams) ──────────────────────────────
    @SerializedName("manual") val manual: ManualCameraParams = ManualCameraParams(),

    // ── 大师滤镜参数 (MasterFilterParams) ────────────────────────────────
    @SerializedName("masterFilter") val masterFilter: MasterFilterParams = MasterFilterParams(),

    // ── 29D 渲染参数 (Render29DParams) ───────────────────────────────────
    @SerializedName("render29D") val render29D: Render29DParams = Render29DParams(),

    // ── 2.9D 视差参数 (Parallax29DParams) ────────────────────────────────
    @SerializedName("parallax") val parallax: Parallax29DParams = Parallax29DParams(),

    // ── 美颜塑形参数 (BeautyShapeParams) ─────────────────────────────────
    @SerializedName("beauty") val beauty: BeautyShapeParams = BeautyShapeParams(),

    // ── 视频大师参数 (VideoMasterParams) ─────────────────────────────────
    @SerializedName("video") val video: VideoMasterParams = VideoMasterParams(),

    // ── LBS 地理信息 (LbsInfo) ────────────────────────────────────────────
    @SerializedName("lbs") val lbs: LbsInfo = LbsInfo()
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): YanbaoMemoryPackage? = try {
            Gson().fromJson(json, YanbaoMemoryPackage::class.java)
        } catch (e: Exception) {
            null
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 子数据类
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 相机基本模式参数
 * 对标: 美颜相机、B612 基础拍摄功能
 */
data class BasicCameraParams(
    @SerializedName("flashMode") val flashMode: String = "AUTO",       // OFF / AUTO / ON / TORCH
    @SerializedName("timerSeconds") val timerSeconds: Int = 0,         // 0 / 3 / 5 / 10
    @SerializedName("gridType") val gridType: String = "NONE",         // NONE / RULE_OF_THIRDS / SQUARE / GOLDEN_RATIO / DIAGONAL
    @SerializedName("levelEnabled") val levelEnabled: Boolean = false, // 水平仪
    @SerializedName("aspectRatio") val aspectRatio: String = "4:3",    // 1:1 / 3:4 / 4:3 / 9:16 / 16:9 / FULL
    @SerializedName("hdrMode") val hdrMode: String = "AUTO",           // OFF / AUTO / ON
    @SerializedName("mirrorFront") val mirrorFront: Boolean = true,    // 前置镜像
    @SerializedName("volumeShutter") val volumeShutter: Boolean = true,// 音量键快门
    @SerializedName("locationTag") val locationTag: Boolean = true,    // 位置标记
    @SerializedName("watermark") val watermark: String = "YANBAO"      // NONE / YANBAO / DATE / CUSTOM
)

/**
 * 原相机手动模式参数
 * 对标: ProCamera、iPhone 专业模式
 */
data class ManualCameraParams(
    @SerializedName("iso") val iso: Int = -1,                          // -1 = AUTO, 50-6400
    @SerializedName("shutterSpeedNanos") val shutterSpeedNanos: Long = -1L, // -1 = AUTO
    @SerializedName("whiteBalanceMode") val whiteBalanceMode: String = "AUTO", // AUTO / DAYLIGHT / CLOUDY / TUNGSTEN / FLUORESCENT / CUSTOM
    @SerializedName("whiteBalanceKelvin") val whiteBalanceKelvin: Int = 5500,  // 2000-10000K
    @SerializedName("focusMode") val focusMode: String = "AUTO",       // AUTO / MANUAL / MACRO / INFINITY
    @SerializedName("focusDistance") val focusDistance: Float = 0f,    // 0.0 (near) - 1.0 (far)
    @SerializedName("exposureCompensation") val exposureCompensation: Float = 0f, // -3.0 to +3.0 EV
    @SerializedName("aperture") val aperture: Float = -1f,             // -1 = AUTO (if supported)
    @SerializedName("noiseReduction") val noiseReduction: String = "FAST", // OFF / FAST / HIGH_QUALITY
    @SerializedName("opticalStabilization") val opticalStabilization: Boolean = true,
    @SerializedName("rawCapture") val rawCapture: Boolean = false,     // RAW + JPEG 双保存
    @SerializedName("histogramEnabled") val histogramEnabled: Boolean = false,
    @SerializedName("focusPeakingEnabled") val focusPeakingEnabled: Boolean = false,
    @SerializedName("zebraPatternEnabled") val zebraPatternEnabled: Boolean = false // 斑马纹过曝警告
)

/**
 * 大师滤镜参数
 * 91 个独家滤镜，支持强度调节
 */
data class MasterFilterParams(
    @SerializedName("filterId") val filterId: String = "",             // 滤镜 ID（如 "HASSELBLAD_CLASSIC"）
    @SerializedName("filterName") val filterName: String = "",
    @SerializedName("intensity") val intensity: Float = 1.0f,         // 0.0 - 1.0 滤镜强度
    @SerializedName("blendMode") val blendMode: String = "NORMAL",    // NORMAL / SOFT_LIGHT / OVERLAY
    @SerializedName("customAdjustments") val customAdjustments: Map<String, Float> = emptyMap()
)

/**
 * 29D 渲染参数（完整 29 维度）
 * 对应 Yanbao29DRenderEngine 的 Shader 参数
 */
data class Render29DParams(
    // D1-D5: 光影维度
    @SerializedName("d1_exposure") val d1Exposure: Float = 0f,
    @SerializedName("d2_highlight") val d2Highlight: Float = 0f,
    @SerializedName("d3_shadow") val d3Shadow: Float = 0f,
    @SerializedName("d4_contrast") val d4Contrast: Float = 0f,
    @SerializedName("d5_brightness") val d5Brightness: Float = 0f,
    // D6-D10: 色彩维度
    @SerializedName("d6_saturation") val d6Saturation: Float = 0f,
    @SerializedName("d7_vibrance") val d7Vibrance: Float = 0f,
    @SerializedName("d8_temperature") val d8Temperature: Float = 0f,
    @SerializedName("d9_tint") val d9Tint: Float = 0f,
    @SerializedName("d10_hue") val d10Hue: Float = 0f,
    // D11-D15: 材质维度
    @SerializedName("d11_clarity") val d11Clarity: Float = 0f,
    @SerializedName("d12_texture") val d12Texture: Float = 0f,
    @SerializedName("d13_dehaze") val d13Dehaze: Float = 0f,
    @SerializedName("d14_vignette") val d14Vignette: Float = 0f,
    @SerializedName("d15_grain") val d15Grain: Float = 0f,
    // D16-D20: 色调曲线维度
    @SerializedName("d16_curveR") val d16CurveR: Float = 0f,
    @SerializedName("d17_curveG") val d17CurveG: Float = 0f,
    @SerializedName("d18_curveB") val d18CurveB: Float = 0f,
    @SerializedName("d19_curveLum") val d19CurveLum: Float = 0f,
    @SerializedName("d20_splitTone") val d20SplitTone: Float = 0f,
    // D21-D25: AI 重构维度
    @SerializedName("d21_aiSkin") val d21AiSkin: Float = 0f,
    @SerializedName("d22_aiSky") val d22AiSky: Float = 0f,
    @SerializedName("d23_aiObject") val d23AiObject: Float = 0f,
    @SerializedName("d24_aiDepth") val d24AiDepth: Float = 0f,
    @SerializedName("d25_aiNight") val d25AiNight: Float = 0f,
    // D26-D29: 骨骼/空间维度
    @SerializedName("d26_perspectiveH") val d26PerspectiveH: Float = 0f,
    @SerializedName("d27_perspectiveV") val d27PerspectiveV: Float = 0f,
    @SerializedName("d28_lensDistortion") val d28LensDistortion: Float = 0f,
    @SerializedName("d29_chromaticAberration") val d29ChromaticAberration: Float = 0f
)

/**
 * 2.9D 视差参数
 */
data class Parallax29DParams(
    @SerializedName("depthScale") val depthScale: Float = 1.0f,
    @SerializedName("parallaxStrength") val parallaxStrength: Float = 0.5f,
    @SerializedName("foregroundLayers") val foregroundLayers: Int = 3,
    @SerializedName("backgroundBlur") val backgroundBlur: Float = 0.3f,
    @SerializedName("motionSensitivity") val motionSensitivity: Float = 0.8f
)

/**
 * 美颜塑形参数（满血版 - 对标 B612/轻颜/Ulike/美颜相机）
 *
 * 分类：
 * - 皮肤类 (Skin): 磨皮、美白、祛痘、祛斑、去黑眼圈、去法令纹、去颈纹
 * - 脸型类 (Face Shape): 瘦脸、V脸、小脸、下颌骨、发际线、窄颧骨
 * - 五官类 (Features): 大眼、眼距、眼角、隆鼻、瘦鼻、嘴型、白牙、牙齿矫正
 * - 身材类 (Body): 长腿、瘦腰、瘦肩、丰胸
 * - 亮眼类 (Eyes): 亮眼、眼白、眼神光
 */
data class BeautyShapeParams(
    // ── 皮肤类 ──────────────────────────────────────────────────────────
    @SerializedName("smoothness") val smoothness: Int = 0,             // 磨皮 0-100
    @SerializedName("whitening") val whitening: Int = 0,               // 美白 0-100
    @SerializedName("acneRemoval") val acneRemoval: Int = 0,           // 祛痘 0-100
    @SerializedName("blemishRemoval") val blemishRemoval: Int = 0,     // 祛斑 0-100
    @SerializedName("darkCircleRemoval") val darkCircleRemoval: Int = 0, // 去黑眼圈 0-100
    @SerializedName("nasolabialFolds") val nasolabialFolds: Int = 0,   // 去法令纹 0-100
    @SerializedName("neckLines") val neckLines: Int = 0,               // 去颈纹 0-100
    @SerializedName("poreMinimizer") val poreMinimizer: Int = 0,       // 缩毛孔 0-100
    @SerializedName("rednessRemoval") val rednessRemoval: Int = 0,     // 去红血丝 0-100
    @SerializedName("skinTone") val skinTone: Int = 0,                 // 肤色调整 -50 to +50

    // ── 脸型类 ──────────────────────────────────────────────────────────
    @SerializedName("faceThin") val faceThin: Int = 0,                 // 瘦脸 0-100
    @SerializedName("vFace") val vFace: Int = 0,                       // V脸 0-100
    @SerializedName("smallFace") val smallFace: Int = 0,               // 小脸 0-100
    @SerializedName("jawbone") val jawbone: Int = 0,                   // 瘦下颌骨 0-100
    @SerializedName("hairline") val hairline: Int = 0,                 // 发际线 -50 to +50
    @SerializedName("cheekbone") val cheekbone: Int = 0,               // 窄颧骨 0-100
    @SerializedName("chinLength") val chinLength: Int = 0,             // 下巴长度 -50 to +50
    @SerializedName("foreheadHeight") val foreheadHeight: Int = 0,     // 额头高度 -50 to +50

    // ── 五官类 ──────────────────────────────────────────────────────────
    @SerializedName("eyeEnlarge") val eyeEnlarge: Int = 0,             // 大眼 0-100
    @SerializedName("eyeDistance") val eyeDistance: Int = 0,           // 眼距 -50 to +50
    @SerializedName("eyeCorner") val eyeCorner: Int = 0,               // 眼角 0-100
    @SerializedName("eyeTilt") val eyeTilt: Int = 0,                   // 眼睛倾斜 -50 to +50
    @SerializedName("noseLift") val noseLift: Int = 0,                 // 隆鼻 0-100
    @SerializedName("noseThin") val noseThin: Int = 0,                 // 瘦鼻 0-100
    @SerializedName("noseLength") val noseLength: Int = 0,             // 鼻子长度 -50 to +50
    @SerializedName("lipShape") val lipShape: Int = 0,                 // 嘴型 -50 to +50
    @SerializedName("lipSize") val lipSize: Int = 0,                   // 嘴唇大小 -50 to +50
    @SerializedName("teethWhitening") val teethWhitening: Int = 0,     // 白牙 0-100
    @SerializedName("teethStraighten") val teethStraighten: Int = 0,   // 牙齿矫正 0-100

    // ── 亮眼类 ──────────────────────────────────────────────────────────
    @SerializedName("eyeBrightness") val eyeBrightness: Int = 0,       // 亮眼 0-100
    @SerializedName("eyeWhitening") val eyeWhitening: Int = 0,         // 眼白 0-100
    @SerializedName("eyeHighlight") val eyeHighlight: Int = 0,         // 眼神光 0-100

    // ── 身材类 ──────────────────────────────────────────────────────────
    @SerializedName("legLengthen") val legLengthen: Int = 0,           // 长腿 0-100
    @SerializedName("waistThin") val waistThin: Int = 0,               // 瘦腰 0-100
    @SerializedName("shoulderThin") val shoulderThin: Int = 0,         // 瘦肩 0-100
    @SerializedName("bustEnhance") val bustEnhance: Int = 0,           // 丰胸 0-100

    // ── 预设存储 ─────────────────────────────────────────────────────────
    @SerializedName("presetName") val presetName: String = "",         // 自定义预设名称
    @SerializedName("presetId") val presetId: String = ""             // 预设 ID（用于快速切换）
)

/**
 * 视频大师参数
 */
data class VideoMasterParams(
    @SerializedName("resolution") val resolution: String = "1080P",    // 720P / 1080P / 4K
    @SerializedName("frameRate") val frameRate: Int = 30,              // 24 / 30 / 60 (4K 最高 60)
    @SerializedName("stabilization") val stabilization: String = "CINEMATIC", // OFF / STANDARD / CINEMATIC
    @SerializedName("zoomSmoothness") val zoomSmoothness: Float = 0.8f,
    @SerializedName("realtimeFilter") val realtimeFilter: String = "",  // 实时视频滤镜 ID
    @SerializedName("audioGain") val audioGain: Float = 1.0f,
    @SerializedName("slowMotionFps") val slowMotionFps: Int = 0,       // 0=关闭, 120, 240
    @SerializedName("timelapseFactor") val timelapseFactor: Int = 0    // 0=关闭, 2x, 4x, 8x
)

/**
 * LBS 地理信息
 */
data class LbsInfo(
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0,
    @SerializedName("locationName") val locationName: String = "",
    @SerializedName("cityName") val cityName: String = "",
    @SerializedName("countryCode") val countryCode: String = "",
    @SerializedName("weatherType") val weatherType: String = "",       // SUNNY / CLOUDY / RAINY / SNOWY / FOGGY
    @SerializedName("temperature") val temperature: Float = 0f,
    @SerializedName("lightCondition") val lightCondition: String = ""  // GOLDEN_HOUR / BLUE_HOUR / MIDDAY / NIGHT
)
