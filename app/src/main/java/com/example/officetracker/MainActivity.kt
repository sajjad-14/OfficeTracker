package com.example.officetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmScheduler.scheduleMorningReminder() // Ensure alarm is set
        setContent {
            OfficeTrackerTheme {
                val navController = rememberNavController()
                val officeLocation = userPreferences.officeLocation.collectAsState(initial = null).value
                val userName = userPreferences.userName.collectAsState(initial = null).value
                val hasSeenTour = userPreferences.hasSeenTour.collectAsState(initial = null).value
                
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

                    NavHost(navController = navController, startDestination = startDest) {
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
                            Column {
                                Box(modifier = Modifier.weight(1f)) {
                                     DashboardScreen(viewModel, onNavigateToProfile = {
                                         navController.navigate("settings")
                                     })
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Button(
                                        onClick = { navController.navigate("analytics") },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Analytics")
                                    }
                                }
                            }
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
