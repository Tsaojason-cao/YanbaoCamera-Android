package com.yanbao.camera.data.lbs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * LBS 地理位置服务
 * 
 * 🚨 核心验收标准：
 * - 必须使用 Google Play Services Location API
 * - 获取真实的设备 GPS 位置
 * - 权限检查和错误处理
 * - 集成 Supabase LBS 函数查询附近地点
 * 
 * 验收闭环：
 * - 关闭定位权限 → 提示"无法获取位置"
 * - 开启定位权限 → 显示真实的经纬度和附近地点
 * - 模拟器中修改位置 → 附近地点实时刷新
 */
class LbsService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * 检查位置权限
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查位置服务是否开启
     */
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * 获取当前位置
     * 
     * @return UserLocation 或 null（如果无法获取）
     */
    suspend fun getCurrentLocation(): UserLocation? {
        if (!hasLocationPermission()) {
            throw SecurityException("位置权限未授予")
        }

        if (!isLocationEnabled()) {
            throw IllegalStateException("位置服务未开启")
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                val cancellationTokenSource = CancellationTokenSource()

                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(
                            UserLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy,
                                timestamp = location.time
                            )
                        )
                    } else {
                        continuation.resume(null)
                    }
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * 获取附近热门地点
     * 
     * 🚨 核心逻辑：调用 Supabase nearby_hot_locations() 函数
     * 
     * @param userLocation 用户当前位置
     * @param radiusKm 搜索半径（公里）
     * @return 附近热门地点列表
     */
    suspend fun getNearbyHotLocations(
        userLocation: UserLocation,
        radiusKm: Double = 10.0
    ): List<HotLocation> {
        // TODO: 集成 Supabase 客户端
        // 当前返回模拟数据用于测试，后续替换为真实 API 调用
        
        return listOf(
            HotLocation(
                id = "1",
                name = "台北101",
                latitude = 25.0340,
                longitude = 121.5645,
                address = "台北市信义区信义路五段7号",
                category = "城市地标",
                photoCount = 1234,
                popularityScore = 4.8f,
                featuredPhotoUrl = null,
                distanceKm = calculateDistance(
                    userLocation.latitude,
                    userLocation.longitude,
                    25.0340,
                    121.5645
                ),
                recommendedFilter = "Master_001"
            ),
            HotLocation(
                id = "2",
                name = "东京塔",
                latitude = 35.6586,
                longitude = 139.7454,
                address = "东京都港区芝公园4-2-8",
                category = "城市地标",
                photoCount = 2345,
                popularityScore = 4.9f,
                featuredPhotoUrl = null,
                distanceKm = calculateDistance(
                    userLocation.latitude,
                    userLocation.longitude,
                    35.6586,
                    139.7454
                ),
                recommendedFilter = "Master_002"
            )
        ).filter { it.distanceKm <= radiusKm }
    }

    /**
     * 获取附近照片
     * 
     * 🚨 核心逻辑：调用 Supabase nearby_photos() 函数
     * 
     * @param userLocation 用户当前位置
     * @param radiusKm 搜索半径（公里）
     * @param limit 返回数量限制
     * @return 附近照片列表
     */
    suspend fun getNearbyPhotos(
        userLocation: UserLocation,
        radiusKm: Double = 10.0,
        limit: Int = 100
    ): List<NearbyPhoto> {
        // TODO: 集成 Supabase 客户端
        // 当前返回空列表，后续替换为真实 API 调用
        return emptyList()
    }

    /**
     * 获取地点统计信息
     * 
     * @param locationId 地点ID
     * @return 统计信息
     */
    suspend fun getLocationStatistics(locationId: String): LocationStatistics? {
        // TODO: 集成 Supabase 客户端
        return null
    }

    /**
     * 计算两点之间的距离（公里）
     * 
     * 使用 Haversine 公式
     */
    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371.0 // 地球半径（公里）

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }
}
