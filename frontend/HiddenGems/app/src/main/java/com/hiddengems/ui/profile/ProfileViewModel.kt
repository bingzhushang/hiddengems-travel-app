package com.hiddengems.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiddengems.data.model.User
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
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Check if user is logged in
        checkLoginState()
    }

    private fun checkLoginState() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Simulate checking login state
            // In a real app, this would check SharedPreferences or            val savedUserId = "user-123" // Placeholder
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
            val mockUser = User(
                id = userId,
                email = "test@example.com",
                nickname = "旅行爱好者",
                avatar = "https://picsum.photos/seed/user1/200/200",
                membershipType = "free"
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
            // Clear saved credentials
            _uiState.value = ProfileUiState(
                isLoading = false,
                user = null,
                isLoggedIn = false
            )
        }
    }
}
