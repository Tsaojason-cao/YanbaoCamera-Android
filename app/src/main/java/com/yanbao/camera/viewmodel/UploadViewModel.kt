package com.yanbao.camera.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.model.Post
import com.yanbao.camera.repository.UploadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 上传ViewModel - 管理上传状态
 */
class UploadViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = UploadRepository(application)
    
    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState
    
    private val _uploadSuccess = MutableStateFlow<Post?>(null)
    val uploadSuccess: StateFlow<Post?> = _uploadSuccess
    
    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError
    
    /**
     * 更新描述
     */
    fun updateDescription(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }
    
    /**
     * 选择位置
     */
    fun selectLocation(locationId: String, locationName: String) {
        _uiState.value = _uiState.value.copy(
            selectedLocationId = locationId,
            locationName = locationName
        )
    }
    
    /**
     * 上传作品
     */
    fun upload(imageUri: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true)
            _uploadError.value = null
            
            val result = repository.uploadPost(
                imageUri,
                _uiState.value.description,
                _uiState.value.selectedLocationId
            )
            
            _uiState.value = _uiState.value.copy(isUploading = false)
            
            result.onSuccess { post ->
                _uploadSuccess.value = post
            }.onFailure { e ->
                _uploadError.value = e.message ?: "上传失败"
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uploadError.value = null
    }
    
    /**
     * 清除成功状态
     */
    fun clearSuccess() {
        _uploadSuccess.value = null
    }
}

/**
 * 上传UI状态
 */
data class UploadUiState(
    val description: String = "",
    val selectedLocationId: String? = null,
    val locationName: String? = null,
    val isUploading: Boolean = false,
    val error: String? = null
)
