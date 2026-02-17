package com.yanbao.camera.repository

import com.yanbao.camera.model.Filter
import com.yanbao.camera.model.FilterCategory
import com.yanbao.camera.model.FilterPresets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 滤镜数据仓库
 * 
 * 管理所有可用的滤镜和当前选中的滤镜
 */
class FilterRepository {
    
    private val _filters = MutableStateFlow<List<Filter>>(FilterPresets.filters)
    val filters: StateFlow<List<Filter>> = _filters.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow<Filter>(FilterPresets.filters.first())
    val selectedFilter: StateFlow<Filter> = _selectedFilter.asStateFlow()
    
    private val _filterIntensity = MutableStateFlow(1.0f)
    val filterIntensity: StateFlow<Float> = _filterIntensity.asStateFlow()
    
    /**
     * 获取所有滤镜
     */
    fun getAllFilters(): List<Filter> = _filters.value
    
    /**
     * 按分类获取滤镜
     */
    fun getFiltersByCategory(category: FilterCategory): List<Filter> {
        return _filters.value.filter { it.category == category }
    }
    
    /**
     * 选择滤镜
     */
    fun selectFilter(filter: Filter) {
        _selectedFilter.value = filter
        _filterIntensity.value = 1.0f
    }
    
    /**
     * 设置滤镜强度
     */
    fun setFilterIntensity(intensity: Float) {
        _filterIntensity.value = intensity.coerceIn(0f, 1f)
    }
    
    /**
     * 获取当前选中的滤镜
     */
    fun getCurrentFilter(): Filter = _selectedFilter.value
    
    /**
     * 获取当前滤镜强度
     */
    fun getCurrentIntensity(): Float = _filterIntensity.value
    
    /**
     * 重置滤镜
     */
    fun resetFilter() {
        _selectedFilter.value = FilterPresets.filters.first()
        _filterIntensity.value = 1.0f
    }
}
