package com.hiddengems.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.hiddengems.data.model.AuthResult
import com.hiddengems.data.model.User
import com.hiddengems.data.repository.AuthRepository
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
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AuthUiState.Idle)
        }
    }

    @Test
    fun `login success updates state to Success`() = runTest {
        // Given
        val mockUser = User(
            id = "1",
            email = "test@example.com",
            nickname = "TestUser",
            membershipType = "FREE",
            contributionPoints = 0,
            level = 1
        )
        val mockAuthResult = AuthResult(
            user = mockUser,
            token = "test-token",
            refreshToken = "test-refresh-token"
        )
        coEvery { authRepository.login("test@example.com", "password123") } returns
            Result.success(mockAuthResult)

        // When
        viewModel.login("test@example.com", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AuthUiState.Success)
            assertEquals(mockUser, (state as AuthUiState.Success).user)
        }
        coVerify { authRepository.login("test@example.com", "password123") }
    }

    @Test
    fun `login failure updates state to Error`() = runTest {
        // Given
        coEvery { authRepository.login("test@example.com", "wrong") } returns
            Result.failure(Exception("Invalid credentials"))

        // When
        viewModel.login("test@example.com", "wrong")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AuthUiState.Error)
            assertEquals("Invalid credentials", (state as AuthUiState.Error).message)
        }
    }

    @Test
    fun `register success updates state to Success`() = runTest {
        // Given
        val mockUser = User(
            id = "1",
            email = "new@example.com",
            nickname = "NewUser",
            membershipType = "FREE",
            contributionPoints = 0,
            level = 1
        )
        val mockAuthResult = AuthResult(
            user = mockUser,
            token = "test-token",
            refreshToken = "test-refresh-token"
        )
        coEvery { authRepository.register("new@example.com", "password123", "NewUser") } returns
            Result.success(mockAuthResult)

        // When
        viewModel.register("new@example.com", "password123", "NewUser")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AuthUiState.Success)
        }
        coVerify { authRepository.register("new@example.com", "password123", "NewUser") }
    }

    @Test
    fun `logout clears state to Idle`() = runTest {
        // Given
        coEvery { authRepository.logout() } returns Unit

        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AuthUiState.Idle)
        }
        coVerify { authRepository.logout() }
    }

    @Test
    fun `isLoading is true during login`() = runTest {
        // Given
        val mockUser = User(
            id = "1",
            email = "test@test.com",
            nickname = "Test",
            membershipType = "FREE",
            contributionPoints = 0,
            level = 1
        )
        coEvery { authRepository.login(any(), any()) } returns
            Result.success(AuthResult(mockUser, "token", "refresh"))

        // When
        viewModel.login("test@example.com", "password")
        // Don't advance scheduler yet

        // Then - should be loading
        assertTrue(viewModel.isLoading.value)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `validateEmail returns false for invalid email`() = runTest {
        assertFalse(viewModel.validateEmail(""))
        assertFalse(viewModel.validateEmail("invalid"))
        assertFalse(viewModel.validateEmail("invalid@"))
        assertFalse(viewModel.validateEmail("@domain.com"))
    }

    @Test
    fun `validateEmail returns true for valid email`() = runTest {
        assertTrue(viewModel.validateEmail("test@example.com"))
        assertTrue(viewModel.validateEmail("user.name@domain.co.uk"))
    }

    @Test
    fun `validatePassword returns false for short password`() = runTest {
        assertFalse(viewModel.validatePassword(""))
        assertFalse(viewModel.validatePassword("12345"))
    }

    @Test
    fun `validatePassword returns true for valid password`() = runTest {
        assertTrue(viewModel.validatePassword("123456"))
        assertTrue(viewModel.validatePassword("password123"))
    }
}
