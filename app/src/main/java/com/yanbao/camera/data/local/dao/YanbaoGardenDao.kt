package com.yanbao.camera.data.local.dao

import androidx.room.*
import com.yanbao.camera.data.local.entity.YanbaoGardenFeedEntity
import com.yanbao.camera.data.local.entity.YanbaoGardenPrivilegeEntity
import kotlinx.coroutines.flow.Flow

/**
 * 雁宝园地 DAO
 *
 * 负责喂食记录和特权状态的数据库操作
 */
@Dao
interface YanbaoGardenDao {

    // ─── 喂食记录 ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedRecord(record: YanbaoGardenFeedEntity): Long

    /** 获取指定日期字符串（yyyy-MM-dd）的所有喂食记录 */
    @Query("SELECT * FROM yanbao_garden_feed WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    suspend fun getFeedRecordsSince(startOfDay: Long): List<YanbaoGardenFeedEntity>

    /** 获取全部喂食记录（用于统计） */
    @Query("SELECT COUNT(*) FROM yanbao_garden_feed")
    fun getTotalFeedCountFlow(): Flow<Int>

    /** 获取最近 N 条喂食记录（用于动画历史展示） */
    @Query("SELECT * FROM yanbao_garden_feed ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentFeedRecords(limit: Int = 10): List<YanbaoGardenFeedEntity>

    // ─── 特权状态 ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrivilege(privilege: YanbaoGardenPrivilegeEntity)

    @Query("SELECT * FROM yanbao_garden_privilege WHERE id = 1")
    fun getPrivilegeFlow(): Flow<YanbaoGardenPrivilegeEntity?>

    @Query("SELECT * FROM yanbao_garden_privilege WHERE id = 1")
    suspend fun getPrivilege(): YanbaoGardenPrivilegeEntity?

    @Query("""
        UPDATE yanbao_garden_privilege 
        SET totalFeedCount = totalFeedCount + 1,
            totalPoints = totalPoints + :points,
            lastFeedTimestamp = :timestamp,
            todayNormalFeedCount = :todayNormal,
            todayShareBonusCount = :todayShare,
            todayDateStr = :dateStr
        WHERE id = 1
    """)
    suspend fun updateAfterFeed(
        points: Int,
        timestamp: Long,
        todayNormal: Int,
        todayShare: Int,
        dateStr: String
    )

    @Query("UPDATE yanbao_garden_privilege SET privilegeLevel = :level WHERE id = 1")
    suspend fun updatePrivilegeLevel(level: Int)

    @Query("UPDATE yanbao_garden_privilege SET isVipUnlocked = :unlocked WHERE id = 1")
    suspend fun setVipUnlocked(unlocked: Boolean)

    @Query("UPDATE yanbao_garden_privilege SET isWatermarkRemoved = :removed WHERE id = 1")
    suspend fun setWatermarkRemoved(removed: Boolean)

    @Query("UPDATE yanbao_garden_privilege SET isExclusiveStickerUnlocked = :unlocked WHERE id = 1")
    suspend fun setExclusiveStickerUnlocked(unlocked: Boolean)
}
