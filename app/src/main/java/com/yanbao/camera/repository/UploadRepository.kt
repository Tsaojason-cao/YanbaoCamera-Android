package com.yanbao.camera.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.yanbao.camera.api.NetworkModule
import com.yanbao.camera.model.Post
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * 上传Repository - 处理作品上传
 */
class UploadRepository(private val context: Context) {
    
    private val api = NetworkModule.apiService
    
    /**
     * 上传作品
     */
    suspend fun uploadPost(
        imageUri: String,
        description: String,
        locationId: String? = null
    ): Result<Post> {
        return try {
            // 将URI转换为File
            val file = uriToFile(imageUri)
            
            val requestFile = file.asRequestBody("image/*".toMediaType())
            val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val descPart = description.toRequestBody("text/plain".toMediaType())
            val locPart = locationId?.toRequestBody("text/plain".toMediaType())
            
            val response = api.uploadPost(part, descPart, locPart)
            if (response.code == 200) {
                Log.d(TAG, "上传作品成功")
                Result.success(response.data)
            } else {
                Log.e(TAG, "上传作品失败: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "上传作品异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 将URI转换为File
     */
    private fun uriToFile(uriString: String): File {
        val uri = Uri.parse(uriString)
        
        // 如果是file://协议，直接转换
        if (uri.scheme == "file") {
            return File(uri.path ?: throw Exception("Invalid file URI"))
        }
        
        // 如果是content://协议，从ContentResolver读取
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open input stream for URI: $uri")
        
        // 创建临时文件
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        inputStream.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        
        return tempFile
    }
    
    companion object {
        private const val TAG = "UploadRepository"
    }
}
