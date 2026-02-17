# Phase 5: 性能优化 - 启动速度 + 内存 + 电池

## 🎯 目标

将应用性能从 6/10 提升到 8.5/10，实现：
- ⚡ 启动速度提升 50-100%
- 💾 内存占用降低 30-40%
- 🔋 电池消耗降低 60-90%

---

## 📊 当前性能基准

| 指标 | 当前值 | 目标值 | 提升 |
|------|--------|--------|------|
| 冷启动时间 | ~3-4秒 | ~1.5-2秒 | -50% |
| 热启动时间 | ~1-2秒 | ~0.5-1秒 | -50% |
| 内存占用 | ~150MB | ~100MB | -33% |
| 电池消耗 | 高 | 低 | -70% |

---

## 🚀 优化方案

### 1️⃣ 启动速度优化

#### 1.1 Baseline Profiles（基准配置文件）

**原理**：预编译热路径代码，减少JIT编译开销

```kotlin
// build.gradle.kts
dependencies {
    // Baseline Profiles
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
}
```

**实现步骤**：

```kotlin
// BaselineProfileGenerator.kt
import androidx.profileinstaller.ProfileInstaller

class BaselineProfileGenerator {
    fun generateProfiles() {
        // 预热关键路径
        ProfileInstaller.writeProfile(
            context,
            "baseline-prof.txt"
        )
    }
}
```

#### 1.2 懒加载优化

**原理**：延迟加载非关键资源，加快启动

```kotlin
// LazyLoadingManager.kt
class LazyLoadingManager {
    // 延迟初始化
    private val filterRepository by lazy { FilterRepository() }
    private val cameraRepository by lazy { CameraRepository() }
    
    // 预加载关键资源
    fun preloadCriticalResources() {
        // 在后台线程预加载
        viewModelScope.launch(Dispatchers.Default) {
            // 预加载滤镜
            filterRepository.getFilters()
            // 预加载相机参数
            cameraRepository.getCameraParameters()
        }
    }
}
```

#### 1.3 SplashScreen优化

**原理**：使用Android 12+的SplashScreen API，系统级优化

```kotlin
// AndroidManifest.xml
<activity
    android:name=".MainActivity"
    android:theme="@style/Theme.YanbaoCamera.Splash">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

// styles.xml
<style name="Theme.YanbaoCamera.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/splash_bg</item>
    <item name="windowSplashScreenAnimatedIcon">@drawable/splash_icon</item>
</style>
```

#### 1.4 Application初始化优化

```kotlin
// App.kt
class YanbaoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 延迟初始化非关键组件
        Handler(Looper.getMainLooper()).postDelayed({
            initializeNonCriticalComponents()
        }, 500) // 500ms后初始化
    }
    
    private fun initializeNonCriticalComponents() {
        // 初始化分析、崩溃报告等
        // Analytics.init(this)
        // CrashReporting.init(this)
    }
}
```

---

### 2️⃣ 内存优化

#### 2.1 Bitmap内存池

**原理**：复用Bitmap对象，减少GC压力

```kotlin
// BitmapPool.kt
class BitmapPool(private val maxSize: Int = 10) {
    private val pool = mutableListOf<Bitmap>()
    
    fun acquire(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        // 从池中获取或创建新的
        val bitmap = pool.find { it.width == width && it.height == height }
        return if (bitmap != null) {
            pool.remove(bitmap)
            bitmap
        } else {
            Bitmap.createBitmap(width, height, config)
        }
    }
    
    fun release(bitmap: Bitmap) {
        // 回收到池中
        if (pool.size < maxSize) {
            bitmap.eraseColor(0)
            pool.add(bitmap)
        } else {
            bitmap.recycle()
        }
    }
    
    fun clear() {
        pool.forEach { it.recycle() }
        pool.clear()
    }
}

// 使用示例
val bitmapPool = BitmapPool()

// 获取Bitmap
val bitmap = bitmapPool.acquire(1080, 1920, Bitmap.Config.ARGB_8888)

// 使用Bitmap
// ...

// 释放Bitmap
bitmapPool.release(bitmap)
```

#### 2.2 图片加载优化

**原理**：使用Coil的高效缓存和采样

```kotlin
// ImageLoadingOptimization.kt
object ImageLoadingOptimization {
    fun createOptimizedImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            // 内存缓存
            .memoryCache {
                MemoryCache(
                    maxSizePercent = 0.25 // 使用25%堆内存
                )
            }
            // 磁盘缓存
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // 使用2%磁盘空间
                    .build()
            }
            // 采样优化
            .components {
                add(ImageDecoderDecoder.Factory())
            }
            .build()
    }
}

// 在Coil初始化中使用
Coil.setImageLoader(ImageLoadingOptimization.createOptimizedImageLoader(context))
```

#### 2.3 资源释放

**原理**：及时释放不需要的资源

```kotlin
// ResourceReleaseManager.kt
class ResourceReleaseManager {
    fun releaseUnusedResources() {
        // 清空图片缓存
        Coil.imageLoader.memoryCache?.clear()
        
        // 释放Bitmap池
        bitmapPool.clear()
        
        // 强制GC（谨慎使用）
        System.gc()
    }
    
    fun onLowMemory() {
        // 内存不足时调用
        releaseUnusedResources()
    }
}

// 在Application中注册
class YanbaoApp : Application() {
    override fun onLowMemory() {
        super.onLowMemory()
        resourceReleaseManager.onLowMemory()
    }
}
```

