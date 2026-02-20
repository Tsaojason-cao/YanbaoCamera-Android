package com.yanbao.camera.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.data.model.RecommendPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页 ViewModel
 * 管理推荐内容流，支持点赞、加载更多
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _posts = MutableStateFlow<List<RecommendPost>>(emptyList())
    val posts: StateFlow<List<RecommendPost>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadInitialPosts()
    }

    private fun loadInitialPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(500) // 模拟网络请求
            _posts.value = generateMockPosts(page = 1)
            _isLoading.value = false
        }
    }

    fun loadMorePosts() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            delay(800)
            val currentPage = (_posts.value.size / 10) + 1
            _posts.value = _posts.value + generateMockPosts(page = currentPage)
            _isLoading.value = false
        }
    }

    fun toggleLike(postId: String) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newLikeCount = if (post.isLiked) {
                    val count = parseLikeCount(post.likeCount) - 1
                    formatLikeCount(count)
                } else {
                    val count = parseLikeCount(post.likeCount) + 1
                    formatLikeCount(count)
                }
                post.copy(isLiked = !post.isLiked, likeCount = newLikeCount)
            } else post
        }
    }

    private fun parseLikeCount(count: String): Int {
        return when {
            count.endsWith("k") -> (count.dropLast(1).toFloatOrNull()?.times(1000))?.toInt() ?: 0
            else -> count.toIntOrNull() ?: 0
        }
    }

    private fun formatLikeCount(count: Int): String {
        return when {
            count >= 1000 -> "${String.format("%.1f", count / 1000f)}k"
            else -> count.toString()
        }
    }

    private fun generateMockPosts(page: Int): List<RecommendPost> {
        val startIndex = (page - 1) * 10
        return (startIndex until startIndex + 10).map { index ->
            RecommendPost(
                id = "post_$index",
                userName = listOf(
                    "yanbao_creator", "photo_master", "ai_artist",
                    "lens_lover", "pixel_queen", "snap_guru",
                    "frame_hunter", "light_chaser", "color_wizard", "moment_keeper"
                )[index % 10],
                isVerified = index % 3 == 0,
                location = listOf(
                    "上海 · 静安区", "北京 · 朝阳区", "台北 · 信义区",
                    "东京 · 涩谷", "首尔 · 江南", "纽约 · 曼哈顿"
                )[index % 6],
                timeAgo = listOf("刚刚", "2分钟前", "1小时前", "3小时前", "昨天")[index % 5],
                description = listOf(
                    "用Yanbao AI拍摄这个魔法时刻！光线真的太棒了。#YanbaoAI #摄影 #光影",
                    "2.9D景深模式让背景虚化效果超级自然，人像摄影必备！#景深 #人像",
                    "今天用大师滤镜CN-01拍了这组街景，复古胶片感十足 #滤镜 #街拍",
                    "美颜模式新升级，磨皮效果更自然，再也不怕素颜出镜 #美颜 #自拍",
                    "雁宝记忆功能真的很贴心，可以保存我最喜欢的拍摄参数 #雁宝AI",
                    "iPhone模拟模式 vs 真实iPhone，你能分辨出来吗？#测评 #摄影技巧",
                    "夜景模式拍摄，城市的夜晚如此迷人 #夜景 #城市 #摄影",
                    "AR特效贴纸太可爱了，拍出来的照片朋友圈点赞率超高 #AR #贴纸",
                    "用Yanbao AI记录生活中的每一个美好瞬间 #生活 #记录 #美好",
                    "专业模式手动调节ISO和快门，拍出了这张绝美的光轨照片 #专业模式 #光轨"
                )[index % 10],
                likeCount = listOf("1.2k", "856", "2.3k", "445", "1.8k", "677", "3.1k", "234", "991", "1.5k")[index % 10],
                commentCount = listOf("350", "128", "567", "89", "423", "156", "789", "45", "234", "312")[index % 10],
                shareCount = listOf("45", "23", "89", "12", "67", "34", "123", "8", "56", "78")[index % 10],
                isLiked = index % 4 == 0,
                placeholderColorStart = listOf(
                    0xFFA78BFA, 0xFFEC4899, 0xFF6366F1, 0xFF8B5CF6,
                    0xFFF472B6, 0xFF7C3AED, 0xFFDB2777, 0xFF9333EA,
                    0xFFE879F9, 0xFFC084FC
                )[index % 10].toLong().toInt(),
                placeholderColorEnd = listOf(
                    0xFFEC4899, 0xFFF9A8D4, 0xFFA78BFA, 0xFFEC4899,
                    0xFFA78BFA, 0xFFEC4899, 0xFFF9A8D4, 0xFF6366F1,
                    0xFFA78BFA, 0xFFEC4899
                )[index % 10].toLong().toInt()
            )
        }
    }
}
