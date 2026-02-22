package com.yanbao.camera.presentation.camera

import kotlinx.serialization.Serializable

/**
 * 29D 参数状态类 - 存储当前所有参数值
 * 用于 OpenGL Shader uniform float u_Params[29] 下发
 * 与 Param29D（描述符）不同，此类存储实际值
 */
@Serializable
data class Param29DState(
    // Tab1 曝光
    val iso: Int = 400,
    val shutterSpeed: String = "125",   // 1/125s 存储为倒数
    val ev: Float = 0f,
    val dynamicRange: Int = 50,
    val shadowComp: Int = 30,
    val highlightProtect: Int = 30,
    // Tab2 色彩
    val colorTemp: Int = 5500,
    val tint: Int = 0,
    val saturation: Int = 100,
    val skinTone: Int = 50,
    val redGain: Float = 1.0f,
    val greenGain: Float = 1.0f,
    val blueGain: Float = 1.0f,
    val colorBoost: Int = 0,
    // Tab3 纹理
    val sharpness: Int = 50,
    val denoise: Int = 30,
    val grain: Int = 0,
    val vignette: Int = 0,
    val clarity: Int = 0,
    val dehaze: Int = 0,
    // Tab4 美颜
    val beautyGlobal: Int = 50,
    val skinSmooth: Int = 60,
    val faceThin: Int = 30,
    val eyeEnlarge: Int = 20,
    val skinWhiten: Int = 40,
    val skinRedden: Int = 20,
    val chinAdjust: Int = 0,
    val noseBridge: Int = 0
) {
    /**
     * 转换为 FloatArray，用于 OpenGL Shader uniform float u_Params[29]
     * 对应 Yanbao29DShader.glsl 中的 u_Params 数组
     */
    fun toFloatArray(): FloatArray = floatArrayOf(
        iso.toFloat(),                          // [0]  ISO
        shutterSpeed.toFloatOrNull() ?: 125f,   // [1]  快门倒数
        ev,                                      // [2]  EV
        dynamicRange.toFloat(),                  // [3]  动态范围
        shadowComp.toFloat(),                    // [4]  阴影补偿
        highlightProtect.toFloat(),              // [5]  高光抑制
        colorTemp.toFloat(),                     // [6]  色温
        tint.toFloat(),                          // [7]  色调
        saturation.toFloat(),                    // [8]  饱和度
        skinTone.toFloat(),                      // [9]  肤色
        redGain,                                 // [10] 红色增益
        greenGain,                               // [11] 绿色增益
        blueGain,                                // [12] 蓝色增益
        colorBoost.toFloat(),                    // [13] 色彩浓度
        sharpness.toFloat(),                     // [14] 锐度
        denoise.toFloat(),                       // [15] 降噪
        grain.toFloat(),                         // [16] 颗粒
        vignette.toFloat(),                      // [17] 暗角
        clarity.toFloat(),                       // [18] 清晰度
        dehaze.toFloat(),                        // [19] 去雾
        beautyGlobal.toFloat(),                  // [20] 全局美颜
        skinSmooth.toFloat(),                    // [21] 磨皮
        faceThin.toFloat(),                      // [22] 瘦脸
        eyeEnlarge.toFloat(),                    // [23] 大眼
        skinWhiten.toFloat(),                    // [24] 美白
        skinRedden.toFloat(),                    // [25] 红润
        chinAdjust.toFloat(),                    // [26] 下巴
        noseBridge.toFloat(),                    // [27] 鼻梁
        0f                                       // [28] 预留
    )
}
