package com.yanbao.camera.presentation.gallery

import androidx.compose.runtime.Immutable

@Immutable
data class GalleryPhoto(
    val id: String,
    val url: String,
    val isMemory: Boolean = false,
    val exif: ExifData? = null
)

data class ExifData(
    val iso: String = "400",
    val shutter: String = "1/125s",
    val focal: String = "26mm",
    val colorTemp: String = "5500K"
)

data class MemoryItem(
    val id: String,
    val thumbnailUrl: String,
    val location: String,
    val params: String,
    val fullParams: ExifData
)
