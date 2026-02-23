package com.yanbao.camera.filter

import com.google.gson.annotations.SerializedName

/**
 * Phase 2: 国家滤镜数据类
 *
 * 对应 assets/filters.json 中的每一条记录。
 * 支持两种渲染模式：
 * 1. LUT 模式：通过 [lutAssetPath] 加载 .png/.cube 文件
 * 2. Shader 参数模式：通过 [shaderParams] 直接调整 29D 参数
 */
data class CountryFilter(
    @SerializedName("countryCode")
    val countryCode: String,

    @SerializedName("name")
    val name: String,

    /** LUT 文件路径（相对于 assets/），为 null 时使用 shaderParams */
    @SerializedName("lutAssetPath")
    val lutAssetPath: String? = null,

    /**
     * 着色器参数覆盖（键名对应 Param29DFull 字段名）
     * 例如：{"saturation": 1.3, "colorTemp": 0.6}
     */
    @SerializedName("shaderParams")
    val shaderParams: Map<String, Float> = emptyMap(),

    /** 滤镜描述（可选） */
    @SerializedName("description")
    val description: String? = null,

    /** 推荐拍摄场景（可选） */
    @SerializedName("scene")
    val scene: String? = null
) {
    /**
     * 将 shaderParams 应用到 Param29DFull，返回新实例
     */
    fun applyTo(base: Param29DFull): Param29DFull {
        var result = base.copy()
        shaderParams.forEach { (key, value) ->
            result = when (key) {
                "iso"              -> result.copy(iso = (value * 6400).toInt())
                "ev"               -> result.copy(ev = value)
                "dynamicRange"     -> result.copy(dynamicRange = value)
                "shadowComp"       -> result.copy(shadowComp = value)
                "highlightProtect" -> result.copy(highlightProtect = value)
                "colorTemp"        -> result.copy(colorTemp = value)
                "tint"             -> result.copy(tint = value)
                "saturation"       -> result.copy(saturation = value)
                "skinTone"         -> result.copy(skinTone = value)
                "redGain"          -> result.copy(redGain = value)
                "greenGain"        -> result.copy(greenGain = value)
                "blueGain"         -> result.copy(blueGain = value)
                "colorBoost"       -> result.copy(colorBoost = value)
                "sharpness"        -> result.copy(sharpness = value)
                "denoise"          -> result.copy(denoise = value)
                "grain"            -> result.copy(grain = value)
                "vignette"         -> result.copy(vignette = value)
                "clarity"          -> result.copy(clarity = value)
                "dehaze"           -> result.copy(dehaze = value)
                "beautyGlobal"     -> result.copy(beautyGlobal = value)
                "skinSmooth"       -> result.copy(skinSmooth = value)
                "faceThin"         -> result.copy(faceThin = value)
                "eyeEnlarge"       -> result.copy(eyeEnlarge = value)
                "skinWhiten"       -> result.copy(skinWhiten = value)
                "skinRedden"       -> result.copy(skinRedden = value)
                "chinAdjust"       -> result.copy(chinAdjust = value)
                "noseBridge"       -> result.copy(noseBridge = value)
                else               -> result
            }
        }
        return result
    }
}
