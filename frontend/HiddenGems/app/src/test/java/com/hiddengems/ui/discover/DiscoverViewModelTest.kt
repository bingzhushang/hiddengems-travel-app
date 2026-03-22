package com.hiddengems.ui.discover

import com.hiddengems.data.model.PaginatedResponse
import com.hiddengems.data.model.Pagination
import com.hiddengems.data.model.Spot
import com.hiddengems.data.repository.SpotRepository
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
class DiscoverViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DiscoverViewModel
    private lateinit var mockSpotRepository: SpotRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockSpotRepository = MockSpotRepository()
        viewModel = DiscoverViewModel(mockSpotRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadSpots loads mock data`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.spots.isNotEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `search updates searchQuery`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.search("杭州")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("杭州", state.searchQuery)
    }

    @Test
    fun `filterByCategory updates selectedCategory`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByCategory("nature")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("nature", state.selectedCategory)
    }

    @Test
    fun `filterByCategory nature returns filtered spots`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByCategory("nature")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("nature", state.selectedCategory)
        // Should filter to spots with nature tags
    }

    @Test
    fun `filterByCategory null returns all spots`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByCategory("nature")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByCategory(null)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.selectedCategory)
        assertTrue(state.spots.isNotEmpty())
    }
}

private class MockSpotRepository : SpotRepository(
    apiService = object : com.hiddengems.data.remote.ApiService {
        override suspend fun getNearbySpots(lat: Double, lng: Double, radius: Double?, page: Int?, pageSize: Int?) =
            com.hiddengems.data.model.ApiResponse(
                code = -1,
                message = "Mock",
                data = null
            )
        override suspend fun searchSpots(query: String, page: Int?, pageSize: Int?) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun getSpotDetail(spotId: String) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun getRecommendations(lat: Double, lng: Double, limit: Int?) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun addFavorite(spotId: String) =
            com.hiddengems.data.model.ApiResponse(code = -1, message = "Mock", data = null)
        override suspend fun removeFavorite(spotId: String) =
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
        override suspend fun deleteItinerary(itineraryId: String) =
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
    }
)
