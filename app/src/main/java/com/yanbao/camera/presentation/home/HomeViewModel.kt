package com.yanbao.camera.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
        startClockTick()
    }

    // ── 每分钟更新一次时间显示 ──
    private fun startClockTick() {
        viewModelScope.launch {
            while (true) {
                _uiState.value = _uiState.value.copy(
                    greeting    = buildGreeting(),
                    greetingSub = buildGreetingSub()
                )
                delay(60_000L)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(
                greeting    = buildGreeting(),
                greetingSub = buildGreetingSub(),
                // 天气数据：实际项目中应接入天气 API，此处保留合理默认值
                temperature = 28,
                weatherDesc = buildWeatherDesc(),
                recentActivities = listOf(
                    RecentActivity("你在台北101拍摄了新照片", "1s ago"),
                    RecentActivity("你在西门町逛了逛了", "10m ago")
                ),
                popularPlaces = listOf(
                    PopularPlace("台北101",  5),
                    PopularPlace("台南波场", 5),
                    PopularPlace("北海坑境", 5)
                )
            )
        }
    }

    // ── 根据当前小时返回问候语 ──
    private fun buildGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..11  -> "早安！"
            hour in 12..17 -> "午安！"
            hour in 18..20 -> "晚安！"
            else           -> "夜深了！"
        }
    }

    // ── 根据时段返回副标题 ──
    private fun buildGreetingSub(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..11  -> "今天也要拍出好照片哦"
            hour in 12..17 -> "下午适合外拍，出发吧"
            hour in 18..20 -> "黄金时段，拍出好照片"
            else           -> "休息一下，明天继续拍"
        }
    }

    // ── 根据时段返回天气描述 ──
    private fun buildWeatherDesc(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 6..11  -> "适合外拍"
            hour in 12..15 -> "光线充足"
            hour in 16..19 -> "黄金时段"
            else           -> "夜拍模式"
        }
    }
}
