package com.hiddengems.ui.spot

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.hiddengems.data.model.SpotDetail
import com.hiddengems.data.model.SpotLocation
import com.hiddengems.data.model.SpotImage
import com.hiddengems.data.model.OpeningHours
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SpotDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var spotRepository: SpotRepository
    private lateinit var viewModel: SpotDetailViewModel

    private val mockSpotDetail = SpotDetail(
        id = "1",
        name = "Hidden Waterfall",
        coverImage = "https://example.com/waterfall.jpg",
        rating = 4.8f,
        reviewCount = 10,
        crowdLevel = "LOW",
        tags = listOf("nature", "waterfall"),
        description = "A beautiful hidden waterfall",
        location = SpotLocation(
            lat = 39.9042,
            lng = 116.4074,
            country = "China",
            address = "Mountain Road 123"
        ),
        openingHours = mapOf("default" to OpeningHours("09:00", "18:00")),
        ticketPrice = 0f,
        ticketInfo = "Free",
        bestSeasons = listOf("spring", "summer"),
        bestTimeOfDay = listOf("morning"),
        images = listOf(
            SpotImage("photo1.jpg"),
            SpotImage("photo2.jpg")
        ),
        isFavorited = false,
        nearbySpots = emptyList()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        spotRepository = mockk()
        viewModel = SpotDetailViewModel(spotRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SpotDetailUiState.Loading)
        }
    }

    @Test
    fun `loadSpotDetail success updates state to Success`() = runTest {
        // Given
        coEvery { spotRepository.getSpotDetail("1") } returns
            Result.success(mockSpotDetail)

        // When
        viewModel.loadSpotDetail("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SpotDetailUiState.Success)
            assertEquals(mockSpotDetail, (state as SpotDetailUiState.Success).spotDetail)
        }
        coVerify { spotRepository.getSpotDetail("1") }
    }

    @Test
    fun `loadSpotDetail failure updates state to Error`() = runTest {
        // Given
        coEvery { spotRepository.getSpotDetail("999") } returns
            Result.failure(Exception("Spot not found"))

        // When
        viewModel.loadSpotDetail("999")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SpotDetailUiState.Error)
            assertEquals("Spot not found", (state as SpotDetailUiState.Error).message)
        }
    }

    @Test
    fun `toggleFavorite adds to favorites when not favorited`() = runTest {
        // Given
        coEvery { spotRepository.getSpotDetail("1") } returns
            Result.success(mockSpotDetail.copy(isFavorited = false))
        coEvery { spotRepository.toggleFavorite("1") } returns
            Result.success(Pair(true, "Added to favorites"))

        viewModel.loadSpotDetail("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { spotRepository.toggleFavorite("1") }
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SpotDetailUiState.Success)
            assertTrue((state as SpotDetailUiState.Success).spotDetail.isFavorited)
        }
    }

    @Test
    fun `toggleFavorite removes from favorites when favorited`() = runTest {
        // Given
        coEvery { spotRepository.getSpotDetail("1") } returns
            Result.success(mockSpotDetail.copy(isFavorited = true))
        coEvery { spotRepository.toggleFavorite("1") } returns
            Result.success(Pair(false, "Removed from favorites"))

        viewModel.loadSpotDetail("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SpotDetailUiState.Success)
            assertFalse((state as SpotDetailUiState.Success).spotDetail.isFavorited)
        }
    }

    @Test
    fun `isFavorited reflects current favorite state`() = runTest {
        // Given
        coEvery { spotRepository.getSpotDetail("1") } returns
            Result.success(mockSpotDetail.copy(isFavorited = true))

        // When
        viewModel.loadSpotDetail("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.isFavorited.value)
    }
}
