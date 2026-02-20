package com.yanbao.camera.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.util.GitBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfile(
    val userName: String = "Yanbao Creator",
    val userId: String = "12345678",
    val memberNumber: String = "YB-88888",
    val remainingDays: Int = 365,
    val location: String = "上海 · 静安区",
    val avatarUri: String? = null,
    val backgroundUri: String? = null
)

data class WorkItem(
    val id: String,
    val colorStart: Long,
    val colorEnd: Long,
    val likeCount: String
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    companion object {
        private const val PREFS_NAME = "yanbao_profile"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_MEMBER_NUMBER = "member_number"
        private const val KEY_REMAINING_DAYS = "remaining_days"
        private const val KEY_AVATAR_URI = "avatar_uri"
        private const val KEY_BACKGROUND_URI = "background_uri"
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gitBackupManager = GitBackupManager(context)

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<UserProfile> = _profile
    
    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus
    
    /**
     * 从 SharedPreferences 加载用户资料
     */
    private fun loadProfile(): UserProfile {
        return UserProfile(
            userName = prefs.getString(KEY_USER_NAME, "Yanbao Creator") ?: "Yanbao Creator",
            userId = prefs.getString(KEY_USER_ID, "88888") ?: "88888",
            memberNumber = prefs.getString(KEY_MEMBER_NUMBER, "YB-88888") ?: "YB-88888",
            remainingDays = prefs.getInt(KEY_REMAINING_DAYS, 365),
            location = "上海 · 静安区",
            avatarUri = prefs.getString(KEY_AVATAR_URI, null),
            backgroundUri = prefs.getString(KEY_BACKGROUND_URI, null)
        )
    }
    
    /**
     * 保存用户资料到 SharedPreferences
     */
    private fun saveProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, profile.userName)
            putString(KEY_USER_ID, profile.userId)
            putString(KEY_MEMBER_NUMBER, profile.memberNumber)
            putInt(KEY_REMAINING_DAYS, profile.remainingDays)
            profile.avatarUri?.let { putString(KEY_AVATAR_URI, it) }
            profile.backgroundUri?.let { putString(KEY_BACKGROUND_URI, it) }
            apply()
        }
    }
    
    /**
     * 更新用户名
     */
    fun updateUserName(name: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(userName = name)
            _profile.value = updated
            saveProfile(updated)
        }
    }
    
    /**
     * 更新用户 ID
     */
    fun updateUserId(id: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(userId = id)
            _profile.value = updated
            saveProfile(updated)
        }
    }
    
    /**
     * 更新头像
     */
    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            val updated = _profile.value.copy(avatarUri = uri.toString())
            _profile.value = updated
            saveProfile(updated)
        }
    }
    
    /**
     * 更新背景
     */
    fun updateBackground(uri: Uri) {
        viewModelScope.launch {
            val updated = _profile.value.copy(backgroundUri = uri.toString())
            _profile.value = updated
            saveProfile(updated)
        }
    }
    
    /**
     * 更新会员号
     */
    fun updateMemberNumber(number: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(memberNumber = number)
            _profile.value = updated
            saveProfile(updated)
        }
    }
    
    /**
     * 更新剩余天数
     */
    fun updateRemainingDays(days: Int) {
        viewModelScope.launch {
            val updated = _profile.value.copy(remainingDays = days)
            _profile.value = updated
            saveProfile(updated)
        }
    }
    
    /**
     * 执行 Git 备份
     */
    fun performGitBackup() {
        viewModelScope.launch {
            _backupStatus.value = "备份中..."
            
            val result = gitBackupManager.performFullBackup()
            
            _backupStatus.value = result.fold(
                onSuccess = { message -> "✅ $message" },
                onFailure = { error -> "❌ 备份失败: ${error.message}" }
            )
        }
    }
    
    /**
     * 检查 Git 仓库状态
     */
    fun checkGitStatus() {
        viewModelScope.launch {
            val result = gitBackupManager.checkGitStatus()
            
            _backupStatus.value = result.fold(
                onSuccess = { status -> "✅ Git 状态: $status" },
                onFailure = { error -> "❌ ${error.message}" }
            )
        }
    }
    
    /**
     * 获取 Git 提交记录
     */
    fun getGitLog() {
        viewModelScope.launch {
            val result = gitBackupManager.getGitLog()
            
            _backupStatus.value = result.fold(
                onSuccess = { log -> "✅ 最近提交:\n$log" },
                onFailure = { error -> "❌ ${error.message}" }
            )
        }
    }

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    private val _works = MutableStateFlow(generateMockWorks())
    val works: StateFlow<List<WorkItem>> = _works

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    private fun generateMockWorks(): List<WorkItem> {
        val colorPairs = listOf(
            0xFFA78BFA to 0xFFEC4899,
            0xFF6366F1 to 0xFFA78BFA,
            0xFFEC4899 to 0xFFF9A8D4,
            0xFF8B5CF6 to 0xFF6366F1,
            0xFFDB2777 to 0xFFEC4899,
            0xFF7C3AED to 0xFF8B5CF6,
            0xFFF472B6 to 0xFFA78BFA,
            0xFF9333EA to 0xFF7C3AED,
            0xFFE879F9 to 0xFFF472B6
        )
        return colorPairs.mapIndexed { index, (start, end) ->
            WorkItem(
                id = "work_$index",
                colorStart = start.toLong(),
                colorEnd = end.toLong(),
                likeCount = listOf("2.5k", "1.8k", "3.2k", "987", "1.1k", "4.5k", "756", "2.1k", "1.4k")[index]
            )
        }
    }
}
