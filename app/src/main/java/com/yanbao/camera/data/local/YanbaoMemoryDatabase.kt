package com.yanbao.camera.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yanbao.camera.data.local.dao.YanbaoMemoryDao
import com.yanbao.camera.data.local.entity.YanbaoMemory

/**
 * YanbaoMemory 数据库
 * 
 * Room Database 配置
 */
@Database(
    entities = [YanbaoMemory::class],
    version = 1,
    exportSchema = false
)
abstract class YanbaoMemoryDatabase : RoomDatabase() {
    
    abstract fun yanbaoMemoryDao(): YanbaoMemoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: YanbaoMemoryDatabase? = null
        
        fun getDatabase(context: Context): YanbaoMemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YanbaoMemoryDatabase::class.java,
                    "yanbao_memory_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
