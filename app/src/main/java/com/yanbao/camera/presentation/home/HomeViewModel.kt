package com.yanbao.camera.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────
// 数据模型
// ─────────────────────────────────────────────────────────────

data class RecentActivity(
    val id: String,
    val description: String,
    val time: String,
    val thumbnailUrl: String = ""
)

data class PopularPlace(
    val id: String,
    val name: String,
    val location: String,
    val rating: Int,          // 1-5
    val imageUrl: String = "",
    val photoCount: Int = 0
)

data class HomeUiState(
    val greeting: String = "早安！",
    val subGreeting: String = "今天也要拍出好照片哦 📷",
    val temperature: Int = 28,
    val weatherDesc: String = "适合外拍",
    val weatherIcon: String = "☀️",
    val motto: String = "用镜头记录每一个美好瞬间",
    val recentActivities: List<RecentActivity> = emptyList(),
    val popularPlaces: List<PopularPlace> = emptyList(),
    val isLoading: Boolean = false
)

// ─────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            // 模拟网络延迟
            delay(300L)
            _uiState.value = HomeUiState(
                greeting = "早安！",
                subGreeting = "今天也要拍出好照片哦 📷",
                temperature = 28,
                weatherDesc = "适合外拍",
                weatherIcon = "☀️",
                motto = "用镜头记录每一个美好瞬间",
                recentActivities = buildRecentActivities(),
                popularPlaces = buildPopularPlaces(),
                isLoading = false
            )
        }
    }

    private fun buildRecentActivities(): List<RecentActivity> = listOf(
        RecentActivity(
            id = "act_001",
            description = "you在台北101拍摄了新照片",
            time = "Time 1s ago",
            thumbnailUrl = ""
        ),
        RecentActivity(
            id = "act_002",
            description = "you在台北101拍摄了新照片",
            time = "Time 1s ago",
            thumbnailUrl = ""
        ),
        RecentActivity(
            id = "act_003",
            description = "你在九份老街完成了一次拍摄",
            time = "2 mins ago",
            thumbnailUrl = ""
        )
    )

    private fun buildPopularPlaces(): List<PopularPlace> = listOf(
        PopularPlace(
            id = "place_001",
            name = "台北101",
            location = "台北市信义区",
            rating = 5,
            photoCount = 2847
        ),
        PopularPlace(
            id = "place_002",
            name = "台南波场",
            location = "台南市安平区",
            rating = 5,
            photoCount = 1923
        ),
        PopularPlace(
            id = "place_003",
            name = "北海坑境",
            location = "新北市瑞芳区",
            rating = 4,
            photoCount = 1456
        ),
        PopularPlace(
            id = "place_004",
            name = "九份老街",
            location = "新北市瑞芳区",
            rating = 5,
            photoCount = 3102
        )
    )

    fun refreshData() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadHomeData()
    }
}
