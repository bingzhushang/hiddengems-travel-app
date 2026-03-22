package com.hiddengems.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiddengems.data.model.User
import com.hiddengems.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = AuthUiState.Loading

            val result = authRepository.login(email, password)
            _isLoading.value = false

            result.fold(
                onSuccess = { authResult ->
                    _uiState.value = AuthUiState.Success(authResult.user)
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState.Error(exception.message ?: "Login failed")
                }
            )
        }
    }

    fun register(email: String, password: String, nickname: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = AuthUiState.Loading

            val result = authRepository.register(email, password, nickname)
            _isLoading.value = false

            result.fold(
                onSuccess = { authResult ->
                    _uiState.value = AuthUiState.Success(authResult.user)
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState.Error(exception.message ?: "Registration failed")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun validateEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
