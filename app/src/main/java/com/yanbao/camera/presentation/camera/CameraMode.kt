package com.yanbao.camera.presentation.camera

import com.yanbao.camera.R

enum class CameraMode(
    val displayName: String,
    val iconRes: Int, // 需要库洛米风格图标资源
    val requires29DPanel: Boolean = false,
    val requiresMasterWheel: Boolean = false,
    val requiresNativeControls: Boolean = false
) {
    MEMORY("雁宝记忆", R.drawable.ic_mode_memory, requires29DPanel = false),
    MASTER("大师", R.drawable.ic_mode_master, requiresMasterWheel = true),
    PARAM29D("29D", R.drawable.ic_mode_29d, requires29DPanel = true),
    PARALLAX("2.9D", R.drawable.ic_mode_parallax),
    BEAUTY("美颜", R.drawable.ic_mode_beauty),
    VIDEO("视频大师", R.drawable.ic_mode_video),
    BASIC("基本", R.drawable.ic_mode_basic),
    NATIVE("原相机", R.drawable.ic_mode_native, requiresNativeControls = true),
    AR("AR空间", R.drawable.ic_mode_ar)
}
