package com.yanbao.camera.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanbao.camera.model.LocationCard
import com.yanbao.camera.model.Post
import com.yanbao.camera.model.User
import com.yanbao.camera.repository.MockDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 首页ViewModel - 管理推荐流和推荐内容
 */
class HomeViewModel : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _locations = MutableStateFlow<List<LocationCard>>(emptyList())
    val locations: StateFlow<List<LocationCard>> = _locations

    private val _recommendedUsers = MutableStateFlow<List<User>>(emptyList())
    val recommendedUsers: StateFlow<List<User>> = _recommendedUsers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentPage = MutableStateFlow(0)

    init {
        loadInitialData()
    }

    /**
     * 加载初始数据
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // 加载推荐流
                val posts = try {
                    MockDataRepository.getRecommendedPosts(0, 10)
                } catch (e: Exception) {
                    emptyList()
                }
                _posts.value = posts
                
                // 加载推荐位置
                val locations = try {
                    MockDataRepository.getRecommendedLocations()
                } catch (e: Exception) {
                    emptyList()
                }
                _locations.value = locations
                
                // 加载推荐用户
                val users = try {
                    MockDataRepository.getRecommendedUsers()
                } catch (e: Exception) {
                    emptyList()
                }
                _recommendedUsers.value = users
                
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载更多推荐内容
     */
    fun loadMorePosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _currentPage.value++
            val newPosts = MockDataRepository.getRecommendedPosts(_currentPage.value, 10)
            val current = _posts.value.toMutableList()
            current.addAll(newPosts)
            _posts.value = current
            _isLoading.value = false
        }
    }

    /**
     * 点赞Post
     */
    fun likePost(post: Post) {
        viewModelScope.launch {
            val updated = _posts.value.map { p ->
                if (p.id == post.id) {
                    p.copy(
                        isLiked = !p.isLiked,
                        likes = if (!p.isLiked) p.likes + 1 else p.likes - 1
                    )
                } else {
                    p
                }
            }
            _posts.value = updated
        }
    }

    /**
     * 刷新推荐流
     */
    fun refreshPosts() {
        viewModelScope.launch {
            _currentPage.value = 0
            loadInitialData()
        }
    }

    /**
     * 关注用户
     */
    fun followUser(user: User) {
        viewModelScope.launch {
            val updated = _recommendedUsers.value.map { u ->
                if (u.id == user.id) {
                    u.copy(isFollowed = !u.isFollowed)
                } else {
                    u
                }
            }
            _recommendedUsers.value = updated
        }
    }

    /**
     * 搜索Post
     */
    fun searchPosts(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadInitialData()
            } else {
                // TODO: 实现搜索功能
            }
        }
    }
}
