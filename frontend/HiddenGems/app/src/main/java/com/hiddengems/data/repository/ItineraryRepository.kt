package com.hiddengems.data.repository

import com.hiddengems.data.model.*
import com.hiddengems.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItineraryRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getMyItineraries(
        status: String? = null,
        page: Int = 1,
        pageSize: Int = 10
    ): Result<PaginatedResponse<Itinerary>> {
        return try {
            val response = apiService.getMyItineraries(status, page, pageSize)
            if (response.code == 0 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItinerary(itineraryId: String): Result<Itinerary> {
        return try {
            val response = apiService.getItinerary(itineraryId)
            if (response.code == 0 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createItinerary(
        title: String,
        startDate: String,
        endDate: String,
        destination: String? = null
    ): Result<Itinerary> {
        return try {
            val request = mapOf(
                "title" to title,
                "startDate" to startDate,
                "endDate" to endDate,
                "destination" to destination
            )
            val response = apiService.createItinerary(request)
            if (response.code == 0 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateItinerary(
        request: ItineraryGenerateRequest
    ): Result<Itinerary> {
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

    suspend fun deleteItinerary(itineraryId: String): Result<Unit> {
        return try {
            val response = apiService.deleteItinerary(itineraryId)
            if (response.code == 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
