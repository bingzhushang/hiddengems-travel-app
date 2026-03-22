package com.hiddengems.data.repository

import com.hiddengems.data.model.*
import com.hiddengems.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun generateItinerary(request: ItineraryGenerateRequest): Result<Itinerary> {
        return try {
            val response = apiService.generateItinerary(request)
            if (response.code == 0 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chat(
        message: String,
        sessionId: String? = null,
        location: LocationPoint? = null
    ): Result<ChatResult> {
        return try {
            val request = ChatRequest(message, sessionId, location)
            val response = apiService.chat(request)
            if (response.code == 0 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsage(): Result<AIUsage> {
        return try {
            val response = apiService.getAIUsage()
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
