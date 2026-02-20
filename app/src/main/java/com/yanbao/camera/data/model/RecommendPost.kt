package com.yanbao.camera.data.model

/**
 * 推荐帖子数据模型
 */
data class RecommendPost(
    val id: String,
    val userName: String,
    val isVerified: Boolean = false,
    val location: String = "",
    val timeAgo: String = "",
    val description: String,
    val likeCount: String,
    val commentCount: String,
    val shareCount: String,
    val isLiked: Boolean = false,
    val placeholderColorStart: Int = 0xFFA78BFA.toInt(),
    val placeholderColorEnd: Int = 0xFFEC4899.toInt()
)

/**
 * 相册照片数据模型
 */
data class GalleryPhoto(
    val id: Long,
    val uri: String,
    val name: String,
    val dateAdded: Long,
    val width: Int = 0,
    val height: Int = 0,
    val size: Long = 0
)

/**
 * 相册分组
 */
data class GalleryAlbum(
    val id: String,
    val name: String,
    val coverUri: String?,
    val photoCount: Int
)

/**
 * 编辑工具（18个工具，按设计图工具栏顺序）
 */
data class EditTool(
    val id: String,
    val name: String,
    val icon: String,
    val minValue: Float = -1f,
    val maxValue: Float = 1f,
    val defaultValue: Float = 0f
)

/**
 * 18个编辑工具完整列表
 */
val editTools = listOf(
    EditTool("brightness",  "亮度",   "☀️"),
    EditTool("contrast",    "对比度", "◑"),
    EditTool("saturation",  "饱和度", "💧"),
    EditTool("ai_enhance",  "AI增强", "✨", 0f, 1f, 0f),
    EditTool("crop",        "裁剪",   "✂️", 0f, 1f, 0f),
    EditTool("text",        "文字",   "Tp", 0f, 1f, 0f),
    EditTool("sticker",     "贴纸",   "😊", 0f, 1f, 0f),
    EditTool("blur",        "模糊",   "🌀", 0f, 1f, 0f),
    EditTool("sharpen",     "锐化",   "🔪", 0f, 1f, 0f),
    EditTool("temperature", "色温",   "🌡️"),
    EditTool("tint",        "色调",   "🎨"),
    EditTool("highlights",  "高光",   "⬜"),
    EditTool("shadows",     "阴影",   "⬛"),
    EditTool("vignette",    "暗角",   "🔲", 0f, 1f, 0f),
    EditTool("grain",       "颗粒",   "🌾", 0f, 1f, 0f),
    EditTool("fade",        "褪色",   "🌫️", 0f, 1f, 0f),
    EditTool("rotate",      "旋转",   "🔄", 0f, 360f, 0f),
    EditTool("flip",        "翻转",   "↔️", 0f, 1f, 0f)
)

/**
 * 大师滤镜地区分类
 */
enum class FilterRegion(val displayName: String) {
    CN("CN"),
    TW("TW"),
    JP("JP"),
    KR("KR"),
    US("US"),
    EU("EU")
}

/**
 * 大师滤镜数据模型（29维参数）
 */
data class MasterFilter(
    val id: String,
    val name: String,
    val region: FilterRegion,
    // 29维参数（对应GLSL着色器uniforms）
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val whites: Float = 0f,
    val blacks: Float = 0f,
    val clarity: Float = 0f,
    val vibrance: Float = 0f,
    val hueRed: Float = 0f,
    val hueOrange: Float = 0f,
    val hueYellow: Float = 0f,
    val hueGreen: Float = 0f,
    val hueAqua: Float = 0f,
    val hueBlue: Float = 0f,
    val huePurple: Float = 0f,
    val hueMagenta: Float = 0f,
    val satRed: Float = 0f,
    val satOrange: Float = 0f,
    val satYellow: Float = 0f,
    val satGreen: Float = 0f,
    val satAqua: Float = 0f,
    val satBlue: Float = 0f,
    val satPurple: Float = 0f,
    val satMagenta: Float = 0f,
    val lumRed: Float = 0f,
    val lumOrange: Float = 0f
)
