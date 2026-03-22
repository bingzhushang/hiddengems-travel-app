package com.hiddengems.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hiddengems.data.model.Spot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val spots: List<Spot> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val error: String? = null
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    // private val spotRepository: SpotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadSpots()
    }

    fun loadSpots() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // TODO: Call repository to load spots
            // For now, simulate data
            kotlinx.coroutines.delay(1000)

            val mockSpots = listOf(
                Spot(
                    id = "spot-1",
                    name = "九溪十八涧",
                    nameEn = "Nine Creeks",
                    description = "一条蜿蜒的山间溪流，沿途风景秀丽",
                    coverImage = "https://picsum.photos/seed/1/400/300",
                    rating = 4.8,
                    reviewCount = 234,
                    latitude = 30.2096,
                    longitude = 120.1215,
                    address = "杭州市西湖区九溪路",
                    tags = listOf("自然", "徒步"),
                    crowdLevel = "low"
                ),
                Spot(
                    id = "spot-2",
                    name = "满觉陇村",
                    nameEn = "Manjuelong Village",
                    description = "桂花飘香的小众村落",
                    coverImage = "https://picsum.photos/seed/2/400/300",
                    rating = 4.7,
                    reviewCount = 189,
                    latitude = 30.2134,
                    longitude = 120.1287,
                    address = "杭州市西湖区满觉陇村",
                    tags = listOf("乡村", "摄影"),
                    crowdLevel = "low"
                ),
                Spot(
                    id = "spot-3",
                    name = "云栖竹径",
                    nameEn = "Yunqi Bamboo Path",
                    description = "幽静的竹林小径",
                    coverImage = "https://picsum.photos/seed/3/400/300",
                    rating = 4.6,
                    reviewCount = 156,
                    latitude = 30.2156,
                    longitude = 120.1198,
                    address = "杭州市西湖区云栖竹径",
                    tags = listOf("自然", "徒步"),
                    crowdLevel = "medium"
                ),
                Spot(
                    id = "spot-4",
                    name = "龙井村",
                    nameEn = "Longjing Village",
                    description = "茶叶产地，田园风光",
                    coverImage = "https://picsum.photos/seed/4/400/300",
                    rating = 4.5,
                    reviewCount = 123,
                    latitude = 30.2234,
                    longitude = 120.1012,
                    address = "杭州市西湖区龙井村",
                    tags = listOf("乡村", "茶文化"),
                    crowdLevel = "low"
                )
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                spots = mockSpots
            )
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        // Filter spots based on search query
        // For now, just trigger a reload
        if (query.isNotBlank()) {
            loadSpots()
        }
    }

    fun filterByCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        // Reload with filter
        loadSpots()
    }
}
