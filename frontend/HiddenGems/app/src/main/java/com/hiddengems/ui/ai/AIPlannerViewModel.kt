package com.hiddengems.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiddengems.data.model.Itinerary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIPlannerUiState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val generatedItinerary: Itinerary? = null,
    val error: String? = null
)

@HiltViewModel
class AIPlannerViewModel @Inject constructor(
    // private val aiService: AIService // Will be created later
) : ViewModel() {

    private val _uiState = MutableStateFlow<AIPlannerUiState>(AIPlannerUiState())
    val uiState: StateFlow<AIPlannerUiState> = _uiState.asStateFlow()

    fun generateItinerary(
        destination: String,
        startDate: String,
        endDate: String,
        budgetLevel: String,
        crowdPreference: String,
        travelStyles: List<String>,
        transportation: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true)

            // Simulate API call
            delay(30000 // Simulate successful generation
            val mockItinerary = Itinerary(
                id = "generated-${System.currentTimeMillis()}",
                userId = "current-user",
                title = "${destination}探索之旅",
                description = "AI 为您生成的专属行程，包含您旅行偏好的精华景点",
                coverImage = "https://picsum.photos/seed/${destination.hashCode()}/800/400",
                startDate = parseDate(startDate),
                endDate = parseDate(endDate),
                daysCount = calculateDays(startDate, endDate),
                destination = destination,
                isAiGenerated = true,
                status = "DRAFT",
                isPublic = false,
                viewCount = 0,
                favoriteCount = 0,
                copyCount = 0,
                travelStyle = travelStyles,
                items = emptyList()
            )

            _uiState.value = AIPlannerUiState(
                isGenerating = false,
                generatedItinerary = mockItinerary
            )
        }
    }

    private fun parseDate(dateString: String): java.util.Date {
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            format.parse(dateString)
        } catch (e: Exception) {
            java.util.Date()
        }
    }

    private fun calculateDays(startDate: String, endDate: String): Int {
        return try {
            val start = parseDate(startDate)
            val end = parseDate(endDate)
            val diff = (end.time - start.time)
            val days = diff / (1000 * 60 * 60 * 24)
            days + 1
        } catch (e: Exception) {
            1
        }
    }
}
