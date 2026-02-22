package com.yanbao.camera.presentation.edit

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor() : ViewModel() {

    private val _selectedCategory = MutableStateFlow<ToolCategory>(ToolCategory.ADJUST)
    val selectedCategory: StateFlow<ToolCategory> = _selectedCategory.asStateFlow()

    private val _selectedTool = MutableStateFlow<EditTool?>(editTools.find { it.id == "brightness" })
    val selectedTool: StateFlow<EditTool?> = _selectedTool.asStateFlow()

    private val _showMemoryPanel = MutableStateFlow(false)
    val showMemoryPanel: StateFlow<Boolean> = _showMemoryPanel.asStateFlow()

    // 调整参数
    private val _brightness = MutableStateFlow(0.5f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _contrast = MutableStateFlow(0.5f)
    val contrast: StateFlow<Float> = _contrast.asStateFlow()

    private val _saturation = MutableStateFlow(0.5f)
    val saturation: StateFlow<Float> = _saturation.asStateFlow()

    private val _temperature = MutableStateFlow(0.5f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _sharpness = MutableStateFlow(0.5f)
    val sharpness: StateFlow<Float> = _sharpness.asStateFlow()

    // 滤镜强度
    private val _filterIntensity = MutableStateFlow(0.75f)
    val filterIntensity: StateFlow<Float> = _filterIntensity.asStateFlow()

    private val _selectedFilterCountry = MutableStateFlow("KR")
    val selectedFilterCountry: StateFlow<String> = _selectedFilterCountry.asStateFlow()

    fun selectCategory(category: ToolCategory) {
        _selectedCategory.value = category
        val firstTool = editTools.find { it.category == category }
        firstTool?.let { _selectedTool.value = it }
    }

    fun selectTool(tool: EditTool) {
        _selectedTool.value = tool
    }

    fun setBrightness(value: Float) { _brightness.value = value }
    fun setContrast(value: Float) { _contrast.value = value }
    fun setSaturation(value: Float) { _saturation.value = value }
    fun setTemperature(value: Float) { _temperature.value = value }
    fun setSharpness(value: Float) { _sharpness.value = value }
    fun setFilterIntensity(value: Float) { _filterIntensity.value = value }
    fun setFilterCountry(country: String) { _selectedFilterCountry.value = country }

    fun toggleMemoryPanel() {
        _showMemoryPanel.value = !_showMemoryPanel.value
    }

    fun undo() {
        // TODO: 实现撤销历史栈
    }

    fun redo() {
        // TODO: 实现重做历史栈
    }

    fun save() {
        // TODO: 保存图片到相册并写入雁宝记忆
    }

    fun applyMemory(memoryId: String) {
        // TODO: 从Room数据库读取记忆参数并应用
        _showMemoryPanel.value = false
    }
}
