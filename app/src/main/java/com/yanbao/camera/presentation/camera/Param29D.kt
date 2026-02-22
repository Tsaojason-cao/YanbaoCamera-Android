package com.yanbao.camera.presentation.camera

/**
 * 29D 参数描述符
 *
 * 每个参数包含：
 * - key:     与 Camera29DState 字段名对应的唯一键
 * - label:   UI 显示名称（中文）
 * - group:   所属分组（Tab 名称）
 * - min:     最小值
 * - max:     最大值
 * - default: 默认值（重置时恢复）
 * - unit:    单位字符串（空字符串表示无单位）
 */
data class Param29D(
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
 *
 * 分组：
 * - 基础曝光（6个）：ISO、快门、曝光补偿、亮度、对比度、动态范围
 * - 色彩（7个）：色温、色调、饱和度、自然饱和度、色相、肤色、色彩增强
 * - 色彩通道（7个）：红、绿、蓝、青、品红、黄、橙
 * - 明暗细节（5个）：高光、阴影、白色、黑色、清晰度
 * - 质感+美颜（4个）：锐度、降噪、颗粒、磨皮
 */
val ALL_29D_PARAMS: List<Param29D> = listOf(

    // ── 基础曝光 ──────────────────────────────────
    Param29D(
        key = "iso", label = "ISO 感光度", group = "基础曝光",
        min = 100f, max = 6400f, default = 400f, unit = ""
    ),
    Param29D(
        key = "shutter", label = "快门速度", group = "基础曝光",
        min = 0.000125f, max = 30f, default = 0.033f, unit = "s"
    ),
    Param29D(
        key = "exposure", label = "曝光补偿", group = "基础曝光",
        min = -3f, max = 3f, default = 0f, unit = "EV"
    ),
    Param29D(
        key = "brightness", label = "亮度", group = "基础曝光",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "contrast", label = "对比度", group = "基础曝光",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "dynamicRange", label = "动态范围", group = "基础曝光",
        min = 0f, max = 1f, default = 0.5f, unit = ""
    ),

    // ── 色彩 ──────────────────────────────────────
    Param29D(
        key = "colorTemp", label = "色温", group = "色彩",
        min = 2000f, max = 10000f, default = 5500f, unit = "K"
    ),
    Param29D(
        key = "tint", label = "色调", group = "色彩",
        min = -100f, max = 100f, default = 0f, unit = ""
    ),
    Param29D(
        key = "saturation", label = "饱和度", group = "色彩",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "vibrance", label = "自然饱和度", group = "色彩",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "hue", label = "色相", group = "色彩",
        min = -180f, max = 180f, default = 0f, unit = "°"
    ),
    Param29D(
        key = "skinTone", label = "肤色", group = "色彩",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "colorBoost", label = "色彩增强", group = "色彩",
        min = 0f, max = 1f, default = 0f, unit = ""
    ),

    // ── 色彩通道 ──────────────────────────────────
    Param29D(
        key = "red", label = "红色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "green", label = "绿色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "blue", label = "蓝色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "cyan", label = "青色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "magenta", label = "品红", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "yellow", label = "黄色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "orange", label = "橙色", group = "色彩通道",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),

    // ── 明暗细节 ──────────────────────────────────
    Param29D(
        key = "highlights", label = "高光", group = "明暗细节",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "shadows", label = "阴影", group = "明暗细节",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "whites", label = "白色", group = "明暗细节",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "blacks", label = "黑色", group = "明暗细节",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "clarity", label = "清晰度", group = "明暗细节",
        min = -1f, max = 1f, default = 0f, unit = ""
    ),

    // ── 质感+美颜 ─────────────────────────────────
    Param29D(
        key = "sharpness", label = "锐度", group = "质感+美颜",
        min = 0f, max = 1f, default = 0.5f, unit = ""
    ),
    Param29D(
        key = "noiseReduction", label = "降噪", group = "质感+美颜",
        min = 0f, max = 1f, default = 0.3f, unit = ""
    ),
    Param29D(
        key = "grain", label = "颗粒感", group = "质感+美颜",
        min = 0f, max = 1f, default = 0f, unit = ""
    ),
    Param29D(
        key = "beautySmooth", label = "磨皮", group = "质感+美颜",
        min = 0f, max = 1f, default = 0f, unit = ""
    )
)
