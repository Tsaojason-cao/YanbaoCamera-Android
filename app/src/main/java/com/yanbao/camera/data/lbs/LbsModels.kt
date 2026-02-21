package com.yanbao.camera.data.lbs

import androidx.compose.ui.graphics.Color

/**
 * LBS 地理位置数据模型
 * 
 * 🚨 核心验收标准：
 * - 所有数据必须从 Supabase LBS 函数查询
 * - 距离必须实时计算（基于用户当前位置）
 * - 严禁使用 hardcode 的模拟数据
 */

/**
 * 热门拍摄地点
 */
data class HotLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val category: String,
    val photoCount: Int,
    val popularityScore: Float,
    val featuredPhotoUrl: String?,
    val distanceKm: Double, // 实时计算的距离
    val recommendedFilter: String? = null // 推荐的大师模式滤镜
)

/**
 * 附近照片
 */
data class NearbyPhoto(
    val id: String,
    val userId: String,
    val storagePath: String,
    val thumbnailPath: String?,
    val latitude: Double,
    val longitude: Double,
    val locationName: String?,
    val address: String?,
    val title: String?,
    val description: String?,
    val tags: List<String>,
    val viewCount: Int,
    val likeCount: Int,
    val isPublic: Boolean,
    val createdAt: Long,
    val distanceKm: Double // 实时计算的距离
)

/**
 * 地点统计信息
 */
data class LocationStatistics(
    val totalPhotos: Int,
    val uniqueUsers: Int,
    val avgLikesPerPhoto: Float,
    val mostPopularTime: String
)

/**
 * 用户当前位置
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 滤镜映射器
 * 
 * 根据地点名称自动推荐对应的大师模式滤镜
 */
object FilterMapper {
    private val filterMap = mapOf(
        "台北101" to "Master_001",
        "东京塔" to "Master_002",
        "巴黎铁塔" to "Master_003",
        "纽约时代广场" to "Master_004",
        "伦敦大本钟" to "Master_005"
    )

    fun getFilterForSpot(spotName: String): String? {
        return filterMap.entries.firstOrNull { (key, _) ->
            spotName.contains(key, ignoreCase = true)
        }?.value
    }

    fun getCategoryColor(category: String): Color {
        return when (category) {
            "城市地标" -> Color(0xFFEC4899)
            "自然风光" -> Color(0xFF10B981)
            "人文建筑" -> Color(0xFFA78BFA)
            "街头摄影" -> Color(0xFFF59E0B)
            else -> Color(0xFF6B7280)
        }
    }
}
