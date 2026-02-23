package com.yanbao.camera.presentation.camera

data class Param29D(
    // 曝光 (6)
    var iso: Int = 400,
    var shutterSpeed: String = "125", // 存储为分母，如125表示1/125s
    var ev: Float = 0f,
    var dynamicRange: Int = 50,
    var shadowComp: Int = 30,
    var highlightProtect: Int = 30,
    // 色彩 (8)
    var colorTemp: Int = 5500,
    var tint: Int = 0,
    var saturation: Int = 100,
    var skinTone: Int = 50,
    var redGain: Float = 1.0f,
    var greenGain: Float = 1.0f,
    var blueGain: Float = 1.0f,
    var colorBoost: Int = 0,
    // 纹理 (6)
    var sharpness: Int = 50,
    var denoise: Int = 30,
    var grain: Int = 0,
    var vignette: Int = 0,
    var clarity: Int = 0,
    var dehaze: Int = 0,
    // 美颜 (8)
    var beautyGlobal: Int = 50,
    var skinSmooth: Int = 60,
    var faceThin: Int = 30,
    var eyeEnlarge: Int = 20,
    var skinWhiten: Int = 40,
    var skinRedden: Int = 20,
    var chinAdjust: Int = 0,
    var noseBridge: Int = 0
) {
    fun toFloatArray(): FloatArray = floatArrayOf(
        iso / 6400f, shutterSpeed.toFloat() / 8000f, ev / 3f,
        dynamicRange / 100f, shadowComp / 100f, highlightProtect / 100f,
        (colorTemp - 2000f) / 8000f, tint / 100f, saturation / 200f,
        skinTone / 100f, redGain, greenGain, blueGain, colorBoost / 100f,
        sharpness / 100f, denoise / 100f, grain / 100f, vignette / 100f,
        clarity / 100f, dehaze / 100f,
        beautyGlobal / 100f, skinSmooth / 100f, faceThin / 100f, eyeEnlarge / 100f,
        skinWhiten / 100f, skinRedden / 100f, chinAdjust / 100f, noseBridge / 100f
    )
}
