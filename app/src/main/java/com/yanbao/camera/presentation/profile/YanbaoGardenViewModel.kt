package com.yanbao.camera.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.data.local.entity.YanbaoGardenFeedEntity
import com.yanbao.camera.data.local.entity.YanbaoGardenPrivilegeEntity
import com.yanbao.camera.data.repository.FeedResult
import com.yanbao.camera.data.repository.TodayFeedStatus
import com.yanbao.camera.data.repository.YanbaoGardenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 雁宝园地 ViewModel
 *
 * 管理「雁宝园地」页面的所有 UI 状态，包括：
 *  - 喂食动作与动画触发
 *  - 今日喂食次数显示
 *  - 特权等级与进度
 *  - 升级弹窗
 *  - 分享奖励流程
 */
@HiltViewModel
class YanbaoGardenViewModel @Inject constructor(
    private val gardenRepository: YanbaoGardenRepository
) : ViewModel() {

    companion object {
        private const val TAG = "YanbaoGardenVM"
    }

    // ─── UI 状态 ───────────────────────────────────────────────────────────

    /** 特权状态（来自数据库） */
    private val _privilege = MutableStateFlow<YanbaoGardenPrivilegeEntity?>(null)
    val privilege: StateFlow<YanbaoGardenPrivilegeEntity?> = _privilege.asStateFlow()

    /** 今日喂食状态 */
    private val _todayStatus = MutableStateFlow(
        TodayFeedStatus(0, 3, 0, 5, true, true)
    )
    val todayStatus: StateFlow<TodayFeedStatus> = _todayStatus.asStateFlow()

    /** 当前雁宝表情状态（用于切换图片） */
    private val _yanbaoMood = MutableStateFlow(YanbaoMood.NORMAL)
    val yanbaoMood: StateFlow<YanbaoMood> = _yanbaoMood.asStateFlow()

    /** 喂食动画是否正在播放 */
    private val _isFeedAnimating = MutableStateFlow(false)
    val isFeedAnimating: StateFlow<Boolean> = _isFeedAnimating.asStateFlow()

    /** 喂食结果消息（短暂显示后清除） */
    private val _feedMessage = MutableStateFlow<String?>(null)
    val feedMessage: StateFlow<String?> = _feedMessage.asStateFlow()

    /** 是否显示升级弹窗 */
    private val _showLevelUpDialog = MutableStateFlow(false)
    val showLevelUpDialog: StateFlow<Boolean> = _showLevelUpDialog.asStateFlow()

    /** 升级弹窗数据 */
    private val _levelUpData = MutableStateFlow<LevelUpData?>(null)
    val levelUpData: StateFlow<LevelUpData?> = _levelUpData.asStateFlow()

    /** 是否显示次数耗尽提示 */
    private val _showLimitDialog = MutableStateFlow(false)
    val showLimitDialog: StateFlow<Boolean> = _showLimitDialog.asStateFlow()

    /** 次数耗尽提示消息 */
    private val _limitMessage = MutableStateFlow("")
    val limitMessage: StateFlow<String> = _limitMessage.asStateFlow()

    /** 累计喂食总次数 */
    private val _totalFeedCount = MutableStateFlow(0)
    val totalFeedCount: StateFlow<Int> = _totalFeedCount.asStateFlow()

    /** 是否正在加载 */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observePrivilege()
        observeTotalFeedCount()
        loadTodayStatus()
    }

    // ─── 数据观察 ──────────────────────────────────────────────────────────

    private fun observePrivilege() {
        viewModelScope.launch {
            gardenRepository.observePrivilege()
                .catch { e -> Log.e(TAG, "observePrivilege error", e) }
                .collect { _privilege.value = it }
        }
    }

    private fun observeTotalFeedCount() {
        viewModelScope.launch {
            gardenRepository.observeTotalFeedCount()
                .catch { e -> Log.e(TAG, "observeTotalFeedCount error", e) }
                .collect { _totalFeedCount.value = it }
        }
    }

    private fun loadTodayStatus() {
        viewModelScope.launch {
            try {
                _todayStatus.value = gardenRepository.getTodayFeedStatus()
            } catch (e: Exception) {
                Log.e(TAG, "loadTodayStatus error", e)
            }
        }
    }

    // ─── 喂食动作 ──────────────────────────────────────────────────────────

    /**
     * 普通喂食（每日上限 3 次）
     */
    fun feedCarrot() {
        if (_isFeedAnimating.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _isFeedAnimating.value = true

            try {
                val result = gardenRepository.feedCarrot(YanbaoGardenFeedEntity.FeedSource.NORMAL)
                handleFeedResult(result)
            } catch (e: Exception) {
                Log.e(TAG, "feedCarrot error", e)
                _feedMessage.value = "喂食失败，请稍后重试"
            } finally {
                _isLoading.value = false
                // 动画播放 1.5 秒后恢复
                delay(1500)
                _isFeedAnimating.value = false
                _yanbaoMood.value = YanbaoMood.NORMAL
                loadTodayStatus()
            }
        }
    }

    /**
     * 分享奖励喂食（每日上限 5 次，通过分享解锁）
     */
    fun feedCarrotByShare() {
        if (_isFeedAnimating.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _isFeedAnimating.value = true

            try {
                val result = gardenRepository.feedCarrot(YanbaoGardenFeedEntity.FeedSource.SHARE)
                handleFeedResult(result)
            } catch (e: Exception) {
                Log.e(TAG, "feedCarrotByShare error", e)
                _feedMessage.value = "分享奖励领取失败，请稍后重试"
            } finally {
                _isLoading.value = false
                delay(1500)
                _isFeedAnimating.value = false
                _yanbaoMood.value = YanbaoMood.NORMAL
                loadTodayStatus()
            }
        }
    }

    private fun handleFeedResult(result: FeedResult) {
        when (result) {
            is FeedResult.Success -> {
                _yanbaoMood.value = reactionToMood(result.reaction)
                _feedMessage.value = buildSuccessMessage(result.pointsEarned, result.remainingToday)
                clearMessageAfterDelay()
                Log.i(TAG, "feed_success: points=${result.pointsEarned}, remaining=${result.remainingToday}")
            }
            is FeedResult.LevelUp -> {
                _yanbaoMood.value = YanbaoMood.HAPPY
                _levelUpData.value = LevelUpData(
                    newLevel = result.newLevel,
                    levelName = result.levelName,
                    unlockedPrivileges = result.unlockedPrivileges
                )
                _showLevelUpDialog.value = true
                Log.i(TAG, "level_up: level=${result.newLevel}, name=${result.levelName}")
            }
            is FeedResult.LimitReached -> {
                _yanbaoMood.value = YanbaoMood.THINKING
                _limitMessage.value = result.message
                _showLimitDialog.value = true
                Log.i(TAG, "limit_reached: remainingShare=${result.remainingShareBonus}")
            }
        }
    }

    private fun buildSuccessMessage(points: Int, remaining: Int): String {
        return when (remaining) {
            0 -> "雁宝吃得好满足！今日胡萝卜已用完，分享给朋友可以再喂哦~"
            1 -> "雁宝开心地吃掉了！今天还剩 1 次机会"
            else -> "雁宝美滋滋！获得 $points 积分，今天还可以喂 $remaining 次"
        }
    }

    private fun clearMessageAfterDelay() {
        viewModelScope.launch {
            delay(3000)
            _feedMessage.value = null
        }
    }

    // ─── 弹窗控制 ──────────────────────────────────────────────────────────

    fun dismissLevelUpDialog() {
        _showLevelUpDialog.value = false
    }

    fun dismissLimitDialog() {
        _showLimitDialog.value = false
    }

    // ─── 辅助函数 ──────────────────────────────────────────────────────────

    private fun reactionToMood(reaction: String): YanbaoMood {
        return when (reaction) {
            "HAPPY" -> YanbaoMood.HAPPY
            "PLAYFUL" -> YanbaoMood.PLAYFUL
            "CARING" -> YanbaoMood.CARING
            "THINKING" -> YanbaoMood.THINKING
            "COOL" -> YanbaoMood.COOL
            else -> YanbaoMood.HAPPY
        }
    }

    /** 获取特权等级名称 */
    fun getPrivilegeLevelName(level: Int): String {
        return YanbaoGardenRepository.PRIVILEGE_THRESHOLDS.getOrNull(level)?.second ?: "普通雁宝"
    }

    /** 获取下一等级所需喂食次数 */
    fun getNextLevelThreshold(currentLevel: Int): Int {
        return YanbaoGardenRepository.PRIVILEGE_THRESHOLDS.getOrNull(currentLevel + 1)?.first ?: Int.MAX_VALUE
    }

    /** 计算到下一等级的进度（0f~1f） */
    fun calculateProgress(totalFeed: Int, currentLevel: Int): Float {
        val currentThreshold = YanbaoGardenRepository.PRIVILEGE_THRESHOLDS.getOrNull(currentLevel)?.first ?: 0
        val nextThreshold = YanbaoGardenRepository.PRIVILEGE_THRESHOLDS.getOrNull(currentLevel + 1)?.first
            ?: return 1f
        if (nextThreshold == currentThreshold) return 1f
        return ((totalFeed - currentThreshold).toFloat() / (nextThreshold - currentThreshold)).coerceIn(0f, 1f)
    }
}

// ─── 枚举与数据类 ─────────────────────────────────────────────────────────────

enum class YanbaoMood {
    NORMAL,   // 默认站立
    HAPPY,    // 开心（吃到胡萝卜）
    PLAYFUL,  // 俏皮（眨眼）
    CARING,   // 温柔（抱心）
    THINKING, // 思考（次数耗尽）
    COOL      // 酷炫（高等级）
}

data class LevelUpData(
    val newLevel: Int,
    val levelName: String,
    val unlockedPrivileges: List<String>
)
