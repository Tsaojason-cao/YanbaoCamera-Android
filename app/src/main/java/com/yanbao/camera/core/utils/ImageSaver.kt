package com.yanbao.camera.core.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 图片保存工具类
 * 支持Android 10+的MediaStore API
 */
object ImageSaver {
    private const val TAG = "ImageSaver"
    private const val ALBUM_NAME = "YanbaoAI"
    
    /**
     * 保存图片到相册
     * @param context Context
     * @param bitmap 要保存的图片
     * @param filterName 滤镜名称（可选）
     * @return 保存后的Uri，失败返回null
     */
    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        filterName: String? = null
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val displayName = if (filterName != null) {
                "YanbaoAI_${filterName}_$timestamp.jpg"
            } else {
                "YanbaoAI_$timestamp.jpg"
            }
            
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ 使用相对路径
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM_NAME")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            
            if (uri == null) {
                Log.e(TAG, "Failed to create new MediaStore record")
                return@withContext null
            }
            
            // 写入图片数据
            resolver.openOutputStream(uri)?.use { outputStream ->
                val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                if (!success) {
                    Log.e(TAG, "Failed to compress bitmap")
                    resolver.delete(uri, null, null)
                    return@withContext null
                }
            } ?: run {
                Log.e(TAG, "Failed to open output stream")
                resolver.delete(uri, null, null)
                return@withContext null
            }
            
            // Android 10+ 需要清除IS_PENDING标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            
            Log.d(TAG, "Image saved successfully: $uri")
            uri
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save image", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while saving image", e)
            null
        }
    }
    
    /**
     * 保存图片并附加EXIF元数据
     * @param context Context
     * @param bitmap 要保存的图片
     * @param exifData EXIF元数据（ISO、曝光时间、GPS等）
     * @return 保存后的Uri，失败返回null
     */
    suspend fun saveBitmapWithExif(
        context: Context,
        bitmap: Bitmap,
        exifData: Map<String, String>? = null
    ): Uri? = withContext(Dispatchers.IO) {
        val uri = saveBitmapToGallery(context, bitmap)
        
        if (uri != null && exifData != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                // 写入EXIF数据
                context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                    val exif = androidx.exifinterface.media.ExifInterface(pfd.fileDescriptor)
                    
                    exifData.forEach { (tag, value) ->
                        when (tag) {
                            "ISO" -> exif.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_ISO_SPEED_RATINGS, value)
                            "ExposureTime" -> exif.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_EXPOSURE_TIME, value)
                            "FNumber" -> exif.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_F_NUMBER, value)
                            "FocalLength" -> exif.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_FOCAL_LENGTH, value)
                            "GPSLatitude" -> {
                                val parts = value.split(",")
                                if (parts.size == 2) {
                                    exif.setLatLong(parts[0].toDouble(), parts[1].toDouble())
                                }
                            }
                        }
                    }
                    
                    exif.saveAttributes()
                    Log.d(TAG, "EXIF data saved successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save EXIF data", e)
            }
        }
        
        uri
    }
}
