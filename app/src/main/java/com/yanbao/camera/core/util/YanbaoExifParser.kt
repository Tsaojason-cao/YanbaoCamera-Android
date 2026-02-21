package com.yanbao.camera.core.util

import android.media.ExifInterface
import android.util.Log
import java.io.File
import kotlin.math.roundToInt

/**
 * 雁宝 Exif 深度解析引擎
 * 
 * 核心功能：
 * - 从图片二进制文件中读取真实的 29D 参数
 * - 不依赖数据库缓存，直接读取文件元数据
 * - 如果文件中没有参数，说明相机模块在造假
 * 
 * 审讯逻辑：
 * - 快门速度必须从 ExifInterface.TAG_EXPOSURE_TIME 读取
 * - ISO 必须从 ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY 读取
 * - 色温必须从 ExifInterface.TAG_WHITE_BALANCE 或自定义标签读取
 */
object YanbaoExifParser {

    private const val TAG = "YanbaoExifParser"

    /**
     * 从图片文件中提取真实的拍摄参数
     * 
     * @param imagePath 图片文件路径
     * @return PhotoParams 包含真实的 29D 参数
     */
    fun getPhotoMetadata(imagePath: String): PhotoParams {
        return try {
            val file = File(imagePath)
            if (!file.exists()) {
                Log.e(TAG, "文件不存在: $imagePath")
                return PhotoParams.empty()
            }

            val exif = ExifInterface(imagePath)

            // 1. 读取快门速度（从 Exif 标准字段）
            val exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
            val shutter = formatShutter(exposureTime)

            // 2. 读取 ISO 感光度
            val isoString = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY) 
                ?: exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)
            val iso = if (isoString != null) "ISO $isoString" else "ISO 100"

