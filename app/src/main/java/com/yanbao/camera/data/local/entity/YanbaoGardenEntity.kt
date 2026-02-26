package com.yanbao.camera.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 雁宝园地 — 喂食胡萝卜记录
 *
 * 每次喂食动作在此表中记录一条数据，用于：
 *  1. 统计当日已喂食次数（每日上限 3 次）
 *  2. 统计通过分享额外获得的次数
 *  3. 累计喂食总次数，用于解锁特权等级
 */
@Entity(tableName = "yanbao_garden_feed")
data class YanbaoGardenFeedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 喂食时间戳（毫秒） */
    val timestamp: Long = System.currentTimeMillis(),
    /** 喂食来源：NORMAL（普通）/ SHARE（分享奖励） */
    val source: String = FeedSource.NORMAL,
    /** 喂食后雁宝的反应：HAPPY / PLAYFUL / CARING / THINKING / COOL */
    val yanbaoReaction: String = "HAPPY",
    /** 当次喂食获得的积分 */
    val pointsEarned: Int = 10
) {
    object FeedSource {
        const val NORMAL = "NORMAL"
        const val SHARE = "SHARE"
    }
}

/**
 * 雁宝园地 — 用户特权状态
 *
 * 单行记录（id = 1），持久化用户在园地中的特权状态
 */
@Entity(tableName = "yanbao_garden_privilege")
data class YanbaoGardenPrivilegeEntity(
    @PrimaryKey val id: Int = 1,
    /** 当前特权等级：0=普通 1=银爪 2=金爪 3=钻石爪 */
    val privilegeLevel: Int = 0,
    /** 累计喂食总次数 */
    val totalFeedCount: Int = 0,
    /** 累计积分 */
    val totalPoints: Int = 0,
    /** 最后一次喂食时间戳 */
    val lastFeedTimestamp: Long = 0L,
    /** 今日已喂食次数（普通） */
    val todayNormalFeedCount: Int = 0,
    /** 今日已分享次数（分享奖励） */
    val todayShareBonusCount: Int = 0,
    /** 今日日期字符串（yyyy-MM-dd），用于跨日重置 */
    val todayDateStr: String = "",
    /** 是否已解锁 VIP 特权 */
    val isVipUnlocked: Boolean = false,
    /** 是否已解锁去水印特权 */
    val isWatermarkRemoved: Boolean = false,
    /** 是否已解锁独家贴纸包 */
    val isExclusiveStickerUnlocked: Boolean = false
)
