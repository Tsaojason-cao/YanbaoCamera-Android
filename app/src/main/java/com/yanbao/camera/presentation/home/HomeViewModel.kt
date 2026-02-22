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

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            delay(500)
            _uiState.value = HomeUiState(
                temperature = 28,
                weatherDesc = "适合外拍",
                motto = "今天也要拍出好照片哦",
                recentActivities = listOf(
                    RecentActivity("你在台北101拍摄了新照片", "1s ago"),
                    RecentActivity("你在西门町逛了逛了", "10m ago")
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
