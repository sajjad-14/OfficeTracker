package com.example.officetracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.officetracker.data.prefs.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.officetracker.data.repository.AttendanceRepository
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val repository: AttendanceRepository
) : ViewModel() {

    val userGoals: StateFlow<UserPreferences.UserGoals> = userPreferences.userGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.UserGoals())

    fun updateGoals(daily: Int, monthly: Int) {
        viewModelScope.launch {
            userPreferences.setGoals(daily, monthly)
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            repository.exportDataToCsv(uri)
        }
    }

    fun clearData(onCleared: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            onCleared()
        }
    }
}

@Composable
fun SettingsScreen(
    onNavigateToSetup: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val userGoals by viewModel.userGoals.collectAsState()

    var dailyGoal by remember(userGoals.dailyGoalHours) { mutableStateOf(userGoals.dailyGoalHours.toFloat()) }
    var monthlyGoal by remember(userGoals.monthlyGoalHours) { mutableStateOf(userGoals.monthlyGoalHours.toFloat()) }

    // Update VM when slider settles? Or Button? 
    // Reactive update is fine for local prefs but let's add a Save button or just auto-save on drag end?
    // Auto-save on drag end is hard with basic slider. Let's start with a Save button or "Done".
    // Actually, let's just update on change finished.
    // For simplicity, let's update immediately but debounce? No need, prefs are fast.
    
    // Better UX: Update state locally and save on "Apply" or "Back".
    // Even better: Instant update.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Goals Configuration", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        // Daily Goal Slider
        Text("Daily Minimum (Streak): ${dailyGoal.toInt()} hrs", color = MaterialTheme.colorScheme.onSurface)
        Slider(
            value = dailyGoal,
            onValueChange = { dailyGoal = it },
            onValueChangeFinished = { viewModel.updateGoals(dailyGoal.toInt(), monthlyGoal.toInt()) },
            valueRange = 4f..12f,
            steps = 7 // 4, 5, 6, 7, 8, 9, 10, 11, 12
        )
        Text("Minimum hours required to extend your daily streak.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(24.dp))

        // Monthly Goal Slider
        Text("Monthly Target: ${monthlyGoal.toInt()} hrs", color = MaterialTheme.colorScheme.onSurface)
        Slider(
            value = monthlyGoal,
            onValueChange = { monthlyGoal = it },
            onValueChangeFinished = { viewModel.updateGoals(dailyGoal.toInt(), monthlyGoal.toInt()) },
            valueRange = 20f..120f,
            steps = 19 // increments of 5? No, integer steps. 100 steps?
        )
         Text("Total hours to reach 100% monthly progress.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(32.dp))
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))

        Text("Office Location", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateToSetup,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Office Location")
        }
        Text("This will reopen the setup map.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(32.dp))
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))

        Text("Data Management", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        val exportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv"),
            onResult = { uri ->
                uri?.let { viewModel.exportData(it) }
            }
        )

        Button(
            onClick = { exportLauncher.launch("office_tracker_data.csv") },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export Data (CSV)")
        }
        Text("Save your work history to a file.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(32.dp))
        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Danger Zone", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        
        var showDeleteDialog by remember { mutableStateOf(false) }
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Reset Application") },
                text = { Text("Are you sure you want to delete ALL data? This includes your profile, sessions, and statistics. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.clearData { 
                                // Navigate to setup/onboarding
                                onNavigateToSetup()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset Everything")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
             Text("Reset App Data", color = MaterialTheme.colorScheme.error)
        }
        Text("Be careful, this will delete all your data.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