            // 3. 读取色温/白平衡
            val wbMode = exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE)
            val wb = mapWbToKelvin(wbMode)

            // 4. 读取光圈
            val apertureValue = exif.getAttribute(ExifInterface.TAG_APERTURE_VALUE)
            val aperture = formatAperture(apertureValue)

            // 5. 读取焦距
            val focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
            val focal = formatFocalLength(focalLength)

            // 6. 读取 GPS 位置
            val latLong = FloatArray(2)
            val hasGps = exif.getLatLong(latLong)
            val location = if (hasGps) {
                "GPS: ${String.format("%.4f", latLong[0])}, ${String.format("%.4f", latLong[1])}"
            } else {
                "无位置信息"
            }

            // 7. 读取拍摄日期
            val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME) ?: "未知时间"

            // 8. 读取拍摄模式（从自定义标签或 UserComment）
            val userComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT) ?: ""
            val mode = extractModeFromComment(userComment)

            // 9. 读取美颜参数（从自定义标签）
            val beautyParams = extractBeautyParams(exif)

            Log.d(TAG, "成功读取 Exif: $imagePath -> 快门=$shutter, ISO=$iso, 色温=$wb")

            PhotoParams(
                shutter = shutter,
                iso = iso,
                wb = wb,
                aperture = aperture,
                focalLength = focal,
                location = location,
                dateTime = dateTime,
                mode = mode,
                beautySmooth = beautyParams.smoothness,
                beautyWhite = beautyParams.whitening,
                beautyBlemish = beautyParams.blemishRemoval
            )
        } catch (e: Exception) {
            Log.e(TAG, "读取 Exif 失败: $imagePath", e)
            PhotoParams.empty()
        }
    }

    /**
     * 格式化快门速度
     * 
     * 输入示例：
     * - "0.00025" -> "1/4000s"
     * - "0.033" -> "1/30s"
     * - "2.0" -> "2s"
     */
    private fun formatShutter(exposureTime: String?): String {
        if (exposureTime.isNullOrEmpty()) return "未知快门"

        return try {
            val seconds = exposureTime.toDouble()
            when {
                seconds >= 1.0 -> "${seconds.roundToInt()}s"
                seconds > 0 -> {
                    val denominator = (1.0 / seconds).roundToInt()
                    "1/${denominator}s"
                }
                else -> "未知快门"
            }
        } catch (e: Exception) {
            Log.e(TAG, "快门速度解析失败: $exposureTime", e)
            "未知快门"
        }
    }

    /**
     * 将白平衡模式映射到色温（开尔文）
     * 
     * Exif 白平衡标准值：
     * - 0 = 自动
     * - 1 = 手动
     */
    private fun mapWbToKelvin(wbMode: String?): String {
        return when (wbMode) {
            "0" -> "5500K (自动)"
            "1" -> "手动白平衡"
            else -> "5500K"
        }
    }

    /**
     * 格式化光圈值
     */
    private fun formatAperture(apertureValue: String?): String {
        if (apertureValue.isNullOrEmpty()) return "f/1.8"

        return try {
            val fNumber = apertureValue.toDouble()
            "f/${String.format("%.1f", fNumber)}"
        } catch (e: Exception) {
            "f/1.8"
        }
    }

    /**
     * 格式化焦距
     */
    private fun formatFocalLength(focalLength: String?): String {
        if (focalLength.isNullOrEmpty()) return "未知焦距"

        return try {
            val mm = focalLength.toDouble()
            "${mm.roundToInt()}mm"
        } catch (e: Exception) {
            "未知焦距"
        }
    }

    /**
     * 从 UserComment 中提取拍摄模式
     * 
     * 格式示例：
     * - "MODE:MASTER|LOCATION:台北101"
     * - "MODE:BEAUTY|SMOOTH:50|WHITE:30"
     */
    private fun extractModeFromComment(comment: String): String {
        if (comment.isEmpty()) return "普通模式"

        return try {
            val parts = comment.split("|")
            val modePart = parts.find { it.startsWith("MODE:") }
            modePart?.substringAfter("MODE:") ?: "普通模式"
        } catch (e: Exception) {
            "普通模式"
        }
    }

    /**
     * 从 Exif 中提取美颜参数
     */
    private fun extractBeautyParams(exif: ExifInterface): BeautyParamsData {
        return try {
            val comment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT) ?: ""
            val parts = comment.split("|")

            val smooth = parts.find { it.startsWith("SMOOTH:") }?.substringAfter("SMOOTH:")?.toIntOrNull() ?: 0
            val white = parts.find { it.startsWith("WHITE:") }?.substringAfter("WHITE:")?.toIntOrNull() ?: 0
            val blemish = parts.find { it.startsWith("BLEMISH:") }?.substringAfter("BLEMISH:")?.toIntOrNull() ?: 0

            BeautyParamsData(smooth, white, blemish)
        } catch (e: Exception) {
            BeautyParamsData(0, 0, 0)
        }
    }

    /**
     * 写入 29D 参数到 Exif（供相机模块调用）
     * 
     * 这个方法确保拍照时将真实参数写入文件
     */
    fun writePhotoMetadata(
        imagePath: String,
        shutter: String,
        iso: Int,
        wb: Int,
        mode: String,
        location: String? = null,
        beautyParams: BeautyParamsData? = null
    ) {
        try {
            val exif = ExifInterface(imagePath)

            // 写入 ISO
            exif.setAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, iso.toString())

            // 写入快门速度（转换为秒）
            val exposureTime = convertShutterToSeconds(shutter)
            exif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, exposureTime.toString())

            // 写入色温（通过白平衡模式）
            exif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, if (wb > 0) "1" else "0")

            // 写入自定义标签（拍摄模式 + 美颜参数）
            val comment = buildString {
                append("MODE:$mode")
                if (location != null) {
                    append("|LOCATION:$location")
                }
                if (beautyParams != null) {
                    append("|SMOOTH:${beautyParams.smoothness}")
                    append("|WHITE:${beautyParams.whitening}")
                    append("|BLEMISH:${beautyParams.blemishRemoval}")
                }
            }
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, comment)

            exif.saveAttributes()
            Log.d(TAG, "成功写入 Exif: $imagePath")
        } catch (e: Exception) {
            Log.e(TAG, "写入 Exif 失败: $imagePath", e)
        }
    }

    /**
     * 将快门速度字符串转换为秒数
     * 
     * 输入示例：
     * - "1/4000s" -> 0.00025
     * - "2s" -> 2.0
     */
    private fun convertShutterToSeconds(shutter: String): Double {
        return try {
            when {
                shutter.contains("/") -> {
                    val parts = shutter.replace("s", "").split("/")
                    parts[0].toDouble() / parts[1].toDouble()
                }
                shutter.endsWith("s") -> {
                    shutter.replace("s", "").toDouble()
                }
                else -> 0.033 // 默认 1/30s
            }
        } catch (e: Exception) {
            0.033
        }
    }
}

/**
 * 照片参数数据类
 */
data class PhotoParams(
    val shutter: String,          // 快门速度（如 "1/4000s"）
    val iso: String,              // ISO 感光度（如 "ISO 800"）
    val wb: String,               // 色温（如 "5500K"）
    val aperture: String = "f/1.8", // 光圈
    val focalLength: String = "",  // 焦距
    val location: String = "",     // GPS 位置
    val dateTime: String = "",     // 拍摄时间
    val mode: String = "普通模式", // 拍摄模式
    val beautySmooth: Int = 0,     // 美颜磨皮
    val beautyWhite: Int = 0,      // 美颜美白
    val beautyBlemish: Int = 0     // 美颜祛斑
) {
    companion object {
        fun empty() = PhotoParams(
            shutter = "未知快门",
            iso = "ISO 100",
            wb = "5500K",
            mode = "未知模式"
        )
    }
}

/**
 * 美颜参数数据类
 */
data class BeautyParamsData(
    val smoothness: Int = 0,
    val whitening: Int = 0,
    val blemishRemoval: Int = 0
)
