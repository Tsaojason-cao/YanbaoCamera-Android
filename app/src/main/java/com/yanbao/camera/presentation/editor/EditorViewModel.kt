package com.yanbao.camera.presentation.editor

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.render.EditParams
import com.yanbao.camera.core.render.GLRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 編輯器 ViewModel
 * 
 * 管理 18 個編輯工具的參數和實時預覽
 */
class EditorViewModel : ViewModel() {
    
    // 當前編輯的圖像
    private val _sourceBitmap = MutableStateFlow<Bitmap?>(null)
    val sourceBitmap: StateFlow<Bitmap?> = _sourceBitmap.asStateFlow()
    
    // 當前編輯參數
    private val _editParams = MutableStateFlow(EditParams())
    val editParams: StateFlow<EditParams> = _editParams.asStateFlow()
    
    // 當前選中的工具
    private val _selectedTool = MutableStateFlow(EditTool.BRIGHTNESS)
    val selectedTool: StateFlow<EditTool> = _selectedTool.asStateFlow()
    
    // 預覽圖像
    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()
    
    // 是否正在處理
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    /**
     * 設置源圖像
     */
    fun setSourceBitmap(bitmap: Bitmap) {
        _sourceBitmap.value = bitmap
        _previewBitmap.value = bitmap
    }
    
    /**
     * 選擇工具
     */
    fun selectTool(tool: EditTool) {
        _selectedTool.value = tool
    }
    
    /**
     * 更新參數
     */
    fun updateParam(tool: EditTool, value: Float) {
        val params = _editParams.value.copy()
        
        when (tool) {
            // 基礎調節
            EditTool.BRIGHTNESS -> params.brightness = value
            EditTool.CONTRAST -> params.contrast = value
            EditTool.SATURATION -> params.saturation = value
            EditTool.SHARPEN -> params.sharpen = value
            
            // 色彩進階
            EditTool.TEMPERATURE -> params.temperature = value
            EditTool.TINT -> params.tint = value
            EditTool.HIGHLIGHTS -> params.highlights = value
            EditTool.SHADOWS -> params.shadows = value
            
            // 光影特效
            EditTool.EXPOSURE -> params.exposure = value
            EditTool.GRAIN -> params.grain = value
            EditTool.VIGNETTE -> params.vignette = value
            EditTool.FADE -> params.fade = value
            
            // 人像/AI
            EditTool.SMOOTHING -> params.smoothing = value
            EditTool.WHITENING -> params.whitening = value
            EditTool.SPOT_REMOVAL -> params.spotRemoval = value
            
            // 29D 專用
            EditTool.CHROMATIC_RGB -> params.chromaticRGB = value
            EditTool.DEPTH_OF_FIELD -> params.depthOfField = value
            EditTool.DYNAMIC_RANGE -> params.dynamicRange = value
        }
        
        _editParams.value = params
    }
    
    /**
     * 應用編輯
     */
    fun applyEdit(renderer: GLRenderer) {
        viewModelScope.launch {
            _isProcessing.value = true
            
            try {
                val source = _sourceBitmap.value
                if (source != null) {
                    renderer.updateParams(_editParams.value)
                    val result = renderer.render(source)
                    _previewBitmap.value = result
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * 重置參數
     */
    fun resetParams() {
        _editParams.value = EditParams()
        _previewBitmap.value = _sourceBitmap.value
    }
    
    /**
     * 重置單個工具的參數
     */
    fun resetToolParam(tool: EditTool) {
        updateParam(tool, getDefaultValue(tool))
    }
    
    /**
     * 獲取工具的默認值
     */
    private fun getDefaultValue(tool: EditTool): Float {
        return when (tool) {
            EditTool.BRIGHTNESS, EditTool.TEMPERATURE, EditTool.TINT,
            EditTool.HIGHLIGHTS, EditTool.SHADOWS, EditTool.EXPOSURE -> 0f
            
            EditTool.CONTRAST, EditTool.SATURATION -> 1f
            
            else -> 0f
        }
    }
}

/**
 * 編輯工具枚舉
 */
enum class EditTool(val displayName: String, val minValue: Float, val maxValue: Float) {
    // 基礎調節
    BRIGHTNESS("亮度", -1f, 1f),
    CONTRAST("對比度", 0f, 2f),
    SATURATION("飽和度", 0f, 2f),
    SHARPEN("銳化", 0f, 1f),
    
    // 色彩進階
    TEMPERATURE("色溫", -1f, 1f),
    TINT("色調", -1f, 1f),
    HIGHLIGHTS("高光", -1f, 1f),
    SHADOWS("陰影", -1f, 1f),
    
    // 光影特效
    EXPOSURE("曝光", -2f, 2f),
    GRAIN("顆粒", 0f, 1f),
    VIGNETTE("晕影", 0f, 1f),
    FADE("褪色", 0f, 1f),
    
    // 人像/AI
    SMOOTHING("磨皮", 0f, 1f),
    WHITENING("美白", 0f, 1f),
    SPOT_REMOVAL("祛斑", 0f, 1f),
    
    // 29D 專用
    CHROMATIC_RGB("29D 色散", 0f, 1f),
    DEPTH_OF_FIELD("2.9D 景深", 0f, 1f),
    DYNAMIC_RANGE("動態範圍", 0f, 1f);
    
    /**
     * 獲取工具分類
     */
    fun getCategory(): ToolCategory {
        return when (this) {
            BRIGHTNESS, CONTRAST, SATURATION, SHARPEN -> ToolCategory.BASIC
            TEMPERATURE, TINT, HIGHLIGHTS, SHADOWS -> ToolCategory.COLOR
            EXPOSURE, GRAIN, VIGNETTE, FADE -> ToolCategory.EFFECT
            SMOOTHING, WHITENING, SPOT_REMOVAL -> ToolCategory.PORTRAIT
            CHROMATIC_RGB, DEPTH_OF_FIELD, DYNAMIC_RANGE -> ToolCategory.ADVANCED_29D
        }
    }
}

/**
 * 工具分類
 */
enum class ToolCategory(val displayName: String) {
    BASIC("基礎調節"),
    COLOR("色彩進階"),
    EFFECT("光影特效"),
    PORTRAIT("人像/AI"),
    ADVANCED_29D("29D 專用")
}
