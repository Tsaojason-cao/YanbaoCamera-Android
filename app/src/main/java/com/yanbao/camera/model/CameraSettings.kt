package com.yanbao.camera.model

/**
 * 相机设置参数
 */
data class CameraSettings(
    // 基础设置
    val isFrontCamera: Boolean = false,
    val flashMode: FlashMode = FlashMode.AUTO,
    
    // 专业模式参数
    val iso: Int = 100,
    val shutterSpeed: Float = 1f / 100f,  // 秒数
    val whiteBalance: WhiteBalance = WhiteBalance.AUTO,
    val exposureCompensation: Float = 0f,  // -3 to +3 EV
    val focusMode: FocusMode = FocusMode.AUTO,
    
    // 编辑参数
    val brightness: Float = 0f,  // -100 to +100
    val contrast: Float = 0f,    // -100 to +100
    val saturation: Float = 0f,  // -100 to +100
    val hue: Float = 0f,         // -180 to +180
    
    // 滤镜
    val filterName: String = "Original",
    val filterIntensity: Float = 1f  // 0 to 1
)

/**
 * 闪光灯模式
 */
enum class FlashMode {
    AUTO,
    ON,
    OFF
}

/**
 * 白平衡模式
 */
enum class WhiteBalance {
    AUTO,
    DAYLIGHT,
    CLOUDY,
    TUNGSTEN,
    FLUORESCENT
}

/**
 * 对焦模式
 */
enum class FocusMode {
    AUTO,
    MANUAL,
    MACRO,
    LANDSCAPE
}

/**
 * 滤镜类型
 */
enum class FilterType {
    ORIGINAL,
    VINTAGE,
    BLACK_WHITE,
    SEPIA,
    VIVID,
    COOL,
    WARM,
    FADE,
    CROSS_PROCESS,
    POSTERIZE,
    SKETCH,
    BLUR,
    SHARPEN,
    EMBOSS,
    EDGE_DETECT,
    PIXELATE,
    TOON,
    CARTOON,
    INVERT,
    SOLARIZE
}
