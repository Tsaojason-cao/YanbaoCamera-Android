package com.yanbao.camera

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 雁宝AI相机 Application 入口
 * 使用 Hilt 进行依赖注入
 */
@HiltAndroidApp
class YanbaoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: YanbaoApp
            private set
    }
}
