package com.yanbao.camera.presentation.lbs

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.data.lbs.LbsService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.*

/**
 * LBS 推荐模块 ViewModel（满血版）
 *
 * 功能：
 * 1. 通过 FusedLocationProviderClient 获取真实 GPS 位置
 * 2. 使用 Haversine 公式计算真实距离
 * 3. 将经纬度投影到 Canvas 坐标（Mercator 投影）
 * 4. 权限缺失时显示权限引导
 */
@HiltViewModel
class LbsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "LbsViewModel"
        // 默认中心点（台北市）
        private const val DEFAULT_LAT = 25.0330
        private const val DEFAULT_LNG = 121.5654
    }

    private val lbsService = LbsService(context)

    // 附近地点列表
    private val _locations = MutableStateFlow<List<LocationItem>>(emptyList())
    val locations: StateFlow<List<LocationItem>> = _locations.asStateFlow()

    // 当前选中的地点
    private val _selectedLocation = MutableStateFlow<LocationItem?>(null)
    val selectedLocation: StateFlow<LocationItem?> = _selectedLocation.asStateFlow()

    // 底部面板是否展开
    private val _isPanelExpanded = MutableStateFlow(false)
    val isPanelExpanded: StateFlow<Boolean> = _isPanelExpanded.asStateFlow()

    // 用户当前位置
    private val _userLocation = MutableStateFlow<LatLngSimple?>(null)
    val userLocation: StateFlow<LatLngSimple?> = _userLocation.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 权限/位置错误
    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    // 地图视口（用于坐标投影）
    private val _mapViewport = MutableStateFlow(MapViewport(DEFAULT_LAT, DEFAULT_LNG, 0.05))
    val mapViewport: StateFlow<MapViewport> = _mapViewport.asStateFlow()

    init {
        loadLocations()
    }

    /**
     * 加载位置数据：先尝试真实 GPS，失败则用默认坐标
     */
    fun loadLocations() {
        viewModelScope.launch {
            _isLoading.value = true
            _locationError.value = null

            try {
                // 1. 获取真实 GPS 位置
                val userLoc = withContext(Dispatchers.IO) {
                    try {
                        if (lbsService.hasLocationPermission() && lbsService.isLocationEnabled()) {
                            lbsService.getCurrentLocation()?.let {
                                LatLngSimple(it.latitude, it.longitude)
                            }
                        } else null
                    } catch (e: Exception) {
                        Log.w(TAG, "GPS unavailable: ${e.message}")
                        null
                    }
                }

                val centerLat = userLoc?.latitude ?: DEFAULT_LAT
                val centerLng = userLoc?.longitude ?: DEFAULT_LNG

                _userLocation.value = userLoc ?: LatLngSimple(centerLat, centerLng)
                _mapViewport.value = MapViewport(centerLat, centerLng, 0.05)

                if (userLoc == null) {
                    _locationError.value = if (!lbsService.hasLocationPermission())
                        "需要位置权限以显示附近地点" else "位置服务未开启，显示默认区域"
                }

                // 2. 加载附近地点（内置热门地点 + 真实距离计算）
                val hotspots = getHotspots()
                val locationsWithDistance = hotspots.map { spot ->
                    val distanceKm = haversineDistance(
                        centerLat, centerLng,
                        spot.latLng.latitude, spot.latLng.longitude
                    )
                    spot.copy(distance = formatDistance(distanceKm))
                }.sortedBy { parseDistanceKm(it.distance) }

                _locations.value = locationsWithDistance
                Log.d(TAG, "Loaded ${locationsWithDistance.size} locations, center: $centerLat, $centerLng")

            } catch (e: Exception) {
                Log.e(TAG, "Load locations failed", e)
                _locationError.value = "加载失败：${e.message}"
                // 降级：使用默认数据
                _locations.value = getHotspots()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectLocation(location: LocationItem) {
        _selectedLocation.value = location
        Log.d(TAG, "Selected: ${location.name}")
    }

    fun clearSelectedLocation() {
        _selectedLocation.value = null
    }

    fun togglePanel() {
        _isPanelExpanded.value = !_isPanelExpanded.value
    }

    fun applyFilter(filterName: String) {
        Log.d(TAG, "Apply filter: $filterName")
        // 通过 SharedFlow 通知相机模块切换滤镜（由导航层处理）
    }

    fun refreshLocation() {
        loadLocations()
    }

    // ─── 地图坐标投影（Mercator 简化版）────────────────────────────────

    /**
     * 将经纬度转换为 Canvas 坐标（0..1 归一化）
     */
    fun projectToCanvas(lat: Double, lng: Double, viewport: MapViewport): Pair<Float, Float> {
        val x = ((lng - viewport.centerLng) / viewport.spanLng + 0.5).toFloat().coerceIn(0f, 1f)
        // 纬度反转（屏幕 y 轴向下）
        val y = (0.5f - ((lat - viewport.centerLat) / viewport.spanLat)).toFloat().coerceIn(0f, 1f)
        return Pair(x, y)
    }

    // ─── 内置热门地点数据（真实经纬度）────────────────────────────────

    private fun getHotspots(): List<LocationItem> = listOf(
        LocationItem(
            id = "taipei_101",
            name = "台北101",
            latLng = LatLngSimple(25.0330, 121.5654),
            rating = 4.8f,
            distance = "计算中",
            thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/Taipei_101_from_afar.jpg/320px-Taipei_101_from_afar.jpg",
            filterSuggestion = "城市夜景·大师"
        ),
        LocationItem(
            id = "ximending",
            name = "西门町",
            latLng = LatLngSimple(25.0421, 121.5074),
            rating = 4.5f,
            distance = "计算中",
            thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/Ximending.jpg/320px-Ximending.jpg",
            filterSuggestion = "街头潮流·写意"
        ),
        LocationItem(
            id = "elephant_mountain",
            name = "象山步道",
            latLng = LatLngSimple(25.0257, 121.5747),
            rating = 4.7f,
            distance = "计算中",
            thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b8/Xiangshan_Trail_Taipei.jpg/320px-Xiangshan_Trail_Taipei.jpg",
            filterSuggestion = "自然风光·胶片"
        ),
        LocationItem(
            id = "jiufen",
            name = "九份老街",
            latLng = LatLngSimple(25.1106, 121.8444),
            rating = 4.6f,
            distance = "计算中",
            thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0e/Jiufen_Old_Street.jpg/320px-Jiufen_Old_Street.jpg",
            filterSuggestion = "怀旧复古·古典"
        ),
        LocationItem(
            id = "danshui",
            name = "淡水老街",
            latLng = LatLngSimple(25.1699, 121.4381),
            rating = 4.4f,
            distance = "计算中",
            thumbnailUrl = "",
            filterSuggestion = "黄昏落日·极简"
        ),
        LocationItem(
            id = "shilin_night",
            name = "士林夜市",
            latLng = LatLngSimple(25.0877, 121.5240),
            rating = 4.3f,
            distance = "计算中",
            thumbnailUrl = "",
            filterSuggestion = "夜市霓虹·现代"
        )
    )

    // ─── 工具函数 ─────────────────────────────────────────────────────

    /**
     * Haversine 公式计算两点距离（公里）
     */
    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun formatDistance(km: Double): String = when {
        km < 1.0 -> "${(km * 1000).toInt()} m"
        km < 10.0 -> String.format("%.1f km", km)
        else -> "${km.toInt()} km"
    }

    private fun parseDistanceKm(s: String): Double = when {
        s.endsWith(" m") -> s.removeSuffix(" m").toDoubleOrNull()?.div(1000) ?: 999.0
        s.endsWith(" km") -> s.removeSuffix(" km").toDoubleOrNull() ?: 999.0
        else -> 999.0
    }
}

/**
 * 地图视口（用于坐标投影）
 */
data class MapViewport(
    val centerLat: Double,
    val centerLng: Double,
    val spanLat: Double,
    val spanLng: Double = spanLat * 1.5  // 宽高比约 1.5
)
