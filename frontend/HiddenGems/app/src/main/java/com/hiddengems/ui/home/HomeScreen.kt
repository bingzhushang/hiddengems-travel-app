package com.hiddengems.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.hiddengems.data.model.Spot
import com.hiddengems.ui.theme.getFullCrowdLevelText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSpotClick: (String) -> Unit,
    onAITripClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nearbySpots by viewModel.nearbySpots.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // Load data on first composition
    LaunchedEffect(Unit) {
        // Default to Beijing coordinates for demo
        val lat = 39.9042
        val lng = 116.4074
        userLocation = Pair(lat, lng)
        viewModel.loadRecommendations(lat, lng)
        viewModel.loadNearbySpots(lat, lng)
    }

    // Handle search
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            viewModel.searchSpots(searchQuery)
        } else {
            viewModel.clearSearch()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("秘境探索")
                    }
                },
                actions = {
                    IconButton(onClick = { /* notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "通知")
                    }
                    IconButton(onClick = { /* profile */ }) {
                        Icon(Icons.Default.Person, contentDescription = "我的")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            userLocation?.let { viewModel.loadRecommendations(it.first, it.second) }
                        }) {
                            Text("重试")
                        }
                    }
                }
            }
            is HomeUiState.Success -> {
                HomeContent(
                    padding = padding,
                    spots = state.spots,
                    nearbySpots = nearbySpots,
                    searchResults = searchResults,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        userLocation?.let { viewModel.refresh(it.first, it.second) }
                    },
                    onSpotClick = onSpotClick,
                    onAITripClick = onAITripClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    padding: PaddingValues,
    spots: List<Spot>,
    nearbySpots: List<Spot>,
    searchResults: List<Spot>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSpotClick: (String) -> Unit,
    onAITripClick: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.padding(padding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Show search results if searching
            if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "搜索结果",
                        actionText = null,
                        onActionClick = {}
                    )
                }

                items(searchResults) { spot ->
                    NearbySpotItem(
                        name = spot.name,
                        rating = spot.rating,
                        distance = spot.distance ?: 0f,
                        tags = spot.tags,
                        crowdLevel = getFullCrowdLevelText(spot.crowdLevel),
                        imageUrl = spot.coverImage,
                        onClick = { onSpotClick(spot.id) },
                        isFavorited = false,
                        onFavoriteClick = { /* TODO */ }
                    )
                }
            } else if (searchQuery.isBlank()) {
                // AI Recommendations
                item {
                    AIRecommendationsSection(
                        spots = spots,
                        onSpotClick = onSpotClick
                    )
                }

                // Nearby Spots
                item {
                    SectionHeader(
                        title = "附近小众景点",
                        actionText = if (nearbySpots.isNotEmpty()) "查看全部" else null,
                        onActionClick = { /* navigate */ }
                    )
                }

                if (nearbySpots.isNotEmpty()) {
                    items(nearbySpots) { spot ->
                        NearbySpotItem(
                            name = spot.name,
                            rating = spot.rating,
                            distance = spot.distance ?: 0f,
                            tags = spot.tags,
                            crowdLevel = getFullCrowdLevelText(spot.crowdLevel),
                            imageUrl = spot.coverImage,
                            onClick = { onSpotClick(spot.id) },
                            isFavorited = false,
                            onFavoriteClick = { /* TODO */ },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "暂无附近景点数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Popular Themes
                item {
                    SectionHeader(
                        title = "热门主题",
                        actionText = null,
                        onActionClick = {}
                    )
                }

                item {
                    ThemeChips(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // AI Trip Planner CTA
                item {
                    AIPlannerCard(
                        onClick = onAITripClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("搜索目的地、景点...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "清除")
                }
            } else {
                IconButton(onClick = { /* voice search */ }) {
                    Icon(Icons.Default.Mic, contentDescription = "语音搜索")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.transparent
        )
    )
}

@Composable
fun AIRecommendationsSection(
    spots: List<Spot>,
    onSpotClick: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AI 为你推荐",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(onClick = { /* refresh */ }) {
                Text("换一批")
            }
        }

        if (spots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无推荐，下拉刷新",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(spots) { spot ->
                    SpotCard(
                        spot = spot,
                        onClick = { onSpotClick(spot.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SpotCard(
    spot: Spot,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(180.dp)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = spot.coverImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Crowd badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = getFullCrowdLevelText(spot.crowdLevel),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = spot.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    val distanceText = spot.distance?.let { "${String.format("%.1f", it)}km" } ?: ""
                    Text(
                        text = " ${spot.rating} · $distanceText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tags
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    spot.tags.take(2).forEach { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String?,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null) {
            TextButton(onClick = onActionClick) {
                Text(actionText)
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun NearbySpotItem(
    name: String,
    rating: Float,
    distance: Float,
    tags: List<String>,
    crowdLevel: String,
    imageUrl: String?,
    onClick: () -> Unit,
    isFavorited: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Image
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(88.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Crop
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " $rating · ${String.format("%.1f", distance)}km · 人流$crowdLevel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tags
                Text(
                    text = tags.joinToString(" ") { "#$it" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Action buttons
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorited) "取消收藏" else "收藏",
                    tint = if (isFavorited) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ThemeChips(
    modifier: Modifier = Modifier
) {
    val themes = listOf(
        "冷门古镇" to Icons.Default.LocationCity,
        "秘境海滩" to Icons.Default.BeachAccess,
        "隐藏美食" to Icons.Default.Restaurant,
        "小众博物馆" to Icons.Default.Museum,
        "古道徒步" to Icons.Default.Hiking,
        "废弃工业" to Icons.Default.Factory
    )

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(themes) { (name, icon) ->
            FilterChip(
                selected = false,
                onClick = { /* filter */ },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(name)
                    }
                }
            )
        }
    }
}

@Composable
fun AIPlannerCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI 行程规划",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "输入需求，一键生成小众路线",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}