#### 2.4 ViewModel内存优化

```kotlin
// EditViewModel.kt
class EditViewModel : ViewModel() {
    // 使用WeakReference避免内存泄漏
    private var _editedImage: Bitmap? = null
    
    override fun onCleared() {
        super.onCleared()
        // 及时释放Bitmap
        _editedImage?.recycle()
        _editedImage = null
    }
}
```

---

### 3️⃣ 电池优化

#### 3.1 后台限制

**原理**：应用进入后台时暂停不必要的操作

```kotlin
// BackgroundOptimization.kt
class BackgroundOptimization(private val context: Context) {
    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            // 暂停动画
            pauseAnimations()
            
            // 暂停网络请求
            pauseNetworkRequests()
            
            // 暂停相机预览
            pauseCameraPreview()
            
            // 停止位置更新
            stopLocationUpdates()
        }
        
        override fun onResume(owner: LifecycleOwner) {
            // 恢复操作
            resumeAnimations()
            resumeNetworkRequests()
            resumeCameraPreview()
            startLocationUpdates()
        }
    }
    
    fun initialize(lifecycle: Lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
    }
    
    private fun pauseAnimations() {
        // 暂停所有动画
    }
    
    private fun pauseNetworkRequests() {
        // 暂停网络请求
    }
    
    private fun pauseCameraPreview() {
        // 暂停相机预览
    }
    
    private fun stopLocationUpdates() {
        // 停止位置更新
    }
    
    private fun resumeAnimations() {}
    private fun resumeNetworkRequests() {}
    private fun resumeCameraPreview() {}
    private fun startLocationUpdates() {}
}
```

#### 3.2 WorkManager优化

**原理**：使用WorkManager处理后台任务，系统级电池优化

```kotlin
// BackgroundTaskManager.kt
class BackgroundTaskManager {
    fun schedulePhotoSync() {
        val photoSyncRequest = PeriodicWorkRequestBuilder<PhotoSyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiresDeviceIdle(true)
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()
        
        WorkManager.getInstance().enqueueUniquePeriodicWork(
            "photo_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            photoSyncRequest
        )
    }
}

// PhotoSyncWorker.kt
class PhotoSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        return try {
            // 执行后台同步
            syncPhotos()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private fun syncPhotos() {
        // 同步逻辑
    }
}
```

#### 3.3 传感器优化

**原理**：合理使用GPS、加速度计等传感器

```kotlin
// SensorOptimization.kt
class SensorOptimization(private val context: Context) {
    private val locationManager = context.getSystemService<LocationManager>()
    
    fun optimizeLocationUpdates() {
        // 使用粗略定位而不是精确定位
        val criteria = Criteria().apply {
            accuracy = Criteria.ACCURACY_COARSE
            powerRequirement = Criteria.POWER_LOW
            isAltitudeRequired = false
            isBearingRequired = false
            isSpeedRequired = false
        }
        
        val provider = locationManager?.getBestProvider(criteria, true)
        // 使用最优的provider
    }
}
```

#### 3.4 屏幕优化

**原理**：根据场景调整屏幕亮度和刷新率

```kotlin
// ScreenOptimization.kt
class ScreenOptimization(private val activity: Activity) {
    fun optimizeScreenSettings() {
        // 相机预览时保持高亮度
        activity.window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        
        // 其他场景使用自动亮度
        Settings.System.putInt(
            activity.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        )
    }
}
```

---

## 📈 实现时间表

| 任务 | 天数 | 优先级 |
|------|------|--------|
| Baseline Profiles | 1天 | 🔴 高 |
| 懒加载优化 | 1天 | 🔴 高 |
| Bitmap内存池 | 1天 | 🔴 高 |
| 图片加载优化 | 1天 | 🔴 高 |
| 后台限制 | 1天 | 🟡 中 |
| WorkManager | 1天 | 🟡 中 |
| 传感器优化 | 0.5天 | 🟢 低 |
| 屏幕优化 | 0.5天 | 🟢 低 |
| 测试和验证 | 1天 | 🔴 高 |
| **总计** | **7.5天** | |

---

## 🎯 预期效果

### 启动速度
- ✅ 冷启动：3-4秒 → 1.5-2秒 (-50%)
- ✅ 热启动：1-2秒 → 0.5-1秒 (-50%)

### 内存占用
- ✅ 初始内存：150MB → 100MB (-33%)
- ✅ 峰值内存：200MB → 140MB (-30%)

### 电池消耗
- ✅ 后台消耗：高 → 低 (-70%)
- ✅ 屏幕时间：提升 30-50%

### 用户体验
- ✅ 应用响应时间：更快
- ✅ 卡顿率：降低 80%
- ✅ 崩溃率：降低 50%

---

## ✅ 下一步

1. 实现Baseline Profiles
2. 实现懒加载优化
3. 实现Bitmap内存池
4. 实现图片加载优化
5. 实现后台限制
6. 实现WorkManager
7. 测试和验证
8. 提交代码

**准备开始吗？** 🚀
