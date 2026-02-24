package com.yanbao.camera.presentation.camera

import com.yanbao.camera.R

enum class CameraMode(
    val displayName: String,
    val iconRes: Int // 暂时用同一个占位图标，后续替换
) {
    MEMORY("雁宝记忆", R.drawable.ic_mode_memory),
    MASTER("大师", R.drawable.ic_mode_master),
    PARAM29D("29D", R.drawable.ic_mode_29d),
    PARALLAX("2.9D", R.drawable.ic_mode_parallax),
    BEAUTY("美颜", R.drawable.ic_mode_beauty),
    VIDEO("视频大师", R.drawable.ic_mode_video),
    BASIC("基本", R.drawable.ic_mode_basic),
    NATIVE("原相机", R.drawable.ic_mode_native),
    AR("AR空间", R.drawable.ic_mode_ar)
}
