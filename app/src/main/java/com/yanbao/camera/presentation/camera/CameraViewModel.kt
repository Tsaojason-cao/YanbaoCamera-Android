package com.yanbao.camera.presentation.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.Surface
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.model.CameraMode
import com.yanbao.camera.core.util.AuditLogger
import com.yanbao.camera.core.util.Camera2Manager
import com.yanbao.camera.data.local.YanbaoMemoryDatabase
import com.yanbao.camera.data.local.entity.YanbaoMemory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 相机 ViewModel - 简化版
 * 
 * 管理相机状态、模式切换、参数调整和数据库保存
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val database: YanbaoMemoryDatabase
) : ViewModel() {
    
    companion object {
        private const val TAG = "CameraViewModel"
    }
    
    // 相机管理器
    private var camera2Manager: Camera2Manager? = null
    
    // 当前模式
    var currentMode by mutableStateOf(CameraMode.PHOTO)
        private set
    
    // 手动参数（专业模式）
    var manualISO by mutableStateOf(800)
        private set
    
    var manualExposureTime by mutableStateOf(10000000L)  // 10ms
        private set
    
    /**
     * SurfaceTexture 可用时调用
     */
    fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, context: Context) {
        Log.d(TAG, "SurfaceTexture available")
        
        camera2Manager = Camera2Manager(context)
        camera2Manager?.onError = { error ->
            Log.e(TAG, "Camera error: $error")
        }
        camera2Manager?.onPhotoSaved = { imagePath ->
            Log.d(TAG, "Photo saved: $imagePath")
            saveMemoryToDatabase(imagePath, context)
        }
        
        // 创建 Surface
        val surface = Surface(surfaceTexture)
        
        // 打开相机
        camera2Manager?.openCamera(surface)
        AuditLogger.logCameraOpen("0", "BACK")
    }
    
    /**
     * SurfaceTexture 销毁时调用
     */
    fun onSurfaceTextureDestroyed() {
        Log.d(TAG, "SurfaceTexture destroyed")
        camera2Manager?.closeCamera()
        camera2Manager = null
        AuditLogger.logCameraClose("0")
    }
    
    /**
     * 设置相机模式
     */
    fun setMode(mode: CameraMode) {
        val oldMode = currentMode
        currentMode = mode
        
        AuditLogger.logModeSwitch(oldMode.displayName, mode.displayName)
        Log.d(TAG, "Mode switched: ${oldMode.displayName} -> ${mode.displayName}")
    }
    
    /**
     * 设置 ISO
     */
    fun setISO(iso: Int) {
        manualISO = iso
        AuditLogger.logParameterAdjustment("ISO", iso, iso)
        Log.d(TAG, "ISO set: $iso")
    }
    
    /**
     * 设置曝光时间
     */
    fun setExposureTime(exposureTime: Long) {
        manualExposureTime = exposureTime
        AuditLogger.logParameterAdjustment("ExposureTime", exposureTime, exposureTime)
        Log.d(TAG, "Exposure time set: ${exposureTime}ns")
    }
    
    /**
     * 拍照
     */
    fun takePhoto(context: Context) {
        Log.d(TAG, "Take photo clicked")
        camera2Manager?.takePhoto()
        // 注: Camera2Manager 的 takePhoto 方法会通过 onPhotoSaved 回调返回结果
    }
    
    /**
     * 保存雁宝记忆到数据库
     */
    private fun saveMemoryToDatabase(imagePath: String, context: Context) {
        viewModelScope.launch {
            try {
                // 构建 29D 参数 JSON
                val params29D = buildParams29DJson()
                
                // 创建 YanbaoMemory 对象
                val memory = YanbaoMemory(
                    appName = "yanbao AI",  // 英文名
                    imagePath = imagePath,
                    timestamp = System.currentTimeMillis(),
                    shootingMode = currentMode.displayName,
                    latitude = 0.0,  // GPS 功能将在 Phase 3 实现
                    longitude = 0.0,
                    locationName = null,
                    weatherType = null,
                    parameterSnapshotJson = params29D,
                    memberNumber = "88888"
                )
                
                // 插入数据库
                val id = database.yanbaoMemoryDao().insert(memory)
                Log.d(TAG, "✅ Memory saved to database: id=$id")
                Log.d(TAG, "✅ Image path: $imagePath")
                Log.d(TAG, "✅ Mode: ${currentMode.displayName}")
                Log.d(TAG, "✅ Params29D: $params29D")
                
                // 审计日志
                AuditLogger.logMemorySaved(imagePath, params29D, 0.0, 0.0)
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to save memory to database", e)
            }
        }
    }
    
    /**
     * 构建 29D 参数 JSON
     */
    private fun buildParams29DJson(): String {
        return """
            {
                "mode": "${currentMode.displayName}",
                "iso": $manualISO,
                "exposureTime": $manualExposureTime,
                "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()
    }
    
    override fun onCleared() {
        super.onCleared()
        camera2Manager?.closeCamera()
    }
}
