package com.example.officetracker.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.officetracker.data.prefs.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var geofenceManager: GeofenceManager
    
    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            alarmScheduler.scheduleMorningReminder()
            
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val location = userPreferences.officeLocation.first()
                if (location.isSet) {
                    geofenceManager.addGeofence(location)
                }
            }
        }
    }
}

