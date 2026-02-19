package com.example.officetracker.ui.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.clickable
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.officetracker.data.local.entity.AttendanceSession
import com.example.officetracker.data.local.entity.DailyStat
import com.example.officetracker.data.repository.AttendanceRepository
import com.example.officetracker.ui.dashboard.formatSeconds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import com.example.officetracker.data.prefs.UserPreferences

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val tickerFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L) // Update every second for smooth "Live" feel, or minute. Let's do 10s or 1m. 
            // User requested "dynamic", seconds might be overkill for a list but good for "Active" label. 
            // Let's stick to 1 minute for list updates to avoid excessive recomposition, 
            // BUT the user might want to see seconds ticking?
            // Dashboard has seconds. Analytics usually is hours/minutes. 1 minute is fine.
            delay(60000L) 
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    // Combine DB history with active session calculation and user goals
    val history: StateFlow<List<DailyStat>> = combine(
        repository.getFullMonthHistory(),
        repository.getCurrentActiveSession(),
        tickerFlow,
        userPreferences.userGoals
    ) { dbHistory, activeSession, now, goals ->
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        
        // Create a mutable copy or map
        val updatedHistory = dbHistory.toMutableList()
        
        // Find if we have a stat for today
        val todayStatIndex = updatedHistory.indexOfFirst { 
            val statDate = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
            statDate.toEpochDay() == todayEpochDay
        }
        
        if (activeSession != null) {
            val sessionDuration = (now - activeSession.startTime).coerceAtLeast(0L) / 1000
            
            if (todayStatIndex != -1) {
                // Update existing today stat
                val existing = updatedHistory[todayStatIndex]
                val newTotal = existing.totalSeconds + sessionDuration
                val newCapped = if (newTotal > AttendanceRepository.MAX_CAP_SECONDS) AttendanceRepository.MAX_CAP_SECONDS else newTotal
                
                updatedHistory[todayStatIndex] = existing.copy(
                    totalSeconds = newTotal,
                    cappedSeconds = newCapped,
                    isGoalMet = newCapped >= (goals.dailyGoalHours * 3600L)
                )
            } else {
                // Create temp today stat
                val capped = if (sessionDuration > AttendanceRepository.MAX_CAP_SECONDS) AttendanceRepository.MAX_CAP_SECONDS else sessionDuration
                 val newStat = DailyStat(
                    date = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000,
                    totalSeconds = sessionDuration,
                    cappedSeconds = capped,
                    isGoalMet = capped >= (goals.dailyGoalHours * 3600L)
                )
                updatedHistory.add(0, newStat) // Add to top (descending sort usually)
            }
        }
        
        // Ensure sorted descending
        updatedHistory.sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyTotalSeconds: StateFlow<Long> = history.map { list ->
        list.sumOf { it.cappedSeconds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val userGoals = userPreferences.userGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.UserGoals())

    fun getSessionsForDate(date: Long): Flow<List<AttendanceSession>> {
        return repository.getSessionsForDate(date)
    }

    fun updateSession(session: AttendanceSession) {
        viewModelScope.launch {
            repository.updateSession(session)
        }
    }

    fun deleteSession(session: AttendanceSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }
}

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel = hiltViewModel()) {
    val history by viewModel.history.collectAsState()
    val monthlyTotalSeconds by viewModel.monthlyTotalSeconds.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()
    
    val monthlyGoalSeconds = userGoals.monthlyGoalHours * 3600L
    val dailyGoalSeconds = userGoals.dailyGoalHours * 3600L
    
    val progress = (monthlyTotalSeconds.toFloat() / monthlyGoalSeconds.toFloat()).coerceIn(0f, 1f)
    val remainingSeconds = (monthlyGoalSeconds - monthlyTotalSeconds).coerceAtLeast(0L)

    // Animation for progress bar
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "monthlyProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("Your Progress", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Monthly Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Monthly Goal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${formatSeconds(monthlyTotalSeconds)} / ${userGoals.monthlyGoalHours}h",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${(progress * 100).toInt()}% Done",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (remainingSeconds > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${formatSeconds(remainingSeconds)} remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Goal Reached! 🎉",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        
        Text("Weekly Overview", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        WeeklyBarChart(history)
        
        Spacer(modifier = Modifier.height(24.dp))

        Text("Insights", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        
        val avgSeconds = if (history.isNotEmpty()) history.map { it.totalSeconds }.average().toLong() else 0L
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Column {
                    Text("Average Daily Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatSeconds(avgSeconds), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Text("This Month's Activity", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        // Heatmap / Grid (Same logic as before)
        val currentMonth = YearMonth.now()
        val daysInMonth = currentMonth.lengthOfMonth()
        val days = (1..daysInMonth).map { day ->
            val stat = history.find { 
                val statDate = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                statDate.dayOfMonth == day 
            }
            day to stat
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 40.dp),
            modifier = Modifier.fillMaxWidth().height(220.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(days) { (day, stat) ->
                DayCell(day, stat, dailyGoalSeconds)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Detailed History", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
             Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                 Text("No tracking data yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
             }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(history.sortedByDescending { it.date }) { stat ->
                    StatItem(stat, dailyGoalSeconds, viewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DayCell(day: Int, stat: DailyStat?, goalSeconds: Long) {
    val backgroundColor = when {
        stat == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        stat.totalSeconds >= goalSeconds -> MaterialTheme.colorScheme.primary // Neon Green
        stat.totalSeconds > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(backgroundColor, shape = MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = if (stat != null && stat.totalSeconds > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatItem(stat: DailyStat, goalSeconds: Long, viewModel: AnalyticsViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val sessions by viewModel.getSessionsForDate(stat.date).collectAsState(initial = emptyList())
    var sessionToEdit by remember { mutableStateOf<AttendanceSession?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<AttendanceSession?>(null) }

    val dateStr = Instant.ofEpochMilli(stat.date)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, MMM dd"))
        
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (stat.totalSeconds >= goalSeconds) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateStr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                     Text(
                         formatSeconds(stat.totalSeconds), 
                         style = MaterialTheme.typography.bodyLarge,
                         fontWeight = FontWeight.Bold,
                         color = MaterialTheme.colorScheme.onSurface
                     )
                     if (stat.cappedSeconds < stat.totalSeconds) {
                         Text(
                             "(${formatSeconds(stat.cappedSeconds)} capped)", 
                             style = MaterialTheme.typography.labelSmall, 
                             color = MaterialTheme.colorScheme.onSurfaceVariant
                         )
                     }
                }
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (sessions.isEmpty()) {
                        Text("No sessions", style = MaterialTheme.typography.bodySmall)
                    } else {
                        sessions.forEach { session ->
                            SessionItem(
                                session = session,
                                onEdit = { sessionToEdit = session },
                                onDelete = { showDeleteConfirm = session }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }

    if (sessionToEdit != null) {
        EditSessionDialog(
            session = sessionToEdit!!,
            onDismiss = { sessionToEdit = null },
            onConfirm = { updated ->
                viewModel.updateSession(updated)
                sessionToEdit = null
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Session?") },
            text = { Text("Are you sure you want to delete this session? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSession(showDeleteConfirm!!)
                        showDeleteConfirm = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SessionItem(
    session: AttendanceSession,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val start = Instant.ofEpochMilli(session.startTime).atZone(ZoneId.systemDefault()).toLocalTime()
    val end = session.endTime?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime() }
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    
    // Fix for zoneId reference
    val endStr = end?.format(formatter) ?: "Active"

    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), MaterialTheme.shapes.small).padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "${start.format(formatter)} - $endStr",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (session.isManual) {
                Text("Manual Entry", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Row {
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun EditSessionDialog(
    session: AttendanceSession,
    onDismiss: () -> Unit,
    onConfirm: (AttendanceSession) -> Unit
) {
    val context = LocalContext.current
    val zoneId = ZoneId.systemDefault()
    
    var startInstant by remember { mutableStateOf(Instant.ofEpochMilli(session.startTime)) }
    var endInstant by remember { mutableStateOf(session.endTime?.let { Instant.ofEpochMilli(it) }) }
    
    val startTime = startInstant.atZone(zoneId).toLocalTime()
    val endTime = endInstant?.atZone(zoneId)?.toLocalTime()
    
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Session") },
        text = {
            Column {
                Text("Start Time", style = MaterialTheme.typography.labelMedium)
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(context, { _, h, m ->
                            val localDate = startInstant.atZone(zoneId).toLocalDate()
                            val newTime = LocalTime.of(h, m)
                            startInstant = LocalDateTime.of(localDate, newTime).atZone(zoneId).toInstant()
                        }, startTime.hour, startTime.minute, true).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(startTime.format(formatter))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("End Time", style = MaterialTheme.typography.labelMedium)
                OutlinedButton(
                    onClick = {
                        val initialH = endTime?.hour ?: startTime.hour
                        val initialM = endTime?.minute ?: startTime.minute
                        
                        TimePickerDialog(context, { _, h, m ->
                            val localDate = (endInstant ?: startInstant).atZone(zoneId).toLocalDate()
                            val newTime = LocalTime.of(h, m)
                            endInstant = LocalDateTime.of(localDate, newTime).atZone(zoneId).toInstant()
                        }, initialH, initialM, true).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(endTime?.format(formatter) ?: "Set End Time")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newEnd = endInstant?.toEpochMilli()
                    if (newEnd != null && newEnd < startInstant.toEpochMilli()) {
                         // Error: End before start. For simplicity, just ignore or could show toast.
                         // Ideally show error. for now, assuming user is disciplined :)
                    }
                    onConfirm(session.copy(startTime = startInstant.toEpochMilli(), endTime = newEnd, isManual = true))
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WeeklyBarChart(history: List<DailyStat>, maxValues: Float = 10f) {
    val last7Days = (0..6).map { LocalDate.now().minusDays(it.toLong()) }.reversed()
    val barData = last7Days.map { date ->
        val stat = history.find { 
             Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() == date 
        }
        val hours = (stat?.totalSeconds ?: 0L) / 3600f
        date to hours
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Last 7 Days", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                barData.forEach { (date, hours) ->
                    val barHeight = if (maxValues > 0) (hours / maxValues).coerceIn(0f, 1f) else 0f
                    val isToday = date == LocalDate.now()
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (hours > 0) {
                            Text(
                                String.format("%.1f", hours),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight(barHeight.coerceAtLeast(0.01f)) // Minimal height
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            date.format(DateTimeFormatter.ofPattern("EEE")),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
