package com.hiddengems.data.repository

import com.hiddengems.data.local.TokenManager
import com.hiddengems.data.model.*
import com.hiddengems.data.remote.ApiService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<AuthResult> {
        return try {
            val response = apiService.login(
                LoginRequest(email, password)
            )
            if (response.code == 0 && response.data != null) {
                // Save tokens
                tokenManager.saveToken(response.data.token)
                tokenManager.saveRefreshToken(response.data.refreshToken)
                tokenManager.saveUserId(response.data.user.id)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, nickname: String): Result<AuthResult> {
        return try {
            val response = apiService.register(
                RegisterRequest(email, password, nickname)
            )
            if (response.code == 0 && response.data != null) {
                tokenManager.saveToken(response.data.token)
                tokenManager.saveRefreshToken(response.data.refreshToken)
                tokenManager.saveUserId(response.data.user.id)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearTokens()
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenManager.hasValidToken()
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.code == 0 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
