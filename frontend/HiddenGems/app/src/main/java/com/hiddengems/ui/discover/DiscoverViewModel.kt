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
            kotlinx.coroutines.delay(800)

            val mockSpots = listOf(
                Spot(
                    id = "spot-1",
                    name = "九溪十八涧",
                    nameEn = "Nine Creeks",
                    description = "一条蜿蜒的山间溪流，沿途风景秀丽，适合徒步和摄影",
                    coverImage = "https://picsum.photos/seed/spot1/400/300",
                    rating = 4.8f,
                    reviewCount = 234,
                    crowdLevel = "low",
                    tags = listOf("自然", "徒步"),
                    city = "杭州市",
                    province = "浙江省",
                    address = "杭州市西湖区九溪路",
                    latitude = 30.2096,
                    longitude = 120.1215
                ),
                Spot(
                    id = "spot-2",
                    name = "满觉陇村",
                    nameEn = "Manjuelong Village",
                    description = "桂花飘香的小众村落，秋天最美",
                    coverImage = "https://picsum.photos/seed/spot2/400/300",
                    rating = 4.7f,
                    reviewCount = 189,
                    crowdLevel = "low",
                    tags = listOf("乡村", "摄影"),
                    city = "杭州市",
                    province = "浙江省",
                    address = "杭州市西湖区满觉陇村",
                    latitude = 30.2134,
                    longitude = 120.1287
                ),
                Spot(
                    id = "spot-3",
                    name = "云栖竹径",
                    nameEn = "Yunqi Bamboo Path",
                    description = "幽静的竹林小径，夏日清凉避暑胜地",
                    coverImage = "https://picsum.photos/seed/spot3/400/300",
                    rating = 4.6f,
                    reviewCount = 156,
                    crowdLevel = "medium",
                    tags = listOf("自然", "徒步"),
                    city = "杭州市",
                    province = "浙江省",
                    address = "杭州市西湖区云栖竹径",
                    latitude = 30.2156,
                    longitude = 120.1198
                ),
                Spot(
                    id = "spot-4",
                    name = "龙井村",
                    nameEn = "Longjing Village",
                    description = "茶叶产地，田园风光，体验茶文化",
                    coverImage = "https://picsum.photos/seed/spot4/400/300",
                    rating = 4.5f,
                    reviewCount = 123,
                    crowdLevel = "low",
                    tags = listOf("乡村", "茶文化"),
                    city = "杭州市",
                    province = "浙江省",
                    address = "杭州市西湖区龙井村",
                    latitude = 30.2234,
                    longitude = 120.1012
                ),
                Spot(
                    id = "spot-5",
                    name = "法喜寺",
                    nameEn = "Faxi Temple",
                    description = "天竺三寺之一，古刹清幽，适合静心",
                    coverImage = "https://picsum.photos/seed/spot5/400/300",
                    rating = 4.4f,
                    reviewCount = 98,
                    crowdLevel = "medium",
                    tags = listOf("人文", "寺庙"),
                    city = "杭州市",
                    province = "浙江省",
                    address = "杭州市西湖区天竺路",
                    latitude = 30.2189,
                    longitude = 120.1156
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
        if (query.isNotBlank()) {
            // Filter spots based on search query
            val filtered = _uiState.value.spots.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.nameEn?.contains(query, ignoreCase = true) == true ||
                it.description?.contains(query, ignoreCase = true) == true
            }
            _uiState.value = _uiState.value.copy(spots = filtered)
        } else {
            loadSpots()
        }
    }

    fun filterByCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        // In real app, would reload from API with filter
        loadSpots()
    }
}
