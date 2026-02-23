package com.yanbao.camera.data.lbs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yanbao.camera.filter.CountryFilter
import com.yanbao.camera.filter.MasterFilterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Phase 2: LBS 位置服务管理器（增强版）
 *
 * 在现有 [LbsService] 基础上新增：
 * - 基于 GPS 坐标自动推荐国家滤镜（集成 [MasterFilterManager]）
 * - 连续位置更新（供实时滤镜推荐）
 * - 位置精度分级（GPS/网络/被动）
 *
 * 使用方式：
 * ```kotlin
 * val lbsManager = LbsManager(context)
 * lbsManager.startLocationUpdates()
 * // 收集推荐滤镜
 * val filter by lbsManager.recommendedFilter.collectAsState()
 * ```
 */
class LbsManager(private val context: Context) {

    companion object {
        private const val TAG = "LbsManager"
        private const val UPDATE_INTERVAL_MS = 30_000L    // 30 秒更新一次
        private const val FASTEST_INTERVAL_MS = 10_000L  // 最快 10 秒
        private const val FILTER_UPDATE_DISTANCE_M = 500f // 移动 500m 才更新滤镜
    }

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val filterManager = MasterFilterManager(context)

    // ─── 状态 ─────────────────────────────────────────────────────────────

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _recommendedFilter = MutableStateFlow<CountryFilter?>(null)
    val recommendedFilter: StateFlow<CountryFilter?> = _recommendedFilter.asStateFlow()

    private val _locationAccuracy = MutableStateFlow(LocationAccuracy.UNKNOWN)
    val locationAccuracy: StateFlow<LocationAccuracy> = _locationAccuracy.asStateFlow()

    private var lastFilterUpdateLocation: Location? = null
    private var locationCallback: LocationCallback? = null

    // ─── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 开始持续位置更新
     */
    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            .setMinUpdateDistanceMeters(100f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                _currentLocation.value = location
                _locationAccuracy.value = when {
                    location.accuracy < 10f  -> LocationAccuracy.HIGH
                    location.accuracy < 50f  -> LocationAccuracy.MEDIUM
                    else                     -> LocationAccuracy.LOW
                }
                Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}, accuracy=${location.accuracy}m")

                // 仅在移动超过 500m 时更新滤镜推荐（避免频繁逆地理编码）
                val lastLoc = lastFilterUpdateLocation
                if (lastLoc == null || location.distanceTo(lastLoc) > FILTER_UPDATE_DISTANCE_M) {
                    lastFilterUpdateLocation = location
                    updateFilterRecommendation(location)
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
            Log.d(TAG, "Location updates started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission denied: ${e.message}")
        }
    }

    /**
     * 停止位置更新
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedClient.removeLocationUpdates(it)
            locationCallback = null
        }
        Log.d(TAG, "Location updates stopped")
    }

    /**
     * 获取一次性当前位置（挂起函数）
     */
    suspend fun getLastKnownLocation(): Location? = suspendCancellableCoroutine { cont ->
        if (!hasLocationPermission()) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        try {
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    _currentLocation.value = location
                    cont.resume(location)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to get last location: ${e.message}")
                    cont.resume(null)
                }
        } catch (e: SecurityException) {
            cont.resume(null)
        }
    }

    /**
     * 根据当前位置获取推荐滤镜
     */
    suspend fun getFilterForCurrentLocation(): CountryFilter? {
        val location = _currentLocation.value ?: getLastKnownLocation() ?: return null
        return filterManager.getFilterByLocation(location.latitude, location.longitude)
    }

    /**
     * 获取所有可用滤镜
     */
    fun getAllFilters(): List<CountryFilter> = filterManager.getAllFilters()

    /**
     * 按国家代码获取滤镜
     */
    fun getFilterByCountry(countryCode: String): CountryFilter? =
        filterManager.getFilterForCountry(countryCode)

    // ─── 私有方法 ─────────────────────────────────────────────────────────

    private fun updateFilterRecommendation(location: Location) {
        // 在后台协程中执行逆地理编码
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val filter = filterManager.getFilterByLocation(location.latitude, location.longitude)
            if (filter != null) {
                _recommendedFilter.value = filter
                Log.d(TAG, "Filter recommendation updated: ${filter.name}")
            }
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
}

enum class LocationAccuracy { HIGH, MEDIUM, LOW, UNKNOWN }
