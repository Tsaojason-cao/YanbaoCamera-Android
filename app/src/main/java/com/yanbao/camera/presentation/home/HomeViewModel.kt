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

// 模拟数据类
data class RecentActivity(
    val description: String,
    val time: String
)

data class PopularPlace(
    val name: String,
    val rating: Int // 1-5
)

data class HomeUiState(
    val temperature: Int = 28,
    val weatherDesc: String = "适合外拍",
    val motto: String = "今天也要拍出好照片哦",
    val recentActivities: List<RecentActivity> = emptyList(),
    val popularPlaces: List<PopularPlace> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 模拟网络延迟，加载真实数据
            delay(500)
            _uiState.value = HomeUiState(
                temperature = 28,
                weatherDesc = "适合外拍",
                motto = "今天也要拍出好照片哦",
                recentActivities = listOf(
                    RecentActivity("你在台北101拍摄了新照片", "Time 1s ago"),
                    RecentActivity("你在台北101拍摄了新照片", "Time 1s ago")
                ),
                popularPlaces = listOf(
                    PopularPlace("台北101", 5),
                    PopularPlace("台南波场", 5),
                    PopularPlace("北海坑境", 5)
                )
            )
        }
    }
}
