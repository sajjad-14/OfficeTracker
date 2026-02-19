package com.example.officetracker.data.repository

import com.example.officetracker.data.local.dao.AttendanceDao
import com.example.officetracker.data.local.entity.AttendanceSession
import com.example.officetracker.data.local.entity.DailyStat
import kotlinx.coroutines.flow.Flow
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

        sessions.forEach { session ->
            val end = session.endTime
                ?: System.currentTimeMillis() // If still running (edge case), use now, but usually this is called after checkout
            // Safety check: ensure end >= start
            if (end > session.startTime) {
                totalDurationMs += (end - session.startTime)
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

    fun getStreak(): Flow<Int> {
        return attendanceDao.getAllDailyStats().map { stats ->
            var streak = 0
            val today = LocalDate.now()
            var checkDate = today

            // 1. Check if we count today
            val todayStat = stats.find {
                Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() == today
            }

            if (todayStat?.isGoalMet == true) {
                streak++
                checkDate = today.minusDays(1)
            } else {
                // Today not done yet, or empty. Start checking from yesterday.
                checkDate = today.minusDays(1)
            }

            // 2. Check backwards
            // Limit loop to avoid infinite in case of error, though logic is safe
            // We can optimize by iterating the list since it's sorted DESC
            // But finding by date is safer against gaps

            // Optimization: Iterate list to find 'checkDate'.
            var currentTarget = checkDate

            // Filter stats to those before or on checkDate
            val pastStats = stats.filter {
                val d = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                !d.isAfter(currentTarget)
            }.sortedByDescending { it.date }

            for (stat in pastStats) {
                val statDate =
                    Instant.ofEpochMilli(stat.date).atZone(ZoneId.systemDefault()).toLocalDate()

                if (statDate == currentTarget && stat.isGoalMet) {
                    streak++
                    currentTarget = currentTarget.minusDays(1)
                } else if (statDate.isBefore(currentTarget)) {
                    // Gap detected? 
                    // If we found a date BEFORE target, it means target is missing.
                    // Streak breaks.
                    break
                }
                // If statDate == currentTarget but goal not met -> handled by 'else' implicitly as Next iteration won't match or loop ends?
                // Actually need explicit check: if found but not met -> break
                if (statDate == currentTarget && !stat.isGoalMet) break
            }

            streak
        }
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
}
