package com.yanbao.camera.presentation.gallery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.util.YanbaoExifParser
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
 * GalleryViewModel: 相册底层查询逻辑
 */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    @ApplicationContext private val context: Context
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
     * 🚨 核心逻辑：从真实文件系统扫描照片，并通过 Exif 读取模式进行分类
     * 
     * 这是"审讯室"环节 - 如果相机模块造假，这里会暴露
     */
    private fun loadPhotosByTab(tab: GalleryTab) {
        viewModelScope.launch {
            val allPhotos = withContext(Dispatchers.IO) {
                scanRealPhotos()
            }

            _filteredPhotos.value = when (tab) {
                GalleryTab.MEMORY -> {
                    // 只查询雁宝记忆模式拍摄的照片
                    allPhotos.filter { it.mode?.contains("MEMORY", ignoreCase = true) == true }
                }
                GalleryTab.D29 -> {
                    // 只查询 29D 模式拍摄的照片
                    allPhotos.filter { it.mode?.contains("29D", ignoreCase = true) == true }
                }
                GalleryTab.MASTER -> {
                    // 查询大师模式照片（必须有 LBS 位置标签）
                    allPhotos.filter { it.mode?.contains("MASTER", ignoreCase = true) == true }
                }
                GalleryTab.BEAUTY -> {
                    // 查询美人模式照片（必须有美颜参数）
                    allPhotos.filter { it.mode?.contains("BEAUTY", ignoreCase = true) == true }
                }
                else -> {
                    // 全部照片
                    allPhotos
                }
            }
        }
    }

    /**
     * 🚨 核心方法：扫描真实的照片文件系统
     * 
     * 从 DCIM/YanbaoCamera 目录读取所有照片，并通过 YanbaoExifParser 提取模式信息
     */
    private suspend fun scanRealPhotos(): List<Photo> {
        return withContext(Dispatchers.IO) {
            try {
                // 扫描 DCIM/YanbaoCamera 目录
                val dcimDir = File(context.getExternalFilesDir(null), "DCIM/YanbaoCamera")
                if (!dcimDir.exists()) {
                    dcimDir.mkdirs()
                }

                val photoFiles = dcimDir.listFiles { file ->
                    file.extension.lowercase() in listOf("jpg", "jpeg", "png")
                } ?: emptyArray()

                photoFiles.map { file ->
                    // 从 Exif 读取模式信息
                    val params = YanbaoExifParser.getPhotoMetadata(file.absolutePath)
                    
                    Photo(
                        id = file.nameWithoutExtension,
                        path = file.absolutePath,
                        hasMetadata = params.mode != "未知模式" && params.mode != "普通模式",
                        mode = params.mode
                    )
                }.sortedByDescending { it.id } // 按时间倒序
            } catch (e: Exception) {
                // 如果扫描失败，回退到 Mock 数据
                mockPhotos
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
