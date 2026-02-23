package com.yanbao.camera.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 记忆 DAO - Phase 1 简化版
 *
 * 提供对 memories 表的 CRUD 操作
 */
@Dao
interface MemoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: MemoryEntity): Long

    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Long): MemoryEntity?

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM memories WHERE locationLat IS NOT NULL AND locationLng IS NOT NULL")
    fun getMemoriesWithLocation(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE mode = :mode ORDER BY timestamp DESC")
    fun getMemoriesByMode(mode: String): Flow<List<MemoryEntity>>

    @Query("DELETE FROM memories")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM memories")
    suspend fun count(): Int
}
