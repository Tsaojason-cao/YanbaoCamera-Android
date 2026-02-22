package com.yanbao.camera.presentation.lbs

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
class LbsViewModel @Inject constructor() : ViewModel() {

    // 附近地点列表
    private val _locations = MutableStateFlow<List<LocationItem>>(emptyList())
    val locations: StateFlow<List<LocationItem>> = _locations.asStateFlow()

    // 当前选中的地点（点击标记后设置）
    private val _selectedLocation = MutableStateFlow<LocationItem?>(null)
    val selectedLocation: StateFlow<LocationItem?> = _selectedLocation.asStateFlow()

    // 底部面板是否展开
    private val _isPanelExpanded = MutableStateFlow(false)
    val isPanelExpanded: StateFlow<Boolean> = _isPanelExpanded.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        viewModelScope.launch {
            delay(300)
            _locations.value = listOf(
                LocationItem(
                    id = "1",
                    name = "台北101",
                    latLng = LatLngSimple(25.0330, 121.5654),
                    rating = 4.8f,
                    distance = "1.2 km",
                    thumbnailUrl = "",
                    filterSuggestion = "城市夜景"
                ),
                LocationItem(
                    id = "2",
                    name = "西门町",
                    latLng = LatLngSimple(25.0421, 121.5074),
                    rating = 4.5f,
                    distance = "2.3 km",
                    thumbnailUrl = "",
                    filterSuggestion = "街头潮流"
                ),
                LocationItem(
                    id = "3",
                    name = "象山步道",
                    latLng = LatLngSimple(25.0257, 121.5747),
                    rating = 4.7f,
                    distance = "3.5 km",
                    thumbnailUrl = "",
                    filterSuggestion = "自然风光"
                ),
                LocationItem(
                    id = "4",
                    name = "九份老街",
                    latLng = LatLngSimple(25.1106, 121.8444),
                    rating = 4.6f,
                    distance = "15.2 km",
                    thumbnailUrl = "",
                    filterSuggestion = "怀旧复古"
                )
            )
        }
    }

    fun selectLocation(location: LocationItem) {
        _selectedLocation.value = location
    }

    fun clearSelectedLocation() {
        _selectedLocation.value = null
    }

    fun togglePanel() {
        _isPanelExpanded.value = !_isPanelExpanded.value
    }

    fun applyFilter(filterName: String) {
        // 跳转相机并应用滤镜（由导航处理）
        android.util.Log.d("LbsViewModel", "Apply filter: $filterName")
    }
}
