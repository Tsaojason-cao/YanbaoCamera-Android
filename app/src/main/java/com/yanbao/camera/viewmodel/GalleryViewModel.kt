package com.yanbao.camera.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.model.Photo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 相册ViewModel - 管理相册和照片列表
 */
class GalleryViewModel : ViewModel() {

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos

    private val _selectedPhoto = MutableStateFlow<Photo?>(null)
    val selectedPhoto: StateFlow<Photo?> = _selectedPhoto

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentPage = MutableStateFlow(0)

    init {
        loadPhotos()
    }

    /**
     * 加载照片列表
     */
    fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            // TODO: 从MediaStore读取照片
            // 暂时使用空列表
            _photos.value = emptyList()
            _isLoading.value = false
        }
    }

    /**
     * 加载更多照片（分页）
     */
    fun loadMorePhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            _currentPage.value++
            // TODO: 实现分页加载
            _isLoading.value = false
        }
    }

    /**
     * 选择照片
     */
    fun selectPhoto(photo: Photo) {
        _selectedPhoto.value = photo
    }

    /**
     * 删除照片
     */
    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            val updated = _photos.value.toMutableList()
            updated.remove(photo)
            _photos.value = updated
            if (_selectedPhoto.value == photo) {
                _selectedPhoto.value = null
            }
        }
    }

    /**
     * 删除多个照片
     */
    fun deletePhotos(photosToDelete: List<Photo>) {
        viewModelScope.launch {
            val updated = _photos.value.toMutableList()
            updated.removeAll(photosToDelete)
            _photos.value = updated
        }
    }

    /**
     * 刷新相册
     */
    fun refreshGallery() {
        viewModelScope.launch {
            _currentPage.value = 0
            loadPhotos()
        }
    }

    /**
     * 搜索照片
     */
    fun searchPhotos(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadPhotos()
            } else {
                // TODO: 实现搜索功能
            }
        }
    }

    /**
     * 按日期排序
     */
    fun sortByDate() {
        viewModelScope.launch {
            val sorted = _photos.value.sortedByDescending { it.timestamp }
            _photos.value = sorted
        }
    }

    /**
     * 按大小排序
     */
    fun sortBySize() {
        viewModelScope.launch {
            val sorted = _photos.value.sortedByDescending { it.size }
            _photos.value = sorted
        }
    }
}
