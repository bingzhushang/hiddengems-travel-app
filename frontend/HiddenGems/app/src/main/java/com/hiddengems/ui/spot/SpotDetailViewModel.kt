package com.hiddengems.ui.spot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiddengems.data.model.SpotDetail
import com.hiddengems.data.repository.SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SpotDetailUiState {
    object Loading : SpotDetailUiState()
    data class Success(val spotDetail: SpotDetail) : SpotDetailUiState()
    data class Error(val message: String) : SpotDetailUiState()
}

@HiltViewModel
class SpotDetailViewModel @Inject constructor(
    private val spotRepository: SpotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SpotDetailUiState>(SpotDetailUiState.Loading)
    val uiState: StateFlow<SpotDetailUiState> = _uiState.asStateFlow()

    private val _isFavorited = MutableStateFlow(false)
    val isFavorited: StateFlow<Boolean> = _isFavorited.asStateFlow()

    private var currentSpotId: String? = null

    fun loadSpotDetail(spotId: String) {
        currentSpotId = spotId
        viewModelScope.launch {
            _uiState.value = SpotDetailUiState.Loading

            val result = spotRepository.getSpotDetail(spotId)
            result.fold(
                onSuccess = { spotDetail ->
                    _uiState.value = SpotDetailUiState.Success(spotDetail)
                    _isFavorited.value = spotDetail.isFavorited
                },
                onFailure = { exception ->
                    _uiState.value = SpotDetailUiState.Error(exception.message ?: "Failed to load spot details")
                }
            )
        }
    }

    fun toggleFavorite() {
        val spotId = currentSpotId ?: return

        viewModelScope.launch {
            val result = spotRepository.toggleFavorite(spotId)
            result.fold(
                onSuccess = { (isFavorited, _) ->
                    _isFavorited.value = isFavorited
                    // Update the state with new favorite status
                    val currentState = _uiState.value
                    if (currentState is SpotDetailUiState.Success) {
                        _uiState.value = SpotDetailUiState.Success(
                            currentState.spotDetail.copy(isFavorited = isFavorited)
                        )
                    }
                },
                onFailure = {
                    // Keep current state on failure
                }
            )
        }
    }
}
