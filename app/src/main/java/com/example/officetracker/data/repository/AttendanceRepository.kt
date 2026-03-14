package com.example.officetracker.data.repository

import com.example.officetracker.data.local.dao.AttendanceDao
import com.example.officetracker.data.local.entity.AttendanceSession
import com.example.officetracker.data.local.entity.DailyStat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import com.example.officetracker.data.prefs.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Singleton
class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) {
    // 6 hours in seconds = 6 * 3600 = 21600
    // 10 hours in seconds = 10 * 3600 = 36000
    companion object {
        const val MIN_GOAL_SECONDS = 21600L
        const val MAX_CAP_SECONDS = 36000L
        const val MONTHLY_GOAL_HOURS = 72L
    }

    fun getTodayStats(): Flow<DailyStat?> {
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        return attendanceDao.getDailyStat(today)
    }

    fun getMonthlyCompletedSeconds(): Flow<Long> {
        val now = LocalDate.now()
        val startOfMonth =
            now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        val endOfMonth =
            now.plusMonths(1).withDayOfMonth(1).minusDays(1).atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond() * 1000
        return attendanceDao.getTotalCappedSecondsRange(startOfMonth, endOfMonth).map { it ?: 0L }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMonthlyStatsIncludingActive(): Flow<Pair<Long, Int>> {
        val ticker = flow {
            while (true) {
                emit(Unit)
                delay(60000) // Re-calculate every minute
            }
        }

        return combine(ticker, getCurrentActiveSession()) { _, active ->
            active
        }.flatMapLatest { active ->
            val now = LocalDate.now()
            val startOfMonth =
                now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
            val endOfMonth = now.plusMonths(1).withDayOfMonth(1).minusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000

            attendanceDao.getStatsRange(startOfMonth, endOfMonth).map { stats ->
                val today = LocalDate.now()

                val recordedCappedSeconds = stats.sumOf { it.cappedSeconds }

                val recordedPresenceCount = stats.count { it.totalSeconds > 0 }
                val todayHasRecordedPresence = stats.any {
                    it.totalSeconds > 0 &&
                            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault())
                                .toLocalDate() == today
                }

                val totalPresenceCount = if (active != null && !todayHasRecordedPresence) {
                    recordedPresenceCount + 1
                } else {
                    recordedPresenceCount
                }

                Pair(recordedCappedSeconds, totalPresenceCount)
            }
        }
    }

    fun getMonthlyPresenceCount(): Flow<Int> {
         return getMonthlyStatsIncludingActive().map { it.second }
    }

    suspend fun checkIn(isManual: Boolean = false) {
        val active = attendanceDao.getCurrentActiveSession()
        if (active != null) return // Already checked in

        val now = System.currentTimeMillis()
        val today = getStartOfDay(now)
        val session = AttendanceSession(
            date = today,
            startTime = now,
            isManual = isManual
        )
        attendanceDao.insertSession(session)
    }

    suspend fun addPastSession(startTime: Long, endTime: Long) {
        val date = getStartOfDay(startTime)
        val session = AttendanceSession(
            date = date,
            startTime = startTime,
            endTime = endTime,
            isManual = true
        )
        attendanceDao.insertSession(session)
        recalculateDailyStats(date)
    }

    suspend fun checkOut() {
        val active = attendanceDao.getCurrentActiveSession() ?: return
        val now = System.currentTimeMillis()

        // Update session
        val updatedSession = active.copy(endTime = now)
        attendanceDao.updateSession(updatedSession)

        // Recalculate Daily Stats
        recalculateDailyStats(active.date)
    }

    suspend fun updateSession(session: AttendanceSession) {
        attendanceDao.updateSession(session)
        recalculateDailyStats(session.date)
    }

    suspend fun deleteSession(session: AttendanceSession) {
        attendanceDao.deleteSession(session)
        recalculateDailyStats(session.date)
    }

    fun getSessionsForDate(date: Long): Flow<List<AttendanceSession>> {
        return attendanceDao.getSessionsForDate(date)
    }

