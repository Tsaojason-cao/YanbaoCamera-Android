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
    EditTool("crop", "裁剪", R.drawable.ic_tool_crop, ToolCategory.CROP),
    EditTool("rotate", "旋转", R.drawable.ic_tool_rotate, ToolCategory.CROP),
    EditTool("flip", "翻转", R.drawable.ic_tool_flip, ToolCategory.CROP),
    EditTool("perspective", "透视", R.drawable.ic_tool_perspective, ToolCategory.CROP),
    // 调整
    EditTool("brightness", "亮度", R.drawable.ic_tool_brightness, ToolCategory.ADJUST),
    EditTool("contrast", "对比度", R.drawable.ic_tool_contrast, ToolCategory.ADJUST),
    EditTool("saturation", "饱和度", R.drawable.ic_tool_saturation, ToolCategory.ADJUST),
    EditTool("temp", "色温", R.drawable.ic_tool_temp, ToolCategory.ADJUST),
    EditTool("tint", "色调", R.drawable.ic_tool_tint, ToolCategory.ADJUST),
    EditTool("sharpness", "清晰度", R.drawable.ic_tool_sharpness, ToolCategory.ADJUST),
    EditTool("denoise", "降噪", R.drawable.ic_tool_denoise, ToolCategory.ADJUST),
    EditTool("grain", "颗粒", R.drawable.ic_tool_grain, ToolCategory.ADJUST),
    EditTool("vignette", "暗角", R.drawable.ic_tool_vignette, ToolCategory.ADJUST),
    // 滤镜
    EditTool("master", "大师", R.drawable.ic_tool_master, ToolCategory.FILTER),
    EditTool("29d", "29D", R.drawable.ic_tool_29d, ToolCategory.FILTER),
    EditTool("parallax", "2.9D", R.drawable.ic_tool_parallax, ToolCategory.FILTER),
    // 美颜
    EditTool("beauty", "一键美颜", R.drawable.ic_tool_beauty, ToolCategory.BEAUTY),
    EditTool("skin", "皮肤", R.drawable.ic_tool_skin, ToolCategory.BEAUTY),
    EditTool("face", "瘦脸", R.drawable.ic_tool_face, ToolCategory.BEAUTY),
    EditTool("eye", "大眼", R.drawable.ic_tool_eye, ToolCategory.BEAUTY),
    EditTool("whiten", "美白", R.drawable.ic_tool_whiten, ToolCategory.BEAUTY),
    EditTool("redden", "红润", R.drawable.ic_tool_redden, ToolCategory.BEAUTY),
    // 创意
    EditTool("text", "文字", R.drawable.ic_tool_text, ToolCategory.CREATIVE),
    EditTool("sticker", "贴纸", R.drawable.ic_tool_sticker, ToolCategory.CREATIVE),
    EditTool("draw", "涂鸦", R.drawable.ic_tool_draw, ToolCategory.CREATIVE),
    EditTool("mosaic", "马赛克", R.drawable.ic_tool_mosaic, ToolCategory.CREATIVE),
    // 高级
    EditTool("curve", "曲线", R.drawable.ic_tool_curve, ToolCategory.ADVANCED),
    EditTool("hsl", "HSL", R.drawable.ic_tool_hsl, ToolCategory.ADVANCED),
    EditTool("local", "局部", R.drawable.ic_tool_local, ToolCategory.ADVANCED),
    EditTool("ai", "AI增强", R.drawable.ic_tool_ai, ToolCategory.ADVANCED)
)
