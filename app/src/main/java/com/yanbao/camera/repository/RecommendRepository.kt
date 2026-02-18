package com.yanbao.camera.repository

import android.util.Log
import com.yanbao.camera.api.NetworkModule
import com.yanbao.camera.api.PagedResponse
import com.yanbao.camera.model.LocationCard
import com.yanbao.camera.model.Post

/**
 * 推荐Repository - 处理推荐数据获取
 */
class RecommendRepository {
    
    private val api = NetworkModule.apiService
    
    /**
     * 获取推荐流
     */
    suspend fun getRecommendedPosts(page: Int, size: Int = 10): Result<PagedResponse<Post>> {
        return try {
            val response = api.getRecommendedPosts(page, size)
            if (response.code == 200) {
                Log.d(TAG, "获取推荐流成功: page=$page, size=${response.data.content.size}")
                Result.success(response.data)
            } else {
                Log.e(TAG, "获取推荐流失败: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取推荐流异常", e)
            // 返回Mock数据作为降级方案
            Result.success(getMockPosts(page, size))
        }
    }
    
    /**
     * 获取推荐位置
     */
    suspend fun getRecommendedLocations(
        category: String? = null,
        page: Int = 0,
        size: Int = 10
    ): Result<PagedResponse<LocationCard>> {
        return try {
            val response = api.getRecommendedLocations(category, page, size)
            if (response.code == 200) {
                Log.d(TAG, "获取推荐位置成功: page=$page, size=${response.data.content.size}")
                Result.success(response.data)
            } else {
                Log.e(TAG, "获取推荐位置失败: ${response.message}")
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取推荐位置异常", e)
            // 返回Mock数据作为降级方案
            Result.success(getMockLocations(page, size))
        }
    }
    
    /**
     * Mock推荐流数据 - 用于测试和降级
     */
    private fun getMockPosts(page: Int, size: Int): PagedResponse<Post> {
        val posts = mutableListOf<Post>()
        repeat(size) { index ->
            posts.add(
                Post(
                    id = "post_${page * size + index}",
                    userId = "user_${index % 5}",
                    userName = "用户${index % 5}",
                    userAvatar = "https://via.placeholder.com/50",
                    imageUrl = "https://via.placeholder.com/300x400",
                    title = "推荐作品 ${page * size + index + 1}",
                    description = "这是一个精美的推荐作品，展示了雁宝AI相机的强大功能",
                    likes = (Math.random() * 1000).toInt(),
                    comments = (Math.random() * 100).toInt(),
                    shares = (Math.random() * 50).toInt(),
                    location = "北京市朝阳区"
                )
            )
        }
        return PagedResponse(
            content = posts,
            totalPages = 10,
            totalElements = 100,
            size = size,
            number = page
        )
    }
    
    /**
     * Mock推荐位置数据 - 用于测试和降级
     */
    private fun getMockLocations(page: Int, size: Int): PagedResponse<LocationCard> {
        val locations = mutableListOf<LocationCard>()
        repeat(size) { index ->
            locations.add(
                LocationCard(
                    id = "location_${page * size + index}",
                    name = "景点${page * size + index + 1}",
                    description = "这是一个美丽的推荐景点",
                    imageUrl = "https://via.placeholder.com/300x200",
                    latitude = 39.9 + Math.random() * 0.1,
                    longitude = 116.4 + Math.random() * 0.1,
                    rating = (3.5 + Math.random() * 1.5).toFloat(),
                    postCount = (Math.random() * 100).toInt()
                )
            )
        }
        return PagedResponse(
            content = locations,
            totalPages = 10,
            totalElements = 100,
            size = size,
            number = page
        )
    }
    
    companion object {
        private const val TAG = "RecommendRepository"
    }
}
