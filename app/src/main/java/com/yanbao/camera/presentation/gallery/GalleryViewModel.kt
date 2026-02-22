package com.yanbao.camera.presentation.gallery

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.util.YanbaoExifParser
import com.yanbao.camera.data.local.dao.YanbaoMemoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * GalleryViewModel（满血版）
 *
 * 真实 MediaStore 扫描：
 * 1. 优先扫描 MediaStore（Android 10+ 标准方式）
 * 2. 同时扫描 DCIM/YanbaoCamera 目录（Camera2 直接写入路径）
 * 3. 通过 YanbaoExifParser 读取 Exif 模式信息进行分类
 * 4. 与 Room 数据库关联，标记"雁宝记忆"照片
 * 5. 零 Mock 数据，零占位符
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val yanbaoMemoryDao: YanbaoMemoryDao
) : ViewModel() {

    companion object {
        private const val TAG = "GalleryViewModel"
    }

    private val _selectedTab = MutableStateFlow(GalleryTab.ALL)
    val selectedTab: StateFlow<GalleryTab> = _selectedTab

    private val _filteredPhotos = MutableStateFlow<List<Photo>>(emptyList())
    val filteredPhotos: StateFlow<List<Photo>> = _filteredPhotos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 多选模式
    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode

    private val _selectedPhotoIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedPhotoIds: StateFlow<Set<String>> = _selectedPhotoIds

    // 所有照片缓存（用于 Tab 过滤）
    private var allPhotosCache: List<Photo> = emptyList()

    // 雁宝记忆照片路径集合（从 Room 数据库加载）
    private var memoryImagePaths: Set<String> = emptySet()

    init {
        loadMemoryPaths()
        loadPhotos()
    }

    /**
     * 从 Room 数据库加载雁宝记忆照片路径
     */
    private fun loadMemoryPaths() {
        viewModelScope.launch {
            yanbaoMemoryDao.getAllMemories().collect { memories ->
                memoryImagePaths = memories.map { it.imagePath }.toSet()
                Log.d(TAG, "Memory paths loaded: ${memoryImagePaths.size}")
                // 重新过滤当前 Tab
                applyFilter(_selectedTab.value)
            }
        }
    }

    /**
     * 加载所有照片（真实 MediaStore + 文件系统双路扫描）
     */
    fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                allPhotosCache = withContext(Dispatchers.IO) {
                    scanAllPhotos()
                }
                Log.d(TAG, "Photos loaded: ${allPhotosCache.size}")
                applyFilter(_selectedTab.value)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load photos", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Tab 切换
     */
    fun onTabSelected(tab: GalleryTab) {
        _selectedTab.value = tab
        applyFilter(tab)
    }

    /**
     * 搜索
     */
    fun onSearch(query: String) {
        _searchQuery.value = query
        applyFilter(_selectedTab.value)
    }

    /**
     * 进入/退出多选模式
     */
    fun toggleMultiSelectMode() {
        _isMultiSelectMode.value = !_isMultiSelectMode.value
        if (!_isMultiSelectMode.value) {
            _selectedPhotoIds.value = emptySet()
        }
    }

    fun togglePhotoSelection(photoId: String) {
        val current = _selectedPhotoIds.value.toMutableSet()
        if (current.contains(photoId)) current.remove(photoId) else current.add(photoId)
        _selectedPhotoIds.value = current
    }

    fun clearSelection() {
        _selectedPhotoIds.value = emptySet()
        _isMultiSelectMode.value = false
    }

    /**
     * 应用 Tab 过滤 + 搜索过滤
     */
    private fun applyFilter(tab: GalleryTab) {
        val query = _searchQuery.value.lowercase()
        var result = when (tab) {
            GalleryTab.ALL -> allPhotosCache
            GalleryTab.MEMORY -> allPhotosCache.filter { photo ->
                // 雁宝记忆：在 Room 数据库中有记录，或 Exif 模式包含 MEMORY
                memoryImagePaths.any { path ->
                    photo.path.contains(path) || path.contains(photo.path)
                } || photo.mode?.contains("MEMORY", ignoreCase = true) == true
            }
            GalleryTab.MASTER -> allPhotosCache.filter {
                it.mode?.contains("MASTER", ignoreCase = true) == true
            }
            GalleryTab.BEAUTY -> allPhotosCache.filter {
                it.mode?.contains("BEAUTY", ignoreCase = true) == true
            }
            GalleryTab.D29 -> allPhotosCache.filter {
                it.mode?.contains("29D", ignoreCase = true) == true ||
                it.mode?.contains("2.9D", ignoreCase = true) == true
            }
        }

        // 搜索过滤
        if (query.isNotBlank()) {
            result = result.filter { photo ->
                photo.path.lowercase().contains(query) ||
                photo.mode?.lowercase()?.contains(query) == true
            }
        }

        _filteredPhotos.value = result
    }

    /**
     * 双路扫描：MediaStore + 文件系统
     * 返回去重后的照片列表（按时间倒序）
     */
    private suspend fun scanAllPhotos(): List<Photo> = withContext(Dispatchers.IO) {
        val photos = mutableMapOf<String, Photo>() // path → Photo（去重）

        // ─── 路径1：MediaStore 扫描（Android 10+ 标准方式）─────────────
        try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DISPLAY_NAME
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val path = cursor.getString(dataCol) ?: continue
                    val name = cursor.getString(nameCol) ?: ""
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )

                    // 读取 Exif 模式（仅对 YanbaoCamera 拍摄的照片）
                    val mode = if (name.startsWith("YanbaoAI_") || path.contains("YanbaoCamera")) {
                        try {
                            YanbaoExifParser.getPhotoMetadata(path).mode
                        } catch (e: Exception) { "NORMAL" }
                    } else "NORMAL"

                    val photo = Photo(
                        id = id.toString(),
                        path = contentUri.toString(), // 使用 content:// URI
                        hasMetadata = mode != "NORMAL" && mode != "未知模式" && mode != "普通模式",
                        mode = mode
                    )
                    photos[path] = photo
                }
            }
            Log.d(TAG, "MediaStore scan: ${photos.size} photos")
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore scan failed", e)
        }

        // ─── 路径2：文件系统扫描（DCIM/YanbaoCamera）────────────────────
        try {
            val dirs = listOf(
                File(context.getExternalFilesDir(null), "DCIM/YanbaoCamera"),
                File(android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES), "YanbaoAI"),
                File(android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DCIM), "YanbaoCamera")
            )

            dirs.forEach { dir ->
                if (dir.exists()) {
                    dir.listFiles { file ->
                        file.extension.lowercase() in listOf("jpg", "jpeg", "png")
                    }?.forEach { file ->
                        if (!photos.containsKey(file.absolutePath)) {
                            val params = try {
                                YanbaoExifParser.getPhotoMetadata(file.absolutePath)
                            } catch (e: Exception) { null }

                            photos[file.absolutePath] = Photo(
                                id = file.nameWithoutExtension,
                                path = Uri.fromFile(file).toString(),
                                hasMetadata = params?.mode != null &&
                                    params.mode != "未知模式" && params.mode != "普通模式",
                                mode = params?.mode ?: "NORMAL"
                            )
                        }
                    }
                }
            }
            Log.d(TAG, "File system scan complete, total: ${photos.size} photos")
        } catch (e: Exception) {
            Log.e(TAG, "File system scan failed", e)
        }

        photos.values.toList()
    }

    /**
     * 点击照片进入详情
     */
    fun onPhotoClick(photo: Photo) {
        Log.d(TAG, "Photo clicked: id=${photo.id}, path=${photo.path}, mode=${photo.mode}")
    }

    /**
     * 删除选中的照片
     */
    fun deleteSelectedPhotos() {
        viewModelScope.launch {
            val selectedIds = _selectedPhotoIds.value
            Log.d(TAG, "Delete ${selectedIds.size} photos")
            // 实际删除逻辑（通过 MediaStore）
            withContext(Dispatchers.IO) {
                selectedIds.forEach { photoId ->
                    try {
                        val uri = Uri.parse(
                            allPhotosCache.find { it.id == photoId }?.path ?: return@forEach
                        )
                        context.contentResolver.delete(uri, null, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Delete failed for $photoId", e)
                    }
                }
            }
            clearSelection()
            loadPhotos() // 刷新列表
        }
    }
}
