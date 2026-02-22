package com.yanbao.camera.presentation.camera

/**
 * 雁宝相机 9 大拍摄模式
 * 注意：此枚举与 core.model.CameraMode 不同，专用于 UI 层模式切换
 */
enum class YanbaoCameraMode(
    val displayName: String,
    val emoji: String
) {
    MEMORY("雁宝记忆", "🧠"),
    MASTER("大师", "🎨"),
    PARAM29D("29D", "🎛"),
    PARALLAX("2.9D", "🌀"),
    BEAUTY("美颜", "✨"),
    VIDEO("视频大师", "🎬"),
    BASIC("基本", "📷"),
    NATIVE("原相机", "📸"),
    AR("AR空间", "🌐")
}
