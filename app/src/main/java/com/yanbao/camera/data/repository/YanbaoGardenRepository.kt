package com.yanbao.camera.data.repository

import android.util.Log
import com.yanbao.camera.data.local.dao.YanbaoGardenDao
import com.yanbao.camera.data.local.entity.YanbaoGardenFeedEntity
import com.yanbao.camera.data.local.entity.YanbaoGardenPrivilegeEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 雁宝园地数据仓库
 *
 * 封装所有与园地相关的业务数据操作，严格遵循以下规则：
 *
 * ## 喂食规则（规格 v2）
 * - 每日固定普通喂食次数：**1 次**
 * - 通过分享可额外获得次数：**最多 2 次**
 * - 每日总上限：**3 次**（1 普通 + 2 分享）
 *
 * ## 特权等级
 * - 普通雁宝（0 次起）
 * - 银爪雁宝（累计 10 次）
 * - 金爪雁宝（累计 30 次）
 * - 钻石爪雁宝（累计 100 次）
 */
@Singleton
class YanbaoGardenRepository @Inject constructor(
    private val gardenDao: YanbaoGardenDao
) {
    companion object {
        private const val TAG = "YanbaoGardenRepo"

        /** 每日普通喂食上限（固定 1 次） */
        const val DAILY_NORMAL_FEED_LIMIT = 1

        /** 每日分享奖励上限（最多额外 2 次） */
        const val DAILY_SHARE_BONUS_LIMIT = 2

        /** 每日总喂食上限 = 1 + 2 = 3 次 */
        const val DAILY_TOTAL_LIMIT = DAILY_NORMAL_FEED_LIMIT + DAILY_SHARE_BONUS_LIMIT

        /** 特权等级阈值（累计喂食次数） */
        val PRIVILEGE_THRESHOLDS = listOf(
            0 to "普通雁宝",
            10 to "银爪雁宝",
            30 to "金爪雁宝",
            100 to "钻石爪雁宝"
        )

        /** 各等级解锁的特权 */
        val PRIVILEGE_UNLOCKS = mapOf(
            1 to listOf("专属粉色主题", "雁宝贴纸包 Vol.1"),
            2 to listOf("去水印导出", "独家滤镜 x5", "雁宝贴纸包 Vol.2"),
            3 to listOf("VIP 功能解锁", "所有独家滤镜", "雁宝贴纸包 Vol.3", "优先推荐")
        )
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /** 观察特权状态（响应式） */
    fun observePrivilege(): Flow<YanbaoGardenPrivilegeEntity?> = gardenDao.getPrivilegeFlow()

    /** 观察累计喂食次数（响应式） */
    fun observeTotalFeedCount(): Flow<Int> = gardenDao.getTotalFeedCountFlow()

    /**
     * 执行喂食动作
     *
     * @param source 喂食来源（NORMAL 或 SHARE）
     * @return FeedResult 喂食结果，包含成功/失败原因及雁宝反应
     */
    suspend fun feedCarrot(source: String): FeedResult {
        val today = dateFormat.format(Date())
        val privilege = gardenDao.getPrivilege() ?: createInitialPrivilege(today)

        // 跨日重置
        val (todayNormal, todayShare) = if (privilege.todayDateStr != today) {
            Log.d(TAG, "新的一天，重置喂食计数")
            0 to 0
        } else {
            privilege.todayNormalFeedCount to privilege.todayShareBonusCount
        }

        // 检查次数限制
        when (source) {
            YanbaoGardenFeedEntity.FeedSource.NORMAL -> {
                if (todayNormal >= DAILY_NORMAL_FEED_LIMIT) {
                    return FeedResult.LimitReached(
                        message = "雁宝今天已经吃过胡萝卜啦！分享给朋友可以再喂 ${(DAILY_SHARE_BONUS_LIMIT - todayShare).coerceAtLeast(0)} 次哦~",
                        remainingShareBonus = (DAILY_SHARE_BONUS_LIMIT - todayShare).coerceAtLeast(0)
                    )
                }
            }
            YanbaoGardenFeedEntity.FeedSource.SHARE -> {
                if (todayShare >= DAILY_SHARE_BONUS_LIMIT) {
                    return FeedResult.LimitReached(
                        message = "今天的分享奖励次数已用完，明天再来吧！",
                        remainingShareBonus = 0
                    )
                }
            }
        }

        // 计算积分
        val points = if (source == YanbaoGardenFeedEntity.FeedSource.SHARE) 20 else 10

        // 决定雁宝反应（根据累计次数和随机性）
        val reaction = selectYanbaoReaction(privilege.totalFeedCount)

        // 插入喂食记录
        gardenDao.insertFeedRecord(
            YanbaoGardenFeedEntity(
                timestamp = System.currentTimeMillis(),
                source = source,
                yanbaoReaction = reaction,
                pointsEarned = points
            )
        )

        // 更新特权状态
        val newTodayNormal = if (source == YanbaoGardenFeedEntity.FeedSource.NORMAL) todayNormal + 1 else todayNormal
        val newTodayShare = if (source == YanbaoGardenFeedEntity.FeedSource.SHARE) todayShare + 1 else todayShare

        gardenDao.updateAfterFeed(
            points = points,
            timestamp = System.currentTimeMillis(),
            todayNormal = newTodayNormal,
            todayShare = newTodayShare,
            dateStr = today
        )

        // 检查是否升级特权等级
        val newTotalFeed = privilege.totalFeedCount + 1
        val newLevel = calculatePrivilegeLevel(newTotalFeed)
        if (newLevel > privilege.privilegeLevel) {
            gardenDao.updatePrivilegeLevel(newLevel)
            if (newLevel >= 2) gardenDao.setWatermarkRemoved(true)
            if (newLevel >= 3) gardenDao.setVipUnlocked(true)
            gardenDao.setExclusiveStickerUnlocked(newLevel >= 1)

            return FeedResult.LevelUp(
                reaction = reaction,
                newLevel = newLevel,
                levelName = PRIVILEGE_THRESHOLDS[newLevel].second,
                unlockedPrivileges = PRIVILEGE_UNLOCKS[newLevel] ?: emptyList(),
                pointsEarned = points,
                remainingToday = DAILY_NORMAL_FEED_LIMIT - newTodayNormal,
                remainingShareBonus = DAILY_SHARE_BONUS_LIMIT - newTodayShare
            )
        }

        return FeedResult.Success(
            reaction = reaction,
            pointsEarned = points,
            totalPoints = privilege.totalPoints + points,
            remainingToday = DAILY_NORMAL_FEED_LIMIT - newTodayNormal,
            remainingShareBonus = DAILY_SHARE_BONUS_LIMIT - newTodayShare,
            progressToNextLevel = calculateProgressToNextLevel(newTotalFeed)
        )
    }

    /** 获取今日喂食状态 */
    suspend fun getTodayFeedStatus(): TodayFeedStatus {
        val today = dateFormat.format(Date())
        val privilege = gardenDao.getPrivilege() ?: createInitialPrivilege(today)

        val (todayNormal, todayShare) = if (privilege.todayDateStr != today) {
            0 to 0
        } else {
            privilege.todayNormalFeedCount to privilege.todayShareBonusCount
        }

        return TodayFeedStatus(
            normalFeedUsed = todayNormal,
            normalFeedLimit = DAILY_NORMAL_FEED_LIMIT,
            shareBonusUsed = todayShare,
            shareBonusLimit = DAILY_SHARE_BONUS_LIMIT,
            canFeedNormal = todayNormal < DAILY_NORMAL_FEED_LIMIT,
            canFeedByShare = todayShare < DAILY_SHARE_BONUS_LIMIT
        )
    }

    private suspend fun createInitialPrivilege(today: String): YanbaoGardenPrivilegeEntity {
        val initial = YanbaoGardenPrivilegeEntity(
            id = 1,
            todayDateStr = today
        )
        gardenDao.upsertPrivilege(initial)
        return initial
    }

    private fun selectYanbaoReaction(totalFeedCount: Int): String {
        // 根据累计喂食次数和随机性选择雁宝反应
        val reactions = when {
            totalFeedCount < 5 -> listOf("HAPPY", "HAPPY", "CARING")
            totalFeedCount < 20 -> listOf("HAPPY", "PLAYFUL", "CARING", "THINKING")
            else -> listOf("HAPPY", "PLAYFUL", "CARING", "THINKING", "COOL")
        }
        return reactions.random()
    }

    private fun calculatePrivilegeLevel(totalFeedCount: Int): Int {
        return when {
            totalFeedCount >= 100 -> 3
            totalFeedCount >= 30 -> 2
            totalFeedCount >= 10 -> 1
            else -> 0
        }
    }

    private fun calculateProgressToNextLevel(totalFeedCount: Int): Float {
        return when {
            totalFeedCount >= 100 -> 1f
            totalFeedCount >= 30 -> (totalFeedCount - 30f) / (100f - 30f)
            totalFeedCount >= 10 -> (totalFeedCount - 10f) / (30f - 10f)
            else -> totalFeedCount / 10f
        }
    }
}

// ─── 结果数据类 ──────────────────────────────────────────────────────────────

sealed class FeedResult {
    data class Success(
        val reaction: String,
        val pointsEarned: Int,
        val totalPoints: Int,
        val remainingToday: Int,
        val remainingShareBonus: Int,
        val progressToNextLevel: Float
    ) : FeedResult()

    data class LevelUp(
        val reaction: String,
        val newLevel: Int,
        val levelName: String,
        val unlockedPrivileges: List<String>,
        val pointsEarned: Int,
        val remainingToday: Int,
        val remainingShareBonus: Int
    ) : FeedResult()

    data class LimitReached(
        val message: String,
        val remainingShareBonus: Int
    ) : FeedResult()
}

data class TodayFeedStatus(
    val normalFeedUsed: Int,
    val normalFeedLimit: Int,
    val shareBonusUsed: Int,
    val shareBonusLimit: Int,
    val canFeedNormal: Boolean,
    val canFeedByShare: Boolean
)

// ─── 大师特权消耗接口（补全会员特权闭环）────────────────────────────────────

/**
 * 检查是否有大师滤镜特权
 *
 * 规则：特权等级 >= 1（银爪雁宝，累计喂食 10 次）即可使用大师滤镜。
 * VIP 等级（等级 3）无限次使用；其余等级每日消耗 1 次特权次数。
 *
 * @return MasterPrivilegeStatus 特权状态
 */
suspend fun checkMasterPrivilege(): MasterPrivilegeStatus {
    val privilege = gardenDao.getPrivilege()
    return when {
        privilege == null -> MasterPrivilegeStatus.NoPrivilege(
            message = "喂食雁宝 10 次即可解锁大师滤镜特权",
            totalFeedRequired = 10
        )
        privilege.isVipUnlocked -> MasterPrivilegeStatus.VipUnlimited(
            level = privilege.privilegeLevel,
            levelName = PRIVILEGE_THRESHOLDS.getOrNull(privilege.privilegeLevel)?.second ?: "钻石爪雁宝"
        )
        privilege.privilegeLevel >= 1 -> {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val usedToday = if (privilege.todayDateStr == today) privilege.masterFilterUsedToday else 0
            val dailyLimit = when (privilege.privilegeLevel) {
                1 -> 3   // 银爪：每日 3 次
                2 -> 10  // 金爪：每日 10 次
                else -> Int.MAX_VALUE
            }
            if (usedToday < dailyLimit) {
                MasterPrivilegeStatus.HasPrivilege(
                    level = privilege.privilegeLevel,
                    levelName = PRIVILEGE_THRESHOLDS.getOrNull(privilege.privilegeLevel)?.second ?: "银爪雁宝",
                    remainingToday = dailyLimit - usedToday,
                    dailyLimit = dailyLimit
                )
            } else {
                MasterPrivilegeStatus.DailyLimitReached(
                    message = "今日大师滤镜次数已用完，明天再来或喂食雁宝升级",
                    level = privilege.privilegeLevel
                )
            }
        }
        else -> MasterPrivilegeStatus.NoPrivilege(
            message = "累计喂食 ${10 - privilege.totalFeedCount} 次即可解锁大师滤镜",
            totalFeedRequired = 10 - privilege.totalFeedCount
        )
    }
}

/**
 * 消耗一次大师滤镜特权
 *
 * 调用前必须先调用 checkMasterPrivilege() 确认有权限。
 * VIP 用户无需消耗（isVipUnlocked = true 时直接返回成功）。
 *
 * @return true 消耗成功，false 消耗失败（无权限或已达上限）
 */
suspend fun consumeMasterPrivilege(): Boolean {
    val privilege = gardenDao.getPrivilege() ?: return false
    if (privilege.isVipUnlocked) return true  // VIP 无限次，不消耗
    if (privilege.privilegeLevel < 1) return false

    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val usedToday = if (privilege.todayDateStr == today) privilege.masterFilterUsedToday else 0
    val dailyLimit = when (privilege.privilegeLevel) {
        1 -> 3
        2 -> 10
        else -> Int.MAX_VALUE
    }
    if (usedToday >= dailyLimit) return false

    // 扣减次数
    gardenDao.incrementMasterFilterUsed(today)
    Log.d(TAG, "大师滤镜特权已消耗，今日已用 ${usedToday + 1}/$dailyLimit 次")
    return true
}

// ─── 大师特权状态密封类 ──────────────────────────────────────────────────────

sealed class MasterPrivilegeStatus {
    /** 有特权，可以使用大师滤镜 */
    data class HasPrivilege(
        val level: Int,
        val levelName: String,
        val remainingToday: Int,
        val dailyLimit: Int
    ) : MasterPrivilegeStatus()

    /** VIP 无限次使用 */
    data class VipUnlimited(
        val level: Int,
        val levelName: String
    ) : MasterPrivilegeStatus()

    /** 无特权（未达到银爪等级） */
    data class NoPrivilege(
        val message: String,
        val totalFeedRequired: Int
    ) : MasterPrivilegeStatus()

    /** 今日次数已达上限 */
    data class DailyLimitReached(
        val message: String,
        val level: Int
    ) : MasterPrivilegeStatus()
}
