package com.hiddengems.ui.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.hiddengems.data.model.Spot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onSpotClick: (String) -> Unit,
    viewModel: DiscoverViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.search(it)
                            },
                            placeholder = { Text("搜索景点...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.transparent,
                                unfocusedBorderColor = MaterialTheme.colorScheme.transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { showSearchBar = false }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("发现") },
                    actions = {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                        IconButton(onClick = { /* filter */ }) {
                            Icon(Icons.Default.FilterList, contentDescription = "筛选")
                        }
                    }
                )
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "加载失败",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Category chips
                    CategoryChips(
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = { viewModel.filterByCategory(it) }
                    )

                    // Spots list
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.spots) { spot ->
                            SpotCard(
                                spot = spot,
                                onClick = { onSpotClick(spot.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChips(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val categories = listOf(
        "全部" to null,
        "自然风光" to "nature",
        "人文古迹" to "culture",
        "小众秘境" to "hidden",
        "乡村田园" to "village"
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (label, value) ->
            FilterChip(
                selected = selectedCategory == value,
                onClick = { onCategorySelected(value) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun SpotCard(
    spot: Spot,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Cover image
            AsyncImage(
                model = spot.coverImage,
                contentDescription = spot.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = spot.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Crowd level badge
                    val (crowdText, crowdColor) = when (spot.crowdLevel) {
                        "low" -> "人流少" to MaterialTheme.colorScheme.primaryContainer
                        "medium" -> "适中" to MaterialTheme.colorScheme.secondaryContainer
                        "high" -> "拥挤" to MaterialTheme.colorScheme.errorContainer
                        else -> "未知" to MaterialTheme.colorScheme.surfaceVariant
                    }
                    SuggestionChip(
                        onClick = {},
                        label = { Text(crowdText, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                spot.nameEn?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " ${spot.rating}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = " · ${spot.reviewCount}条评价",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tags
                if (spot.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        spot.tags.take(3).forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }
    }
}
