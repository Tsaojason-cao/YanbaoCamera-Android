package com.yanbao.camera.presentation.profile

data class UserProfile(
    val id: String = "88888",
    val nickname: String = "Yanbao Creator",
    val avatarUrl: String? = null,
    val backgroundUrl: String? = null,
    val memberLevel: String = "黄金会员",
    val memberDays: Int = 365,
    val worksCount: Int = 2500,
    val followersCount: Int = 5200,
    val followingCount: Int = 320
)
