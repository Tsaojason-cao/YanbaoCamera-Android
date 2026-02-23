package com.yanbao.camera.data.filter

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache

/**
 * 滤镜预览缓存管理器
 * 
 * 核心功能：
 * - 使用LruCache缓存91个滤镜的预览图
 * - 内存管理：最大缓存大小 = 可用内存的1/8
 * - 60fps性能保证：缓存命中时0ms加载
 * 
 * 技术实现：
 * - Key: filterId (1-91)
 * - Value: Bitmap (预览图)
 * - 缓存策略：LRU（最近最少使用）
 */
object FilterPreviewCache {
    
    // 获取可用内存（单位：KB）
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    
    // 缓存大小：可用内存的1/8
    private val cacheSize = maxMemory / 8
    
    // LruCache实例
    private val cache = object : LruCache<Int, Bitmap>(cacheSize) {
        override fun sizeOf(key: Int, bitmap: Bitmap): Int {
            // 返回Bitmap的大小（单位：KB）
            return bitmap.byteCount / 1024
        }
        
        override fun entryRemoved(
            evicted: Boolean,
            key: Int,
            oldValue: Bitmap,
            newValue: Bitmap?
        ) {
            if (evicted) {
                Log.d("FilterPreviewCache", "🗑️ 缓存淘汰: filterId=$key")
            }
        }
    }
    
    init {
        Log.d("FilterPreviewCache", """
            [OK] 滤镜预览缓存初始化完成
            - 最大内存: ${maxMemory}KB
            - 缓存大小: ${cacheSize}KB
            - 预计可缓存: ${cacheSize / 500}张预览图（假设每张500KB）
        """.trimIndent())
    }
    
    /**
     * 获取滤镜预览图
     * 
     * @param filterId 滤镜ID（1-91）
     * @return 预览图Bitmap，如果缓存未命中则返回null
     */
    fun get(filterId: Int): Bitmap? {
        val bitmap = cache.get(filterId)
        if (bitmap != null) {
            Log.d("FilterPreviewCache", "✅ 缓存命中: filterId=$filterId")
        } else {
            Log.d("FilterPreviewCache", "❌ 缓存未命中: filterId=$filterId")
        }
        return bitmap
    }
    
    /**
     * 存储滤镜预览图
     * 
     * @param filterId 滤镜ID（1-91）
     * @param bitmap 预览图Bitmap
     */
    fun put(filterId: Int, bitmap: Bitmap) {
        cache.put(filterId, bitmap)
        Log.d("FilterPreviewCache", """
            SAVE 缓存存储: filterId=$filterId
            - 当前缓存大小: ${cache.size()}KB / ${cacheSize}KB
            - 缓存命中率: ${getCacheHitRate()}%
        """.trimIndent())
    }
    
    /**
     * 预加载滤镜预览图
     * 
     * @param filterIds 需要预加载的滤镜ID列表
     * @param previewGenerator 预览图生成器
     */
    suspend fun preload(
        filterIds: List<Int>,
        previewGenerator: suspend (Int) -> Bitmap
    ) {
        Log.d("FilterPreviewCache", "🔄 开始预加载 ${filterIds.size} 个滤镜预览图...")
        
        var successCount = 0
        var failCount = 0
        
        filterIds.forEach { filterId ->
            try {
                if (get(filterId) == null) {
                    val bitmap = previewGenerator(filterId)
                    put(filterId, bitmap)
                    successCount++
                }
            } catch (e: Exception) {
                Log.e("FilterPreviewCache", "⚠️ 预加载失败: filterId=$filterId", e)
                failCount++
            }
        }
        
        Log.d("FilterPreviewCache", """
            [OK] 预加载完成
            - 成功: $successCount
            - 失败: $failCount
            - 当前缓存大小: ${cache.size()}KB / ${cacheSize}KB
        """.trimIndent())
    }
    
    /**
     * 清空缓存
     */
    fun clear() {
        cache.evictAll()
        Log.d("FilterPreviewCache", "🗑️ 缓存已清空")
    }
    
    /**
     * 获取缓存命中率
     */
    private fun getCacheHitRate(): Int {
        val hitCount = cache.hitCount()
        val missCount = cache.missCount()
        val totalCount = hitCount + missCount
        
        return if (totalCount > 0) {
            ((hitCount.toFloat() / totalCount) * 100).toInt()
        } else {
            0
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getStats(): CacheStats {
        return CacheStats(
            maxSize = cacheSize,
            currentSize = cache.size(),
            hitCount = cache.hitCount(),
            missCount = cache.missCount(),
            hitRate = getCacheHitRate()
        )
    }
}

/**
 * 缓存统计信息
 */
data class CacheStats(
    val maxSize: Int,
    val currentSize: Int,
    val hitCount: Int,
    val missCount: Int,
    val hitRate: Int
)
