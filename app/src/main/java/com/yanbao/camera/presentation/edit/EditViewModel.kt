package com.yanbao.camera.presentation.edit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Canvas as AndroidCanvas
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.utils.ImageSaver
import com.yanbao.camera.data.local.dao.YanbaoMemoryDao
import com.yanbao.camera.data.local.entity.YanbaoMemoryFactory
import com.yanbao.camera.data.model.Camera29DState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 编辑模块 ViewModel（满血版）
 *
 * 功能：
 * - 撤销/重做历史栈（最多 20 步）
 * - 真实保存图片到相册（MediaStore API）
 * - 从 Room 数据库读取雁宝记忆参数并应用
 * - 所有参数变更自动推入历史栈
 */
@HiltViewModel
class EditViewModel @Inject constructor(
    private val yanbaoMemoryDao: YanbaoMemoryDao
) : ViewModel() {

    companion object {
        private const val TAG = "EditViewModel"
        private const val MAX_HISTORY = 20
    }

    // ─── 当前编辑参数状态 ───────────────────────────────────────────────
    data class EditParams(
        val brightness: Float = 0.5f,
        val contrast: Float = 0.5f,
        val saturation: Float = 0.5f,
        val temperature: Float = 0.5f,
        val sharpness: Float = 0.5f,
        val filterIntensity: Float = 0.75f,
        val selectedFilterCountry: String = "KR"
    )

    // 历史栈（撤销）
    private val undoStack = ArrayDeque<EditParams>()
    // 重做栈
    private val redoStack = ArrayDeque<EditParams>()

    private val _params = MutableStateFlow(EditParams())
    val params: StateFlow<EditParams> = _params.asStateFlow()

    // 单独暴露各参数（保持向后兼容）
    val brightness: StateFlow<Float> get() = MutableStateFlow(_params.value.brightness).also {
        viewModelScope.launch { _params.collect { p -> it.value = p.brightness } }
    }

    private val _selectedCategory = MutableStateFlow<ToolCategory>(ToolCategory.ADJUST)
    val selectedCategory: StateFlow<ToolCategory> = _selectedCategory.asStateFlow()

    private val _selectedTool = MutableStateFlow<EditTool?>(editTools.find { it.id == "brightness" })
    val selectedTool: StateFlow<EditTool?> = _selectedTool.asStateFlow()

    private val _showMemoryPanel = MutableStateFlow(false)
    val showMemoryPanel: StateFlow<Boolean> = _showMemoryPanel.asStateFlow()

    // 当前编辑的照片 URI（从相册或相机传入）
    private val _currentPhotoUri = MutableStateFlow<String?>(null)
    val currentPhotoUri: StateFlow<String?> = _currentPhotoUri.asStateFlow()

    // 保存状态
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    // 是否可撤销/重做
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // 雁宝记忆列表（用于记忆面板）
    private val _memories = MutableStateFlow<List<MemoryItem>>(emptyList())
    val memories: StateFlow<List<MemoryItem>> = _memories.asStateFlow()

    init {
        loadMemories()
    }

    // ─── 参数修改（自动推入历史栈）──────────────────────────────────────

    private fun updateParams(newParams: EditParams) {
        // 推入撤销栈
        undoStack.addLast(_params.value)
        if (undoStack.size > MAX_HISTORY) undoStack.removeFirst()
        // 清空重做栈（新操作后不能再重做）
        redoStack.clear()
        _params.value = newParams
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = false
    }

    fun setBrightness(value: Float) = updateParams(_params.value.copy(brightness = value))
    fun setContrast(value: Float) = updateParams(_params.value.copy(contrast = value))
    fun setSaturation(value: Float) = updateParams(_params.value.copy(saturation = value))
    fun setTemperature(value: Float) = updateParams(_params.value.copy(temperature = value))
    fun setSharpness(value: Float) = updateParams(_params.value.copy(sharpness = value))
    fun setFilterIntensity(value: Float) = updateParams(_params.value.copy(filterIntensity = value))
    fun setFilterCountry(country: String) = updateParams(_params.value.copy(selectedFilterCountry = country))

    fun selectCategory(category: ToolCategory) {
        _selectedCategory.value = category
        val firstTool = editTools.find { it.category == category }
        firstTool?.let { _selectedTool.value = it }
    }

    fun selectTool(tool: EditTool) {
        _selectedTool.value = tool
    }

    fun toggleMemoryPanel() {
        _showMemoryPanel.value = !_showMemoryPanel.value
    }

    fun setCurrentPhoto(uri: String) {
        _currentPhotoUri.value = uri
        // 重置历史栈
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _params.value = EditParams()
    }

    // ─── 撤销 ──────────────────────────────────────────────────────────

    fun undo() {
        if (undoStack.isEmpty()) return
        val prev = undoStack.removeLast()
        redoStack.addLast(_params.value)
        _params.value = prev
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = true
        Log.d(TAG, "Undo: brightness=${prev.brightness}, contrast=${prev.contrast}")
    }

    // ─── 重做 ──────────────────────────────────────────────────────────

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeLast()
        undoStack.addLast(_params.value)
        _params.value = next
        _canUndo.value = true
        _canRedo.value = redoStack.isNotEmpty()
        Log.d(TAG, "Redo: brightness=${next.brightness}, contrast=${next.contrast}")
    }

    // ─── 保存图片到相册（真实 MediaStore 写入）─────────────────────────

    fun save(context: Context) {
        val photoUri = _currentPhotoUri.value
        if (photoUri == null) {
            _saveState.value = SaveState.Error("未选择照片")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                val resultUri = withContext(Dispatchers.IO) {
                    // 1. 加载原始 Bitmap
                    val inputStream = if (photoUri.startsWith("content://") || photoUri.startsWith("file://")) {
                        context.contentResolver.openInputStream(Uri.parse(photoUri))
                    } else {
                        java.io.FileInputStream(photoUri)
                    }
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                        ?: return@withContext null

                    // 2. 应用当前参数（ColorMatrix 调整）
                    val adjustedBitmap = applyAdjustments(originalBitmap, _params.value)

                    // 3. 保存到相册
                    val filterName = _params.value.selectedFilterCountry
                    ImageSaver.saveBitmapToGallery(context, adjustedBitmap, filterName)
                }

                if (resultUri != null) {
                    // 4. 写入雁宝记忆数据库
                    val memory = YanbaoMemoryFactory.create(
                        imagePath = resultUri.toString(),
                        latitude = 0.0,
                        longitude = 0.0,
                        locationName = null,
                        weatherType = null,
                        shootingMode = "Edit",
                        parameterSnapshotJson = Camera29DState(
                            brightness = _params.value.brightness,
                            contrast = _params.value.contrast,
                            saturation = _params.value.saturation,
                            colorTemp = _params.value.temperature * 10000f,
                            sharpness = _params.value.sharpness
                        ).toJson()
                    )
                    yanbaoMemoryDao.insert(memory)
                    _saveState.value = SaveState.Success(resultUri.toString())
                    Log.d(TAG, "Save success: $resultUri")
                } else {
                    _saveState.value = SaveState.Error("保存失败")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Save error", e)
                _saveState.value = SaveState.Error(e.message ?: "未知错误")
            }
        }
    }

    /**
     * 应用 ColorMatrix 调整（亮度/对比度/饱和度/色温）
     */
    private fun applyAdjustments(src: Bitmap, p: EditParams): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(result)
        val paint = Paint()

        // 亮度：[-1, 1] → translate
        val brightness = (p.brightness - 0.5f) * 2f * 255f
        // 对比度：[0, 2]
        val contrast = p.contrast * 2f
        // 饱和度：[0, 2]
        val saturation = p.saturation * 2f
        // 色温偏移（暖/冷）
        val warmth = (p.temperature - 0.5f) * 60f

        val cm = ColorMatrix()

        // 饱和度矩阵
        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(saturation)

        // 对比度 + 亮度矩阵
        val scale = contrast
        val translate = brightness + (1f - contrast) * 128f
        val contrastMatrix = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))

        // 色温矩阵（暖色 → 红+黄，冷色 → 蓝+青）
        val tempMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, warmth,
            0f, 1f, 0f, 0f, warmth * 0.3f,
            0f, 0f, 1f, 0f, -warmth,
            0f, 0f, 0f, 1f, 0f
        ))

        cm.postConcat(satMatrix)
        cm.postConcat(contrastMatrix)
        cm.postConcat(tempMatrix)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    // ─── 从 Room 读取雁宝记忆并应用参数 ────────────────────────────────

    fun applyMemory(memoryId: String) {
        viewModelScope.launch {
            try {
                val memory = yanbaoMemoryDao.getMemoryById(memoryId.toLongOrNull() ?: 0L)
                if (memory != null) {
                    val state = Camera29DState.fromJson(memory.parameterSnapshotJson)
                    // 将 29D 参数映射到编辑参数
                    val newParams = EditParams(
                        brightness = state.brightness,
                        contrast = state.contrast,
                        saturation = state.saturation,
                        temperature = (state.colorTemp / 10000f).coerceIn(0f, 1f),
                        sharpness = state.sharpness,
                        filterIntensity = _params.value.filterIntensity,
                        selectedFilterCountry = _params.value.selectedFilterCountry
                    )
                    updateParams(newParams)
                    Log.d(TAG, "Memory applied: id=$memoryId, mode=${memory.shootingMode}")
                } else {
                    Log.w(TAG, "Memory not found: id=$memoryId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Apply memory error", e)
            } finally {
                _showMemoryPanel.value = false
            }
        }
    }

    // ─── 加载雁宝记忆列表 ───────────────────────────────────────────────

    private fun loadMemories() {
        viewModelScope.launch {
            yanbaoMemoryDao.getAllMemories().collect { list ->
                _memories.value = list.map { m ->
                    MemoryItem(
                        id = m.id.toString(),
                        imagePath = m.imagePath,
                        locationName = m.locationName ?: "未知地点",
                        shootingMode = m.shootingMode,
                        timestamp = m.timestamp
                    )
                }
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}

// ─── 辅助数据类 ─────────────────────────────────────────────────────────

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Success(val uri: String) : SaveState()
    data class Error(val message: String) : SaveState()
}

data class MemoryItem(
    val id: String,
    val imagePath: String,
    val locationName: String,
    val shootingMode: String,
    val timestamp: Long
)
