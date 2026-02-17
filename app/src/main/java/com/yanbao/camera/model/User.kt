package com.yanbao.camera.model

/**
 * 用户数据模型
 */
data class User(
    val id: String,
    val name: String,
    val avatar: String,
    val bio: String = "",
    val followers: Int = 0,
    val following: Int = 0,
    val postCount: Int = 0,
    val likeCount: Int = 0,
    val isFollowed: Boolean = false,
    val joinDate: Long = System.currentTimeMillis()
)

/**
 * 用户统计信息
 */
data class UserStats(
    val totalPhotos: Int = 0,
    val totalLikes: Int = 0,
    val totalViews: Int = 0,
    val totalComments: Int = 0,
    val favoriteCount: Int = 0
)
