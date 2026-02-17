package com.yanbao.camera.model

/**
 * 推荐流Post数据模型
 */
data class Post(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val imageUrl: String,
    val title: String,
    val description: String,
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = "",
    val tags: List<String> = emptyList(),
    val isLiked: Boolean = false
)

/**
 * 推荐位置卡片
 */
data class LocationCard(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rating: Float = 0f,
    val postCount: Int = 0
)
