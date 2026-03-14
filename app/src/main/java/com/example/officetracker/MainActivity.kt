package com.example.officetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.officetracker.data.prefs.UserPreferences
import com.example.officetracker.worker.AlarmScheduler
import com.example.officetracker.ui.analytics.AnalyticsScreen
import com.example.officetracker.ui.analytics.AnalyticsViewModel
import com.example.officetracker.ui.dashboard.DashboardScreen
import com.example.officetracker.ui.dashboard.DashboardViewModel
import com.example.officetracker.ui.onboarding.SetupScreen
import com.example.officetracker.ui.settings.SettingsScreen
import com.example.officetracker.ui.theme.OfficeTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    
    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmScheduler.scheduleAllReminders() // Schedule morning, midday, and evening smart reminders
        setContent {
            OfficeTrackerTheme {
                val navController = rememberNavController()
                val officeLocation = userPreferences.officeLocation.collectAsState(initial = null).value
                val userName = userPreferences.userName.collectAsState(initial = null).value
                val hasSeenTour = userPreferences.hasSeenTour.collectAsState(initial = null).value
                
                // Start tracking service automatically once setup is finished
                LaunchedEffect(officeLocation, hasSeenTour) {
                    if (officeLocation?.isSet == true && hasSeenTour == true) {
                        com.example.officetracker.service.LocationService.start(this@MainActivity)
                    }
                }
                
                // Determine start destination
                // We need to wait for all preferences to load
                if (officeLocation != null && userName != null && hasSeenTour != null) {
                    val startDest = when {
                        userName.isBlank() -> "welcome"
                        !hasSeenTour -> "tour"
                        !officeLocation.isSet -> "setup"
                        else -> "dashboard"
                    }

                    LaunchedEffect(userName) {
                        if (userName.isBlank()) {
                            navController.navigate("welcome") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    // Track current route to highlight selected nav item
                    val currentBackStack by navController.currentBackStackEntryFlow.collectAsState(initial = navController.currentBackStackEntry)
                    val currentRoute = currentBackStack?.destination?.route
                    val showBottomBar = currentRoute in listOf("dashboard", "analytics")

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                        label = { Text("Home") },
                                        selected = currentRoute == "dashboard",
                                        onClick = {
                                            if (currentRoute != "dashboard") {
                                                navController.navigate("dashboard") {
                                                    popUpTo("dashboard") { inclusive = true }
                                                }
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Analytics") },
                                        label = { Text("Analytics") },
                                        selected = currentRoute == "analytics",
                                        onClick = {
                                            if (currentRoute != "analytics") {
                                                navController.navigate("analytics") {
                                                    popUpTo("dashboard") { inclusive = false }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                    val routeOrder = listOf("welcome", "tour", "setup", "dashboard", "analytics", "settings")
                    fun getRouteIndex(route: String?): Int = routeOrder.indexOf(route?.split("?")?.get(0))

                    NavHost(
                        navController = navController,
                        startDestination = startDest,
                        modifier = Modifier.padding(innerPadding),
                        enterTransition = {
                            val initial = getRouteIndex(initialState.destination.route)
                            val target = getRouteIndex(targetState.destination.route)
                            if (target > initial) {
                                slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
                            } else {
                                slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
                            }
                        },
                        exitTransition = {
                            val initial = getRouteIndex(initialState.destination.route)
                            val target = getRouteIndex(targetState.destination.route)
                            if (target > initial) {
                                slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
                            } else {
                                slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
                            }
                        },
                        popEnterTransition = {
                            val initial = getRouteIndex(initialState.destination.route)
                            val target = getRouteIndex(targetState.destination.route)
                            if (target > initial) {
                                slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
                            } else {
                                slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
                            }
                        },
                        popExitTransition = {
                            val initial = getRouteIndex(initialState.destination.route)
                            val target = getRouteIndex(targetState.destination.route)
                            if (target > initial) {
                                slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500))
                            } else {
                                slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500))
                            }
                        }
                    ) {
                        composable("welcome") {
                            com.example.officetracker.ui.onboarding.WelcomeScreen(onNext = {
                                navController.navigate("tour") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            })
                        }
                        composable("tour") {
                            com.example.officetracker.ui.onboarding.TourScreen(onTourComplete = {
                                navController.navigate("setup") {
                                    popUpTo("tour") { inclusive = true }
                                }
                            })
                        }
                        composable("setup") {
                            SetupScreen(onSetupComplete = {
                                navController.navigate("dashboard") {
                                    popUpTo("setup") { inclusive = true }
                                }
                            })
                        }
                        composable("dashboard") {
                            val viewModel = hiltViewModel<DashboardViewModel>()
                            DashboardScreen(viewModel, onNavigateToProfile = {
                                navController.navigate("settings")
                            })
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateToSetup = { navController.navigate("setup") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("analytics") {
                            AnalyticsScreen()
                        }
                    }
                    }
                }
            }
        }
    }
}
