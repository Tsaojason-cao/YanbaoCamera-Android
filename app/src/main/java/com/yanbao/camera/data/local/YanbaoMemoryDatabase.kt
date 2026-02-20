package com.yanbao.camera.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yanbao.camera.data.local.dao.YanbaoMemoryDao
import com.yanbao.camera.data.local.entity.YanbaoMemory

/**
 * YanbaoMemory 数据库
 * 
 * Room Database 配置
 */
@Database(
    entities = [YanbaoMemory::class],
    version = 2,  // 升级到版本 2，支持自动迁移
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
                )
                // 核心：自动迁移策略
                // 开发阶段使用 fallbackToDestructiveMigration()
                // 生产阶段使用 addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()  // 开发阶段：数据库结构变更时自动删除重建
                .addMigrations(MIGRATION_1_2)      // 生产阶段：定义具体迁移路径
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 定义从版本 1 到 2 的迁移路径
         * 
         * 示例：如果未来需要增加字段，在此编写 SQL
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 示例：增加会员天数字段
                // database.execSQL("ALTER TABLE memories ADD COLUMN member_days INTEGER NOT NULL DEFAULT 0")
                
                // 当前版本：无需迁移操作（仅版本号升级）
                // 未来如需增加字段，在此处添加 SQL 语句
            }
        }
    }
}
