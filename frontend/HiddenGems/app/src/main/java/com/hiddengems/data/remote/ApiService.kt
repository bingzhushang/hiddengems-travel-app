package com.hiddengems.data.remote

import com.hiddengems.data.model.*
import retrofit2.http.*

interface ApiService {

    // Auth endpoints
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthResult>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResult>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: Map<String, String>): ApiResponse<Map<String, String>>

    // User endpoints
    @GET("users/me")
    suspend fun getCurrentUser(): ApiResponse<User>

    @PATCH("users/me")
    suspend fun updateUser(@Body request: Map<String, Any>): ApiResponse<User>

    // Spot endpoints
    @GET("spots/recommendations")
    suspend fun getRecommendations(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("limit") limit: Int = 10
    ): ApiResponse<Map<String, List<Spot>>>

    @GET("spots/nearby")
    suspend fun getNearbySpots(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double = 50.0,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PaginatedResponse<Spot>>

    @GET("spots/search")
    suspend fun searchSpots(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PaginatedResponse<Spot>>

    @GET("spots/{spotId}")
    suspend fun getSpotDetail(@Path("spotId") spotId: String): ApiResponse<SpotDetail>

    @POST("spots/{spotId}/favorite")
    suspend fun addFavorite(@Path("spotId") spotId: String): ApiResponse<Map<String, Any>>

    @DELETE("spots/{spotId}/favorite")
    suspend fun removeFavorite(@Path("spotId") spotId: String): ApiResponse<Map<String, Any>>

    // Itinerary endpoints
    @GET("itineraries/me")
    suspend fun getMyItineraries(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): ApiResponse<PaginatedResponse<Itinerary>>

    @GET("itineraries/{itineraryId}")
    suspend fun getItinerary(@Path("itineraryId") itineraryId: String): ApiResponse<Itinerary>

    @POST("itineraries")
    suspend fun createItinerary(@Body request: Map<String, Any>): ApiResponse<Itinerary>

    @PATCH("itineraries/{itineraryId}")
    suspend fun updateItinerary(
        @Path("itineraryId") itineraryId: String,
        @Body request: Map<String, Any>
    ): ApiResponse<Itinerary>

    @DELETE("itineraries/{itineraryId}")
    suspend fun deleteItinerary(@Path("itineraryId") itineraryId: String): ApiResponse<Unit>

    // AI endpoints
    @POST("ai/itinerary/generate")
    suspend fun generateItinerary(@Body request: ItineraryGenerateRequest): ApiResponse<Itinerary>

    @POST("ai/chat")
    suspend fun chat(@Body request: ChatRequest): ApiResponse<ChatResult>

    @GET("ai/usage")
    suspend fun getAIUsage(): ApiResponse<AIUsage>

    // Community endpoints
    @GET("community/feed")
    suspend fun getCommunityFeed(
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PaginatedResponse<Any>> // Replace with Post model

    @POST("community/posts")
    suspend fun createPost(@Body request: Map<String, Any>): ApiResponse<Map<String, String>>
}
