package com.yanbao.camera.data.local.dao

import androidx.room.*
import com.yanbao.camera.data.local.entity.YanbaoMemory
import kotlinx.coroutines.flow.Flow

/**
 * YanbaoMemory DAO
 * 
 * 数据库操作接口
 */
@Dao
interface YanbaoMemoryDao {
    /**
     * 插入一条雁宝记忆
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: YanbaoMemory): Long
    
    /**
     * 获取所有雁宝记忆（按时间倒序）
     */
    @Query("SELECT * FROM yanbao_memory_table ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<YanbaoMemory>>
    
    /**
     * 根据 ID 获取雁宝记忆
     */
    @Query("SELECT * FROM yanbao_memory_table WHERE id = :id")
    suspend fun getMemoryById(id: Long): YanbaoMemory?
    
    /**
     * 根据拍摄模式获取雁宝记忆
     */
    @Query("SELECT * FROM yanbao_memory_table WHERE shootingMode = :mode ORDER BY timestamp DESC")
    fun getMemoriesByMode(mode: String): Flow<List<YanbaoMemory>>
    
    /**
     * 删除一条雁宝记忆
     */
    @Delete
    suspend fun delete(memory: YanbaoMemory)
    
    /**
     * 清空所有雁宝记忆
     */
    @Query("DELETE FROM yanbao_memory_table")
    suspend fun deleteAll()
}
