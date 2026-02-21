package com.yanbao.camera.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户数据实体（Room 数据库）
 * 
 * 核心字段：
 * - uid: 硬件指纹生成的唯一会员编号（YB-888888格式）
 * - userId: 用户自定义ID（如 @YanbaoMaster）
 * - avatar: 头像URL
 * - backgroundUrl: 背景图URL
 * - joinDate: 注册时间（用于计算与雁宝同行天数）
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String,                    // 硬件指纹UID（YB-888888）
    val userId: String = "",            // 用户自定义ID（@YanbaoMaster）
    val nickname: String = "",          // 昵称
    val avatar: String = "",            // 头像URL
    val backgroundUrl: String = "",     // 背景图URL
    val bio: String = "",               // 个人简介
    val joinDate: Long = System.currentTimeMillis(), // 注册时间戳
    val photoCount: Int = 0,            // 拍摄照片数
    val followerCount: Int = 0,         // 粉丝数
    val followingCount: Int = 0,        // 关注数
    val likeCount: Int = 0,             // 获赞数
    val lastLoginTime: Long = System.currentTimeMillis(), // 最后登录时间
    val isVip: Boolean = false,         // 是否VIP会员
    val vipExpireDate: Long? = null     // VIP过期时间
)
