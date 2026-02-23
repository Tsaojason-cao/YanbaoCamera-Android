package com.yanbao.camera.render

/**
 * Phase 2: 完整 29D 参数定义
 *
 * 涵盖曝光(6)、色彩(8)、纹理(6)、美颜(8) 共 28 个可调参数，
 * 外加 1 个保留参数，合计 29D。
 *
 * 所有参数均归一化到 [0,1] 或 [-1,1] 区间，方便直接传入 GLSL uniform。
 */
data class Param29DFull(
    // ─── 曝光 (6) ────────────────────────────────────────────────────────
    var iso: Int = 400,                    // 100-6400
    var shutterSpeed: Float = 1 / 125f,    // 1/8000-30s 归一化存储
    var ev: Float = 0f,                    // -3..+3
    var dynamicRange: Float = 0.5f,        // 0-1
    var shadowComp: Float = 0.3f,          // 0-1
    var highlightProtect: Float = 0.3f,    // 0-1

    // ─── 色彩 (8) ────────────────────────────────────────────────────────
    var colorTemp: Float = 0.5f,           // 2000-10000K -> 0-1
    var tint: Float = 0f,                  // -1..1
    var saturation: Float = 1f,            // 0-2
    var skinTone: Float = 0.5f,            // 0-1
    var redGain: Float = 1f,
    var greenGain: Float = 1f,
    var blueGain: Float = 1f,
    var colorBoost: Float = 0f,            // 0-1

    // ─── 纹理 (6) ────────────────────────────────────────────────────────
    var sharpness: Float = 0.5f,           // 0-1
    var denoise: Float = 0.3f,
    var grain: Float = 0f,
    var vignette: Float = 0f,
    var clarity: Float = 0f,               // -1..1
    var dehaze: Float = 0f,

    // ─── 美颜 (8) ────────────────────────────────────────────────────────
    var beautyGlobal: Float = 0.5f,
    var skinSmooth: Float = 0.6f,
    var faceThin: Float = 0.3f,
    var eyeEnlarge: Float = 0.2f,
    var skinWhiten: Float = 0.4f,
    var skinRedden: Float = 0.2f,
    var chinAdjust: Float = 0f,            // -1..1
    var noseBridge: Float = 0f,            // -1..1
) {
    /**
     * 将所有参数序列化为 28 维 FloatArray，直接对应 GLSL uniform float uParams[28]。
     */
    fun toFloatArray(): FloatArray = floatArrayOf(
        iso / 6400f, shutterSpeed, ev / 3f, dynamicRange, shadowComp, highlightProtect,
        colorTemp, tint, saturation / 2f, skinTone, redGain, greenGain, blueGain, colorBoost,
        sharpness, denoise, grain, vignette, clarity, dehaze,
        beautyGlobal, skinSmooth, faceThin, eyeEnlarge, skinWhiten, skinRedden,
        chinAdjust, noseBridge
    )

    companion object {
        /** 默认参数（直出模式，不做任何处理） */
        val DEFAULT = Param29DFull()

        /** 人像模式（美颜增强） */
        val PORTRAIT = Param29DFull(
            skinSmooth = 0.7f,
            skinWhiten = 0.5f,
            faceThin = 0.4f,
            eyeEnlarge = 0.3f,
            beautyGlobal = 0.6f
        )

        /** 风景模式（色彩增强） */
        val LANDSCAPE = Param29DFull(
            saturation = 1.3f,
            clarity = 0.3f,
            sharpness = 0.7f,
            dehaze = 0.2f,
            colorBoost = 0.3f
        )
    }
}
