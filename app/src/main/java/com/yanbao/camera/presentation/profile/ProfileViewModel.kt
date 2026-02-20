package com.yanbao.camera.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户资料数据类
 */
data class UserProfile(
    val userName: String = "Yanbao Creator",
    val userId: String = "88888",
    val memberNumber: String = "YB-88888",
    val remainingDays: Int = 365,
    val location: String = "上海 · 静安区",
    val avatarUri: String? = null,
    val backgroundUri: String? = null
)

/**
 * 个人中心 ViewModel
 * 
 * 功能：
 * - 用户资料管理（头像、ID、会员号）
 * - SharedPreferences 持久化
 * - 背景图片更换
 */
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
    
    // 用户资料状态
    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<UserProfile> = _profile
    
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
     * 更新背景图片
     */
    fun updateBackground(uri: Uri) {
        viewModelScope.launch {
            val updated = _profile.value.copy(backgroundUri = uri.toString())
            _profile.value = updated
            saveProfile(updated)
        }
    }
}
