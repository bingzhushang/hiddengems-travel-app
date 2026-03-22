package com.hiddengems.ui.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiddengems.data.model.Itinerary
import com.hiddengems.data.model.ItineraryGenerateRequest
import com.hiddengems.data.repository.ItineraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ItineraryUiState {
    object Loading : ItineraryUiState()
    data class Success(val itineraries: List<Itinerary>) : ItineraryUiState()
    data class Error(val message: String) : ItineraryUiState()
}

@HiltViewModel
class ItineraryViewModel @Inject constructor(
    private val itineraryRepository: ItineraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ItineraryUiState>(ItineraryUiState.Loading)
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    private val _generatedItinerary = MutableStateFlow<Itinerary?>(null)
    val generatedItinerary: StateFlow<Itinerary?> = _generatedItinerary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentFilter: String? = null

    fun loadItineraries(page: Int = 1, pageSize: Int = 10) {
        viewModelScope.launch {
            _uiState.value = ItineraryUiState.Loading

            val result = itineraryRepository.getMyItineraries(currentFilter, page, pageSize)
            result.fold(
                onSuccess = { response ->
                    _uiState.value = ItineraryUiState.Success(response.items)
                },
                onFailure = { exception ->
                    _uiState.value = ItineraryUiState.Error(exception.message ?: "Failed to load itineraries")
                }
            )
        }
    }

    fun loadItinerary(itineraryId: String) {
        viewModelScope.launch {
            _uiState.value = ItineraryUiState.Loading

            val result = itineraryRepository.getItinerary(itineraryId)
            result.fold(
                onSuccess = { itinerary ->
                    _uiState.value = ItineraryUiState.Success(listOf(itinerary))
                },
                onFailure = { exception ->
                    _uiState.value = ItineraryUiState.Error(exception.message ?: "Failed to load itinerary")
                }
            )
        }
    }

    fun createItinerary(title: String, startDate: String, endDate: String, destination: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = itineraryRepository.createItinerary(title, startDate, endDate, destination)
            _isLoading.value = false

            result.fold(
                onSuccess = {
                    // Reload itineraries after creating
                    loadItineraries()
                },
                onFailure = {
                    // Handle error
                }
            )
        }
    }

    fun deleteItinerary(itineraryId: String) {
        viewModelScope.launch {
            val result = itineraryRepository.deleteItinerary(itineraryId)
            result.fold(
                onSuccess = {
                    // Remove from current list
                    val currentState = _uiState.value
                    if (currentState is ItineraryUiState.Success) {
                        _uiState.value = ItineraryUiState.Success(
                            currentState.itineraries.filter { it.id != itineraryId }
                        )
                    }
                },
                onFailure = {
                    // Handle error
                }
            )
        }
    }

    fun generateItineraryWithAI(request: ItineraryGenerateRequest) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = itineraryRepository.generateItinerary(request)
            _isLoading.value = false

            result.fold(
                onSuccess = { itinerary ->
                    _generatedItinerary.value = itinerary
                },
                onFailure = {
                    _generatedItinerary.value = null
                }
            )
        }
    }

    fun filterByStatus(status: String?) {
        currentFilter = status
        loadItineraries()
    }

    fun refresh() {
        loadItineraries()
    }

    fun clearGeneratedItinerary() {
        _generatedItinerary.value = null
    }
}
