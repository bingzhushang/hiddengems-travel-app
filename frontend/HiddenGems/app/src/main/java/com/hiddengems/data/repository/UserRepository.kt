package com.hiddengems.data.repository

import com.hiddengems.data.model.User
import com.hiddengems.data.model.UserStats
import com.hiddengems.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCurrentUser(): User {
        return apiService.getCurrentUser().data!!
    }

    suspend fun updateUser(nickname: String?, avatar: String?, bio: String?): User {
        val updates = mutableMapOf<String, Any>()
        nickname?.let { updates["nickname"] = it }
        avatar?.let { updates["avatar"] = it }
        bio?.let { updates["bio"] = it }

        return apiService.updateUser(updates).data!!
    }

    suspend fun getUserStats(): UserStats {
        // This would typically be a separate API call
        // For now, return from the user object's stats
        return getCurrentUser().stats ?: UserStats()
    }
}
