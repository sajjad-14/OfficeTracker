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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
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

    val monthlyStats: StateFlow<Pair<Long, Int>> = repository.getMonthlyStatsIncludingActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0L, 0))

    val daysInOffice: StateFlow<Int> = repository.getDaysInOfficeThisMonth()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    val disciplineScore: StateFlow<Float> = repository.getDisciplineScore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val userGoals = userPreferences.userGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.UserGoals())

    val userName = userPreferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val todaySessions: StateFlow<List<AttendanceSession>> = repository.getSessionsForDate(
        java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toEpochSecond() * 1000
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyStats: StateFlow<List<DailyStat>> = repository.getStatsRange(
        java.time.LocalDate.now().minusDays(6).atStartOfDay(java.time.ZoneId.systemDefault()).toEpochSecond() * 1000,
        java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toEpochSecond() * 1000
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val tickerFlow = flow {
        while (true) {
            emit(Unit)
            delay(30000)
        }
    }

    val currentSessionDuration: StateFlow<Long> = combine(
        activeSession,
        todaySessions,
        tickerFlow
    ) { active, sessions, _ ->
        if (active != null) {
            val firstStart = sessions.minOfOrNull { it.startTime } ?: active.startTime
            (System.currentTimeMillis() - firstStart) / 1000
        } else {
            0L
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

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
    val daysInOffice by viewModel.daysInOffice.collectAsState()
    val disciplineScore by viewModel.disciplineScore.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val currentSessionDuration by viewModel.currentSessionDuration.collectAsState()
    
    // Total calculation moved to sub-components
    
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
                    
                    // Circular Progress for Daily (Live Update)
                    DailyProgressSection(
                        currentSessionDuration = currentSessionDuration,
                        activeSession = activeSession,
                        todayStats = todayStats,
                        dailyGoalHours = userGoals.dailyGoalHours
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Status Card
                    Card(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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
                            
                            // Bounce Button Animation
                            val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.92f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "button_bounce"
                            )

                            Button(
                                onClick = {
                                    if (activeSession != null) viewModel.checkOutManual() else viewModel.checkInManual()
                                },
                                interactionSource = interactionSource,
                                modifier = Modifier.scale(scale),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activeSession != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    contentColor = if (activeSession != null) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(if (activeSession != null) "Check Out" else "Check In", fontWeight = FontWeight.Bold)
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
                            title = "Days in Office",
                            value = "$daysInOffice",
                            icon = "🏢",
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

                    // Monthly Progress (Live Update)
                    MonthlyProgressSection(
                        currentSessionDuration = currentSessionDuration,
                        activeSession = activeSession,
                        todayStats = todayStats,
                        monthlySeconds = monthlySeconds,
                        monthlyGoalHours = userGoals.monthlyGoalHours
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val weeklyStats by viewModel.weeklyStats.collectAsState()
                    if (weeklyStats.isNotEmpty()) {
                        WeeklySummarySection(stats = weeklyStats)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    val sessions by viewModel.todaySessions.collectAsState()
                    if (sessions.isNotEmpty()) {
                        TodayLogSection(sessions = sessions)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DailyProgressSection(
    currentSessionDuration: Long,
    activeSession: AttendanceSession?,
    todayStats: DailyStat?,
    dailyGoalHours: Int
) {
    
    // When a session is active, currentSessionDuration = (now - firstEntry) for today.
    // This already includes whatever is in todayStats.totalSeconds for completed sessions.
    // So we just use it directly when active; otherwise use what's recorded in the DB.
    val totalDailyRealSeconds = if (activeSession != null) currentSessionDuration else (todayStats?.totalSeconds ?: 0L)
    val dailyGoalSeconds = dailyGoalHours * 3600L
    val dailyProgress = (totalDailyRealSeconds.toFloat() / dailyGoalSeconds.toFloat()).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val size = minOf(maxWidth * 0.7f, 240.dp)
        
        val progressColor = if (totalDailyRealSeconds >= dailyGoalSeconds) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.tertiary

        CircularProgress(
            progress = dailyProgress, 
            color = progressColor,
            size = size
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatSeconds(totalDailyRealSeconds),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "/ ${dailyGoalHours}h Goal",
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
}

@Composable
fun MonthlyProgressSection(
    currentSessionDuration: Long,
    activeSession: AttendanceSession?,
    todayStats: DailyStat?,
    monthlySeconds: Long,
    monthlyGoalHours: Int
) {
    
    // When session is active, currentSessionDuration = firstEntry->now for today.
    // That's the live daily real seconds. Otherwise use what's recorded.
    val todayTotalLive = if (activeSession != null) currentSessionDuration else (todayStats?.totalSeconds ?: 0L)
    val todayTotalCapped = minOf(todayTotalLive, 36000L) // Cap at 10h
    
    val monthlyGoalSeconds = monthlyGoalHours * 3600L
    
    // monthlySeconds includes today's already-recorded cappedSeconds from DB.
    // Replace it with the live value for today.
    val todayRecordedCapped = todayStats?.cappedSeconds ?: 0L
    val monthlyExcToday = (monthlySeconds - todayRecordedCapped).coerceAtLeast(0L)
    val finalMonthlySeconds = monthlyExcToday + todayTotalCapped
    val monthlyProgress = finalMonthlySeconds.toFloat() / monthlyGoalSeconds.toFloat()

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
                formatSeconds(finalMonthlySeconds), 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Target: ${monthlyGoalHours}h", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    
    val gradientColors = listOf(
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )
    
    val sweepGradient = Brush.sweepGradient(gradientColors)
    
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 28.dp.toPx()
            // Track
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            // Progress
            drawArc(
                brush = sweepGradient,
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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

@Composable
fun WeeklySummarySection(stats: List<DailyStat>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Weekly Goal Status", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ensure we have exactly 7 days, padding with empty stats if needed
                val today = java.time.LocalDate.now()
                val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }
                
                last7Days.forEach { date ->
                    val stat = stats.find { 
                        java.time.Instant.ofEpochMilli(it.date).atZone(java.time.ZoneId.systemDefault()).toLocalDate() == date 
                    }
                    WeeklyDayItem(date = date, stat = stat)
                }
            }
        }
    }
}

@Composable
fun WeeklyDayItem(date: java.time.LocalDate, stat: DailyStat?) {
    val dayName = date.dayOfWeek.name.take(1)
    val color = when {
        stat == null || stat.totalSeconds == 0L -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        stat.isGoalMet -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
    }
    
    val isToday = date == java.time.LocalDate.now()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color, androidx.compose.foundation.shape.CircleShape)
                .then(
                    if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, androidx.compose.foundation.shape.CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (stat != null && stat.isGoalMet) {
                Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TodayLogSection(sessions: List<AttendanceSession>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Today's Log", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                sessions.reversed().forEachIndexed { index, session ->
                    TodayLogItem(session = session)
                    if (index < sessions.size - 1) {
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodayLogItem(session: AttendanceSession) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    if (session.endTime == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    androidx.compose.foundation.shape.CircleShape
                )
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (session.endTime == null) "Arrival (Active)" else "Visit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${formatTime(session.startTime)} — ${session.endTime?.let { formatTime(it) } ?: "Now"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (session.isManual) {
            Text(
                "Manual",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.tertiaryContainer, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

fun formatTime(millis: Long): String {
    val instant = java.time.Instant.ofEpochMilli(millis)
    val time = instant.atZone(java.time.ZoneId.systemDefault()).toLocalTime()
    return time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
}
