package com.yanbao.camera.presentation.camera

/**
 * 雁宝相机 9 大拍摄模式
 * 注意：此枚举与 core.model.CameraMode 不同，专用于 UI 层模式切换
 */
enum class YanbaoCameraMode(
    val displayName: String,
    val emoji: String
) {
    MEMORY("雁宝记忆", "M"),
    MASTER("大师", "A"),
    PARAM29D("29D", "D"),
    PARALLAX("2.9D", "X"),
    BEAUTY("美颜", "B"),
    VIDEO("视频大师", "V"),
    BASIC("基本", "B"),
    NATIVE("原相机", "N"),
    AR("AR空间", "R")
}
