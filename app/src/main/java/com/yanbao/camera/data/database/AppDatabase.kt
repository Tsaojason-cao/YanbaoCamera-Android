package com.yanbao.camera.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * App 数据库 - Phase 1 简化版
 *
 * 包含 memories 表，存储拍照时的参数快照
 *
 * 注意：项目主数据库为 YanbaoMemoryDatabase（yanbao_memory_table），
 * 本数据库（memories 表）作为补充，存储更完整的参数快照。
 */
@Database(entities = [MemoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yanbao_memory_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
