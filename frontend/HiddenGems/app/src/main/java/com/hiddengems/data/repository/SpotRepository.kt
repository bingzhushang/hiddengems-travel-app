package com.hiddengems.data.repository

import com.hiddengems.data.model.*
import com.hiddengems.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getRecommendations(
        lat: Double,
        lng: Double,
        limit: Int = 10
    ): Result<List<Spot>> {
        return try {
            val response = apiService.getRecommendations(lat, lng, limit)
            if (response.code == 0 && response.data != null) {
                val items = response.data["items"] ?: emptyList()
                Result.success(items)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNearbySpots(
        lat: Double,
        lng: Double,
        radius: Double = 50.0,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<PaginatedResponse<Spot>> {
        return try {
            val response = apiService.getNearbySpots(lat, lng, radius, page, pageSize)
            if (response.code == 0 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchSpots(
        query: String,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<PaginatedResponse<Spot>> {
        return try {
            val response = apiService.searchSpots(query, page, pageSize)
            if (response.code == 0 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSpotDetail(spotId: String): Result<SpotDetail> {
        return try {
            val response = apiService.getSpotDetail(spotId)
            if (response.code == 0 && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(spotId: String): Result<Pair<Boolean, String>> {
        return try {
            // Try to add favorite first
            val response = apiService.addFavorite(spotId)
            if (response.code == 0) {
                val isFavorited = response.data?.get("isFavorited") as? Boolean ?: true
                val message = response.data?.get("message") as? String ?: ""
                Result.success(Pair(isFavorited, message))
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            // If add fails, try to remove
            try {
                val response = apiService.removeFavorite(spotId)
                if (response.code == 0) {
                    val isFavorited = response.data?.get("isFavorited") as? Boolean ?: false
                    val message = response.data?.get("message") as? String ?: ""
                    Result.success(Pair(isFavorited, message))
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
