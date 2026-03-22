package com.hiddengems.data.repository

import com.hiddengems.data.model.*
import com.hiddengems.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getFeed(
        type: String = "hot",
        page: Int = 1,
        pageSize: Int = 20
    ): PaginatedResponse<Itinerary> {
        val response = apiService.getCommunityFeed(type, page, pageSize)
        if (response.code == 0 && response.data != null) {
            @Suppress("UNCHECKED_CAST")
            val items = (response.data.items as List<Itinerary>)
            return PaginatedResponse(
                items = items,
                pagination = response.data.pagination
            )
        }
        return PaginatedResponse(emptyList(), Pagination(1, 20, 0, 0, false))
    }

    suspend fun getItineraryDetail(itineraryId: String): Itinerary? {
        val response = apiService.getItinerary(itineraryId)
        return if (response.code == 1) response.data else null
    }

    suspend fun toggleFavorite(itineraryId: String): Boolean {
        val response = apiService.addFavorite(itineraryId)
        return response.code == 1
    }

    suspend fun copyItinerary(itineraryId: String): Itinerary? {
        // This would be a custom endpoint
        // For now, return null as it's not in ApiService
        return null
    }
}
