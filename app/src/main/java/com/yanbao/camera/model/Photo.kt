package com.yanbao.camera.model

import java.util.Date

/**
 * 照片数据模型
 */
data class Photo(
    val id: String,
    val uri: String,
    val timestamp: Long = System.currentTimeMillis(),
    val width: Int = 0,
    val height: Int = 0,
    val size: Long = 0,
    val mimeType: String = "image/jpeg",
    val exif: PhotoExif = PhotoExif()
)

/**
 * 照片EXIF信息
 */
data class PhotoExif(
    val camera: String = "Unknown",
    val iso: Int = 0,
    val shutterSpeed: String = "",
    val aperture: String = "",
    val focalLength: String = "",
    val dateTime: String = "",
    val gps: String = ""
)
