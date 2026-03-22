package com.hiddengems.ui.itinerary

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.hiddengems.data.model.Itinerary
import com.hiddengems.data.model.ItineraryGenerateRequest
import com.hiddengems.data.model.PaginatedResponse
import com.hiddengems.data.repository.ItineraryRepository
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
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ItineraryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var itineraryRepository: ItineraryRepository
    private lateinit var viewModel: ItineraryViewModel

    private val mockItinerary = Itinerary(
        id = "1",
        userId = "user1",
        title = "Beijing Adventure",
        startDate = "2024-03-01",
        endDate = "2024-03-05",
        daysCount = 5,
        destination = "Beijing",
        status = "DRAFT",
        isPublic = false,
        isAiGenerated = false,
        viewCount = 0,
        favoriteCount = 0,
        copyCount = 0,
        travelStyle = emptyList(),
        items = emptyList()
    )

    private fun createPaginatedResponse(itineraries: List<Itinerary>) = com.hiddengems.data.model.PaginatedResponse(
        items = itineraries,
        pagination = com.hiddengems.data.model.Pagination(
            page = 1,
            pageSize = 10,
            total = itineraries.size,
            totalPages = 1,
            hasMore = false
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        itineraryRepository = mockk()
        viewModel = ItineraryViewModel(itineraryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ItineraryUiState.Loading)
        }
    }

    @Test
    fun `loadItineraries success updates state to Success`() = runTest {
        // Given
        coEvery { itineraryRepository.getMyItineraries(null, 1, 10) } returns
            Result.success(createPaginatedResponse(listOf(mockItinerary)))

        // When
        viewModel.loadItineraries()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ItineraryUiState.Success)
            assertEquals(1, (state as ItineraryUiState.Success).itineraries.size)
        }
        coVerify { itineraryRepository.getMyItineraries(null, 1, 10) }
    }

    @Test
    fun `loadItineraries failure updates state to Error`() = runTest {
        // Given
        coEvery { itineraryRepository.getMyItineraries(any(), any(), any()) } returns
            Result.failure(Exception("Failed to load"))

        // When
        viewModel.loadItineraries()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ItineraryUiState.Error)
            assertEquals("Failed to load", (state as ItineraryUiState.Error).message)
        }
    }

    @Test
    fun `createItinerary success adds new itinerary`() = runTest {
        // Given
        coEvery {
            itineraryRepository.createItinerary("New Trip", "2024-04-01", "2024-04-05", null)
        } returns Result.success(mockItinerary.copy(id = "2", title = "New Trip"))

        // When
        viewModel.createItinerary("New Trip", "2024-04-01", "2024-04-05")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { itineraryRepository.createItinerary("New Trip", "2024-04-01", "2024-04-05", null) }
    }

    @Test
    fun `deleteItinerary removes from list`() = runTest {
        // Given
        coEvery { itineraryRepository.deleteItinerary("1") } returns
            Result.success(Unit)

        // When
        viewModel.deleteItinerary("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { itineraryRepository.deleteItinerary("1") }
    }

    @Test
    fun `generateItineraryWithAI creates AI-generated itinerary`() = runTest {
        // Given
        val request = ItineraryGenerateRequest(
            destination = "Shanghai",
            startDate = "2024-05-01",
            endDate = "2024-05-03",
            travelStyles = listOf("food", "culture"),
            budgetLevel = "MEDIUM"
        )
        val generatedItinerary = mockItinerary.copy(
            id = "ai-1",
            title = "AI Shanghai Trip",
            destination = "Shanghai"
        )
        coEvery { itineraryRepository.generateItinerary(request) } returns
            Result.success(generatedItinerary)

        // When
        viewModel.generateItineraryWithAI(request)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { itineraryRepository.generateItinerary(request) }
        viewModel.generatedItinerary.test {
            val result = awaitItem()
            assertEquals("AI Shanghai Trip", result?.title)
        }
    }

    @Test
    fun `filterByStatus filters itineraries`() = runTest {
        // Given
        val draftItinerary = mockItinerary.copy(status = "DRAFT")
        coEvery { itineraryRepository.getMyItineraries("DRAFT", 1, 10) } returns
            Result.success(createPaginatedResponse(listOf(draftItinerary)))

        // When
        viewModel.filterByStatus("DRAFT")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { itineraryRepository.getMyItineraries("DRAFT", 1, 10) }
    }

    @Test
    fun `refresh reloads itineraries`() = runTest {
        // Given
        coEvery { itineraryRepository.getMyItineraries(any(), any(), any()) } returns
            Result.success(createPaginatedResponse(listOf(mockItinerary)))

        // When
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { itineraryRepository.getMyItineraries(null, 1, 10) }
    }
}
