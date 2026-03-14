package com.example.officetracker.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.officetracker.data.local.dao.AttendanceDao
import com.example.officetracker.service.LocationService
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

    @Inject
    lateinit var attendanceDao: AttendanceDao

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            
            // 1. Restore all smart reminders
            alarmScheduler.scheduleAllReminders()
            
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                // 2. Re-register Geofences
                val location = userPreferences.officeLocation.first()
                if (location.isSet) {
                    geofenceManager.addGeofence(location)
                }

                // 3. Resume LocationService if an active session exists
                val activeSession = attendanceDao.getCurrentActiveSession()
                if (activeSession != null) {
                    LocationService.start(context)
                }
            }
        }
    }
}

