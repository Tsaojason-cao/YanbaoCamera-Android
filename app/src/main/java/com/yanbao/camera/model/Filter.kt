package com.yanbao.camera.model

/**
 * 滤镜数据模型
 * 
 * 支持20+实时滤镜
 */
data class Filter(
    val id: String,
    val name: String,
    val description: String,
    val intensity: Float = 1.0f,  // 0.0 - 1.0
    val category: FilterCategory = FilterCategory.BASIC,
    val icon: String = "",
    val isSelected: Boolean = false
)

/**
 * 滤镜分类
 */
enum class FilterCategory {
    BASIC,      // 基础滤镜
    VINTAGE,    // 复古滤镜
    CINEMATIC,  // 电影滤镜
    ARTISTIC,   // 艺术滤镜
    PROFESSIONAL // 专业滤镜
}

/**
 * 滤镜预设
 */
object FilterPresets {
    val filters = listOf(
        // 基础滤镜
        Filter("original", "原图", "无滤镜", category = FilterCategory.BASIC),
        Filter("grayscale", "黑白", "经典黑白效果", category = FilterCategory.BASIC),
        Filter("sepia", "褐色", "怀旧褐色效果", category = FilterCategory.BASIC),
        Filter("cool", "冷色", "冷色调效果", category = FilterCategory.BASIC),
        Filter("warm", "暖色", "暖色调效果", category = FilterCategory.BASIC),
        
        // 复古滤镜
        Filter("vintage_1", "复古1", "80年代复古风格", category = FilterCategory.VINTAGE),
        Filter("vintage_2", "复古2", "胶片复古风格", category = FilterCategory.VINTAGE),
        Filter("polaroid", "宝丽来", "宝丽来相纸效果", category = FilterCategory.VINTAGE),
        Filter("retro", "复古色", "复古色彩效果", category = FilterCategory.VINTAGE),
        Filter("nostalgia", "怀旧", "怀旧滤镜效果", category = FilterCategory.VINTAGE),
        
        // 电影滤镜
        Filter("cinema_1", "电影1", "好莱坞风格", category = FilterCategory.CINEMATIC),
        Filter("cinema_2", "电影2", "欧洲风格", category = FilterCategory.CINEMATIC),
        Filter("noir", "黑色电影", "黑色电影风格", category = FilterCategory.CINEMATIC),
        Filter("dramatic", "戏剧", "戏剧效果", category = FilterCategory.CINEMATIC),
        Filter("moody", "忧郁", "忧郁氛围", category = FilterCategory.CINEMATIC),
        
        // 艺术滤镜
        Filter("lomo", "LOMO", "LOMO相机风格", category = FilterCategory.ARTISTIC),
        Filter("sketch", "素描", "素描效果", category = FilterCategory.ARTISTIC),
        Filter("oil_painting", "油画", "油画效果", category = FilterCategory.ARTISTIC),
        Filter("watercolor", "水彩", "水彩效果", category = FilterCategory.ARTISTIC),
        Filter("neon", "霓虹", "霓虹灯效果", category = FilterCategory.ARTISTIC),
        
        // 专业滤镜
        Filter("portrait", "人像", "人像优化", category = FilterCategory.PROFESSIONAL),
        Filter("landscape", "风景", "风景优化", category = FilterCategory.PROFESSIONAL)
    )
}

/**
 * 相机参数数据模型
 */
data class CameraParameters(
    val iso: Int = 400,                    // ISO值 100-6400
    val shutterSpeed: Float = 1f / 125f,  // 快门速度
    val whiteBalance: WhiteBalance = WhiteBalance.AUTO,
    val exposureCompensation: Float = 0f, // -3.0 ~ +3.0
    val focusMode: FocusMode = FocusMode.AUTO,
    val zoomLevel: Float = 1.0f            // 缩放级别
)

// WhiteBalance and FocusMode are defined in CameraSettings.kt

/**
 * 拍照模式
 */
enum class CameraMode {
    AUTO,       // 自动模式
    PORTRAIT,   // 人像模式
    LANDSCAPE,  // 风景模式
    NIGHT,      // 夜景模式
    VIDEO       // 视频模式
}

/**
 * 编辑工具数据模型
 */
data class EditTool(
    val id: String,
    val name: String,
    val icon: String,
    val category: EditCategory
)

/**
 * 编辑工具分类
 */
enum class EditCategory {
    BASIC,      // 基础编辑（裁剪、旋转等）
    FILTER,     // 滤镜
    CURVE,      // 曲线
    HSL,        // HSL调节
    LOCAL,      // 局部调整
    HEALING     // 修复
}
