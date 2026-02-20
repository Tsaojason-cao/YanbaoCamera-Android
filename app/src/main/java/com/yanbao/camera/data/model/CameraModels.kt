package com.yanbao.camera.data.model

/**
 * 7种拍摄模式（设计文档规格）
 * 对应相机底部模式栏：NORMAL | BEAUTY | 2.9D | AR | IPHONE | MASTER | MEMORY | VIDEO
 */
enum class CameraMode(val displayName: String, val englishName: String) {
    NORMAL("普通", "NORMAL"),
    BEAUTY("美颜", "BEAUTY"),
    MODE_29D("2.9D", "2.9D"),
    AR("AR", "AR"),
    IPHONE("原相机", "IPHONE"),
    MASTER("大师", "MASTER"),
    MEMORY("记忆", "MEMORY"),
    VIDEO("录像", "VIDEO")
}

/**
 * 闪光灯模式
 */
enum class FlashMode(val displayName: String, val icon: String) {
    OFF("关闭", "⚡"),
    AUTO("自动", "⚡A"),
    ON("开启", "⚡"),
    TORCH("手电筒", "🔦")
}

/**
 * 网格线类型
 */
enum class GridType {
    NONE, THREE_BY_THREE, GOLDEN_RATIO, SQUARE
}

/**
 * 宽高比
 */
enum class AspectRatio(val displayName: String, val ratio: Float) {
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_1_1("1:1", 1f),
    RATIO_FULL("全屏", 0f)
}

/**
 * 相机UI状态
 */
data class CameraUiState(
    val currentMode: CameraMode = CameraMode.NORMAL,
    val flashMode: FlashMode = FlashMode.AUTO,
    val isFrontCamera: Boolean = false,
    val zoomRatio: Float = 1.0f,
    val isRecording: Boolean = false,
    val recordingDuration: Long = 0L,
    val gridType: GridType = GridType.THREE_BY_THREE,
    val aspectRatio: AspectRatio = AspectRatio.RATIO_4_3,
    val isCapturing: Boolean = false,
    val lastPhotoUri: String? = null,
    val errorMessage: String? = null
)
