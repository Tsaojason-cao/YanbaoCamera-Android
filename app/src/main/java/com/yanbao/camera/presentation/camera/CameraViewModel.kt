package com.yanbao.camera.presentation.camera

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.data.model.Camera29DState
import com.yanbao.camera.core.model.CameraMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 相机 ViewModel
 * 
 * 使用 StateFlow 管理 29D 参数状态
 * 确保滑块一动，预览立即响应
 */
@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {
    
    companion object {
        private const val TAG = "CameraViewModel"
    }
    
    // 29D 参数状态
    private val _camera29DState = MutableStateFlow(Camera29DState())
    val camera29DState: StateFlow<Camera29DState> = _camera29DState.asStateFlow()
    
    // 当前相机模式
    private val _currentMode = MutableStateFlow(CameraMode.PHOTO)
    val currentMode: StateFlow<CameraMode> = _currentMode.asStateFlow()
    
    // 是否显示 29D 专业面板
    private val _show29DPanel = MutableStateFlow(false)
    val show29DPanel: StateFlow<Boolean> = _show29DPanel.asStateFlow()
    
    /**
     * 更新 29D 参数
     * 
     * @param name 参数名称
     * @param value 参数值
     */
    fun updateParameter(name: String, value: Any) {
        viewModelScope.launch {
            _camera29DState.update { current ->
                val updated = when (name) {
                    // 基础曝光参数
                    "brightness" -> current.copy(brightness = value as Float)
                    "contrast" -> current.copy(contrast = value as Float)
                    "saturation" -> current.copy(saturation = value as Float)
                    "sharpness" -> current.copy(sharpness = value as Float)
                    "exposure" -> current.copy(exposure = value as Float)
                    
                    // 色彩参数
                    "colorTemp" -> current.copy(colorTemp = value as Float)
                    "tint" -> current.copy(tint = value as Float)
                    "hue" -> current.copy(hue = value as Float)
                    "vibrance" -> current.copy(vibrance = value as Float)
                    
                    // 高级色彩通道
                    "red" -> current.copy(red = value as Float)
                    "green" -> current.copy(green = value as Float)
                    "blue" -> current.copy(blue = value as Float)
                    "cyan" -> current.copy(cyan = value as Float)
                    "magenta" -> current.copy(magenta = value as Float)
                    "yellow" -> current.copy(yellow = value as Float)
                    "orange" -> current.copy(orange = value as Float)
                    
                    // 明暗细节
                    "highlights" -> current.copy(highlights = value as Float)
                    "shadows" -> current.copy(shadows = value as Float)
                    "whites" -> current.copy(whites = value as Float)
                    "blacks" -> current.copy(blacks = value as Float)
                    
                    // 细节与清晰度
                    "clarity" -> current.copy(clarity = value as Float)
                    "dehaze" -> current.copy(dehaze = value as Float)
                    "noiseReduction" -> current.copy(noiseReduction = value as Float)
                    "grain" -> current.copy(grain = value as Float)
                    "vignette" -> current.copy(vignette = value as Float)
                    
                    // 美颜参数
                    "beautySmooth" -> current.copy(beautySmooth = value as Float)
                    "beautyWhiten" -> current.copy(beautyWhiten = value as Float)
                    "beautyEyeEnlarge" -> current.copy(beautyEyeEnlarge = value as Float)
                    "beautyFaceSlim" -> current.copy(beautyFaceSlim = value as Float)
                    
                    // 相机硬件参数
                    "iso" -> current.copy(iso = value as Int)
                    "exposureTime" -> current.copy(exposureTime = value as Long)
                    "whiteBalance" -> current.copy(whiteBalance = value as Int)
                    "focusMode" -> current.copy(focusMode = value as String)
                    "zoomRatio" -> current.copy(zoomRatio = value as Float)
                    
                    // 特殊模式
                    "masterFilterId" -> current.copy(masterFilterId = value as String?)
                    "is2Dot9DEnabled" -> current.copy(is2Dot9DEnabled = value as Boolean)
                    "parallaxOffset" -> current.copy(parallaxOffset = value as Pair<Float, Float>)
                    
                    else -> current
                }
                
                // 日志输出：参数变化
                Log.d(TAG, "Parameter updated: $name = $value")
                
                updated
            }
            
            // Phase 3: 调用底层 Camera2 API 真实调节硬件
            // applyHardwareSettings(_camera29DState.value)
        }
    }
    
    /**
     * 切换相机模式
     */
    fun switchMode(mode: CameraMode) {
        viewModelScope.launch {
            _currentMode.value = mode
            Log.d(TAG, "Camera mode switched to: ${mode.displayName}")
            
            // 根据模式调整参数
            when (mode) {
                CameraMode.PROFESSIONAL -> {
                    // 专业模式：启用手动控制
                    Log.d(TAG, "专业模式：启用手动控制")
                }
                CameraMode.NIGHT -> {
                    // 夜景模式：增加曝光时间
                    updateParameter("exposureTime", 100000000L) // 100ms
                }
                CameraMode.PORTRAIT -> {
                    // 人像模式：启用美颜
                    updateParameter("beautySmooth", 0.3f)
                    updateParameter("beautyWhiten", 0.2f)
                }
                else -> {
                    // 其他模式：使用默认参数
                }
            }
        }
    }
    
    /**
     * 切换 29D 专业面板显示状态
     */
    fun toggle29DPanel() {
        viewModelScope.launch {
            _show29DPanel.value = !_show29DPanel.value
            Log.d(TAG, "29D panel toggled: ${_show29DPanel.value}")
        }
    }
    
    /**
     * 重置所有参数到默认值
     */
    fun resetParameters() {
        viewModelScope.launch {
            _camera29DState.value = Camera29DState()
            Log.d(TAG, "All parameters reset to default")
        }
    }
    
    /**
     * 从 JSON 恢复参数（用于"雁宝记忆"回溯功能）
     */
    fun restoreParametersFromJson(json: String) {
        viewModelScope.launch {
            try {
                _camera29DState.value = Camera29DState.fromJson(json)
                Log.d(TAG, "Parameters restored from JSON")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore parameters from JSON", e)
            }
        }
    }
}
