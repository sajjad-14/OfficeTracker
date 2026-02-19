package com.example.officetracker.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.officetracker.data.repository.AttendanceRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: AttendanceRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) {
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        val scope = CoroutineScope(Dispatchers.IO)

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            scope.launch {
                repository.checkIn()
                notificationHelper.sendNotification("Checked In", "Welcome to the office! Tracking started.")
            }
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            scope.launch {
                // Check if early exit
                val activeSession = repository.getCurrentActiveSession().first()
                val now = System.currentTimeMillis()
                var message = "See you later! Tracking paused."
                
                if (activeSession != null) {
                    val durationSeconds = (now - activeSession.startTime) / 1000
                    if (durationSeconds < AttendanceRepository.MIN_GOAL_SECONDS) {
                        message = "Warning: Early exit! You've only done ${durationSeconds/3600}h this session."
                    }
                }
                
                repository.checkOut()
                notificationHelper.sendNotification("Checked Out", message)
            }
        }
    }
}

