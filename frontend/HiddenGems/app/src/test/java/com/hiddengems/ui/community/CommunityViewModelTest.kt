package com.hiddengems.ui.community

import com.hiddengems.data.model.Itinerary
import com.hiddengems.data.model.PaginatedResponse
import com.hiddengems.data.model.Pagination
import com.hiddengems.data.model.User
import com.hiddengems.data.repository.CommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CommunityViewModel
    private val mockRepository = MockCommunityRepository()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CommunityViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has hot tab selected`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CommunityTab.HOT, state.selectedTab)
    }

    @Test
    fun `selectTab changes tab and reloads data`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectTab(CommunityTab.NEW)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CommunityTab.NEW, state.selectedTab)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadCommunityItineraries loads mock data`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.itineraries.isNotEmpty())
        assertNull(state.error)
    }
}

private class MockCommunityRepository : CommunityRepository(
    apiService = object : com.hiddengems.data.remote.ApiService {
        override suspend fun getNearbySpots(lat: Double, lng: Double, radius: Double?, page: Int?, pageSize: Int?) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun searchSpots(query: String, page: Int?, pageSize: Int?) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun getSpotDetail(spotId: String) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun getRecommendations(lat: Double, lng: Double, limit: Int?) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun addFavorite(spotId: String) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun removeFavorite(favoriteId: String) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun getCurrentUser() =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun updateUser(updates: Map<String, Any>) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun getMyItineraries(status: String?, page: Int?, pageSize: Int?) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun getItinerary(itineraryId: String) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun createItinerary(request: Map<String, Any?>) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun generateItinerary(request: com.hiddengems.data.model.ItineraryGenerateRequest) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun getCommunityFeed(type: String, page: Int?, pageSize: Int?) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun chat(request: com.hiddengems.data.model.ChatRequest) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun getAIUsage() =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun login(request: com.hiddengems.data.model.LoginRequest) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun register(request: com.hiddengems.data.model.RegisterRequest) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun refreshToken(refreshToken: String) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun deleteItinerary(itineraryId: String) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
    }
) {
    override suspend fun getFeed(type: String, page: Int, pageSize: Int): PaginatedResponse<Itinerary> {
        val mockItinerary = Itinerary(
            id = "mock-1",
            userId = "user-1",
            title = "测试行程",
            description = "这是一个测试行程",
            coverImage = null,
            startDate = "2025-01-01",
            endDate = "2025-01-03",
            daysCount = 3,
            destination = "测试目的地",
            isAiGenerated = false,
            status = "PUBLISHED",
            isPublic = true,
            viewCount = 100,
            favoriteCount = 10,
            copyCount = 5,
            travelStyle = listOf("自然", "人文"),
            items = emptyList(),
            user = User(id = "user-1", nickname = "测试用户")
        )
        return PaginatedResponse(
            items = listOf(mockItinerary),
            pagination = Pagination(1, 10, 1, 1, false)
        )
    }
}
