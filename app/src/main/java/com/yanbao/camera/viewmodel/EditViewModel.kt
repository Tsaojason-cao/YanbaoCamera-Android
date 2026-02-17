package com.yanbao.camera.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.model.CameraSettings
import com.yanbao.camera.model.FilterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 编辑ViewModel - 管理图片编辑状态
 */
class EditViewModel : ViewModel() {

    private val _editSettings = MutableStateFlow(CameraSettings())
    val editSettings: StateFlow<CameraSettings> = _editSettings

    private val _currentFilter = MutableStateFlow(FilterType.ORIGINAL)
    val currentFilter: StateFlow<FilterType> = _currentFilter

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    /**
     * 调整亮度
     */
    fun setBrightness(value: Float) {
        viewModelScope.launch {
            val current = _editSettings.value
            _editSettings.value = current.copy(brightness = value.coerceIn(-100f, 100f))
        }
    }

    /**
     * 调整对比度
     */
    fun setContrast(value: Float) {
        viewModelScope.launch {
            val current = _editSettings.value
            _editSettings.value = current.copy(contrast = value.coerceIn(-100f, 100f))
        }
    }

    /**
     * 调整饱和度
     */
    fun setSaturation(value: Float) {
        viewModelScope.launch {
            val current = _editSettings.value
            _editSettings.value = current.copy(saturation = value.coerceIn(-100f, 100f))
        }
    }

    /**
     * 调整色调
     */
    fun setHue(value: Float) {
        viewModelScope.launch {
            val current = _editSettings.value
            _editSettings.value = current.copy(hue = value.coerceIn(-180f, 180f))
        }
    }

    /**
     * 应用滤镜
     */
    fun applyFilter(filterType: FilterType, intensity: Float = 1f) {
        viewModelScope.launch {
            _isProcessing.value = true
            _currentFilter.value = filterType
            val current = _editSettings.value
            _editSettings.value = current.copy(
                filterName = filterType.name,
                filterIntensity = intensity.coerceIn(0f, 1f)
            )
            // 模拟处理延迟
            kotlinx.coroutines.delay(300)
            _isProcessing.value = false
        }
    }

    /**
     * 设置滤镜强度
     */
    fun setFilterIntensity(intensity: Float) {
        viewModelScope.launch {
            val current = _editSettings.value
            _editSettings.value = current.copy(filterIntensity = intensity.coerceIn(0f, 1f))
        }
    }

    /**
     * 重置所有编辑
     */
    fun resetEdits() {
        viewModelScope.launch {
            _editSettings.value = CameraSettings()
            _currentFilter.value = FilterType.ORIGINAL
        }
    }

    /**
     * 撤销上一步
     */
    fun undo() {
        // TODO: 实现撤销功能
    }

    /**
     * 重做
     */
    fun redo() {
        // TODO: 实现重做功能
    }

    /**
     * 保存编辑
     */
    fun saveEdit() {
        viewModelScope.launch {
            _isProcessing.value = true
            // TODO: 实现保存逻辑
            kotlinx.coroutines.delay(500)
            _isProcessing.value = false
        }
    }
}
