package com.hiddengems.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiddengems.data.model.PaginatedResponse
import com.hiddengems.data.model.Spot
import com.hiddengems.data.repository.SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val spots: List<Spot>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val spotRepository: SpotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _nearbySpots = MutableStateFlow<List<Spot>>(emptyList())
    val nearbySpots: StateFlow<List<Spot>> = _nearbySpots.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Spot>>(emptyList())
    val searchResults: StateFlow<List<Spot>> = _searchResults.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // Initial state is Loading until data is loaded
    }

    fun loadRecommendations(lat: Double, lng: Double, limit: Int = 10) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            val result = spotRepository.getRecommendations(lat, lng, limit)
            result.fold(
                onSuccess = { spots ->
                    _uiState.value = HomeUiState.Success(spots)
                },
                onFailure = { exception ->
                    _uiState.value = HomeUiState.Error(exception.message ?: "Failed to load recommendations")
                }
            )
        }
    }

    fun loadNearbySpots(lat: Double, lng: Double, radius: Double = 50.0, page: Int = 1, pageSize: Int = 20) {
        viewModelScope.launch {
            val result = spotRepository.getNearbySpots(lat, lng, radius, page, pageSize)
            result.fold(
                onSuccess = { response ->
                    _nearbySpots.value = response.items
                },
                onFailure = {
                    // Keep existing data on failure
                }
            )
        }
    }

    fun searchSpots(query: String, page: Int = 1, pageSize: Int = 20) {
        viewModelScope.launch {
            val result = spotRepository.searchSpots(query, page, pageSize)
            result.fold(
                onSuccess = { response ->
                    _searchResults.value = response.items
                },
                onFailure = {
                    _searchResults.value = emptyList()
                }
            )
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    fun refresh(lat: Double, lng: Double) {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadRecommendations(lat, lng)
            _isRefreshing.value = false
        }
    }
}
