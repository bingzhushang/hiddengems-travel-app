package com.hiddengems.ui.community

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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CommunityViewModel()
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
