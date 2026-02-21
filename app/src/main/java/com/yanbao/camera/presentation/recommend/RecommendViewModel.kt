package com.yanbao.camera.presentation.recommend

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 推荐模块 ViewModel
 * 
 * 功能：
 * - Tab 切换逻辑
 * - 照片机位数据管理
 * - LBS 定位和距离计算
 */
@HiltViewModel
class RecommendViewModel @Inject constructor() : ViewModel() {

    private val _selectedTab = MutableStateFlow(RecommendTab.NEARBY)
    val selectedTab: StateFlow<RecommendTab> = _selectedTab.asStateFlow()

    private val _filteredSpots = MutableStateFlow<List<PhotoSpot>>(emptyList())
    val filteredSpots: StateFlow<List<PhotoSpot>> = _filteredSpots.asStateFlow()

    init {
        loadPhotoSpots()
    }

    fun onTabSelected(tab: RecommendTab) {
        _selectedTab.value = tab
        filterSpotsByTab(tab)
    }

    private fun loadPhotoSpots() {
        viewModelScope.launch {
            // 模拟数据（实际应从数据库或 API 加载）
            val allSpots = listOf(
                PhotoSpot(
                    id = "1",
                    title = "台北101观景台",
                    location = "台北市信义區",
                    description = "最佳夜景拍摄地，俯瞰全城。",
                    imageUrl = "",
                    rating = 4.8f,
                    category = "📊 城市地标",
                    categoryColor = Color(0xFF6B7FFF),
                    badgeIcon = R.drawable.kuromi,
                    distance = 2.1f,
                    photoCount = 345
                ),
                PhotoSpot(
                    id = "2",
                    title = "九份老街",
                    location = "新北市瑞芳區",
                    description = "挂满红灯笼的狭窄老街，千与千寻灵感地。",
                    imageUrl = "",
                    rating = 4.8f,
                    category = "🏮 古色古香",
                    categoryColor = Color(0xFFFF6B6B),
                    badgeIcon = R.drawable.kuromi_bl,
                    distance = 12.4f,
                    photoCount = 280
                ),
                PhotoSpot(
                    id = "3",
                    title = "日月潭",
                    location = "南投縣魚池鄉",
                    description = "如诗如画的湖光山色，清晨拍摄最佳。",
                    imageUrl = "",
                    rating = 4.8f,
                    category = "🏞️ 自然风光",
                    categoryColor = Color(0xFF4CAF50),
                    badgeIcon = R.drawable.kuromi_br,
                    distance = 45.2f,
                    photoCount = 189
                ),
                PhotoSpot(
                    id = "4",
                    title = "太鲁阁国家公园",
                    location = "花蓮縣秀林鄉",
                    description = "世界级峡谷景观，徒步拍摄的绝佳地点。",
                    imageUrl = "",
                    rating = 4.8f,
                    category = "⛰️ 壮观峡谷",
                    categoryColor = Color(0xFF8BC34A),
                    badgeIcon = R.drawable.kuromi_tl,
                    distance = 78.3f,
                    photoCount = 156
                )
            )
            
            _filteredSpots.value = allSpots
        }
    }

    private fun filterSpotsByTab(tab: RecommendTab) {
        viewModelScope.launch {
            val spots = _filteredSpots.value
            _filteredSpots.value = when (tab) {
                RecommendTab.NEARBY -> spots.sortedBy { it.distance }
                RecommendTab.LATEST -> spots.reversed() // 模拟最新
                RecommendTab.HOT -> spots.sortedByDescending { it.photoCount }
                RecommendTab.RATING -> spots.sortedByDescending { it.rating }
            }
        }
    }
}
