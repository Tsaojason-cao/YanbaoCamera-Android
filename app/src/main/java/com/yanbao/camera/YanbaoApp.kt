package com.yanbao.camera

import android.app.Application
import com.yanbao.camera.optimization.AppInitializationOptimization
import com.yanbao.camera.optimization.ImageLoadingOptimization
import com.yanbao.camera.optimization.PerformanceMonitor
import com.yanbao.camera.optimization.ResourceReleaseManager

/**
 * 应用主类
 * 
 * 集成性能优化：
 * 1. 应用初始化优化
 * 2. 图片加载优化
 * 3. 资源释放管理
 * 4. 性能监控
 */
class YanbaoApp : Application() {
    
    companion object {
        private lateinit var instance: YanbaoApp
        
        fun getInstance(): YanbaoApp = instance
    }
    
    private lateinit var resourceReleaseManager: ResourceReleaseManager
    private lateinit var performanceMonitor: PerformanceMonitor
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 初始化性能监控
        performanceMonitor = PerformanceMonitor()
        
        // 初始化资源释放管理器
        resourceReleaseManager = ResourceReleaseManager(this)
        
        // 优化应用初始化
        val initOptimization = AppInitializationOptimization(this)
        initOptimization.optimizeInitialization()
        
        // 记录启动时间
        performanceMonitor.recordStartupTime()
        
        // 注册低内存监听
        registerComponentCallbacks(object : android.content.ComponentCallbacks2 {
            override fun onTrimMemory(level: Int) {
                if (level == android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
                    resourceReleaseManager.onLowMemory()
                }
            }
            
            override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {}
            override fun onLowMemory() {
                resourceReleaseManager.onLowMemory()
            }
        })
    }
    
    /**
     * 获取资源释放管理器
     */
    fun getResourceReleaseManager(): ResourceReleaseManager = resourceReleaseManager
    
    /**
     * 获取性能监控
     */
    fun getPerformanceMonitor(): PerformanceMonitor = performanceMonitor
}
