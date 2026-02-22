package com.yanbao.camera.presentation.lbs

/**
 * LBS 推荐模块数据模型
 * 不依赖 Google Maps SDK，使用简单坐标对
 */
data class LatLngSimple(
    val latitude: Double,
    val longitude: Double
)

data class LocationItem(
    val id: String,
    val name: String,
    val latLng: LatLngSimple,
    val rating: Float,           // 0-5
    val distance: String,        // 如 "1.2 km"
    val thumbnailUrl: String,    // 缩略图路径或资源ID
    val filterSuggestion: String // 推荐滤镜名称
)
