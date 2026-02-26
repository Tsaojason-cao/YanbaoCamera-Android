package com.yanbao.camera.presentation.editor

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.render.EditParams
import com.yanbao.camera.core.render.GLRenderer
import com.yanbao.camera.data.repository.MasterPrivilegeStatus
import com.yanbao.camera.data.repository.YanbaoGardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 编辑器 ViewModel —— 满血版
 *
 * 新增功能：
 * - 注入 YanbaoGardenRepository，实现大师滤镜特权闭环
 * - 使用大师滤镜前调用 checkMasterPrivilege()
 * - 使用成功后调用 consumeMasterPrivilege() 扣减次数
 * - 无特权时弹出引导弹窗（masterPrivilegeStatus 状态）
 */
@HiltViewModel
class EditorViewModel @Inject constructor(
    private val gardenRepository: YanbaoGardenRepository
) : ViewModel() {

    // 当前编辑的图像
    private val _sourceBitmap = MutableStateFlow<Bitmap?>(null)
    val sourceBitmap: StateFlow<Bitmap?> = _sourceBitmap.asStateFlow()

    // 当前编辑参数
    private val _editParams = MutableStateFlow(EditParams())
    val editParams: StateFlow<EditParams> = _editParams.asStateFlow()

    // 当前选中的工具
    private val _selectedTool = MutableStateFlow(EditTool.BRIGHTNESS)
    val selectedTool: StateFlow<EditTool> = _selectedTool.asStateFlow()

    // 预览图像
    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    // 是否正在处理
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // ── 大师滤镜特权状态 ─────────────────────────────────────────────────────
    /** 大师滤镜特权检查结果（null = 未检查，HasPrivilege/VipUnlimited = 有权限，NoPrivilege/DailyLimitReached = 无权限） */
    private val _masterPrivilegeStatus = MutableStateFlow<MasterPrivilegeStatus?>(null)
    val masterPrivilegeStatus: StateFlow<MasterPrivilegeStatus?> = _masterPrivilegeStatus.asStateFlow()

    /** 是否显示「去雁宝园地喂食」引导弹窗 */
    private val _showGardenGuideDialog = MutableStateFlow(false)
    val showGardenGuideDialog: StateFlow<Boolean> = _showGardenGuideDialog.asStateFlow()

    /** 引导弹窗文案 */
    private val _gardenGuideMessage = MutableStateFlow("")
    val gardenGuideMessage: StateFlow<String> = _gardenGuideMessage.asStateFlow()

    // ── 基础操作 ─────────────────────────────────────────────────────────────

    fun setSourceBitmap(bitmap: Bitmap) {
        _sourceBitmap.value = bitmap
        _previewBitmap.value = bitmap
    }

    fun selectTool(tool: EditTool) {
        _selectedTool.value = tool
        // 如果选中大师级工具，自动检查特权
        if (tool.getCategory() == ToolCategory.ADVANCED_29D || tool.isMasterFilter) {
            checkMasterPrivilegeForTool()
        }
    }

    fun updateParam(tool: EditTool, value: Float) {
        val params = _editParams.value.copy()
        when (tool) {
            EditTool.BRIGHTNESS -> params.brightness = value
            EditTool.CONTRAST -> params.contrast = value
            EditTool.SATURATION -> params.saturation = value
            EditTool.SHARPEN -> params.sharpen = value
            EditTool.TEMPERATURE -> params.temperature = value
            EditTool.TINT -> params.tint = value
            EditTool.HIGHLIGHTS -> params.highlights = value
            EditTool.SHADOWS -> params.shadows = value
            EditTool.EXPOSURE -> params.exposure = value
            EditTool.GRAIN -> params.grain = value
            EditTool.VIGNETTE -> params.vignette = value
            EditTool.FADE -> params.fade = value
            EditTool.SMOOTHING -> params.smoothing = value
            EditTool.WHITENING -> params.whitening = value
            EditTool.SPOT_REMOVAL -> params.spotRemoval = value
            EditTool.CHROMATIC_RGB -> params.chromaticRGB = value
            EditTool.DEPTH_OF_FIELD -> params.depthOfField = value
            EditTool.DYNAMIC_RANGE -> params.dynamicRange = value
        }
        _editParams.value = params
    }

    // ── 大师滤镜特权闭环 ─────────────────────────────────────────────────────

    /**
     * 检查大师滤镜特权（选中大师工具时自动调用）
     */
    private fun checkMasterPrivilegeForTool() {
        viewModelScope.launch {
            val status = gardenRepository.checkMasterPrivilege()
            _masterPrivilegeStatus.value = status
            when (status) {
                is MasterPrivilegeStatus.NoPrivilege -> {
                    _gardenGuideMessage.value = status.message
                    _showGardenGuideDialog.value = true
                }
                is MasterPrivilegeStatus.DailyLimitReached -> {
                    _gardenGuideMessage.value = status.message
                    _showGardenGuideDialog.value = true
                }
                else -> { /* HasPrivilege 或 VipUnlimited，无需弹窗 */ }
            }
        }
    }

    /**
     * 应用大师滤镜（含特权检查与消耗）
     *
     * 调用流程：
     * 1. checkMasterPrivilege() 检查是否有权限
     * 2. 有权限 → consumeMasterPrivilege() 扣减次数 → 执行渲染
     * 3. 无权限 → 弹出引导弹窗
     *
     * @param renderer GL 渲染器
     * @param onNoPrivilege 无特权时的回调（可用于跳转雁宝园地）
     */
    fun applyMasterFilter(renderer: GLRenderer, onNoPrivilege: () -> Unit = {}) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val status = gardenRepository.checkMasterPrivilege()
                when (status) {
                    is MasterPrivilegeStatus.HasPrivilege,
                    is MasterPrivilegeStatus.VipUnlimited -> {
                        // 消耗特权次数
                        val consumed = gardenRepository.consumeMasterPrivilege()
                        if (consumed) {
                            // 执行渲染
                            val source = _sourceBitmap.value
                            if (source != null) {
                                renderer.updateParams(_editParams.value)
                                val result = renderer.render(source)
                                _previewBitmap.value = result
                            }
                        }
                    }
                    is MasterPrivilegeStatus.NoPrivilege -> {
                        _gardenGuideMessage.value = status.message
                        _showGardenGuideDialog.value = true
                        onNoPrivilege()
                    }
                    is MasterPrivilegeStatus.DailyLimitReached -> {
                        _gardenGuideMessage.value = status.message
                        _showGardenGuideDialog.value = true
                        onNoPrivilege()
                    }
                }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * 应用普通编辑（无需特权检查）
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

    fun dismissGardenGuideDialog() {
        _showGardenGuideDialog.value = false
    }

    fun resetParams() {
        _editParams.value = EditParams()
        _previewBitmap.value = _sourceBitmap.value
    }

    fun resetToolParam(tool: EditTool) {
        updateParam(tool, getDefaultValue(tool))
    }

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
 * 编辑工具枚举
 */
enum class EditTool(
    val displayName: String,
    val minValue: Float,
    val maxValue: Float,
    /** 是否为大师级滤镜（需要消耗特权次数） */
    val isMasterFilter: Boolean = false
) {
    // 基础调节
    BRIGHTNESS("亮度", -1f, 1f),
    CONTRAST("对比度", 0f, 2f),
    SATURATION("饱和度", 0f, 2f),
    SHARPEN("锐化", 0f, 1f),

    // 色彩进阶
    TEMPERATURE("色温", -1f, 1f),
    TINT("色调", -1f, 1f),
    HIGHLIGHTS("高光", -1f, 1f),
    SHADOWS("阴影", -1f, 1f),

    // 光影特效
    EXPOSURE("曝光", -2f, 2f),
    GRAIN("颗粒", 0f, 1f),
    VIGNETTE("晕影", 0f, 1f),
    FADE("褪色", 0f, 1f),

    // 人像/AI
    SMOOTHING("磨皮", 0f, 1f),
    WHITENING("美白", 0f, 1f),
    SPOT_REMOVAL("祛斑", 0f, 1f),

    // 2.9D 专用（大师级，需要特权）
    CHROMATIC_RGB("2.9D 色散", 0f, 1f, isMasterFilter = true),
    DEPTH_OF_FIELD("2.9D 景深", 0f, 1f, isMasterFilter = true),
    DYNAMIC_RANGE("动态范围", 0f, 1f, isMasterFilter = true);

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
 * 工具分类
 */
enum class ToolCategory(val displayName: String) {
    BASIC("基础调节"),
    COLOR("色彩进阶"),
    EFFECT("光影特效"),
    PORTRAIT("人像/AI"),
    ADVANCED_29D("2.9D 专用")
}
