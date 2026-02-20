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

@HiltViewModel
class RecommendViewModel @Inject constructor() : ViewModel() {

    private val _posts = MutableStateFlow<List<RecommendPost>>(emptyList())
    val posts: StateFlow<List<RecommendPost>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(400)
            _posts.value = (0 until 20).map { index ->
                RecommendPost(
                    id = "rec_$index",
                    userName = listOf("photo_master", "ai_artist", "lens_lover", "pixel_queen", "snap_guru")[index % 5],
                    description = "精彩作品 #$index",
                    likeCount = "${(100 + index * 37) % 9999}",
                    commentCount = "${(20 + index * 11) % 999}",
                    shareCount = "${(5 + index * 7) % 99}",
                    isLiked = index % 3 == 0,
                    placeholderColorStart = listOf(
                        0xFFA78BFA, 0xFFEC4899, 0xFF6366F1, 0xFF8B5CF6, 0xFFF472B6
                    )[index % 5].toInt(),
                    placeholderColorEnd = listOf(
                        0xFFEC4899, 0xFFF9A8D4, 0xFFA78BFA, 0xFFEC4899, 0xFFA78BFA
                    )[index % 5].toInt()
                )
            }
            _isLoading.value = false
        }
    }
}