    private suspend fun recalculateDailyStats(date: Long) {
        val sessions = attendanceDao.getSessionsForDateSync(date)
        var totalDurationMs = 0L

        if (sessions.isNotEmpty()) {
            val firstStart = sessions.minOf { it.startTime }
            
            // If any session doesn't have an end time, it means they are currently "In Office"
            val hasActiveSession = sessions.any { it.endTime == null }
            val lastEnd = if (hasActiveSession) {
                System.currentTimeMillis()
            } else {
                sessions.maxOf { it.endTime ?: it.startTime }
            }

            if (lastEnd > firstStart) {
                totalDurationMs = lastEnd - firstStart
            }
        }

        // Get dynamic goal
        val goals = userPreferences.userGoals.first()
        val minGoalSeconds = goals.dailyGoalHours * 3600L

        val totalSeconds = totalDurationMs / 1000
        val cappedSeconds = if (totalSeconds > MAX_CAP_SECONDS) MAX_CAP_SECONDS else totalSeconds

        val stat = DailyStat(
            date = date,
            totalSeconds = totalSeconds,
            cappedSeconds = cappedSeconds,
            isGoalMet = cappedSeconds >= minGoalSeconds
        )
        attendanceDao.insertOrUpdateDailyStat(stat)
    }

    private fun getStartOfDay(timestamp: Long): Long {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond() * 1000
    }

    fun getFullMonthHistory(): Flow<List<DailyStat>> {
        val now = LocalDate.now()
        val startOfMonth =
            now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
        val endOfMonth = now.plusMonths(1).atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond() * 1000 // Until start of next month
        return attendanceDao.getStatsRange(startOfMonth, endOfMonth)
    }

    fun getCurrentActiveSession(): Flow<AttendanceSession?> {
        // Since Room queries with limit 1 return a list or object, and we want to observe "is active"
        // We'll observe all sessions and filter. Or simpler, just query active.
        // But for Flow, let's observe all sessions for today descending and pick first if undefined end.
        return attendanceDao.getAllSessions().map { list ->
            list.firstOrNull { it.endTime == null }
        }
    }

    fun getAllSessions(): Flow<List<AttendanceSession>> {
        return attendanceDao.getAllSessions()
    }

    fun getDisciplineScore(): Flow<Float> {
        return attendanceDao.getAllDailyStats().map { stats ->
            val totalDaysWithWork = stats.count { it.totalSeconds > 0 }
            if (totalDaysWithWork == 0) 0f else {
                val daysMet = stats.count { it.isGoalMet }
                (daysMet.toFloat() / totalDaysWithWork.toFloat()) * 100f
            }
        }
    }

    suspend fun exportDataToCsv(uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        // Header
                        writer.write("Date,Start Time,End Time,Duration (Hours),Is Manual\n")

                        // Data
                        val sessions = attendanceDao.getAllSessions().first()
                        val zoneId = ZoneId.systemDefault()

                        sessions.forEach { session ->
                            val date =
                                Instant.ofEpochMilli(session.date).atZone(zoneId).toLocalDate()
                            val start = Instant.ofEpochMilli(session.startTime).atZone(zoneId)
                                .toLocalTime()
                            val end = session.endTime?.let {
                                Instant.ofEpochMilli(it).atZone(zoneId).toLocalTime()
                            }

                            val durationSeconds = if (session.endTime != null) {
                                (session.endTime - session.startTime) / 1000
                            } else {
                                0 // Active session
                            }
                            val durationHours = String.format("%.2f", durationSeconds / 3600f)

                            writer.write("$date,$start,${end ?: "Active"},$durationHours,${session.isManual}\n")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }


    suspend fun clearAllData() {
        attendanceDao.deleteAllSessions()
        attendanceDao.deleteAllDailyStats()
        userPreferences.clear()
        // Note: Geofences should be cleared by ViewModel calling GeofenceManager
    }

    suspend fun injectMockData() {
        // Clear old first for a fresh start
        attendanceDao.deleteAllSessions()
        attendanceDao.deleteAllDailyStats()

        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        
        // Generate last 60 days
        for (i in 60 downTo 1) {
            val date = today.minusDays(i.toLong())
            // Skip weekends mostly
            if (date.dayOfWeek.value == 6 || date.dayOfWeek.value == 7) {
                if (Math.random() > 0.1) continue // 10% chance to work on weekend
            }

            val dateMillis = date.atStartOfDay(zoneId).toEpochSecond() * 1000
            
            // Random duration between 4 and 11 hours (to hit all red/green thresholds)
            val durationHours = 4 + (Math.random() * 7).toFloat()
            val durationMs = (durationHours * 3600 * 1000).toLong()

            // Random start time between 8 AM and 10 AM
            val startHour = 8 + (Math.random() * 2).toInt()
            val startTime = date.atTime(startHour, 0).atZone(zoneId).toEpochSecond() * 1000
            val endTime = startTime + durationMs

            val session = AttendanceSession(
                date = dateMillis,
                startTime = startTime,
                endTime = endTime,
                isManual = true
            )
            attendanceDao.insertSession(session)
            recalculateDailyStats(dateMillis)
        }
    }
}
