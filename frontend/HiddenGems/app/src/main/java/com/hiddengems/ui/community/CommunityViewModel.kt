package com.hiddengems.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiddengems.data.model.Itinerary
import com.hiddengems.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val isLoading: Boolean = false,
    val itineraries: List<Itinerary> = emptyList(),
    val selectedTab: CommunityTab = CommunityTab.HOT,
    val error: String? = null
)

enum class CommunityTab {
    HOT,
    NEW,
    FOLLOWING
}

@HiltViewModel
class CommunityViewModel @Inject constructor(
    // private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadCommunityItineraries()
    }

    fun loadCommunityItineraries() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Simulate API call
            kotlinx.coroutines.delay(500)

            // Mock data
            val mockItineraries = listOf(
                createMockItinerary(
                    id = "comm-1",
                    title = "杭州小众3日深度游",
                    userName = "旅行达人小王",
                    avatarUrl = "https://picsum.photos/seed/user1/100/100",
                    viewCount = 1234,
                    favoriteCount = 89,
                    copyCount = 23
                ),
                createMockItinerary(
                    id = "comm-2",
                    title = "云南秘境探索",
                    userName = "背包客小李",
                    avatarUrl = "https://picsum.photos/seed/user2/100/100",
                    viewCount = 987,
                    favoriteCount = 56,
                    copyCount = 12
                ),
                createMockItinerary(
                    id = "comm-3",
                    title = "川西秋色7日游",
                    userName = "摄影师老张",
                    avatarUrl = "https://picsum.photos/seed/user3/100/100",
                    viewCount = 2341,
                    favoriteCount = 156,
                    copyCount = 45
                ),
                createMockItinerary(
                    id = "comm-4",
                    title = "江南水乡慢游",
                    userName = "文艺青年",
                    avatarUrl = "https://picsum.photos/seed/user4/100/100",
                    viewCount = 678,
                    favoriteCount = 42,
                    copyCount = 8
                ),
                createMockItinerary(
                    id = "comm-5",
                    title = "新疆自驾15天",
                    userName = "自驾达人",
                    avatarUrl = "https://picsum.photos/seed/user5/100/100",
                    viewCount = 3456,
                    favoriteCount = 234,
                    copyCount = 67
                )
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                itineraries = mockItineraries
            )
        }
    }

    fun selectTab(tab: CommunityTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        loadCommunityItineraries()
    }

    private fun createMockItinerary(
        id: String,
        title: String,
        userName: String,
        avatarUrl: String,
        viewCount: Int,
        favoriteCount: Int,
        copyCount: Int
    ): Itinerary {
        return Itinerary(
            id = id,
            userId = "user-$id",
            title = title,
            description = "这是一次难忘的旅行体验，推荐给大家！精心规划的路线，带你发现小众秘境。",
            coverImage = "https://picsum.photos/seed/$id/400/300",
            startDate = java.util.Date(),
            endDate = java.util.Date(),
            daysCount = 3,
            destination = "中国",
            isAiGenerated = true,
            status = "PUBLISHED",
            isPublic = true,
            viewCount = viewCount,
            favoriteCount = favoriteCount,
            copyCount = copyCount,
            travelStyle = listOf("自然", "人文"),
            items = emptyList(),
            user = User(
                id = "user-$id",
                nickname = userName,
                avatar = avatarUrl
            )
        )
    }
}
