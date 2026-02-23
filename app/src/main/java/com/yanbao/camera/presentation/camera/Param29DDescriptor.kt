package com.yanbao.camera.presentation.camera

/**
 * 29D 参数描述符（用于 Parameter29DPanel 的参数元数据）
 *
 * 注意：此类为描述符，与 Param29D（值类）区分。
 * - Param29DDescriptor：描述参数的元数据（key/label/group/min/max/default/unit）
 * - Param29D：存储参数的实际值（iso/shutterSpeed/ev/...）
 */
data class Param29DDescriptor(
    val key: String,
    val label: String,
    val group: String,
    val min: Float,
    val max: Float,
    val default: Float,
    val unit: String = ""
)

/**
 * 全部 29 个参数的完整列表
 */
val ALL_29D_PARAMS: List<Param29DDescriptor> = listOf(

    // ── 基础曝光 ──────────────────────────────────
    Param29DDescriptor(
        key = "iso", label = "ISO 感光度", group = "基础曝光",
        min = 100f, max = 6400f, default = 400f, unit = ""
    ),
    Param29DDescriptor(
        key = "shutter", label = "快门速度", group = "基础曝光",
        min = 0.000125f, max = 30f, default = 0.033f, unit = "s"
    ),
    Param29DDescriptor(
        key = "exposure", label = "曝光补偿", group = "基础曝光",
        min = -3f, max = 3f, default = 0f, unit = "EV"
    ),
    Param29DDescriptor(
        key = "brightness", label = "亮度", group = "基础曝光",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "contrast", label = "对比度", group = "基础曝光",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "dynamicRange", label = "动态范围", group = "基础曝光",
        min = 0f, max = 1f, default = 0.5f, unit = ""
    ),

    // ── 色彩 ──────────────────────────────────────
    Param29DDescriptor(
        key = "colorTemp", label = "色温", group = "色彩",
        min = 2000f, max = 10000f, default = 5500f, unit = "K"
    ),
    Param29DDescriptor(
        key = "tint", label = "色调", group = "色彩",
        min = -100f, max = 100f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "saturation", label = "饱和度", group = "色彩",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "vibrance", label = "自然饱和度", group = "色彩",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "hue", label = "色相", group = "色彩",
        min = -180f, max = 180f, default = 0f, unit = "°"
    ),
    Param29DDescriptor(
        key = "skinTone", label = "肤色", group = "色彩",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "colorBoost", label = "色彩增强", group = "色彩",
        min = 0f, max = 1f, default = 0f, unit = ""
    ),

    // ── 色彩通道 ──────────────────────────────────
    Param29DDescriptor(
        key = "red", label = "红色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "green", label = "绿色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "blue", label = "蓝色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "cyan", label = "青色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "magenta", label = "品红", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "yellow", label = "黄色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "orange", label = "橙色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),

    // ── 明暗细节 ──────────────────────────────────
    Param29DDescriptor(
        key = "highlights", label = "高光", group = "明暗细节",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "shadows", label = "阴影", group = "明暗细节",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "whites", label = "白色", group = "明暗细节",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "blacks", label = "黑色", group = "明暗细节",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "clarity", label = "清晰度", group = "明暗细节",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),

    // ── 质感+美颜 ─────────────────────────────────
    Param29DDescriptor(
        key = "sharpness", label = "锐度", group = "质感+美颜",
        min = 0f, max = 1f, default = 0.5f, unit = ""
    ),
    Param29DDescriptor(
        key = "noiseReduction", label = "降噪", group = "质感+美颜",
        min = 0f, max = 1f, default = 0.3f, unit = ""
    ),
    Param29DDescriptor(
        key = "grain", label = "颗粒感", group = "质感+美颜",
        min = 0f, max = 1f, default = 0f, unit = ""
    ),
    Param29DDescriptor(
        key = "beautySmooth", label = "磨皮", group = "质感+美颜",
        min = 0f, max = 1f, default = 0f, unit = ""
    )
)
