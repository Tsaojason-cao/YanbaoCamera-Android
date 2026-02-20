package com.yanbao.camera.viewmodel

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.data.model.GalleryPhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 相册 ViewModel
 * 真实从 Android MediaStore 读取手机相册中的照片
 * 不是 Mock 数据，不是空列表
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "YanbaoGalleryVM"

    private val _photos = MutableStateFlow<List<GalleryPhoto>>(emptyList())
    val photos: StateFlow<List<GalleryPhoto>> = _photos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * 从 MediaStore 真实读取手机相册中的所有照片
     * 按拍摄时间倒序排列（最新的在前面）
     */
    fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val photos = withContext(Dispatchers.IO) {
                    queryMediaStore()
                }
                _photos.value = photos
                Log.d(TAG, "成功加载 ${photos.size} 张照片")
            } catch (e: Exception) {
                Log.e(TAG, "加载照片失败: ${e.message}", e)
                _photos.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 真实 MediaStore 查询
     * 查询所有图片，按日期倒序，返回 URI 列表
     */
    private fun queryMediaStore(): List<GalleryPhoto> {
        val photos = mutableListOf<GalleryPhoto>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.SIZE
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val query = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "未知"
                val dateAdded = cursor.getLong(dateColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val size = cursor.getLong(sizeColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                photos.add(
                    GalleryPhoto(
                        id = id,
                        uri = contentUri.toString(),
                        name = name,
                        dateAdded = dateAdded,
                        width = width,
                        height = height,
                        size = size
                    )
                )
            }
        }

        Log.d(TAG, "MediaStore 查询完成，找到 ${photos.size} 张照片")
        return photos
    }

    /**
     * 删除照片
     */
    fun deletePhoto(photoId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    photoId
                )
                val deleted = context.contentResolver.delete(uri, null, null)
                Log.d(TAG, "删除照片 $photoId，结果: $deleted")
            }
            // 重新加载相册
            loadPhotos()
        }
    }
}
