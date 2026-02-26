package com.yanbao.camera.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yanbao.camera.data.local.dao.YanbaoGardenDao
import com.yanbao.camera.data.local.entity.YanbaoGardenFeedEntity
import com.yanbao.camera.data.local.entity.YanbaoGardenPrivilegeEntity

/**
 * 雁宝园地数据库
 *
 * 存储喂食记录与特权状态，独立于相机记忆数据库，
 * 便于后续功能扩展与数据迁移。
 */
@Database(
    entities = [
        YanbaoGardenFeedEntity::class,
        YanbaoGardenPrivilegeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class YanbaoGardenDatabase : RoomDatabase() {

    abstract fun gardenDao(): YanbaoGardenDao

    companion object {
        @Volatile
        private var INSTANCE: YanbaoGardenDatabase? = null

        fun getDatabase(context: Context): YanbaoGardenDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    YanbaoGardenDatabase::class.java,
                    "yanbao_garden_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
