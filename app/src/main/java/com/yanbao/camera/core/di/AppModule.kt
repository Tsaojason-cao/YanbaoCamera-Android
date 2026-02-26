package com.yanbao.camera.core.di

import android.content.Context
import com.yanbao.camera.core.util.Camera2Manager
import com.yanbao.camera.core.util.Camera2ManagerEnhanced
import com.yanbao.camera.data.local.YanbaoGardenDatabase
import com.yanbao.camera.data.local.YanbaoMemoryDatabase
import com.yanbao.camera.data.local.dao.YanbaoGardenDao
import com.yanbao.camera.data.local.dao.YanbaoMemoryDao
import com.yanbao.camera.data.repository.YanbaoGardenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCamera2Manager(@ApplicationContext context: Context): Camera2Manager {
        return Camera2Manager(context)
    }

    @Provides
    @Singleton
    fun provideCamera2ManagerEnhanced(@ApplicationContext context: Context): Camera2ManagerEnhanced {
        return Camera2ManagerEnhanced(context)
    }

    // ─── YanbaoMemory 数据库 ────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideYanbaoMemoryDatabase(@ApplicationContext context: Context): YanbaoMemoryDatabase {
        return YanbaoMemoryDatabase.getDatabase(context)
    }

    @Provides
    fun provideYanbaoMemoryDao(database: YanbaoMemoryDatabase): YanbaoMemoryDao {
        return database.yanbaoMemoryDao()
    }

    // ─── 雁宝园地数据库 ─────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideYanbaoGardenDatabase(@ApplicationContext context: Context): YanbaoGardenDatabase {
        return YanbaoGardenDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideYanbaoGardenDao(database: YanbaoGardenDatabase): YanbaoGardenDao {
        return database.gardenDao()
    }

    @Provides
    @Singleton
    fun provideYanbaoGardenRepository(dao: YanbaoGardenDao): YanbaoGardenRepository {
        return YanbaoGardenRepository(dao)
    }
}
