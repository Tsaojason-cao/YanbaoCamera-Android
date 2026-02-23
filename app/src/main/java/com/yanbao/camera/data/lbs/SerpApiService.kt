package com.yanbao.camera.data.lbs

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * SerpApi Google Maps 搜索服务
 *
 * 用于查询附近摄影/艺术类地点，替代静态内置数据。
 * API Key: 7453c897b08195f42a488af5f0669307897f18bd2d0ec205ddc6fadec8b2a9ae
 */
object SerpApiService {

    private const val TAG = "SerpApiService"
    private const val API_KEY = "7453c897b08195f42a488af5f0669307897f18bd2d0ec205ddc6fadec8b2a9ae"
    private const val BASE_URL = "https://serpapi.com/search.json"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * 搜索附近摄影/艺术类地点
     *
     * @param lat 纬度
     * @param lng 经度
     * @param query 搜索关键词（默认：摄影 景点）
     * @return 地点列表
     */
    suspend fun searchNearbyPhotoSpots(
        lat: Double,
        lng: Double,
        query: String = "摄影 景点 打卡"
    ): List<SerpApiPlace> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val ll = "@$lat,$lng,14z"
            val url = "$BASE_URL?engine=google_maps&q=$encodedQuery&ll=$ll&api_key=$API_KEY&hl=zh-CN"

            Log.d(TAG, "Searching nearby spots: $url")

            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "SerpApi response failed: ${response.code}")
                return@withContext emptyList()
            }

            val body = response.body?.string() ?: return@withContext emptyList()
            parsePlaces(body, lat, lng)
        } catch (e: Exception) {
            Log.e(TAG, "SerpApi search failed", e)
            emptyList()
        }
    }

    /**
     * 解析 SerpApi 返回的 JSON
     */
    private fun parsePlaces(json: String, userLat: Double, userLng: Double): List<SerpApiPlace> {
        return try {
            val root = JSONObject(json)
            val results = root.optJSONArray("local_results") ?: return emptyList()
            val places = mutableListOf<SerpApiPlace>()

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val gps = item.optJSONObject("gps_coordinates")
                val lat = gps?.optDouble("latitude") ?: continue
                val lng = gps.optDouble("longitude")
                val name = item.optString("title", "未知地点")
                val rating = item.optDouble("rating", 0.0).toFloat()
                val address = item.optString("address", "")
                val type = item.optJSONArray("type")?.optString(0) ?: "景点"
                val thumbnail = item.optString("thumbnail", "")
                val placeId = item.optString("place_id", name)

                // 计算距离
                val distKm = haversine(userLat, userLng, lat, lng)
                val distStr = when {
                    distKm < 1.0 -> "${(distKm * 1000).toInt()} m"
                    distKm < 10.0 -> String.format("%.1f km", distKm)
                    else -> "${distKm.toInt()} km"
                }

                // 根据类型推荐滤镜
                val filterSuggestion = when {
                    type.contains("公园") || type.contains("park", true) -> "自然风光·胶片"
                    type.contains("博物馆") || type.contains("museum", true) -> "怀旧复古·古典"
                    type.contains("寺庙") || type.contains("temple", true) -> "禅意东方·极简"
                    type.contains("夜市") || type.contains("night", true) -> "夜市霓虹·现代"
                    type.contains("海") || type.contains("beach", true) -> "海岸清新·蓝调"
                    else -> "城市人文·大师"
                }

                places.add(
                    SerpApiPlace(
                        id = placeId,
                        name = name,
                        address = address,
                        latitude = lat,
                        longitude = lng,
                        rating = rating,
                        distance = distStr,
                        distanceKm = distKm,
                        category = type,
                        thumbnailUrl = thumbnail,
                        filterSuggestion = filterSuggestion
                    )
                )
            }

            Log.d(TAG, "Parsed ${places.size} places from SerpApi")
            places.sortedBy { it.distanceKm }
        } catch (e: Exception) {
            Log.e(TAG, "Parse failed", e)
            emptyList()
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).let { it * it } +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).let { it * it }
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}

/**
 * SerpApi 地点数据模型
 */
data class SerpApiPlace(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Float,
    val distance: String,
    val distanceKm: Double,
    val category: String,
    val thumbnailUrl: String,
    val filterSuggestion: String
)
