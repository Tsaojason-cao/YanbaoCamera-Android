package com.yanbao.camera.presentation.profile

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.core.util.DeviceUidGenerator
import com.yanbao.camera.core.util.GitBackupManager
import com.yanbao.camera.data.local.dao.YanbaoMemoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class UserProfile(
    val userName: String = "Yanbao Creator",
    val userId: String = "@YanbaoUser",
    val memberNumber: String,
    val joinDate: Long,
    val daysWithYanbao: Int,
    val location: String = "上海 · 静安区",
    val avatarUri: String? = null,
    val backgroundUri: String? = null
)

data class UserStats(
    val worksCount: Int = 0,
    val memoriesCount: Int = 0,
    val likesCount: String = "0"
)

data class WorkItem(
    val id: String,
    val colorStart: Long,
    val colorEnd: Long,
    val likeCount: String
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val yanbaoMemoryDao: YanbaoMemoryDao
) : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
        private const val PREFS_NAME = "yanbao_profile"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_MEMBER_NUMBER = "member_number"
        private const val KEY_JOIN_DATE = "join_date"
        private const val KEY_AVATAR_URI = "avatar_uri"
        private const val KEY_BACKGROUND_URI = "background_uri"
        private const val KEY_TOTAL_LIKES = "total_likes"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gitBackupManager = GitBackupManager(context)

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<UserProfile> = _profile

    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus

    // 统计数据
    private val _stats = MutableStateFlow(UserStats())
    val stats: StateFlow<UserStats> = _stats

    // 缓存大小
    private val _cacheSize = MutableStateFlow("计算中...")
    val cacheSize: StateFlow<String> = _cacheSize

    // 清理状态
    private val _clearCacheStatus = MutableStateFlow<String?>(null)
    val clearCacheStatus: StateFlow<String?> = _clearCacheStatus

    // 通知开关
    private val _notificationsEnabled = MutableStateFlow(
        prefs.getBoolean("notifications_enabled", true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    // 自动备份开关
    private val _autoBackupEnabled = MutableStateFlow(
        prefs.getBoolean("auto_backup_enabled", false)
    )
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    // 高质量导出开关
    private val _highQualityExport = MutableStateFlow(
        prefs.getBoolean("high_quality_export", true)
    )
    val highQualityExport: StateFlow<Boolean> = _highQualityExport

    // 选中的 Tab（作品/记忆）
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    // 作品列表（颜色块，代表照片缩略图）
    private val _works = MutableStateFlow(generateColorWorks())
    val works: StateFlow<List<WorkItem>> = _works

    init {
        loadStats()
        calculateCacheSize()
    }

    // ─── 加载用户资料 ─────────────────────────────────────────────────────
    private fun loadProfile(): UserProfile {
        val memberNumber = prefs.getString(KEY_MEMBER_NUMBER, null) ?: run {
            val uid = DeviceUidGenerator.generateUid(context)
            prefs.edit().putString(KEY_MEMBER_NUMBER, uid).apply()
            uid
        }
        val joinDate = prefs.getLong(KEY_JOIN_DATE, 0L).let { saved ->
            if (saved == 0L) {
                val now = System.currentTimeMillis()
                prefs.edit().putLong(KEY_JOIN_DATE, now).apply()
                now
            } else saved
        }
        return UserProfile(
            userName = prefs.getString(KEY_USER_NAME, "Yanbao Creator") ?: "Yanbao Creator",
            userId = prefs.getString(KEY_USER_ID, "@YanbaoUser") ?: "@YanbaoUser",
            memberNumber = memberNumber,
            joinDate = joinDate,
            daysWithYanbao = TimeUnit.MILLISECONDS.toDays(
                System.currentTimeMillis() - joinDate
            ).toInt(),
            location = "上海 · 静安区",
            avatarUri = prefs.getString(KEY_AVATAR_URI, null),
            backgroundUri = prefs.getString(KEY_BACKGROUND_URI, null)
        )
    }

    private fun saveProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, profile.userName)
            putString(KEY_USER_ID, profile.userId)
            profile.avatarUri?.let { putString(KEY_AVATAR_URI, it) }
            profile.backgroundUri?.let { putString(KEY_BACKGROUND_URI, it) }
            apply()
        }
    }

    // ─── 统计数据：从 MediaStore + Room 读取 ─────────────────────────────
    private fun loadStats() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // 1. 作品数：MediaStore 中 YanbaoCamera 拍摄的照片数
                    val worksCount = countYanbaoPhotos(context.contentResolver)

                    // 2. 记忆数：Room 数据库中的记忆条目数
                    val memoriesCount = try {
                        yanbaoMemoryDao.getAll().size
                    } catch (e: Exception) { 0 }

                    // 3. 获赞数：从 SharedPreferences 读取（用户互动数据）
                    val savedLikes = prefs.getInt(KEY_TOTAL_LIKES, 0)
                    val likesDisplay = when {
                        savedLikes >= 10000 -> "${String.format("%.1f", savedLikes / 10000.0)}w"
                        savedLikes >= 1000 -> "${String.format("%.1f", savedLikes / 1000.0)}k"
                        else -> savedLikes.toString()
                    }

                    _stats.value = UserStats(
                        worksCount = worksCount,
                        memoriesCount = memoriesCount,
                        likesCount = likesDisplay
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "loadStats failed", e)
                }
            }
        }
    }

    private fun countYanbaoPhotos(resolver: ContentResolver): Int {
        return try {
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = "${MediaStore.Images.Media.DATA} LIKE ? OR " +
                "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
            val args = arrayOf("%YanbaoCamera%", "YanbaoAI_%")
            resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, args, null
            )?.use { it.count } ?: 0
        } catch (e: Exception) { 0 }
    }

    // ─── 缓存计算与清理 ───────────────────────────────────────────────────
    private fun calculateCacheSize() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val size = getDirSize(context.cacheDir) + getDirSize(context.externalCacheDir)
                _cacheSize.value = formatSize(size)
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _clearCacheStatus.value = "清理中..."
            withContext(Dispatchers.IO) {
                try {
                    deleteDir(context.cacheDir)
                    context.externalCacheDir?.let { deleteDir(it) }
                    _clearCacheStatus.value = "✅ 缓存已清理"
                    _cacheSize.value = "0 B"
                } catch (e: Exception) {
                    _clearCacheStatus.value = "❌ 清理失败: ${e.message}"
                }
            }
        }
    }

    private fun getDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private fun deleteDir(dir: File) {
        if (dir.isDirectory) {
            dir.listFiles()?.forEach { deleteDir(it) }
        }
        dir.delete()
    }

    private fun formatSize(bytes: Long): String = when {
        bytes >= 1024 * 1024 * 1024 -> "${String.format("%.1f", bytes / (1024.0 * 1024 * 1024))} GB"
        bytes >= 1024 * 1024 -> "${String.format("%.1f", bytes / (1024.0 * 1024))} MB"
        bytes >= 1024 -> "${String.format("%.1f", bytes / 1024.0)} KB"
        else -> "$bytes B"
    }

    // ─── 用户资料更新 ─────────────────────────────────────────────────────
    fun updateUserName(name: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(userName = name)
            _profile.value = updated
            saveProfile(updated)
        }
    }

    fun updateUserId(id: String) {
        viewModelScope.launch {
            val updated = _profile.value.copy(userId = id)
            _profile.value = updated
            saveProfile(updated)
        }
    }

    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            val updated = _profile.value.copy(avatarUri = uri.toString())
            _profile.value = updated
            saveProfile(updated)
        }
    }

    fun updateBackground(uri: Uri) {
        viewModelScope.launch {
            val updated = _profile.value.copy(backgroundUri = uri.toString())
            _profile.value = updated
            saveProfile(updated)
        }
    }

    // ─── 开关设置 ─────────────────────────────────────────────────────────
    fun toggleNotifications() {
        val newVal = !_notificationsEnabled.value
        _notificationsEnabled.value = newVal
        prefs.edit().putBoolean("notifications_enabled", newVal).apply()
    }

    fun toggleAutoBackup() {
        val newVal = !_autoBackupEnabled.value
        _autoBackupEnabled.value = newVal
        prefs.edit().putBoolean("auto_backup_enabled", newVal).apply()
        if (newVal) performGitBackup()
    }

    fun toggleHighQualityExport() {
        val newVal = !_highQualityExport.value
        _highQualityExport.value = newVal
        prefs.edit().putBoolean("high_quality_export", newVal).apply()
    }

    // ─── Git 备份 ─────────────────────────────────────────────────────────
    fun performGitBackup() {
        viewModelScope.launch {
            _backupStatus.value = "备份中..."
            val result = gitBackupManager.performFullBackup()
            _backupStatus.value = result.fold(
                onSuccess = { "✅ $it" },
                onFailure = { "❌ 备份失败: ${it.message}" }
            )
        }
    }

    fun checkGitStatus() {
        viewModelScope.launch {
            val result = gitBackupManager.checkGitStatus()
            _backupStatus.value = result.fold(
                onSuccess = { "✅ Git 状态: $it" },
                onFailure = { "❌ ${it.message}" }
            )
        }
    }

    fun getGitLog() {
        viewModelScope.launch {
            val result = gitBackupManager.getGitLog()
            _backupStatus.value = result.fold(
                onSuccess = { "✅ 最近提交:\n$it" },
                onFailure = { "❌ ${it.message}" }
            )
        }
    }

    // ─── Tab 切换 ─────────────────────────────────────────────────────────
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun refreshDaysWithYanbao() {
        viewModelScope.launch {
            val updated = _profile.value.copy(
                daysWithYanbao = TimeUnit.MILLISECONDS.toDays(
                    System.currentTimeMillis() - _profile.value.joinDate
                ).toInt()
            )
            _profile.value = updated
        }
    }

    @Deprecated("Member number is hardware-based and cannot be modified")
    fun updateMemberNumber(number: String) { /* 不允许修改 */ }

    // ─── 颜色块作品列表（代表照片缩略图，无需真实图片） ──────────────────
    private fun generateColorWorks(): List<WorkItem> {
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
