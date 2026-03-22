package com.hiddengems.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hiddengems.ui.home.HomeScreen
import com.hiddengems.ui.discover.DiscoverScreen
import com.hiddengems.ui.itinerary.ItineraryScreen
import com.hiddengems.ui.itinerary.ItineraryDetailScreen
import com.hiddengems.ui.community.CommunityScreen
import com.hiddengems.ui.profile.ProfileScreen
import com.hiddengems.ui.spot.SpotDetailScreen
import com.hiddengems.ui.auth.LoginScreen
import com.hiddengems.ui.auth.RegisterScreen
import com.hiddengems.ui.ai.AIPlannerScreen

sealed class Screen(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null
) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object Discover : Screen("discover", "发现", Icons.Default.Explore)
    object Itinerary : Screen("itinerary", "行程", Icons.Default.Map)
    object Community : Screen("community", "社区", Icons.Default.Forum)
    object Profile : Screen("profile", "我的", Icons.Default.Person)
    object SpotDetail : Screen("spot/{spotId}") {
        fun createRoute(spotId: String) = "spot/$spotId"
    }
    object Login : Screen("login")
    object Register : Screen("register")
    object AIPlanner : Screen("ai_planner")
    object ItineraryDetail : Screen("itinerary/{itineraryId}") {
        fun createRoute(itineraryId: String) = "itinerary/$itineraryId"
    }
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Discover,
    Screen.Itinerary,
    Screen.Community,
    Screen.Profile
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenGemsNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title!!) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onSpotClick = { spotId ->
                        navController.navigate(Screen.SpotDetail.createRoute(spotId))
                    },
                    onAITripClick = {
                        navController.navigate(Screen.AIPlanner.route)
                    }
                )
            }

            composable(Screen.Discover.route) {
                DiscoverScreen(
                    onSpotClick = { spotId ->
                        navController.navigate(Screen.SpotDetail.createRoute(spotId))
                    }
                )
            }

            composable(Screen.Itinerary.route) {
                ItineraryScreen(
                    onItineraryClick = { itineraryId ->
                        navController.navigate(Screen.ItineraryDetail.createRoute(itineraryId))
                    },
                    onAIGenerateClick = {
                        navController.navigate(Screen.AIPlanner.route)
                    }
                )
            }

            composable(Screen.Community.route) {
                CommunityScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLoginClick = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }

            composable(
                route = Screen.SpotDetail.route,
                arguments = listOf(navArgument("spotId") { type = NavType.StringType })
            ) { backStackEntry ->
                val spotId = backStackEntry.arguments?.getString("spotId") ?: return@composable
                SpotDetailScreen(
                    spotId = spotId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateClick = { lat, lng ->
                        // TODO: Open maps navigation
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLoginSuccess = {
                        navController.popBackStack()
                    },
                    onRegisterClick = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.popBackStack(Screen.Profile.route, inclusive = false)
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AIPlanner.route) {
                AIPlannerScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onItineraryGenerated = {
                        navController.popBackStack()
                        navController.navigate(Screen.Itinerary.route)
                    }
                )
            }

            composable(
                route = Screen.ItineraryDetail.route,
                arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: return@composable
                ItineraryDetailScreen(
                    itineraryId = itineraryId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPlannerScreen(
    onBackClick: () -> Unit,
    onItineraryGenerated: () -> Unit
) {
    // Placeholder AI Planner Screen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 行程规划") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "AI 行程规划功能",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "即将推出",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryDetailScreen(
    itineraryId: String,
    onBackClick: () -> Unit
) {
    // Placeholder Itinerary Detail Screen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("行程详情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "行程ID: $itineraryId",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
