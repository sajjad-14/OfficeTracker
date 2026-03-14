package com.example.officetracker.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.officetracker.data.local.dao.AttendanceDao
import com.example.officetracker.data.repository.AttendanceRepository
import com.example.officetracker.data.prefs.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var repository: AttendanceRepository
    @Inject lateinit var attendanceDao: AttendanceDao
    @Inject lateinit var userPreferences: UserPreferences

    companion object {
        const val EXTRA_ALARM_TYPE = "alarm_type"
        const val ALARM_MORNING    = "morning"
        const val ALARM_MIDDAY     = "midday"
        const val ALARM_EVENING    = "evening"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmType = intent.getStringExtra(EXTRA_ALARM_TYPE) ?: ALARM_MORNING

        // Use a coroutine to check DB state before sending the notification
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val settings = userPreferences.notificationSettings.firstOrNull()
                ?: UserPreferences.NotificationSettings(true, true, true)

            val activeSession = attendanceDao.getCurrentActiveSession()
            val today = LocalDate.now()
            val todayMs = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
            val todayStat = repository.getTodayStats().firstOrNull()

            when (alarmType) {
                ALARM_MORNING -> {
                    // Only notify if enabled AND user is NOT already checked in
                    if (settings.morningEnabled && activeSession == null) {
                        notificationHelper.sendNotification(
                            title = "🏢 Time to head in!",
                            message = "You haven't checked in yet. Don't fall behind on your daily goal!"
                        )
                    }
                }

                ALARM_MIDDAY -> {
                    // Only notify if enabled AND user is not in office AND today's recorded hours are less than half the daily goal
                    val recordedSeconds = todayStat?.totalSeconds ?: 0L
                    val halfGoalSeconds = 3L * 3600 // 3 hours = half of 6h goal
                    if (settings.middayEnabled && activeSession == null && recordedSeconds < halfGoalSeconds) {
                        notificationHelper.sendNotification(
                            title = "⚠️ Goal at risk!",
                            message = "It's midday and you're not in the office yet. You may miss today's attendance goal!"
                        )
                    }
                }

                ALARM_EVENING -> {
                    // Only notify if enabled AND remind to check out if still active at end of day
                    if (settings.eveningEnabled && activeSession != null) {
                        val startedHoursAgo = (System.currentTimeMillis() - activeSession.startTime) / 3600000
                        notificationHelper.sendNotification(
                            title = "👋 Time to wrap up!",
                            message = "You've been in the office for ${startedHoursAgo}h. Don't forget to check out!"
                        )
                    }
                }
            }
        }
    }
}
