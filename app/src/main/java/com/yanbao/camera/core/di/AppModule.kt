package com.yanbao.camera.core.di

import android.content.Context
import com.yanbao.camera.core.util.Camera2Manager
import com.yanbao.camera.core.util.Camera2ManagerEnhanced
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
}
