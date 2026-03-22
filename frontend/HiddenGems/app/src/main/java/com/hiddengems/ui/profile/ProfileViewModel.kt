package com.hiddengems.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiddengems.data.model.User
import com.hiddengems.data.model.UserStats
import com.hiddengems.data.repository.UserRepository
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
    // private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Check if user is logged in
        checkLoginState()
    }

    private fun checkLoginState() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Simulate checking login state
            // In a real app, this would check SharedPreferences or DataStore
            kotlinx.coroutines.delay(300)

            // For demo purposes, show logged in state
            val savedUserId = "user-123" // Placeholder - set to null to show logged out state
            if (savedUserId != null) {
                loadUserProfile(savedUserId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = false
                )
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Simulate API call
            kotlinx.coroutines.delay(500)

            val mockUser = User(
                id = userId,
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

            _uiState.value = ProfileUiState(
                isLoading = false,
                user = mockUser,
                isLoggedIn = true
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Clear saved credentials (would clear DataStore/SharedPreferences in real app)
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
