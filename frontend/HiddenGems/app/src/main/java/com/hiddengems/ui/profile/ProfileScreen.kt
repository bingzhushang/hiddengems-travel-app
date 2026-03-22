package com.hiddengems.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.hiddengems.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit,
    onFavoritesClick: () -> Unit = {},
    onItinerariesClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User profile card
            item {
                if (uiState.isLoggedIn && uiState.user != null) {
                    UserProfileCard(
                        user = uiState.user!!,
                        onLogout = { showLogoutDialog = true }
                    )
                } else {
                    LoginPromptCard(onLoginClick = onLoginClick)
                }
            }

            // Stats cards (only show when logged in)
            if (uiState.isLoggedIn && uiState.user?.stats != null) {
                item {
                    UserStatsRow(
                        stats = uiState.user!!.stats!!,
                        onFavoritesClick = onFavoritesClick,
                        onItinerariesClick = onItinerariesClick
                    )
                }
            }

            // Menu items
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        if (uiState.isLoggedIn) {
                            ProfileMenuItem(
                                icon = Icons.Default.Star,
                                title = "会员中心",
                                subtitle = uiState.user?.membershipType?.let {
                                    when (it) {
                                        "pro" -> "专业版"
                                        "explorer" -> "探索版"
                                        else -> "免费版"
                                    }
                                } ?: "免费版",
                                onClick = {}
                            )
                            HorizontalDivider()
                        }

                        ProfileMenuItem(
                            icon = Icons.Default.Favorite,
                            title = "我的收藏",
                            subtitle = if (uiState.isLoggedIn) {
                                "已收藏 ${uiState.user?.stats?.favoriteCount ?: 0} 个景点"
                            } else null,
                            onClick = if (uiState.isLoggedIn) onFavoritesClick else onLoginClick
                        )

                        HorizontalDivider()

                        ProfileMenuItem(
                            icon = Icons.Default.Map,
                            title = "我的行程",
                            subtitle = if (uiState.isLoggedIn) {
                                "${uiState.user?.stats?.itineraryCount ?: 0} 个行程"
                            } else null,
                            onClick = if (uiState.isLoggedIn) onItinerariesClick else onLoginClick
                        )

                        HorizontalDivider()

                        ProfileMenuItem(
                            icon = Icons.Default.Place,
                            title = "足迹地图",
                            subtitle = if (uiState.isLoggedIn) {
                                "去过 ${uiState.user?.stats?.visitedCount ?: 0} 个地方"
                            } else null,
                            onClick = {}
                        )
                    }
                }
            }

            // Settings menu
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        ProfileMenuItem(
                            icon = Icons.Default.Settings,
                            title = "设置",
                            onClick = onSettingsClick
                        )

                        HorizontalDivider()

                        ProfileMenuItem(
                            icon = Icons.Default.Help,
                            title = "帮助与反馈",
                            onClick = {}
                        )

                        HorizontalDivider()

                        ProfileMenuItem(
                            icon = Icons.Default.Info,
                            title = "关于我们",
                            onClick = {}
                        )
                    }
                }
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出当前账号吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun UserProfileCard(
    user: User,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.avatar ?: "https://picsum.photos/seed/${user.id}/200/200",
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.nickname,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                user.bio?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Membership badge
                val (badgeText, badgeColor) = when (user.membershipType) {
                    "pro" -> "专业版会员" to MaterialTheme.colorScheme.primaryContainer
                    "explorer" -> "探索版会员" to MaterialTheme.colorScheme.secondaryContainer
                    else -> "免费会员" to MaterialTheme.colorScheme.surfaceVariant
                }
                SuggestionChip(
                    onClick = {},
                    label = { Text(badgeText, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "退出登录"
                )
            }
        }
    }
}

@Composable
fun LoginPromptCard(
    onLoginClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLoginClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "登录 / 注册",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "登录后享受更多功能",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UserStatsRow(
    stats: com.hiddengems.data.model.UserStats,
    onFavoritesClick: () -> Unit,
    onItinerariesClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Favorite,
            value = stats.favoriteCount.toString(),
            label = "收藏",
            onClick = onFavoritesClick
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Map,
            value = stats.itineraryCount.toString(),
            label = "行程",
            onClick = onItinerariesClick
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Place,
            value = stats.visitedCount.toString(),
            label = "足迹",
            onClick = {}
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Star,
            value = stats.reviewCount.toString(),
            label = "评价",
            onClick = {}
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
