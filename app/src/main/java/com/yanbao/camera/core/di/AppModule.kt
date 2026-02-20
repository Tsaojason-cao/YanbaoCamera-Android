package com.yanbao.camera.core.di

import android.content.Context
import com.yanbao.camera.core.util.CameraManager
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
    fun provideCameraManager(): CameraManager {
        return CameraManager()
    }
}
