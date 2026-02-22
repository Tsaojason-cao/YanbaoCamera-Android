package com.yanbao.camera.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
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
            _uiState.value = HomeUiState(
                greeting = getGreeting(),
                greetingSub = "今天也要拍出好照片哦",
                temperature = 28,
                weatherDesc = "适合外拍",
                recentActivities = listOf(
                    RecentActivity("你在台北101拍摄了新照片", "1s ago"),
                    RecentActivity("你在西门町逛了逛了", "10m ago")
                ),
                popularPlaces = listOf(
                    PopularPlace("台北101", 5, R.drawable.place_taipei101),
                    PopularPlace("台南波场", 5, R.drawable.place_tainan),
                    PopularPlace("北海坑境", 5, R.drawable.place_hokkaido)
                )
            )
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "早安！"
            in 12..17 -> "午安！"
            in 18..20 -> "晚安！"
            else -> "夜深了！"
        }
    }
}

// --- Data Classes ---
data class HomeUiState(
    val greeting: String = "",
    val greetingSub: String = "",
    val temperature: Int = 0,
    val weatherDesc: String = "",
    val recentActivities: List<RecentActivity> = emptyList(),
    val popularPlaces: List<PopularPlace> = emptyList()
)

data class RecentActivity(val description: String, val time: String)
data class PopularPlace(val name: String, val rating: Int, val imageRes: Int)
