package com.hiddengems.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
    val meta: Meta? = null
)

@Serializable
data class Meta(
    val timestamp: String,
    @SerialName("requestId") val requestId: String
)

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val pagination: Pagination
)

@Serializable
data class Pagination(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int,
    val hasMore: Boolean
)

// User models
@Serializable
data class User(
    val id: String,
    val email: String,
    val nickname: String,
    val avatar: String? = null,
    @SerialName("membershipType") val membershipType: String,
    @SerialName("membershipExpireAt") val membershipExpireAt: String? = null,
    @SerialName("contributionPoints") val contributionPoints: Int,
    val level: Int,
    val preferences: UserPreferences? = null
)

@Serializable
data class UserPreferences(
    val tags: List<String>? = null,
    val budget: String? = null,
    @SerialName("crowdPreference") val crowdPreference: String? = null
)

@Serializable
data class AuthResult(
    val user: User,
    val token: String,
    @SerialName("refreshToken") val refreshToken: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    @SerialName("deviceToken") val deviceToken: String? = null
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String
)

// Spot models
@Serializable
data class Spot(
    val id: String,
    val name: String,
    @SerialName("coverImage") val coverImage: String? = null,
    val rating: Float,
    val distance: Float? = null,
    @SerialName("crowdLevel") val crowdLevel: String,
    val tags: List<String>,
    val city: String? = null,
    val province: String? = null,
    @SerialName("aiReason") val aiReason: String? = null
)

@Serializable
data class SpotDetail(
    val id: String,
    val name: String,
    @SerialName("nameEn") val nameEn: String? = null,
    val description: String? = null,
    @SerialName("aiSummary") val aiSummary: String? = null,
    @SerialName("coverImage") val coverImage: String? = null,
    val rating: Float,
    @SerialName("reviewCount") val reviewCount: Int,
    @SerialName("crowdLevel") val crowdLevel: String,
    val tags: List<String>,
    val city: String? = null,
    val province: String? = null,
    val location: SpotLocation,
    val category: SpotCategory? = null,
    @SerialName("crowdData") val crowdData: CrowdData? = null,
    @SerialName("openingHours") val openingHours: Map<String, OpeningHours>? = null,
    @SerialName("ticketPrice") val ticketPrice: Float? = null,
    @SerialName("ticketInfo") val ticketInfo: String? = null,
    @SerialName("suggestedDuration") val suggestedDuration: Int? = null,
    @SerialName("bestSeasons") val bestSeasons: List<String>,
    @SerialName("bestTimeOfDay") val bestTimeOfDay: List<String>,
    val images: List<SpotImage>,
    @SerialName("isFavorited") val isFavorited: Boolean,
    @SerialName("nearbySpots") val nearbySpots: List<NearbySpot>
)

@Serializable
data class SpotLocation(
    val lat: Double,
    val lng: Double,
    val country: String,
    val province: String? = null,
    val city: String? = null,
    val district: String? = null,
    val address: String? = null
)

@Serializable
data class SpotCategory(
    val id: String,
    val name: String,
    val slug: String
)

@Serializable
data class CrowdData(
    val current: String,
    val forecast: Map<String, String>? = null
)

@Serializable
data class OpeningHours(
    val open: String,
    val close: String
)

@Serializable
data class SpotImage(
    val url: String,
    val caption: String? = null
)

@Serializable
data class NearbySpot(
    val id: String,
    val name: String,
    val rating: Float,
    val distance: Float
)

// Itinerary models
@Serializable
data class Itinerary(
    val id: String,
    @SerialName("userId") val userId: String,
    val title: String,
    val description: String? = null,
    @SerialName("coverImage") val coverImage: String? = null,
    @SerialName("startDate") val startDate: String,
    @SerialName("endDate") val endDate: String,
    @SerialName("daysCount") val daysCount: Int,
    val destination: String? = null,
    @SerialName("budgetLevel") val budgetLevel: String? = null,
    @SerialName("estimatedBudget") val estimatedBudget: Float? = null,
    @SerialName("travelStyle") val travelStyle: List<String>,
    @SerialName("isAiGenerated") val isAiGenerated: Boolean,
    val status: String,
    @SerialName("isPublic") val isPublic: Boolean,
    @SerialName("viewCount") val viewCount: Int,
    @SerialName("favoriteCount") val favoriteCount: Int,
    @SerialName("copyCount") val copyCount: Int,
    val items: List<ItineraryItem>
)

@Serializable
data class ItineraryItem(
    val id: String,
    @SerialName("dayNumber") val dayNumber: Int,
    @SerialName("orderInDay") val orderInDay: Int,
    val spot: SpotReference? = null,
    @SerialName("spotName") val spotName: String? = null,
    @SerialName("startTime") val startTime: String? = null,
    @SerialName("endTime") val endTime: String? = null,
    val duration: Int? = null,
    @SerialName("itemType") val itemType: String,
    @SerialName("customTitle") val customTitle: String? = null,
    @SerialName("customContent") val customContent: String? = null,
    @SerialName("estimatedCost") val estimatedCost: Float? = null,
    val notes: String? = null,
    val status: String
)

@Serializable
data class SpotReference(
    val id: String,
    val name: String,
    @SerialName("coverImage") val coverImage: String? = null,
    val rating: Float
)

// AI models
@Serializable
data class ItineraryGenerateRequest(
    val destination: String,
    @SerialName("destinationLocation") val destinationLocation: LocationPoint? = null,
    @SerialName("startDate") val startDate: String,
    @SerialName("endDate") val endDate: String,
    @SerialName("budgetLevel") val budgetLevel: String? = null,
    @SerialName("travelStyles") val travelStyles: List<String>? = null,
    @SerialName("crowdPreference") val crowdPreference: String? = null,
    val transportation: String? = null,
    @SerialName("specialRequests") val specialRequests: String? = null
)

@Serializable
data class LocationPoint(
    val lat: Double,
    val lng: Double
)

@Serializable
data class ChatRequest(
    val message: String,
    @SerialName("sessionId") val sessionId: String? = null,
    val location: LocationPoint? = null
)

@Serializable
data class ChatResult(
    val reply: String,
    @SerialName("recommendedSpots") val recommendedSpots: List<String>,
    @SerialName("sessionId") val sessionId: String
)

@Serializable
data class AIUsage(
    val recommendation: UsageInfo,
    val itinerary: UsageInfo,
    val chat: UsageInfo
)

@Serializable
data class UsageInfo(
    val used: Int,
    val limit: Int,
    val unlimited: Boolean
)
