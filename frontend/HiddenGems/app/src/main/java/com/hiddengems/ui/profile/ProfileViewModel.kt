package com.hiddengems.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiddengems.data.model.User
import com.hiddengems.data.model.UserStats
import com.hiddengems.data.repository.UserRepository
import com.hiddengems.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        checkLoginState()
    }

    private fun checkLoginState() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Check if user has a valid token
            val token = tokenManager.getToken()

            if (token != null) {
                loadUserProfile()
            } else {
                // For demo purposes, show logged in state with mock user
                // In production, this would show logged out state
                val mockUser = getMockUser()
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    user = mockUser,
                    isLoggedIn = true
                )
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val user = userRepository.getCurrentUser()
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    user = user,
                    isLoggedIn = true
                )
            } catch (e: Exception) {
                // Fallback to mock user if API fails
                val mockUser = getMockUser()
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    user = mockUser,
                    isLoggedIn = true
                )
            }
        }
    }

    private fun getMockUser(): User {
        return User(
            id = "user-123",
            email = "test@example.com",
            nickname = "旅行爱好者",
            avatar = "https://picsum.photos/seed/user1/200/200",
            bio = "热爱探索小众目的地，喜欢徒步和摄影",
            membershipType = "pro",
            membershipExpireAt = "2025-12-31",
            contributionPoints = 1250,
            level = 5,
            stats = UserStats(
                favoriteCount = 48,
                itineraryCount = 12,
                visitedCount = 86,
                reviewCount = 23
            )
        )
    }

    fun logout() {
        viewModelScope.launch {
            // Clear saved credentials
            tokenManager.clearTokens()
            _uiState.value = ProfileUiState(
                isLoading = false,
                user = null,
                isLoggedIn = false
            )
        }
    }

    fun refresh() {
        checkLoginState()
    }
}
