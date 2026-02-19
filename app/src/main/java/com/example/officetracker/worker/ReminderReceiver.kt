package com.example.officetracker.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        notificationHelper.sendNotification(
            "Start your day!",
            "It's 8 AM. Time to head to the office and crush your goals!"
        )
    }
}
