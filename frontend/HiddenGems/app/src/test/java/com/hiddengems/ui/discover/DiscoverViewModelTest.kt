package com.hiddengems.ui.discover

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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DiscoverViewModel()
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
}
