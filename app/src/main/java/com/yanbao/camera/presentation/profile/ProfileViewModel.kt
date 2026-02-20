package com.yanbao.camera.presentation.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class UserProfile(
    val userName: String = "Yanbao Creator",
    val userId: String = "12345678",
    val memberNumber: String = "88888",
    val remainingDays: Int = 365,
    val location: String = "上海 · 静安区"
)

data class WorkItem(
    val id: String,
    val colorStart: Long,
    val colorEnd: Long,
    val likeCount: String
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

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
