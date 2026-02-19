package com.example.officetracker.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import java.util.Calendar
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.officetracker.data.local.entity.AttendanceSession
import com.example.officetracker.data.local.entity.DailyStat
import com.example.officetracker.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.officetracker.data.prefs.UserPreferences

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val activeSession: StateFlow<AttendanceSession?> = repository.getCurrentActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayStats: StateFlow<DailyStat?> = repository.getTodayStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val monthlySeconds: StateFlow<Long> = repository.getMonthlyCompletedSeconds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val streak: StateFlow<Int> = repository.getStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val disciplineScore: StateFlow<Float> = repository.getDisciplineScore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val userGoals = userPreferences.userGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.UserGoals())

    val userName = userPreferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Ticker to update UI during active session
    private val _currentSessionDuration = mutableStateOf(0L)
    val currentSessionDuration: State<Long> = _currentSessionDuration

    init {
        viewModelScope.launch {
            while(true) {
                activeSession.value?.let {
                   _currentSessionDuration.value = (System.currentTimeMillis() - it.startTime) / 1000
                }
                delay(1000)
            }
        }
    }

    fun checkInManual() {
        viewModelScope.launch { repository.checkIn(isManual = true) }
    }

    fun checkOutManual() {
        viewModelScope.launch { repository.checkOut() }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            userPreferences.setUserName(name)
        }
    }

    fun logOut() {
        viewModelScope.launch {
            userPreferences.clear()
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToProfile: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val monthlySeconds by viewModel.monthlySeconds.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val disciplineScore by viewModel.disciplineScore.collectAsState()
    val currentSessionDuration by viewModel.currentSessionDuration
    val userName by viewModel.userName.collectAsState()
    
    val userGoals by viewModel.userGoals.collectAsState()

    // Calculate totals
    val sessionSeconds = if (activeSession != null) currentSessionDuration else 0L
    val recordedDailySeconds = todayStats?.totalSeconds ?: 0L
    val totalDailyRealSeconds = recordedDailySeconds + sessionSeconds
    
    // Dynamic Goals
    val dailyGoalSeconds = userGoals.dailyGoalHours * 3600L
    val monthlyGoalSeconds = userGoals.monthlyGoalHours * 3600L
    
    val dailyProgress = (totalDailyRealSeconds.toFloat() / dailyGoalSeconds.toFloat()).coerceIn(0f, 1f)
    val monthlyProgress = (monthlySeconds + totalDailyRealSeconds).toFloat() / monthlyGoalSeconds.toFloat()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                         Text(
                            text = if (userName.isNotEmpty()) userName.first().toString().uppercase() else "U",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Self-Discipline Warrior",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Divider()
                NavigationDrawerItem(
                    label = { Text("Edit Profile") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showEditDialog = true
                    },
                    icon = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToProfile()
                    },
                    icon = { Icon(Icons.Default.Settings, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.weight(1f))
                Divider()
                NavigationDrawerItem(
                    label = { Text("Log Out") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.logOut()
                    },
                    icon = { Icon(Icons.Default.ExitToApp, null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    ) {
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                // Profile Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        // Dynamic Greeting
                        val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
                        val greeting = when (currentHour) {
                            in 5..11 -> "Good Morning"
                            in 12..16 -> "Good Afternoon"
                            in 17..20 -> "Good Evening"
                            else -> "Hello"
                        }
                        
                        Text(
                            text = "$greeting, $userName",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Ready to be productive?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Profile Icon (Triggers Drawer)
                    val initial = if (userName.isNotEmpty()) userName.first().toString().uppercase() else "U"

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                scope.launch { drawerState.open() }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (showEditDialog) {
                    var newName by remember { mutableStateOf(userName) }
                    AlertDialog(
                        onDismissRequest = { showEditDialog = false },
                        title = { Text("Edit Profile") },
                        text = {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Name") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (newName.isNotBlank()) {
                                        viewModel.updateUserName(newName)
                                        showEditDialog = false
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Active Session Pulsing Card REMOVED

                    // Circular Progress for Daily
                    Box(contentAlignment = Alignment.Center) {
                        val progressColor = if (totalDailyRealSeconds >= dailyGoalSeconds) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.tertiary

                        CircularProgress(
                            progress = dailyProgress, 
                            color = progressColor,
                            size = 220.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatSeconds(totalDailyRealSeconds),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "/ ${userGoals.dailyGoalHours}h Goal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (activeSession != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Status Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                         Row(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("STATUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val statusText = if (activeSession != null) "IN OFFICE" else "OUT OF OFFICE"
                                val statusColor = if (activeSession != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                
                                // Pulse Effect for IN OFFICE Text
                                val infiniteTransition = rememberInfiniteTransition()
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = if (activeSession != null) 0.5f else 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "pulse"
                                )

                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = statusColor.copy(alpha = if (activeSession != null) alpha else 1f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Button(
                                onClick = {
                                    if (activeSession != null) viewModel.checkOutManual() else viewModel.checkInManual()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activeSession != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    contentColor = if (activeSession != null) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(if (activeSession != null) "Check Out" else "Check In")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Current Streak",
                            value = "$streak Days",
                            icon = "🔥",
                            color = Color(0xFFFF9800)
                        )
                        
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Discipline Score",
                            value = String.format("%.0f%%", disciplineScore),
                            icon = "🛡️",
                            color = Color(0xFF2196F3)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Monthly Progress
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monthly Goal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "${(monthlyProgress * 100).toInt()}%", 
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = monthlyProgress.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp)),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                formatSeconds(monthlySeconds + totalDailyRealSeconds), 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Target: ${userGoals.monthlyGoalHours}h", 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun CircularProgress(progress: Float, color: Color, size: Dp) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress, 
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Box(modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Track
            drawCircle(
                color = color.copy(alpha = 0.2f),
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

fun formatSeconds(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


