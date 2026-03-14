package com.example.officetracker.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Schedule all 3 smart daily reminders. Called on boot and app launch. */
    fun scheduleAllReminders() {
        scheduleMorningReminder()   // 9:00 AM — check in reminder (smart: skips if already in)
        scheduleMiddayReminder()    // 1:00 PM — goal-at-risk warning (smart: skips if progress is fine)
        scheduleEveningReminder()   // 6:00 PM — checkout nudge (smart: only fires if still active)
    }

    /** @deprecated Use scheduleAllReminders() instead */
    fun scheduleMorningReminder() {
        scheduleRepeatingAlarm(
            requestCode = 1001,
            alarmType = ReminderReceiver.ALARM_MORNING,
            hour = 9,
            minute = 0
        )
    }

    fun scheduleMiddayReminder() {
        scheduleRepeatingAlarm(
            requestCode = 1002,
            alarmType = ReminderReceiver.ALARM_MIDDAY,
            hour = 13,
            minute = 0
        )
    }

    fun scheduleEveningReminder() {
        scheduleRepeatingAlarm(
            requestCode = 1003,
            alarmType = ReminderReceiver.ALARM_EVENING,
            hour = 18,
            minute = 0
        )
    }

    fun cancelAllReminders() {
        listOf(1001, 1002, 1003).forEach { code ->
            val intent = Intent(context, ReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context, code, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { alarmManager.cancel(it) }
        }
    }

    private fun scheduleRepeatingAlarm(requestCode: Int, alarmType: String, hour: Int, minute: Int) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_ALARM_TYPE, alarmType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}
