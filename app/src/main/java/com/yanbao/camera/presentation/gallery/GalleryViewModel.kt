package com.yanbao.camera.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GalleryViewModel: 相册底层查询逻辑
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    // private val repository: PhotoRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(GalleryTab.ALL)
    val selectedTab: StateFlow<GalleryTab> = _selectedTab

    private val _filteredPhotos = MutableStateFlow<List<Photo>>(emptyList())
    val filteredPhotos: StateFlow<List<Photo>> = _filteredPhotos

    init {
        loadMockPhotos()
    }

    /**
     * Tab 切换逻辑
     */
    fun onTabSelected(tab: GalleryTab) {
        _selectedTab.value = tab
        loadPhotosByTab(tab)
    }

    /**
     * 根据选择的 Tab，从数据库过滤包含特定元数据的照片
     */
    private fun loadPhotosByTab(tab: GalleryTab) {
        viewModelScope.launch {
            _filteredPhotos.value = when (tab) {
                GalleryTab.MEMORY -> {
                    // 只查询带 29D 参数的照片
                    mockPhotos.filter { it.hasMetadata }
                }
                GalleryTab.D29 -> {
                    // 只查询 29D 模式拍摄的照片
                    mockPhotos.filter { it.mode == "29D" }
                }
                GalleryTab.MASTER -> {
                    // 查询大师模式照片
                    mockPhotos.filter { it.mode == "MASTER" }
                }
                GalleryTab.BEAUTY -> {
                    // 查询美人模式照片
                    mockPhotos.filter { it.mode == "BEAUTY" }
                }
                else -> {
                    // 全部照片
                    mockPhotos
                }
            }
        }
    }

    /**
     * 点击图片进入详情，必须传递 29D 参数快照
     */
    fun onPhotoClick(photo: Photo) {
        // 跳转详情页，并解析 Exif 中的物理参数 (WB, Shutter, ISO)
        // navController.navigate("photo_detail/${photo.id}")
    }

    private fun loadMockPhotos() {
        mockPhotos = generateMockPhotos()
        _filteredPhotos.value = mockPhotos
    }

    companion object {
        private var mockPhotos: List<Photo> = emptyList()

        private fun generateMockPhotos(): List<Photo> {
            return List(20) { index ->
                Photo(
                    id = "photo_$index",
                    path = "https://picsum.photos/400/400?random=$index",
                    hasMetadata = index % 3 == 0,
                    mode = when (index % 5) {
                        0 -> "29D"
                        1 -> "MASTER"
                        2 -> "BEAUTY"
                        else -> "NORMAL"
                    }
                )
            }
        }
    }
}
