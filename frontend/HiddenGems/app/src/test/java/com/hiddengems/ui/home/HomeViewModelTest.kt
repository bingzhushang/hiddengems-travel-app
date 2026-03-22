package com.hiddengems.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.hiddengems.data.model.Spot
import com.hiddengems.data.repository.SpotRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var spotRepository: SpotRepository
    private lateinit var viewModel: HomeViewModel

    private val mockSpots = listOf(
        Spot(
            id = "1",
            name = "Hidden Waterfall",
            coverImage = "https://example.com/waterfall.jpg",
            rating = 4.8f,
            distance = 5.2f,
            crowdLevel = "LOW",
            tags = listOf("nature", "waterfall")
        ),
        Spot(
            id = "2",
            name = "Secret Garden",
            coverImage = "https://example.com/garden.jpg",
            rating = 4.5f,
            distance = 3.1f,
            crowdLevel = "MEDIUM",
            tags = listOf("garden", "peaceful")
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        spotRepository = mockk()
        viewModel = HomeViewModel(spotRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is HomeUiState.Loading)
        }
    }

    @Test
    fun `loadRecommendations success updates state to Success`() = runTest {
        // Given
        coEvery { spotRepository.getRecommendations(39.9042, 116.4074, 10) } returns
            Result.success(mockSpots)

        // When
        viewModel.loadRecommendations(39.9042, 116.4074)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is HomeUiState.Success)
            assertEquals(mockSpots, (state as HomeUiState.Success).spots)
        }
        coVerify { spotRepository.getRecommendations(39.9042, 116.4074, 10) }
    }

    @Test
    fun `loadRecommendations failure updates state to Error`() = runTest {
        // Given
        coEvery { spotRepository.getRecommendations(any(), any(), any()) } returns
            Result.failure(Exception("Network error"))

        // When
        viewModel.loadRecommendations(39.9042, 116.4074)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is HomeUiState.Error)
            assertEquals("Network error", (state as HomeUiState.Error).message)
        }
    }

    private fun createPaginatedResponse(spots: List<Spot>) = com.hiddengems.data.model.PaginatedResponse(
        items = spots,
        pagination = com.hiddengems.data.model.Pagination(
            page = 1,
            pageSize = 20,
            total = spots.size,
            totalPages = 1,
            hasMore = false
        )
    )

    @Test
    fun `loadNearbySpots updates nearbySpots state`() = runTest {
        // Given
        coEvery { spotRepository.getNearbySpots(39.9042, 116.4074, 50.0, 1, 20) } returns
            Result.success(createPaginatedResponse(mockSpots))

        // When
        viewModel.loadNearbySpots(39.9042, 116.4074)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.nearbySpots.test {
            val spots = awaitItem()
            assertEquals(2, spots.size)
        }
    }

    @Test
    fun `searchSpots updates searchResults state`() = runTest {
        // Given
        val searchQuery = "waterfall"
        coEvery { spotRepository.searchSpots(searchQuery, 1, 20) } returns
            Result.success(createPaginatedResponse(listOf(mockSpots[0])))

        // When
        viewModel.searchSpots(searchQuery)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.searchResults.test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals("Hidden Waterfall", results[0].name)
        }
    }

    @Test
    fun `clearSearch clears searchResults`() = runTest {
        // Given - set some search results first
        coEvery { spotRepository.searchSpots(any(), any(), any()) } returns
            Result.success(createPaginatedResponse(mockSpots))
        viewModel.searchSpots("test")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearSearch()

        // Then
        viewModel.searchResults.test {
            val results = awaitItem()
            assertTrue(results.isEmpty())
        }
    }

    @Test
    fun `refresh reloads recommendations`() = runTest {
        // Given
        coEvery { spotRepository.getRecommendations(any(), any(), any()) } returns
            Result.success(mockSpots)

        // When
        viewModel.refresh(39.9042, 116.4074)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { spotRepository.getRecommendations(39.9042, 116.4074, 10) }
    }
}
