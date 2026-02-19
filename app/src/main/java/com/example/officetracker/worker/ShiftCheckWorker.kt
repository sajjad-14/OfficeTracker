package com.example.officetracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.officetracker.data.repository.AttendanceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ShiftCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: AttendanceRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Logic to check if user forgot to checkout or check 6h/10h marks if exact timing is missed
        // For simplicity in this demo, we'll verify if active session exceeds 10h cap
        
        val activeSession = repository.getCurrentActiveSession().first()
        if (activeSession != null) {
            val now = System.currentTimeMillis()
            val duration = (now - activeSession.startTime) / 1000
            if (duration >= AttendanceRepository.MAX_CAP_SECONDS) {
                 notificationHelper.sendNotification("10h Limit Reached", "You have utilized your daily cap of 10 hours.")
            } else if (duration >= AttendanceRepository.MIN_GOAL_SECONDS && duration < AttendanceRepository.MIN_GOAL_SECONDS + 900) { // 15 min window
                 notificationHelper.sendNotification("Goal Met", "You have reached your 6h daily goal!")
            }
        }
        return Result.success()
    }
}
