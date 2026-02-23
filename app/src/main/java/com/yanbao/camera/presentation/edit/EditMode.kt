package com.yanbao.camera.presentation.edit

import com.yanbao.camera.R

// 工具分类
enum class ToolCategory(val displayName: String) {
    CROP("裁剪"),
    ADJUST("调整"),
    FILTER("滤镜"),
    BEAUTY("美颜"),
    CREATIVE("创意"),
    ADVANCED("高级")
}

// 单个工具定义
data class EditTool(
    val id: String,
    val name: String,
    val iconRes: Int,
    val category: ToolCategory
)

// 预定义18种工具（按分类组织）
val editTools = listOf(
    // 裁剪
    EditTool("crop", "裁剪", R.drawable.ic_tool_crop_kuromi, ToolCategory.CROP),
    EditTool("rotate", "旋转", R.drawable.ic_tool_rotate_kuromi, ToolCategory.CROP),
    EditTool("flip", "翻转", R.drawable.ic_tool_flip_kuromi, ToolCategory.CROP),
    EditTool("perspective", "透视", R.drawable.ic_tool_perspective_kuromi, ToolCategory.CROP),
    // 调整
    EditTool("brightness", "亮度", R.drawable.ic_tool_brightness_kuromi, ToolCategory.ADJUST),
    EditTool("contrast", "对比度", R.drawable.ic_tool_contrast_kuromi, ToolCategory.ADJUST),
    EditTool("saturation", "饱和度", R.drawable.ic_tool_saturation_kuromi, ToolCategory.ADJUST),
    EditTool("temp", "色温", R.drawable.ic_tool_temp_kuromi, ToolCategory.ADJUST),
    EditTool("tint", "色调", R.drawable.ic_tool_tint_kuromi, ToolCategory.ADJUST),
    EditTool("sharpness", "清晰度", R.drawable.ic_tool_sharpness_kuromi, ToolCategory.ADJUST),
    EditTool("denoise", "降噪", R.drawable.ic_tool_denoise_kuromi, ToolCategory.ADJUST),
    EditTool("grain", "颗粒", R.drawable.ic_tool_grain_kuromi, ToolCategory.ADJUST),
    EditTool("vignette", "暗角", R.drawable.ic_tool_vignette_kuromi, ToolCategory.ADJUST),
    // 滤镜
    EditTool("master", "大师", R.drawable.ic_tool_master_kuromi, ToolCategory.FILTER),
    EditTool("29d", "29D", R.drawable.ic_tool_29d_kuromi, ToolCategory.FILTER),
    EditTool("parallax", "2.9D", R.drawable.ic_tool_parallax_kuromi, ToolCategory.FILTER),
    // 美颜
    EditTool("beauty", "一键美颜", R.drawable.ic_tool_beauty_kuromi, ToolCategory.BEAUTY),
    EditTool("skin", "皮肤", R.drawable.ic_tool_skin_kuromi, ToolCategory.BEAUTY),
    EditTool("face", "瘦脸", R.drawable.ic_tool_face_kuromi, ToolCategory.BEAUTY),
    EditTool("eye", "大眼", R.drawable.ic_tool_eye_kuromi, ToolCategory.BEAUTY),
    EditTool("whiten", "美白", R.drawable.ic_tool_whiten_kuromi, ToolCategory.BEAUTY),
    EditTool("redden", "红润", R.drawable.ic_tool_redden_kuromi, ToolCategory.BEAUTY),
    // 创意
    EditTool("text", "文字", R.drawable.ic_tool_text_kuromi, ToolCategory.CREATIVE),
    EditTool("sticker", "贴纸", R.drawable.ic_tool_sticker_kuromi, ToolCategory.CREATIVE),
    EditTool("draw", "涂鸦", R.drawable.ic_tool_draw_kuromi, ToolCategory.CREATIVE),
    EditTool("mosaic", "马赛克", R.drawable.ic_tool_mosaic_kuromi, ToolCategory.CREATIVE),
    // 高级
    EditTool("curve", "曲线", R.drawable.ic_tool_curve_kuromi, ToolCategory.ADVANCED),
    EditTool("hsl", "HSL", R.drawable.ic_tool_hsl_kuromi, ToolCategory.ADVANCED),
    EditTool("local", "局部", R.drawable.ic_tool_local_kuromi, ToolCategory.ADVANCED),
    EditTool("ai", "AI增强", R.drawable.ic_tool_ai_kuromi, ToolCategory.ADVANCED)
)
