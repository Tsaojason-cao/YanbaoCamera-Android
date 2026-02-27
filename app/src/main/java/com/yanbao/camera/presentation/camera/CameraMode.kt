package com.yanbao.camera.presentation.camera

import com.yanbao.camera.R

/**
 * 摄颜相机模式枚举 — 严格对应 CAM_01~CAM_09 设计稿顺序
 *
 * 模式顺序（从左到右，与设计图 ModeSelectorRow 完全一致）：
 * 原相机手动 / 大师模式 / 29D渲染 / 相机基本 / 美颜塑形 / 视频大师 / 雁宝记忆 / AR空间 / 2.9D视差
 */
enum class CameraMode(
    val displayName: String,
    val iconRes: Int,
    val requires29DPanel: Boolean = false,
    val requiresMasterWheel: Boolean = false,
    val requiresNativeControls: Boolean = false
) {
    NATIVE("原相机手动", R.drawable.ic_mode_native, requiresNativeControls = true),
    MASTER("大师模式", R.drawable.ic_mode_master, requiresMasterWheel = true),
    PARAM29D("29D渲染", R.drawable.ic_mode_29d, requires29DPanel = true),
    BASIC("相机基本", R.drawable.ic_mode_basic),
    BEAUTY("美颜塑形", R.drawable.ic_mode_beauty),
    VIDEO("视频大师", R.drawable.ic_mode_video),
    MEMORY("雁宝记忆", R.drawable.ic_mode_memory),
    AR("AR空间", R.drawable.ic_mode_ar),
    PARALLAX("2.9D视差", R.drawable.ic_mode_parallax)
}
